package com.wjg.vueboke.service.impl;

import com.alibaba.fastjson.JSON;
import com.wjg.vueboke.comment.Result;
import com.wjg.vueboke.comment.WebSocket;
import com.wjg.vueboke.comment.page;
import com.wjg.vueboke.dao.SysArticlesDao;
import com.wjg.vueboke.dao.SysCommentsDao;
import com.wjg.vueboke.dao.SysMessageDao;
import com.wjg.vueboke.dao.SysMessageDao;
import com.wjg.vueboke.po.SysArticles;
import com.wjg.vueboke.po.SysComments;
import com.wjg.vueboke.po.SysCustomer;
import com.wjg.vueboke.po.SysMessage;
import com.wjg.vueboke.service.ISysComments;
import com.wjg.vueboke.service.IWsservice;
import com.wjg.vueboke.vo.customerVo;
import org.apache.shiro.SecurityUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SysCommentsImpl implements ISysComments {

    @Resource
    public SysCommentsDao commentsDao;
    @Resource
    public SysMessageDao sysMessageDao;
    @Resource
    public SysArticlesDao articlesDao;
    @Autowired
    public RedisTemplate redisTemplate;
    @Autowired
    public WebSocket webSocket;
    @Autowired
    public RestHighLevelClient restHighLevelClient;

    @Override
    public List<SysComments> selectAll(Integer curr, Integer page, Integer articleId) {
        int current=(curr-1)*page;
        List<SysComments> data=child(commentsDao.selectAll(current,page,articleId));
        return data;
    }

    @Override
    public Integer selectCount(Integer articleId) {
        return commentsDao.selectCount(articleId);
    }

    //新增评论
    @Transactional  //事务
    @Override
    public Result insertTwo(SysMessage message) throws ParseException, IOException {
        SysComments comments=new SysComments();
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-mm-dd");
        String now=dateFormat.format(new Date());
        Calendar c=Calendar.getInstance();
        //判断是否自己回复自己
        if (message.getTouserid()==message.getFromuserid()){
            return Result.error("请不要回复自己的评论");
        }
        //新增评论表
        comments.setUserId(Long.valueOf(message.getFromuserid()));
        comments.setToUserId(message.getTouserid().intValue());
        comments.setArticleId(Long.valueOf(message.getPotsid()));
        comments.setLikeCount(Long.valueOf(0));
        comments.setCommentDate(c.getTime());
        comments.setCommentContent(message.getMessage());
        comments.setParentCommentId(message.getParentCommentId()); //父评论ID
        commentsDao.insert(comments);
        //获取登录用户信息
        SysCustomer DLuser=(SysCustomer) SecurityUtils.getSubject().getPrincipal();
        //回复评论
        message.setType((byte)2);
        message.setStatus((byte)1); //未读
        message.setMessDate(c.getTime()); //评论信息
        message.setFromuserid(message.getFromuserid()); //发送人ID
        message.setCommentid(comments.getCommentId()); //评论ID
        //新增
        sysMessageDao.insert(message);
        //修改评论数
        SysArticles articles=new SysArticles();
        int bot=articlesDao.updateCount(message.getPotsid());
        if (bot==0){
            return Result.error("评论失败");
        }
        //查询未读消息数
        int messnum=sysMessageDao.selectWD(message.getPostUserId());
        //判断是否回复作者
        if (message.getTouserid() == message.getPostUserId()){
            webSocket.sendOneMessage(message.getTouserid(),String.valueOf(messnum));
        }else{
            int messnum2=sysMessageDao.selectWD(message.getTouserid());
            webSocket.sendOneMessage(message.getTouserid(),String.valueOf(messnum2));
            webSocket.sendOneMessage(message.getPostUserId(),String.valueOf(messnum));
        }
        Set<String> dt = redisTemplate.boundZSetOps(now).range(0,-1);
        //添加缓存数据
        if (dt.size()>0){
          for (String a:dt) {
            if (!a.equals(String.valueOf(message.getPotsid()))){
                //没有则添加
                redisTemplate.boundZSetOps(now).add(String.valueOf(message.getPotsid()),1);
            }else{
                //有则自增
                redisTemplate.boundZSetOps(now).incrementScore(String.valueOf(message.getPotsid()),1);
            }
          }
        }else{
            //没有则添加
            redisTemplate.boundZSetOps(now).add(String.valueOf(message.getPotsid()),1);
        }
        //查询评论
        List<SysComments> returndata=child(commentsDao.selectAll(0,3,message.getPotsid()));
        //查询总数
        Integer count=commentsDao.selectCount(message.getPotsid());

        //更新es中的文章评论数据
        //先查询
        GetRequest search=new GetRequest("boke",String.valueOf(message.getPotsid()));
        GetResponse response=restHighLevelClient.get(search, RequestOptions.DEFAULT);
        SysArticles esdata= JSON.parseObject(JSON.parse(response.getSourceAsString()).toString(),SysArticles.class);
        //更新es文章的点赞数
        esdata.setArticlesCount(count);
        UpdateRequest request=new UpdateRequest("boke",String.valueOf(message.getPotsid()));
        request.doc(JSON.toJSONString(esdata), XContentType.JSON);
        UpdateResponse updateResponse=restHighLevelClient.update(request,RequestOptions.DEFAULT);
        //修改redis中的文章评论数据
        if (redisTemplate.hasKey("post"+message.getPotsid())){
            //先判断redis是否存在该key数据
            redisTemplate.opsForValue().set("post"+message.getPotsid(),esdata);
        }

        page da=new page();
        da.setDatalist(returndata);
        da.setCount(count);
        return Result.success("评论成功",da);
    }

    //新增评论,统一回复到文章作者
    @Transactional  //事务
    @Override
    public Result insert(SysMessage message) throws ParseException, IOException {
        SysComments comments=new SysComments();
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String now=dateFormat.format(new Date());
        Calendar c=Calendar.getInstance();
        //新增评论表
        comments.setUserId(Long.valueOf(message.getFromuserid()));
        comments.setToUserId(message.getTouserid());
        comments.setArticleId(Long.valueOf(message.getPotsid()));
        comments.setLikeCount(Long.valueOf(0));
        comments.setCommentDate(c.getTime());
        comments.setCommentContent(message.getMessage());
        commentsDao.insert(comments);
        message.setType((byte)1);  //普通评论
        message.setStatus((byte)1); //未读
        message.setMessDate(c.getTime()); // 评论信息时间
        message.setCommentid(comments.getCommentId());  //评论ID.LCDLOTKLOYMK,JLB;BV;P[PP
        //新增消息通知表
        sysMessageDao.insert(message);
        //修改评论数
        int bot=articlesDao.updateCount(message.getPotsid());
        if (bot<=0){
            return Result.error("评论失败");
        }
        //查询未读消息数
        int messnum=sysMessageDao.selectWD(message.getTouserid());
        //推送消息给发布文章作者
        webSocket.sendOneMessage(message.getTouserid(),String.valueOf(messnum));
        //从redis缓存数据中获取当天的评论
        Set<String> dt = redisTemplate.boundZSetOps(now).range(0,-1);
        //添加缓存数据
        if (dt.size()>0){
            for (String a:dt) {
                //判断当天中该文章有没有评论数据
                if (!a.equals(String.valueOf(message.getPotsid()))){
                    //没有则添加
                    redisTemplate.boundZSetOps(now).add(String.valueOf(message.getPotsid()),1);
                }else{
                    //有则自增
                    redisTemplate.boundZSetOps(now).incrementScore(String.valueOf(message.getPotsid()),1);
                }
            }
        }else{
            //没有则添加
            redisTemplate.boundZSetOps(now).add(String.valueOf(message.getPotsid()),1);
            //当天评论热议设置7天过期时间
            redisTemplate.boundZSetOps(now).expire(7, TimeUnit.DAYS);
        }
        //查询评论
        List<SysComments> returndata=child(commentsDao.selectAll(0,3,message.getPotsid()));
        //查询总数
        Integer count=commentsDao.selectCount(message.getPotsid());

        //更新es中的文章评论数据
        //先查询
        GetRequest search=new GetRequest("boke",String.valueOf(message.getPotsid()));
        GetResponse response=restHighLevelClient.get(search, RequestOptions.DEFAULT);
        SysArticles esdata= JSON.parseObject(JSON.parse(response.getSourceAsString()).toString(),SysArticles.class);
        //更新es文章的点赞数
        esdata.setArticlesCount(count);
        UpdateRequest request=new UpdateRequest("boke",String.valueOf(message.getPotsid()));
        request.doc(JSON.toJSONString(esdata), XContentType.JSON);
        UpdateResponse updateResponse=restHighLevelClient.update(request,RequestOptions.DEFAULT);
        //修改redis中的文章评论数据
        if (redisTemplate.hasKey("post"+message.getPotsid())){
            //先判断redis是否存在该key数据
            redisTemplate.opsForValue().set("post"+message.getPotsid(),esdata);
        }

        page da=new page();
        da.setDatalist(returndata);
        da.setCount(count);
        return Result.success("评论成功",da);
    }

    @Override
    public List<SysComments> selectBydate(String startDate, String enddate) {
        return commentsDao.selectBydate(startDate,enddate);
    }

    //用于查询子信息
    public List<SysComments> child(List<SysComments> Comments){
        List<SysComments> listdata=new ArrayList<>();
        for (SysComments one:Comments) {
            //查询数据，看看当前评论是否有子评论
            List<SysComments> childata=commentsDao.selectChild(one.getCommentId());
            if (childata!=null && childata.size()>0){
                one.setChildList(childata);
                listdata.add(one);
            }else{
                 //父评论为0代表根评论
                 listdata.add(one);
            }
        }
        return listdata;
    }

}
