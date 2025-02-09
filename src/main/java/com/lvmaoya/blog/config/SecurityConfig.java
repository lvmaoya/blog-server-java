package com.lvmaoya.blog.config;

import com.lvmaoya.blog.filter.JwtAuthenticationTokenFilter;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Resource
    private AccessDeniedHandler accessDeniedHandler;
    @Resource
    private AuthenticationEntryPoint authenticationEntryPoint;

    /**
     * 配置密码加密方式
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * SpringSecurity 认证管理器:实现自定义账号密码登录
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    /**
     * SpringSecurity过滤器
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.sessionManagement(session -> session.maximumSessions(1).maxSessionsPreventsLogin(true))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/login").permitAll()//登录和未登录的人都可以访问
                            .requestMatchers("/captcha").permitAll()//登录和未登录的人都可以访问
                            .requestMatchers("/todo*").permitAll()//登录和未登录的人都可以访问
                            .requestMatchers("/todo/*").permitAll()//登录和未登录的人都可以访问
                            .anyRequest().authenticated())//其它所有请求需要认证访问
                    .csrf(AbstractHttpConfigurer::disable)//防止跨域伪造
                    .logout(AbstractHttpConfigurer::disable);// 禁用 Spring Security 的默认注销功能

            // 配置认证失败的处理器
            http.exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(authenticationEntryPoint));

            // 把jwtAuthenticationTokenFilter添加到SpringSecurity的过滤器中
            http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
    }
}
