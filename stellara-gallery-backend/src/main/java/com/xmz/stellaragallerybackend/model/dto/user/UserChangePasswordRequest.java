package com.xmz.stellaragallerybackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前登录用户修改自己的密码请求。
 */
@Data
public class UserChangePasswordRequest implements Serializable {

    /**
     * 旧密码，用于确认当前用户身份。
     */
    private String oldPassword;

    /**
     * 新密码。
     */
    private String newPassword;

    /**
     * 确认新密码。
     */
    private String checkPassword;
}
