package com.tinyflow.graph.node;

import com.tinyflow.model.AgentGraphState;
import com.tinyflow.model.TaskResult;
import com.tinyflow.model.TaskSpec;
import com.tinyflow.service.EventEmitter;
import com.tinyflow.tool.Tool;
import com.tinyflow.tool.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Execute node - executes tasks (subagents or direct LLM calls).
 * Compatible with LangGraph4J.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecuteNode implements GraphNode {

    private final ChatClient chatClient;
    private final ToolRegistry toolRegistry;

    // Pattern to detect tool calls in LLM response
    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile(
            "<tool_call>\\s*\\{\\s*\"name\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"arguments\"\\s*:\\s*(\\{[^}]+\\})\\s*\\}\\s*</tool_call>",
            Pattern.DOTALL
    );

    @Override
    public Map<String, Object> execute(AgentGraphState state) {
        String threadId = state.metadata().get("thread_id").toString();
        List<TaskSpec> pendingTasks = state.pendingTasks();
        int pendingTaskCount = pendingTasks != null ? pendingTasks.size() : 0;
        log.info("ExecuteNode executing for thread: {}, pendingTasks: {}", threadId, pendingTaskCount);
        
        List<TaskResult> completedTasks = new ArrayList<>();

        if (pendingTasks == null || pendingTasks.isEmpty()) {
            // No pending tasks, execute directly with LLM
            log.info("ExecuteNode: No pending tasks for thread: {}, executing directly", threadId);
            String response = executeDirect(state);
            log.info("ExecuteNode completed for thread: {}, direct execution output length: {}, routing to: reflector", 
                    threadId, response.length());
            return Map.of(
                    "previousRoundOutput", response,
                    "route", "reflector"
            );
        }

        // Execute each pending task
        EventEmitter emitter = EventEmitter.get(state.metadata().get("thread_id").toString());
        List<Map<String, Object>> updatedTodos = new ArrayList<>();

        // Initialize todos from state
        if (state.todos() != null) {
            for (var todo : state.todos()) {
                Map<String, Object> todoMap = new HashMap<>();
                todoMap.put("id", todo.getId());
                todoMap.put("content", todo.getContent());
                todoMap.put("status", "in_progress");
                todoMap.put("error", todo.getError() != null ? todo.getError() : null);
                updatedTodos.add(todoMap);
            }
        }

        // Emit initial todo_update with in_progress status
        if (emitter != null && !updatedTodos.isEmpty()) {
            emitter.emitTodoUpdate(updatedTodos);
        }

        for (TaskSpec task : pendingTasks) {
            TaskResult result = executeTask(task, state);
            completedTasks.add(result);

            // Update todo status
            String todoStatus = result.getStatus() == TaskResult.Status.COMPLETED ? "completed" :
                    result.getStatus() == TaskResult.Status.FAILED ? "failed" : "completed";
            String todoError = result.getStatus() == TaskResult.Status.FAILED ? result.getError() : null;

            // Find and update the corresponding todo
            for (int i = 0; i < updatedTodos.size(); i++) {
                Map<String, Object> todo = updatedTodos.get(i);
                if (task.getDescription().equals(todo.get("content"))) {
                    Map<String, Object> updatedTodo = new HashMap<>();
                    updatedTodo.put("id", todo.get("id"));
                    updatedTodo.put("content", todo.get("content"));
                    updatedTodo.put("status", todoStatus);
                    updatedTodo.put("error", todoError);
                    updatedTodos.set(i, updatedTodo);
                    break;
                }
            }

            // Emit todo_update event
            if (emitter != null) {
                emitter.emitTodoUpdate(updatedTodos);
            }

            // Emit subagent_result event
            if (emitter != null) {
                String status = result.getStatus().name().toLowerCase();
                String label;
                if (result.getStatus() == TaskResult.Status.COMPLETED) {
                    label = String.format("研究完成 (%.1fs)", result.getDurationSeconds());
                } else if (result.getStatus() == TaskResult.Status.TIMED_OUT) {
                    label = "任务超时";
                } else {
                    label = "任务失败";
                }
                emitter.emitSubagentResult(result.getTaskId(), status, label);
            }
        }

        // Combine results
        String combinedOutput = completedTasks.stream()
                .map(TaskResult::getOutput)
                .collect(Collectors.joining("\n\n---\n\n"));

        String route = "ultra".equals(state.executionMode()) ? "merge" : "reflector";
        log.info("ExecuteNode completed for thread: {}, executed {} tasks, output length: {}, routing to: {}", 
                threadId, completedTasks.size(), combinedOutput.length(), route);
        
        return Map.of(
                "pendingTasks", new ArrayList<>(),
                "completedTasks", completedTasks,
                "previousRoundOutput", combinedOutput,
                "route", route
        );
    }

    private static final int MAX_REACT_ITERATIONS = 6;

    private String executeDirect(AgentGraphState state) {
        String conversation = state.messages().stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        String memoryContext = state.memoryContext();
        EventEmitter emitter = EventEmitter.get(state.metadata().get("thread_id").toString());

        // Build conversation history for ReAct loop
        List<String> conversationHistory = new ArrayList<>();

        StringBuilder systemPrompt = new StringBuilder();
        if (memoryContext != null && !memoryContext.isEmpty()) {
            systemPrompt.append(memoryContext).append("\n\n");
        }

        // Add current date context (matching Python)
        systemPrompt.append(getCurrentDateContext()).append("\n\n");

        // Add tool instructions
        systemPrompt.append("你是一个智能助手，可以使用以下工具完成任务。\n");
        systemPrompt.append("搜索时必须在查询中包含今天的日期以获取最新信息。\n");
        systemPrompt.append("每次只调用一个工具，根据结果决定下一步。\n");
        systemPrompt.append("当你认为任务完成时，直接回复最终答案（不调用工具）。\n\n");
        systemPrompt.append("可用工具：\n");
        toolRegistry.getAllTools().forEach((name, tool) -> {
            systemPrompt.append("- ").append(name).append(": ").append(tool.getDescription()).append("\n");
        });
        systemPrompt.append("\n如果需要使用工具，请按以下格式输出：\n");
        systemPrompt.append("<tool_call>{\"name\": \"tool_name\", \"arguments\": {\"param\": \"value\"}}</tool_call>\n\n");

        conversationHistory.add("SYSTEM: " + systemPrompt.toString());
        conversationHistory.add("USER: " + conversation);

        try {
            return reactLoop(conversationHistory, emitter);

        } catch (Exception e) {
            log.error("Direct execution failed: {}", e.getMessage());
            return "执行失败: " + e.getMessage();
        }
    }

    /**
     * Get current date/time context string (matching Python implementation).
     */
    private String getCurrentDateContext() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
        String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return String.format("当前时间：%d年%02d月%02d日 %s %02d:%02d (Asia/Shanghai)",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                weekdays[now.getDayOfWeek().getValue() - 1],
                now.getHour(), now.getMinute());
    }

    /**
     * ReAct loop: Think → Act → Observe, repeat until done (matching Python SubagentRunner).
     */
    private String reactLoop(List<String> conversationHistory, EventEmitter emitter) {
        StringBuilder fullConversation = new StringBuilder();
        for (String msg : conversationHistory) {
            fullConversation.append(msg).append("\n");
        }

        for (int iteration = 0; iteration < MAX_REACT_ITERATIONS; iteration++) {
            log.info("ReAct iteration {}/{}", iteration + 1, MAX_REACT_ITERATIONS);

            String response = chatClient.prompt()
                    .user(fullConversation.toString())
                    .call()
                    .content();

            fullConversation.append("ASSISTANT: ").append(response).append("\n");

            // Check for tool calls
            List<ToolCall> toolCalls = extractToolCalls(response);

            if (toolCalls.isEmpty()) {
                // No tool calls → final answer
                return response;
            }

            // Execute tool calls and add results to conversation
            for (ToolCall tc : toolCalls) {
                // Emit tool_call event
                if (emitter != null) {
                    emitter.emitToolCall(tc.name, tc.getQuery(), "");
                }

                // Execute tool
                Tool tool = toolRegistry.getTool(tc.name);
                String toolResult;
                if (tool != null) {
                    toolResult = tool.execute(tc.arguments);
                } else {
                    toolResult = "{\"error\": \"Tool not found: " + tc.name + "\"}";
                }

                // Emit tool_result event
                if (emitter != null) {
                    String preview = toolResult.length() > 200 ? toolResult.substring(0, 200) + "..." : toolResult;
                    emitter.emitToolResult(tc.name, preview);
                }

                // Add tool result to conversation (as tool response)
                String toolResponse = String.format("[工具结果: %s]\n%s", tc.name, toolResult);
                fullConversation.append("TOOL: ").append(toolResponse).append("\n");
            }
        }

        // Max iterations reached — force a final summary
        log.warn("ReAct hit max_iterations ({}), forcing summary", MAX_REACT_ITERATIONS);
        fullConversation.append("USER: 已达到最大迭代次数，请根据目前收集到的信息给出最终回答。\n");

        String finalResponse = chatClient.prompt()
                .user(fullConversation.toString())
                .call()
                .content();

        return finalResponse;
    }

    /**
     * Tool call representation.
     */
    private static class ToolCall {
        final String name;
        final Map<String, Object> arguments;

        ToolCall(String name, Map<String, Object> arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        String getQuery() {
            Object query = arguments.get("query");
            return query != null ? query.toString() : "";
        }
    }

    /**
     * Extract tool calls from LLM response.
     */
    private List<ToolCall> extractToolCalls(String response) {
        List<ToolCall> toolCalls = new ArrayList<>();
        Matcher matcher = TOOL_CALL_PATTERN.matcher(response);

        while (matcher.find()) {
            String toolName = matcher.group(1);
            String argsJson = matcher.group(2);
            Map<String, Object> args = parseArgs(argsJson);
            toolCalls.add(new ToolCall(toolName, args));
        }

        return toolCalls;
    }

    private String extractQueryFromArgs(String argsJson) {
        try {
            // Simple extraction of query parameter
            if (argsJson.contains("\"query\"")) {
                int start = argsJson.indexOf("\"query\"") + 8;
                while (start < argsJson.length() && (argsJson.charAt(start) == '"' || argsJson.charAt(start) == ':' || argsJson.charAt(start) == ' ')) {
                    start++;
                }
                int end = argsJson.indexOf('"', start);
                if (end > start) {
                    return argsJson.substring(start, end);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract query from args");
        }
        return "";
    }

    private Map<String, Object> parseArgs(String argsJson) {
        Map<String, Object> params = new HashMap<>();
        try {
            // Simple JSON parsing for basic cases
            argsJson = argsJson.trim();
            if (argsJson.startsWith("{") && argsJson.endsWith("}")) {
                argsJson = argsJson.substring(1, argsJson.length() - 1);
                String[] pairs = argsJson.split(",");
                for (String pair : pairs) {
                    String[] kv = pair.split(":", 2);
                    if (kv.length == 2) {
                        String key = kv[0].trim().replace("\"", "");
                        String value = kv[1].trim();
                        // Try to parse as number
                        try {
                            if (value.contains(".")) {
                                params.put(key, Double.parseDouble(value));
                            } else {
                                params.put(key, Integer.parseInt(value));
                            }
                        } catch (NumberFormatException e) {
                            // Treat as string
                            params.put(key, value.replace("\"", ""));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse args: {}", argsJson);
        }
        return params;
    }

    private TaskResult executeTask(TaskSpec task, AgentGraphState state) {
        String conversation = state.messages().stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        StringBuilder prompt = new StringBuilder();

        // Add skill system prompt if available
        if (task.getSkillSystemPrompt() != null) {
            prompt.append(task.getSkillSystemPrompt()).append("\n\n");
        }

        prompt.append("任务: ").append(task.getDescription()).append("\n\n");
        prompt.append("上下文:\n").append(conversation);

        long startTime = System.currentTimeMillis();

        try {
            String response = chatClient.prompt()
                    .user(prompt.toString())
                    .call()
                    .content();

            long duration = (System.currentTimeMillis() - startTime) / 1000;

            return TaskResult.builder()
                    .taskId(task.getId())
                    .status(TaskResult.Status.COMPLETED)
                    .output(response)
                    .durationSeconds(duration)
                    .skillName(task.getSkillName())
                    .build();

        } catch (Exception e) {
            long duration = (System.currentTimeMillis() - startTime) / 1000;

            return TaskResult.builder()
                    .taskId(task.getId())
                    .status(TaskResult.Status.FAILED)
                    .error(e.getMessage())
                    .durationSeconds(duration)
                    .skillName(task.getSkillName())
                    .build();
        }
    }

}
