package com.xmz.stellaragallerybackend.model.dto.user;

import com.xmz.stellaragallerybackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
/**
 * 用户分页查询请求。
 */
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * 统一搜索关键词，可匹配用户 ID、账号或昵称。
     */
    private String keyword;

    /**
     * 用户 ID 精确查询。
     */
    private Long id;

    /**
     * 用户账号精确查询。
     */
    private String userAccount;

    /**
     * 用户昵称模糊查询。
     */
    private String userName;

    /**
     * 用户角色筛选：user / admin。
     */
    private String userRole;

    /**
     * 用户状态筛选：0 正常，1 禁用。
     */
    private Integer userStatus;
}
