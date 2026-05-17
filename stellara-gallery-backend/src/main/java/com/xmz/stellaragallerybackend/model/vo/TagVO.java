package com.xmz.stellaragallerybackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
/**
 * 标签视图对象。
 */
public class TagVO implements Serializable {

    private Long id;

    private String name;

    private String color;

    private Integer useCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
