package com.tinyflow.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Agent state definition for the LangGraph state machine.
 * This is the central state container that flows through all graph nodes.
 */
@Data
@Builder(toBuilder = true)
public class GraphState {

    /**
     * Conversation messages (accumulated via reducer)
     */
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    /**
     * Route decision: "direct" | "subagent" | "skill" | "continue_execute" | "done"
     */
    private String route;

    /**
     * Execution mode: "flash" | "thinking" | "pro" | "ultra"
     */
    private String executionMode;

    /**
     * Pending tasks for subagent execution
     */
    @Builder.Default
    private List<TaskSpec> pendingTasks = new ArrayList<>();

    /**
     * Completed task results
     */
    @Builder.Default
    private List<TaskResult> completedTasks = new ArrayList<>();

    /**
     * Output from the previous execution round
     */
    private String previousRoundOutput;

    /**
     * Current iteration count (for loop detection)
     */
    @Builder.Default
    private int iteration = 0;

    /**
     * Memory context injected into prompts
     */
    private String memoryContext;

    /**
     * Metadata including thread_id
     */
    @Builder.Default
    private Map<String, Object> metadata = Map.of();

    /**
     * Tool calls from the last execute round
     */
    @Builder.Default
    private List<ToolCall> lastToolCalls = new ArrayList<>();

    /**
     * TODO items for execution plan
     */
    @Builder.Default
    private List<TodoItem> todos = new ArrayList<>();

    /**
     * Node-specific outputs for event extraction
     */
    private String nodeOutput;

    /**
     * Loop detection flags
     */
    private boolean loopTerminated;
    private String loopReason;

    /**
     * Context compaction flags
     */
    private boolean contextCompacted;
    private int originalMessageCount;
    private int compactedMessageCount;

    /**
     * Create a new state with a message appended
     */
    public GraphState withMessage(Message message) {
        List<Message> newMessages = new ArrayList<>(this.messages);
        newMessages.add(message);
        return this.toBuilder().messages(newMessages).build();
    }

    /**
     * Create a new state with updated metadata
     */
    public GraphState withMetadata(String key, Object value) {
        Map<String, Object> newMetadata = new java.util.HashMap<>(this.metadata);
        newMetadata.put(key, value);
        return this.toBuilder().metadata(newMetadata).build();
    }

}
