package com.tinyflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Message entity - stores conversation messages
 * Maps to tf_message table
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tf_message")
public class MessageEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("thread_id")
    private String threadId;

    /**
     * Role: user, assistant, system, tool
     */
    private String role;

    private String content;

    /**
     * Metadata in JSON format (model, tokens, etc.)
     */
    private String metadata;

    /**
     * Message sequence order in conversation
     */
    private Integer sequence;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // Role constants
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_TOOL = "tool";
}
