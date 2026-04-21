package com.tinyflow.tool;

import java.util.Map;

/**
 * Interface for agent tools.
 */
public interface Tool {

    /**
     * Get the tool name.
     */
    String getName();

    /**
     * Get the tool description.
     */
    String getDescription();

    /**
     * Execute the tool with given parameters.
     *
     * @param params Tool parameters
     * @return Tool execution result
     */
    String execute(Map<String, Object> params);
}
