package com.lvmaoya.blog.controller;

import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.domain.vo.UploadResult;
import com.lvmaoya.blog.utils.QiniuCloudUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping("/qiniu")
public class QiniuController {

    @Autowired
    private QiniuCloudUtil qiniuCloudUtil;

    // 文件上传接口
    @PostMapping("/upload")
    public R uploadFileToFolder(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = qiniuCloudUtil.uploadFile(file);
            var map = new HashMap<String, String>();
            map.put("url", fileUrl);
            if (fileUrl != null) {
                return R.success(map);
            } else {
                return R.error(400, "文件上传失败");
            }
        } catch (IOException e) {
            return R.error(400, e.getMessage());
        }
    }

    // 文件下载接口
    @GetMapping("/download/{key}")
    public String getDownloadUrl(@PathVariable String key) {
        return qiniuCloudUtil.getDownloadUrl(key);
    }
}