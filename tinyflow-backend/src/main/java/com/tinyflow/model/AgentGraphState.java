package com.tinyflow.model;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent state for LangGraph4J.
 * Mirrors Python GraphState but extends AgentState for LangGraph4J compatibility.
 */
public class AgentGraphState extends AgentState {

    // State schema with channels - use static initializer for more than 10 entries
    public static final Map<String, Channel<?>> SCHEMA;
    static {
        Map<String, Channel<?>> schema = new HashMap<>();
        schema.put("messages", Channels.appender(ArrayList::new));
        schema.put("route", Channels.base(() -> ""));
        schema.put("executionMode", Channels.base(() -> "flash"));
        schema.put("pendingTasks", Channels.appender(ArrayList::new));
        schema.put("completedTasks", Channels.appender(ArrayList::new));
        schema.put("previousRoundOutput", Channels.base(() -> ""));
        schema.put("iteration", Channels.base(() -> 0));
        schema.put("memoryContext", Channels.base(() -> ""));
        schema.put("metadata", Channels.base(() -> new HashMap<String, Object>()));
        schema.put("lastToolCalls", Channels.appender(ArrayList::new));
        schema.put("todos", Channels.appender(ArrayList::new));
        schema.put("loopTerminated", Channels.base(() -> false));
        schema.put("loopReason", Channels.base(() -> ""));
        schema.put("contextCompacted", Channels.base(() -> false));

        SCHEMA = java.util.Collections.unmodifiableMap(schema);
    }

    public AgentGraphState(Map<String, Object> initData) {
        super(initData);
    }

    // Factory method for initial state
    public static AgentGraphState createInitial() {
        Map<String, Object> initData = new HashMap<>();
        initData.put("messages", new ArrayList<Message>());
        initData.put("pendingTasks", new ArrayList<TaskSpec>());
        initData.put("completedTasks", new ArrayList<TaskResult>());
        initData.put("lastToolCalls", new ArrayList<ToolCall>());
        initData.put("todos", new ArrayList<TodoItem>());
        initData.put("iteration", 0);
        initData.put("metadata", new HashMap<String, Object>());
        initData.put("loopTerminated", false);
        initData.put("contextCompacted", false);
        return new AgentGraphState(initData);
    }

    // Typed accessors
    @SuppressWarnings("unchecked")
    public List<Message> messages() {
        return (List<Message>) value("messages").orElse(new ArrayList<>());
    }

    public String route() {
        return (String) value("route").orElse("");
    }

    public String executionMode() {
        return (String) value("executionMode").orElse("flash");
    }

    @SuppressWarnings("unchecked")
    public List<TaskSpec> pendingTasks() {
        return (List<TaskSpec>) value("pendingTasks").orElse(new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public List<TaskResult> completedTasks() {
        return (List<TaskResult>) value("completedTasks").orElse(new ArrayList<>());
    }

    public String previousRoundOutput() {
        return (String) value("previousRoundOutput").orElse("");
    }

    public int iteration() {
        return (Integer) value("iteration").orElse(0);
    }

    public String memoryContext() {
        return (String) value("memoryContext").orElse("");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> metadata() {
        return (Map<String, Object>) value("metadata").orElse(new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    public List<ToolCall> lastToolCalls() {
        return (List<ToolCall>) value("lastToolCalls").orElse(new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public List<TodoItem> todos() {
        return (List<TodoItem>) value("todos").orElse(new ArrayList<>());
    }

    public boolean loopTerminated() {
        return (Boolean) value("loopTerminated").orElse(false);
    }

    public String loopReason() {
        return (String) value("loopReason").orElse("");
    }

    public boolean contextCompacted() {
        return (Boolean) value("contextCompacted").orElse(false);
    }

    // Update methods (return new state with changes)
    public AgentGraphState withMessages(List<Message> messages) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("messages", messages);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withRoute(String route) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("route", route);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withExecutionMode(String mode) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("executionMode", mode);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withPendingTasks(List<TaskSpec> tasks) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("pendingTasks", tasks);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withCompletedTasks(List<TaskResult> tasks) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("completedTasks", tasks);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withPreviousRoundOutput(String output) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("previousRoundOutput", output);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withIteration(int iteration) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("iteration", iteration);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withMemoryContext(String context) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("memoryContext", context);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withMetadata(Map<String, Object> metadata) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("metadata", metadata);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withLastToolCalls(List<ToolCall> calls) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("lastToolCalls", calls);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withTodos(List<TodoItem> todos) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("todos", todos);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withLoopTerminated(boolean terminated) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("loopTerminated", terminated);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withLoopReason(String reason) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("loopReason", reason);
        return new AgentGraphState(newData);
    }

    public AgentGraphState withContextCompacted(boolean compacted) {
        Map<String, Object> newData = new HashMap<>(data());
        newData.put("contextCompacted", compacted);
        return new AgentGraphState(newData);
    }

    // Helper to convert to Map for LangGraph4J
    public Map<String, Object> toMap() {
        return new HashMap<>(data());
    }

}
