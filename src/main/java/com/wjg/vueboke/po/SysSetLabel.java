package com.wjg.vueboke.po;

import lombok.Data;

import java.io.Serializable;

/**
 * sys_set_label
 * @author 
 */
@Data
public class SysSetLabel implements Serializable {
    /**
     * 文章ID
     */
    private Integer wzId;

    /**
     * 标签ID
     */
    private Integer bqId;

    private static final long serialVersionUID = 1L;
}