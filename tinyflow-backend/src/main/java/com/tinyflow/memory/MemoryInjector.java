package com.tinyflow.memory;

import com.tinyflow.model.Fact;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds memory prompt from facts for injection into LLM context.
 */
@Component
public class MemoryInjector {

    private static final int DEFAULT_TOKEN_BUDGET = 500;
    private static final double DEFAULT_MIN_CONFIDENCE = 0.7;

    /**
     * Build memory prompt from facts
     */
    public String buildPrompt(List<Fact> facts, int tokenBudget, double minConfidence) {
        // Filter by confidence and sort by weight
        List<Fact> relevantFacts = facts.stream()
                .filter(f -> f.getConfidence() >= minConfidence)
                .sorted(Comparator.comparingDouble(Fact::getWeight).reversed())
                .toList();

        if (relevantFacts.isEmpty()) {
            return "";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("## 关于用户的记忆\n\n");

        int estimatedTokens = 0;
        int factCount = 0;

        for (Fact fact : relevantFacts) {
            String factText = "- " + fact.getContent() + "\n";
            int factTokens = estimateTokens(factText);

            if (estimatedTokens + factTokens > tokenBudget) {
                break;
            }

            prompt.append(factText);
            estimatedTokens += factTokens;
            factCount++;
        }

        if (factCount > 0) {
            prompt.append("\n请根据以上记忆来更好地回答用户的问题。\n");
        }

        return prompt.toString();
    }

    /**
     * Build memory prompt with default settings
     */
    public String buildPrompt(List<Fact> facts) {
        return buildPrompt(facts, DEFAULT_TOKEN_BUDGET, DEFAULT_MIN_CONFIDENCE);
    }

    /**
     * Rough token estimation (1 token ≈ 4 characters for CJK, 4 chars ≈ 1 token for English)
     */
    private int estimateTokens(String text) {
        int cjkChars = 0;
        int otherChars = 0;

        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS) {
                cjkChars++;
            } else {
                otherChars++;
            }
        }

        return cjkChars + (otherChars / 4);
    }

}
