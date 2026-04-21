package com.tinyflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Specification for a task to be executed by subagent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum TaskType {
        SUBAGENT,
        SKILL_SUBAGENT,
        SKILL_INJECT
    }

    @Builder.Default
    private String id = "task_" + UUID.randomUUID().toString().substring(0, 8);

    @Builder.Default
    private TaskType type = TaskType.SUBAGENT;

    private String description;

    /**
     * Agent type for generic subagent
     */
    private String agentType;

    /**
     * Skill name for skill-based execution
     */
    private String skillName;

    /**
     * System prompt for skill injection
     */
    private String skillSystemPrompt;

    /**
     * Tool names available for this task
     */
    private List<String> tools;

    @Builder.Default
    private int timeout = 300;

    /**
     * Convert from Map (for JSON deserialization compatibility)
     */
    @SuppressWarnings("unchecked")
    public static TaskSpec fromMap(Map<String, Object> map) {
        TaskSpec spec = new TaskSpec();
        spec.id = map.get("id") != null ? map.get("id").toString() : "task_" + UUID.randomUUID().toString().substring(0, 8);
        spec.type = map.get("type") != null ? TaskType.valueOf(map.get("type").toString()) : TaskType.SUBAGENT;
        spec.description = map.get("description") != null ? map.get("description").toString() : null;
        spec.agentType = map.get("agentType") != null ? map.get("agentType").toString() : null;
        spec.skillName = map.get("skillName") != null ? map.get("skillName").toString() : null;
        spec.skillSystemPrompt = map.get("skillSystemPrompt") != null ? map.get("skillSystemPrompt").toString() : null;
        spec.tools = map.get("tools") != null ? (List<String>) map.get("tools") : null;
        spec.timeout = map.get("timeout") != null ? ((Number) map.get("timeout")).intValue() : 300;
        return spec;
    }

}
