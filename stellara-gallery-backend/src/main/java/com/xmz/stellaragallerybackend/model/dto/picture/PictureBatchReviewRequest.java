package com.xmz.stellaragallerybackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
/**
 * 图片批量审核请求。
 */
public class PictureBatchReviewRequest implements Serializable {

    /**
     * 图片 ID 列表。
     */
    private List<Long> ids;

    /**
     * 审核状态：1 通过，2 拒绝。
     */
    private Integer reviewStatus;

    /**
     * 审核说明。
     */
    private String reviewMessage;
}
