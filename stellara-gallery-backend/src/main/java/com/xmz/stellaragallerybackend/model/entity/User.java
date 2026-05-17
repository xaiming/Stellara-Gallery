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
@TableName("user")
/**
 * 用户数据库实体。
 */
public class User implements Serializable {

    /**
     * 用户主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号，数据库层唯一。
     */
    private String userAccount;

    /**
     * 加密后的用户密码。
     */
    private String userPassword;

    /**
     * 用户昵称。
     */
    private String userName;

    /**
     * 用户头像 URL。
     */
    private String userAvatar;

    /**
     * 个人主页封面 URL。
     */
    private String userCover;

    /**
     * 个人简介。
     */
    private String userProfile;

    /**
     * 用户角色：user / admin。
     */
    private String userRole;

    /**
     * 用户状态：0 正常，1 禁用。
     */
    private Integer userStatus;

    /**
     * 最后登录时间。
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0 未删除，1 已删除。
     */
    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;
}
