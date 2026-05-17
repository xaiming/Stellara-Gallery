package com.xmz.stellaragallerybackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
/**
 * 分类视图对象。
 */
public class CategoryVO implements Serializable {

    private Long id;

    private String name;

    private String icon;

    private Integer sort;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
