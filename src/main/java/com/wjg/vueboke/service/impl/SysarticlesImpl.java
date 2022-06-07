package com.wjg.vueboke.service.impl;

import com.alibaba.fastjson.JSON;
import com.wjg.vueboke.comment.Result;
import com.wjg.vueboke.comment.WebSocket;
import com.wjg.vueboke.config.higlioghtField;
import com.wjg.vueboke.dao.SysArticlesDao;
import com.wjg.vueboke.dao.SysCollectionDao;
import com.wjg.vueboke.dao.SysCommentsDao;
import com.wjg.vueboke.dao.SysMessageDao;
import com.wjg.vueboke.po.SysArticles;
import com.wjg.vueboke.po.SysComments;
import com.wjg.vueboke.po.SysCustomer;
import com.wjg.vueboke.po.SysMessage;
import com.wjg.vueboke.service.ISysarticles;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
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
public class SysarticlesImpl implements ISysarticles {

    @Resource
    public SysArticlesDao sysArticlesDao;
    @Resource
    public SysCollectionDao sysCollectionDao;
    @Resource
    public SysCommentsDao commentsDao;
    @Resource
    public SysMessageDao messageDao;
    @Autowired
    public RestHighLevelClient restHighLevelClient;
    @Autowired
    public RedisTemplate redisTemplate;
    @Autowired
    public WebSocket webSocket;

    @Override
    public List<SysArticles> selectAll(Integer page, Integer limit,Integer sort_id,Integer user_id,Integer technologyId) {
        int jump=(limit-1)*page;
        return sysArticlesDao.selectAll(page, jump,sort_id,user_id,technologyId);
    }

    @Override
    public Integer selectCount(Integer sortId,Integer user_id,Integer technology_id) {
        return sysArticlesDao.selectCount(sortId,user_id,technology_id);
    }

    @Override
    public SysArticles selectById(Integer articlesId) {
        return sysArticlesDao.selectByPrimaryKey(articlesId);
    }

    @Override
    public List<SysArticles> selectWz(Integer page, Integer limit,Integer customerId) {
        int jump=(limit-1)*page;
        return sysArticlesDao.selectWz(page,jump,customerId);
    }

    @Override
    public Integer selectCountColl(Integer customer_id) {
        return sysCollectionDao.selectCount(customer_id);
    }

    //将新发表的文章同步到ES中
    @Override
    public boolean UploadEs(Integer articlesId) throws IOException {
        //查询xingz博文
        SysArticles listdata=sysArticlesDao.selectByPrimaryKey(articlesId);
        //创建批量插入
        BulkRequest bulkRequest=new BulkRequest();
        bulkRequest.add(new IndexRequest("boke")
                            .id(""+listdata.getArticlesId()+"")
                            .source(JSON.toJSONString(listdata), XContentType.JSON));
        //响应
        BulkResponse response=restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        String str=String.valueOf(response.status());
        if (!str.equals("OK")){
            return false;
        }
        return true;
    }

    @Override
    public int update(SysArticles record) {
        return sysArticlesDao.update(record);
    }

    //ES查询
    @Override
    public List<Map<String, Object>> selectByEs(String title) throws IOException {
        SearchRequest request=new SearchRequest("boke");  //查询的索引库
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();  //开启查询条件
        //可以根据标题、内容、作者、分类查询
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(title,"articlesTitle","articlesContent"));
        //设置查询高亮
        HighlightBuilder highfield=new HighlightBuilder();
        highfield.field("articlesTitle");
        highfield.field("articlesContent");
        highfield.preTags("<em style='color:red'>");
        highfield.postTags("</em>");
        searchSourceBuilder.highlighter(highfield);
        //设置条件
        request.source(searchSourceBuilder);
        //响应查询
        SearchResponse response=restHighLevelClient.search(request,RequestOptions.DEFAULT);

