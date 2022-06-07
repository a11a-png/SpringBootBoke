package com.wjg.vueboke.MQ;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//队列
@Configuration
public class MqConfig {

    //创建队列
    @Bean
    public Queue esQueue(){
        return QueueBuilder.durable("esQueue").build();
    }

    //创建直接交换机
    @Bean
    public DirectExchange esexchange(){
        return new DirectExchange("eschange");
    }

    //进行绑定
    @Bean
    public Binding bind(@Qualifier("esQueue") Queue esQueue,
                        @Qualifier("esexchange") DirectExchange eschange){
        return BindingBuilder.bind(esQueue).to(eschange).with("eskey");
    }

    //創建发布订阅队列
    @Bean
    public Queue dyQueue(){
        return QueueBuilder.durable("dyQueue").build();
    }

    @Bean
    public DirectExchange dyExchange(){
        return new DirectExchange("dyExchange");
    }

    @Bean
    public Binding bind2(@Qualifier("dyQueue") Queue dyQueue,
                         @Qualifier("dyExchange") DirectExchange dyExchange){
        return BindingBuilder.bind(dyQueue).to(dyExchange).with("dyId");
    }
}
