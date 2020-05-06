package com.acs.util.db;

import com.acs.common.AcsConstants;
import com.acs.listener.EnvironmentInitListener;
import com.acs.model.database.DBConnectInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LBM
 * @time 2017年11月29日 下午2:08:13 项目名称：codecreator 类名称：DbUtils 类描述：数据库工具类
 */
public class DbUtils extends JdbcDaoSupport {
    private Logger logger = LogManager.getLogger(DbUtils.class);

    /**
     * 数据库连接工具
     *
     * @param bds
     */
    public DbUtils(BasicDataSource bds) {
        if (bds == null) {
            BasicDataSource dataSource = (BasicDataSource) EnvironmentInitListener.getTarget(AcsConstants.DATA_BASE);
            super.setDataSource(dataSource);
        } else {
            super.setDataSource(bds);
        }
    }

    /**
     * 构建数据库连接路径
     *
     * @param ip
     * @param port
     * @param database : 数据库名称
     * @param type     ： mysql，oracle等
     * @return
     */
    public static String buildUrl(String ip, String port, String database, String type) {
        if (type.equalsIgnoreCase("mysql")) {
            return "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=false&&useUnicode=true&characterEncoding=utf-8";
        } else if (type.equalsIgnoreCase("oracle")) {
            return "jdbc:oracle:thin:@" + ip + ":" + port + ":" + database;
        } else if (type.equalsIgnoreCase("postgresql")) {
            return "jdbc:postgresql://" + ip + ":" + port + "/" + database;
        }
        return null;
    }

