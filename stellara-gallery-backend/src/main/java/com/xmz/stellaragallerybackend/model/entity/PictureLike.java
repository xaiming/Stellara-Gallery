package com.xmz.stellaragallerybackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("picture_like")
/**
 * 图片点赞实体。
 */
public class PictureLike implements Serializable {

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
     * 点赞时间。
     */
    private LocalDateTime createTime;
}
