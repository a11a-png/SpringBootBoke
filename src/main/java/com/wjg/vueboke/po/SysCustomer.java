package com.wjg.vueboke.po;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * sys_customer
 * @author 
 */
@Data
public class SysCustomer implements Serializable {
    private Integer userid;

    @NotBlank(message = "账号不能为空")
    private String userip;

    private String userName;

    @NotBlank(message = "密码不能为空")
    private String userPassword;

    private String userEmail;

    private String userPhoto;

    private Date createTime;

    private Date userBirthday;

    private Integer userAge;

    private String userPhone;

    private String userZsname;

    private String salt;

    private Integer roleID;

    private String yzm;

    private String token;  //验证登录token

    private Integer message; //未读消息

    private String likePostId; //点赞文章

    private String collectPostId;  //收藏文章

    private String focusUserId;  //关注ID

    private String focusMyuserId;  //關注我的ID

    private Byte add; //0 代表关注  -1代表取关

    private static final long serialVersionUID = 1L;
}