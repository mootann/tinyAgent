package com.tinyflow.middleware;

import com.tinyflow.model.AgentGraphState;
import com.tinyflow.model.Message;
import com.tinyflow.service.EventEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Middleware for compacting context when message history is too long.
 * Compatible with LangGraph4J.
 */
@Slf4j
@Component
public class ContextCompactionMiddleware implements Middleware {

    private static final int MAX_MESSAGES = 10;
    private static final int SUMMARY_THRESHOLD = 8;

    @Override
    public AgentGraphState beforeNode(AgentGraphState state) {
        List<Message> messages = state.messages();

        if (messages.size() <= MAX_MESSAGES) {
            return state;
        }

        // Need to compact
        int originalCount = messages.size();

        // Keep system message if present
        List<Message> compacted = new ArrayList<>();
        int startIndex = 0;

        if (!messages.isEmpty() && messages.get(0).getRole() == Message.Role.SYSTEM) {
            compacted.add(messages.get(0));
            startIndex = 1;
        }

        // Add summary placeholder (in production, generate actual summary)
        compacted.add(Message.system("[之前的对话已压缩为摘要]"));

        // Keep last few messages
        int messagesToKeep = Math.min(SUMMARY_THRESHOLD, messages.size() - startIndex);
        compacted.addAll(messages.subList(messages.size() - messagesToKeep, messages.size()));

        log.info("Context compacted: {} -> {} messages", originalCount, compacted.size());

        // Emit context_compacted event
        EventEmitter emitter = EventEmitter.get(state.metadata().get("thread_id").toString());
        if (emitter != null) {
            emitter.emitContextCompacted(originalCount, compacted.size());
        }

        return state
                .withMessages(compacted)
                .withContextCompacted(true);
    }

    @Override
    public AgentGraphState afterNode(AgentGraphState state) {
        // No action needed after node
        return state;
    }

}
