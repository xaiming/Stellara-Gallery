package com.xmz.stellaragallerybackend.constant;

/**
 * 用户模块常量。
 */
public final class UserConstant {

    /**
     * 旧 session 登录态 key，保留用于兼容历史代码语义。
     */
    public static final String USER_LOGIN_STATE = "user_login";

    /**
     * 普通用户角色。
     */
    public static final String DEFAULT_ROLE = "user";

    /**
     * 管理员角色。
     */
    public static final String ADMIN_ROLE = "admin";

    private UserConstant() {
    }
}
