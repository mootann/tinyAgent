package com.tinyflow.middleware;

import com.tinyflow.model.AgentGraphState;

/**
 * Middleware interface for processing state before and after node execution.
 * Compatible with LangGraph4J.
 */
public interface Middleware {

    /**
     * Process state before node execution.
     *
     * @param state Current state
     * @return Modified state
     */
    AgentGraphState beforeNode(AgentGraphState state);

    /**
     * Process state after node execution.
     *
     * @param state Current state
     * @return Modified state
     */
    AgentGraphState afterNode(AgentGraphState state);

}
