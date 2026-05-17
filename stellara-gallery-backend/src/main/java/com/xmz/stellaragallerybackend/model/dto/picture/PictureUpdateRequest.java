package com.xmz.stellaragallerybackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
/**
 * 图片编辑请求。
 */
public class PictureUpdateRequest implements Serializable {

    /**
     * 图片 ID。
     */
    private Long id;

    /**
     * 图片名称。
     */
    private String name;

    /**
     * 图片简介。
     */
    private String introduction;

    /**
     * 分类 ID。
     */
    private Long categoryId;

    /**
     * 标签列表。
     */
    private List<String> tags;

    /**
     * 是否公开。
     */
    private Integer isPublic;
}
