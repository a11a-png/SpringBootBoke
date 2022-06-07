package com.wjg.vueboke.dao;

import com.wjg.vueboke.po.SysComments;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysCommentsDao {
    List<SysComments> select();

    int deleteByPrimaryKey(Integer commentId);

    int insert(SysComments record);

//    SysComments selectByPrimaryKey(Integer commentId);

    int updateByPrimaryKeySelective(SysComments record);

    int updateByPrimaryKey(SysComments record);

    List<SysComments> selectAll(@Param("curr") Integer curr, @Param("page")Integer page, @Param("articleId")Integer articleId);

    List<SysComments> selectChild(@Param("commentId") Integer commentId);

    Integer selectCount(@Param("articleId") Integer articleId);

    //根据日期查询评论
    List<SysComments> selectBydate(@Param("startDate")String startDate,@Param("enddate")String enddate);
}