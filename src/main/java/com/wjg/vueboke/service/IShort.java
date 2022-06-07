package com.wjg.vueboke.service;

import com.wjg.vueboke.po.SysSort;
import com.wjg.vueboke.po.Systechnology;

import java.util.List;

public interface IShort {

    List<SysSort> select(Integer sortId);

    List<Systechnology> selecttechnology();
}
