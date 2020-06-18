package com.wxapp.springboot.controller;

import com.wxapp.springboot.service.DbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@RequestMapping("/")
public class DbController {
    @Autowired
    DbService dbService = new DbService();

    @ResponseBody
    @RequestMapping("/db")
    public String dbService(String sql) {
        return dbService.handleSql(sql);
    }

    @RequestMapping("/db/initUser")
    public boolean initUserService(String openid) {
        return dbService.initUser(openid);
    }

    @RequestMapping("/db/deleteBudget")
    public boolean deletebudgetService(String openid, String budgetid) throws SQLException {
        return dbService.deleteBudget(openid, budgetid);
    }

    @RequestMapping("/db/delete1History")
    public boolean delete1History(String openid, String historyid) throws SQLException {
        return dbService.delete1History(openid, historyid, true);
    }
}
