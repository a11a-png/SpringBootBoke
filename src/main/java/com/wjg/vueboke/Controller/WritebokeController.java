package com.wjg.vueboke.Controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.wjg.vueboke.comment.Result;
import com.wjg.vueboke.comment.WebSocket;
import com.wjg.vueboke.comment.page;
import com.wjg.vueboke.po.SysArticles;
import com.wjg.vueboke.po.SysComments;
import com.wjg.vueboke.po.SysCustomer;
import com.wjg.vueboke.po.SysMessage;
import com.wjg.vueboke.service.*;
import org.apache.lucene.spatial.prefix.tree.S2PrefixTree;
import org.apache.shiro.SecurityUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;

@RestController
public class WritebokeController {
     @Autowired
     public IShort shortImpl;
     @Autowired
     public ISysarticles sysarticles;
     //mq发送消息类
     @Autowired
     public RabbitTemplate rabbitTemplate;
     @Autowired
     public RedisTemplate redisTemplate;
     @Autowired
     public ISysComments sysComments;
     @Autowired
     public RestHighLevelClient restHighLevelClient;
     @Autowired
     public ISysCustomer sysCustomer;
     @Autowired
     public WebSocket webSocket;
     @Autowired
     public ISysmessage sysmessage;

     //首页文章初始化,查询本周热议前10的文章
     @GetMapping("/hotArticle")
     public Result hotArticle() throws IOException {
        List<SysArticles> articlesList=new ArrayList<>();
         //获取zset聚合值排名
        Set<Integer> article=redisTemplate.boundZSetOps("comcount").reverseRange(0,9);
        if (article.size()>0){
            for (Integer str:article) {
                //从redis中获取
                SysArticles sele = (SysArticles)redisTemplate.opsForValue().get("post"+str);
                articlesList.add(sele);
            }
        }
        //判断是否有热议文章
        if (article.size()<=0 || article.size()!=10){
           //默认查询热议前10，若不够10条则在访问量查询填够10条
            int rysize=10-article.size();
           //若没有则获取访问量前10的
           Set<String> article2 = redisTemplate.boundZSetOps("viewcount").reverseRange(0,rysize);
           //从数据库中查询
           if (article2.size()>0){
               for (String str:article2) {
                   //如果查询到有热议文章，那么就进行判断不要查询相同的文章
                   if (article.size()>0){
                       for (Integer str2:article) {
                           Integer postid=Integer.valueOf(str.replace("post",""));
                           //同一篇跳过
                           if (postid==str2){
                               continue;
                           }
                           //从redis中获取数据
                           SysArticles sele=(SysArticles)redisTemplate.boundValueOps(str).get();
                           articlesList.add(sele);
                       }
                   }else{
                       //从redis中获取数据
                       SysArticles sele=(SysArticles)redisTemplate.boundValueOps(str).get();
                       articlesList.add(sele);
                   }
               }
           }
        }
        //返回热议前10
        return Result.success(articlesList);
     }

     //跳转写作页面
     @GetMapping("/writeboke")
     public Result writeboke(){
        return Result.success().action("/Writeboke");
     }

     //发表文章
     @PostMapping("/saveboke")
     public Result saveboke(@RequestBody SysArticles articles){
           if (articles.getSortId()==null){
               return Result.error("请选择文章分类");
           }
          if (articles.getArticlesTitle() == null || articles.getArticlesTitle() == "") {
               return Result.error("请填写文章标题");
          }
          if (articles.getArticlesContent() == null || articles.getArticlesContent() == "") {
               return Result.error("请填写文章内容");
          }
          articles.setArticlesViews(0); //浏览
          articles.setArticlesCount(0); //评论
          articles.setArticlesDate(new Date());
          articles.setLikeCount(0); //点赞
          articles.setLikeUserId(""); //点赞用户ID
          articles.setCollectUserId(""); //收藏用户ID
          //新增
          boolean bt=sysarticles.insert(articles);
          if (!bt){
             return Result.error("发表失败");
          }
          //使用MQ发送消息
          CorrelationData correlationData=new CorrelationData("1");
          rabbitTemplate.convertAndSend("eschange","eskey",""+articles.getArticlesId()+"",correlationData);
          rabbitTemplate.convertAndSend("dyExchange","dyId",articles,correlationData);
          return Result.success("发表成功",null).action("/");
     }

