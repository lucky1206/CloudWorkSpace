package com.acs.util.db;

import com.acs.common.AcsConstants;
import com.acs.listener.EnvironmentInitListener;
import com.acs.model.database.DBConnectInfo;
import com.acs.model.file.FileInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据处理工具类，该类并未用到JdbcTemplate类，建议采用JdbcTemplate简化数据crud操作
 */
public class DataProcessSupport extends JdbcDaoSupport {
    private Logger logger = LogManager.getLogger(DataProcessSupport.class);

    /**
     * 构造函数
     *
     * @param dbci 数据库配置
     */
    public DataProcessSupport(DBConnectInfo dbci) {
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
     * 创建文件上传记录表
     *
     * @return
     */
    public boolean createUploadTable() {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        Statement stmt = null;
        boolean isSuccess = false;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                stmt = conn.createStatement();

                StringBuffer sb = new StringBuffer();
                sb.append("CREATE TABLE IF NOT EXISTS uploaddatas (");
                sb.append("dbname VARCHAR(45) DEFAULT NULL COMMENT '关联数据库名称',");
                sb.append("tablename VARCHAR(45) DEFAULT NULL COMMENT '关联数据表名称',");
                sb.append("filename VARCHAR(100) DEFAULT NULL COMMENT '文件名称',");
                sb.append("filepath VARCHAR(150) DEFAULT NULL COMMENT '服务器存放路径',");
                sb.append("filetype VARCHAR(45) DEFAULT NULL COMMENT '文件类型',");
                sb.append("filesize DOUBLE DEFAULT NULL COMMENT '数据大小',");
                sb.append("uploaddate DATETIME DEFAULT NULL COMMENT '数据上传日期',");
                sb.append("state int(2) DEFAULT NULL COMMENT '数据处理状态',");
                sb.append("filedesc MEDIUMTEXT COMMENT '元数据',");
                //sb.append("ctsql MEDIUMTEXT COMMENT '建表sql',");
                //sb.append("UNIQUE KEY filename_UNIQUE (filename)");//todo 2019-07-01 限定重复文件名不能上传
                sb.append("ctsql MEDIUMTEXT COMMENT '建表sql',");//不限定
                sb.append("fileid int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',");
                sb.append("PRIMARY KEY (fileid)");
                sb.append(")  ENGINE=INNODB DEFAULT CHARSET=UTF8 COMMENT='上传文件资源中心'");
                stmt.execute(sb.toString());
                isSuccess = true;
                logger.info("文件上传记录表创建成功！");
            } catch (SQLException s) {
                logger.error("文件上传记录表创建失败！");
            } finally {
                release(conn, stmt);
            }
        }
        return isSuccess;
    }

    /**
     * 创建要素属性表
     *
     * @return
     */
    public boolean createFeatureTable(String ctSql) {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        Statement stmt = null;
        boolean isSuccess = false;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                stmt = conn.createStatement();
                stmt.execute(ctSql);
                isSuccess = true;
                logger.info("要素表创建成功！");
            } catch (SQLException s) {
                logger.error("要素表创建失败！");
            } finally {
                release(conn, stmt);
            }
        }
        return isSuccess;
    }

    /**
     * shp文件元数据入库
     *
     * @param fileInfo
     * @return
     */
    public boolean recordFileInfo(FileInfo fileInfo) {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        PreparedStatement preparedStmt = null;
        boolean isSuccess = false;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                String iSql = "INSERT INTO uploaddatas (dbname,tablename, filename, filepath, filetype, filesize, uploaddate, state) VALUES (?,?, ?, ?, ?, ?, str_to_date(?, '%Y-%m-%d %H:%i:%s'), ?)";
                preparedStmt = conn.prepareStatement(iSql);
                preparedStmt.setString(1, fileInfo.getDbName());
                preparedStmt.setString(2, fileInfo.getTableName());
                preparedStmt.setString(3, fileInfo.getFileName());
                preparedStmt.setString(4, fileInfo.getFilePath());
                preparedStmt.setString(5, fileInfo.getFileType());
                preparedStmt.setDouble(6, fileInfo.getFileSize());
                preparedStmt.setString(7, fileInfo.getUploadDate());
                preparedStmt.setInt(8, fileInfo.getFileState());
                // 执行操作
                int affectedRecords = preparedStmt.executeUpdate();
                if (affectedRecords > 0) {
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
                logger.info(fileInfo.getFileName() + "文件上传记录保存成功！");
            } catch (SQLException s) {
                logger.error("文件上传记录保存失败！");
            } finally {
                release(conn, preparedStmt);
            }
        }
        return isSuccess;
    }

    public boolean recordFileData(String tableName, String fileDesc, JSONArray dataArray, long totalCounter, int srid) throws SQLException {
        boolean isSuccess = false;
        //动态组装INSERT语句
        net.sf.json.JSONArray fields = net.sf.json.JSONArray.fromObject(fileDesc);
        if (fields != null) {
            List fieldNames = new ArrayList<String>();
            StringBuffer stringBuffer = new StringBuffer();
            int size = fields.size();
            String fieldStr = "";
            String valueStr = "";
            for (int i = 0; i < size; i++) {
                net.sf.json.JSONObject jsonObject = fields.getJSONObject(i);
                String name = jsonObject.getString("name");
                String type = jsonObject.getString("type");
                fieldStr += name + ",";
                if (type.equalsIgnoreCase("datetime")) {
                    valueStr += "str_to_date(?, '%Y-%m-%d %H:%i:%s'),";
                } else {
                    valueStr += "?,";
                }

                fieldNames.add(name);
            }

            //附加字段
            if (!fieldNames.contains("AREA")) {
                fieldStr += "AREA,";
                valueStr += "?,";
            }
            if (!fieldNames.contains("X")) {
                fieldStr += "X,";
                valueStr += "?,";
            }
            if (!fieldNames.contains("Y")) {
                fieldStr += "Y,";
                valueStr += "?,";
            }
            if (!fieldNames.contains("FUID")) {
                fieldStr += "FUID,";
                valueStr += "?,";
            }

            //geometry
            fieldStr += "GEOM";
            valueStr += "ST_GeomFromText(?," + srid + ")";

            stringBuffer.append("INSERT INTO " + tableName + " (" + fieldStr + ") VALUES (" + valueStr + ")");

            //执行数据插入
            BasicDataSource bds = (BasicDataSource) this.getDataSource();
            Connection conn = null;
            PreparedStatement preparedStmt = null;
            if (bds != null) {
                try {
                    conn = bds.getConnection();
                    String iSql = stringBuffer.toString();
                    preparedStmt = conn.prepareStatement(iSql);
                    conn.setAutoCommit(false);
                    //批量插入组装
                    int dataSize = dataArray.size();
                    for (int i = 0; i < dataSize; i++) {
                        int fieldCounter = 0;
                        JSONObject dataJson = dataArray.getJSONObject(i);
                        for (int j = 0; j < size; j++) {
                            net.sf.json.JSONObject fieldJson = fields.getJSONObject(j);
                            String name = fieldJson.getString("name");
                            String type = fieldJson.getString("type");
                            fieldCounter = fieldCounter + 1;
                            if (type.equalsIgnoreCase("double")) {
                                preparedStmt.setDouble(fieldCounter, dataJson.getDouble(name));
                            } else if (type.equalsIgnoreCase("int(32)") || type.equalsIgnoreCase("int(64)")) {
                                preparedStmt.setInt(fieldCounter, dataJson.getIntValue(name));
                            } else {
                                preparedStmt.setString(fieldCounter, dataJson.getString(name));
                            }
                        }

                        //附加字段
                        if (!fieldNames.contains("AREA")) {
                            fieldCounter = fieldCounter + 1;
                            preparedStmt.setDouble(fieldCounter, dataJson.getDouble("AREA"));
                        }
                        if (!fieldNames.contains("X")) {
                            fieldCounter = fieldCounter + 1;
                            preparedStmt.setDouble(fieldCounter, dataJson.getDouble("X"));
                        }
                        if (!fieldNames.contains("Y")) {
                            fieldCounter = fieldCounter + 1;
                            preparedStmt.setDouble(fieldCounter, dataJson.getDouble("Y"));
                        }
                        if (!fieldNames.contains("FUID")) {
                            fieldCounter = fieldCounter + 1;
                            preparedStmt.setString(fieldCounter, dataJson.getString("FUID"));
                        }

                        fieldCounter = fieldCounter + 1;
                        preparedStmt.setString(fieldCounter, dataJson.getString("GEOM"));
                        preparedStmt.addBatch();

                        if (i % 10 == 0) {
                            //中间批处理入库操作
                            preparedStmt.executeBatch();
                            preparedStmt.clearBatch();
                            logger.info(tableName + "已入库" + (totalCounter + i) + "条记录");
                        }
                    }

                    // 执行剩余入库操作
                    preparedStmt.executeBatch();
                    //Commit it
                    conn.commit();
                    isSuccess = true;
                    logger.info(tableName + "已入库" + (totalCounter + dataSize) + "条记录");
                } catch (SQLException s) {
                    conn.rollback();
                    logger.error("入库失败！");
                } finally {
                    release(conn, preparedStmt);
                }
            }
        }
        return isSuccess;
    }

    /**
     * 获取文件元数据列表
     *
     * @param state
     * @return
     */
    public JSONArray getFileInfoList(int state) {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        JSONArray jsonArray = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                stmt = conn.createStatement();
                String sSql = "";
                if (state == 2) {
                    sSql = "SELECT \n" +
                            "    filename,\n" +
                            "    dbname,\n" +
                            "    tablename,\n" +
                            "    filepath,\n" +
                            "    filetype,\n" +
                            "    filesize,\n" +
                            "    uploaddate,\n" +
                            "    state,\n" +
                            "    filedesc,\n" +
                            "    ctsql,\n" +
                            "    CONVERT( filesize / (1024 * 1024) , DECIMAL (10 , 3 )) AS fsize,\n" +
                            "    DATE_FORMAT(uploaddate, '%Y-%m-%d %H:%i:%s') AS fdate,\n" +
                            "    (CASE state\n" +
                            "        WHEN (- 1) THEN '未入库'\n" +
                            "        WHEN 2 THEN '已入库'\n" +
                            "    END) AS fstate\n" +
                            "FROM\n" +
                            "    uploaddatas \n" +
                            "ORDER BY uploaddate DESC";
                } else {
                    sSql = "SELECT \n" +
                            "    filename,\n" +
                            "    dbname,\n" +
                            "    tablename,\n" +
                            "    filepath,\n" +
                            "    filetype,\n" +
                            "    filesize,\n" +
                            "    uploaddate,\n" +
                            "    state,\n" +
                            "    filedesc,\n" +
                            "    ctsql,\n" +
                            "    CONVERT( filesize / (1024 * 1024) , DECIMAL (10 , 3 )) AS fsize,\n" +
                            "    DATE_FORMAT(uploaddate, '%Y-%m-%d %H:%i:%s') AS fdate,\n" +
                            "    (CASE state\n" +
                            "        WHEN (- 1) THEN '未入库'\n" +
                            "        WHEN 2 THEN '已入库'\n" +
                            "    END) AS fstate\n" +
                            "FROM\n" +
                            "    uploaddatas\n" +
                            "WHERE\n" +
                            "    state = " + state + "\n" +
                            "ORDER BY uploaddate DESC";
                }
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
                                String columnName = rsm.getColumnName(i);
                                jsonObject.put(columnName.toLowerCase(), (obj == null) ? "" : obj);
                            }
                            jsonArray.add(jsonObject);
                        }
                    }

                }
            } catch (SQLException e) {
                logger.error(e.getMessage() + "文件记录清单获取失败！");
            } finally {
                release(conn, stmt, rs);
            }
        }
        return jsonArray;
    }

    //获取字典数据
    public JSONArray getDictInfoList(String dictType) {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        PreparedStatement preparedStmt = null;
        ResultSet rs = null;
        JSONArray jsonArray = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                String sSql = "SELECT * FROM dictionarys where dicttype=? order by dictname desc";
                preparedStmt = conn.prepareStatement(sSql);
                preparedStmt.setString(1, dictType);
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
                                String columnName = rsm.getColumnName(i);
                                jsonObject.put(columnName.toLowerCase(), (obj == null) ? "" : obj);
                            }
                            jsonArray.add(jsonObject);
                        }
                    }

                }
            } catch (SQLException e) {
                logger.error(e.getMessage() + "字典数据获取失败！");
            } finally {
                release(conn, preparedStmt, rs);
            }
        }
        return jsonArray;
    }

    /**
     * 更新shp文件元数据
     *
     * @param fileName
     * @param metaData
     * @param ctSql
     * @return
     */
    public boolean updateFileMeta(String fileName, String metaData, String ctSql) {
        boolean isSuccess = false;
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        PreparedStatement preparedStmt = null;
        Connection conn = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                String updateSql = "UPDATE uploaddatas SET filedesc=?,ctsql=? WHERE filename=?";
                preparedStmt = conn.prepareStatement(updateSql);
                preparedStmt.setString(1, metaData);
                preparedStmt.setString(2, ctSql);
                preparedStmt.setString(3, fileName);
                int affectedRecords = preparedStmt.executeUpdate();
                if (affectedRecords > 0) {
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
            } catch (SQLException e) {
                logger.error("上传shp文件元数据更新失败");
            } finally {
                release(conn, preparedStmt);
            }
        }
        return isSuccess;
    }

    public boolean updateFileState(String fileName) {
        boolean isSuccess = false;
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        PreparedStatement preparedStmt = null;
        Connection conn = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                String updateSql = "UPDATE uploaddatas SET state=? WHERE filename=?";
                preparedStmt = conn.prepareStatement(updateSql);
                preparedStmt.setInt(1, 2);
                preparedStmt.setString(2, fileName);
                int affectedRecords = preparedStmt.executeUpdate();
                if (affectedRecords > 0) {
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
            } catch (SQLException e) {
                logger.error("上传shp文件处理状态更新");
            } finally {
                release(conn, preparedStmt);
            }
        }
        return isSuccess;
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
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
