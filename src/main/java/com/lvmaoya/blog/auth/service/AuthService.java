package com.lvmaoya.blog.auth.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.lvmaoya.blog.auth.entity.CustomUserDetails;
import com.lvmaoya.blog.auth.pojo.LoginUserVo;
import com.lvmaoya.blog.common.pojo.R;
import com.lvmaoya.blog.handler.exception.BusinessException;
import com.lvmaoya.blog.user.pojo.UserVo;
import com.lvmaoya.blog.utils.BeanCopyUtil;
import com.lvmaoya.blog.utils.JwtUtil;
import com.lvmaoya.blog.utils.RedisCacheUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    final AuthenticationManager authenticationManager;
    final RedisCacheUtil redisCacheUtil;

    public R<LoginUserVo> login(String username, String password, String captcha, HttpServletRequest request) {
        try{
            // 0. 校验验证码
            String sessionCaptcha = (String) request.getSession().getAttribute("captcha");
            if (sessionCaptcha == null) {
                throw new BusinessException(400, "验证码已失效，请刷新后重试");
            }
            if (!sessionCaptcha.equalsIgnoreCase(captcha)) {
                throw new BusinessException(400, "验证码错误");
            }
            // 验证码一次性使用
            request.getSession().removeAttribute("captcha");
            // 1. 构建认证对象
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

            // 2. 进行认证
            Authentication authenticate = authenticationManager.authenticate(authenticationToken);

            // 3. 获取登录成功后的用户信息
            CustomUserDetails customUserDetails = (CustomUserDetails)authenticate.getPrincipal();
            String userId = customUserDetails.getUser().getId().toString();

            // 4. 生成JWT
            String jwt = JwtUtil.generateToken(userId);
            UserVo userVo = BeanCopyUtil.copyBean(customUserDetails.getUser(), UserVo.class);
            LoginUserVo loginUserVo = new LoginUserVo(jwt,userVo);

            // 5. 保存登录状态到 Redis
            redisCacheUtil.set("blogLogin" + userId, customUserDetails);

            // 6. 返回信息
            return R.success(loginUserVo);
        } catch (BadCredentialsException e) {
            throw new BusinessException(400, "账号或密码错误");
        } catch (DisabledException e) {
            throw new BusinessException(400, "用户已被禁用");
        } catch (Exception e) {
            throw new BusinessException(400, "登录失败：" + e.getMessage());
        }
    }

    public R<?> logout() {
        // 解析token获取到用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
        Integer id = customUserDetails.getUser().getId();

        // 删除redis登录
        redisCacheUtil.delete("blogLogin" + id);
        return R.success();
    }

    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 定义图形验证码的宽、高、验证码字符数、干扰线的条数
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 30, 4, 20);

        // 获取验证码中的文本
        String code = lineCaptcha.getCode();

        // 将验证码存储在会话中，用于后续验证
        request.getSession().setAttribute("captcha", code);

        // 设置响应的内容类型为图片
        response.setContentType("image/png");

        // 获取输出流
        ServletOutputStream outputStream = response.getOutputStream();

        // 将验证码写出到输出流
        lineCaptcha.write(outputStream);

        // 关闭输出流
        outputStream.close();
    }
}
