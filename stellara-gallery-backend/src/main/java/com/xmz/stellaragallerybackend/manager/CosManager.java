package com.xmz.stellaragallerybackend.manager;

import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.config.CosClientConfig;
import com.xmz.stellaragallerybackend.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
/**
 * COS 上传管理器。
 */
public class CosManager {

    /**
     * COS 配置项。
     */
    private final CosClientConfig cosClientConfig;

    public CosManager(CosClientConfig cosClientConfig) {
        this.cosClientConfig = cosClientConfig;
    }

    /**
     * 上传文件并返回公开 URL。
     */
    public String uploadFile(String key, MultipartFile file) {
        checkConfig();
        COSClient cosClient = buildClient();
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosClientConfig.getBucket(),
                    key,
                    file.getInputStream(),
                    objectMetadata
            );
            cosClient.putObject(putObjectRequest);
            return StrUtil.removeSuffix(cosClientConfig.getHost(), "/") + "/" + key;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片上传失败");
        } finally {
            cosClient.shutdown();
        }
    }

    /**
     * 创建 COS 客户端。
     */
    private COSClient buildClient() {
        COSCredentials credentials = new BasicCOSCredentials(cosClientConfig.getSecretId(), cosClientConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosClientConfig.getRegion()));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        return new COSClient(credentials, clientConfig);
    }

    /**
     * 校验 COS 必填配置，避免运行时使用空密钥。
     */
    private void checkConfig() {
        if (StrUtil.hasBlank(
                cosClientConfig.getHost(),
                cosClientConfig.getSecretId(),
                cosClientConfig.getSecretKey(),
                cosClientConfig.getRegion(),
                cosClientConfig.getBucket()
        )) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "COS 配置未完成");
        }
    }
}
