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
@TableName("tag")
/**
 * 图片标签实体。
 */
public class Tag implements Serializable {

    /**
     * 标签主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称。
     */
    private String name;

    /**
     * 标签展示色值。
     */
    private String color;

    /**
     * 标签使用次数。
     */
    private Integer useCount;

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
