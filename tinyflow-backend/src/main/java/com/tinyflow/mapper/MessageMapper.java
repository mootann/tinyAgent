package com.tinyflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tinyflow.entity.MessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Message mapper - MyBatis-Plus base mapper for tf_message table
 */
@Mapper
public interface MessageMapper extends BaseMapper<MessageEntity> {

    /**
     * Find messages by thread_id ordered by sequence
     */
    @Select("SELECT * FROM tf_message WHERE thread_id = #{threadId} ORDER BY sequence ASC, created_at ASC")
    List<MessageEntity> findByThreadIdOrderBySequence(@Param("threadId") String threadId);

    /**
     * Find messages by thread_id and role
     */
    @Select("SELECT * FROM tf_message WHERE thread_id = #{threadId} AND role = #{role} ORDER BY sequence ASC")
    List<MessageEntity> findByThreadIdAndRole(@Param("threadId") String threadId, @Param("role") String role);

    /**
     * Get max sequence number for a thread
     */
    @Select("SELECT COALESCE(MAX(sequence), 0) FROM tf_message WHERE thread_id = #{threadId}")
    Integer getMaxSequenceByThreadId(@Param("threadId") String threadId);

    /**
     * Delete messages by thread_id
     */
    @Select("DELETE FROM tf_message WHERE thread_id = #{threadId}")
    void deleteByThreadId(@Param("threadId") String threadId);
}
