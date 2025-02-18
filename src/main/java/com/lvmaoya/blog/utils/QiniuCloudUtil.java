package com.lvmaoya.blog.utils;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class QiniuCloudUtil {

    @Value("${qiniu.access-key}")
    private String accessKey;

    @Value("${qiniu.secret-key}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.domain}")
    private String domain;

    @Value("${qiniu.folder}")
    private String folder;

    // 生成上传凭证
    public String getUploadToken() {
        Auth auth = Auth.create(accessKey, secretKey);
        return auth.uploadToken(bucket);
    }

    // 上传文件
    public String uploadFile(MultipartFile file) throws IOException {
        // 构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.autoZone());
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        String fileName = folder + "/" + file.getOriginalFilename();

        try {
            Response response = uploadManager.put(file.getBytes(), fileName, upToken);
            // 解析上传成功的结果
            DefaultPutRet putRet = response.jsonToObject(DefaultPutRet.class);
            return domain + "/" + putRet.key;
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }
        return null;
    }

    // 下载文件
    public String getDownloadUrl(String key) {
        Auth auth = Auth.create(accessKey, secretKey);
        String publicUrl = String.format("%s/%s", domain, key);
        return auth.privateDownloadUrl(publicUrl);
    }
}