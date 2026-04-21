package com.tinyflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a message in the conversation.
 * Compatible with LangChain/LangGraph message formats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Role {
        SYSTEM,
        USER,
        ASSISTANT,
        TOOL
    }

    private Role role;
    private String content;

    /**
     * Additional metadata (tool calls, etc.)
     */
    private Map<String, Object> metadata;

    /**
     * Create a user message
     */
    public static Message user(String content) {
        return Message.builder()
                .role(Role.USER)
                .content(content)
                .build();
    }

    /**
     * Create an assistant message
     */
    public static Message assistant(String content) {
        return Message.builder()
                .role(Role.ASSISTANT)
                .content(content)
                .build();
    }

    /**
     * Create a system message
     */
    public static Message system(String content) {
        return Message.builder()
                .role(Role.SYSTEM)
                .content(content)
                .build();
    }

    /**
     * Convert from Map (for JSON deserialization compatibility)
     */
    @SuppressWarnings("unchecked")
    public static Message fromMap(Map<String, Object> map) {
        Message message = new Message();
        message.role = map.get("role") != null ? Role.valueOf(map.get("role").toString()) : null;
        message.content = map.get("content") != null ? map.get("content").toString() : null;
        message.metadata = map.get("metadata") != null ? (Map<String, Object>) map.get("metadata") : null;
        return message;
    }

}
