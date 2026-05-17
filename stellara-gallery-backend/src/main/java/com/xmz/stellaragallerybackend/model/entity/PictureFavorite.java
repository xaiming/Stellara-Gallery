package com.xmz.stellaragallerybackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("picture_favorite")
/**
 * 图片收藏实体。
 */
public class PictureFavorite implements Serializable {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图片 ID。
     */
    private Long pictureId;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 收藏时间。
     */
    private LocalDateTime createTime;
}
