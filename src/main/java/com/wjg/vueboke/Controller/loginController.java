package com.wjg.vueboke.Controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.digest.DigestUtil;
import com.wjg.vueboke.comment.Result;
import com.wjg.vueboke.po.SysCustomer;
import com.wjg.vueboke.po.SysSort;
import com.wjg.vueboke.po.Systechnology;
import com.wjg.vueboke.service.IShort;
import com.wjg.vueboke.service.ISysCustomer;
import com.wjg.vueboke.service.ISysmessage;
import com.wjg.vueboke.service.ShiroService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class loginController {
     @Autowired
     HttpServletResponse response;
     @Autowired
     HttpServletRequest request;
     @Resource
     public ISysCustomer iSysCustomer;
     @Resource
     public ShiroService shiroService;
     @Autowired
     public IShort shortImpl;
     @Autowired
     public ISysmessage sysmessage;

     @GetMapping("/login")
     public Result login(){
         return Result.success().action("/login");
     }

     @GetMapping("/createUser")
     public Result createUser(){
         return Result.success().action("/createUser");
     }

     //验证码
     @GetMapping("/getYzm")
     public void getYzm(@RequestParam("time") Long time) throws IOException {
          // 随机生成 4 位数
          RandomGenerator randomGenerator=new RandomGenerator("0123456789",4);
          //设置图片大小
          LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200,40);
          response.setContentType("image/jepg");
          response.setHeader("Pragma","No-cache");
          lineCaptcha.setGenerator(randomGenerator);
          //将验证码存到session中
          request.getSession().setAttribute("YZM",lineCaptcha.getCode());
          //输出流
          lineCaptcha.write(response.getOutputStream());
          //关闭流
          response.getOutputStream().close();
     }

     //验证登录
     @PostMapping("/DLlogin")
     public Result DLlogin(@RequestBody SysCustomer customer){
          //判断是否为空
          if (customer.getUserName().isEmpty()){
               return Result.error("请填写账号");
          }
          if (customer.getUserPassword().isEmpty()){
               return Result.error("请填写密码");
          }
          if (customer.getYzm().isEmpty()){
               return Result.error("请填写验证码");
          }
          //判断验证码是否正确
          String getyzm=(String)request.getSession().getAttribute("YZM");
          if (!getyzm.equals(customer.getYzm())){
               return Result.error("验证码错误");
          }
          //判断密码是否正确
          SysCustomer rz=iSysCustomer.selectBykey(null,customer.getUserName());
          if (rz==null){
               return Result.error("用户不存在");
          }
          String md5pass= DigestUtil.md5Hex(customer.getUserPassword()+rz.getSalt());
          if (!md5pass.equals(rz.getUserPassword())){
               return Result.error("密码输入错误");
          }
          int messnum=sysmessage.selectWD(rz.getUserid());
          rz.setMessage(messnum);
          List<Object> obj=new ArrayList<>();
          rz.setToken(shiroService.createToken(rz.getUserid()));
          //查询分类信息
          List<SysSort> list = shortImpl.select(null);
          //查询内容分类信息
          List<Systechnology> lit=shortImpl.selecttechnology();
          obj.add(rz);
          obj.add(list);
          obj.add(lit);
          return Result.success("登录成功",obj).action("/");
     }

     //注册
     @PostMapping("/create")
     public Result create(@RequestBody SysCustomer customer){
          //判断是否为空
          if (customer.getUserName().isEmpty()){
             return Result.error("请填写账号");
          }
          if (customer.getUserPassword().isEmpty()){
               return Result.error("请填写密码");
          }
          if (customer.getYzm().isEmpty()){
               return Result.error("请填写验证码");
          }
          //判断验证码是否正确
          String getyzm=(String)request.getSession().getAttribute("YZM");
          if (!getyzm.equals(customer.getYzm())){
               return Result.error("验证码错误");
          }
          SysCustomer save=new SysCustomer();
          String str="";
          //随机生成6位数
          for (int i=0;i<7;i++){
            str+=String.valueOf(Math.round((Math.random()*9)+1));
          }
          //加密保存
          save.setSalt(str);
          save.setUserPassword(DigestUtil.md5Hex(customer.getUserPassword()+str));
          save.setUserName(customer.getUserName());
          customer.setCreateTime(new Date());
          int i=iSysCustomer.insert(save);
          if (i<0){
               return Result.error("注册失败");
          }
          return Result.success("注册成功").action("/login");
     }

     //注销
     @GetMapping("/logout")
     public Result logout(@RequestParam("token") String token){
          //修改原来的token值
          shiroService.logout(token);
          return Result.success("注销成功").action("/");
     }
}
