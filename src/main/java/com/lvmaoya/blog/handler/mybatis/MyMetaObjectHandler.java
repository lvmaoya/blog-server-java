package com.lvmaoya.blog.handler.mybatis;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.util.Date;


@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {


    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("执行插入");
        this.strictInsertFill(metaObject, "publishedTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "createdTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updatedTime",  Date.class, new Date());
    }


    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("执行更新");
        this.strictUpdateFill(metaObject, "updatedTime", Date.class, new Date());
    }
}