package com.xmz.stellaragallerybackend.model.dto.category;

import lombok.Data;

import java.io.Serializable;

@Data
/**
 * 分类新增或更新请求。
 */
public class CategoryRequest implements Serializable {

    /**
     * 分类 ID，新增时为空。
     */
    private Long id;

    /**
     * 分类名称。
     */
    private String name;

    /**
     * 图标名称。
     */
    private String icon;

    /**
     * 排序值。
     */
    private Integer sort;

    /**
     * 状态：0 启用，1 禁用。
     */
    private Integer status;
}
