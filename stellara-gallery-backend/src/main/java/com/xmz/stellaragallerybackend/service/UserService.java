package com.xmz.stellaragallerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmz.stellaragallerybackend.model.dto.user.UserChangePasswordRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserQueryRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserResetPasswordRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserUpdateRequest;
import com.xmz.stellaragallerybackend.model.entity.User;
import com.xmz.stellaragallerybackend.model.vo.UserVO;

/**
 * 用户业务服务接口。
 */
public interface UserService extends IService<User> {

    /**
     * 注册用户并返回新用户 ID。
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @param userName 用户昵称
     * @return 新用户 ID
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String userName);

    /**
     * 校验账号密码并写入 Sa-Token 登录态。
     */
    UserVO userLogin(String userAccount, String userPassword);

    /**
     * 获取当前登录用户。
     */
    UserVO getLoginUser();

    /**
     * 退出当前登录用户。
     */
    boolean userLogout();

    /**
     * 当前登录用户更新自己的个人资料。
     */
    boolean updateMyUser(UserUpdateRequest userUpdateRequest);

    /**
     * 当前登录用户修改自己的密码。
     */
    boolean changeMyPassword(UserChangePasswordRequest userChangePasswordRequest);

    /**
     * 管理员重置指定用户密码。
     */
    boolean resetUserPassword(UserResetPasswordRequest userResetPasswordRequest);

    /**
     * 记录用户模块审计日志。
     */
    void recordAudit(Long operatorId, Long targetUserId, String action, String detail);

    /**
     * 对明文密码进行加密。
     */
    String getEncryptPassword(String userPassword);

    /**
     * 校验用户实体字段是否合法。
     */
    void validUser(User user, boolean add);

    /**
     * 根据查询请求构造 MyBatis-Plus 查询条件。
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 将用户实体转换为前端可见的用户视图对象。
     */
    UserVO getUserVO(User user);
}