        List<Map<String,Object>> data=new ArrayList<>();
        //higlioghtField封装的一个类，用来添加高亮元素
        higlioghtField highclass=new higlioghtField();
        List<String> arrstr=new ArrayList<>();
        arrstr.add("articlesTitle");
        arrstr.add("articlesContent");
        for (SearchHit hit:response.getHits().getHits()){
            data.add(highclass.field(hit,arrstr));
        }
        return data;
    }

    //新增
    @Override
    public boolean insert(SysArticles articles) {
        int t=sysArticlesDao.insert(articles);
        if (t>0){
            //使用MQ消息队列往ES中添加数据
            return true;
        }
        return false;
    }

    //点赞
    @Override
    public Result addlike(SysArticles articles, SysMessage message) throws IOException {
        int likecount=sysArticlesDao.addlike(articles);
        if (likecount<=0){
            return Result.error("出现异常错误，稍后再试");
        }
        SysArticles data=sysArticlesDao.selectlike(message.getPotsid());
        //添加信息,推送给作者
        if (articles.getAdd()==0){
            message.setMessage("点赞");
        }else {
            message.setMessage("取消点赞");
        }
        message.setMessDate(new Date());
        message.setType((byte)0);
        message.setStatus((byte)1);
        messageDao.insert(message);
        //推送消息
        //查询未读消息数
        int messnum=messageDao.selectWD(message.getTouserid());
        //推送消息给发布文章作者
        webSocket.sendOneMessage(message.getTouserid(),String.valueOf(messnum));
        //先查询
        GetRequest search=new GetRequest("boke",String.valueOf(message.getPotsid()));
        GetResponse response=restHighLevelClient.get(search,RequestOptions.DEFAULT);
        SysArticles esdata=JSON.parseObject(JSON.parse(response.getSourceAsString()).toString(),SysArticles.class);
        //更新es文章的点赞数
        esdata.setLikeCount(articles.getLikeCount());
        UpdateRequest request=new UpdateRequest("boke",String.valueOf(message.getPotsid()));
        request.doc(JSON.toJSONString(esdata),XContentType.JSON);
        UpdateResponse updateResponse=restHighLevelClient.update(request,RequestOptions.DEFAULT);
        //更新redis中文章的点赞数
        if (redisTemplate.hasKey("post"+message.getPotsid())){
            //先判断redis是否存在该key数据
            redisTemplate.opsForValue().set("post"+message.getPotsid(),esdata);
        }
        if (message.getAdd()==0){
            return Result.success("点赞＋1",data);
        }else{
            return Result.success("点赞-1",data);
        }
    }

    @Override
    public SysArticles selectlike(Integer articlesId) {
        return sysArticlesDao.selectlike(articlesId);
    }

    //收藏
    @Override
    public Result addcollect(SysArticles articles, SysMessage message) throws IOException {
        int count=sysArticlesDao.addcollect(articles);
        if (count<=0){
            return Result.error("出现异常错误，稍后再试");
        }
        SysArticles data=sysArticlesDao.selectlike(message.getPotsid());
        //添加信息,推送给作者
        if (articles.getAdd()==0){
            message.setMessage("收藏");
        }else {
            message.setMessage("取消收藏");
        }
        message.setMessDate(new Date());
        message.setType((byte)0);
        message.setStatus((byte)1);
        messageDao.insert(message);
        //推送消息
        //查询未读消息数
        int messnum=messageDao.selectWD(message.getTouserid());
        //推送消息给发布文章作者
        webSocket.sendOneMessage(message.getTouserid(),String.valueOf(messnum));
        //先查询
        GetRequest search=new GetRequest("boke",String.valueOf(message.getPotsid()));
        GetResponse response=restHighLevelClient.get(search,RequestOptions.DEFAULT);
        SysArticles esdata=JSON.parseObject(JSON.parse(response.getSourceAsString()).toString(),SysArticles.class);
        //更新es文章的收藏数
        esdata.setLikeCount(data.getCollectCount());
        UpdateRequest request=new UpdateRequest("boke",String.valueOf(message.getPotsid()));
        request.doc(JSON.toJSONString(esdata),XContentType.JSON);
        UpdateResponse updateResponse=restHighLevelClient.update(request,RequestOptions.DEFAULT);
        //更新redis中文章的收藏数
        if (redisTemplate.hasKey("post"+message.getPotsid())){
            //先判断redis是否存在该key数据
            redisTemplate.opsForValue().set("post"+message.getPotsid(),esdata);
        }
        if (message.getAdd()==0){
            return Result.success("收藏＋1",data);
        }else{
            return Result.success("收藏-1",data);
        }
    }

    @Override
    public int addcomments(Integer articlesId) {
        return sysArticlesDao.addcomments(articlesId);
    }

    @Override
    public int upviews(Integer articlesId, Integer articlesViews) {
        return sysArticlesDao.upviews(articlesId,articlesViews);
    }

    //redis博客初始化
    @Override
    public void initialization() throws ParseException {
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        List<String> dt=new ArrayList<>();
        for (int i=1;i<8;i++){
            int time=i*86400000;
            Long datetime=new Date().getTime()-time;
            dt.add(dateFormat.format(datetime));
            redisTemplate.delete(dateFormat.format(datetime));
        }
        String now= dateFormat.format(new Date());
        //1、查询7天内的评论
        List<SysComments> comments=commentsDao.selectBydate(dt.get(6),now);
        //2、对评论进行获取博文信息
        for (SysComments a:comments){
            String id=String.valueOf(a.getArticleId());
            String dy=dateFormat.format(a.getCommentDate());
            redisTemplate.delete(id);
            //判断是单天评论的热议统计
            if (!redisTemplate.hasKey(dy)){
                //若没有则添加, 并设置时长为7天
                redisTemplate.boundZSetOps(dateFormat.format(a.getCommentDate())).expire(7,TimeUnit.DAYS);
                //若没有则添加该文章7天内的评论数
                redisTemplate.boundZSetOps(dateFormat.format(a.getCommentDate())).add(id,1);
            }else{
                Set<String> setlist=redisTemplate.boundZSetOps(dy).range(0,-1);
                if (setlist.size()>0){
                    for (String b:setlist) {
                        //若有则判断是否，是否有该文章的评论
                        if (!b.equals(id)){
                            //若没有则添加该文章7天内的评论数
                            redisTemplate.boundZSetOps(dateFormat.format(a.getCommentDate())).add(id,1);
                        }else{
                            //若有则自增1
                            redisTemplate.boundZSetOps(dateFormat.format(a.getCommentDate())).incrementScore(id,1);
                        }
                    }
                }else{
                    redisTemplate.boundZSetOps(dateFormat.format(a.getCommentDate())).add(id,1);
                }
            }
        }
        //3、聚合列表
        redisTemplate.opsForZSet().unionAndStore(now,dt,"comcount");
        //获取前10的数据
        Set<String> socr=redisTemplate.boundZSetOps("comcount").reverseRange(0,9);
        for (String z:socr){
            //查询前10热议的论文信息
            SysArticles articles=sysArticlesDao.selectByPrimaryKey(Integer.valueOf(z));
            //缓存论文,并设置过期时间 7天
            redisTemplate.boundValueOps("post"+z).set(articles,7, TimeUnit.DAYS);
        }
    }

    //缓存论文
    public void redissave(String key,SysArticles articles){
         //判断是否已存在
         if (!redisTemplate.hasKey(key)){
             //若没有则添加,并设置过期时间 7天
             redisTemplate.boundValueOps(key).set(articles,7, TimeUnit.DAYS);
         }
    }

}
