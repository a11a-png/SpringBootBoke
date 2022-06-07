package com.wjg.vueboke.config;

import cn.hutool.crypto.digest.DigestUtil;
import com.wjg.vueboke.po.SysCustomer;
import com.wjg.vueboke.po.SysRole;
import com.wjg.vueboke.po.SysToken;
import com.wjg.vueboke.service.ISysCustomer;
import com.wjg.vueboke.service.ShiroService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class shiroRealm extends AuthorizingRealm {

    @Resource
    public ISysCustomer iSysCustomer;
    @Resource
    public ShiroService shiroService;

    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        //获取用户
        //1. 从 PrincipalCollection 中来获取登录用户的信息
        SysCustomer user = (SysCustomer) principalCollection.getPrimaryPrincipal();
        //2.添加角色和权限
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRole(String.valueOf(user.getRoleID()));
        return simpleAuthorizationInfo;
    }

    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //获取token，即前端传入的token
        String accessToken=(String) authenticationToken.getPrincipal();
        //根据accessToken 查询用户信息
        SysToken tokenEntity=shiroService.findByToken(accessToken);
        //判断token失效
//        if (tokenEntity == null || tokenEntity.getExpireTime().getTime() < System.currentTimeMillis()) {
//            throw new IncorrectCredentialsException("token失效，请重新登录");
//        }
        if (tokenEntity == null) {
            throw new IncorrectCredentialsException("token失效，请重新登录");
        }
        //查询用户信息
        SysCustomer customer=shiroService.findByUserId(tokenEntity.getId());
        if (customer==null){
            throw new UnknownAccountException("用户不存在!");
        }
        return new SimpleAuthenticationInfo(customer,accessToken,"");
    }
}
