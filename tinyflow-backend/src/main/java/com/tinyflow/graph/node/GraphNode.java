package com.tinyflow.graph.node;

import com.tinyflow.model.AgentGraphState;

import java.util.Map;

/**
 * Interface for graph nodes.
 * Compatible with LangGraph4J.
 */
@FunctionalInterface
public interface GraphNode {

    /**
     * Execute the node logic.
     *
     * @param state Current state
     * @return Updated state as Map
     */
    Map<String, Object> execute(AgentGraphState state);

}
