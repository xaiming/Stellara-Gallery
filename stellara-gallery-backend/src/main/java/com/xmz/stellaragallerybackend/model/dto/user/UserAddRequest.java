package com.xmz.stellaragallerybackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAddRequest implements Serializable {

    private String userAccount;

    private String userPassword;

    private String userName;

    private String userAvatar;

    private String userCover;

    private String userProfile;

    private String userRole;

    private Integer userStatus;
}
