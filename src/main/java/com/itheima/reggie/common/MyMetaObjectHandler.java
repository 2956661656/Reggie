package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.itheima.reggie.util.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理器
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入操作，自动填充
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段填充...插入...");

        Long empId = BaseContext.getCurrentId();    //获取当前线程中的用户ID，用于设置

//        log.info(metaObject.toString());
        LocalDateTime now = LocalDateTime.now();
        metaObject.setValue("createTime", now);
        metaObject.setValue("updateTime", now);
        metaObject.setValue("createUser", empId);
        metaObject.setValue("updateUser", empId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段填充...更新...");

        long threadId = Thread.currentThread().getId();
        log.info("线程ID为{}", threadId);

        Long empId = BaseContext.getCurrentId();

//        log.info(metaObject.toString());
        LocalDateTime now = LocalDateTime.now();
        metaObject.setValue("updateTime", now);
        metaObject.setValue("updateUser", empId);
    }
}
