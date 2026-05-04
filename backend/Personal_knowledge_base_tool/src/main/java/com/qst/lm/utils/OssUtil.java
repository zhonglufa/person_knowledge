package com.qst.lm.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 */
@Component
public class OssUtil {

    private static final Logger logger = LoggerFactory.getLogger(OssUtil.class);

    @Autowired
    private OSS ossClient;

    @Value("${oss.bucketName}")
    private String bucketName;

    @Value("${oss.endpoint}")
    private String endpoint;

    // 默认文件有效期（秒）
    private static final int DEFAULT_EXPIRY_SECONDS = 60 * 60 * 24 * 7; // 3天
    // 私有文件短效期（用于临时访问）
    private static final int PRIVATE_FILE_EXPIRY_SECONDS = 60 * 60; // 1小时

    /**
     * 上传文件（自动根据isPublic参数决定文件访问权限）
     *
     * @param file 文件
     * @param isPublic 是否公开（true=公开可读，false=私有需签名）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, boolean isPublic) throws Exception {
        logger.info("开始上传文件: 原始文件名={}, 大小={}字节, 类型={}, 公开状态={}",
                   file.getOriginalFilename(), file.getSize(), file.getContentType(), isPublic);

        // 生成唯一文件名
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        logger.info("生成唯一文件名: {}", fileName);

        // 上传文件
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream);

            // 根据公开状态设置文件权限
            if (isPublic) {
                // 设置公共读权限
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                putObjectRequest.setMetadata(metadata);
                // 公开文件直接上传，OSS默认是私有的
            }

            ossClient.putObject(putObjectRequest);
        }

        logger.info("文件上传成功，正在生成访问URL");

        // 根据权限类型返回相应URL
        String url = generateFileUrl(fileName, isPublic);
        logger.info("文件访问URL生成成功: {}", url);
        return url;
    }

    /**
     * 上传文件（默认私有）
     */
    public String uploadFile(MultipartFile file) throws Exception {
        return uploadFile(file, false);
    }

    /**
     * 上传字节数组（支持权限控制）
     *
     * @param bytes 字节数组
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param isPublic 是否公开
     * @return 文件访问URL
     */
    public String uploadBytes(byte[] bytes, String fileName, String contentType, boolean isPublic) throws Exception {
        logger.info("开始上传字节数组: 文件名={}, 大小={}字节, 类型={}, 公开状态={}",
                   fileName, bytes.length, contentType, isPublic);

        // 生成唯一文件名
        String uniqueFileName = generateUniqueFileName(fileName);
        logger.info("生成唯一文件名: {}", uniqueFileName);

        // 上传字节数组
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, uniqueFileName, inputStream);

            // 设置元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(bytes.length);
            putObjectRequest.setMetadata(metadata);

            // 根据公开状态设置权限
            if (isPublic) {
                // 公开文件直接上传，OSS默认是私有的
            }

            ossClient.putObject(putObjectRequest);
        }

        logger.info("字节数组上传成功，正在生成访问URL");

        // 根据权限类型返回相应URL
        String url = generateFileUrl(uniqueFileName, isPublic);
        logger.info("文件访问URL生成成功: {}", url);
        return url;
    }

    /**
     * 生成文件访问URL
     *
     * @param fileName 文件名
     * @param isPublic 是否公开
     * @return 文件访问URL
     */
    public String generateFileUrl(String fileName, boolean isPublic) throws Exception {
        if (isPublic) {
            // 公开文件返回直接访问URL
            return getPublicFileUrl(fileName);
        } else {
            // 私有文件返回临时签名URL
            return getPrivateFileUrl(fileName);
        }
    }

    /**
     * 获取公开文件URL
     *
     * @param fileName 文件名
     * @return 公开访问URL
     */
    public String getPublicFileUrl(String fileName) {
        return String.format("https://%s.%s/%s", bucketName, endpoint.replace("https://", ""), fileName);
    }

    /**
     * 获取私有文件临时访问URL
     *
     * @param fileName 文件名
     * @return 临时访问URL
     */
    public String getPrivateFileUrl(String fileName) throws Exception {
        Date expiration = new Date(new Date().getTime() + PRIVATE_FILE_EXPIRY_SECONDS * 1000L);
        URL url = ossClient.generatePresignedUrl(bucketName, fileName, expiration);
        return url.toString();
    }

    /**
     * 管理员获取文件访问权限（无视公开状态）
     *
     * @param fileName 文件名
     * @return 管理员访问URL
     */
    public String getAdminFileUrl(String fileName) throws Exception {
        Date expiration = new Date(new Date().getTime() + DEFAULT_EXPIRY_SECONDS * 1000L);
        URL url = ossClient.generatePresignedUrl(bucketName, fileName, expiration);
        return url.toString();
    }

    /**
     * 删除文件
     *
     * @param fileName 文件名
     * @return 删除结果
     */
    public boolean deleteFile(String fileName) throws Exception {
        logger.info("删除文件: {}", fileName);
        ossClient.deleteObject(bucketName, fileName);
        return true;
    }

    /**
     * 检查文件是否存在
     *
     * @param fileName 文件名
     * @return 是否存在
     */
    public boolean fileExists(String fileName) throws Exception {
        return ossClient.doesObjectExist(bucketName, fileName);
    }

    /**
     * 生成唯一文件名
     *
     * @param originalFileName 原始文件名
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 获取存储桶名称
     *
     * @return 存储桶名称
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * 关闭OSS客户端
     */
    public void close() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
