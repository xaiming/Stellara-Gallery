package com.xmz.stellaragallerybackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
/**
 * 空间视图对象。
 */
public class SpaceVO implements Serializable {

    private Long id;

    private String spaceName;

    private Integer spaceType;

    private Integer spaceLevel;

    private Long userId;

    private Long maxSize;

    private Integer maxCount;

    private Long totalSize;

    private Integer totalCount;

    private Integer spaceStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
