package com.tinyflow.memory;

import com.tinyflow.config.TinyFlowProperties;
import com.tinyflow.model.Fact;
import com.tinyflow.model.Message;
import com.tinyflow.service.EventEmitter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Memory engine - orchestrates Extract -> Score -> Merge -> Inject pipeline.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryEngine {

    private final MemoryStorage storage;
    private final FactExtractor extractor;
    private final FactScorer scorer;
    private final FactMerger merger;
    private final MemoryInjector injector;
    private final TinyFlowProperties properties;

    private ExecutorService executor;

    @PostConstruct
    public void init() {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "memory-processor");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Inject memory context into prompt
     */
    public String inject() {
        var memoryConfig = properties.getMemory();
        storage.applyDecay(memoryConfig.getDecayDays(), memoryConfig.getDecayFactor());

        List<Fact> facts = storage.getFacts();
        return injector.buildPrompt(
                facts,
                memoryConfig.getTokenBudget(),
                memoryConfig.getMinConfidence()
        );
    }

    /**
     * Process conversation asynchronously to extract and store facts
     */
    public void processConversation(List<Message> messages, String threadId) {
        processConversation(messages, threadId, null);
    }

    /**
     * Process conversation asynchronously with event emitter.
     */
    public void processConversation(List<Message> messages, String threadId, EventEmitter emitter) {
        executor.submit(() -> {
            try {
                // Extract facts
                List<Fact> newFacts = extractor.extract(messages, threadId);
                if (newFacts.isEmpty()) {
                    return;
                }

                // Score facts
                List<Fact> existingFacts = storage.getFacts();
                List<Fact> scoredFacts = scorer.score(newFacts, existingFacts);

                // Merge facts
                List<Fact> mergedFacts = merger.merge(scoredFacts, existingFacts);

                // Limit max facts
                var memoryConfig = properties.getMemory();
                if (mergedFacts.size() > memoryConfig.getMaxFacts()) {
                    mergedFacts = mergedFacts.subList(0, memoryConfig.getMaxFacts());
                }

                // Save facts
                storage.saveFacts(mergedFacts);
                log.info("Memory updated: {} new facts processed", newFacts.size());

                // Emit memory_update event
                if (emitter != null) {
                    List<Map<String, Object>> factData = newFacts.stream()
                            .map(f -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("id", f.getId());
                                map.put("content", f.getContent());
                                map.put("category", f.getCategory());
                                map.put("confidence", f.getConfidence());
                                return map;
                            })
                            .collect(java.util.stream.Collectors.toList());
                    emitter.emitMemoryUpdate(factData);
                }
            } catch (Exception e) {
                log.warn("Memory pipeline failed: {}", e.getMessage());
            }
        });
    }

    /**
     * Get all facts (for debugging/admin)
     */
    public List<Fact> getFacts() {
        return storage.getFacts();
    }

    /**
     * Add a new fact manually.
     */
    public Fact addFact(String content, String category, double confidence) {
        Fact fact = Fact.builder()
                .content(content)
                .category(category != null ? category : "general")
                .confidence(confidence)
                .build();

        List<Fact> facts = storage.getFacts();
        facts.add(fact);
        storage.saveFacts(facts);

        log.debug("Added fact: {}", fact.getId());
        return fact;
    }

    /**
     * Delete a fact by ID.
     */
    public void deleteFact(String factId) {
        List<Fact> facts = storage.getFacts();
        facts.removeIf(f -> f.getId().equals(factId));
        storage.saveFacts(facts);
        log.debug("Deleted fact: {}", factId);
    }

}
