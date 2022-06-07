package com.wjg.vueboke.config;

import com.wjg.vueboke.comment.AuthFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@Slf4j
public class shiroConfig {

    //安全管理器
    @Bean
    public SecurityManager securityManager(shiroRealm accountRealm){
        DefaultWebSecurityManager securityManager=new DefaultWebSecurityManager();
        //将Realm添加到安全管理器中
        securityManager.setRealm(accountRealm);
        log.info("---------------------->securityManager注入成功");
        return securityManager;
    }

    //过滤器链
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager){
        ShiroFilterFactoryBean filterFactoryBean=new ShiroFilterFactoryBean();
        //将安全管理器 添加到过滤器链
        filterFactoryBean.setSecurityManager(securityManager);
        //设置登录请求
        filterFactoryBean.setLoginUrl("/login");
        //设置登录成功页面
        filterFactoryBean.setSuccessUrl("/");
        //设置未授权
        filterFactoryBean.setUnauthorizedUrl("/Unautho");
        //添加自定义过滤器
        Map<String, Filter> filters=new HashMap<>();
        filters.put("auth",new AuthFilter());
        filterFactoryBean.setFilters(filters);
        /*
           anon: 无需认证就可以访问
           authc: 必须认证了才能访问
           user： 必须拥有记住我 功能才能用
           perms： 拥有对某个资源的权限才能访问
           role： 拥有某个角色权限才能访问
        */
        //添加过滤请求
        Map<String,String> hashMap=new LinkedHashMap<>();
        hashMap.put("/create","anon");
        hashMap.put("/DLlogin","anon");
        hashMap.put("/logout","anon");
        hashMap.put("/getYzm","anon");
        hashMap.put("/webjars/**", "anon");
        hashMap.put("/druid/**", "anon");
        hashMap.put("/v2/api-docs", "anon");
        hashMap.put("/hotArticle", "anon");
        hashMap.put("/lookboke", "anon");
        hashMap.put("/websocket/**","anon");
        hashMap.put("/selectAllComments/**","anon");
        hashMap.put("/file/**","anon");
        hashMap.put("/selectpost/**","anon");
        hashMap.put("/downfile/**","anon");
        hashMap.put("/selecttechologty/**","anon");

        // 除了以上路径，其他都需要权限验证
        hashMap.put("/**","auth");

        filterFactoryBean.setFilterChainDefinitionMap(hashMap);
        return filterFactoryBean;
    }


}
