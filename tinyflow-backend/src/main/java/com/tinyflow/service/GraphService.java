package com.tinyflow.service;

import com.tinyflow.graph.LangGraphBuilder;
import com.tinyflow.memory.MemoryEngine;
import com.tinyflow.model.AgentGraphState;
import com.tinyflow.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.tinyflow.service.EventEmitter.*;

/**
 * Service for executing the agent graph using LangGraph4J.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphService {

    private final LangGraphBuilder langGraphBuilder;
    private final MemoryEngine memoryEngine;
    private final MessageService messageService;

    /**
     * Execute a chat request through the agent graph.
     *
     * @param message  User message
     * @param threadId Thread ID for conversation context
     * @return Final state after graph execution
     */
    public AgentGraphState execute(String message, String threadId) {
        return execute(message, threadId, null);
    }

    /**
     * Execute a chat request through the agent graph with event emitter.
     *
     * @param message  User message
     * @param threadId Thread ID for conversation context
     * @param emitter  Event emitter for SSE streaming
     * @return Final state after graph execution
     */
    public AgentGraphState execute(String message, String threadId, EventEmitter emitter) {
        log.debug("Executing graph for thread: {}, message: {}", threadId, message);

        try {
            // Build and compile the graph
            var compiledGraph = langGraphBuilder.build();

            // Load conversation history from MySQL
            List<Message> history = messageService.getMessagesAsModel(threadId);

            // Create message list with history + new message
            List<Message> messages = new java.util.ArrayList<>(history);
            messages.add(Message.user(message));

            // Create initial state
            Map<String, Object> initialInputs = new java.util.HashMap<>();
            initialInputs.put("messages", messages);
            initialInputs.put("metadata", Map.of("thread_id", threadId));

            // Execute graph (checkpoint is managed by the graph itself)
            var result = compiledGraph.invoke(initialInputs);

            log.debug("Graph execution completed for thread: {}", threadId);

            // Save messages to MySQL only for non-default threads
            if (!"default".equals(threadId)) {
                // Save user message to MySQL
                messageService.saveUserMessage(threadId, message);

                // Extract and save assistant response
                result.ifPresent(state -> {
                    AgentGraphState agentState = (AgentGraphState) state;
                    List<Message> finalMessages = agentState.messages();
                    if (!finalMessages.isEmpty()) {
                        Message lastMessage = finalMessages.get(finalMessages.size() - 1);
                        if (lastMessage.getRole() == Message.Role.ASSISTANT) {
                            messageService.saveAssistantMessage(threadId, lastMessage.getContent());
                        }
                    }
                });
            }

            // Process conversation for memory extraction
            if (memoryEngine != null) {
                memoryEngine.processConversation(messages, threadId, emitter);
            }

            return result.map(state -> (AgentGraphState) state)
                    .orElse(new AgentGraphState(initialInputs));

        } catch (GraphStateException e) {
            log.error("Graph execution failed: {}", e.getMessage(), e);
            throw new RuntimeException("Graph execution failed", e);
        }
    }
}


