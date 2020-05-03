package com.wxapp.springboot.controller;

import com.wxapp.springboot.service.ScheduledService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedule")
public class ScheduledController {
    @Autowired
    ScheduledService ss;

    @ResponseBody
    @RequestMapping("/test")
    public void scheduleTest() {
        //ss.test();
    }
}
