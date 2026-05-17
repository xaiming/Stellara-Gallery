package com.xmz.stellaragallerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmz.stellaragallerybackend.model.dto.user.UserQueryRequest;
import com.xmz.stellaragallerybackend.model.dto.user.UserUpdateRequest;
import com.xmz.stellaragallerybackend.model.entity.User;
import com.xmz.stellaragallerybackend.model.vo.UserVO;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @param userName
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String userName);

    UserVO userLogin(String userAccount, String userPassword);

    UserVO getLoginUser();

    boolean userLogout();

    boolean updateMyUser(UserUpdateRequest userUpdateRequest);

    String getEncryptPassword(String userPassword);

    void validUser(User user, boolean add);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    UserVO getUserVO(User user);
}
