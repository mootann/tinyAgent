package com.tinyflow.graph.node;

import com.tinyflow.model.AgentGraphState;
import com.tinyflow.model.TaskResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Merge node - merges parallel task results for ultra mode.
 * Compatible with LangGraph4J.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MergeNode implements GraphNode {

    private final ChatClient chatClient;

    private static final String MERGE_PROMPT = """
            请综合以下研究结果，生成一份完整的回答。
            
            原始问题: %s
            
            研究结果:
            %s
            
            请整合以上信息，生成一份结构清晰、内容完整的回答。""";

    @Override
    public Map<String, Object> execute(AgentGraphState state) {
        List<TaskResult> completedTasks = state.completedTasks();

        if (completedTasks == null || completedTasks.isEmpty()) {
            return Map.of("route", "reflector");
        }

        // Format results
        String resultsText = completedTasks.stream()
                .map(r -> "[" + r.getSkillName() + "]\n" + r.getOutput())
                .collect(Collectors.joining("\n\n"));

        String originalQuery = getLastUserMessage(state);

        String prompt = String.format(MERGE_PROMPT, originalQuery, resultsText);

        try {
            String mergedResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return Map.of(
                    "previousRoundOutput", mergedResponse,
                    "route", "reflector"
            );

        } catch (Exception e) {
            log.error("Merge node failed: {}", e.getMessage());

            // Fallback: just concatenate results
            String fallback = completedTasks.stream()
                    .map(TaskResult::getOutput)
                    .collect(Collectors.joining("\n\n"));

            return Map.of(
                    "previousRoundOutput", fallback,
                    "route", "reflector"
            );
        }
    }

    private String getLastUserMessage(AgentGraphState state) {
        var messages = state.messages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getRole().toString().equals("USER")) {
                return messages.get(i).getContent();
            }
        }
        return "未知问题";
    }

}
