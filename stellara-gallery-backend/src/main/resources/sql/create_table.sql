DROP TABLE IF EXISTS picture_favorite;
DROP TABLE IF EXISTS picture_like;
DROP TABLE IF EXISTS space_user;
DROP TABLE IF EXISTS picture;
DROP TABLE IF EXISTS space;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS audit_log;
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

CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',

    operator_id BIGINT DEFAULT NULL COMMENT '操作人用户ID',
    operator_account VARCHAR(64) DEFAULT NULL COMMENT '操作人账号',
    target_user_id BIGINT DEFAULT NULL COMMENT '被操作用户ID',
    action VARCHAR(64) NOT NULL COMMENT '操作类型',
    detail VARCHAR(512) DEFAULT NULL COMMENT '操作说明',

    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',

    KEY idx_operator_id (operator_id),
    KEY idx_target_user_id (target_user_id),
    KEY idx_action (action),
    KEY idx_create_time (create_time)
) COMMENT='审计日志表';

CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    icon VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0启用 1禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
    UNIQUE KEY uk_name (name),
    KEY idx_status (status),
    KEY idx_sort (sort)
) COMMENT='分类表';

CREATE TABLE tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
    name VARCHAR(64) NOT NULL COMMENT '标签名称',
    color VARCHAR(32) DEFAULT NULL COMMENT '标签颜色',
    use_count INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
    UNIQUE KEY uk_name (name),
    KEY idx_use_count (use_count)
) COMMENT='标签表';

CREATE TABLE space (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '空间ID',
    space_name VARCHAR(128) NOT NULL COMMENT '空间名称',
    space_type TINYINT NOT NULL DEFAULT 0 COMMENT '空间类型：0个人空间 1团队空间',
    space_level TINYINT NOT NULL DEFAULT 0 COMMENT '空间等级：0普通版 1专业版 2旗舰版',
    user_id BIGINT NOT NULL COMMENT '空间创建者ID',
    max_size BIGINT NOT NULL DEFAULT 1073741824 COMMENT '空间最大容量，默认1GB',
    max_count INT NOT NULL DEFAULT 1000 COMMENT '空间最大图片数量',
    total_size BIGINT NOT NULL DEFAULT 0 COMMENT '当前已使用容量',
    total_count INT NOT NULL DEFAULT 0 COMMENT '当前图片数量',
    space_status TINYINT NOT NULL DEFAULT 0 COMMENT '空间状态：0正常 1禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
    UNIQUE KEY uk_user_space_type (user_id, space_type),
    KEY idx_user_id (user_id),
    KEY idx_space_type (space_type),
    KEY idx_space_status (space_status),
    KEY idx_create_time (create_time)
) COMMENT='空间表';

CREATE TABLE picture (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    url VARCHAR(1024) NOT NULL COMMENT '图片地址',
    thumbnail_url VARCHAR(1024) DEFAULT NULL COMMENT '缩略图地址',
    name VARCHAR(128) NOT NULL COMMENT '图片名称',
    introduction VARCHAR(512) DEFAULT NULL COMMENT '图片简介',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    tags VARCHAR(512) DEFAULT NULL COMMENT '标签JSON，例如 ["星空","少女","壁纸"]',
    pic_size BIGINT DEFAULT NULL COMMENT '图片大小，单位字节',
    pic_width INT DEFAULT NULL COMMENT '图片宽度',
    pic_height INT DEFAULT NULL COMMENT '图片高度',
    pic_scale DOUBLE DEFAULT NULL COMMENT '图片宽高比',
    pic_format VARCHAR(32) DEFAULT NULL COMMENT '图片格式，例如 jpg/png/webp',
    pic_color VARCHAR(32) DEFAULT NULL COMMENT '图片主色调',
    user_id BIGINT NOT NULL COMMENT '上传用户ID',
    space_id BIGINT NOT NULL COMMENT '所属空间ID',
    is_public TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开：0私有 1公开',
    review_status TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态：0待审核 1通过 2拒绝',
    review_message VARCHAR(512) DEFAULT NULL COMMENT '审核信息',
    reviewer_id BIGINT DEFAULT NULL COMMENT '审核人ID',
    review_time DATETIME DEFAULT NULL COMMENT '审核时间',
    view_count INT NOT NULL DEFAULT 0 COMMENT '去重浏览量UV',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    download_count INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
    KEY idx_user_id (user_id),
    KEY idx_space_id (space_id),
    KEY idx_category_id (category_id),
    KEY idx_is_public (is_public),
    KEY idx_review_status (review_status),
    KEY idx_public_review (is_public, review_status),
    KEY idx_create_time (create_time)
) COMMENT='图片表';

CREATE TABLE space_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    space_id BIGINT NOT NULL COMMENT '空间ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    space_role VARCHAR(32) NOT NULL DEFAULT 'viewer' COMMENT '空间角色：admin/editor/viewer',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_space_user (space_id, user_id),
    KEY idx_space_id (space_id),
    KEY idx_user_id (user_id),
    KEY idx_space_role (space_role)
) COMMENT='空间用户关联表';

CREATE TABLE picture_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    picture_id BIGINT NOT NULL COMMENT '图片ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_picture_user (picture_id, user_id),
    KEY idx_picture_id (picture_id),
    KEY idx_user_id (user_id)
) COMMENT='图片点赞表';

CREATE TABLE picture_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    picture_id BIGINT NOT NULL COMMENT '图片ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_picture_user (picture_id, user_id),
    KEY idx_picture_id (picture_id),
    KEY idx_user_id (user_id)
) COMMENT='图片收藏表';

INSERT INTO category (name, icon, sort, status) VALUES
('星空壁纸', 'Sparkles', 10, 0),
('二次元角色', 'Crown', 20, 0),
('天空之城', 'Cloud', 30, 0),
('梦幻插画', 'Palette', 40, 0);

INSERT INTO tag (name, color, use_count) VALUES
('星空', '#7b61ff', 0),
('少女', '#e28cff', 0),
('壁纸', '#4d7cff', 0),
('梦幻', '#ff9fd7', 0),
('天空之城', '#6dd6ff', 0);
