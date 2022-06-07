package com.wjg.vueboke.service;
import com.wjg.vueboke.comment.page;
import com.wjg.vueboke.po.SysArticles;
import com.wjg.vueboke.po.SysCustomer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ISysCustomer {

    Integer insert(SysCustomer user);

    SysCustomer selectBykey(Integer userid,String userName);

    SysCustomer selUserMessage(@Param("userid") Integer userid);

    int update(SysCustomer user);

    //我的收藏
    page selectmyCollect(Integer userid, Integer curr);
}
