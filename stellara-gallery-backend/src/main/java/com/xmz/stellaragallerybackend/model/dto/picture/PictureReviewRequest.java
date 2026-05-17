package com.xmz.stellaragallerybackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
/**
 * 图片审核请求。
 */
public class PictureReviewRequest implements Serializable {

    /**
     * 图片 ID。
     */
    private Long id;

    /**
     * 审核状态：1 通过，2 拒绝。
     */
    private Integer reviewStatus;

    /**
     * 审核说明。
     */
    private String reviewMessage;
}
