package com.lvmaoya.blog.domain.dto;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import lombok.Data;

@Data
public class UpdateCommentStatusDto {
    private Integer commentId;
    private Integer status;
}