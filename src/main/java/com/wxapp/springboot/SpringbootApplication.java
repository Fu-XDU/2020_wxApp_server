package com.wxapp.springboot;

import org.springframework.beans.factory.annotation.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

//2020年微信小程序大赛小程序作品服务端
@Component
@Configuration
@PropertySource("classpath:application.properties")
@SpringBootApplication
public class SpringbootApplication {
    public static void main(String[] args) {
        TomcatConfig TCC = new TomcatConfig();
        SpringApplication.run(SpringbootApplication.class, args);
        TCC.webServerFactory();
        Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
        logger.info("Server listening on https://127.0.0.1:8081");
    }
}
