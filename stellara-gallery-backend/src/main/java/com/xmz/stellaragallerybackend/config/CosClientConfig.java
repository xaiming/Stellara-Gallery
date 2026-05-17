package com.xmz.stellaragallerybackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cos.client")
/**
 * 腾讯云 COS 客户端配置。
 */
public class CosClientConfig {

    /**
     * COS 公开访问域名。
     */
    private String host;

    /**
     * 腾讯云 SecretId，建议通过环境变量注入。
     */
    private String secretId;

    /**
     * 腾讯云 SecretKey，禁止提交到 Git。
     */
    private String secretKey;

    /**
     * 存储桶地域，例如 ap-shanghai。
     */
    private String region;

    /**
     * 存储桶名称，例如 bucket-1250000000。
     */
    private String bucket;

    /**
     * 对象 Key 前缀，用于隔离项目上传目录。
     */
    private String uploadPrefix = "stellara-gallery";
}
