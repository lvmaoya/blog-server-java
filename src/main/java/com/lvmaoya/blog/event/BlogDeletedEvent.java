package com.lvmaoya.blog.event;

public class BlogDeletedEvent {
    private final Integer blogId;

    public BlogDeletedEvent(Integer blogId) {
        this.blogId = blogId;
    }

    public Integer getBlogId() {
        return blogId;
    }
}