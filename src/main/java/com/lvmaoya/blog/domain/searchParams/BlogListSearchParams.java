package com.lvmaoya.blog.domain.searchParams;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class BlogListSearchParams {
    private Integer page;
    private Integer size;
    private String title;
    private String keywords;
    private String category;
    private String sortBy;
    private String sortOrder;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishedStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishedEnd;
}
