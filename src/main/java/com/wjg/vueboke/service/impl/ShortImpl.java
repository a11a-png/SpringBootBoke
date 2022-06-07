package com.wjg.vueboke.service.impl;

import com.wjg.vueboke.dao.SysSortDao;
import com.wjg.vueboke.dao.SystechnologyDao;
import com.wjg.vueboke.po.SysSort;
import com.wjg.vueboke.po.Systechnology;
import com.wjg.vueboke.service.IShort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ShortImpl implements IShort {

    @Resource
    public SysSortDao sysSortDao;
    @Resource
    public SystechnologyDao systechnologyDao;

    @Override
    public List<SysSort> select(Integer sortId) {
        return sysSortDao.select(sortId);
    }

    @Override
    public List<Systechnology> selecttechnology() {
        return systechnologyDao.selectAll();
    }
}
