package com.wxapp.springboot.controller;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DbControllerTest {
    DbController dbC = new DbController();

    @Test
    void dbService() {
        ArrayList<String> al = new ArrayList<>();
        al.add("select now()");
        al.add("show tables");
        for (String sql : al)
            System.out.println(dbC.dbService(sql));
    }

    @Test
    void initUserService() {
        if (dbC.initUserService("test")) {
            dbC.dbService("DROP TABLE test");
            dbC.dbService("DROP TABLE testhistory");
            System.out.println("True");
        } else {
            System.out.println("False");
        }
    }
}