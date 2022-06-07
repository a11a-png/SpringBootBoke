package com.wjg.vueboke.service.impl;

import com.wjg.vueboke.dao.SysMenuDao;
import com.wjg.vueboke.po.SysMenu;
import com.wjg.vueboke.service.ISysmenu;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SysmenuImpl implements ISysmenu {

    @Resource
    public SysMenuDao menuDao;


    @Override
    public List<SysMenu> selectmenu() {
        return menuDao.selectmenu();
    }
}
