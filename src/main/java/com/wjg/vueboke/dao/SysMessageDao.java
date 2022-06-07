package com.wjg.vueboke.dao;

import com.wjg.vueboke.po.SysMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysMessageDao {
    int deleteByPrimaryKey(Integer messageId);

    int insert(SysMessage record);

    SysMessage selectByPrimaryKey(Integer messageId);

    int updateByPrimaryKeySelective(SysMessage record);

    int updateByPrimaryKey(SysMessage record);

    List<SysMessage> selectAll(@Param("curr")Integer curr,@Param("page")Integer page,@Param("userID") Integer userID);

    List<SysMessage> selectTomess(@Param("curr")Integer curr,@Param("page")Integer page,@Param("toUserId") Integer toUserId);

    //查询未读消息总数
    int selectWD(@Param("userid") Integer userid);

    int selectCount(@Param("userid") Integer userid);

    //设置已读状态
    int updateYD(@Param("userid") Integer userid);
}