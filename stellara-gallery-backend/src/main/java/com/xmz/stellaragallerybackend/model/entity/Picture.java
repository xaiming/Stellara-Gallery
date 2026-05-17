package com.xmz.stellaragallerybackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("picture")
/**
 * 图片实体。
 */
public class Picture implements Serializable {

    /**
     * 图片主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原图访问地址。
     */
    private String url;

    /**
     * 缩略图访问地址。
     */
    private String thumbnailUrl;

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
     * 标签 JSON。
     */
    private String tags;

    /**
     * 文件大小，单位字节。
     */
    private Long picSize;

    /**
     * 图片宽度。
     */
    private Integer picWidth;

    /**
     * 图片高度。
     */
    private Integer picHeight;

    /**
     * 图片宽高比。
     */
    private Double picScale;

    /**
     * 图片格式。
     */
    private String picFormat;

    /**
     * 主色调，本期先预留。
     */
    private String picColor;

    /**
     * 上传用户 ID。
     */
    private Long userId;

    /**
     * 所属空间 ID。
     */
    private Long spaceId;

    /**
     * 是否公开：0 私有，1 公开。
     */
    private Integer isPublic;

    /**
     * 审核状态：0 待审核，1 通过，2 拒绝。
     */
    private Integer reviewStatus;

    /**
     * 审核说明。
     */
    private String reviewMessage;

    /**
     * 审核人 ID。
     */
    private Long reviewerId;

    /**
     * 审核时间。
     */
    private LocalDateTime reviewTime;

    /**
     * 去重浏览量 UV，来源于 Redis HyperLogLog。
     */
    private Integer viewCount;

    /**
     * 点赞数。
     */
    private Integer likeCount;

    /**
     * 收藏数。
     */
    private Integer favoriteCount;

    /**
     * 下载次数。
     */
    private Integer downloadCount;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记。
     */
    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;
}
