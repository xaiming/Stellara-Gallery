package com.xmz.stellaragallerybackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
/**
 * 返回给前端的用户视图对象。
 */
public class UserVO implements Serializable {

    /**
     * 用户主键 ID。
     */
    private Long id;

    /**
     * 用户账号。
     */
    private String userAccount;

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
}
