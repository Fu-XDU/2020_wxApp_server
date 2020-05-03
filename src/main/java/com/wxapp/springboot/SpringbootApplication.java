package com.wxapp.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//2020年微信小程序大赛小程序作品服务端
@SpringBootApplication
public class SpringbootApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
        System.out.println("Start success!Server listening on https://127.0.0.1:443");
    }
}
