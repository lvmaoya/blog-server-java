package com.lvmaoya.blog.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvmaoya.blog.utils.IoUtil;
import com.lvmaoya.blog.utils.QiniuCloudUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("sys/ueditor")
public class UeditorController {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private QiniuCloudUtil qiniuCloudUtil;
    @RequestMapping("")
    public Object index(String action, String callback, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (action.equals("config")) {
            ClassPathResource classPathResource = new ClassPathResource("config.json");
            InputStream inputStream = classPathResource.getInputStream();
            String text = IoUtil.readStrByInputStream(inputStream);
            // 使用Jackson解析JSON
            Map<String, Object> config = objectMapper.readValue(text, new TypeReference<Map<String, Object>>() {});

            // 确保包含state字段
            config.putIfAbsent("state", "SUCCESS");
            return config;
        } else if (action.equals("uploadimage")) {
            request.getRequestDispatcher("/sys/ueditor/ueditorUpload").forward(request, response);
        } else if (action.equals("uploadvideo")) {
            request.getRequestDispatcher("/sys/ueditor/ueditorUpload").forward(request, response);
        }  else if (action.equals("uploadscrawl")) {
            request.getRequestDispatcher("/sys/ueditor/ueditorUploadScrawl").forward(request, response);
        }
        return "";
    }
    @RequestMapping("/ueditorUpload")
    public Object uploadUeditor(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("上传文件不能为空");
        }
        String url = qiniuCloudUtil.uploadFile(file);
        return createSuccessResult(file.getOriginalFilename(), url);
    }
    @RequestMapping("/ueditorUploadScrawl")
    public Object uploadScrawl(@RequestParam("file") String base64Data) throws Exception {
        if (StringUtils.isEmpty(base64Data)) {
            throw new Exception("上传数据不能为空");
        }

        // 生成随机文件名
        String fileName = UUID.randomUUID().toString() + ".png";

        // 处理base64数据
        String[] parts = base64Data.split(",");
        String base64 = parts.length > 1 ? parts[1] : parts[0];
        byte[] data = Base64.getDecoder().decode(base64);

        // 上传到七牛云
        String url = qiniuCloudUtil.uploadFile(data, fileName);

        return createSuccessResult(fileName, url);
    }
    private Map<String, String> createSuccessResult(String originalName, String url) {
        Map<String, String> map = new HashMap<>();
        map.put("state", "SUCCESS");
        map.put("url", url);
        map.put("title", originalName);
        map.put("original", originalName);
        return map;
    }
}