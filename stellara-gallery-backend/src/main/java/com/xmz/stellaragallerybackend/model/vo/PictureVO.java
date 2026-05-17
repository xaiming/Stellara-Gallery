package com.xmz.stellaragallerybackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
/**
 * 图片视图对象。
 */
public class PictureVO implements Serializable {

    private Long id;

    private String url;

    private String thumbnailUrl;

    private String name;

    private String introduction;

    private Long categoryId;

    private String categoryName;

    private List<String> tags;

    private Long picSize;

    private Integer picWidth;

    private Integer picHeight;

    private Double picScale;

    private String picFormat;

    private String picColor;

    private Long userId;

    private String userName;

    private String userAvatar;

    private Long spaceId;

    private Integer isPublic;

    private Integer reviewStatus;

    private String reviewMessage;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private Integer viewCount;

    private Integer likeCount;

    private Integer favoriteCount;

    private Integer downloadCount;

    private Boolean liked;

    private Boolean favorited;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
