package com.wjg.vueboke.dao;

import com.wjg.vueboke.po.SysCustomer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysCustomerDao {
    int deleteByPrimaryKey(Integer userid);

    int insert(SysCustomer record);

    SysCustomer selectByPrimaryKey(@Param("userid") Integer userid,@Param("userName") String userName);

    int update(SysCustomer record);

    SysCustomer selUserMessage(@Param("userid") Integer userid);

    SysCustomer selectcollectAndlike(@Param("userid") Integer userid);
}