package com.wxapp.springboot.controller;

import com.wxapp.springboot.service.BaseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
//@Slf4j
public class BaseController {

    BaseService base = new BaseService();

    @ResponseBody
    @RequestMapping("/ping")
    public String ping() {
        return "Ping!";
    }

    @ResponseBody
    @RequestMapping("/time")
    public String time(String fmt) {
        return base.getTime(fmt);
    }

    @ResponseBody
    @RequestMapping("/timestamp")
    public long time() {
        return base.getTime();
    }
}
