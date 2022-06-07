package com.wjg.vueboke.po;

import lombok.Data;

import java.io.Serializable;

/**
 * sys_sort
 * @author 
 */
@Data
public class SysSort implements Serializable {
    private Integer sortId;

    private String sortName;

    private String sortAlias;

    private String sortDescription;

    private Integer parentSortId;

    private static final long serialVersionUID = 1L;
}