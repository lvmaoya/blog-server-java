package com.lvmaoya.blog.blog.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogTimeRangeStatsVO implements Serializable {
    private List<String> timeRange;       // 时间范围列表 ["2023-01", "2023-02", ...]
    private List<Integer> articleCount;   // 各时段文章数
    private List<Integer> pageView;       // 各时段阅读量
    private List<Integer> charCount;       // 各时段阅读量
    private List<Integer> preferNum;      // 各时段点赞数
}