package com.acs.util.db;

import com.acs.common.AcsConstants;
import com.acs.listener.EnvironmentInitListener;
import com.acs.model.database.DBConnectInfo;
import com.acs.model.satellite.TleGroupInfo;
import com.acs.util.UUIDUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * 卫星轨道预测工具类, 支持自动事务管理
 */
@Transactional(rollbackFor = Exception.class)
public class SatelliteSupport extends JdbcDaoSupport {
    /**
     * 构造函数
     *
     * @param dbci 数据库配置
     */
    public SatelliteSupport(DBConnectInfo dbci) {
        if (dbci == null) {
            BasicDataSource dataSource = (BasicDataSource) EnvironmentInitListener.getTarget(AcsConstants.DATA_BASE);
            super.setDataSource(dataSource);
        } else {
            // 初始化连接池
            BasicDataSource bds = new BasicDataSource();
            // 将JDBC建立连接所需要的信息设置到连接池中
            bds.setDriverClassName(dbci.getDbType());
            if (dbci.getDbType().indexOf("oracle") > -1) {
                bds.setUrl(DataProcessSupport.buildUrl(dbci.getDbAddress(), dbci.getDbPort(), dbci.getDbName(), AcsConstants.ORACLE));
                bds.setConnectionProperties("remarksReporting=true");
            } else if (dbci.getDbType().indexOf("mysql") > -1) {
                bds.setUrl(DataProcessSupport.buildUrl(dbci.getDbAddress(), dbci.getDbPort(), dbci.getDbName(), AcsConstants.MYSQL));
            }
            bds.setUsername(dbci.getDbUser());
            bds.setPassword(dbci.getDbPassword());
            bds.setRemoveAbandonedTimeout(300);// 单位秒
            bds.setMaxActive(100);
            bds.setMaxWait(60000);
            super.setDataSource(bds);
        }
    }

    /**
     * 构建数据库连接路径
     *
     * @param ip
     * @param port
     * @param database 数据库名称
     * @param type     mysql，oracle等
     * @return
     */
    public static String buildUrl(String ip, String port, String database, String type) {
        if (type.equalsIgnoreCase("mysql")) {
            return "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=false&&useUnicode=true&characterEncoding=utf-8";
        } else if (type.equalsIgnoreCase("oracle")) {
            return "jdbc:oracle:thin:@" + ip + ":" + port + ":" + database;
        }
        return null;
    }

    /**
     * 模块启动，默认初始化卫星预测模块相关表
     *
     * @return
     */
    public boolean initSatellite() {
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        if (jdbcTemplate != null) {
            try {
                String tlesSql = "CREATE TABLE IF NOT EXISTS tles (\n" +
                        "  tle_id varchar(45) NOT NULL,\n" +
                        "  tle_name varchar(45) DEFAULT NULL COMMENT '卫星名称',\n" +
                        "  tle_frow varchar(300) DEFAULT NULL COMMENT '第一行根数',\n" +
                        "  tle_srow varchar(300) DEFAULT NULL COMMENT '第二行根数',\n" +
                        "  tle_desc varchar(45) DEFAULT NULL COMMENT '描述',\n" +
                        "  PRIMARY KEY (tle_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='卫星两行根数表'";
                jdbcTemplate.execute(tlesSql);

                String tlesGroupSql = "CREATE TABLE IF NOT EXISTS tlesgroup (\n" +
                        "  group_id varchar(45) NOT NULL COMMENT '分组ID',\n" +
                        "  groupname varchar(45) NOT NULL COMMENT '分组名称',\n" +
                        "  groupdate datetime DEFAULT NULL COMMENT '分组创建日期',\n" +
                        "  groupuser varchar(45) DEFAULT NULL COMMENT '分组用户',\n" +
                        "  groupuserid varchar(45) DEFAULT NULL COMMENT '分组用户ID',\n" +
                        "  groupdesc mediumtext COMMENT '分组描述',\n" +
                        "  PRIMARY KEY (group_id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='卫星分组表'";
                jdbcTemplate.execute(tlesGroupSql);

                String tgSql = "CREATE TABLE IF NOT EXISTS tlesgroupref (\n" +
                        "  group_id varchar(45) NOT NULL COMMENT '分组ID',\n" +
                        "  tle_id varchar(45) NOT NULL COMMENT '两行根数ID'\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='分组与TLE关联表'";
                jdbcTemplate.execute(tgSql);
                return true;
            } catch (Exception e) {
                logger.error("初始化卫星预测环境失败");
            } finally {
                release(jdbcTemplate);
            }
        }
        return false;
    }

