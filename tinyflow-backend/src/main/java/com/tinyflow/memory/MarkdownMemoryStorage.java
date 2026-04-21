package com.tinyflow.memory;

import com.tinyflow.config.TinyFlowProperties;
import com.tinyflow.model.Fact;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Markdown-based memory storage.
 * Stores facts as human-readable Markdown files organized by category.
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class MarkdownMemoryStorage implements MemoryStorage {

    private final TinyFlowProperties properties;
    private Path storagePath;
    private Path indexPath;

    private static final String FACTS_FILENAME = "facts.md";
    private static final String INDEX_FILENAME = "index.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final Pattern FACT_PATTERN = Pattern.compile("- \\[(\\d\\.\\d+)\\] (.+) \\| (\\d{4}-\\d{2}-\\d{2}) \\| verified: (\\d+)");

    @PostConstruct
    public void init() {
        String pathStr = properties.getMemory().getStoragePath();
        this.storagePath = Path.of(pathStr != null ? pathStr : "./data/memory");
        this.indexPath = storagePath.resolve(INDEX_FILENAME);
        
        try {
            Files.createDirectories(storagePath);
            log.info("Markdown memory storage initialized at: {}", storagePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create memory storage directory: {}", storagePath, e);
            throw new RuntimeException("Failed to initialize memory storage", e);
        }
    }

    @Override
    public List<Fact> getFacts() {
        List<Fact> allFacts = new ArrayList<>();
        
        try {
            if (!Files.exists(storagePath)) {
                return allFacts;
            }

            // Walk through all thread directories
            try (var stream = Files.list(storagePath)) {
                stream.filter(Files::isDirectory)
                        .forEach(threadDir -> {
                            Path factsFile = threadDir.resolve(FACTS_FILENAME);
                            if (Files.exists(factsFile)) {
                                allFacts.addAll(readFactsFromFile(factsFile, threadDir.getFileName().toString()));
                            }
                        });
            }
        } catch (IOException e) {
            log.error("Failed to read facts from storage", e);
        }

        return allFacts;
    }

    @Override
    public void saveFacts(List<Fact> facts) {
        if (facts == null || facts.isEmpty()) {
            return;
        }

        // Group facts by threadId
        Map<String, List<Fact>> factsByThread = facts.stream()
                .filter(f -> f.getThreadId() != null)
                .collect(Collectors.groupingBy(Fact::getThreadId));

        // Also handle facts without threadId (put in "general")
        List<Fact> generalFacts = facts.stream()
                .filter(f -> f.getThreadId() == null)
                .toList();
        if (!generalFacts.isEmpty()) {
            factsByThread.put("general", generalFacts);
        }

        // Save each group to its own file
        factsByThread.forEach((threadId, threadFacts) -> {
            try {
                saveFactsToThreadFile(threadId, threadFacts);
            } catch (IOException e) {
                log.error("Failed to save facts for thread: {}", threadId, e);
            }
        });

        // Update index
        updateIndex();
    }

    /**
     * Read facts from a markdown file
     */
    private List<Fact> readFactsFromFile(Path file, String threadId) {
        List<Fact> facts = new ArrayList<>();
        
        try {
            String content = Files.readString(file);
            String[] lines = content.split("\n");
            
            String currentCategory = "general";
            
            for (String line : lines) {
                line = line.trim();
                
                // Check for category header
                if (line.startsWith("## ")) {
                    currentCategory = line.substring(3).trim().toLowerCase();
                    continue;
                }
                
                // Parse fact entry
                Matcher matcher = FACT_PATTERN.matcher(line);
                if (matcher.matches()) {
                    double confidence = Double.parseDouble(matcher.group(1));
                    String factContent = matcher.group(2);
                    String dateStr = matcher.group(3);
                    int accessCount = Integer.parseInt(matcher.group(4));
                    
                    LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
                    Instant createdAt = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                    
                    Fact fact = Fact.builder()
                            .content(factContent)
                            .category(currentCategory)
                            .confidence(confidence)
                            .createdAt(createdAt)
                            .lastAccessed(createdAt) // Simplified - use created date
                            .threadId(threadId)
                            .weight(1.0)
                            .build();
                    
                    facts.add(fact);
                }
            }
        } catch (IOException e) {
            log.error("Failed to read facts file: {}", file, e);
        }
        
        return facts;
    }

    /**
     * Save facts to a thread-specific markdown file
     */
    private void saveFactsToThreadFile(String threadId, List<Fact> facts) throws IOException {
        Path threadDir = storagePath.resolve(threadId);
        Files.createDirectories(threadDir);
        
        Path factsFile = threadDir.resolve(FACTS_FILENAME);
        
        // Group facts by category
        Map<String, List<Fact>> factsByCategory = facts.stream()
                .collect(Collectors.groupingBy(f -> f.getCategory() != null ? f.getCategory() : "general"));
        
        // Build markdown content
        StringBuilder sb = new StringBuilder();
        sb.append("# Memory Facts - Thread: ").append(threadId).append("\n\n");
        sb.append("*Auto-generated memory storage*\n\n");
        
        // Define category order
        List<String> categoryOrder = List.of("preference", "context", "behavior", "knowledge", "general");
        
        for (String category : categoryOrder) {
            List<Fact> categoryFacts = factsByCategory.get(category);
            if (categoryFacts == null || categoryFacts.isEmpty()) {
                continue;
            }
            
            sb.append("## ").append(category).append("\n\n");
            
            for (Fact fact : categoryFacts) {
                String dateStr = DATE_FORMATTER.format(fact.getCreatedAt());
                int accessCount = 1; // Simplified
                
                sb.append(String.format("- [%.2f] %s | %s | verified: %d\n",
                        fact.getConfidence(),
                        fact.getContent(),
                        dateStr,
                        accessCount));
            }
            
            sb.append("\n");
        }
        
        // Write to file
        Files.writeString(factsFile, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.debug("Saved {} facts to {}", facts.size(), factsFile);
    }

    /**
     * Update the index file
     */
    private void updateIndex() {
        try {
            Map<String, Object> index = new HashMap<>();
            index.put("updated_at", Instant.now().toString());
            
            List<Map<String, String>> entries = new ArrayList<>();
            
            try (var stream = Files.list(storagePath)) {
                stream.filter(Files::isDirectory)
                        .forEach(threadDir -> {
                            Map<String, String> entry = new HashMap<>();
                            entry.put("thread_id", threadDir.getFileName().toString());
                            entry.put("path", threadDir.toString());
                            entries.add(entry);
                        });
            }
            
            index.put("threads", entries);
            
            // Simple JSON serialization
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"updated_at\": \"").append(index.get("updated_at")).append("\",\n");
            json.append("  \"threads\": [\n");
            
            for (int i = 0; i < entries.size(); i++) {
                Map<String, String> entry = entries.get(i);
                json.append("    {\n");
                json.append("      \"thread_id\": \"").append(entry.get("thread_id")).append("\",\n");
                json.append("      \"path\": \"").append(entry.get("path")).append("\"\n");
                json.append("    }");
                if (i < entries.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("  ]\n");
            json.append("}\n");
            
            Files.writeString(indexPath, json.toString());
        } catch (IOException e) {
            log.error("Failed to update index", e);
        }
    }

    @Override
    public void applyDecay(int decayDays, double decayFactor) {
        List<Fact> facts = getFacts();
        Instant cutoff = Instant.now().minusSeconds(decayDays * 86400L);

        List<Fact> decayedFacts = facts.stream()
                .map(fact -> {
                    if (fact.getLastAccessed().isBefore(cutoff)) {
                        return fact.withDecay(decayFactor);
                    }
                    return fact;
                })
                .filter(fact -> fact.getWeight() > 0.1)
                .collect(Collectors.toList());

        saveFacts(decayedFacts);
        log.debug("Applied decay to {} facts", facts.size());
    }
}
