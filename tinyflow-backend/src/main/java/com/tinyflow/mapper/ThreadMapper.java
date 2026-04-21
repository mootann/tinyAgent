package com.tinyflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tinyflow.entity.ThreadEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Thread mapper - MyBatis-Plus base mapper for tf_thread table
 */
@Mapper
public interface ThreadMapper extends BaseMapper<ThreadEntity> {

    /**
     * Find thread by thread_id
     */
    @Select("SELECT * FROM tf_thread WHERE thread_id = #{threadId}")
    ThreadEntity findByThreadId(@Param("threadId") String threadId);

    /**
     * List all threads ordered by updated_at desc
     */
    @Select("SELECT * FROM tf_thread ORDER BY updated_at DESC")
    List<ThreadEntity> listAllOrderByUpdatedAtDesc();

    /**
     * Find threads by status
     */
    @Select("SELECT * FROM tf_thread WHERE status = #{status} ORDER BY updated_at DESC")
    List<ThreadEntity> findByStatus(@Param("status") Integer status);
}
