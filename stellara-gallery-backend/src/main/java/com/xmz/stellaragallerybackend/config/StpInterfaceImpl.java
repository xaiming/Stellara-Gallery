package com.xmz.stellaragallerybackend.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.util.StrUtil;
import com.xmz.stellaragallerybackend.model.entity.User;
import com.xmz.stellaragallerybackend.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
/**
 * Sa-Token 角色与权限数据源实现。
 */
public class StpInterfaceImpl implements StpInterface {

    /**
     * 用于从数据库查询当前登录用户的角色。
     */
    private final UserService userService;

    public StpInterfaceImpl(UserService userService) {
        this.userService = userService;
    }

    /**
     * 当前项目暂时只做角色控制，权限码列表先留空。
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    /**
     * Sa-Token 在执行 checkRole("admin") 时会调用这里获取用户角色。
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (loginId == null) {
            return Collections.emptyList();
        }
        User user = userService.getById(Long.valueOf(String.valueOf(loginId)));
        // 禁用、缺失或无角色的账号不授予任何角色。
        if (user == null || user.getUserStatus() != null && user.getUserStatus() == 1 || StrUtil.isBlank(user.getUserRole())) {
            return Collections.emptyList();
        }
        return List.of(user.getUserRole());
    }
}
