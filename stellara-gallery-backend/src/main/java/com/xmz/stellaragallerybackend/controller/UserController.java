package com.xmz.stellaragallerybackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmz.stellaragallerybackend.common.BaseResponse;
import com.xmz.stellaragallerybackend.common.DeleteRequest;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.common.ResultUtils;
import com.xmz.stellaragallerybackend.exception.ThrowUtils;
import com.xmz.stellaragallerybackend.model.dto.user.UserAddRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserLoginRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserQueryRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserRegisterRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserUpdateRequest;
import com.xmz.stellaragallerybackend.model.entity.User;
import com.xmz.stellaragallerybackend.model.vo.UserVO;
import com.xmz.stellaragallerybackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long userId = userService.userRegister(
                userRegisterRequest.getUserAccount(),
                userRegisterRequest.getUserPassword(),
                userRegisterRequest.getCheckPassword(),
                userRegisterRequest.getUserName()
        );
        return ResultUtils.success(userId);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        UserVO userVO = userService.userLogin(
                userLoginRequest.getUserAccount(),
                userLoginRequest.getUserPassword(),
                request
        );
        return ResultUtils.success(userVO);
    }

    @Operation(summary = "用户退出登录")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(request));
    }

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        return ResultUtils.success(userService.getLoginUser(request));
    }

    @Operation(summary = "创建用户")
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = BeanUtil.copyProperties(userAddRequest, User.class);
        userService.validUser(user, true);
        boolean exists = userService.lambdaQuery()
                .eq(User::getUserAccount, user.getUserAccount())
                .exists();
        ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "用户账号已存在");
        user.setUserPassword(userService.getEncryptPassword(user.getUserPassword()));
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    @Operation(summary = "删除用户")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @Operation(summary = "更新用户")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null || userUpdateRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        User user = BeanUtil.copyProperties(userUpdateRequest, User.class);
        userService.validUser(user, false);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @Operation(summary = "根据 ID 获取用户")
    @GetMapping("/get")
    public BaseResponse<UserVO> getUserById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    @Operation(summary = "分页查询用户")
    @PostMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        long current = userQueryRequest == null ? 1 : userQueryRequest.getCurrent();
        long pageSize = userQueryRequest == null ? 10 : userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        userVOPage.setRecords(userPage.getRecords().stream().map(userService::getUserVO).toList());
        return ResultUtils.success(userVOPage);
    }
}
