package com.wjg.vueboke.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.wjg.vueboke.comment.page;
import com.wjg.vueboke.dao.SysArticlesDao;
import com.wjg.vueboke.dao.SysCustomerDao;
import com.wjg.vueboke.po.SysArticles;
import com.wjg.vueboke.po.SysCustomer;
import com.wjg.vueboke.service.ISysCustomer;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("iSysCustomer")
public class SysCustomerImpl implements ISysCustomer {

    @Resource
    public SysCustomerDao sysCustomerDao;
    @Resource
    public SysArticlesDao sysArticlesDao;

    @Override
    public Integer insert(SysCustomer user) {
        return sysCustomerDao.insert(user);
    }

    @Override
    public SysCustomer selectBykey(Integer userid,String userName) {
        return sysCustomerDao.selectByPrimaryKey(userid,userName);
    }

    //查询用户信息和未读消息数
    @Override
    public SysCustomer selUserMessage(Integer userid) {
        return sysCustomerDao.selUserMessage(userid);
    }

    @Override
    public int update(SysCustomer user) {
        return sysCustomerDao.update(user);
    }

    @Override
    public page selectmyCollect(Integer userid, Integer curr){
        int currpage=(curr-1)*3;
        int page=curr*3;
        SysCustomer customer=sysCustomerDao.selectcollectAndlike(userid);
        List<SysArticles> articles=new ArrayList<>();
        String[] strarr = customer.getCollectPostId().split(",");
        for (int i=currpage;i<page;i++) {
            if (i> (strarr.length-1)){
                break;
            }
            SysArticles select=sysArticlesDao.selectuserCollect(Integer.valueOf(strarr[i]));
            articles.add(select);
        }
        page pg=new page();
        pg.setDatalist(articles);
        pg.setCount(strarr.length);
        return pg;
    }


//    @Override
//    public shiroFile selectByPrimaryKey(Integer userid, String userIP,String password) {
//        SysCustomer customer= sysCustomerDao.selectByPrimaryKey(userid,userIP);
//        if (customer==null){
//            throw new UnknownAccountException();
//        }else if (!customer.getUserPassword().equals(password)) {
//            throw new IncorrectCredentialsException();
//        }
//
//        shiroFile shirodata=new shiroFile();
//        //反射赋值
//        BeanUtil.copyProperties(customer,shirodata);
//
//        return shirodata;
//    }

    //获取用户（用于消息聊天）
//    @Override
//    public mineUser getmineUser(){
//        mineUser minuser=new mineUser();
//        shiroFile user= (shiroFile)SecurityUtils.getSubject().getPrincipal();
//        if (user!=null){
//            minuser.setId(user.getUserid());
//            minuser.setAvatar(user.getUserPhoto());
//            minuser.setStatus("online");
//            minuser.setUsername(user.getUserName());
//        }else{
//            Integer imuserID=(Integer) SecurityUtils.getSubject().getSession().getAttribute("imUserId");
//            minuser.setId(imuserID!=null? imuserID: RandomUtil.randomInt());
//            SecurityUtils.getSubject().getSession().setAttribute("imUserId",minuser.getId());
//            //匿名用户
//            minuser.setAvatar("http://tp1.sinaimg.cn/5619439268/180/40030060651/1");
//            minuser.setStatus("online");
//            minuser.setUsername("匿名用户");
//        }
//        return minuser;
//    }
}
