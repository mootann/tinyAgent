package com.tinyflow.controller;

import com.tinyflow.memory.MemoryEngine;
import com.tinyflow.model.Fact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Memory management controller.
 * Mirrors Python implementation.
 */
@Slf4j
@RestController
@RequestMapping("/api/memory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MemoryController {

    private final MemoryEngine memoryEngine;

    /**
     * Get all memory facts.
     */
    @GetMapping
    public MemoryResponse getMemory() {
        List<Fact> facts = memoryEngine.getFacts();

        List<FactResponse> factResponses = facts.stream()
                .map(f -> new FactResponse(
                        f.getId(),
                        f.getContent(),
                        f.getCategory(),
                        f.getConfidence()
                ))
                .toList();

        return new MemoryResponse(
                factResponses,
                Map.of("total", facts.size())
        );
    }

    /**
     * Add a new memory fact.
     */
    @PostMapping
    public FactResponse addMemory(@RequestBody AddFactRequest request) {
        Fact fact = memoryEngine.addFact(request.content(), request.category(), request.confidence());
        return new FactResponse(fact.getId(), fact.getContent(), fact.getCategory(), fact.getConfidence());
    }

    /**
     * Delete a memory fact.
     */
    @DeleteMapping("/{factId}")
    public Map<String, String> deleteMemory(@PathVariable String factId) {
        memoryEngine.deleteFact(factId);
        return Map.of("status", "deleted");
    }

    // Record classes
    public record MemoryResponse(List<FactResponse> facts, Map<String, Object> stats) {
    }

    public record FactResponse(String id, String content, String category, double confidence) {
    }

    public record AddFactRequest(String content, String category, Double confidence) {
        public AddFactRequest {
            if (category == null) {
                category = "general";
            }
            if (confidence == null) {
                confidence = 1.0;
            }
        }
    }

}
