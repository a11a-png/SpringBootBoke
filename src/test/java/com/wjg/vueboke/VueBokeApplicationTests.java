package com.wjg.vueboke;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSON;
import com.wjg.vueboke.dao.SysArticlesDao;
import com.wjg.vueboke.dao.SysCommentsDao;
import com.wjg.vueboke.po.SysArticles;
import com.wjg.vueboke.po.SysComments;
import com.wjg.vueboke.service.ISysComments;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@Slf4j
public class VueBokeApplicationTests {

    @Autowired
    public RedisTemplate redisTemplate;
    @Resource
    public SysCommentsDao sysCommentsDao;
    @Resource
    public SysArticlesDao sysArticlesDao;
    @Autowired
    public RestHighLevelClient restHighLevelClient;


    @Test
    public void contextLoads() {
        //redisTemplate.boundZSetOps("viewcount").remove("post20");
        redisTemplate.boundZSetOps("viewcount").add("post18",49);
    }

    //初始化评论
    @Test
    public void tt(){
        //查询评论信息
        List<SysComments> cot=sysCommentsDao.select();
        SimpleDateFormat dt=new SimpleDateFormat("yyyy-MM-dd");
        Map<String,Integer> mm=new HashMap<>();
        for (SysComments ct: cot) {
            //更新
            String date=dt.format(ct.getCommentDate());
            if (redisTemplate.boundZSetOps(date).score(ct.getArticleId())!=null){
                //若存在则自增1
                redisTemplate.boundZSetOps(date).incrementScore(ct.getArticleId(),1);
            }else{
                redisTemplate.boundZSetOps(date).add(ct.getArticleId(),1);
            }
        }
    }

    //热议聚合
    @Test
    public void tt2(){
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        List<String> dt=new ArrayList<>();
        for (int i=1;i<8;i++){
            int time=i*86400000;
            Long datetime=new Date().getTime()-time;
            dt.add(dateFormat.format(datetime));
        }
        String now= dateFormat.format(new Date());
        redisTemplate.opsForZSet().unionAndStore(now,dt,"comcount");
    }

    //热议聚合
    @Test
    public void tt3(){
        SysArticles articles2=sysArticlesDao.selectByPrimaryKey(18);
        redisTemplate.boundValueOps("post18").set(articles2);
    }

    @Test
    public void task(){
        //获取缓存数据
        Set<String> strarr = redisTemplate.boundZSetOps("viewcount").range(0,-1);
        //分隔字符串
        for (String str:strarr) {
            Integer postid=Integer.valueOf(str.replace("post",""));
            //获取缓存数据
            Double viewcount=(Double)redisTemplate.boundZSetOps("viewcount").score(str);
            //查询对应的文章
            SysArticles articles=new SysArticles();
            //进行修改
            articles.setArticlesViews(viewcount.intValue());
            articles.setArticlesId(postid);
            //修改
            int bo=sysArticlesDao.update(articles);
            if (bo>0){
                log.info("文章"+postid+"同步成功");
                //删除缓存
                //redisTemplate.boundZSetOps("viewCount").remove();
            }else{
                log.info("文章"+postid+"同步失败");
            }
        }
    }

    @Test
    public void bb() throws IOException {
        SysArticles articles=sysArticlesDao.selectByPrimaryKey(18);
//        UpdateRequest request=new UpdateRequest("boke","18");
//        request.doc(JSON.toJSONString(articles), XContentType.JSON);
//        UpdateResponse response=restHighLevelClient.update(request, RequestOptions.DEFAULT);

        SysArticles articles2=sysArticlesDao.selectByPrimaryKey(21);
//        UpdateRequest request2=new UpdateRequest("boke","21");
//        request2.doc(JSON.toJSONString(articles2), XContentType.JSON);
//        UpdateResponse response2=restHighLevelClient.update(request2, RequestOptions.DEFAULT);

        SysArticles articles3=sysArticlesDao.selectByPrimaryKey(22);
        redisTemplate.opsForValue().set("post18",articles);
        redisTemplate.opsForValue().set("post21",articles2);
        redisTemplate.opsForValue().set("post22",articles3);
//        UpdateRequest request3=new UpdateRequest("boke","26");
//        request3.doc(JSON.toJSONString(articles3), XContentType.JSON);
//        UpdateResponse response3=restHighLevelClient.update(request3, RequestOptions.DEFAULT);
    }


}
