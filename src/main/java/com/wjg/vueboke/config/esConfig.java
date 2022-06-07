package com.wjg.vueboke.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

//连接ES配置类
@Configuration
// @AutoConfigureBefore将会在Redis类加载前加载
// 由于Redis采用的是Yaml配置的方式，没有使用配置类方式，所以采用RedisAutoConfiguration
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class esConfig {

    // @PostConstruct在@Bean之前执行
    @PostConstruct
    void init(){
        // 解决elasticsearch启动保存问题
        // 不检查netty版本，避免了与redis的netty中的版本冲突
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    //注册 rest高级客户端
    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client =new RestHighLevelClient(RestClient.builder(
                new HttpHost("192.168.200.130",9200,"http")));
        return client;
    }
}
