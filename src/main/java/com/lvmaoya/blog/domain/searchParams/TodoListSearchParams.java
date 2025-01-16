package com.lvmaoya.blog.domain.searchParams;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodoListSearchParams {
    private Integer page = 1;
    private Integer size = 999;
    private int id = -1;
    private String taskName;
    private String description;
    private int progress = -1;
    private String priority;
    private String category;
    private String assignee;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdEnd;
}
