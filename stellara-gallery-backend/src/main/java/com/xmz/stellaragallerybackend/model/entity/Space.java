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
@TableName("space")
/**
 * 图片空间实体。
 */
public class Space implements Serializable {

    /**
     * 空间主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 空间名称。
     */
    private String spaceName;

    /**
     * 空间类型：0 个人空间，1 团队空间。
     */
    private Integer spaceType;

    /**
     * 空间等级。
     */
    private Integer spaceLevel;

    /**
     * 创建者用户 ID。
     */
    private Long userId;

    /**
     * 最大容量。
     */
    private Long maxSize;

    /**
     * 最大图片数量。
     */
    private Integer maxCount;

    /**
     * 已使用容量。
     */
    private Long totalSize;

    /**
     * 已保存图片数量。
     */
    private Integer totalCount;

    /**
     * 状态：0 正常，1 禁用。
     */
    private Integer spaceStatus;

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
