package com.qst.lm.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 */
@Configuration
public class OssConfig {
    
    @Value("${oss.endpoint}")
    private String endpoint;
    
    @Value("${oss.accessKeyId}")
    private String accessKeyId;
    
    @Value("${oss.accessKeySecret}")
    private String accessKeySecret;
    
    @Value("${oss.bucketName}")
    private String bucketName;
    
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
}