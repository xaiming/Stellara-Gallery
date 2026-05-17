package com.xmz.stellaragallerybackend.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmz.stellaragallerybackend.common.BaseResponse;
import com.xmz.stellaragallerybackend.common.DeleteRequest;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.common.PageRequest;
import com.xmz.stellaragallerybackend.common.ResultUtils;
import com.xmz.stellaragallerybackend.exception.ThrowUtils;
import com.xmz.stellaragallerybackend.mapper.AuditLogMapper;
import com.xmz.stellaragallerybackend.model.dto.user.UserChangePasswordRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserAddRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserLoginRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserQueryRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserRegisterRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserResetPasswordRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserUpdateRequest;
import com.xmz.stellaragallerybackend.model.entity.AuditLog;
import com.xmz.stellaragallerybackend.model.entity.User;
import com.xmz.stellaragallerybackend.model.vo.UserVO;
import com.xmz.stellaragallerybackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
/**
 * 用户接口控制器。
 */
public class UserController {

    /**
     * 用户业务服务，封装登录、注册、查询和更新逻辑。
     */
    private final UserService userService;

    /**
     * 审计日志 Mapper，用于后台查看用户操作记录。
     */
    private final AuditLogMapper auditLogMapper;

    public UserController(UserService userService, AuditLogMapper auditLogMapper) {
        this.userService = userService;
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * 注册普通用户账号，默认角色为 user。
     */
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

    /**
     * 用户登录成功后由 Sa-Token 写入登录态 Cookie。
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        UserVO userVO = userService.userLogin(
                userLoginRequest.getUserAccount(),
                userLoginRequest.getUserPassword()
        );
        return ResultUtils.success(userVO);
    }

    /**
     * 退出当前登录账号，并清理 Sa-Token 登录态。
     */
    @Operation(summary = "用户退出登录")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout() {
        return ResultUtils.success(userService.userLogout());
    }

    /**
     * 获取当前登录用户信息，前端顶部栏和个人主页依赖该接口。
     */
    @Operation(summary = "获取当前登录用户")
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getLoginUser() {
        return ResultUtils.success(userService.getLoginUser());
    }

    /**
     * 管理员创建用户，可指定角色和状态。
     */
    @Operation(summary = "创建用户")
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        // 只有管理员可以在后台新增用户。
        StpUtil.checkRole("admin");
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
        userService.recordAudit(StpUtil.getLoginIdAsLong(), user.getId(), "ADD_USER", "管理员创建用户");
        return ResultUtils.success(user.getId());
    }

    /**
     * 管理员删除用户，底层使用 MyBatis-Plus 逻辑删除。
     */
    @Operation(summary = "删除用户")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        // 删除属于后台管理能力，必须先校验管理员角色。
        StpUtil.checkRole("admin");
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StpUtil.getLoginIdAsLong() == deleteRequest.getId(), ErrorCode.OPERATION_ERROR, "不能删除当前登录管理员");
        boolean result = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        userService.recordAudit(StpUtil.getLoginIdAsLong(), deleteRequest.getId(), "DELETE_USER", "管理员删除用户");
        return ResultUtils.success(true);
    }

    /**
     * 管理员更新任意用户资料、角色或状态。
     */
    @Operation(summary = "更新用户")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        // 该接口可修改角色和状态，必须限制为管理员。
        StpUtil.checkRole("admin");
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null || userUpdateRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        Long loginUserId = StpUtil.getLoginIdAsLong();
        ThrowUtils.throwIf(loginUserId.equals(userUpdateRequest.getId()) && ObjUtil.equal(userUpdateRequest.getUserStatus(), 1),
                ErrorCode.OPERATION_ERROR, "不能禁用当前登录管理员");
        User user = BeanUtil.copyProperties(userUpdateRequest, User.class);
        userService.validUser(user, false);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        userService.recordAudit(loginUserId, userUpdateRequest.getId(), "UPDATE_USER", "管理员更新用户信息");
        return ResultUtils.success(true);
    }

    /**
     * 当前登录用户更新自己的个人资料，不允许修改角色和状态。
     */
    @Operation(summary = "更新当前登录用户")
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        return ResultUtils.success(userService.updateMyUser(userUpdateRequest));
    }

    /**
     * 当前登录用户修改自己的密码。
     */
    @Operation(summary = "修改当前登录用户密码")
    @PostMapping("/password/change")
    public BaseResponse<Boolean> changeMyPassword(@RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        return ResultUtils.success(userService.changeMyPassword(userChangePasswordRequest));
    }

    /**
     * 管理员重置指定用户密码。
     */
    @Operation(summary = "管理员重置用户密码")
    @PostMapping("/password/reset")
    public BaseResponse<Boolean> resetUserPassword(@RequestBody UserResetPasswordRequest userResetPasswordRequest) {
        StpUtil.checkRole("admin");
        return ResultUtils.success(userService.resetUserPassword(userResetPasswordRequest));
    }

    /**
     * 管理员根据用户 ID 查看用户详情。
     */
    @Operation(summary = "根据 ID 获取用户")
    @GetMapping("/get")
    public BaseResponse<UserVO> getUserById(@RequestParam Long id) {
        // 用户详情查询属于后台管理能力。
        StpUtil.checkRole("admin");
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 管理员分页查询用户列表，支持账号、昵称、角色和状态筛选。
     */
    @Operation(summary = "分页查询用户")
    @PostMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        // 用户列表包含平台账号信息，只允许管理员访问。
        StpUtil.checkRole("admin");
        long current = userQueryRequest == null ? 1 : userQueryRequest.getCurrent();
        long pageSize = userQueryRequest == null ? 10 : userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        userVOPage.setRecords(userPage.getRecords().stream().map(userService::getUserVO).toList());
        return ResultUtils.success(userVOPage);
    }

    /**
     * 管理员分页查看用户操作审计日志。
     */
    @Operation(summary = "分页查询用户审计日志")
    @PostMapping("/audit/list/page")
    public BaseResponse<Page<AuditLog>> listAuditLogByPage(@RequestBody PageRequest pageRequest) {
        StpUtil.checkRole("admin");
        long current = pageRequest == null ? 1 : pageRequest.getCurrent();
        long pageSize = pageRequest == null ? 10 : pageRequest.getPageSize();
        return ResultUtils.success(auditLogMapper.selectPage(new Page<>(current, pageSize), null));
    }
}
