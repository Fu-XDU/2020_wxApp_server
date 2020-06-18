package com.wxapp.springboot.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseControllerTest {
    BaseController bc = new BaseController();

    @Test
    void ping() {
        System.out.println(bc.ping());
    }

    @Test
    void time() {
        System.out.println(bc.time("yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    void timestamp() {
        System.out.println(bc.time());
    }
}