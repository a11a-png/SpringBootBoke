package com.wjg.vueboke.dao;

import com.wjg.vueboke.po.SysCollection;
import org.apache.ibatis.annotations.Param;

public interface SysCollectionDao {
    int deleteByPrimaryKey(Integer collectionId);

    int insert(SysCollection record);

    int insertSelective(SysCollection record);

    SysCollection selectByPrimaryKey(Integer collectionId);

    int updateByPrimaryKeySelective(SysCollection record);

    int updateByPrimaryKey(SysCollection record);

    Integer selectCount(@Param("customer_id") Integer customer_id);
}