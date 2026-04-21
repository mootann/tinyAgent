package com.tinyflow.memory;

import com.tinyflow.model.Fact;

import java.util.List;

/**
 * Storage interface for memory facts.
 */
public interface MemoryStorage {

    /**
     * Get all stored facts
     */
    List<Fact> getFacts();

    /**
     * Save facts to storage
     */
    void saveFacts(List<Fact> facts);

    /**
     * Apply decay to facts older than specified days
     */
    default void applyDecay(int decayDays, double decayFactor) {
        List<Fact> facts = getFacts();
        java.time.Instant cutoff = java.time.Instant.now().minusSeconds(decayDays * 86400L);

        List<Fact> decayedFacts = facts.stream()
                .map(fact -> {
                    if (fact.getLastAccessed().isBefore(cutoff)) {
                        return fact.withDecay(decayFactor);
                    }
                    return fact;
                })
                .filter(fact -> fact.getWeight() > 0.1) // Remove very low weight facts
                .toList();

        saveFacts(decayedFacts);
    }

}
