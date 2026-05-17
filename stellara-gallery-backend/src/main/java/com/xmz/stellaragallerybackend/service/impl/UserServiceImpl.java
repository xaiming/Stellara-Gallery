package com.xmz.stellaragallerybackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmz.stellaragallerybackend.common.ErrorCode;
import com.xmz.stellaragallerybackend.constant.UserConstant;
import com.xmz.stellaragallerybackend.exception.ThrowUtils;
import com.xmz.stellaragallerybackend.mapper.UserMapper;
import com.xmz.stellaragallerybackend.model.dto.user.UserQueryRequest;
import com.xmz.stellaragallerybackend.model.entity.User;
import com.xmz.stellaragallerybackend.model.vo.UserVO;
import com.xmz.stellaragallerybackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String SALT = "stellara_gallery";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userName) {
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号不能少于 4 位");
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码不能少于 8 位");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");

        synchronized (userAccount.intern()) {
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

    @Override
    public UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号错误");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码错误");

        String encryptPassword = getEncryptPassword(userPassword);
        User user = this.lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword)
                .one();
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        ThrowUtils.throwIf(ObjUtil.equal(user.getUserStatus(), 1), ErrorCode.FORBIDDEN_ERROR, "用户已被禁用");

        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user.getId());
        return getUserVO(user);
    }

    @Override
    public UserVO getLoginUser(HttpServletRequest request) {
        Object userIdObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(userIdObj == null, ErrorCode.NOT_LOGIN_ERROR);
        Long userId = Long.valueOf(String.valueOf(userIdObj));
        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(ObjUtil.equal(user.getUserStatus(), 1), ErrorCode.FORBIDDEN_ERROR, "用户已被禁用");
        return getUserVO(user);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        return DigestUtil.md5Hex(SALT + userPassword);
    }

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

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (userQueryRequest == null) {
            return queryWrapper;
        }
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        queryWrapper.eq(userQueryRequest.getId() != null, "id", userQueryRequest.getId());
        queryWrapper.eq(StrUtil.isNotBlank(userQueryRequest.getUserAccount()), "user_account", userQueryRequest.getUserAccount());
        queryWrapper.like(StrUtil.isNotBlank(userQueryRequest.getUserName()), "user_name", userQueryRequest.getUserName());
        queryWrapper.eq(StrUtil.isNotBlank(userQueryRequest.getUserRole()), "user_role", userQueryRequest.getUserRole());
        queryWrapper.eq(userQueryRequest.getUserStatus() != null, "user_status", userQueryRequest.getUserStatus());
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }
}
