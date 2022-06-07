package com.wjg.vueboke.po;

import lombok.Data;

import java.io.Serializable;

/**
 * sys_user_friends
 * @author 
 */
@Data
public class SysUserFriends implements Serializable {
    private Integer id;

    private Integer userId;

    private Integer userFirendId;

    private String userNote;

    private String userStatus;

    private static final long serialVersionUID = 1L;
}