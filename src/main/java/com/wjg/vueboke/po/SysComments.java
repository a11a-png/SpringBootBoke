package com.wjg.vueboke.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * sys_comments
 * @author 
 */
@Data
public class SysComments implements Serializable {
    private Integer commentId;

    private Long userId;

    private Long articleId;

    private Long likeCount;

    private Date commentDate;

    private String commentContent;

    private Integer parentCommentId;

    private Integer toUserId;

    private String userName; //发评论人

    private String userPhoto;  //评论人头像

    private String toUserName; //回复评论人名

    private List<SysComments> childList;

    private static final long serialVersionUID = 1L;
}