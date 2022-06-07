package com.wjg.vueboke.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * sys_message
 * @author 
 */
@Data
public class SysMessage implements Serializable {
    private Integer messageId;

    private Integer fromuserid;  //发送消息方

    private Integer touserid;    //接收消息方

    private Integer potsid;    //评论文章ID

    private Integer commentid;  //评论ID

    private String message;   //评论消息

    /**
     * 0系统消息   1评论消息   2回复消息
     */
    private Byte type;

    /**
     * 0已读   1未读
     */
    private Byte status;

    private String fromName; //发送方姓名

    private String toName; //接收方姓名

    private String articlesTitle; //文章标题

    private Date messDate; //日期

    private Integer postUserId; //文章作者ID

    private Integer parentCommentId; //父评论ID

    private Integer currentPage;  //当前页数

    private Integer pageSize;   //查询条数

    private Integer add; //用于点赞收藏[0代表加一,-1代表减一]

    private static final long serialVersionUID = 1L;
}