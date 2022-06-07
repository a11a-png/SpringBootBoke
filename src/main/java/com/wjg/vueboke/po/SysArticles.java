package com.wjg.vueboke.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * sys_articles
 * @author 
 */
@Data
public class SysArticles implements Serializable {
    private Integer articlesId;

    private Integer userId;

    private String articlesTitle;

    private String articlesContent;

    private Integer articlesViews;

    private Integer articlesCount;  //评论数量

    private Date articlesDate;

    private Integer likeCount;

    private String userName;

    private String articlesCover;

    private String sortName;

    private Integer sortId;

    private String title;

    private Integer itemID;

    private Integer collectCount;

    private String likeUserId;

    private String collectUserId;

    private Integer technologyId;

    private String technology;

    private byte add;  // 用于点赞收藏[0代表加一,-1代表减一]

    private String focusMyuserId;

    private static final long serialVersionUID = 1L;
}