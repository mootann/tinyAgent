package com.tinyflow.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration.
 */
@Configuration
@EnableConfigurationProperties(TinyFlowProperties.class)
public class AppConfig {

}
