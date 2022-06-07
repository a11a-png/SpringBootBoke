package com.wjg.vueboke.Controller;

import cn.hutool.core.io.FileUtil;
import com.wjg.vueboke.comment.Result;
import com.wjg.vueboke.comment.page;
import com.wjg.vueboke.po.SysArticles;
import com.wjg.vueboke.po.SysCustomer;
import com.wjg.vueboke.po.SysMessage;
import com.wjg.vueboke.service.ISysCustomer;
import com.wjg.vueboke.service.ISysarticles;
import com.wjg.vueboke.service.ISysmessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class MessageController {

    @Autowired
    private ISysCustomer customer;
    @Autowired
    private ISysarticles sysarticles;
    @Autowired
    private ISysmessage sysmessage;

    public String userpoho="";

    //上传用户图片
    @RequestMapping("/uploadUser")
    public Result uploadUser(MultipartFile file,@RequestParam("userid") Integer userid) throws IOException, ParseException {
          //创建保存文件名
          //判断是否上传图片
          if (file!=null && file.getOriginalFilename()!=""){
              String savefile="F:\\xianmu\\VueBoke\\src\\main\\resources\\static\\file\\userimg\\"+userid+"_"+file.getOriginalFilename();
              //用户保存文件名
              userpoho="http://localhost:8801/file/userimg/"+userid+"_"+file.getOriginalFilename();
              File fe=new File(savefile);
              //判断目录是否存在
              if (fe.getParentFile().exists()){
                  fe.getParentFile().mkdirs();
              }
              //查询用户是否有头像
              SysCustomer cus=customer.selectBykey(userid,null);
              if (cus.getUserPhoto()!=null){
                  //删除以前的文件图片
                  File delefile=new File(cus.getUserPhoto());
                  delefile.delete();
              }
              //进行新增文件和修改用户头像
              FileUtil.writeBytes(file.getBytes(),savefile);
          }
          return Result.success();
    }

    //修改用户信息
    @PostMapping("/upload")
    public Result upload(@RequestBody SysCustomer cus){
        if (userpoho!=null && userpoho!=""){
            cus.setUserPhoto(userpoho);
        }
        int count=customer.update(cus);
        if (count>0){
            return Result.success("修改成功",cus);
        }
        return Result.error("修改失败");
    }

    //获取我的文章
    @PostMapping("/getmypost")
    public Result getmypost(@RequestParam("userid") Integer userid,@RequestParam("curr") Integer curr){
        //查询文章
        List<SysArticles> articles=sysarticles.selectAll(3,curr,null,userid,null);
        int count =sysarticles.selectCount(null,userid,null);
        page pg=new page();
        pg.setDatalist(articles);
        pg.setCount(count);
        return Result.success(null,pg);
    }

    //我的收藏
    @PostMapping("/getmyCollect")
    public Result getmyCollect(@RequestParam("userid") Integer userid,@RequestParam("curr") Integer curr){
        page articles=customer.selectmyCollect(userid,curr);
        return Result.success(null,articles);
    }

    //我的消息
    @PostMapping("/getmyMessage")
    public Result getmyMessage(@RequestParam("userid") Integer userid,@RequestParam("curr") Integer curr){
         //设置已读
         sysmessage.updateYD(userid);
         int jump=(curr-1)*10;
         List<SysMessage> messages=sysmessage.selectTomess(jump,10,userid);
         int count = sysmessage.selectCount(userid);
         page pg=new page();
         pg.setDatalist(messages);
         pg.setCount(count);
         return Result.success(null,pg);
    }

    //查詢關注我的用戶
    @GetMapping("/selectfocusmy")
    public Result selectfocusmy(@RequestParam("focusMyuserId") String focusMyuserId){
          String[] arrid=focusMyuserId.split("_");
          List<SysCustomer> data=new ArrayList<>();
        for (String str:arrid) {
            if (str.equals("")){
                continue;
            }
            SysCustomer cus=customer.selectBykey(Integer.valueOf(str),null);
            if (cus!=null){
                data.add(cus);
            }
        }
          return Result.success(null,data);
    }

    //查詢我關注的用戶
    @GetMapping("/selectmyfocus")
    public Result selectmyfocus(@RequestParam("focusUserId") String focusUserId){
        String[] arrid=focusUserId.split("_");
        List<SysCustomer> data=new ArrayList<>();
        for (String str:arrid) {
            if (str.equals("")){
                continue;
            }
            SysCustomer cus=customer.selectBykey(Integer.valueOf(str),null);
            if (cus!=null){
                data.add(cus);
            }
        }
        return Result.success(null,data);
    }
}
