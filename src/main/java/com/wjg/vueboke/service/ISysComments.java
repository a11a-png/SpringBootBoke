package com.wjg.vueboke.service;

import com.wjg.vueboke.po.SysComments;
import com.wjg.vueboke.po.SysMessage;
import com.wjg.vueboke.comment.Result;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface ISysComments {

    List<SysComments> selectAll(Integer curr, Integer page, Integer articleId);

    Integer selectCount(Integer articleId);

    Result insert(SysMessage message) throws ParseException, IOException;

    Result insertTwo(SysMessage message) throws ParseException, IOException;

    //根据日期查询评论
    List<SysComments> selectBydate(String startDate,String enddate);
}
