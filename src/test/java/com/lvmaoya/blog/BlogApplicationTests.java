package com.lvmaoya.blog;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class BlogApplicationTests {

    @Test
    void contextLoads() {
    }


    @Resource
    PasswordEncoder passwordEncoder;

    @Test
    public void getPasswordEncoder() {
        String encode = passwordEncoder.encode("123456");
        System.out.println(encode);
    }

}
