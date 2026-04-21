package com.tinyflow.memory;

import com.tinyflow.model.Fact;
import com.tinyflow.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts facts from conversation messages.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FactExtractor {

    private final ChatClient chatClient;

    private static final String EXTRACTION_PROMPT = """
            Analyze the following conversation and extract important facts about the user.
            Focus on:
            - User preferences and interests
            - Personal information (name, email, etc.)
            - Important context that would be useful in future conversations
            
            Return each fact on a new line starting with "- ".
            Only extract clear, factual information. Be concise.
            
            Conversation:
            %s
            
            Facts:""";

    private static final Pattern FACT_PATTERN = Pattern.compile("^-\\s*(.+)$", Pattern.MULTILINE);

    /**
     * Extract facts from a list of messages
     */
    public List<Fact> extract(List<Message> messages, String threadId) {
        if (messages.isEmpty()) {
            return List.of();
        }

        String conversationText = formatConversation(messages);
        String prompt = String.format(EXTRACTION_PROMPT, conversationText);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            return parseFacts(response, threadId);
        } catch (Exception e) {
            log.warn("Failed to extract facts: {}", e.getMessage());
            return List.of();
        }
    }

    private String formatConversation(List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        for (Message msg : messages) {
            String role = msg.getRole().toString().toLowerCase();
            sb.append(role).append(": ").append(msg.getContent()).append("\n");
        }
        return sb.toString();
    }

    private List<Fact> parseFacts(String response, String threadId) {
        List<Fact> facts = new java.util.ArrayList<>();
        Matcher matcher = FACT_PATTERN.matcher(response);

        while (matcher.find()) {
            String content = matcher.group(1).trim();
            if (!content.isEmpty()) {
                facts.add(Fact.builder()
                        .content(content)
                        .threadId(threadId)
                        .build());
            }
        }

        return facts;
    }

}
