package com.xmz.stellaragallerybackend.model.dto.tag;

import lombok.Data;

import java.io.Serializable;

@Data
/**
 * 标签新增或更新请求。
 */
public class TagRequest implements Serializable {

    /**
     * 标签 ID，新增时为空。
     */
    private Long id;

    /**
     * 标签名称。
     */
    private String name;

    /**
     * 标签颜色。
     */
    private String color;
}
