package com.wjg.vueboke.service.impl;

import com.wjg.vueboke.dao.SysMessageDao;
import com.wjg.vueboke.service.IWsservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WsserviceImpl implements IWsservice {

    @Resource
    public SysMessageDao sysMessageDao;
//    @Autowired
//    public SimpMessagingTemplate messagingTemplate;

    //推送消息
    @Async //异步处理
    @Override
    public void sendMessage(Integer userId) {
//        //查询未读的消息总数
//        //userId接收消息的用户ID
//       int count= sysMessageDao.selectWD(userId);
//        /*
//           websocket通知 (/user/userId/messCount)  user在config中配置的
//           count为传递消息信息
//        */
//       messagingTemplate.convertAndSendToUser(userId.toString(),"/messCount",count);
    }




}
