package com.tinyflow.graph.node;

import com.tinyflow.config.TinyFlowProperties;
import com.tinyflow.model.AgentGraphState;
import com.tinyflow.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reflector node - reviews execution and decides next step.
 * Compatible with LangGraph4J.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReflectorNode implements GraphNode {

    private final ChatClient chatClient;
    private final TinyFlowProperties properties;

    private static final String REFLECTOR_PROMPT = """
            请审查以下执行结果，判断是否需要继续执行更多任务。
            
            原始问题: %s
            
            当前执行结果:
            %s
            
            请回复以下之一：
            - SATISFIED: 结果已足够，可以结束
            - CONTINUE: 需要继续执行更多任务
            
            只回复上述标签，不要其他内容。""";

    @Override
    public Map<String, Object> execute(AgentGraphState state) {
        String threadId = state.metadata().get("thread_id").toString();
        int iteration = state.iteration();
        String previousOutput = state.previousRoundOutput();
        boolean isOutputEmpty = previousOutput == null || previousOutput.trim().isEmpty();
        
        log.info("ReflectorNode executing for thread: {}, iteration: {}/{}, previousRoundOutput empty: {}", 
                threadId, iteration, properties.getGraph().getMaxIterations(), isOutputEmpty);
        
        // Check if max iterations reached
        int maxIterations = properties.getGraph().getMaxIterations();
        if (iteration >= maxIterations) {
            log.warn("Max iterations reached for thread: {}, ending execution", threadId);
            return finalizeState(state);
        }

        // Check if loop was terminated
        if (state.loopTerminated()) {
            log.info("Loop was terminated for thread: {}, ending execution", threadId);
            return finalizeState(state);
        }

        String originalQuery = getLastUserMessage(state);
        String executionOutput = previousOutput;

        String prompt = String.format(REFLECTOR_PROMPT, originalQuery, executionOutput);

        try {
            String decision = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content()
                    .trim()
                    .toUpperCase();

            if (decision.contains("CONTINUE")) {
                log.info("ReflectorNode decided to CONTINUE execution for thread: {}", threadId);
                return Map.of("route", "continue_execute");
            } else {
                log.info("ReflectorNode SATISFIED with results for thread: {}, finalizing state", threadId);
                return finalizeState(state);
            }

        } catch (Exception e) {
            log.error("ReflectorNode failed for thread: {}, defaulting to end: {}", threadId, e.getMessage());
            return finalizeState(state);
        }
    }

    private Map<String, Object> finalizeState(AgentGraphState state) {
        String output = state.previousRoundOutput();
        String threadId = state.metadata().get("thread_id").toString();

        // If output is empty, generate a response using LLM
        if (output == null || output.trim().isEmpty()) {
            log.warn("previousRoundOutput is empty for thread: {}, generating fallback response", threadId);
            output = generateFallbackResponse(state);
        }

        // Add the final response as an assistant message
        List<Message> messages = new ArrayList<>(state.messages());
        messages.add(Message.assistant(output));

        log.info("ReflectorNode finalized state for thread: {}, output length: {}", threadId, output.length());

        return Map.of(
                "route", "done",
                "messages", messages
        );
    }

    /**
     * Generate a fallback response when previousRoundOutput is empty.
     * Uses LLM to generate a response based on conversation history.
     */
    private String generateFallbackResponse(AgentGraphState state) {
        String conversation = state.messages().stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        String prompt = """
                基于以下对话历史，请生成一个合适的回复：
                
                %s
                
                请根据用户的最后一个问题给出有帮助的回复。""".formatted(conversation);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Failed to generate fallback response", e);
            return "我正在处理您的请求，请稍候...";
        }
    }

    private String getLastUserMessage(AgentGraphState state) {
        var messages = state.messages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getRole() == Message.Role.USER) {
                return messages.get(i).getContent();
            }
        }
        return "";
    }

}
