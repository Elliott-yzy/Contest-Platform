package com.w16a.danish.judge.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Eddy ZHANG
 * @date 2025/03/18
 * @description MyMetaObjectHandler for auto fill createdAt and updatedAt
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * Auto fill createdAt and updatedA
     *
     * @param metaObject metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "joinedAt", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * Auto fill updatedAt
     *
     * @param metaObject metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
