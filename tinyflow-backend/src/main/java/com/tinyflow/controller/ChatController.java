package com.tinyflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinyflow.model.AgentGraphState;
import com.tinyflow.service.EventEmitter;
import com.tinyflow.service.GraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;

/**
 * Chat controller with SSE streaming support.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final GraphService graphService;
    private final ObjectMapper objectMapper;

    /**
     * Chat endpoint with SSE streaming.
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody ChatRequest request) {
        log.debug("Chat request: thread={}, message={}", request.threadId(), request.message());

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        // Create event emitter for this request
        EventEmitter emitter = new EventEmitter(request.threadId(), sink, objectMapper);

        // Execute graph asynchronously
        Schedulers.boundedElastic().schedule(() -> {
            try {
                log.info("[Chat] Starting graph execution for thread: {}", request.threadId());

                // Send initial thinking event
                emitter.emitThinking("router", "正在分析您的问题...");

                // Execute graph with event emitter
                AgentGraphState result = graphService.execute(request.message(), request.threadId(), emitter);

                // Extract and send final response
                String response = extractResponse(result);
                log.info("[Chat] Extracted response: {}", response.substring(0, Math.min(100, response.length())));
                emitter.emitContent(response);

                // Send done event
                emitter.emitDone();
                log.info("[Chat] Completed graph execution for thread: {}", request.threadId());
                sink.tryEmitComplete();

            } catch (Exception e) {
                log.error("Chat execution failed", e);
                emitter.emitError(e.getMessage());
                sink.tryEmitComplete();
            } finally {
                EventEmitter.remove(request.threadId());
            }
        });

        return sink.asFlux()
                .delayElements(Duration.ofMillis(50))
                .map(jsonStr -> {
                    try {
                        Map<String, Object> eventData = objectMapper.readValue(jsonStr, Map.class);
                        String eventType = (String) eventData.get("event");
                        String data = (String) eventData.get("data");
                        return ServerSentEvent.<String>builder()
                                .event(eventType)
                                .data(data)
                                .build();
                    } catch (Exception e) {
                        log.error("Failed to parse event", e);
                        return ServerSentEvent.<String>builder()
                                .event("error")
                                .data("{\"error\":\"Failed to parse event\"}")
                                .build();
                    }
                });
    }

    private String createEvent(String event, Map<String, Object> data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            return "data: " + jsonData + "\n\n";
        } catch (Exception e) {
            log.error("Failed to create event", e);
            return "data: {}\n\n";
        }
    }

    private String extractResponse(AgentGraphState state) {
        var messages = state.messages();
        if (messages.isEmpty()) {
            return "抱歉，无法生成回复。";
        }

        // Get last assistant message
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getRole().toString().equals("ASSISTANT")) {
                return messages.get(i).getContent();
            }
        }

        return "抱歉，无法生成回复。";
    }

    public record ChatRequest(String threadId, String message, String model) {
        public ChatRequest {
            if (threadId == null || threadId.isEmpty()) {
                threadId = "default";
            }
        }
    }


}
