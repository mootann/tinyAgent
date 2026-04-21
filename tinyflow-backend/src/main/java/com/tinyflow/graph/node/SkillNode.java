package com.tinyflow.graph.node;

import com.tinyflow.model.AgentGraphState;
import com.tinyflow.model.Message;
import com.tinyflow.model.Skill;
import com.tinyflow.model.TaskSpec;
import com.tinyflow.skill.SkillRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Skill node - matches and routes to appropriate skill.
 * Compatible with LangGraph4J.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillNode implements GraphNode {

    private final SkillRouter skillRouter;

    @Override
    public Map<String, Object> execute(AgentGraphState state) {
        String threadId = state.metadata().get("thread_id").toString();
        log.info("SkillNode executing for thread: {}", threadId);
        
        String lastMessage = getLastUserMessage(state);
        if (lastMessage == null) {
            log.warn("SkillNode: No user message found for thread: {}, routing to execute", threadId);
            return Map.of("route", "execute");
        }

        var skillOpt = skillRouter.route(lastMessage);

        if (skillOpt.isPresent()) {
            Skill skill = skillOpt.get();
            log.info("SkillNode matched skill: {} for thread: {}", skill.getName(), threadId);

            // Create task for skill execution
            TaskSpec task = TaskSpec.builder()
                    .description(lastMessage)
                    .type(TaskSpec.TaskType.SKILL_SUBAGENT)
                    .skillName(skill.getName())
                    .skillSystemPrompt(skill.getSystemPrompt())
                    .tools(skill.getTools())
                    .timeout(300)
                    .build();

            List<TaskSpec> pendingTasks = new ArrayList<>();
            pendingTasks.add(task);

            log.info("SkillNode completed for thread: {}, created task for skill: {}, routing to: execute", threadId, skill.getName());
            
            return Map.of(
                    "pendingTasks", pendingTasks,
                    "route", "execute"
            );
        } else {
            log.info("SkillNode: No skill matched for thread: {}, using default execution", threadId);
            return Map.of("route", "execute");
        }
    }

    private String getLastUserMessage(AgentGraphState state) {
        var messages = state.messages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getRole() == Message.Role.USER) {
                return messages.get(i).getContent();
            }
        }
        return null;
    }

}
