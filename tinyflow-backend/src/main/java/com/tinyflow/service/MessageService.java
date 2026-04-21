package com.tinyflow.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinyflow.entity.MessageEntity;
import com.tinyflow.mapper.MessageMapper;
import com.tinyflow.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Message service - manages conversation messages using MySQL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService extends ServiceImpl<MessageMapper, MessageEntity> {

    private final MessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    /**
     * Save a message with duplicate check
     */
    @Transactional
    public void saveMessage(String threadId, Message message) {
        // Check for duplicate message (same role and content within last 5 seconds)
        List<MessageEntity> recentMessages = messageMapper.findByThreadIdOrderBySequence(threadId);
        if (!recentMessages.isEmpty()) {
            MessageEntity lastMessage = recentMessages.get(recentMessages.size() - 1);
            if (lastMessage.getRole().equals(message.getRole().name().toLowerCase()) 
                    && lastMessage.getContent().equals(message.getContent())) {
                log.debug("Duplicate message detected for thread: {}, skipping save", threadId);
                return;
            }
        }
        
        Integer maxSequence = messageMapper.getMaxSequenceByThreadId(threadId);
        
        String metadataJson = null;
        if (message.getMetadata() != null) {
            try {
                metadataJson = objectMapper.writeValueAsString(message.getMetadata());
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize metadata: {}", e.getMessage());
            }
        }

        MessageEntity entity = MessageEntity.builder()
                .threadId(threadId)
                .role(message.getRole().name().toLowerCase())
                .content(message.getContent())
                .metadata(metadataJson)
                .sequence(maxSequence + 1)
                .build();

        save(entity);
        log.debug("Saved message for thread: {}, sequence: {}", threadId, entity.getSequence());
    }

    /**
     * Save user message
     */
    @Transactional
    public void saveUserMessage(String threadId, String content) {
        saveMessage(threadId, Message.user(content));
    }

    /**
     * Save assistant message
     */
    @Transactional
    public void saveAssistantMessage(String threadId, String content) {
        saveMessage(threadId, Message.assistant(content));
    }

    /**
     * Get messages by thread ID ordered by sequence
     */
    public List<MessageEntity> getMessagesByThreadId(String threadId) {
        return messageMapper.findByThreadIdOrderBySequence(threadId);
    }

    /**
     * Convert MessageEntity to Message model
     */
    public Message toMessage(MessageEntity entity) {
        Message.Role role;
        try {
            role = Message.Role.valueOf(entity.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            role = Message.Role.USER;
        }

        Map<String, Object> metadata = null;
        if (entity.getMetadata() != null) {
            try {
                metadata = objectMapper.readValue(entity.getMetadata(), Map.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize metadata: {}", e.getMessage());
            }
        }

        return Message.builder()
                .role(role)
                .content(entity.getContent())
                .metadata(metadata)
                .build();
    }

    /**
     * Get messages as Message model list
     */
    public List<Message> getMessagesAsModel(String threadId) {
        return getMessagesByThreadId(threadId).stream()
                .map(this::toMessage)
                .collect(Collectors.toList());
    }

    /**
     * Format conversation history for LLM prompt
     */
    public String formatConversationHistory(String threadId) {
        List<MessageEntity> messages = getMessagesByThreadId(threadId);
        
        return messages.stream()
                .map(m -> {
                    String role = m.getRole().equals("user") ? "User" : "Assistant";
                    return role + ": " + m.getContent();
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * Delete messages by thread ID
     */
    @Transactional
    public void deleteMessagesByThreadId(String threadId) {
        messageMapper.deleteByThreadId(threadId);
        log.debug("Deleted messages for thread: {}", threadId);
    }

    /**
     * Get message count for thread
     */
    public long getMessageCount(String threadId) {
        return lambdaQuery()
                .eq(MessageEntity::getThreadId, threadId)
                .count();
    }
}
