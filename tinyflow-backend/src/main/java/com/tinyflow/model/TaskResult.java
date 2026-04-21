package com.tinyflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Result of a completed task.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResult implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        COMPLETED,
        FAILED,
        TIMED_OUT
    }

    private String taskId;
    private Status status;

    @Builder.Default
    private String output = "";

    private String error;

    @Builder.Default
    private double durationSeconds = 0.0;

    @Builder.Default
    private List<ToolCall> toolCalls = new ArrayList<>();

    private String skillName;

    /**
     * Convert from Map (for JSON deserialization compatibility)
     */
    @SuppressWarnings("unchecked")
    public static TaskResult fromMap(Map<String, Object> map) {
        TaskResult result = new TaskResult();
        result.taskId = map.get("taskId") != null ? map.get("taskId").toString() : null;
        result.status = map.get("status") != null ? Status.valueOf(map.get("status").toString()) : null;
        result.output = map.get("output") != null ? map.get("output").toString() : "";
        result.error = map.get("error") != null ? map.get("error").toString() : null;
        result.durationSeconds = map.get("durationSeconds") != null ? ((Number) map.get("durationSeconds")).doubleValue() : 0.0;
        result.toolCalls = map.get("toolCalls") != null ? (List<ToolCall>) map.get("toolCalls") : new ArrayList<>();
        result.skillName = map.get("skillName") != null ? map.get("skillName").toString() : null;
        return result;
    }

}
