package com.wjg.vueboke.po;

import lombok.Data;

import java.io.Serializable;

/**
 * sys_artitle_sort
 * @author 
 */
@Data
public class SysArtitleSort implements Serializable {
    private Integer articleId;

    private Integer sortId;

    private static final long serialVersionUID = 1L;
}