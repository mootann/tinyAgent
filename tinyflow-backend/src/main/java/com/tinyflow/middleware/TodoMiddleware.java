package com.tinyflow.middleware;

import com.tinyflow.model.AgentGraphState;
import com.tinyflow.model.TodoItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Middleware for tracking TODO items in execution plan.
 * Compatible with LangGraph4J.
 */
@Slf4j
@Component
public class TodoMiddleware implements Middleware {

    @Override
    public AgentGraphState beforeNode(AgentGraphState state) {
        // Track which TODOs are in progress based on node type
        String executionMode = state.executionMode();

        if ("pro".equals(executionMode) || "ultra".equals(executionMode)) {
            List<TodoItem> todos = new ArrayList<>(state.todos());

            // Update first pending todo to in_progress
            for (int i = 0; i < todos.size(); i++) {
                TodoItem todo = todos.get(i);
                if (todo.getStatus() == TodoItem.Status.PENDING) {
                    todos.set(i, todo.toBuilder()
                            .status(TodoItem.Status.IN_PROGRESS)
                            .build());
                    break;
                }
            }

            return state.withTodos(todos);
        }

        return state;
    }

    @Override
    public AgentGraphState afterNode(AgentGraphState state) {
        // Mark completed todos
        List<TodoItem> todos = new ArrayList<>(state.todos());
        boolean updated = false;

        for (int i = 0; i < todos.size(); i++) {
            TodoItem todo = todos.get(i);
            if (todo.getStatus() == TodoItem.Status.IN_PROGRESS) {
                todos.set(i, todo.toBuilder()
                        .status(TodoItem.Status.COMPLETED)
                        .build());
                updated = true;
                break;
            }
        }

        if (updated) {
            return state.withTodos(todos);
        }

        return state;
    }

}
