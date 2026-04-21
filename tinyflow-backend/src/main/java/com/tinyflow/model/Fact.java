package com.tinyflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * A fact extracted from conversation for the memory system.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Fact implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    /**
     * The fact content
     */
    private String content;

    /**
     * Confidence score (0.0 - 1.0)
     */
    @Builder.Default
    private double confidence = 0.7;

    /**
     * When this fact was created
     */
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * Last access time for decay calculation
     */
    @Builder.Default
    private Instant lastAccessed = Instant.now();

    /**
     * Source thread ID
     */
    private String threadId;

    /**
     * Category of the fact (e.g., "general", "preference", "fact")
     */
    @Builder.Default
    private String category = "general";

    /**
     * Weight for decay calculation (starts at 1.0)
     */
    @Builder.Default
    private double weight = 1.0;

    /**
     * Apply decay to this fact
     */
    public Fact withDecay(double decayFactor) {
        return this.toBuilder()
                .weight(this.weight * decayFactor)
                .build();
    }

    /**
     * Update last accessed time
     */
    public Fact accessed() {
        return this.toBuilder()
                .lastAccessed(Instant.now())
                .build();
    }

}
