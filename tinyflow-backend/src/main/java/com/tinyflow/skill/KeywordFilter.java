package com.tinyflow.skill;

import com.tinyflow.model.Skill;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Phase 1: O(n) keyword scan for skill pre-filtering.
 */
@Component
public class KeywordFilter {

    /**
     * Filter skills by keyword match, returns top candidates sorted by relevance.
     *
     * @param skills       All available skills
     * @param query        User query
     * @param maxCandidates Maximum number of candidates to return
     * @return List of matching skills
     */
    public List<Skill> filter(List<Skill> skills, String query, int maxCandidates) {
        record ScoredSkill(int matchCount, int priority, Skill skill) {
        }

        return skills.stream()
                .map(skill -> {
                    int matchCount = skill.keywordMatch(query);
                    return new ScoredSkill(matchCount, skill.getPriority(), skill);
                })
                .filter(scored -> scored.matchCount > 0)
                .sorted(Comparator
                        .comparingInt(ScoredSkill::matchCount).reversed()
                        .thenComparingInt(ScoredSkill::priority).reversed())
                .map(ScoredSkill::skill)
                .limit(maxCandidates)
                .toList();
    }

    /**
     * Filter with default max candidates (5)
     */
    public List<Skill> filter(List<Skill> skills, String query) {
        return filter(skills, query, 5);
    }

}
