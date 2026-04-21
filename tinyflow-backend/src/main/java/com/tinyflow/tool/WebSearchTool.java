package com.tinyflow.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web search tool using Tavily API.
 */
@Slf4j
@Component
public class WebSearchTool implements Tool {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${tavily.api.key:${TAVILY_API_KEY:}}")
    private String tavilyApiKey;

    private static final String TAVILY_API_URL = "https://api.tavily.com/search";

    @Override
    public String getName() {
        return "web_search";
    }

    @Override
    public String getDescription() {
        return "Search the web for information. Use this tool to find current information, news, articles, and facts.";
    }

    @Override
    public String execute(Map<String, Object> params) {
        String query = (String) params.get("query");
        int maxResults = params.get("max_results") != null ?
                ((Number) params.get("max_results")).intValue() : 5;

        if (query == null || query.isEmpty()) {
            return "{\"error\": \"Query is required\"}";
        }

        try {
            // Check if API key is available
            if (tavilyApiKey == null || tavilyApiKey.isEmpty()) {
                log.warn("TAVILY_API_KEY not set, returning mock result");
                return createMockResult(query);
            }

            log.info("Using Tavily API key: {}...", tavilyApiKey.substring(0, Math.min(10, tavilyApiKey.length())));

            // Call Tavily API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("api_key", tavilyApiKey);
            requestBody.put("query", query);
            requestBody.put("max_results", maxResults);
            requestBody.put("search_depth", "basic");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    TAVILY_API_URL,
                    requestBody,
                    Map.class
            );

            if (response.getBody() != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("query", query);

                List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
                if (results != null) {
                    result.put("total_results", results.size());
                    result.put("results", results.stream()
                            .map(r -> {
                                Map<String, Object> item = new HashMap<>();
                                item.put("title", r.getOrDefault("title", ""));
                                item.put("url", r.getOrDefault("url", ""));
                                item.put("content", r.getOrDefault("content", ""));
                                return item;
                            })
                            .toList());
                } else {
                    result.put("total_results", 0);
                    result.put("results", List.of());
                }

                return objectMapper.writeValueAsString(result);
            }

            return "{\"error\": \"Empty response from search API\", \"query\": \"" + query + "\"}";

        } catch (Exception e) {
            log.error("Web search failed: {}", e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\", \"query\": \"" + query + "\"}";
        }
    }

    private String createMockResult(String query) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("query", query);
            result.put("total_results", 1);
            Map<String, Object> mockItem = new HashMap<>();
            mockItem.put("title", "Search results for: " + query);
            mockItem.put("url", "https://example.com/search");
            mockItem.put("content", "This is a mock search result. Please set TAVILY_API_KEY environment variable for real search functionality.");
            result.put("results", List.of(mockItem));
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\": \"Failed to create mock result\"}";
        }
    }
}
