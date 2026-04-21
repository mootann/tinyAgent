package com.tinyflow.skill;

import com.tinyflow.model.Skill;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Document;
import org.commonmark.node.Heading;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Loads skills from Markdown files.
 */
@Slf4j
@Component
public class SkillLoader {

    private final Parser markdownParser = Parser.builder().build();

    /**
     * Load all skills from the given directories
     */
    public List<Skill> loadFromDirectories(List<String> directories) {
        List<Skill> skills = new ArrayList<>();

        for (String dir : directories) {
            Path path = Path.of(dir);
            if (!Files.exists(path)) {
                log.warn("Skill directory does not exist: {}", dir);
                continue;
            }

            try (Stream<Path> files = Files.walk(path)) {
                files.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".md"))
                        .forEach(file -> {
                            try {
                                Skill skill = parseSkillFile(file);
                                if (skill != null) {
                                    skills.add(skill);
                                }
                            } catch (Exception e) {
                                log.warn("Failed to parse skill file: {}", file, e);
                            }
                        });
            } catch (IOException e) {
                log.error("Failed to walk skill directory: {}", dir, e);
            }
        }

        log.info("Loaded {} skills from {}", skills.size(), directories);
        return skills;
    }

    /**
     * Parse a single SKILL.md file
     */
    private Skill parseSkillFile(Path file) throws IOException {
        String content = Files.readString(file);
        String fileName = file.getFileName().toString();
        String skillName = fileName.replace(".md", "");

        Document doc = (Document) markdownParser.parse(content);

        String description = "";
        List<String> keywords = new ArrayList<>();
        String systemPrompt = content; // Default to full content
        int priority = 0;

        // Simple parsing - in production, use frontmatter or structured format
        String[] lines = content.split("\n");
        StringBuilder descBuilder = new StringBuilder();
        boolean inDescription = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("# ")) {
                // Title - skip
                continue;
            }

            if (trimmed.startsWith("## Keywords")) {
                inDescription = false;
            } else if (trimmed.startsWith("## ")) {
                inDescription = false;
            } else if (trimmed.startsWith("- ") && !inDescription) {
                // Could be keywords
                String keyword = trimmed.substring(2).trim();
                if (!keyword.isEmpty()) {
                    keywords.add(keyword.toLowerCase());
                }
            } else if (!trimmed.isEmpty() && description.isEmpty()) {
                // First paragraph as description
                descBuilder.append(trimmed).append(" ");
            }
        }

        if (descBuilder.length() > 0) {
            description = descBuilder.toString().trim();
        }

        // If no explicit keywords, extract from description
        if (keywords.isEmpty()) {
            keywords = extractKeywords(description);
        }

        return Skill.builder()
                .name(skillName)
                .description(description)
                .keywords(keywords)
                .priority(priority)
                .systemPrompt(systemPrompt)
                .build();
    }

    private List<String> extractKeywords(String text) {
        // Simple keyword extraction - in production, use NLP
        List<String> keywords = new ArrayList<>();
        String[] words = text.toLowerCase().split("\\s+");

        for (String word : words) {
            word = word.replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "");
            if (word.length() > 2) {
                keywords.add(word);
            }
        }

        return keywords.stream().distinct().limit(10).toList();
    }

}
