package com.wjg.vueboke.dao;

import com.wjg.vueboke.po.SysArticles;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysArticlesDao {
    int deleteByPrimaryKey(Integer articlesId);

    int insert(SysArticles record);

    SysArticles selectByPrimaryKey(@Param("articlesId") Integer articlesId);

    int update(SysArticles record);

    List<SysArticles> selectAll(@Param("page")Integer page, @Param("lit")Integer limit,@Param("sort_id")Integer sort_id,@Param("user_id") Integer user_id,@Param("technologyId")Integer technologyId);

    Integer selectCount(@Param("sortId") Integer sortId,@Param("user_id") Integer user_id,@Param("technology_id")Integer technology_id);

    List<SysArticles> selectWz(@Param("page")Integer page, @Param("lit")Integer limit,@Param("customerId") Integer customerId);

    int updateCount(@Param("articlesId") Integer articlesId);

    int addlike(SysArticles articles);

    SysArticles selectlike(@Param("articlesId") Integer articlesId);

    int addcollect(SysArticles articles);

    int addcomments(@Param("articlesId") Integer articlesId);

    int upviews(@Param("articlesId") Integer articlesId,@Param("articlesViews") Integer articlesViews);

    SysArticles selectuserCollect(@Param("articlesId") Integer articlesId);
}