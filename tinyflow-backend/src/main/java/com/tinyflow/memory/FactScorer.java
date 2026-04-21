package com.tinyflow.memory;

import com.tinyflow.model.Fact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scores facts based on confidence and relevance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FactScorer {

    private final ChatClient chatClient;

    private static final String SCORING_PROMPT = """
            Rate the confidence of each fact below on a scale of 0.0 to 1.0.
            Consider:
            - Specificity: More specific facts are better
            - Clarity: Clear statements are better than ambiguous ones
            - Importance: Facts that reveal user preferences or important context
            
            Return in format: "fact: score" (one per line)
            
            Facts to score:
            %s""";

    private static final Pattern SCORE_PATTERN = Pattern.compile("(.+):\\s*(\\d\\.\\d+)");

    /**
     * Score new facts against existing facts
     */
    public List<Fact> score(List<Fact> newFacts, List<Fact> existingFacts) {
        if (newFacts.isEmpty()) {
            return List.of();
        }

        String factsText = formatFacts(newFacts);
        String prompt = String.format(SCORING_PROMPT, factsText);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            return applyScores(newFacts, response);
        } catch (Exception e) {
            log.warn("Failed to score facts: {}", e.getMessage());
            // Return facts with default confidence
            return newFacts.stream()
                    .map(f -> f.toBuilder().confidence(0.7).build())
                    .toList();
        }
    }

    private String formatFacts(List<Fact> facts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < facts.size(); i++) {
            sb.append(i + 1).append(". ").append(facts.get(i).getContent()).append("\n");
        }
        return sb.toString();
    }

    private List<Fact> applyScores(List<Fact> facts, String response) {
        List<Fact> scoredFacts = new java.util.ArrayList<>();
        Matcher matcher = SCORE_PATTERN.matcher(response);

        int index = 0;
        while (matcher.find() && index < facts.size()) {
            try {
                double score = Double.parseDouble(matcher.group(2));
                score = Math.max(0.0, Math.min(1.0, score)); // Clamp to [0, 1]

                Fact fact = facts.get(index).toBuilder()
                        .confidence(score)
                        .build();
                scoredFacts.add(fact);
                index++;
            } catch (NumberFormatException e) {
                // Skip invalid scores
            }
        }

        // Add remaining facts with default score
        while (index < facts.size()) {
            scoredFacts.add(facts.get(index).toBuilder()
                    .confidence(0.7)
                    .build());
            index++;
        }

        return scoredFacts;
    }

}
