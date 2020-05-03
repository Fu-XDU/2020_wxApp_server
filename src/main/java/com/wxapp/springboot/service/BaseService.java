package com.wxapp.springboot.service;

import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class BaseService {
    public String getTime(String fmt) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        return sdf.format(new Date());
    }

    public long getTime() {
        return new Date().getTime();
    }

    // 计算两个日期相隔的天数
    public int nDaysBetweenTwoDate(String firstString, String secondString) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date firstDate = null;
        Date secondDate = null;
        try {
            firstDate = df.parse(firstString);
            secondDate = df.parse(secondString);
        } catch (Exception e) {
            // 日期型字符串格式错误
            System.out.println("日期型字符串格式错误");
        }
        return (int) ((secondDate.getTime() - firstDate.getTime()) / (24 * 60 * 60 * 1000));
    }

    public String lastDay(int i) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        ca.add(Calendar.MONTH, i);
        return format.format(ca.getTime());
    }

    public int getWeek() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
        return day == 0 ? 7 : day;
    }
}