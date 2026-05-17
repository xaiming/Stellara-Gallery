package com.xmz.stellaragallerybackend.model.dto.user;

import com.xmz.stellaragallerybackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private String userAccount;

    private String userName;

    private String userRole;

    private Integer userStatus;
}