    /**
     * 清空tle数据
     *
     * @return
     */
    public boolean clearTles() {
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        if (jdbcTemplate != null) {
            try {
                String clearSql = "DELETE FROM tles";
                jdbcTemplate.execute(clearSql);
                return true;
            } catch (Exception e) {
                logger.error("Tle数据清除失败");
            } finally {
                release(jdbcTemplate);
            }
        }
        return false;
    }

    /**
     * tles数据入库
     *
     * @param tles
     * @return
     */
    public boolean saveTles(String[] tles) throws SQLException {
        boolean isSuccess = false;
        String saveSql = "INSERT INTO acdb.tles (tle_id, tle_name, tle_frow, tle_srow, tle_desc) VALUES (?, ?, ?, ?, ?)";
        //执行数据插入
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        PreparedStatement preparedStmt = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                preparedStmt = conn.prepareStatement(saveSql);
                conn.setAutoCommit(false);
                int size = tles.length;
                int counter = 0;
                for (int i = 0; i < size - 3; i += 3) {
                    String tleName = tles[i].trim().substring(2);//去掉开始位置的“0 ”
                    String tleId = tleName + "-TLE-NUM-" + counter;//UUIDUtil.getUUID();
                    counter++;
                    String tleFirstRow = tles[i + 1].trim();
                    String tleSecondRow = tles[i + 2].trim();
                    String tleDesc = "";
                    preparedStmt.setString(1, tleId);
                    preparedStmt.setString(2, tleName);
                    preparedStmt.setString(3, tleFirstRow);
                    preparedStmt.setString(4, tleSecondRow);
                    preparedStmt.setString(5, tleDesc);
                    preparedStmt.addBatch();
                    if (i % 10 == 0) {
                        //中间批处理入库操作
                        preparedStmt.executeBatch();
                        preparedStmt.clearBatch();
                    }
                    //logger.info(tleName);
                }
                // 执行剩余入库操作
                preparedStmt.executeBatch();
                conn.commit();
                isSuccess = true;
                logger.info("TLE数据导入成功");
            } catch (SQLException e) {
                conn.rollback();
                logger.error("TLE数据导入失败");
            } finally {
                release(conn, preparedStmt);
            }
        }
        return isSuccess;
    }

    /**
     * 根据卫星名称列表配置查询相关的tle数据
     *
     * @return
     */
    public JSONArray getTlesByConfig(String[] satellites) {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        JSONArray jsonArray = null;
        String sSql = "";
        if (bds != null) {
            try {
                int size = satellites.length;
                if (size > 0) {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < size; i++) {
                        if (i != size - 1) {
                            sb.append("'" + satellites[i] + "',");
                        } else {
                            sb.append("'" + satellites[i] + "'");
                        }
                    }

                    sSql = "SELECT \n" +
                            "    tle_id AS tid,\n" +
                            "    tle_name AS text,\n" +
                            "    tle_frow AS tfr,\n" +
                            "    tle_srow AS tsr,\n" +
                            "    tle_desc AS td\n" +
                            "FROM\n" +
                            "    tles\n" +
                            "WHERE\n" +
                            "    tle_name IN (" + sb.toString() + ")";
                    conn = bds.getConnection();
                    stmt = conn.createStatement();
                    rs = stmt.executeQuery(sSql);
                    if (null != rs) {
                        ResultSetMetaData rsm = rs.getMetaData();
                        int count = rsm.getColumnCount();
                        if (count > 0) {
                            jsonArray = new JSONArray();
                            while (rs.next()) {
                                JSONObject jsonObject = new JSONObject();
                                for (int i = 1; i <= count; i++) {
                                    Object obj = rs.getObject(i);
                                    //String columnName = rsm.getColumnName(i);//列名
                                    String columnName = rsm.getColumnLabel(i);//别名
                                    jsonObject.put(columnName.trim(), (obj == null) ? "" : obj);
                                }
                                jsonArray.add(jsonObject);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                logger.error("TLE数据获取失败");
            } finally {
                release(conn, stmt, rs);
            }
        }
        return jsonArray;
    }

    /**
     * 根据tleid查询tle
     *
     * @param tleId
     * @return
     */
    public JSONArray getTleById(String tleId) {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        JSONArray jsonArray = null;
        if (bds != null) {
            try {
                String sSql = "SELECT \n" +
                        "    tle_id AS tid,\n" +
                        "    tle_name AS text,\n" +
                        "    tle_frow AS tfr,\n" +
                        "    tle_srow AS tsr,\n" +
                        "    tle_desc AS td\n" +
                        "FROM\n" +
                        "    tles\n" +
                        "WHERE\n" +
                        "    tle_id = '" + tleId + "'";
                conn = bds.getConnection();
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sSql);
                if (null != rs) {
                    ResultSetMetaData rsm = rs.getMetaData();
                    int count = rsm.getColumnCount();
                    if (count > 0) {
                        jsonArray = new JSONArray();
                        while (rs.next()) {
                            JSONObject jsonObject = new JSONObject();
                            for (int i = 1; i <= count; i++) {
                                Object obj = rs.getObject(i);
                                //String columnName = rsm.getColumnName(i);//列名
                                String columnName = rsm.getColumnLabel(i);//别名
                                jsonObject.put(columnName.trim(), (obj == null) ? "" : obj);
                            }
                            jsonArray.add(jsonObject);
                        }
                    }
                    return jsonArray;
                }
            } catch (SQLException e) {
                logger.error("TLE数据获取失败");
            } finally {
                release(conn, stmt, rs);
            }
        }
        return null;
    }

    //根据条件查询tle，支持模糊查询
    public JSONArray getTlesByName(String tleName) {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        JSONArray jsonArray = null;
        String sSql = "";
        if (bds != null) {
            try {
                if (!("").equalsIgnoreCase(tleName)) {
                    sSql = "SELECT \n" +
                            "    tle_id AS tid,\n" +
                            "    tle_name AS text,\n" +
                            "    tle_frow AS tfr,\n" +
                            "    tle_srow AS tsr,\n" +
                            "    tle_desc AS td\n" +
                            "FROM\n" +
                            "    tles\n" +
                            "WHERE\n" +
                            "    tle_name LIKE '%" + tleName + "%'\n" +
                            "        AND tle_name <> 'TBA - TO BE ASSIGNED' GROUP BY tle_name";
                } else {
                    sSql = "SELECT \n" +
                            "    tle_id AS tid,\n" +
                            "    tle_name AS text,\n" +
                            "    tle_frow AS tfr,\n" +
                            "    tle_srow AS tsr,\n" +
                            "    tle_desc AS td\n" +
                            "FROM\n" +
                            "    tles\n" +
                            "WHERE\n" +
                            "    tle_name <> 'TBA - TO BE ASSIGNED' GROUP BY tle_name";
                }
                conn = bds.getConnection();
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sSql);
                if (null != rs) {
                    ResultSetMetaData rsm = rs.getMetaData();
                    int count = rsm.getColumnCount();
                    if (count > 0) {
                        jsonArray = new JSONArray();
                        while (rs.next()) {
                            JSONObject jsonObject = new JSONObject();
                            for (int i = 1; i <= count; i++) {
                                Object obj = rs.getObject(i);
                                //String columnName = rsm.getColumnName(i);//列名
                                String columnName = rsm.getColumnLabel(i);//别名
                                jsonObject.put(columnName.trim(), (obj == null) ? "" : obj);
                            }
                            jsonArray.add(jsonObject);
                        }
                    }
                    return jsonArray;
                }
            } catch (SQLException e) {
                logger.error("TLE数据获取失败");
            } finally {
                release(conn, stmt, rs);
            }
        }
        return null;
    }

    //查询组中tles
    public JSONArray getTlesByGroupId(String groupId) {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        PreparedStatement preparedStmt = null;
        ResultSet rs = null;
        JSONArray jsonArray = null;
        String sSql = "SELECT \n" +
                "    ts.tle_id AS tid,\n" +
                "    ts.tle_name AS text,\n" +
                "    ts.tle_frow AS tfr,\n" +
                "    ts.tle_srow AS tsr,\n" +
                "    ts.tle_desc AS td\n" +
                "FROM\n" +
                "    tles ts\n" +
                "WHERE\n" +
                "    ts.tle_id IN (SELECT \n" +
                "            tr.tle_id\n" +
                "        FROM\n" +
                "            tlesgroupref tr\n" +
                "        WHERE\n" +
                "            tr.group_id = ?)";
        if (bds != null) {
            try {
                conn = bds.getConnection();
                preparedStmt = conn.prepareStatement(sSql);
                preparedStmt.setString(1, groupId);
                rs = preparedStmt.executeQuery();
                if (null != rs) {
                    ResultSetMetaData rsm = rs.getMetaData();
                    int count = rsm.getColumnCount();
                    if (count > 0) {
                        jsonArray = new JSONArray();
                        while (rs.next()) {
                            JSONObject jsonObject = new JSONObject();
                            for (int i = 1; i <= count; i++) {
                                Object obj = rs.getObject(i);
                                String columnName = rsm.getColumnLabel(i);
                                jsonObject.put(columnName, (obj == null) ? "" : obj);
                            }
                            jsonObject.put("leaf", true);
                            //jsonObject.put("checked", false);
                            jsonObject.put("iconCls", "cloud icon-sate");
                            jsonArray.add(jsonObject);
                        }
                    }
                    return jsonArray;
                }
            } catch (SQLException e) {
                logger.error("TLE数据获取失败");
            } finally {
                release(conn, preparedStmt, rs);
            }
        }
        return null;
    }

    //查询组
    public JSONArray getTlesGroup() {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        JSONArray jsonArray = null;
        String sSql = "SELECT \n" +
                "    tg.group_id AS gid,\n" +
                "    tg.groupname AS text,\n" +
                "    date_format(tg.groupdate, '%Y-%m-%d %H:%i:%s') AS gdate,\n" +
                "    tg.groupuser AS guser,\n" +
                "    tg.groupuserid AS guserid,\n" +
                "    tg.groupdesc AS gdesc\n" +
                "FROM\n" +
                "    tlesgroup tg";
        if (bds != null) {
            try {
                conn = bds.getConnection();
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sSql);
                if (null != rs) {
                    ResultSetMetaData rsm = rs.getMetaData();
                    int count = rsm.getColumnCount();
                    if (count > 0) {
                        jsonArray = new JSONArray();
                        while (rs.next()) {
                            JSONObject jsonObject = new JSONObject();
                            for (int i = 1; i <= count; i++) {
                                Object obj = rs.getObject(i);
                                //String columnName = rsm.getColumnName(i);//列名
                                String columnName = rsm.getColumnLabel(i);//别名
                                jsonObject.put(columnName.trim(), (obj == null) ? "" : obj);
                            }
                            jsonObject.put("leaf", true);
                            jsonObject.put("iconCls", "cloud icon-satellite");
                            jsonArray.add(jsonObject);
                        }
                    }
                    return jsonArray;
                }
            } catch (SQLException e) {
                logger.error("TLE数据获取失败");
            } finally {
                release(conn, stmt, rs);
            }
        }
        return null;
    }

    //增加组
    public boolean addTlesGroup(TleGroupInfo tleGroupInfo) {
        //1、执行tle group记录添加
        //2、执行tle group ref记录添加
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        Connection conn = null;
        PreparedStatement preparedStmt = null;
        if (jdbcTemplate != null) {
            try {
                //保存group
                int effectedGroupRow = jdbcTemplate.update("INSERT INTO tlesgroup (group_id, groupname, groupdate, groupuser, groupuserid, groupdesc) VALUES (?, ?, str_to_date(?, '%Y-%m-%d %H:%i:%s'), ?, ?, ?)", new Object[]{tleGroupInfo.getGroupId(), tleGroupInfo.getGroupName(), tleGroupInfo.getGroupDate(), tleGroupInfo.getGroupUser(), tleGroupInfo.getGroupUserId(), tleGroupInfo.getGroupDesc()});
                if (effectedGroupRow > 0) {
                    //保存tle ref
                    conn = jdbcTemplate.getDataSource().getConnection();
                    //conn.setAutoCommit(false);//事务由Spring自动管理
                    preparedStmt = conn.prepareStatement("INSERT INTO tlesgroupref (group_id, tle_id) VALUES (?, ?)");
                    String[] tleIds = tleGroupInfo.getRefTleIds();
                    String groupId = tleGroupInfo.getGroupId();
                    for (int i = 0; i < tleIds.length; i++) {
                        preparedStmt.setString(1, groupId);
                        preparedStmt.setString(2, tleIds[i]);
                        preparedStmt.addBatch();
                    }
                    // 执行剩余入库操作
                    preparedStmt.executeBatch();
                    //Commit it
                    //conn.commit();
                    return true;
                }
                return false;
            } catch (Exception e) {
                logger.error("卫星分组创建失败");
            } finally {
                release(jdbcTemplate, conn, preparedStmt);
            }
        }
        return false;
    }

    //编辑组
    public boolean editTlesGroup(TleGroupInfo tleGroupInfo) {
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        Connection conn = null;
        PreparedStatement preparedStmt = null;
        if (jdbcTemplate != null) {
            //1、更新group信息
            //2、先清空与group id对应的tles
            //3、更新关联的tles信息
            try {
                int effectedRow = jdbcTemplate.update("UPDATE tlesgroup SET groupname=?, groupdate=str_to_date(?, '%Y-%m-%d %H:%i:%s'), groupuser=?, groupuserid=?, groupdesc=? WHERE group_id=?", new Object[]{tleGroupInfo.getGroupName(), tleGroupInfo.getGroupDate(), tleGroupInfo.getGroupUser(), tleGroupInfo.getGroupUserId(), tleGroupInfo.getGroupDesc(), tleGroupInfo.getGroupId()});
                if (effectedRow > 0) {
                    effectedRow = jdbcTemplate.update("DELETE FROM tlesgroupref WHERE group_id = ?", new Object[]{tleGroupInfo.getGroupId()});
                    if (effectedRow > 0) {
                        //保存tle ref
                        conn = jdbcTemplate.getDataSource().getConnection();
                        //conn.setAutoCommit(false);//事务由Spring自动管理
                        preparedStmt = conn.prepareStatement("INSERT INTO tlesgroupref (group_id, tle_id) VALUES (?, ?)");
                        String[] tleIds = tleGroupInfo.getRefTleIds();
                        String groupId = tleGroupInfo.getGroupId();
                        for (int i = 0; i < tleIds.length; i++) {
                            preparedStmt.setString(1, groupId);
                            preparedStmt.setString(2, tleIds[i]);
                            preparedStmt.addBatch();
                        }
                        // 执行剩余入库操作
                        preparedStmt.executeBatch();
                        //Commit it
                        //conn.commit();
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                logger.error("卫星分组保存失败");
            } finally {
                release(jdbcTemplate, conn, preparedStmt);
            }
        }
        return false;
    }

    //删除组
    public boolean deleteTlesGroup(String groupId) {
        //1、删除group关联记录
        //2、删除group记录
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        if (jdbcTemplate != null) {
            try {
                int effectedRow = jdbcTemplate.update("DELETE FROM tlesgroupref WHERE group_id = ?", groupId);
                if (effectedRow > 0) {
                    effectedRow = jdbcTemplate.update("DELETE FROM tlesgroup WHERE group_id=?", groupId);
                    return effectedRow > 0;
                }
                return false;
            } catch (Exception e) {
                logger.error("TLE组删除失败");
            } finally {
                release(jdbcTemplate);
            }
        }
        return false;
    }

    /**
     * 获取中国省边界面
     *
     * @return
     */
    public JSONArray getProvinces() {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        JSONArray jsonArray = null;
        String sSql = "";
        if (bds != null) {
            try {
                conn = bds.getConnection();
                stmt = conn.createStatement();
                sSql = "SELECT \n" +
                        "    GB AS code,\n" +
                        "    PROVINCE AS name,\n" +
                        "    X AS x,\n" +
                        "    Y AS y,\n" +
                        "    ASTEXT(GEOM) AS geom\n" +
                        "FROM\n" +
                        "    chinaprovincecgcs";
                rs = stmt.executeQuery(sSql);
                if (null != rs) {
                    ResultSetMetaData rsm = rs.getMetaData();
                    int count = rsm.getColumnCount();
                    if (count > 0) {
                        jsonArray = new JSONArray();
                        while (rs.next()) {
                            JSONObject jsonObject = new JSONObject();
                            for (int i = 1; i <= count; i++) {
                                Object obj = rs.getObject(i);
                                //String columnName = rsm.getColumnName(i);//列名
                                String columnName = rsm.getColumnLabel(i);//别名
                                jsonObject.put(columnName.trim(), (obj == null) ? "" : obj);
                            }
                            jsonArray.add(jsonObject);
                        }
                    }
                }
            } catch (SQLException e) {
                logger.error("行政省界获取失败！");
            } finally {
                release(conn, stmt, rs);
            }
        }
        return jsonArray;
    }

    public JSONObject calcPos4SingleSate(String fpcs) {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        JSONObject jsonObject4Predict = new JSONObject();
        if (bds != null) {
            try {
                conn = bds.getConnection();
                stmt = conn.createStatement();

                //计算当前卫星过境省
                String sSql = "SELECT \n" +
                        "    GB AS code, PROVINCE AS name, X AS x, Y AS y\n" +
                        "FROM\n" +
                        "    chinaprovincecgcs\n" +
                        "WHERE\n" +
                        "    ST_INTERSECTS(GEOMFROMTEXT('" + fpcs + "'), GEOMFROMWKB(GEOM))=1";
                rs = stmt.executeQuery(sSql);
                if (null != rs) {
                    ResultSetMetaData rsm = rs.getMetaData();
                    int count = rsm.getColumnCount();
                    if (count > 0) {
                        JSONArray regions = new JSONArray();
                        while (rs.next()) {
                            //区域（省）
                            JSONObject region = new JSONObject();
                            for (int j = 1; j <= count; j++) {
                                Object obj = rs.getObject(j);
                                String columnName = rsm.getColumnLabel(j);//别名
                                region.put(columnName.trim(), (obj == null) ? "" : obj);
                            }
                            regions.add(region);
                        }
                        jsonObject4Predict.put("regions", regions);
                    }
                } else {
                    jsonObject4Predict.put("regions", null);
                }
                return jsonObject4Predict;
            } catch (SQLException e) {
                logger.error("单颗卫星分析失败");
            } finally {
                release(conn, stmt, rs);
            }
        }
        return null;
    }

    /**
     * 根据预测卫星查询过境省
     * 执行效率待提高
     *
     * @param satePosList
     * @return
     */
    public JSONObject spatialAnalysis4Sate(JSONArray satePosList) {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String sSql = "";
        JSONObject jsonObject4Predict = new JSONObject();
        //存放空间分析结果
        JSONArray jsonArray4Regions = new JSONArray();
        //存放全部过境卫星参数
        JSONArray jsonArray4Sates = new JSONArray();
        Hashtable regionsHashTable = new Hashtable();
        if (bds != null && satePosList != null && satePosList.size() > 0) {
            try {
                conn = bds.getConnection();
                stmt = conn.createStatement();
                int satPosSize = satePosList.size();
                for (int i = 0; i < satPosSize; i++) {
                    JSONObject sate = satePosList.getJSONObject(i);
                    //卫星名称
                    String sateName = sate.getString("sateName");
                    //卫星位置参数
                    JSONObject satePos = sate.getJSONObject("satPos");
                    String rangeCircleWkt = sate.getString("rangeCircleWkt");

                    //全部参与预测卫星
                    JSONObject passedSate = new JSONObject();
                    passedSate.put("sateName", sateName);
                    passedSate.put("rangeCircle", sate.getJSONArray("rangeCircle"));
                    passedSate.put("elevation", satePos.getDoubleValue("elevation"));
                    passedSate.put("phase", satePos.getDoubleValue("phase"));
                    passedSate.put("altitude", satePos.getDoubleValue("altitude"));
                    passedSate.put("aboveHorizon", satePos.getBooleanValue("aboveHorizon"));
                    passedSate.put("latitude", satePos.getDoubleValue("latitude"));
                    passedSate.put("range", satePos.getDoubleValue("range"));
                    passedSate.put("azimuth", satePos.getDoubleValue("azimuth"));
                    passedSate.put("theta", satePos.getDoubleValue("theta"));
                    passedSate.put("rangeRate", satePos.getDoubleValue("rangeRate"));
                    passedSate.put("rangeCircleRadiusKm", satePos.getDoubleValue("rangeCircleRadiusKm"));
                    passedSate.put("time", satePos.getString("time"));
                    passedSate.put("longitude", satePos.getDoubleValue("longitude"));
                    jsonArray4Sates.add(passedSate);
                    logger.info(sateName);

                    //空间分析卫星过境省
                    sSql = "SELECT \n" +
                            "    GB AS code, PROVINCE AS name, X AS x, Y AS y\n" +
                            "FROM\n" +
                            "    chinaprovincecgcs\n" +
                            "WHERE\n" +
                            "    ST_INTERSECTS(GEOMFROMTEXT('" + rangeCircleWkt + "'), GEOMFROMWKB(GEOM))=1";
                    rs = stmt.executeQuery(sSql);
                    if (null != rs) {
                        ResultSetMetaData rsm = rs.getMetaData();
                        int count = rsm.getColumnCount();
                        if (count > 0) {
                            while (rs.next()) {
                                //区域（省）
                                JSONObject region = new JSONObject();
                                for (int j = 1; j < count; j++) {
                                    Object obj = rs.getObject(j);
                                    String columnName = rsm.getColumnLabel(j);//别名
                                    region.put(columnName.trim(), (obj == null) ? "" : obj);
                                }
                                //省名
                                String regionName = region.getString("name");
                                if (regionsHashTable.containsKey(regionName)) {
                                    region = (JSONObject) regionsHashTable.get(regionName);
                                } else {
                                    regionsHashTable.put(regionName, region);
                                }

                                if (region.getJSONArray("regionSates") == null) {
                                    region.put("regionSates", new JSONArray());
                                }

                                //当前省过境卫星，仅保存名称
                                JSONObject passedSate4Region = new JSONObject();
                                passedSate4Region.put("sateName", sateName);

                                if (!region.getJSONArray("regionSates").contains(passedSate4Region)) {
                                    region.getJSONArray("regionSates").add(passedSate4Region);
                                }
                            }
                        }
                    }
                }

                //组装分析结果
                jsonObject4Predict.put("satellites", jsonArray4Sates);
                if (!regionsHashTable.keySet().isEmpty()) {
                    /*//遍历hashtable的两种方式，二选一即可
                    //遍历key
                    Enumeration e = regionsHashTable.keys();
                    while (e.hasMoreElements()) {
                        System.out.println(e.nextElement());
                    }

                    //遍历value
                    e = regionsHashTable.elements();
                    while (e.hasMoreElements()) {
                        System.out.println(e.nextElement());
                    }*/

                    Enumeration e = regionsHashTable.elements();
                    while (e.hasMoreElements()) {
                        jsonArray4Regions.add(e.nextElement());
                    }
                    jsonObject4Predict.put("regions", jsonArray4Regions);
                } else {
                    jsonObject4Predict.put("regions", null);
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
            } finally {
                regionsHashTable.clear();
                release(conn, stmt, rs);
            }
        }
        return jsonObject4Predict;
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
                        if (o instanceof BasicDataSource) {
                            ((BasicDataSource) o).close();
                        } else if (o instanceof ResultSet) {
                            ((ResultSet) o).close();
                        } else if (o instanceof Statement) {
                            ((Statement) o).close();
                        } else if (o instanceof Connection) {
                            ((Connection) o).close();
                        } else if (o instanceof PreparedStatement) {
                            ((PreparedStatement) o).close();
                        } else if (o instanceof JdbcTemplate) {
                            ((JdbcTemplate) o).getDataSource().getConnection().close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
