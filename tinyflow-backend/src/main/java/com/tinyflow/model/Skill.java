package com.tinyflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Skill definition for the skill routing system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Skill implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String description;

    /**
     * Keywords for keyword-based pre-filtering
     */
    private List<String> keywords;

    /**
     * Priority for sorting (higher = more important)
     */
    @Builder.Default
    private int priority = 0;

    /**
     * System prompt for this skill
     */
    private String systemPrompt;

    /**
     * Available tools for this skill
     */
    private List<String> tools;

    /**
     * Calculate keyword match score for a query
     */
    public int keywordMatch(String query) {
        if (keywords == null || keywords.isEmpty()) {
            return 0;
        }
        String lowerQuery = query.toLowerCase();
        int score = 0;
        for (String keyword : keywords) {
            if (lowerQuery.contains(keyword.toLowerCase())) {
                score++;
            }
        }
        return score;
    }

}
