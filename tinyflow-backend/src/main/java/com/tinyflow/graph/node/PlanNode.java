package com.tinyflow.graph.node;

import com.tinyflow.model.AgentGraphState;
import com.tinyflow.model.Message;
import com.tinyflow.model.TodoItem;
import com.tinyflow.service.EventEmitter;
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
 * Plan node - generates execution plan with TODO items.
 * Compatible with LangGraph4J.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanNode implements GraphNode {

    private final ChatClient chatClient;

    private static final String PLAN_PROMPT = """
            %s
            
            请为以下用户请求制定执行计划。
            将任务分解为具体的 TODO 步骤。
            
            对话：
            %s
            
            请按以下格式输出 TODO 列表：
            TODO:
            1. [步骤1描述]
            2. [步骤2描述]
            ...
            
            只输出 TODO 列表，不要其他内容。""";

    private static final Pattern TODO_PATTERN = Pattern.compile("^\\d+\\.\\s*(.+)$", Pattern.MULTILINE);

    @Override
    public Map<String, Object> execute(AgentGraphState state) {
        String conversation = formatConversation(state);
        String memoryContext = state.memoryContext();

        String prompt = String.format(PLAN_PROMPT,
                memoryContext != null ? memoryContext : "",
                conversation);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            List<TodoItem> todos = parseTodos(response);

            // Emit todo_update event
            EventEmitter emitter = EventEmitter.get(state.metadata().get("thread_id").toString());
            if (emitter != null && !todos.isEmpty()) {
                List<Map<String, Object>> todoData = todos.stream()
                        .map(t -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", t.getId());
                            map.put("content", t.getContent());
                            map.put("status", t.getStatus().name().toLowerCase());
                            map.put("error", t.getError() != null ? t.getError() : null);
                            return map;
                        })
                        .collect(Collectors.toList());
                emitter.emitTodoUpdate(todoData);
            }

            return Map.of(
                    "todos", todos,
                    "route", "dispatch"
            );

        } catch (Exception e) {
            log.error("Plan node failed: {}", e.getMessage());
            // Return with empty todos, will fall back to skill node
            return Map.of(
                    "todos", new ArrayList<>(),
                    "route", "skill_node"
            );
        }
    }

    private String formatConversation(AgentGraphState state) {
        return state.messages().stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
    }

    private List<TodoItem> parseTodos(String response) {
        List<TodoItem> todos = new ArrayList<>();
        Matcher matcher = TODO_PATTERN.matcher(response);

        while (matcher.find()) {
            String content = matcher.group(1).trim();
            if (!content.isEmpty()) {
                todos.add(TodoItem.builder()
                        .content(content)
                        .status(TodoItem.Status.PENDING)
                        .build());
            }
        }

        return todos;
    }

}
