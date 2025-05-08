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
        // 构造配置类和上传管理器
        Configuration cfg = new Configuration(Zone.autoZone());
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        // 构造文件存储路径（如：folder/original_filename.ext）
        String fileName = folder + "/" + file.getOriginalFilename();
        String fileUrl = domain + "/" + fileName; // 直接拼接访问路径

        try {
            // 尝试上传文件
            Response response = uploadManager.put(file.getBytes(), fileName, upToken);
            DefaultPutRet putRet = response.jsonToObject(DefaultPutRet.class);
            return domain + "/" + putRet.key;
        } catch (QiniuException ex) {
            if (ex.code() == 614) { // 614 表示文件已存在
                System.out.println("文件已存在，直接返回路径：" + fileUrl);
                return fileUrl;
            } else {
                // 其他异常处理
                System.err.println("上传失败：" + ex.response.toString());
                try {
                    System.err.println(ex.response.bodyString());
                } catch (QiniuException ex2) {
                    // ignore
                }
                return null;
            }
        }
    }
    /**
     * 实际执行七牛云上传的方法
     * @param data 文件数据
     * @param fileName 文件名（包含路径）
     * @return 文件访问URL
     * @throws IOException
     */
    public String uploadFile(byte[] data, String fileName) throws IOException {
        Configuration cfg = new Configuration(Zone.autoZone());
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        try {
            Response response = uploadManager.put(data, fileName, upToken);
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
            throw new IOException("文件上传失败", ex);
        }
    }

    // 下载文件
    public String getDownloadUrl(String key) {
        Auth auth = Auth.create(accessKey, secretKey);
        String publicUrl = String.format("%s/%s", domain, key);
        return auth.privateDownloadUrl(publicUrl);
    }
}