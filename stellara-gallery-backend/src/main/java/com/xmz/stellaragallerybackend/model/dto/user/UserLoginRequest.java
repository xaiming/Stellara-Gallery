package com.xmz.stellaragallerybackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
/**
 * 用户登录请求。
 */
public class UserLoginRequest implements Serializable {

    /**
     * 登录账号。
     */
    private String userAccount;

    /**
     * 登录密码。
     */
    private String userPassword;
}
