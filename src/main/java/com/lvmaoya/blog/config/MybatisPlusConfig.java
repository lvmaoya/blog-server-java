package com.lvmaoya.blog.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.lvmaoya.blog.handler.mybatis.MyMetaObjectHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({
        "com.lvmaoya.blog.blog.mapper",
        "com.lvmaoya.blog.user.mapper",
        "com.lvmaoya.blog.role.mapper",
        "com.lvmaoya.blog.comment.mapper",
        "com.lvmaoya.blog.category.mapper",
        "com.lvmaoya.blog.todo.mapper",
        "com.lvmaoya.blog.permission.mapper"
})
public class MybatisPlusConfig {

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}