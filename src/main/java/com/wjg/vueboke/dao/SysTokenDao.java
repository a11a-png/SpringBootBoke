package com.wjg.vueboke.dao;

import com.wjg.vueboke.po.SysToken;
import org.apache.ibatis.annotations.Param;

public interface SysTokenDao{
    /**
     * 通过token查找
     * @param token
     * @return
     */
    SysToken selectByToken(@Param("token") String token);

    /**
     * 通过userID查找
     * @param userid
     * @return
     */
    SysToken selectById(@Param("userid") Integer userid);

    int save(SysToken token);

    int update(SysToken token);

}
