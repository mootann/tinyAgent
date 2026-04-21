package com.tinyflow.memory;

import com.tinyflow.model.Fact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Merges new facts with existing facts, handling conflicts.
 */
@Slf4j
@Component
public class FactMerger {

    private static final double SIMILARITY_THRESHOLD = 0.7;

    /**
     * Merge new facts with existing facts
     */
    public List<Fact> merge(List<Fact> newFacts, List<Fact> existingFacts) {
        List<Fact> merged = new ArrayList<>(existingFacts);

        for (Fact newFact : newFacts) {
            // Skip low confidence facts
            if (newFact.getConfidence() < 0.5) {
                continue;
            }

            // Check for similar existing facts
            boolean mergedWithExisting = false;
            for (int i = 0; i < merged.size(); i++) {
                Fact existing = merged.get(i);
                if (isSimilar(newFact, existing)) {
                    // Replace if new fact has higher confidence
                    if (newFact.getConfidence() > existing.getConfidence()) {
                        merged.set(i, newFact);
                    }
                    mergedWithExisting = true;
                    break;
                }
            }

            // Add as new fact if not similar to any existing
            if (!mergedWithExisting) {
                merged.add(newFact);
            }
        }

        return merged;
    }

    /**
     * Check if two facts are similar (simple string similarity)
     */
    private boolean isSimilar(Fact fact1, Fact fact2) {
        String content1 = fact1.getContent().toLowerCase();
        String content2 = fact2.getContent().toLowerCase();

        // Exact match
        if (content1.equals(content2)) {
            return true;
        }

        // Check for significant word overlap
        String[] words1 = content1.split("\\s+");
        String[] words2 = content2.split("\\s+");

        int commonWords = 0;
        for (String word1 : words1) {
            if (word1.length() > 3) { // Only consider significant words
                for (String word2 : words2) {
                    if (word1.equals(word2)) {
                        commonWords++;
                        break;
                    }
                }
            }
        }

        double similarity = (2.0 * commonWords) / (words1.length + words2.length);
        return similarity > SIMILARITY_THRESHOLD;
    }

}
