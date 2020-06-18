package com.wxapp.springboot.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class ScheduledService {
    DbService dbService = new DbService();

    @Scheduled(cron = "0 0 0 * * ?")//每日触发一次
    public void updateTables() throws Exception {
        dbService.updateTables();
    }
}