    /**
     * 释放数据库操作相关实例
     *
     * @param obj
     */
    public static void release(Object... obj) {
        if (null != obj) {
            for (Object o : obj) {
                if (o != null) {
                    try {
                        if (o instanceof ResultSet) {
                            ((ResultSet) o).close();
                        } else if (o instanceof Statement) {
                            ((Statement) o).close();
                        } else if (o instanceof Connection) {
                            ((Connection) o).close();
                        } else if (o instanceof PreparedStatement) {
                            ((PreparedStatement) o).close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 测试数据库
     *
     * @return
     */
    public boolean testConnect() {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        if (bds != null) {
            try {
                logger.info("正在尝试连接!!");
                conn = bds.getConnection();
                logger.info("连接成功!!");
                return true;
            } catch (SQLException s) {
                logger.error("连接失败！");
            } finally {
                release(conn);
            }
        }
        return false;
    }

    public boolean insertDataBase(DBConnectInfo dbConnectInfo) {
        boolean isSuccess = false;
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        PreparedStatement preparedStmt = null;
        PreparedStatement uStmt = null;
        Connection conn = null;
        ResultSet rs = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();

                // 创建SQL语句
                String selectSql = "SELECT count(*) FROM dbconfigs WHERE dbdesc=?";
                // 创建预解析器
                preparedStmt = conn.prepareStatement(selectSql);
                preparedStmt.setString(1, dbConnectInfo.getDbDesc());
                rs = preparedStmt.executeQuery();
                int count = -1;
                while (rs.next()) {
                    count = rs.getInt(1);
                }
                if (count == 0) {
                    String insertSql = "INSERT INTO dbconfigs (dbname,dbtype,dbaddress,dbuser,dbpwd,dbport,hasredis,redisaddress,redisport,redispwd,dbdesc)" + " values (?,?,?,?,?,?,?,?,?,?,?)";

                    // 创建预解析器
                    preparedStmt = conn.prepareStatement(insertSql);
                    preparedStmt.setString(1, dbConnectInfo.getDbName());
                    preparedStmt.setString(2, dbConnectInfo.getDbType());
                    preparedStmt.setString(3, dbConnectInfo.getDbAddress());
                    preparedStmt.setString(4, dbConnectInfo.getDbUser());
                    preparedStmt.setString(5, dbConnectInfo.getDbPassword());
                    preparedStmt.setString(6, dbConnectInfo.getDbPort());
                    preparedStmt.setString(7, dbConnectInfo.getNeedRedis());
                    preparedStmt.setString(8, dbConnectInfo.getRedisAddress());
                    preparedStmt.setString(9, dbConnectInfo.getRedisPort());
                    preparedStmt.setString(10, dbConnectInfo.getRedisPassword());
                    preparedStmt.setString(11, dbConnectInfo.getDbDesc());
                    // 执行操作
                    preparedStmt.execute();
                } else {
                    String updateSql = "UPDATE dbconfigs SET  dbname=?,dbtype=?,dbaddress=?,dbuser=?,dbpwd=?,dbport=?,hasredis=?,redisaddress=?,redisport=?,redispwd=? WHERE dbdesc = ?";
                    // 创建预解析器
                    uStmt = conn.prepareStatement(updateSql);
                    uStmt.setString(1, dbConnectInfo.getDbName());
                    uStmt.setString(2, dbConnectInfo.getDbType());
                    uStmt.setString(3, dbConnectInfo.getDbAddress());
                    uStmt.setString(4, dbConnectInfo.getDbUser());
                    uStmt.setString(5, dbConnectInfo.getDbPassword());
                    uStmt.setString(6, dbConnectInfo.getDbPort());
                    uStmt.setString(7, dbConnectInfo.getNeedRedis());
                    uStmt.setString(8, dbConnectInfo.getRedisAddress());
                    uStmt.setString(9, dbConnectInfo.getRedisPort());
                    uStmt.setString(10, dbConnectInfo.getRedisPassword());
                    uStmt.setString(11, dbConnectInfo.getDbDesc());
                    // 执行操作
                    uStmt.execute();
                }

                isSuccess = true;
                logger.info("数据库信息保存成功！");
            } catch (SQLException s) {
                logger.error("数据库信息保存失败！");
                isSuccess = false;
            } finally {
                release(conn, preparedStmt, uStmt, rs);
            }
        }
        return isSuccess;
    }

    public JSONArray getDataBase() {
        JSONArray ja = new JSONArray();
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        PreparedStatement preparedStmt = null;
        Connection conn = null;
        ResultSet rs = null;
        if (bds != null) {
            try {
                // System.out.println("正在尝试查询!");
                conn = bds.getConnection();

                // 创建SQL语句
                String selectSql = "SELECT db.databases_id as dbId,db.dbname as dbName,db.dbtype as dbType,db.dbaddress as dbAddress, db.dbuser as dbUser, db.dbpwd as dbPassword, db.dbport as dbPort, db.dbdesc as dbDesc, db.hasredis as isNeedRedis, db.redisaddress as redisAddress, db.redisport as redisPort, db.redispwd as redisPassword FROM dbconfigs db";

                // 创建预解析器
                preparedStmt = conn.prepareStatement(selectSql);
                rs = preparedStmt.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCount = rsmd.getColumnCount();
                while (rs.next()) {
                    JSONObject jo = new JSONObject();
                    for (int i = 1; i <= colCount; i++) {
                        if (rsmd.getColumnLabel(i) != null) {
                            jo.put(rsmd.getColumnLabel(i), rs.getObject(i) != null ? rs.getObject(i) : "");//获取字段别名
                        } else {
                            jo.put(rsmd.getColumnName(i), rs.getObject(i) != null ? rs.getObject(i) : "");//获取字段名
                        }
                    }
                    ja.add(jo);
                }
            } catch (SQLException s) {
                System.out.println("数据库配置列表获取失败！");
                logger.error(s.getMessage());
            } finally {
                release(conn, preparedStmt, rs);
            }
        }
        return ja;
    }

    //删除数据库连接配置
    public boolean deleteDataBase(String dbDesc) {
        boolean isSuccess = false;
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        PreparedStatement preparedStmt = null;
        Connection conn = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                // 创建SQL语句
                String delSql = "DELETE FROM dbconfigs WHERE dbdesc=?";
                // 创建预解析器
                preparedStmt = conn.prepareStatement(delSql);
                preparedStmt.setString(1, dbDesc);
                int affectedRecords = preparedStmt.executeUpdate();
                if (affectedRecords > 0) {
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                release(conn, preparedStmt);
            }
        }
        return isSuccess;
    }

    //删除接口分组
    public boolean deleteApiGroup(String groupName) {
        boolean isSuccess = false;
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        PreparedStatement preparedStmt = null;
        Connection conn = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                // 创建SQL语句
                String delSql = "DELETE FROM apigroups WHERE groupname=?";
                // 创建预解析器
                preparedStmt = conn.prepareStatement(delSql);
                preparedStmt.setString(1, groupName);
                int affectedRecords = preparedStmt.executeUpdate();
                if (affectedRecords > 0) {
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                release(conn, preparedStmt);
            }
        }
        return isSuccess;
    }

    // 删除工程配置信息
    public boolean deleteProject(String projectName) {
        boolean isSuccess = false;
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        PreparedStatement preparedStmt = null;
        Connection conn = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                // 创建SQL语句
                String delSql = "DELETE FROM projects WHERE projectname=?";
                // 创建预解析器
                preparedStmt = conn.prepareStatement(delSql);
                preparedStmt.setString(1, projectName);
                int affectedRecords = preparedStmt.executeUpdate();
                if (affectedRecords > 0) {
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                release(conn, preparedStmt);
            }
        }
        return isSuccess;

    }

    // 插入工程配置信息
    public boolean insertProject(String projectName, String projectConfig) {
        boolean isSuccess = false;
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        PreparedStatement preparedStmt = null;
        PreparedStatement uStmt = null;
        Connection conn = null;
        ResultSet rs = null;
        if (bds != null) {
            try {
                preparedStmt = null;
                conn = bds.getConnection();

                // 创建SQL语句
                String selectSql = "SELECT count(*) FROM projects WHERE projectname=?";
                // 创建预解析器
                preparedStmt = conn.prepareStatement(selectSql);
                preparedStmt.setString(1, projectName);
                rs = preparedStmt.executeQuery();
                int count = -1;
                while (rs.next()) {
                    count = rs.getInt(1);
                }
                if (count == 0) {
                    String insertSql = "INSERT INTO projects (projectname,projectconfig)" + " values (?, ?)";

                    // 创建预解析器
                    preparedStmt = conn.prepareStatement(insertSql);
                    preparedStmt.setString(1, projectName);
                    preparedStmt.setString(2, projectConfig);
                    // 执行操作
                    preparedStmt.execute();
                } else {
                    String updateSql = "UPDATE projects SET  projectconfig = ? WHERE projectname = ?";
                    // 创建预解析器
                    uStmt = conn.prepareStatement(updateSql);
                    uStmt.setString(1, projectConfig);
                    uStmt.setString(2, projectName);
                    // 执行操作
                    uStmt.execute();
                }

                isSuccess = true;
            } catch (SQLException s) {
                isSuccess = false;
                logger.error("工程配置保存失败！" + s.getMessage());
            } finally {
                release(conn, preparedStmt, uStmt, rs);
            }
        }
        return isSuccess;
    }

    public JSONArray getProjects() {
        JSONArray ja = new JSONArray();
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        PreparedStatement preparedStmt = null;
        Connection conn = null;
        ResultSet rs = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();

                // 创建SQL语句
                String selectSql = "SELECT * FROM projects";
                // 创建预解析器
                preparedStmt = conn.prepareStatement(selectSql);
                rs = preparedStmt.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCount = rsmd.getColumnCount();
                while (rs.next()) {
                    JSONObject jo = new JSONObject();
                    for (int i = 1; i <= colCount; i++) {
                        jo.put(rsmd.getColumnName(i), rs.getObject(i).toString());
                    }
                    ja.add(jo);
                }
            } catch (SQLException s) {
                logger.error("工程配置列表查询失败！" + s.getMessage());
            } finally {
                release(conn, preparedStmt, rs);
            }
        }
        return ja;
    }

    public JSONArray getApiGroups() {
        JSONArray ja = new JSONArray();
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        PreparedStatement preparedStmt = null;
        Connection conn = null;
        ResultSet rs = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();

                // 创建SQL语句
                String selectSql = "SELECT * FROM apigroups";
                // 创建预解析器
                preparedStmt = conn.prepareStatement(selectSql);
                rs = preparedStmt.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCount = rsmd.getColumnCount();
                while (rs.next()) {
                    JSONObject jo = new JSONObject();
                    for (int i = 1; i <= colCount; i++) {
                        jo.put(rsmd.getColumnName(i), rs.getObject(i).toString());
                    }
                    ja.add(jo);
                }
            } catch (SQLException s) {
                logger.error("接口分组列表查询失败！" + s.getMessage());
            } finally {
                release(conn, preparedStmt, rs);
            }
        }
        return ja;
    }

    // 插入工程配置信息
    public boolean insertApiGroup(String apiGroupName) {
        boolean isSuccess = false;
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        PreparedStatement preparedStmt = null;
        Connection conn = null;
        ResultSet rs = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();

                // 创建SQL语句
                String selectSql = "SELECT count(*) FROM apigroups WHERE groupname=?";
                // 创建预解析器
                preparedStmt = conn.prepareStatement(selectSql);
                preparedStmt.setString(1, apiGroupName);
                rs = preparedStmt.executeQuery();
                int count = -1;
                while (rs.next()) {
                    count = rs.getInt(1);
                }
                if (count == 0) {
                    String insertSql = "INSERT INTO apigroups (groupname)" + " values (?)";

                    // 创建预解析器
                    preparedStmt = conn.prepareStatement(insertSql);
                    preparedStmt.setString(1, apiGroupName);
                    // 执行操作
                    preparedStmt.execute();
                }

                isSuccess = true;
            } catch (SQLException s) {
                isSuccess = false;
                logger.error("接口分组配置保存失败！" + s.getMessage());
            } finally {
                release(conn, preparedStmt, rs);
            }
        }
        return isSuccess;
    }

    /**
     * 获取当前配置数据库信息-Oracle
     *
     * @throws SQLException
     */
    public List<Table> getAllTable4Oracle(DBConnectInfo dbci) {
        List<Table> tablelist = new ArrayList<Table>(1000);
        try {
            // 获取数据库连接信息
            Connection conn = this.getConnection();
            /*PreparedStatement pstmt;
            ResultSetMetaData rsmd;
            ResultSet rs;*/
            ResultSet pkrs;
            Map<String, String> pkmap;
            Map<String, String> descmap;
            if (conn == null) {
                logger.info("无法获取数据库连接");
                return null;
            }

            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tablers = databaseMetaData.getTables(null, dbci.getDbUser().toUpperCase(), "%",
                    new String[]{"TABLE"});
            int it = 0;
            while (tablers.next()) {
                pkmap = new HashMap<>();
                descmap = new HashMap<>();
                Table tb = new Table();

                String tableName = tablers.getString("TABLE_NAME");
                if (tableName.indexOf("BIN$") > -1) {
                    continue;
                }
                // System.out.println(Table.getFormatTablename(tableName));
                tb.setTablename(convertName(tb.getFormatTablename(tableName)));
                tb.setDbtablename(tableName);

                it++;
                logger.info("正在处理第" + it + "张表：" + tableName);
                // System.out.println("正在处理第" + it + "张表：" + tableName);

                pkrs = databaseMetaData.getPrimaryKeys(null, null, tableName);
                while (pkrs.next()) {
                    pkmap.put(pkrs.getString(4), null);
                    // System.err.println("COLUMN_NAME: " + pkrs.getObject(4));
                }

                ResultSet columnSet = databaseMetaData.getColumns(null, dbci.getDbUser().toUpperCase(), tableName, "%");
                while (columnSet.next()) {
                    //列名
                    String columnName = columnSet.getString("COLUMN_NAME");
                    //列类型
                    String columnType = columnSet.getString("TYPE_NAME");//列字段类型
                    // 备注
                    String columnComment = columnSet.getString("REMARKS");
                    if (null != columnComment && !"".equalsIgnoreCase(columnComment)) {
                        descmap.put(columnName, columnComment.replaceAll("\r|\n|\\s", ""));
                    } else {
                        descmap.put(columnName, columnComment);
                    }

                    Column cn = new Column();
                    cn.setDbType(oracleType2MybatisJDBCType(columnType));
                    String colName = convertName(columnName.toLowerCase());
                    cn.setJavaname(colName);
                    cn.setUpname(colName.substring(0, 1).toUpperCase() + colName.substring(1));
                    cn.setDbname(columnName);
                    cn.setJavaType(oracleType2JavaType(cn.getDbType()));
                    String desc = descmap.get(cn.getDbname());
                    if (desc == null || "".equals(desc)) {
                        cn.setDesc("此属性没有添加描述");
                    } else {
                        cn.setDesc(desc);
                    }
                    if (pkmap.containsKey(cn.getDbname())) {
                        cn.setPk("1");
                        tb.setPkJavaType(cn.getJavaType());
                    }
                    tb.getColumns().add(cn);
                }

                /*pstmt = conn.prepareStatement("SELECT * FROM " + tableName + " where rownum<=2");
                rs = pstmt.executeQuery();
                rsmd = rs.getMetaData();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    Column cn = new Column();
                    cn.setDbType(oracleType2MybatisJDBCType(rsmd.getColumnTypeName(i)));
                    String colName = convertName(rsmd.getColumnName(i).toLowerCase());
                    cn.setJavaname(colName);
                    cn.setUpname(colName.substring(0, 1).toUpperCase() + colName.substring(1));
                    cn.setDbname(rsmd.getColumnName(i));
                    cn.setJavaType(oracleType2JavaType(cn.getDbType()));
                    String desc = descmap.get(cn.getDbname());
                    if (desc == null || "".equals(desc)) {
                        cn.setDesc("此属性没有添加描述");
                    } else {
                        cn.setDesc(desc);
                    }
                    if (pkmap.containsKey(cn.getDbname())) {
                        cn.setPk("1");
                        tb.setPkJavaType(cn.getJavaType());
                    }
                    tb.getColumns().add(cn);
                }*/
                tablelist.add(tb);
                /*pstmt.close();
                rs.close();*/
                pkrs.close();
                pkmap.clear();
                columnSet.close();
                descmap.clear();
            }
            logger.info("提示：数据库表元数据获取完成。");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tablelist;
    }

    /**
     * 获取当前配置数据库表信息-MySQL
     *
     * @return
     */
    public List<Table> getAllTable4MySQL(DBConnectInfo dbci) {
        List<Table> tablelist = new ArrayList<Table>(1000);
        try {
            // 获取数据库连接信息
            Connection conn = this.getConnection();
            /*PreparedStatement pstmt;
            ResultSetMetaData rsmd;
            ResultSet rs;*/
            ResultSet pkrs;
            Map<String, String> pkmap;
            Map<String, String> descmap;
            if (conn == null) {
                logger.info("无法获取数据库连接");
                return null;
            }

            DatabaseMetaData databaseMetaData = conn.getMetaData();

            int it = 0;
            ResultSet tablers = databaseMetaData.getTables(null, "%", "%", new String[]{"TABLE"});
            while (tablers.next()) {
                pkmap = new HashMap<>();
                descmap = new HashMap<>();
                Table tb = new Table();

                String tableName = tablers.getString("TABLE_NAME");
                // System.out.println(Table.getFormatTablename(tableName));
                tb.setTablename(convertName(tb.getFormatTablename(tableName)));
                tb.setDbtablename(tableName);

                it++;
                logger.info("正在处理第" + it + "张表：" + tableName);

                pkrs = databaseMetaData.getPrimaryKeys(null, null, tableName);
                while (pkrs.next()) {
                    pkmap.put(pkrs.getString(4), null);
                    // System.err.println("COLUMN_NAME: " + pkrs.getObject(4));
                }

                //查看MySQL表列信息 show columns from tbl_name
                ResultSet columnSet = databaseMetaData.getColumns(null, "%", tableName, "%");
                while (columnSet.next()) {
                    //列名
                    String columnName = columnSet.getString("COLUMN_NAME");
                    //列类型
                    String columnType = columnSet.getString("TYPE_NAME");//列字段类型
                    // 备注
                    String columnComment = columnSet.getString("REMARKS");
                    if (null != columnComment && !"".equalsIgnoreCase(columnComment)) {
                        descmap.put(columnName, columnComment.replaceAll("\r|\n|\\s", ""));
                    } else {
                        descmap.put(columnName, columnComment);
                    }

                    Column cn = new Column();
                    cn.setDbType(mysqlType2MybatisJDBCType(columnType));
                    String colName = convertName(columnName.toLowerCase());
                    cn.setJavaname(colName);
                    cn.setUpname(colName.substring(0, 1).toUpperCase() + colName.substring(1));
                    cn.setDbname(columnName);
                    cn.setJavaType(mysqlType2JavaType(cn.getDbType()));
                    String desc = descmap.get(cn.getDbname());
                    if (desc == null || "".equals(desc)) {
                        cn.setDesc("此属性没有添加描述");
                    } else {
                        cn.setDesc(desc);
                    }
                    if (pkmap.containsKey(cn.getDbname())) {
                        cn.setPk("1");
                        tb.setPkJavaType(cn.getJavaType());
                    }
                    tb.getColumns().add(cn);
                }

                /*pstmt = conn.prepareStatement("SELECT * FROM " + tableName + " limit 0,2");
                rs = pstmt.executeQuery();
                rsmd = rs.getMetaData();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    Column cn = new Column();
                    cn.setDbType(mysqlType2MybatisJDBCType(rsmd.getColumnTypeName(i)));
                    String colName = convertName(rsmd.getColumnName(i).toLowerCase());
                    cn.setJavaname(colName);
                    cn.setUpname(colName.substring(0, 1).toUpperCase() + colName.substring(1));
                    cn.setDbname(rsmd.getColumnName(i));
                    cn.setJavaType(mysqlType2JavaType(cn.getDbType()));
                    String desc = descmap.get(cn.getDbname());
                    if (desc == null || "".equals(desc)) {
                        cn.setDesc("此属性没有添加描述");
                    } else {
                        cn.setDesc(desc);
                    }
                    if (pkmap.containsKey(cn.getDbname())) {
                        cn.setPk("1");
                        tb.setPkJavaType(cn.getJavaType());
                    }
                    tb.getColumns().add(cn);
                }*/
                tablelist.add(tb);
                /*pstmt.close();
                rs.close();*/
                pkrs.close();
                pkmap.clear();
                columnSet.close();
                descmap.clear();
            }
            logger.info("提示：数据库表元数据获取完成。");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tablelist;
    }

    /**
     * 获取当前配置数据库表信息-PostgreSQL
     * 通过SQL语句查询PostgreSQL数据库表信息
     * "select * from pg_tables" —— 得到当前db中所有表的信息（这里pg_tables是系统视图）
     * "select tablename from pg_tables where schemaname='public'" —— 得到所有用户自定义表的名字（这里"tablename"字段是表的名字，"schemaname"是schema的名字。用户自定义的表，如果未经特殊处理，默认都是放在名为public的schema下）
     *
     * @return
     */
    public List<Table> getAllTable4PostgreSQL(DBConnectInfo dbci) {
        List<Table> tablelist = new ArrayList<Table>(1000);
        try {
            // 获取数据库连接信息
            Connection conn = this.getConnection();
            ResultSet pkrs;
            Map<String, String> pkmap;
            Map<String, String> descmap;
            if (conn == null) {
                logger.info("无法获取数据库连接");
                return null;
            }

            DatabaseMetaData databaseMetaData = conn.getMetaData();
            //根据数据库元数据获取schema列表
            JSONArray schemaList = new JSONArray();
            BasicDataSource bds = (BasicDataSource) this.getDataSource();
            PreparedStatement preparedStmt = null;
            ResultSet schemaRs = null;
            try {
                /**
                 * @description PG库系统默认模式, 用于过滤
                 * @author winnerlbm
                 * @date 2019-12-31 17:38:37
                 */
                ArrayList<String> filterSchemas = new ArrayList<>();
                filterSchemas.add("pg_catalog");
                filterSchemas.add("information_schema");
                // 创建SQL语句
                String selectSql = "SELECT table_schema FROM information_schema.tables GROUP BY table_schema";
                // 创建预解析器
                preparedStmt = conn.prepareStatement(selectSql);
                schemaRs = preparedStmt.executeQuery();
                ResultSetMetaData rsmd = schemaRs.getMetaData();
                int colCount = rsmd.getColumnCount();
                while (schemaRs.next()) {
                    for (int i = 1; i <= colCount; i++) {
                        String smValue = schemaRs.getObject(i).toString().toLowerCase();
                        if (filterSchemas.indexOf(smValue) == -1) {
                            JSONObject jo = new JSONObject();
                            jo.put(rsmd.getColumnName(i), schemaRs.getObject(i).toString());
                            schemaList.add(jo);
                        }
                    }
                }
                filterSchemas.clear();
            } catch (SQLException s) {
                logger.error("数据模式列表查询失败！" + s.getMessage());
            } finally {
                release(preparedStmt, schemaRs);
            }

            if (schemaList != null && schemaList.size() > 0) {
                for (Object obj : schemaList) {
                    String schemaName = ((JSONObject) obj).getString("table_schema");
                    int it = 0;
                    //仅获取public里面的表
                    ResultSet tablers = databaseMetaData.getTables(null, schemaName, "%", new String[]{"TABLE"});
                    //获取pg所有表含系统表
                    /*ResultSet tablers = databaseMetaData.getTables(null, "%", "%", new String[]{"TABLE"});*/
                    while (tablers.next()) {
                        pkmap = new HashMap<>();
                        descmap = new HashMap<>();
                        Table tb = new Table();

                        String tableName = tablers.getString("TABLE_NAME");
                        // System.out.println(Table.getFormatTablename(tableName));
                        tb.setTablename(convertName(tb.getFormatTablename(tableName)));
                        tb.setDbtablename(tableName);
                        tb.setSchemaname(schemaName);

                        it++;
                        logger.info("正在处理" + schemaName + "第" + it + "张表：" + tableName);

                        pkrs = databaseMetaData.getPrimaryKeys(null, null, tableName);
                        while (pkrs.next()) {
                            pkmap.put(pkrs.getString(4), null);
                            // System.err.println("COLUMN_NAME: " + pkrs.getObject(4));
                        }

                        //查看PostgreSQL表列信息 show columns from tbl_name
                        ResultSet columnSet = databaseMetaData.getColumns(null, "%", tableName, "%");
                        //rsmd = columnSet.getMetaData();
                        //int numberOfColumns = rsmd.getColumnCount();//表中列个数
                        while (columnSet.next()) {
                            //列名
                            String columnName = columnSet.getString("COLUMN_NAME");
                            //列类型
                            String columnType = columnSet.getString("TYPE_NAME");//列字段类型
                            // 备注
                            String columnComment = columnSet.getString("REMARKS");
                            if (null != columnComment && !"".equalsIgnoreCase(columnComment)) {
                                descmap.put(columnName, columnComment.replaceAll("\r|\n|\\s", ""));
                            } else {
                                descmap.put(columnName, columnComment);
                            }

                            Column cn = new Column();
                            cn.setDbType(pgType2MybatisJDBCType(columnType));
                            String colName = convertName(columnName.toLowerCase());
                            cn.setJavaname(colName);
                            cn.setUpname(colName.substring(0, 1).toUpperCase() + colName.substring(1));
                            cn.setDbname(columnName);
                            cn.setJavaType(pgType2JavaType(cn.getDbType()));
                            String desc = descmap.get(cn.getDbname());
                            if (desc == null || "".equals(desc)) {
                                cn.setDesc("此属性没有添加描述");
                            } else {
                                cn.setDesc(desc);
                            }
                            if (pkmap.containsKey(cn.getDbname())) {
                                cn.setPk("1");
                                tb.setPkJavaType(cn.getJavaType());
                            }
                            tb.getColumns().add(cn);
                        }

                        tablelist.add(tb);
                        pkrs.close();
                        pkmap.clear();
                        columnSet.close();
                        descmap.clear();
                    }
                }
            }
            logger.info("提示：数据库表元数据获取完成。");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tablelist;
    }

    private String convertName(String column) {
        String[] cls = column.split("_");
        StringBuffer sb = new StringBuffer(cls[0]);

        int l = cls.length;
        for (int i = 1; i < l; i++) {
            sb.append(firstLetterUpcase(cls[i]));
        }
        return sb.toString();
    }

    public String firstLetterUpcase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // Oracle类型转换
    private String oracleType2MybatisJDBCType(String oracleType) {
        if (StringUtils.isBlank(oracleType))
            return "";
        if ("CHAR".equalsIgnoreCase(oracleType))
            return "CHAR";
        else if ("VARCHAR2".equalsIgnoreCase(oracleType))
            return "VARCHAR";
        else if ("LONG".equalsIgnoreCase(oracleType))
            return "LONGVARCHAR";
        else if ("NUMBER".equalsIgnoreCase(oracleType))// 这里有多种对应关系，目前只考虑转double类型
            return "DOUBLE";
        else if ("RAW".equalsIgnoreCase(oracleType))
            return "VARBINARY";
        else if ("LONGRAW".equalsIgnoreCase(oracleType))
            return "LONGVARBINARY";
        else if ("DATE".equalsIgnoreCase(oracleType))
            return "TIMESTAMP";
        else if ("TIMESTAMP".equalsIgnoreCase(oracleType))
            return "TIMESTAMP";
        else if ("BLOB".equalsIgnoreCase(oracleType))
            return "BLOB";
        else if ("CLOB".equalsIgnoreCase(oracleType))
            return "CLOB";
        else if ("STRUCT".equalsIgnoreCase(oracleType))
            return "STRUCT";
        else if ("REF".equalsIgnoreCase(oracleType))
            return "REF";
        else if ("ARRAY".equalsIgnoreCase(oracleType))
            return "ARRAY";
        else if ("BFILE".equalsIgnoreCase(oracleType))
            return "BFILE";
        else if ("ROWID".equalsIgnoreCase(oracleType))
            return "ROWID";
        else if ("REF CURSOR".equalsIgnoreCase(oracleType))
            return "CURSOR";
        else if ("TIMESTAMP WITH TIME ZONE".equalsIgnoreCase(oracleType))
            return "TIMESTAMPTZ";
        else if ("TIMESTAMP WITH LOCAL TIME ZONE".equalsIgnoreCase(oracleType))
            return "TIMESTAMPLTZ";
        else
            return "";
    }

    private String oracleType2JavaType(String oracleType) {
        if (StringUtils.isBlank(oracleType))
            return "";
        if ("VARCHAR".equalsIgnoreCase(oracleType))
            return String.class.getSimpleName();
        else if ("CHAR".equalsIgnoreCase(oracleType))
            return String.class.getSimpleName();
        else if ("LONGVARCHAR".equalsIgnoreCase(oracleType))
            return String.class.getSimpleName();
        else if ("NUMERIC".equalsIgnoreCase(oracleType))
            return BigDecimal.class.getSimpleName();
        else if ("DECIMAL".equalsIgnoreCase(oracleType))
            return BigDecimal.class.getSimpleName();
        else if ("BIT".equalsIgnoreCase(oracleType))
            return Boolean.class.getSimpleName();
        else if ("BOOLEAN".equalsIgnoreCase(oracleType))
            return Boolean.class.getSimpleName();
        else if ("TINYINT".equalsIgnoreCase(oracleType))
            return Byte.class.getSimpleName();
        else if ("SMALLINT".equalsIgnoreCase(oracleType))
            return Short.class.getSimpleName();
        else if ("INTEGER".equalsIgnoreCase(oracleType))
            return Integer.class.getSimpleName();
        else if ("BIT".equalsIgnoreCase(oracleType))
            return Boolean.class.getSimpleName();
        else if ("BIGINT".equalsIgnoreCase(oracleType))
            return Long.class.getSimpleName();
        else if ("REAL".equalsIgnoreCase(oracleType))
            return Float.class.getSimpleName();
        else if ("FLOAT".equalsIgnoreCase(oracleType))
            return Double.class.getSimpleName();
        else if ("DOUBLE".equalsIgnoreCase(oracleType))
            return Double.class.getSimpleName();
        else if ("BINARY".equalsIgnoreCase(oracleType))
            return Byte[].class.getSimpleName();
        else if ("VARBINARY".equalsIgnoreCase(oracleType))
            return Byte[].class.getSimpleName();
        else if ("LONGVARBINARY".equalsIgnoreCase(oracleType))
            return Byte[].class.getSimpleName();
        else if ("DATE".equalsIgnoreCase(oracleType))
            return String.class.getSimpleName();
        else if ("TIME".equalsIgnoreCase(oracleType))
            return String.class.getSimpleName();
        else if ("TIMESTAMP".equalsIgnoreCase(oracleType))
            return String.class.getSimpleName();
        else if ("CLOB".equalsIgnoreCase(oracleType))
            return String.class.getSimpleName();
        else if ("BLOB".equalsIgnoreCase(oracleType))
            return Byte[].class.getSimpleName();
        else if ("ARRAY".equalsIgnoreCase(oracleType))
            return Array.class.getSimpleName();
        else if ("STRUCT".equalsIgnoreCase(oracleType))
            return Struct.class.getSimpleName();
        else if ("REF".equalsIgnoreCase(oracleType))
            return Ref.class.getSimpleName();
        else if ("DATALINK".equalsIgnoreCase(oracleType))
            return URL.class.getSimpleName();
        else
            return "";
    }

    /// MySQL类型转换
    private String mysqlType2MybatisJDBCType(String mysqltype) {
        if (StringUtils.isBlank(mysqltype))
            return "";
        if ("VARCHAR".equalsIgnoreCase(mysqltype))
            return "VARCHAR";
        else if ("CHAR".equalsIgnoreCase(mysqltype))
            return "VARCHAR";
        else if ("BLOB".equalsIgnoreCase(mysqltype) || "GEOMETRY".equalsIgnoreCase(mysqltype))
            return "BLOB";
        else if ("TEXT".equalsIgnoreCase(mysqltype) || "MEDIUMTEXT".equalsIgnoreCase(mysqltype)
                || "LONGTEXT".equalsIgnoreCase(mysqltype))
            return "BLOB";
        else if ("INT".equalsIgnoreCase(mysqltype) || "INT UNSIGNED".equalsIgnoreCase(mysqltype))
            return "INTEGER";
        else if ("INTEGER".equalsIgnoreCase(mysqltype))
            return "INTEGER";
        else if ("TINYINT".equalsIgnoreCase(mysqltype) || "TINYINT UNSIGNED".equalsIgnoreCase(mysqltype))
            return "TINYINT";
        else if ("SMALLINT".equalsIgnoreCase(mysqltype))
            return "SMALLINT";
        else if ("MEDIUMINT".equalsIgnoreCase(mysqltype))
            return "INTEGER";
        else if ("BIT".equalsIgnoreCase(mysqltype))
            return "BIT";
        else if ("BIGINT".equalsIgnoreCase(mysqltype) || "BIGINT UNSIGNED".equalsIgnoreCase(mysqltype))
            return "BIGINT";
        else if ("FLOAT".equalsIgnoreCase(mysqltype) || "FLOAT UNSIGNED".equalsIgnoreCase(mysqltype))
            return "FLOAT";
        else if ("DOUBLE".equalsIgnoreCase(mysqltype))
            return "DOUBLE";
        else if ("DECIMAL".equalsIgnoreCase(mysqltype))
            return "DOUBLE";
        else if ("BOOLEAN".equalsIgnoreCase(mysqltype))
            return "BOOLEAN";
        else if ("ID".equalsIgnoreCase(mysqltype))
            return "BIGINT";
        else if ("DATE".equalsIgnoreCase(mysqltype))
            return "DATE";
        else if ("TIME".equalsIgnoreCase(mysqltype))
            return "DATE";
        else if ("DATETIME".equalsIgnoreCase(mysqltype))
            return "TIMESTAMP";
        else if ("TIMESTAMP".equalsIgnoreCase(mysqltype))
            return "TIMESTAMP";
        else if ("YEAR".equalsIgnoreCase(mysqltype))
            return "DATE";
        else
            return "";

    }

    private String mysqlType2JavaType(String mysqltype) {
        if (StringUtils.isBlank(mysqltype))
            return "";
        if ("VARCHAR".equalsIgnoreCase(mysqltype))
            return String.class.getSimpleName();
        else if ("CHAR".equalsIgnoreCase(mysqltype))
            return String.class.getSimpleName();
        else if ("BLOB".equalsIgnoreCase(mysqltype) || "GEOMETRY".equalsIgnoreCase(mysqltype))
            return Byte[].class.getSimpleName();
        else if ("TEXT".equalsIgnoreCase(mysqltype) || "MEDIUMTEXT".equalsIgnoreCase(mysqltype) || "LONGTEXT".equalsIgnoreCase(mysqltype))
            return String.class.getSimpleName();
        else if ("INT".equalsIgnoreCase(mysqltype) || "INT UNSIGNED".equalsIgnoreCase(mysqltype))
            return Integer.class.getSimpleName();
        else if ("INTEGER".equalsIgnoreCase(mysqltype))
            return Integer.class.getSimpleName();
        else if ("TINYINT".equalsIgnoreCase(mysqltype) || "TINYINT UNSIGNED".equalsIgnoreCase(mysqltype))
            return Short.class.getSimpleName();
        else if ("SMALLINT".equalsIgnoreCase(mysqltype))
            return Integer.class.getSimpleName();
        else if ("MEDIUMINT".equalsIgnoreCase(mysqltype))
            return Integer.class.getSimpleName();
        else if ("BIT".equalsIgnoreCase(mysqltype))
            return Boolean.class.getSimpleName();
        else if ("BIGINT".equalsIgnoreCase(mysqltype) || "BIGINT UNSIGNED".equalsIgnoreCase(mysqltype))
            return Long.class.getSimpleName();
        else if ("FLOAT".equalsIgnoreCase(mysqltype) || "FLOAT UNSIGNED".equalsIgnoreCase(mysqltype))
            return Float.class.getSimpleName();
        else if ("DOUBLE".equalsIgnoreCase(mysqltype))
            return Double.class.getSimpleName();
        else if ("DECIMAL".equalsIgnoreCase(mysqltype))
            return Double.class.getSimpleName();
        else if ("BOOLEAN".equalsIgnoreCase(mysqltype))
            return Short.class.getSimpleName();
        else if ("ID".equalsIgnoreCase(mysqltype))
            return Long.class.getSimpleName();
        else if ("DATE".equalsIgnoreCase(mysqltype))
            return String.class.getSimpleName();
        else if ("TIME".equalsIgnoreCase(mysqltype))
            return String.class.getSimpleName();
        else if ("DATETIME".equalsIgnoreCase(mysqltype))
            return String.class.getSimpleName();
        else if ("TIMESTAMP".equalsIgnoreCase(mysqltype))
            return String.class.getSimpleName();
        else if ("YEAR".equalsIgnoreCase(mysqltype))
            return String.class.getSimpleName();
        else
            return "";

    }

    /// PostgreSQL类型转换
    private String pgType2MybatisJDBCType(String pgtype) {
        if (StringUtils.isBlank(pgtype))
            return "";
        if ("VARCHAR".equalsIgnoreCase(pgtype) || "CIDR".equalsIgnoreCase(pgtype) || "INET".equalsIgnoreCase(pgtype) || "MACADDR".equalsIgnoreCase(pgtype) || "UUID".equalsIgnoreCase(pgtype))
            return "VARCHAR";
        else if ("TEXT".equalsIgnoreCase(pgtype) || "CHAR".equalsIgnoreCase(pgtype) || "NATIONAL CHARACTER".equalsIgnoreCase(pgtype) || "BPCHAR".equalsIgnoreCase(pgtype) || "GEOMETRY".equalsIgnoreCase(pgtype))
            return "VARCHAR";
        else if ("BLOB".equalsIgnoreCase(pgtype))
            return "BLOB";
        else if ("NATIONAL CHARACTER VARYING".equalsIgnoreCase(pgtype) || "BYTEA".equalsIgnoreCase(pgtype))
            return "BLOB";
        else if ("INT".equalsIgnoreCase(pgtype) || "INT UNSIGNED".equalsIgnoreCase(pgtype) || "SERIAL".equalsIgnoreCase(pgtype) || "INT2".equalsIgnoreCase(pgtype) || "INT4".equalsIgnoreCase(pgtype) || "INT8".equalsIgnoreCase(pgtype))
            return "INTEGER";
        else if ("INTEGER".equalsIgnoreCase(pgtype))
            return "INTEGER";
        else if ("TINYINT".equalsIgnoreCase(pgtype) || "TINYINT UNSIGNED".equalsIgnoreCase(pgtype))
            return "TINYINT";
        else if ("SMALLINT".equalsIgnoreCase(pgtype) || "SMALLSERIAL".equalsIgnoreCase(pgtype))
            return "SMALLINT";
        else if ("MEDIUMINT".equalsIgnoreCase(pgtype))
            return "INTEGER";
        else if ("BIT".equalsIgnoreCase(pgtype))
            return "BIT";
        else if ("BIGINT".equalsIgnoreCase(pgtype) || "BIGINT UNSIGNED".equalsIgnoreCase(pgtype) || "BIGSERIAL".equalsIgnoreCase(pgtype))
            return "BIGINT";
        else if ("FLOAT".equalsIgnoreCase(pgtype) || "FLOAT4".equalsIgnoreCase(pgtype) || "FLOAT8".equalsIgnoreCase(pgtype) || "REAL".equalsIgnoreCase(pgtype))
            return "FLOAT";
        else if ("DOUBLE".equalsIgnoreCase(pgtype) || "DOUBLE PRECISION".equalsIgnoreCase(pgtype))
            return "DOUBLE";
        else if ("DECIMAL".equalsIgnoreCase(pgtype) || "NUMERIC".equalsIgnoreCase(pgtype) || "MONEY".equalsIgnoreCase(pgtype))
            return "DOUBLE";
        else if ("BOOLEAN".equalsIgnoreCase(pgtype))
            return "BOOLEAN";
        else if ("ID".equalsIgnoreCase(pgtype))
            return "BIGINT";
        else if ("DATE".equalsIgnoreCase(pgtype))
            return "DATE";
        else if ("TIME".equalsIgnoreCase(pgtype) || "INTERVAL".equalsIgnoreCase(pgtype))
            return "DATE";
        else if ("DATETIME".equalsIgnoreCase(pgtype))
            return "TIMESTAMP";
        else if ("TIMESTAMP".equalsIgnoreCase(pgtype))
            return "TIMESTAMP";
        else if ("YEAR".equalsIgnoreCase(pgtype))
            return "DATE";
        else
            return "";

    }

    private String pgType2JavaType(String pgtype) {
        if (StringUtils.isBlank(pgtype))
            return "";
        if ("VARCHAR".equalsIgnoreCase(pgtype) || "BPCHAR".equalsIgnoreCase(pgtype))
            return String.class.getSimpleName();
        else if ("CIDR".equalsIgnoreCase(pgtype) || "INET".equalsIgnoreCase(pgtype) || "MACADDR".equalsIgnoreCase(pgtype) || "BOX".equalsIgnoreCase(pgtype) || "CIRCLE".equalsIgnoreCase(pgtype) || "INTERVAL".equalsIgnoreCase(pgtype) || "LINE".equalsIgnoreCase(pgtype) || "LSEG".equalsIgnoreCase(pgtype) || "PATH".equalsIgnoreCase(pgtype) || "POINT".equalsIgnoreCase(pgtype) || "POLYGON".equalsIgnoreCase(pgtype) || "VARBIT".equalsIgnoreCase(pgtype) || "TXID_SNAPSHOT".equalsIgnoreCase(pgtype) || "GEOMETRY".equalsIgnoreCase(pgtype))
            return String.class.getSimpleName();
        else if ("CHAR".equalsIgnoreCase(pgtype) || "NATIONAL CHARACTER".equalsIgnoreCase(pgtype) || "NATIONAL CHARACTER VARYING".equalsIgnoreCase(pgtype))
            return String.class.getSimpleName();
        else if ("BLOB".equalsIgnoreCase(pgtype) || "BYTEA".equalsIgnoreCase(pgtype))
            return Byte[].class.getSimpleName();
        else if ("TEXT".equalsIgnoreCase(pgtype))
            return String.class.getSimpleName();
        else if ("INT".equalsIgnoreCase(pgtype) || "INT UNSIGNED".equalsIgnoreCase(pgtype) || "INT2".equalsIgnoreCase(pgtype) || "INT4".equalsIgnoreCase(pgtype) || "INT8".equalsIgnoreCase(pgtype) || "INTEGER".equalsIgnoreCase(pgtype))
            return Integer.class.getSimpleName();
        else if ("TINYINT".equalsIgnoreCase(pgtype) || "TINYINT UNSIGNED".equalsIgnoreCase(pgtype))
            return Short.class.getSimpleName();
        else if ("SMALLINT".equalsIgnoreCase(pgtype) || "SMALLSERIAL".equalsIgnoreCase(pgtype))
            return Integer.class.getSimpleName();
        else if ("MEDIUMINT".equalsIgnoreCase(pgtype))
            return Integer.class.getSimpleName();
        else if ("BIT".equalsIgnoreCase(pgtype) || "BOOL".equalsIgnoreCase(pgtype))
            return Boolean.class.getSimpleName();
        else if ("BIGINT".equalsIgnoreCase(pgtype) || "BIGINT UNSIGNED".equalsIgnoreCase(pgtype) || "BIGSERIAL".equalsIgnoreCase(pgtype))
            return Long.class.getSimpleName();
        else if ("FLOAT".equalsIgnoreCase(pgtype) || "FLOAT4".equalsIgnoreCase(pgtype) || "FLOAT8".equalsIgnoreCase(pgtype) || "REAL".equalsIgnoreCase(pgtype))
            return Float.class.getSimpleName();
        else if ("DOUBLE".equalsIgnoreCase(pgtype) || "DOUBLE PRECISION".equalsIgnoreCase(pgtype))
            return Double.class.getSimpleName();
        else if ("DECIMAL".equalsIgnoreCase(pgtype) || "MONEY".equalsIgnoreCase(pgtype) || "NUMERIC".equalsIgnoreCase(pgtype))
            return Double.class.getSimpleName();
        else if ("BOOLEAN".equalsIgnoreCase(pgtype))
            return Short.class.getSimpleName();
        else if ("ID".equalsIgnoreCase(pgtype))
            return Long.class.getSimpleName();
        else if ("DATE".equalsIgnoreCase(pgtype))
            return String.class.getSimpleName();
        else if ("TIME".equalsIgnoreCase(pgtype))
            return String.class.getSimpleName();
        else if ("DATETIME".equalsIgnoreCase(pgtype))
            return String.class.getSimpleName();
        else if ("TIMESTAMP".equalsIgnoreCase(pgtype))
            return String.class.getSimpleName();
        else if ("YEAR".equalsIgnoreCase(pgtype))
            return String.class.getSimpleName();
        else
            return "";

    }

    public static void main(String[] args) {

        // System.out.println(int.class.getSimpleName());
    }
}