     //上传图片
     @RequestMapping("/uploadfile")
     public JSONObject uploadfile(@RequestParam("token") String token, MultipartFile file) throws IOException {
          //创建保存文件名
          //user.dir=F:\xianmu\VueBoke
          String savefile=System.getProperty("user.dir")+"/file/"+file.getOriginalFilename();
          //判断目录是否存在
          File one=new File(savefile);
          if (!one.getParentFile().exists()){
               //没有则创建
               one.getParentFile().mkdirs();
          }
          //读取文件字节流写入到磁盘中
          FileUtil.writeBytes(file.getBytes(),savefile);
          //跳转到下载页面方法
          String downurl="http://localhost:8801/downfile/"+file.getOriginalFilename();
          //返回富文本插件接收的json格式
          JSONObject json=new JSONObject();
          json.set("errno",0);
          JSONArray arr=new JSONArray();
          JSONObject data=new JSONObject();
          arr.add(data);
          data.set("url",downurl);
          json.set("data",arr);
          return json;
     }

     //上传后，进行下载返回给文本编辑器
     @RequestMapping("/downfile/{imgName}")
     public void downfile(@PathVariable("imgName") String imgName, HttpServletResponse response){
          OutputStream os;  //创建一个输出流对象
          String basePath=System.getProperty("user.dir")+"/file/"+imgName;
          if (StrUtil.isNotEmpty(basePath)){
             //设置响应头
             try {
                  response.addHeader("Content-Disposition","attachment;filename=" + URLEncoder.encode(imgName, "UTF-8"));
                  response.setContentType("application/octet-stream");
                  //将文件转换为字节流
                  byte[] bytes=FileUtil.readBytes(basePath);
                  //创建输出流对象
                  os=response.getOutputStream();
                  //将文件输出
                  os.write(bytes);
                  os.flush();
                  os.close();
             } catch (UnsupportedEncodingException e) {
                  e.printStackTrace();
             } catch (IOException e) {
                  e.printStackTrace();
             }
          }
     }

     //查看文章
     @GetMapping("/lookboke")
     public Result lookboke(@RequestParam("articlesId") Integer articlesId) throws IOException {
         List<Object> data=new ArrayList<>();
         //查詢文章
         SysArticles articles=sysarticles.selectById(articlesId);
         if (articles==null){
            return Result.error("文章不存在或已刪除");
         }
         Double viewCount = redisTemplate.boundZSetOps("viewcount").score("post"+articlesId);
         //设置redis浏览量+1
         if (viewCount==null || viewCount==0){
             //判断是否存在，若不存在则 articles.getArticlesViews（自身）+1
             articles.setArticlesViews(articles.getArticlesViews()+1);

         }else{
             //判断是否存在，若存在则：viewCount+1
             articles.setArticlesViews(viewCount.intValue()+1);
         }
         redisTemplate.boundZSetOps("viewcount").add("post"+articlesId,articles.getArticlesViews());
         //更新es中的数据
         //先查询
         GetRequest search=new GetRequest("boke",String.valueOf(articlesId));
         GetResponse response=restHighLevelClient.get(search, RequestOptions.DEFAULT);
         SysArticles esdata= com.alibaba.fastjson.JSON.parseObject(com.alibaba.fastjson.JSON.parse(response.getSourceAsString()).toString(),SysArticles.class);
         //更新es文章的浏览量
         esdata.setLikeCount(articles.getArticlesViews());
         UpdateRequest request=new UpdateRequest("boke",String.valueOf(articlesId));
         request.doc(com.alibaba.fastjson.JSON.toJSONString(esdata), XContentType.JSON);
         UpdateResponse updateResponse=restHighLevelClient.update(request,RequestOptions.DEFAULT);
         data.add(articles);
         //查詢評論
         List<SysComments> comments=sysComments.selectAll(1,3,articlesId);
         //查询总数
         Integer count=sysComments.selectCount(articlesId);
         page dt=new page();
         dt.setDatalist(comments);
         dt.setCount(count);
         data.add(dt);
         return Result.success(data).action("/article");
     }

    /**
     * 点赞
     * @param message
     * @return
     */
     @PostMapping("/addlike")
     public Result addlike(@RequestBody SysMessage message) throws IOException {
          SysCustomer customer=(SysCustomer) SecurityUtils.getSubject().getPrincipal();
          String str="_"+customer.getUserid();
          SysArticles articles=new SysArticles();
          articles.setArticlesId(message.getPotsid());
          articles.setAdd(message.getAdd().byteValue());
          articles.setLikeUserId(str);
          //直接更改数据库值
          return sysarticles.addlike(articles,message);
     }

