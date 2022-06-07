package com.wjg.vueboke.service.impl;

import com.wjg.vueboke.comment.Result;
import com.wjg.vueboke.comment.TokenGenerator;
import com.wjg.vueboke.dao.SysTokenDao;
import com.wjg.vueboke.po.SysCustomer;
import com.wjg.vueboke.po.SysToken;
import com.wjg.vueboke.service.ISysCustomer;
import com.wjg.vueboke.service.ShiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("shiroService")
public class ShiroServiceImpl implements ShiroService {

    @Resource
    private ISysCustomer iSysCustomer;
    @Resource
    private SysTokenDao sysTokenDao;

    /**
     * 根据username查找用户
     *
     * @param username
     * @return User
     */
    @Override
    public SysCustomer findByUsername(String username) {
        SysCustomer user = iSysCustomer.selectBykey(null,username);
        return user;
    }

    //12小时后过期
    private final static int EXPIRE = 3600 * 12;

    @Override
    /**
     * 生成一个token
     *@param  [userId]
     *@return Result
     */
    public String createToken(Integer userId) {
        //生成一个token
        String token = TokenGenerator.generateValue();
        //当前时间
        Date now = new Date();
        //过期时间
        Date expireTime = new Date(now.getTime() + EXPIRE * 1000);
        //判断是否生成过token
        SysToken tokenEntity = sysTokenDao.selectById(userId);
        if (tokenEntity == null) {
            tokenEntity = new SysToken();
            tokenEntity.setId(userId);
            tokenEntity.setToken(token);
            tokenEntity.setUpdateTime(now);
            tokenEntity.setExpireTime(expireTime);
            //保存token
            sysTokenDao.save(tokenEntity);
        } else {
            tokenEntity.setToken(token);
            tokenEntity.setUpdateTime(now);
            tokenEntity.setExpireTime(expireTime);
            tokenEntity.setId(userId);
            //更新token
            sysTokenDao.update(tokenEntity);
        }
        return token;
    }

    /*
    * 用来注销用户的，根据token查询对应的数据，再用修改更新token
    * */
    @Override
    public void logout(String token) {
        SysToken byToken = findByToken(token);
        //生成一个token
        token = TokenGenerator.generateValue();
        //修改token
        SysToken tokenEntity = new SysToken();
        tokenEntity.setId(byToken.getId());
        tokenEntity.setToken(token);
        sysTokenDao.update(tokenEntity);
    }

    /*
    * 根据token查询对应的token数据
    * */
    @Override
    public SysToken findByToken(String accessToken) {
        return sysTokenDao.selectByToken(accessToken);
    }

    /*
     * 根据用户ID查询对应的token数据
     * */
    @Override
    public SysCustomer findByUserId(Integer userId) {
        return iSysCustomer.selectBykey(userId,null);
    }
}
