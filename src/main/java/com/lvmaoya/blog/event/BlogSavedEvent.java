package com.lvmaoya.blog.event;

public class BlogSavedEvent {
    private final Integer blogId;
    private final boolean keepDesc;

    public BlogSavedEvent(Integer blogId, boolean keepDesc) {
        this.blogId = blogId;
        this.keepDesc = keepDesc;
    }

    public Integer getBlogId() {
        return blogId;
    }

    public boolean isKeepDesc() {
        return keepDesc;
    }
}