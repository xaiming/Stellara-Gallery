package com.xmz.stellaragallerybackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
/**
 * 用户注册请求。
 */
public class UserRegisterRequest implements Serializable {

    /**
     * 注册账号。
     */
    private String userAccount;

    /**
     * 注册密码。
     */
    private String userPassword;

    /**
     * 确认密码，用于校验两次输入是否一致。
     */
    private String checkPassword;

    /**
     * 注册时填写的昵称，可为空。
     */
    private String userName;
}
