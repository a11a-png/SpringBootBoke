package com.wjg.vueboke.dao;

import com.wjg.vueboke.po.SysUserFriends;

public interface SysUserFriendsDao {
    int deleteByPrimaryKey(Integer id);

    int insert(SysUserFriends record);

    int insertSelective(SysUserFriends record);

    SysUserFriends selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysUserFriends record);

    int updateByPrimaryKey(SysUserFriends record);
}