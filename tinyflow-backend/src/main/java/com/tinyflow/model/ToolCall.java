package com.tinyflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a tool call made by the agent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String query;
    private String preview;
    private String output;

    /**
     * Convert from Map (for JSON deserialization compatibility)
     */
    public static ToolCall fromMap(Map<String, Object> map) {
        ToolCall call = new ToolCall();
        call.name = map.get("name") != null ? map.get("name").toString() : null;
        call.query = map.get("query") != null ? map.get("query").toString() : null;
        call.preview = map.get("preview") != null ? map.get("preview").toString() : null;
        call.output = map.get("output") != null ? map.get("output").toString() : null;
        return call;
    }

}
