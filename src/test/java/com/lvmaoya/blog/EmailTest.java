package com.lvmaoya.blog;

import com.lvmaoya.blog.utils.EmailUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailTest {
    @Autowired
    private EmailUtil emailUtil;


    @Test
    public void sendEmail() {
        //发送邮件
        boolean b = emailUtil.sendGeneralEmail("测试邮件", " 这是测试邮件", "1504734652@qq.com");
        System.out.println(b);
    }
}