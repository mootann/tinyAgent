package com.tinyflow.controller;

import com.tinyflow.entity.MessageEntity;
import com.tinyflow.entity.ThreadEntity;
import com.tinyflow.service.MessageService;
import com.tinyflow.service.ThreadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Thread management controller.
 * Uses MySQL for persistence via ThreadService and MessageService.
 */
@Slf4j
@RestController
@RequestMapping("/api/threads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ThreadController {

    private final ThreadService threadService;
    private final MessageService messageService;
    private final ChatClient chatClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    /**
     * List all threads.
     */
    @GetMapping
    public List<ThreadResponse> listThreads() {
        return threadService.listAllThreads().stream()
                .map(t -> new ThreadResponse(
                        t.getThreadId(),
                        t.getTitle(),
                        DATE_FORMATTER.format(t.getCreatedAt()),
                        DATE_FORMATTER.format(t.getUpdatedAt()),
                        messageService.getMessageCount(t.getThreadId())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Create a new thread.
     */
    @PostMapping
    public ThreadCreateResponse createThread() {
        ThreadEntity thread = threadService.createThread("新对话");
        log.debug("Created thread: {}", thread.getThreadId());

        return new ThreadCreateResponse(
                thread.getThreadId(),
                thread.getTitle(),
                DATE_FORMATTER.format(thread.getCreatedAt())
        );
    }

    /**
     * Get thread details.
     */
    @GetMapping("/{threadId}")
    public ThreadDetailResponse getThread(@PathVariable String threadId,
                                          @RequestParam(defaultValue = "false") boolean messages) {
        ThreadEntity thread = threadService.getByThreadId(threadId).orElse(null);
        if (thread == null) {
            return new ThreadDetailResponse(threadId, "新对话", "", "", 0, List.of());
        }

        List<MessageResponse> msgList = List.of();
        if (messages) {
            msgList = loadMessages(threadId);
        }

        return new ThreadDetailResponse(
                thread.getThreadId(),
                thread.getTitle(),
                DATE_FORMATTER.format(thread.getCreatedAt()),
                DATE_FORMATTER.format(thread.getUpdatedAt()),
                messageService.getMessageCount(threadId),
                msgList
        );
    }

    /**
     * Update thread.
     */
    @PatchMapping("/{threadId}")
    public ThreadResponse updateThread(@PathVariable String threadId,
                                       @RequestBody UpdateThreadRequest request) {
        ThreadEntity thread = threadService.getOrCreateThread(threadId, "新对话");

        String title = thread.getTitle();
        if (request.title() != null) {
            title = request.title();
            threadService.updateTitle(threadId, title);
        } else if (request.firstMessage() != null) {
            title = autoTitle(request.firstMessage());
            threadService.updateTitle(threadId, title);
        }

        // Save messages if provided
        if (request.messages() != null) {
            saveMessagesFromRequest(threadId, request.messages());
        }

        return new ThreadResponse(
                threadId,
                title,
                DATE_FORMATTER.format(thread.getCreatedAt()),
                DATE_FORMATTER.format(thread.getUpdatedAt()),
                messageService.getMessageCount(threadId)
        );
    }

    /**
     * Delete a thread.
     */
    @DeleteMapping("/{threadId}")
    public Map<String, String> deleteThread(@PathVariable String threadId) {
        threadService.deleteThread(threadId);
        messageService.deleteMessagesByThreadId(threadId);

        log.debug("Deleted thread: {}", threadId);
        return Map.of("status", "deleted");
    }

    /**
     * Get messages for a thread.
     */
    @GetMapping("/{threadId}/messages")
    public List<MessageResponse> getMessages(@PathVariable String threadId) {
        return loadMessages(threadId);
    }

    private List<MessageResponse> loadMessages(String threadId) {
        return messageService.getMessagesByThreadId(threadId).stream()
                .map(m -> new MessageResponse(
                        m.getRole(),
                        m.getContent(),
                        DATE_FORMATTER.format(m.getCreatedAt())
                ))
                .collect(Collectors.toList());
    }

    private void saveMessagesFromRequest(String threadId, List<Map<String, Object>> messages) {
        // This is used for bulk importing messages (e.g., from frontend)
        for (Map<String, Object> msg : messages) {
            String role = (String) msg.get("role");
            String content = (String) msg.get("content");
            if (role != null && content != null) {
                if ("user".equals(role)) {
                    messageService.saveUserMessage(threadId, content);
                } else if ("assistant".equals(role)) {
                    messageService.saveAssistantMessage(threadId, content);
                }
            }
        }
    }

    /**
     * Auto-generate title from first message using LLM.
     * Mirrors Python implementation.
     */
    private String autoTitle(String message) {
        try {
            String response = chatClient.prompt()
                    .system("根据用户的第一条消息，生成一个简洁的对话标题（5-15个字）。只输出标题文字，不要引号、标点或解释。")
                    .user(message)
                    .call()
                    .content();

            String title = response.trim().replaceAll("^['\"\"'\"]+|['\"\"'\"]+$", "");
            if (title.length() >= 2 && title.length() <= 30) {
                return title;
            }
        } catch (Exception e) {
            log.warn("Failed to generate title using LLM, falling back to truncation: {}", e.getMessage());
        }

        // Fallback: truncate first line
        String firstLine = message.trim().split("\n")[0].trim();
        if (firstLine.length() <= 30) {
            return firstLine;
        }
        return firstLine.substring(0, 27) + "...";
    }

    // Record classes for requests and responses
    public record ThreadResponse(String threadId, String title, String createdAt, String updatedAt,
                                  long messageCount) {
    }

    public record ThreadCreateResponse(String threadId, String title, String createdAt) {
    }

    public record ThreadDetailResponse(String threadId, String title, String createdAt,
                                        String updatedAt, long messageCount,
                                        List<MessageResponse> messages) {
    }

    public record MessageResponse(String role, String content, String timestamp) {
    }

    public record UpdateThreadRequest(String title, String firstMessage,
                                       List<Map<String, Object>> messages) {
    }

}
