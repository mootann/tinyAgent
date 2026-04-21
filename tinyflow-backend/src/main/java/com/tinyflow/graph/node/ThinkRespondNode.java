package com.tinyflow.graph.node;

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
 * ThinkRespond node - generates response with deep reasoning for thinking mode.
 * Compatible with LangGraph4J.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThinkRespondNode implements GraphNode {

    private final ChatClient chatClient;

    private static final String THINKING_PROMPT = """
            %s
            
            请仔细思考以下问题，展示你的推理过程，然后给出最终答案。
            使用 <think> 标签包裹你的思考过程，然后给出回答。
            
            对话：
            %s
            
            请用中文回答。""";

    @Override
    public Map<String, Object> execute(AgentGraphState state) {
        String conversation = formatConversation(state);
        String memoryContext = state.memoryContext();

        String prompt = String.format(THINKING_PROMPT,
                memoryContext != null ? memoryContext : "",
                conversation);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            // Extract content outside <think> tags for the final answer
            String finalAnswer = extractFinalAnswer(response);

            // Add assistant message to state
            List<Message> messages = new ArrayList<>(state.messages());
            messages.add(Message.assistant(finalAnswer));

            return Map.of("messages", messages);

        } catch (Exception e) {
            log.error("ThinkRespond node failed: {}", e.getMessage());
            List<Message> messages = new ArrayList<>(state.messages());
            messages.add(Message.assistant("抱歉，深度推理时出现问题。"));
            return Map.of("messages", messages);
        }
    }

    private String formatConversation(AgentGraphState state) {
        return state.messages().stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
    }

    private String extractFinalAnswer(String response) {
        // Remove <think>...</think> blocks
        String result = response.replaceAll("<think>.*?</think>", "").trim();

        // If nothing left after removing think blocks, use original
        if (result.isEmpty()) {
            result = response;
        }

        return result;
    }

}
