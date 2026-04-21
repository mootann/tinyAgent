package com.tinyflow.skill;

import com.tinyflow.model.Skill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Two-phase skill routing: keyword pre-filter + LLM semantic select.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillRouter {

    private final SkillRegistry skillRegistry;
    private final KeywordFilter keywordFilter;
    private final SkillSelector skillSelector;

    /**
     * Route a query to the best matching skill.
     *
     * @param query User query
     * @return Optional of best matching skill
     */
    public Optional<Skill> route(String query) {
        List<Skill> allSkills = skillRegistry.getAllSkills();

        if (allSkills.isEmpty()) {
            log.warn("No skills available for routing");
            return Optional.empty();
        }

        // Phase 1: Keyword filtering
        List<Skill> candidates = keywordFilter.filter(allSkills, query);

        if (candidates.isEmpty()) {
            log.debug("No skill candidates found for query: {}", query);
            return Optional.empty();
        }

        log.debug("Keyword filter returned {} candidates", candidates.size());

        // Phase 2: LLM semantic selection
        Skill bestSkill = skillSelector.select(candidates, query);

        if (bestSkill != null) {
            log.debug("Selected skill: {} for query: {}", bestSkill.getName(), query);
        }

        return Optional.ofNullable(bestSkill);
    }

    /**
     * Check if any skill matches the query (without full LLM selection)
     */
    public boolean hasMatchingSkill(String query) {
        List<Skill> allSkills = skillRegistry.getAllSkills();
        List<Skill> candidates = keywordFilter.filter(allSkills, query);
        return !candidates.isEmpty();
    }

}
