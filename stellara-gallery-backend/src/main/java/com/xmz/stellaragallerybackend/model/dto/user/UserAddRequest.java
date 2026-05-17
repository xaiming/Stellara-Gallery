package com.xmz.stellaragallerybackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
/**
 * 管理员新增用户请求。
 */
public class UserAddRequest implements Serializable {

    /**
     * 用户登录账号。
     */
    private String userAccount;

    /**
     * 用户明文密码，保存前会在服务层加密。
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
}
