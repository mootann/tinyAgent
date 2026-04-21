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
 * Respond node - generates direct response for flash mode.
 * Compatible with LangGraph4J.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RespondNode implements GraphNode {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> execute(AgentGraphState state) {
        String conversation = formatConversation(state);
        String memoryContext = state.memoryContext();

        String prompt = buildPrompt(conversation, memoryContext);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            // Add assistant message to state
            List<Message> messages = new ArrayList<>(state.messages());
            messages.add(Message.assistant(response));

            return Map.of("messages", messages);

        } catch (Exception e) {
            log.error("Respond node failed: {}", e.getMessage());
            List<Message> messages = new ArrayList<>(state.messages());
            messages.add(Message.assistant("抱歉，处理您的请求时出现问题。"));
            return Map.of("messages", messages);
        }
    }

    private String formatConversation(AgentGraphState state) {
        return state.messages().stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
    }

    private String buildPrompt(String conversation, String memoryContext) {
        StringBuilder prompt = new StringBuilder();

        if (memoryContext != null && !memoryContext.isEmpty()) {
            prompt.append(memoryContext).append("\n\n");
        }

        prompt.append("基于以下对话，给出简洁的回答：\n\n");
        prompt.append(conversation);
        prompt.append("\n\nAssistant: ");

        return prompt.toString();
    }

}
