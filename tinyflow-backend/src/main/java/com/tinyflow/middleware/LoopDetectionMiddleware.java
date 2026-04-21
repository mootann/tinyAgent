package com.tinyflow.middleware;

import com.tinyflow.model.AgentGraphState;
import com.tinyflow.service.EventEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Middleware for detecting and preventing infinite loops.
 * Compatible with LangGraph4J.
 */
@Slf4j
@Component
public class LoopDetectionMiddleware implements Middleware {

    private static final int MAX_ITERATIONS = 3;
    private static final int HISTORY_SIZE = 5;

    private final Set<String> stateHistory = new HashSet<>();

    @Override
    public AgentGraphState beforeNode(AgentGraphState state) {
        int iteration = state.iteration();

        // Check iteration count
        if (iteration >= MAX_ITERATIONS) {
            log.warn("Loop detected: max iterations ({}) reached", MAX_ITERATIONS);
            // Emit loop_warning event
            EventEmitter emitter = EventEmitter.get(state.metadata().get("thread_id").toString());
            if (emitter != null) {
                emitter.emitLoopWarning(iteration, "达到最大迭代次数");
            }
            return state
                    .withLoopTerminated(true)
                    .withLoopReason("达到最大迭代次数")
                    .withRoute("done");
        }

        // Check for repeating state patterns
        String stateSignature = createStateSignature(state);
        if (stateHistory.contains(stateSignature)) {
            log.warn("Loop detected: repeating state pattern");
            // Emit loop_warning event
            EventEmitter emitter = EventEmitter.get(state.metadata().get("thread_id").toString());
            if (emitter != null) {
                emitter.emitLoopWarning(iteration, "检测到循环模式");
            }
            return state
                    .withLoopTerminated(true)
                    .withLoopReason("检测到循环模式")
                    .withRoute("done");
        }

        // Add to history
        stateHistory.add(stateSignature);

        // Limit history size
        if (stateHistory.size() > HISTORY_SIZE) {
            stateHistory.clear(); // Simple reset, could use LRU
        }

        return state;
    }

    @Override
    public AgentGraphState afterNode(AgentGraphState state) {
        // Increment iteration counter
        return state.withIteration(state.iteration() + 1);
    }

    /**
     * Create a simple signature of the state for loop detection
     */
    private String createStateSignature(AgentGraphState state) {
        // Use route, execution mode, and pending tasks count as signature
        int pendingCount = state.pendingTasks() != null ? state.pendingTasks().size() : 0;
        return String.format("%s:%s:%d",
                state.route(),
                state.executionMode(),
                pendingCount);
    }

}
