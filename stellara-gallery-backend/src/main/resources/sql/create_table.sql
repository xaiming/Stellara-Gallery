DROP TABLE IF EXISTS user;

CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',

    user_account VARCHAR(64) NOT NULL COMMENT '用户账号',
    user_password VARCHAR(255) NOT NULL COMMENT '用户密码',
    user_name VARCHAR(64) DEFAULT NULL COMMENT '用户昵称',
    user_avatar VARCHAR(1024) DEFAULT NULL COMMENT '用户头像',
    user_cover VARCHAR(1024) DEFAULT NULL COMMENT '用户主页封面',
    user_profile VARCHAR(512) DEFAULT NULL COMMENT '用户简介',

    user_role VARCHAR(32) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
    user_status TINYINT NOT NULL DEFAULT 0 COMMENT '用户状态：0正常 1禁用',

    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',

    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',

    UNIQUE KEY uk_user_account (user_account),
    KEY idx_user_name (user_name),
    KEY idx_user_role (user_role),
    KEY idx_user_status (user_status),
    KEY idx_create_time (create_time)
) COMMENT='用户表';
