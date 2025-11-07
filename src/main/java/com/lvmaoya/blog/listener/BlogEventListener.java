package com.lvmaoya.blog.listener;

import com.lvmaoya.blog.event.BlogSavedEvent;
import com.lvmaoya.blog.service.AsyncBlogService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BlogEventListener {

    private static final Logger log = LoggerFactory.getLogger(BlogEventListener.class);

    @Resource
    private AsyncBlogService asyncBlogService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskExecutor")
    public void onBlogSaved(BlogSavedEvent event) {
        try {
            if (event == null) return;
            // 根据标志决定是否生成摘要
            if (event.isKeepDesc()) {
                log.info("事务已提交，跳过摘要生成（keepDesc=true），blogId={}", event.getBlogId());
            } else {
                log.info("事务已提交，开始异步生成摘要，blogId={}", event.getBlogId());
                asyncBlogService.updateBlog(event.getBlogId());

                // 进行向量索引增量重建
                log.info("事务已提交，开始异步重建向量索引，blogId={}", event.getBlogId());
                asyncBlogService.upsertRagIndex(event.getBlogId().longValue());
            }
        } catch (Exception e) {
            log.error("异步摘要生成失败，blogId={}", event != null ? event.getBlogId() : null, e);
        }
    }
}