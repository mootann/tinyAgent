package com.tinyflow.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus meta object handler for auto-filling create_time and update_time
 */
@Slf4j
@Component
public class CustomMetaObjectHandler implements com.baomidou.mybatisplus.core.handlers.MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("Auto-filling create_time and update_time for insert");
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("Auto-filling update_time for update");
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
