package com.wjg.vueboke.service.impl;

import com.wjg.vueboke.dao.SysMessageDao;
import com.wjg.vueboke.po.SysMessage;
import com.wjg.vueboke.service.ISysmessage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SysmessageImpl implements ISysmessage {

    @Resource
    public SysMessageDao sysmessage;

    @Override
    public List<SysMessage> selectAll(Integer curr,Integer page, Integer userID) {
        return sysmessage.selectAll(curr,page,userID);
    }

    @Override
    public List<SysMessage> selectTomess(Integer curr, Integer page, Integer userID) {
        return sysmessage.selectTomess(curr, page, userID);
    }

    @Override
    public int insert(SysMessage record) {
        return sysmessage.insert(record);
    }

    @Override
    public int selectWD(Integer userid) {
        return sysmessage.selectWD(userid);
    }

    @Override
    public int selectCount(Integer userid) {
        return sysmessage.selectCount(userid);
    }

    @Override
    public int updateYD(Integer userid) {
        return sysmessage.updateYD(userid);
    }
}
