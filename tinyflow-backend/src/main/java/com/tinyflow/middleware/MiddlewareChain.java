package com.tinyflow.middleware;

import com.tinyflow.model.AgentGraphState;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Chain of middlewares that process state in sequence.
 * Compatible with LangGraph4J.
 */
@RequiredArgsConstructor
public class MiddlewareChain {

    private final List<Middleware> middlewares;

    /**
     * Run a node with middleware processing.
     *
     * @param state    Input state
     * @param nodeFunc Node function to execute
     * @return Output state as Map
     */
    public Map<String, Object> runNode(AgentGraphState state, Function<AgentGraphState, Map<String, Object>> nodeFunc) {
        // Before node: process in order
        AgentGraphState currentState = state;
        for (Middleware mw : middlewares) {
            currentState = mw.beforeNode(currentState);
        }

        // Execute node
        Map<String, Object> result = nodeFunc.apply(currentState);

        // Convert result back to state for afterNode processing
        currentState = mergeResultIntoState(currentState, result);

        // After node: process in reverse order
        for (int i = middlewares.size() - 1; i >= 0; i--) {
            currentState = middlewares.get(i).afterNode(currentState);
        }

        // Return final state as map
        return currentState.toMap();
    }

    /**
     * Merge node result into state.
     */
    private AgentGraphState mergeResultIntoState(AgentGraphState state, Map<String, Object> result) {
        // Start with current state data
        var newData = new java.util.HashMap<>(state.data());
        // Apply updates from result
        newData.putAll(result);
        return new AgentGraphState(newData);
    }

}
