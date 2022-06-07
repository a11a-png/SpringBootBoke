package com.wjg.vueboke.service;

import com.wjg.vueboke.comment.Result;
import com.wjg.vueboke.po.SysArticles;
import com.wjg.vueboke.po.SysMessage;
import org.apache.ibatis.annotations.Param;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface ISysarticles {
    List<SysArticles> selectAll(Integer page,Integer limit,Integer sort_id,Integer user_id,Integer technologyId);

    Integer selectCount(Integer sortId,Integer user_id,Integer technology_id);

    SysArticles selectById(Integer articlesId);

    List<SysArticles> selectWz(Integer page, Integer limit,Integer customerId);

    Integer selectCountColl(Integer customer_id);

    boolean UploadEs(Integer articlesId) throws IOException;

    int update(SysArticles record);

    //通过ES模糊搜索
//    List<Map<String,Object>> selectByEs(String title) throws IOException;

    //新增
    boolean insert(SysArticles articles);

    //redis博客初始化
//    void initialization() throws ParseException;

    Result addlike(SysArticles articles, SysMessage message) throws IOException;

    SysArticles selectlike(Integer articlesId);

    Result addcollect(SysArticles articles, SysMessage message) throws IOException;

    int addcomments(@Param("articlesId") Integer articlesId);

    int upviews(@Param("articlesId") Integer articlesId,@Param("articlesViews") Integer articlesViews);

    List<Map<String, Object>> selectByEs(String title) throws IOException;

    void initialization() throws ParseException;
}
