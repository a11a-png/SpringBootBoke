package com.wjg.vueboke.config;

import com.wjg.vueboke.po.SysArticles;
import com.wjg.vueboke.service.ISysarticles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

//同步数据
@Component
@Slf4j
public class SyncTimerConfig {

    @Autowired
    public RedisTemplate redisTemplate;
    @Autowired
    public ISysarticles sysarticles;

//    @Scheduled(cron = "0/5 * * * * *")
//    public void task(){
//        //获取缓存数据
//        Set<String> strarr = redisTemplate.boundZSetOps("viewCount").range(0,-1);
//        //分隔字符串
//        for (String str:strarr) {
//            Integer postid=Integer.valueOf(str.replace("post",""));
//            //获取缓存数据
//            Double viewcount=redisTemplate.boundZSetOps("viewCount").score("post"+postid);
//            //查询对应的文章
//            SysArticles articles=new SysArticles();
//            //进行修改
//            articles.setArticlesViews(viewcount.intValue());
//            articles.setArticlesId(postid);
//            //修改
//            int bo=sysarticles.update(articles);
//            if (bo>0){
//                log.info("文章"+postid+"同步成功");
//                //删除缓存
//                redisTemplate.boundZSetOps("viewCount").remove("post"+postid);
//            }else{
//                log.info("文章"+postid+"同步失败");
//            }
//        }
//    }
}
