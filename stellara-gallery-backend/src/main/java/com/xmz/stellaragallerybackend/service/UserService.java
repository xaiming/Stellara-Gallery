package com.xmz.stellaragallerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmz.stellaragallerybackend.model.dto.user.UserQueryRequest;
import com.xmz.stellaragallerybackend.model.entity.User;
import com.xmz.stellaragallerybackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

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

    UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    UserVO getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);

    String getEncryptPassword(String userPassword);

    void validUser(User user, boolean add);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    UserVO getUserVO(User user);
}
