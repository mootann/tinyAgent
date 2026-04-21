package com.tinyflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "tinyflow")
public class TinyFlowProperties {

    private ModelConfig model;
    private ExecutorConfig executor;
    private MemoryConfig memory;
    private SkillsConfig skills;
    private GraphConfig graph;

    @Data
    public static class ModelConfig {
        private String defaultModel;
        private List<ProviderConfig> providers;
    }

    @Data
    public static class ProviderConfig {
        private String name;
        private String apiKeyEnv;
    }

    @Data
    public static class ExecutorConfig {
        private int schedulerWorkers = 3;
        private int executionWorkers = 3;
        private int defaultTimeout = 300;
    }

    @Data
    public static class MemoryConfig {
        private int tokenBudget = 500;
        private int decayDays = 30;
        private double decayFactor = 0.8;
        private double minConfidence = 0.7;
        private int maxFacts = 50;
        private String storageType = "markdown";
        private String storagePath = "./data/memory";
    }

    @Data
    public static class SkillsConfig {
        private List<String> dirs;
        private boolean hotReload = true;
    }

    @Data
    public static class GraphConfig {
        private int maxIterations = 3;
    }

}
