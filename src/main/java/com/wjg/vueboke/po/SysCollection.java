package com.wjg.vueboke.po;

import lombok.Data;

import java.io.Serializable;

/**
 * sys_collection
 * @author 
 */
@Data
public class SysCollection implements Serializable {
    /**
     * 收藏ID
     */
    private Integer collectionId;

    /**
     * 用户ID
     */
    private Integer customerId;

    /**
     * 博文ID
     */
    private Integer articlesId;

    private static final long serialVersionUID = 1L;
}