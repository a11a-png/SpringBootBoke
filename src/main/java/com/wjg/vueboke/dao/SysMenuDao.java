package com.wjg.vueboke.dao;

import com.wjg.vueboke.po.SysMenu;

import java.util.List;

public interface SysMenuDao {
    int deleteByPrimaryKey(Integer menuId);

    int insert(SysMenu record);

    int insertSelective(SysMenu record);

    SysMenu selectByPrimaryKey(Integer menuId);

    List<SysMenu> selectmenu();

    int updateByPrimaryKeySelective(SysMenu record);

    int updateByPrimaryKey(SysMenu record);
}