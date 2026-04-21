package com.tinyflow.graph.node;

import com.tinyflow.model.AgentGraphState;
import com.tinyflow.model.TaskSpec;
import com.tinyflow.model.TodoItem;
import com.tinyflow.service.EventEmitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dispatch node - creates parallel tasks for ultra mode.
 * Compatible with LangGraph4J.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchNode implements GraphNode {

    @Override
    public Map<String, Object> execute(AgentGraphState state) {
        List<TodoItem> todos = state.todos();
        List<TaskSpec> pendingTasks = new ArrayList<>();

        // Create a task for each TODO
        EventEmitter emitter = EventEmitter.get(state.metadata().get("thread_id").toString());
        for (TodoItem todo : todos) {
            TaskSpec task = TaskSpec.builder()
                    .description(todo.getContent())
                    .type(TaskSpec.TaskType.SUBAGENT)
                    .agentType("researcher")
                    .timeout(300)
                    .build();

            pendingTasks.add(task);

            // Emit subagent_status event
            if (emitter != null) {
                String skill = task.getSkillName() != null ? task.getSkillName() : "research";
                emitter.emitSubagentStatus(
                        task.getId(),
                        "running",
                        "subagent",
                        "技能 [" + skill + "] 执行中"
                );
            }
        }

        log.debug("Dispatch created {} tasks", pendingTasks.size());

        return Map.of(
                "pendingTasks", pendingTasks,
                "route", "execute"
        );
    }

}
