package com.tinyflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thread entity - stores conversation metadata
 * Maps to tf_thread table
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tf_thread")
public class ThreadEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("thread_id")
    private String threadId;

    private String title;

    @TableField("user_id")
    private Long userId;

    /**
     * Status: 0-active, 1-waiting, 2-completed
     */
    private Integer status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // Status constants
    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_WAITING = 1;
    public static final int STATUS_COMPLETED = 2;
}
