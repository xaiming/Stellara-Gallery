package com.xmz.stellaragallerybackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateRequest implements Serializable {

    private Long id;

    private String userName;

    private String userAvatar;

    private String userCover;

    private String userProfile;

    private String userRole;

    private Integer userStatus;
}
