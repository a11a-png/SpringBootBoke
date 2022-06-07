package com.wjg.vueboke;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.wjg.vueboke.dao")
@EnableScheduling   //开启定时器
public class VueBokeApplication {

    public static void main(String[] args) {
        SpringApplication.run(VueBokeApplication.class, args);
    }

}
