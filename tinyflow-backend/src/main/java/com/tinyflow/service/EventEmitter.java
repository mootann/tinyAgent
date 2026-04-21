package com.tinyflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Event emitter for SSE streaming.
 * Allows graph nodes to emit events during execution.
 */
@Slf4j
public class EventEmitter {

    private final Sinks.Many<String> sink;
    private final ObjectMapper objectMapper;
    private final String threadId;

    // Thread-local storage for current emitter
    private static final Map<String, EventEmitter> EMITTERS = new ConcurrentHashMap<>();

    public EventEmitter(String threadId, Sinks.Many<String> sink, ObjectMapper objectMapper) {
        this.threadId = threadId;
        this.sink = sink;
        this.objectMapper = objectMapper;
        EMITTERS.put(threadId, this);
    }

    /**
     * Get the current emitter for this thread.
     */
    public static EventEmitter get(String threadId) {
        return EMITTERS.get(threadId);
    }

    /**
     * Remove emitter when done.
     */
    public static void remove(String threadId) {
        EMITTERS.remove(threadId);
    }

    /**
     * Emit an event to the SSE stream.
     * Note: Spring WebFlux automatically wraps each Flux element in SSE format,
     * so we only need to send the data content here.
     */
    public void emit(String eventType, Map<String, Object> data) {
        try {
            // Create a ServerSentEvent object that Spring can properly serialize
            Map<String, Object> sseEvent = new java.util.HashMap<>();
            sseEvent.put("event", eventType);
            sseEvent.put("data", objectMapper.writeValueAsString(data));
            String json = objectMapper.writeValueAsString(sseEvent);
            log.info("[SSE] Sending event: {} | JSON: {}", eventType, json);
            sink.tryEmitNext(json);
            log.debug("Emitted event: {}", eventType);
        } catch (Exception e) {
            log.error("Failed to emit event: {}", eventType, e);
        }
    }

    /**
     * Emit a thinking event.
     */
    public void emitThinking(String node, String content) {
        emit("thinking", Map.of(
                "node", node,
                "content", content
        ));
    }

    /**
     * Emit a content event (streaming token).
     */
    public void emitContent(String content) {
        emit("content", Map.of("content", content));
    }

    /**
     * Emit a mode_selected event.
     */
    public void emitModeSelected(String mode, String reason) {
        Map<String, String> modeLabels = Map.of(
                "flash", "⚡ 快速回答",
                "thinking", "🧠 深度推理",
                "pro", "📋 规划执行",
                "ultra", "🚀 并行研究"
        );
        String desc = modeLabels.getOrDefault(mode, mode);
        emit("mode_selected", Map.of(
                "mode", mode,
                "reason", reason != null ? reason : "自动选择 " + desc + " 模式"
        ));
        emitThinking("router", "决策: " + desc);
    }

    /**
     * Emit a tool_call event.
     */
    public void emitToolCall(String name, String query, String preview) {
        emit("tool_call", Map.of(
                "name", name,
                "query", query,
                "preview", preview != null ? preview : ""
        ));
    }

    /**
     * Emit a tool_result event.
     */
    public void emitToolResult(String name, String preview) {
        emit("tool_result", Map.of(
                "name", name,
                "preview", preview != null ? preview : ""
        ));
    }

    /**
     * Emit a subagent_status event.
     */
    public void emitSubagentStatus(String taskId, String status, String type, String label) {
        emit("subagent_status", Map.of(
                "task_id", taskId,
                "status", status,
                "type", type != null ? type : "subagent",
                "label", label != null ? label : "任务执行中"
        ));
    }

    /**
     * Emit a subagent_result event.
     */
    public void emitSubagentResult(String taskId, String status, String label) {
        emit("subagent_result", Map.of(
                "task_id", taskId,
                "status", status,
                "label", label != null ? label : "任务完成"
        ));
    }

    /**
     * Emit a todo_update event.
     */
    public void emitTodoUpdate(java.util.List<Map<String, Object>> todos) {
        Map<String, Object> data = new HashMap<>();
        data.put("todos", todos);
        emit("todo_update", data);
    }

    /**
     * Emit a loop_warning event.
     */
    public void emitLoopWarning(int iteration, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("iteration", iteration);
        data.put("message", message != null ? message : "检测到循环");
        emit("loop_warning", data);
    }

    /**
     * Emit a context_compacted event.
     */
    public void emitContextCompacted(int originalCount, int compactedCount) {
        Map<String, Object> data = new HashMap<>();
        data.put("original_messages", originalCount);
        data.put("compacted_to", compactedCount);
        emit("context_compacted", data);
    }

    /**
     * Emit a memory_update event.
     */
    public void emitMemoryUpdate(java.util.List<Map<String, Object>> facts) {
        Map<String, Object> data = new HashMap<>();
        data.put("facts", facts);
        emit("memory_update", data);
    }

    /**
     * Emit a done event.
     */
    public void emitDone() {
        emit("done", Map.of());
    }

    /**
     * Emit an error event.
     */
    public void emitError(String error) {
        emit("error", Map.of("error", error));
    }

}
