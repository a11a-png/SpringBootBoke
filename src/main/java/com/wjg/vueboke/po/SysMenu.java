package com.wjg.vueboke.po;

import lombok.Data;

import java.io.Serializable;

/**
 * sys_menu
 * @author 
 */
@Data
public class SysMenu implements Serializable {
    private Integer menuId;

    private String menuName;

    private static final long serialVersionUID = 1L;
}