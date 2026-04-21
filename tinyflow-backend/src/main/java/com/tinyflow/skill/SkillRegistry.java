package com.tinyflow.skill;

import com.tinyflow.config.TinyFlowProperties;
import com.tinyflow.model.Skill;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing loaded skills.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillRegistry {

    private final SkillLoader skillLoader;
    private final TinyFlowProperties properties;

    @Getter
    private final Map<String, Skill> skills = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        reloadSkills();
    }

    /**
     * Reload all skills from configured directories
     */
    public void reloadSkills() {
        var skillsConfig = properties.getSkills();
        if (skillsConfig == null || skillsConfig.getDirs() == null) {
            log.warn("No skill directories configured");
            return;
        }

        List<Skill> loadedSkills = skillLoader.loadFromDirectories(skillsConfig.getDirs());

        skills.clear();
        for (Skill skill : loadedSkills) {
            skills.put(skill.getName(), skill);
        }

        log.info("Skill registry loaded {} skills", skills.size());
    }

    /**
     * Get a skill by name
     */
    public Optional<Skill> getSkill(String name) {
        return Optional.ofNullable(skills.get(name));
    }

    /**
     * Get all skills as a list
     */
    public List<Skill> getAllSkills() {
        return new ArrayList<>(skills.values());
    }

    /**
     * Check if a skill exists
     */
    public boolean hasSkill(String name) {
        return skills.containsKey(name);
    }

}
