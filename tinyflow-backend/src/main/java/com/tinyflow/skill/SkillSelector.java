package com.tinyflow.skill;

import com.tinyflow.model.Skill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Phase 2: LLM semantic selection of best skill from candidates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillSelector {

    private final ChatClient chatClient;

    private static final String SELECTION_PROMPT = """
            用户请求: %s

            候选 Skills:
            %s

            请选择最匹配的 skill 名称。如果没有合适的，回复 "none"。只回复名称。""";

    /**
     * Select the best skill from candidates using LLM.
     *
     * @param candidates List of candidate skills
     * @param query      User query
     * @return Best matching skill or null if none matches
     */
    public Skill select(List<Skill> candidates, String query) {
        if (candidates.isEmpty()) {
            return null;
        }

        // If only one candidate, return it directly
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        String candidatesText = candidates.stream()
                .map(s -> "- " + s.getName() + ": " + s.getDescription())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        String prompt = String.format(SELECTION_PROMPT, query, candidatesText);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content()
                    .trim()
                    .toLowerCase();

            if (response.equals("none") || response.isEmpty()) {
                return null;
            }

            // Find matching skill
            for (Skill skill : candidates) {
                String skillName = skill.getName().toLowerCase();
                if (response.equals(skillName) || response.contains(skillName)) {
                    return skill;
                }
            }

            // If no exact match, return first candidate as fallback
            log.warn("LLM selected unknown skill: {}, using fallback", response);
            return candidates.get(0);

        } catch (Exception e) {
            log.warn("LLM skill selection failed: {}", e.getMessage());
            return candidates.get(0);
        }
    }

}
