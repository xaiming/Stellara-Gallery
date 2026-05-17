package com.xmz.stellaragallerybackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员重置用户密码请求。
 */
@Data
public class UserResetPasswordRequest implements Serializable {

    /**
     * 被重置密码的用户 ID。
     */
    private Long id;

    /**
     * 新密码。
     */
    private String newPassword;
}
