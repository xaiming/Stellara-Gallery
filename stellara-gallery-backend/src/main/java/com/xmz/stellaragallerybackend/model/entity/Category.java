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
@TableName("category")
/**
 * 图片分类实体。
 */
public class Category implements Serializable {

    /**
     * 分类主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称。
     */
    private String name;

    /**
     * 前端展示图标名称。
     */
    private String icon;

    /**
     * 排序值，越小越靠前。
     */
    private Integer sort;

    /**
     * 状态：0 启用，1 禁用。
     */
    private Integer status;

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