    //收藏
    @PostMapping("/addcollect")
    public Result addcollect(@RequestBody SysMessage message) throws IOException {
        SysCustomer customer=(SysCustomer) SecurityUtils.getSubject().getPrincipal();
        String str="_"+customer.getUserid();
        SysArticles articles=new SysArticles();
        articles.setArticlesId(message.getPotsid());
        articles.setAdd(message.getAdd().byteValue());
        articles.setCollectUserId(str);
        //直接更改数据库值
        return sysarticles.addcollect(articles,message);
    }

    //发布评论
    @PostMapping("/tocomments")
    public Result tocomments(@RequestBody SysMessage message) throws ParseException, IOException {
       if (message.getMessage()==null || message.getMessage()==""){
           return Result.error("请填写评论");
       }
       return sysComments.insert(message);
    }

    //回复评论
    @PostMapping("/replycomments")
    public Result replycomments(@RequestBody SysMessage message) throws ParseException, IOException {
        if (message.getMessage()==null || message.getMessage()==""){
            return Result.error("请填写评论");
        }
        return sysComments.insertTwo(message);
    }

    //查询评论
    @GetMapping("/selectAllComments")
    public Result selectAllComments(@RequestParam("pageSize")Integer pageSize,
                                    @RequestParam("currpage")Integer currpage,
                                    @RequestParam("articlesId")Integer articlesId){
        //查询文章评论
        List<SysComments> comments=sysComments.selectAll(currpage,3,articlesId);
        //查询总数
        Integer count=sysComments.selectCount(articlesId);
        page dt=new page();
        dt.setDatalist(comments);
        dt.setCount(count);
        return Result.success(null,dt);
    }

    //Elastic查询文章
    @GetMapping("/selectpost")
    public Result selectpost(@RequestParam("content") String content) throws IOException {
        List<Map<String, Object>> data=sysarticles.selectByEs(content);
        return Result.success(null,data).action("/selectpost");
    }

    @GetMapping("/selecttechologty")
    public Result selecttechologty(@RequestParam("technologyid")Integer technologyid,@RequestParam(value = "curr",required = false,defaultValue = "1") Integer curr){
        //根据专业知识查询对应的文章
        List<SysArticles> articles=sysarticles.selectAll(3,curr,null,null,technologyid);
        Integer count=sysarticles.selectCount(null,null,technologyid);
        List<Object> data=new ArrayList<>();
        data.add(articles);
        data.add(count);
        return Result.success(null,data).action("/technology");
    }

    //关注作者
    @PostMapping("/selectlikeauthor")
    public Result selectlikeauthor(@RequestBody SysMessage message){
         SysCustomer customer=new SysCustomer();
         customer.setFocusUserId("_"+message.getTouserid());
         customer.setUserid(message.getFromuserid());
         customer.setAdd((byte)0);
         //修改關注的人
         Integer count=sysCustomer.update(customer);
         if (count>0){
             //被關注的人
             SysCustomer customer2=new SysCustomer();
             customer2.setFocusMyuserId("_"+message.getFromuserid());
             customer2.setUserid(message.getTouserid());
             customer2.setAdd((byte)0);
             Integer count2=sysCustomer.update(customer2);
             if (count2<=0){
                 return Result.error("關注失敗");
             }
             //新增消息推送
             message.setStatus((byte)1);
             message.setType((byte)0);
             message.setMessage("关注了你");
             message.setMessDate(new Date());
             int ct = sysmessage.insert(message);
             if (ct>0){
                 //推送消息
                 int messnum=sysmessage.selectWD(message.getTouserid());
                 webSocket.sendOneMessage(message.getTouserid(),String.valueOf(messnum));
                 return Result.success("成功关注","_"+message.getTouserid());
             }else{
                 return Result.error("关注失败");
             }
         }
         return Result.error("关注失败");
    }

    //取关作者
    @GetMapping("/closeauthor")
    public Result closeauthor(@RequestParam("userid")Integer userid,@RequestParam("touserid") Integer touserid){
        SysCustomer customer=new SysCustomer();
        customer.setFocusUserId("_"+touserid);
        customer.setUserid(userid);
        customer.setAdd((byte)-1);
        Integer count=sysCustomer.update(customer);
        if (count>0){
            //被關注的人
            SysCustomer customer2=new SysCustomer();
            customer2.setFocusMyuserId("_"+userid);
            customer2.setUserid(touserid);
            customer2.setAdd((byte)-1);
            Integer count2=sysCustomer.update(customer2);
            if (count2<=0){
                return Result.error("關注失敗");
            }
            return Result.success("取関成功");
        }
        return Result.error("取関失敗");
    }
}