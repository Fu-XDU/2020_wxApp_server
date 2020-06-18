package com.wxapp.springboot.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.stringtree.json.JSONValidatingWriter;

@Service
public class DbService {
    public static Connection conn;
    public static PreparedStatement stmt;
    private final static String ip = "127.0.0.1";
    private final static String port = "3306";
    private final static String driver = "com.mysql.cj.jdbc.Driver";
    public static String dbname = "wxapp";
    public static String user = "wxapp";
    public static String password = "12345678";

    public String handleSql(String sql) {
        if (!connect())
            return "数据库连接失败！";
        if (sql.split(" ")[0].toUpperCase().equals("SELECT") || sql.split(" ")[0].toUpperCase().equals("SHOW"))
            return select(sql);
        else return runMysql(sql);
    }

    private boolean connect() {
        String url = "jdbc:mysql://" + ip + ":" + port + "/" + dbname + "?useUnicode=true&characterEncoding=utf8";
        try {
            if (conn == null || conn.isClosed()) {
                //加载驱动程序
                Class.forName(driver);
                conn = DriverManager.getConnection(url, user, password);
            }
            if (!conn.isClosed()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private String select(String sql) {
        try {
            connect();
            return new JSONValidatingWriter().write(
                    new QueryRunner().query(conn, sql, new MapListHandler())
            );
        } catch (SQLException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String text = sw.toString();
            return text.substring(0, text.indexOf("at"));
        }
    }

    private String runMysql(@NotNull String sql) {
        int add = 0, ret;
        String iosql = null;
        if (sql.endsWith("_Add")) {
            sql = sql.substring(0, sql.length() - 4);
            add = 1;
        } else if (sql.endsWith("_AddIncome") || sql.endsWith("_AddExpenditure") || sql.endsWith("_AddTransaction")) {
            String n1 = (sql.substring(sql.indexOf("(") + 1)).substring(sql.substring(sql.indexOf("(") + 1).indexOf("n"));
            String n = sql.substring(0, 47) + "(" + n1;
            add = 1;
            if (sql.endsWith("_AddIncome")) {
                iosql = sql;
                sql = n;
                sql = sql.substring(0, sql.length() - 10);
                add = 2;
            } else if (sql.endsWith("_AddExpenditure")) {
                iosql = sql;
                sql = n;
                sql = sql.substring(0, sql.length() - 15);
                add = 3;
            } else if (sql.endsWith("_AddTransaction")) {
                iosql = sql;
                sql = sql.substring(0, 40) + "history(" + n1;
                sql = sql.substring(0, sql.length() - 15);
                add = 4;
            }
        }
        try {
            connect();
            stmt = conn.prepareStatement(sql);
            ret = stmt.executeUpdate();
            stmt.close();
            if (add == 1)
                updateTables();
            else if (add > 1) {
                String n = (iosql.substring(iosql.indexOf("(") + 1)).substring(0, iosql.substring(iosql.indexOf("(") + 1).indexOf("n"));
                if (add == 2) {
                    addIncome(iosql.substring(12, 40), iosql.substring(47, iosql.indexOf("(")), n);
                } else if (add == 3) {
                    addExpenditure(iosql.substring(12, 40), iosql.substring(47, iosql.indexOf("(")), n);
                } else if (add == 4) {
                    addExpenditure(iosql.substring(12, 40), iosql.substring(40, iosql.indexOf("history")), "-" + n);
                    addIncome(iosql.substring(12, 40), iosql.substring(iosql.indexOf("history") + 7, iosql.indexOf("(")), n);
                }
            }
            return ret + "";
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        }
    }

    private @NotNull List<String> searchTableNamesbyLen(String dbname, int len) throws Exception {
        connect();
        String sql = "select table_name from information_schema.tables where table_schema='" + dbname + "'";
        List<String> list = new ArrayList<>();
        ResultSet result = selectReturnSet(sql);
        while (result.next()) {
            String tbname = result.getString("TABLE_NAME");
            if (tbname.length() == len)
                list.add(tbname);
        }
        stmt.close();
        return list;
    }

    public void updateTables() throws Exception {
        updateRecording();
    }

    private void updateRecording() throws Exception {
        List<String> tbnamelist = searchTableNamesbyLen("wxapp", 28);//openid的长度为28
        Iterator<String> iter = tbnamelist.iterator();
        BaseService base = new BaseService();
        int day = Integer.parseInt(base.getTime("dd")), remaindays = 0, datatype, beginTime, diff, id;
        double balance = 0, todayleft;
        boolean balanceupdate = false;
        String sql, tbname, time, begindate, enddate, nexttime;
        while (iter.hasNext()) {
            tbname = iter.next();
            sql = "select * from " + tbname;
            ResultSet result = selectReturnSet(sql);
            while (result.next()) {
                //计算并存储remaindays
                if (true) {
                    datatype = result.getInt("dataType");
                    if (datatype == 0) {
                        //每月
                        beginTime = Integer.parseInt(result.getString("beginTime"));
                        time = base.getTime("yyyy-MM-dd");
                        nexttime = time.substring(0, 5) + (Integer.parseInt(time.substring(5, 7)) + 1 + "") + "-" + beginTime;
                        if (day >= beginTime) {
                            remaindays = base.nDaysBetweenTwoDate(base.getTime("yyyy-MM-dd"), nexttime);
                        } else {
                            remaindays = beginTime == 32 ? base.nDaysBetweenTwoDate(time, base.lastDay(0)) : base.nDaysBetweenTwoDate(time, base.lastDay(0).substring(0, 8) + beginTime);
                            if (remaindays == 0)
                                remaindays = beginTime == 32 ? base.nDaysBetweenTwoDate(time, base.lastDay(1)) : base.nDaysBetweenTwoDate(time, nexttime);
                            else if (remaindays < 0)
                                continue;
                        }
                    } else if (datatype == 1) {
                        diff = base.getWeek() - Integer.parseInt(result.getString("beginTime"));
                        remaindays = diff + (diff > 0 ? 0 : 7);
                    } else if (datatype == 2) {
                        begindate = result.getString("beginTime");
                        enddate = result.getString("endTime");
                        remaindays = base.nDaysBetweenTwoDate(begindate, enddate);
                        remaindays = remaindays == 0 ? 1 : Math.max(remaindays, 0);
                    }
                    id = result.getInt("id");
                    //将remaindays存表
                    if (result.getString("remaindays") == null || Integer.parseInt(result.getString("remaindays")) != remaindays) {
                        runMysql("update " + tbname + " set remaindays='" + remaindays + "' where id=" + id + "");
                    }
                }
                //周期初触发 更新balance为total或total+上期balance，更新totalpay为0
                if (result.getString("remaindays") == null) {
                    runMysql("update " + tbname + " set totalpay=0 where id=" + id + "");
                } else if (remaindays - Integer.parseInt(result.getString("remaindays")) > 2) {
                    balance = result.getDouble("total") + result.getInt("rollover") == 1 ? result.getDouble("balance") : 0;
                    runMysql("update " + tbname + " set balance=" + balance + ",totalpay=0 where id=" + id + "");
                    balanceupdate = true;
                }
                //获取到balance和remaindays后，计算出todayleft并置todaypay为0
                if (!balanceupdate) balance = result.getDouble("balance");
                todayleft = (balance / (remaindays == 0 ? 1 : remaindays)) * (remaindays == 0 ? 0 : 1);
                runMysql("update " + tbname + " set todayleft=" + todayleft + ",todaypay=0 where id=" + id + "");
            }
            stmt.close();
        }
    }

    private void addIncome(String tbname, String id, String income) throws SQLException {
        String sql = "select * from " + tbname + " where id=" + id + "";
        ResultSet result = selectReturnSet(sql);
        double balance = 0, todayleft = 0;
        if (result.next()) {
            balance = result.getDouble("balance") + Double.parseDouble(income);
            todayleft = result.getDouble("todayleft") + Double.parseDouble(income) / result.getDouble("remaindays");
        }
        runMysql("update " + tbname + " set balance=" + balance + ",todayleft=" + todayleft + " where id=" + id + "");
        stmt.close();
    }

    /*
     * 添加支出
     * @para {expenditure} 若为正值则表示删除支出
     */
    private void addExpenditure(String tbname, String id, String expenditure) throws SQLException {
        String sql = "select * from " + tbname + " where id=" + id + "";
        ResultSet result = selectReturnSet(sql);
        double balance = 0, todayleft = 0, todaypay = 0, totalpay = 0;
        if (result.next()) {
            balance = result.getDouble("balance") + Double.parseDouble(expenditure);
            todayleft = result.getDouble("todayleft") + Double.parseDouble(expenditure);
            todaypay = result.getDouble("todaypay") - Double.parseDouble(expenditure);
            totalpay = result.getDouble("totalpay") - Double.parseDouble(expenditure);
        }
        runMysql("update " + tbname + " set balance=" + balance + ",todayleft=" + todayleft + ",todaypay=" + todaypay + ",totalpay=" + totalpay + " where id=" + id + "");
        stmt.close();
    }

    public boolean initUser(String openid) {
        return (runMysql("CREATE TABLE `" + openid + "` (\n" +
                "  `id` int unsigned NOT NULL AUTO_INCREMENT,\n" +
                "  `name` varchar(30) DEFAULT NULL,\n" +
                "  `balance` double(255,2) unsigned DEFAULT NULL,\n" +
                "  `total` double(255,2) unsigned DEFAULT NULL,\n" +
                "  `dataType` int unsigned DEFAULT NULL,\n" +
                "  `currency` int unsigned DEFAULT NULL,\n" +
                "  `beginTime` varchar(30) DEFAULT NULL,\n" +
                "  `endTime` varchar(30) DEFAULT NULL,\n" +
                "  `rollover` tinyint(1) DEFAULT NULL,\n" +
                "  `remaindays` varchar(30) DEFAULT NULL,\n" +
                "  `todayleft` double(255,2) DEFAULT NULL,\n" +
                "  `totalpay` double(255,2) DEFAULT NULL,\n" +
                "  `todaypay` double(255,2) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4")).equals("0")
                &&
                (runMysql("CREATE TABLE `" + openid + "history` (\n" +
                        "  `id` int unsigned NOT NULL AUTO_INCREMENT,\n" +
                        "  `name` varchar(30) DEFAULT NULL,\n" +
                        "  `nameid` int DEFAULT NULL,\n" +
                        "  `peer` varchar(30) DEFAULT NULL,\n" +
                        "  `peerid` int DEFAULT NULL,\n" +
                        "  `value` double(255,2) DEFAULT NULL,\n" +
                        "  `time` varchar(30) DEFAULT NULL,\n" +
                        "  `remarks` varchar(255) DEFAULT NULL,\n" +
                        "  PRIMARY KEY (`id`)\n" +
                        ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4")).equals("0");
    }

    public boolean deleteBudget(String openid, String budgetid) throws SQLException {
        String sql = "select * from " + openid;
        ResultSet result = selectReturnSet(sql);
        int count = 0;
        while (result.next()) {
            if (++count == 2)
                break;
        }
        if (count == 1) {
            //删表
            return runMysql("DROP TABLE " + openid).equals("0") && runMysql("DROP TABLE " + openid + "history").equals("0");
        }
        return runMysql("delete from " + openid + " where id=" + budgetid).equals("1") && deleteHistory(openid, budgetid, false);
    }

    /*
     * 删除某nameid的所有历史，同时将历史记录进行恢复，加回到原预算额
     */
    private boolean deleteHistory(String openid, String nameid, boolean rollback) throws SQLException {
        String sql = "select * from " + openid + "history where nameid=" + nameid + " or peerid=" + nameid;
        ResultSet result = selectReturnSet(sql);
        //删掉上面查到的记录
        runMysql("delete from " + openid + "history where nameid=" + nameid + " or peerid=" + nameid);
        if (!rollback) return true;
        recoverHistory(result, openid, nameid);
        return true;
    }

    public ResultSet selectReturnSet(String sql) throws SQLException {
        connect();
        return conn.prepareStatement(sql).executeQuery(sql);
    }

    /*
     * 将每条记录进行回复，加回到原预算额
     * nameid是一定存在的
     * 如果是转账历史，peer可能已经被删了，可以不判判断
     */
    private void recoverHistory(ResultSet result, String openid, String nameid) throws SQLException {
        while (result.next()) {
            Double value = result.getDouble("value");
            //先处理peer为null的情况
            if (result.getString("peerid") == null) {
                //value>0则为收入,value<0则为支出,value!=0
                if (value > 0)
                    addIncome(openid, nameid, -value + "");
                else
                    addExpenditure(openid, nameid, -value + "");
            } else if (result.getString("nameid").equals(nameid)) {
                addIncome(openid, result.getInt("peerid") + "", -value + "");
                addExpenditure(openid, nameid, value + "");
            } else if (result.getString("peerid").equals(nameid)) {
                addExpenditure(openid, result.getInt("nameid") + "", value + "");
                addIncome(openid, nameid, -value + "");
            }
        }
    }

    /*
     * 删除历史，同时将历史记录进行恢复，加回到原预算额
     */
    public boolean delete1History(String openid, String id, boolean rollback) throws SQLException {
        String sql = "select * from " + openid + "history where id=" + id;
        ResultSet result = selectReturnSet(sql);
        //删掉上面查到的记录
        runMysql("delete from " + openid + "history where id=" + id);
        if (!rollback) return true;
        //对历史记录进行恢复
        recoverHistory(result, openid, result.getString("nameid"));
        return true;
    }
}
