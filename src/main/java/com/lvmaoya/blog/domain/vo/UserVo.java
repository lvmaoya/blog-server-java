package com.lvmaoya.blog.domain.vo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo implements Serializable {
    private Integer id;
    private String name;
    private String nickName;
    private String email;
    private String mobile;
    private String otherContact;
    private String introduce;
    private String avatar;
    private String createdAt;
    private String updatedAt;
}
