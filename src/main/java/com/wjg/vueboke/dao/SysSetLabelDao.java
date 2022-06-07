package com.wjg.vueboke.dao;

import com.wjg.vueboke.po.SysSetLabel;

public interface SysSetLabelDao {
    int deleteByPrimaryKey(Integer wzId);

    int insert(SysSetLabel record);

    int insertSelective(SysSetLabel record);

    SysSetLabel selectByPrimaryKey(Integer wzId);

    int updateByPrimaryKeySelective(SysSetLabel record);

    int updateByPrimaryKey(SysSetLabel record);
}