package com.wjg.vueboke.service;

import com.wjg.vueboke.comment.Result;
import com.wjg.vueboke.po.SysCustomer;
import com.wjg.vueboke.po.SysToken;

import java.util.Map;

public interface ShiroService {
    SysCustomer findByUsername(String username);
    String createToken(Integer userId);
    void logout(String token);
    SysToken findByToken(String accessToken);
    SysCustomer findByUserId(Integer userId);
}
