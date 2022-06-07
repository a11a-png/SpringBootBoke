package com.wjg.vueboke.dao;

import com.wjg.vueboke.po.SysArtitleSort;

public interface SysArtitleSortDao {
    int deleteByPrimaryKey(Integer articleId);

    int insert(SysArtitleSort record);

    int insertSelective(SysArtitleSort record);

    SysArtitleSort selectByPrimaryKey(Integer articleId);

    int updateByPrimaryKeySelective(SysArtitleSort record);

    int updateByPrimaryKey(SysArtitleSort record);
}