package com.xmz.stellaragallerybackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("space_user")
/**
 * 团队空间成员实体。
 */
public class SpaceUser implements Serializable {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 空间 ID。
     */
    private Long spaceId;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 空间角色：admin / editor / viewer。
     */
    private String spaceRole;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
