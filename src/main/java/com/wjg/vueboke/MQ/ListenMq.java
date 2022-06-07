package com.wjg.vueboke.MQ;

import com.wjg.vueboke.comment.WebSocket;
import com.wjg.vueboke.po.SysArticles;
import com.wjg.vueboke.po.SysMessage;
import com.wjg.vueboke.service.ISysarticles;
import com.wjg.vueboke.service.ISysmessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

//监听队列
@Component
@Slf4j
public class ListenMq {

    @Autowired
    public ISysarticles sysarticles;
    @Autowired
    public ISysmessage sysmessage;
    @Autowired
    public WebSocket webSocket;

    @RabbitListener(queues = "esQueue")
    public void takeEs(String articlesId) throws IOException {
        sysarticles.UploadEs(Integer.valueOf(articlesId));
    }

    @RabbitListener(queues = "dyQueue")
    public void takefocusmy(SysArticles articles) throws IOException {
        String[] arrid=articles.getFocusMyuserId().split("_");
        for (String str:arrid) {
            SysMessage message=new SysMessage();
            if (str.equals("")){
                continue;
            }
            message.setFromuserid(articles.getUserId());
            message.setTouserid(Integer.valueOf(str));
            message.setPotsid(articles.getArticlesId());
            message.setMessage("你关注的"+articles.getUserName()+"用户发表了新的文章，文章标题为："+articles.getArticlesTitle());
            message.setType((byte)0);
            message.setStatus((byte)1);
            message.setMessDate(new Date());
            int isok=sysmessage.insert(message);
            if (isok>0){
                int count=sysmessage.selectWD(Integer.valueOf(str));
                webSocket.sendOneMessage(Integer.valueOf(str),String.valueOf(count));
            }else{
                log.info(str+"通知异常");
            }
        }
    }

}
