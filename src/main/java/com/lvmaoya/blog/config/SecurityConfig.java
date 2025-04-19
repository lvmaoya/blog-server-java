package com.lvmaoya.blog.config;

import com.lvmaoya.blog.filter.JwtAuthenticationTokenFilter;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Resource
    private AuthenticationEntryPoint authenticationEntryPoint;
    @Resource
    private AccessDeniedHandler accessDeniedHandler;
    @Resource
    private UserDetailsService userDetailsService;
    @Resource
    private CorsFilter corsFilter;
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
     * 配置认证
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    /**
     * SpringSecurity过滤器
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http// 禁用CSRF和默认注销
                    .csrf(AbstractHttpConfigurer::disable)
                    .logout(AbstractHttpConfigurer::disable)

                    // 会话管理配置
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                            .maximumSessions(1)
                            .maxSessionsPreventsLogin(true)
                    )
// 异常处理
                    .exceptionHandling(exception -> exception
                            .accessDeniedHandler(accessDeniedHandler)
                            .authenticationEntryPoint(authenticationEntryPoint)
                    )
                    // 授权配置
                    .authorizeHttpRequests(auth -> auth
                            // 公开访问的端点
                            .requestMatchers(
                                    "/login",
                                    "/captcha",
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/doc.html"
                            ).permitAll()

                            // 角色权限控制
//                            .requestMatchers("/blog/**").hasRole("ADMIN1")
//                            .requestMatchers("/user/**").hasAnyRole("ADMIN", "USER")

                            // 其他请求需要认证
                            .anyRequest().authenticated()
                    )



                    // 添加自定义过滤器
                    .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                    .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
                    // 认证提供者
                    .authenticationProvider(authenticationProvider());
        return http.build();
    }
    /**
     * 配置Web资源忽略规则（静态资源等）
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/favicon.ico",
                "/error",
                "/webjars/**",
                "/static/**",
                "/resources/**"
        );
    }
}
