package com.lvmaoya.blog.controller;

import com.lvmaoya.blog.domain.vo.UploadResult;
import com.lvmaoya.blog.utils.QiniuCloudUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/qiniu")
public class QiniuController {

    @Autowired
    private QiniuCloudUtil qiniuCloudUtil;

    // 文件上传接口
    @PostMapping("/upload")
    public UploadResult uploadFileToFolder(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = qiniuCloudUtil.uploadFile(file);
            if (fileUrl != null) {
                return new UploadResult(200, "文件上传成功", fileUrl);
            } else {
                return new UploadResult(500, "文件上传失败", null);
            }
        } catch (IOException e) {
            return new UploadResult(500, "文件上传发生异常: " + e.getMessage(), null);
        }
    }
    // 文件下载接口
    @GetMapping("/download/{key}")
    public String getDownloadUrl(@PathVariable String key) {
        return qiniuCloudUtil.getDownloadUrl(key);
    }
}