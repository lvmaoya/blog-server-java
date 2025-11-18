package com.lvmaoya.blog.filter;

import com.lvmaoya.blog.auth.entity.CustomUserDetails;
import com.lvmaoya.blog.user.entity.User;
import com.lvmaoya.blog.user.mapper.UserMapper;
import com.lvmaoya.blog.utils.JwtUtil;
import com.lvmaoya.blog.utils.RedisCacheUtil;
import com.lvmaoya.blog.utils.WebUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

// spring security认证过滤器
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Resource
    private RedisCacheUtil redisCacheUtil;

    @Resource
    private UserMapper userMapper;

    public JwtAuthenticationTokenFilter(RedisCacheUtil redisCacheUtil) {
        this.redisCacheUtil = redisCacheUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 获取请求头中的token
        String authorization = request.getHeader("Authorization");
        logger.info(authorization);
        // 白名单放行等
        if(authorization == null){
            filterChain.doFilter(request, response);
            return;
        }

        // 解析获取userId
        Claims claims = null;
        try {
            claims = JwtUtil.getAllClaimsFromToken(authorization);
        }catch (Exception e){
            logger.error("Failed to parse token: ", e);
            WebUtil.renderUnauthorized(response);
            // token 超时、非法
            return;
        }
        String userId = claims.getSubject();


        try {
            // 数据库校验
            User user = userMapper.selectById(userId);
            if (user == null || user.getDeleted() == 1) {
                // 账号不存在 / 已逻辑删除 / 已禁用
                WebUtil.renderUnauthorized(response);
                return;
            }
            // 从redis中获取用户 如果转换失败就可以提示重新登录
            CustomUserDetails customUserDetails = (CustomUserDetails) redisCacheUtil.get("blogLogin" + userId);

            // 如何没有这个用户，说明登录过期，提示重新登录
            if(Objects.isNull(customUserDetails)){
                WebUtil.renderUnauthorized(response);
                // token 超时、非法
                return;
            }

            //存入SecurityContextHolder中
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 放行
            filterChain.doFilter(request, response);
        }catch (Exception e){
            WebUtil.renderUnauthorized(response);
        }
    }
}