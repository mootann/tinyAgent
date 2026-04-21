package com.tinyflow.tool;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all available tools.
 */
@Component
public class ToolRegistry {

    private final Map<String, Tool> tools = new HashMap<>();

    public ToolRegistry(WebSearchTool webSearchTool) {
        // Register tools
        register(webSearchTool);
    }

    /**
     * Register a tool.
     */
    public void register(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    /**
     * Get a tool by name.
     */
    public Tool getTool(String name) {
        return tools.get(name);
    }

    /**
     * Check if a tool exists.
     */
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    /**
     * Get all tool names.
     */
    public Map<String, Tool> getAllTools() {
        return new HashMap<>(tools);
    }
}
