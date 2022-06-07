package com.wjg.vueboke.config;

import com.wjg.vueboke.dao.SysArticlesDao;
import com.wjg.vueboke.po.SysArticles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//定时器
@Component
@Slf4j
public class TimerConfig {

   @Autowired
   public RedisTemplate redisTemplate;
   @Resource
   public SysArticlesDao sysArticlesDao;

   //每隔一天更新redis中的热议评论
   @Scheduled(cron = "0 0 0 * * *")  //每天的0点进行更新
   public void Syncdata(){
       SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
       //7天日期集合
       List<String> dt=new ArrayList<>();
       //获取前7天的热议评论信息
       for (int i=1;i<8;i++){
           int time=i*86400000;
           Long datetime=new Date().getTime()-time;
           dt.add(dateFormat.format(datetime));
       }
       String now= dateFormat.format(new Date());
       //聚合统计
       redisTemplate.opsForZSet().unionAndStore(now,dt,"comcount");
       //将热议排行前10的文章保存到redis中
       Set<String> socr=redisTemplate.boundZSetOps("comcount").reverseRange(0,9);  //从大往小
       for (String z:socr){
           //先判断是否已保存
           if (redisTemplate.hasKey(z)){
               //存在则跳过
               continue;
           }
           //查询前10热议的论文信息
           SysArticles articles=sysArticlesDao.selectByPrimaryKey(Integer.valueOf(z));
           //缓存论文,并设置过期时间 7天
           redisTemplate.boundValueOps(z).set(articles,7, TimeUnit.DAYS);
       }
   }

    //更新数据库中文章的浏览量
    @Scheduled(cron = "0/5 * * * * *")  //每5秒进行更新
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
            //修改redis浏览量前10中的文章内容
            //获取
            SysArticles selectArtice=(SysArticles)redisTemplate.boundValueOps(str).get();
            //判断是否存在，若不存在则不在是热议，不进行修改
            if (selectArtice!=null){
                //修改浏览量
                selectArtice.setArticlesViews(viewcount.intValue());
                redisTemplate.boundValueOps(str).set(selectArtice);
            }
            if (bo>0){
                log.info("文章"+postid+"同步成功");
            }else{
                log.info("文章"+postid+"同步失败");
            }
        }
    }


    //每5秒进行，查询浏览量前十的文章缓存到redis中
    @Scheduled(cron = "0/5 * * * * *")  //每5秒进行更新
    public void task2(){
        //获取缓存浏览量前10的文章
        Set<String> strarr = redisTemplate.boundZSetOps("viewcount").range(0,9);
        //分隔字符串
        for (String str:strarr) {
            //查询排名前十的数据
            //先查看redis是否有该文章数据
            if (redisTemplate.opsForValue().get(str)!=null){
               //跳过
                continue;
            }
            Integer postid=Integer.valueOf(str.replace("post",""));
            SysArticles articles=sysArticlesDao.selectByPrimaryKey(postid);
            //保存在redis中,时长为7天
            redisTemplate.boundValueOps("post"+postid).set(articles,7,TimeUnit.DAYS);
        }
    }
}
