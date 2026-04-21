package com.tinyflow.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tinyflow.entity.ThreadEntity;
import com.tinyflow.mapper.ThreadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Thread service - manages conversation threads using MySQL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadService extends ServiceImpl<ThreadMapper, ThreadEntity> {

    private final ThreadMapper threadMapper;

    /**
     * Create a new thread
     */
    @Transactional
    public ThreadEntity createThread(String title) {
        String threadId = generateThreadId();
        
        ThreadEntity thread = ThreadEntity.builder()
                .threadId(threadId)
                .title(title != null ? title : "新对话")
                .status(ThreadEntity.STATUS_ACTIVE)
                .build();
        
        save(thread);
        log.info("Created thread: {}", threadId);
        return thread;
    }

    /**
     * Get thread by thread_id
     */
    public Optional<ThreadEntity> getByThreadId(String threadId) {
        return Optional.ofNullable(threadMapper.findByThreadId(threadId));
    }

    /**
     * Get or create thread
     */
    @Transactional
    public ThreadEntity getOrCreateThread(String threadId, String title) {
        return getByThreadId(threadId)
                .orElseGet(() -> {
                    ThreadEntity thread = ThreadEntity.builder()
                            .threadId(threadId)
                            .title(title != null ? title : "新对话")
                            .status(ThreadEntity.STATUS_ACTIVE)
                            .build();
                    save(thread);
                    return thread;
                });
    }

    /**
     * Update thread title
     */
    @Transactional
    public void updateTitle(String threadId, String title) {
        ThreadEntity thread = threadMapper.findByThreadId(threadId);
        if (thread != null) {
            thread.setTitle(title);
            updateById(thread);
            log.debug("Updated thread title: {} -> {}", threadId, title);
        }
    }

    /**
     * Update thread status
     */
    @Transactional
    public void updateStatus(String threadId, int status) {
        ThreadEntity thread = threadMapper.findByThreadId(threadId);
        if (thread != null) {
            thread.setStatus(status);
            updateById(thread);
            log.debug("Updated thread status: {} -> {}", threadId, status);
        }
    }

    /**
     * List all threads ordered by updated_at desc
     */
    public List<ThreadEntity> listAllThreads() {
        return threadMapper.listAllOrderByUpdatedAtDesc();
    }

    /**
     * Delete thread and related data
     */
    @Transactional
    public void deleteThread(String threadId) {
        ThreadEntity thread = threadMapper.findByThreadId(threadId);
        if (thread != null) {
            removeById(thread.getId());
            log.info("Deleted thread: {}", threadId);
        }
    }

    /**
     * Generate unique thread ID
     */
    private String generateThreadId() {
        return UUID.randomUUID().toString().substring(0, 12);
    }
}
