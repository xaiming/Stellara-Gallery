package com.xmz.stellaragallerybackend.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.util.StrUtil;
import com.xmz.stellaragallerybackend.model.entity.User;
import com.xmz.stellaragallerybackend.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    private final UserService userService;

    public StpInterfaceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (loginId == null) {
            return Collections.emptyList();
        }
        User user = userService.getById(Long.valueOf(String.valueOf(loginId)));
        if (user == null || user.getUserStatus() != null && user.getUserStatus() == 1 || StrUtil.isBlank(user.getUserRole())) {
            return Collections.emptyList();
        }
        return List.of(user.getUserRole());
    }
}
