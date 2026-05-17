package com.xmz.stellaragallerybackend.model.dto.picture;

import com.xmz.stellaragallerybackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
/**
 * 图片分页查询请求。
 */
public class PictureQueryRequest extends PageRequest {

    /**
     * 关键词，匹配名称或简介。
     */
    private String keyword;

    /**
     * 分类 ID。
     */
    private Long categoryId;

    /**
     * 标签名称。
     */
    private String tag;

    /**
     * 上传用户 ID。
     */
    private Long userId;

    /**
     * 空间 ID。
     */
    private Long spaceId;

    /**
     * 是否公开。
     */
    private Integer isPublic;

    /**
     * 审核状态。
     */
    private Integer reviewStatus;
}
