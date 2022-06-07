package com.wjg.vueboke.vo;

import com.wjg.vueboke.po.SysCustomer;
import lombok.Data;

import java.util.List;

@Data
public class customerVo extends SysCustomer {

    private List<SysCustomer> childList;

}
