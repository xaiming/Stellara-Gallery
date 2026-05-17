package com.xmz.stellaragallerybackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
/**
 * 用户更新请求。
 */
public class UserUpdateRequest implements Serializable {

    /**
     * 被更新用户 ID；管理员更新用户时必传。
     */
    private Long id;

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
     * 用户角色：user / admin，仅管理员接口允许修改。
     */
    private String userRole;

    /**
     * 用户状态：0 正常，1 禁用，仅管理员接口允许修改。
     */
    private Integer userStatus;
}
