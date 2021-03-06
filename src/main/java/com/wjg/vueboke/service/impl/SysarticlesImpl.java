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

    //??????????????????????????????ES???
    @Override
    public boolean UploadEs(Integer articlesId) throws IOException {
        //??????xingz??????
        SysArticles listdata=sysArticlesDao.selectByPrimaryKey(articlesId);
        //??????????????????
        BulkRequest bulkRequest=new BulkRequest();
        bulkRequest.add(new IndexRequest("boke")
                            .id(""+listdata.getArticlesId()+"")
                            .source(JSON.toJSONString(listdata), XContentType.JSON));
        //??????
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

    //ES??????
    @Override
    public List<Map<String, Object>> selectByEs(String title) throws IOException {
        SearchRequest request=new SearchRequest("boke");  //??????????????????
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();  //??????????????????
        //???????????????????????????????????????????????????
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(title,"articlesTitle","articlesContent"));
        //??????????????????
        HighlightBuilder highfield=new HighlightBuilder();
        highfield.field("articlesTitle");
        highfield.field("articlesContent");
        highfield.preTags("<em style='color:red'>");
        highfield.postTags("</em>");
        searchSourceBuilder.highlighter(highfield);
        //????????????
        request.source(searchSourceBuilder);
        //????????????
        SearchResponse response=restHighLevelClient.search(request,RequestOptions.DEFAULT);

        List<Map<String,Object>> data=new ArrayList<>();
        //higlioghtField?????????????????????????????????????????????
        higlioghtField highclass=new higlioghtField();
        List<String> arrstr=new ArrayList<>();
        arrstr.add("articlesTitle");
        arrstr.add("articlesContent");
        for (SearchHit hit:response.getHits().getHits()){
            data.add(highclass.field(hit,arrstr));
        }
        return data;
    }

    //??????
    @Override
    public boolean insert(SysArticles articles) {
        int t=sysArticlesDao.insert(articles);
        if (t>0){
            //??????MQ???????????????ES???????????????
            return true;
        }
        return false;
    }

    //??????
    @Override
    public Result addlike(SysArticles articles, SysMessage message) throws IOException {
        int likecount=sysArticlesDao.addlike(articles);
        if (likecount<=0){
            return Result.error("?????????????????????????????????");
        }
        SysArticles data=sysArticlesDao.selectlike(message.getPotsid());
        //????????????,???????????????
        if (articles.getAdd()==0){
            message.setMessage("??????");
        }else {
            message.setMessage("????????????");
        }
        message.setMessDate(new Date());
        message.setType((byte)0);
        message.setStatus((byte)1);
        messageDao.insert(message);
        //????????????
        //?????????????????????
        int messnum=messageDao.selectWD(message.getTouserid());
        //?????????????????????????????????
        webSocket.sendOneMessage(message.getTouserid(),String.valueOf(messnum));
        //?????????
        GetRequest search=new GetRequest("boke",String.valueOf(message.getPotsid()));
        GetResponse response=restHighLevelClient.get(search,RequestOptions.DEFAULT);
        SysArticles esdata=JSON.parseObject(JSON.parse(response.getSourceAsString()).toString(),SysArticles.class);
        //??????es??????????????????
        esdata.setLikeCount(articles.getLikeCount());
        UpdateRequest request=new UpdateRequest("boke",String.valueOf(message.getPotsid()));
        request.doc(JSON.toJSONString(esdata),XContentType.JSON);
        UpdateResponse updateResponse=restHighLevelClient.update(request,RequestOptions.DEFAULT);
        //??????redis?????????????????????
        if (redisTemplate.hasKey("post"+message.getPotsid())){
            //?????????redis???????????????key??????
            redisTemplate.opsForValue().set("post"+message.getPotsid(),esdata);
        }
        if (message.getAdd()==0){
            return Result.success("?????????1",data);
        }else{
            return Result.success("??????-1",data);
        }
    }

    @Override
    public SysArticles selectlike(Integer articlesId) {
        return sysArticlesDao.selectlike(articlesId);
    }

    //??????
    @Override
    public Result addcollect(SysArticles articles, SysMessage message) throws IOException {
        int count=sysArticlesDao.addcollect(articles);
        if (count<=0){
            return Result.error("?????????????????????????????????");
        }
        SysArticles data=sysArticlesDao.selectlike(message.getPotsid());
        //????????????,???????????????
        if (articles.getAdd()==0){
            message.setMessage("??????");
        }else {
            message.setMessage("????????????");
        }
        message.setMessDate(new Date());
        message.setType((byte)0);
        message.setStatus((byte)1);
        messageDao.insert(message);
        //????????????
        //?????????????????????
        int messnum=messageDao.selectWD(message.getTouserid());
        //?????????????????????????????????
        webSocket.sendOneMessage(message.getTouserid(),String.valueOf(messnum));
        //?????????
        GetRequest search=new GetRequest("boke",String.valueOf(message.getPotsid()));
        GetResponse response=restHighLevelClient.get(search,RequestOptions.DEFAULT);
        SysArticles esdata=JSON.parseObject(JSON.parse(response.getSourceAsString()).toString(),SysArticles.class);
        //??????es??????????????????
        esdata.setLikeCount(data.getCollectCount());
        UpdateRequest request=new UpdateRequest("boke",String.valueOf(message.getPotsid()));
        request.doc(JSON.toJSONString(esdata),XContentType.JSON);
        UpdateResponse updateResponse=restHighLevelClient.update(request,RequestOptions.DEFAULT);
        //??????redis?????????????????????
        if (redisTemplate.hasKey("post"+message.getPotsid())){
            //?????????redis???????????????key??????
            redisTemplate.opsForValue().set("post"+message.getPotsid(),esdata);
        }
        if (message.getAdd()==0){
            return Result.success("?????????1",data);
        }else{
            return Result.success("??????-1",data);
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

    //redis???????????????
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
        //1?????????7???????????????
        List<SysComments> comments=commentsDao.selectBydate(dt.get(6),now);
        //2????????????????????????????????????
        for (SysComments a:comments){
            String id=String.valueOf(a.getArticleId());
            String dy=dateFormat.format(a.getCommentDate());
            redisTemplate.delete(id);
            //????????????????????????????????????
            if (!redisTemplate.hasKey(dy)){
                //??????????????????, ??????????????????7???
                redisTemplate.boundZSetOps(dateFormat.format(a.getCommentDate())).expire(7,TimeUnit.DAYS);
                //???????????????????????????7??????????????????
                redisTemplate.boundZSetOps(dateFormat.format(a.getCommentDate())).add(id,1);
            }else{
                Set<String> setlist=redisTemplate.boundZSetOps(dy).range(0,-1);
                if (setlist.size()>0){
                    for (String b:setlist) {
                        //???????????????????????????????????????????????????
                        if (!b.equals(id)){
                            //???????????????????????????7??????????????????
                            redisTemplate.boundZSetOps(dateFormat.format(a.getCommentDate())).add(id,1);
                        }else{
                            //???????????????1
                            redisTemplate.boundZSetOps(dateFormat.format(a.getCommentDate())).incrementScore(id,1);
                        }
                    }
                }else{
                    redisTemplate.boundZSetOps(dateFormat.format(a.getCommentDate())).add(id,1);
                }
            }
        }
        //3???????????????
        redisTemplate.opsForZSet().unionAndStore(now,dt,"comcount");
        //?????????10?????????
        Set<String> socr=redisTemplate.boundZSetOps("comcount").reverseRange(0,9);
        for (String z:socr){
            //?????????10?????????????????????
            SysArticles articles=sysArticlesDao.selectByPrimaryKey(Integer.valueOf(z));
            //????????????,????????????????????? 7???
            redisTemplate.boundValueOps("post"+z).set(articles,7, TimeUnit.DAYS);
        }
    }

    //????????????
    public void redissave(String key,SysArticles articles){
         //?????????????????????
         if (!redisTemplate.hasKey(key)){
             //??????????????????,????????????????????? 7???
             redisTemplate.boundValueOps(key).set(articles,7, TimeUnit.DAYS);
         }
    }

}
