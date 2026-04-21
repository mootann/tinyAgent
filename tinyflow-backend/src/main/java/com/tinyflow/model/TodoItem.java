package com.tinyflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * A single step in the execution plan.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TodoItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    private String id;

    private String content;

    @Builder.Default
    private Status status = Status.PENDING;

    private String error;

    /**
     * Custom builder to ensure unique ID generation
     */
    public static class TodoItemBuilder {
        private Status status = Status.PENDING;
        
        public TodoItem build() {
            if (id == null) {
                id = "todo_" + UUID.randomUUID().toString().substring(0, 8);
            }
            return new TodoItem(id, content, status, error);
        }
    }

    /**
     * Convert from Map (for JSON deserialization compatibility)
     */
    public static TodoItem fromMap(Map<String, Object> map) {
        TodoItem item = new TodoItem();
        item.id = map.get("id") != null ? map.get("id").toString() : "todo_" + UUID.randomUUID().toString().substring(0, 6);
        item.content = map.get("content") != null ? map.get("content").toString() : null;
        item.status = map.get("status") != null ? Status.valueOf(map.get("status").toString()) : Status.PENDING;
        item.error = map.get("error") != null ? map.get("error").toString() : null;
        return item;
    }

}
