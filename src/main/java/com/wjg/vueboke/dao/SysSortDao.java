package com.wjg.vueboke.dao;

import com.wjg.vueboke.po.SysSort;

import java.util.List;

public interface SysSortDao {
    int deleteByPrimaryKey(Integer sortId);

    int insert(SysSort record);

    int insertSelective(SysSort record);

    List<SysSort> select(Integer sortId);

    int updateByPrimaryKeySelective(SysSort record);

    int updateByPrimaryKey(SysSort record);
}