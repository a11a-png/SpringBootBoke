package com.wjg.vueboke.service.impl;

import com.wjg.vueboke.service.IChat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

//@Service
//public class ChatImpl implements IChat {
//
////    @Autowired
////    RedisTemplate redisTemplate;
////
////    @Override
////    public void setGroupHistoryMsg(ImMess message) {
////        redisTemplate.boundSetOps("message").add(message);
////    }
////
////    @Override
////    public Set<Object> getGroupHistoryMsg() {
////        //根据大小获取对应的信息
////        return redisTemplate.boundSetOps("message").members();
////    }
//}
