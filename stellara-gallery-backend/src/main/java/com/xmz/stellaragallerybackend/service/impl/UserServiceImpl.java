package com.xmz.stellaragallerybackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.constant.UserConstant;
import com.xmz.stellaragallerybackend.exception.ThrowUtils;
import com.xmz.stellaragallerybackend.mapper.AuditLogMapper;
import com.xmz.stellaragallerybackend.mapper.UserMapper;
import com.xmz.stellaragallerybackend.model.dto.user.UserChangePasswordRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserQueryRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserResetPasswordRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserUpdateRequest;
import com.xmz.stellaragallerybackend.model.entity.AuditLog;
import com.xmz.stellaragallerybackend.model.entity.User;
import com.xmz.stellaragallerybackend.model.vo.UserVO;
import com.xmz.stellaragallerybackend.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
/**
 * 用户业务服务实现。
 */
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 密码加盐值，用于避免直接存储明文密码。
     */
    private static final String SALT = "stellara_gallery";

    /**
     * 审计日志 Mapper，用于记录关键用户操作。
     */
    private final AuditLogMapper auditLogMapper;

    public UserServiceImpl(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * 注册普通用户，账号唯一且密码必须满足基础长度要求。
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userName) {
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号不能少于 4 位");
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码不能少于 8 位");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");

        synchronized (userAccount.intern()) {
            // 同一个账号串行注册，避免并发下重复创建账号。
            boolean exists = this.lambdaQuery().eq(User::getUserAccount, userAccount).exists();
            ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "用户账号已存在");
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(getEncryptPassword(userPassword));
            user.setUserName(StrUtil.blankToDefault(userName, userAccount));
            user.setUserRole(UserConstant.DEFAULT_ROLE);
            user.setUserStatus(0);
            boolean saveResult = this.save(user);
            ThrowUtils.throwIf(!saveResult, ErrorCode.OPERATION_ERROR);
            return user.getId();
        }
    }

    /**
     * 登录时先校验账号密码，再由 Sa-Token 记录登录用户 ID。
     */
    @Override
    public UserVO userLogin(String userAccount, String userPassword) {
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号错误");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码错误");

        String encryptPassword = getEncryptPassword(userPassword);
        // 使用加密后的密码匹配数据库，避免明文密码参与查询。
        User user = this.lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword)
                .one();
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        ThrowUtils.throwIf(ObjUtil.equal(user.getUserStatus(), 1), ErrorCode.FORBIDDEN_ERROR, "用户已被禁用");

        // Sa-Token 会根据 application.yml 配置写入登录态 Cookie。
        StpUtil.login(user.getId());
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);
        return getUserVO(user);
    }

    /**
     * 根据 Sa-Token 当前登录 ID 查询用户，并拦截已禁用账号。
     */
    @Override
    public UserVO getLoginUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(ObjUtil.equal(user.getUserStatus(), 1), ErrorCode.FORBIDDEN_ERROR, "用户已被禁用");
        return getUserVO(user);
    }

    /**
     * 清理当前会话的 Sa-Token 登录态。
     */
    @Override
    public boolean userLogout() {
        StpUtil.logout();
        return true;
    }

    /**
     * 普通用户只能更新自己的展示资料，不能修改角色、状态或其它用户 ID。
     */
    @Override
    public boolean updateMyUser(UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        Long loginUserId = StpUtil.getLoginIdAsLong();
        User user = new User();
        // 强制使用当前登录用户 ID，忽略前端传入的 id，避免越权修改他人资料。
        user.setId(loginUserId);
        user.setUserName(userUpdateRequest.getUserName());
        user.setUserAvatar(userUpdateRequest.getUserAvatar());
        user.setUserCover(userUpdateRequest.getUserCover());
        user.setUserProfile(userUpdateRequest.getUserProfile());
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        recordAudit(loginUserId, loginUserId, "UPDATE_MY_PROFILE", "用户更新自己的个人资料");
        return true;
    }

    /**
     * 当前登录用户修改密码，必须提供正确旧密码。
     */
    @Override
    public boolean changeMyPassword(UserChangePasswordRequest userChangePasswordRequest) {
        ThrowUtils.throwIf(userChangePasswordRequest == null, ErrorCode.PARAMS_ERROR);
        String oldPassword = userChangePasswordRequest.getOldPassword();
        String newPassword = userChangePasswordRequest.getNewPassword();
        String checkPassword = userChangePasswordRequest.getCheckPassword();
        ThrowUtils.throwIf(StrUtil.hasBlank(oldPassword, newPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(newPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "新密码不能少于 8 位");
        ThrowUtils.throwIf(!newPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的新密码不一致");

        Long loginUserId = StpUtil.getLoginIdAsLong();
        User user = this.getById(loginUserId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(!getEncryptPassword(oldPassword).equals(user.getUserPassword()), ErrorCode.PARAMS_ERROR, "旧密码错误");

        User updateUser = new User();
        updateUser.setId(loginUserId);
        updateUser.setUserPassword(getEncryptPassword(newPassword));
        boolean result = this.updateById(updateUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        recordAudit(loginUserId, loginUserId, "CHANGE_PASSWORD", "用户修改自己的密码");
        return true;
    }

    /**
     * 管理员重置用户密码。
     */
    @Override
    public boolean resetUserPassword(UserResetPasswordRequest userResetPasswordRequest) {
        ThrowUtils.throwIf(userResetPasswordRequest == null || userResetPasswordRequest.getId() == null || userResetPasswordRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        String newPassword = userResetPasswordRequest.getNewPassword();
        ThrowUtils.throwIf(StrUtil.isBlank(newPassword) || newPassword.length() < 8, ErrorCode.PARAMS_ERROR, "新密码不能少于 8 位");
        User targetUser = this.getById(userResetPasswordRequest.getId());
        ThrowUtils.throwIf(targetUser == null, ErrorCode.NOT_FOUND_ERROR);

        User updateUser = new User();
        updateUser.setId(userResetPasswordRequest.getId());
        updateUser.setUserPassword(getEncryptPassword(newPassword));
        boolean result = this.updateById(updateUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        recordAudit(StpUtil.getLoginIdAsLong(), userResetPasswordRequest.getId(), "RESET_PASSWORD", "管理员重置用户密码");
        return true;
    }

    /**
     * 密码加密，注册和登录必须使用同一套规则。
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        return DigestUtil.md5Hex(SALT + userPassword);
    }

    /**
     * 校验用户字段；新增场景必须校验账号和密码，更新场景只校验传入字段。
     */
    @Override
    public void validUser(User user, boolean add) {
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);
        String userAccount = user.getUserAccount();
        String userPassword = user.getUserPassword();
        String userRole = user.getUserRole();
        Integer userStatus = user.getUserStatus();

        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(userAccount), ErrorCode.PARAMS_ERROR, "用户账号不能为空");
            ThrowUtils.throwIf(StrUtil.isBlank(userPassword), ErrorCode.PARAMS_ERROR, "用户密码不能为空");
            ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号不能少于 4 位");
            ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码不能少于 8 位");
        }
        if (StrUtil.isNotBlank(userRole)) {
            ThrowUtils.throwIf(!UserConstant.DEFAULT_ROLE.equals(userRole) && !UserConstant.ADMIN_ROLE.equals(userRole), ErrorCode.PARAMS_ERROR, "用户角色不合法");
        }
        if (userStatus != null) {
            ThrowUtils.throwIf(userStatus != 0 && userStatus != 1, ErrorCode.PARAMS_ERROR, "用户状态不合法");
        }
    }

    /**
     * 组装用户列表查询条件。
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (userQueryRequest == null) {
            return queryWrapper;
        }
        String keyword = userQueryRequest.getKeyword();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        if (StrUtil.isNotBlank(keyword)) {
            String keywordValue = keyword.trim();
            queryWrapper.and(wrapper -> {
                if (StrUtil.isNumeric(keywordValue)) {
                    wrapper.eq("id", Long.valueOf(keywordValue)).or();
                }
                wrapper.like("user_account", keywordValue).or().like("user_name", keywordValue);
            });
        }
        // 空字段不会参与查询，避免构造过多无效条件。
        queryWrapper.eq(userQueryRequest.getId() != null, "id", userQueryRequest.getId());
        queryWrapper.eq(StrUtil.isNotBlank(userQueryRequest.getUserAccount()), "user_account", userQueryRequest.getUserAccount());
        queryWrapper.like(StrUtil.isNotBlank(userQueryRequest.getUserName()), "user_name", userQueryRequest.getUserName());
        queryWrapper.eq(StrUtil.isNotBlank(userQueryRequest.getUserRole()), "user_role", userQueryRequest.getUserRole());
        queryWrapper.eq(userQueryRequest.getUserStatus() != null, "user_status", userQueryRequest.getUserStatus());
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    /**
     * 输出给前端的用户信息不包含密码、逻辑删除等敏感字段。
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    /**
     * 记录用户模块关键操作，审计失败不影响主流程。
     */
    @Override
    public void recordAudit(Long operatorId, Long targetUserId, String action, String detail) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setOperatorId(operatorId);
            User operator = operatorId == null ? null : this.getById(operatorId);
            auditLog.setOperatorAccount(operator == null ? null : operator.getUserAccount());
            auditLog.setTargetUserId(targetUserId);
            auditLog.setAction(action);
            auditLog.setDetail(detail);
            auditLog.setCreateTime(LocalDateTime.now());
            auditLogMapper.insert(auditLog);
        } catch (Exception ignored) {
            // 审计日志是辅助能力，不能因为日志写入失败中断用户操作。
        }
    }
}
