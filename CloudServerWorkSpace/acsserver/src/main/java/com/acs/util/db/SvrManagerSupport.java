package com.acs.util.db;

import com.acs.common.AcsConstants;
import com.acs.listener.EnvironmentInitListener;
import com.acs.model.database.DBConnectInfo;
import com.acs.model.svrreg.SvrCatalog;
import com.acs.model.svrreg.SvrInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.*;

public class SvrManagerSupport extends JdbcDaoSupport {
    private Logger logger = LogManager.getLogger(this.getClass());

    /**
     * 构造函数
     *
     * @param dbci 数据库配置
     */
    public SvrManagerSupport(DBConnectInfo dbci) {
        if (dbci == null) {
            BasicDataSource dataSource = (BasicDataSource) EnvironmentInitListener.getTarget(AcsConstants.DATA_BASE);
            super.setDataSource(dataSource);
        } else {
            // 初始化连接池
            BasicDataSource bds = new BasicDataSource();
            // 将JDBC建立连接所需要的信息设置到连接池中
            bds.setDriverClassName(dbci.getDbType());
            if (dbci.getDbType().indexOf("oracle") > -1) {
                bds.setUrl(SvrManagerSupport.buildUrl(dbci.getDbAddress(), dbci.getDbPort(), dbci.getDbName(), AcsConstants.ORACLE));
                bds.setConnectionProperties("remarksReporting=true");
            } else if (dbci.getDbType().indexOf("mysql") > -1) {
                bds.setUrl(SvrManagerSupport.buildUrl(dbci.getDbAddress(), dbci.getDbPort(), dbci.getDbName(), AcsConstants.MYSQL));
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
     * 初始化服务注册相关表
     *
     * @return
     */
    public boolean initSvrRegedit() {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        if (bds != null) {
            //通过数据库模板自动管理连接
            JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
            if (jdbcTemplate != null) {
                try {
                    //创建目录树表
                    String cloudcatalog = "CREATE TABLE IF NOT EXISTS cloudcatalog (\n" +
                            "  catalog_id varchar(45) NOT NULL COMMENT 'ID',\n" +
                            "  node_id varchar(45) DEFAULT NULL COMMENT '节点ID',\n" +
                            "  node_pid varchar(45) DEFAULT NULL COMMENT '父节点ID',\n" +
                            "  nodename varchar(45) DEFAULT NULL COMMENT '节点名称',\n" +
                            "  node_level int(11) DEFAULT NULL COMMENT '节点级别',\n" +
                            "  PRIMARY KEY (catalog_id),\n" +
                            "  UNIQUE KEY catalog_id_UNIQUE (catalog_id)\n" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务目录树'";
                    //创建服务表
                    String cloudservice = "CREATE TABLE cloudservice (\n" +
                            "  services_id varchar(45) NOT NULL COMMENT 'ID',\n" +
                            "  catalog_id varchar(45) DEFAULT NULL COMMENT '服务父节点ID',\n" +
                            "  svrname varchar(45) DEFAULT NULL COMMENT '服务名称',\n" +
                            "  svrlayername varchar(45) DEFAULT NULL COMMENT '服务图层名',\n" +
                            "  svrtype varchar(45) DEFAULT NULL COMMENT '服务类型',\n" +
                            "  svrurl text COMMENT '服务地址',\n" +
                            "  svrsrid varchar(45) DEFAULT NULL COMMENT '坐标系ID',\n" +
                            "  cx double DEFAULT NULL COMMENT '地图服务范围中心经度',\n" +
                            "  cy double DEFAULT NULL COMMENT '地图服务范围中心纬度',\n" +
                            "  west double DEFAULT NULL COMMENT '地图服务范围最小经度',\n" +
                            "  south double DEFAULT NULL COMMENT '地图服务范围最小纬度',\n" +
                            "  east double DEFAULT NULL COMMENT '地图服务范围最大经度',\n" +
                            "  north double DEFAULT NULL COMMENT '地图服务范围最大纬度',\n" +
                            "  svrDate datetime DEFAULT NULL COMMENT '服务注册时间',\n" +
                            "  svrprovider varchar(45) DEFAULT NULL COMMENT '服务注册者',\n" +
                            "  svrproviderid varchar(45) DEFAULT NULL COMMENT '服务注册者ID',\n" +
                            "  svrdescription text COMMENT '服务描述',\n" +
                            "  PRIMARY KEY (services_id),\n" +
                            "  UNIQUE KEY services_id_UNIQUE (services_id)\n" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='数据服务表'";
                    jdbcTemplate.execute(cloudcatalog);
                    jdbcTemplate.execute(cloudservice);
                    return true;
                } catch (Exception e) {
                    logger.error("初始化服务注册环境失败或环境已注册");
                } finally {
                    release(jdbcTemplate);
                }
            }
        }
        return false;
    }

    /**
     * 获取服务目录树节点最大深度
     *
     * @return
     */
    public int getMaxTreeNodeLevel() {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        JSONArray jsonArray = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                String sSql = "SELECT \n" +
                        "    MAX(node_level) AS maxLevel\n" +
                        "FROM\n" +
                        "    cloudcatalog";
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
                                String columnName = rsm.getColumnLabel(i);
                                jsonObject.put(columnName, (obj == null) ? "" : obj);
                            }
                            jsonArray.add(jsonObject);
                        }
                    }
                }
            } catch (SQLException e) {
                logger.error(e.getMessage() + "服务目录获取失败！");
            } finally {
                release(conn, stmt, rs);
            }
        }
        if (jsonArray != null && jsonArray.size() > 0) {
            JSONObject jo = jsonArray.getJSONObject(0);
            return jo.getIntValue("maxLevel");
        }
        return 0;
    }

    /**
     * 获取服务目录树所有节点
     *
     * @return
     */
    public JSONArray getAllTreeNodes() {
        BasicDataSource bds = (BasicDataSource) this.getDataSource();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        JSONArray jsonArray = null;
        if (bds != null) {
            try {
                conn = bds.getConnection();
                String sSql = "SELECT \n" +
                        "    c.catalog_id AS catalogId,\n" +
                        "    c.node_id AS nodeId,\n" +
                        "    c.node_pid AS parentNodeId,\n" +
                        "    c.nodename AS nodeName,\n" +
                        "    c.node_level AS nodeLevel,\n" +
                        "    s.services_id AS svrId,\n" +
                        "    s.svrname AS svrName,\n" +
                        "    s.svrlayername AS svrLayerName,\n" +
                        "    s.svrtype AS svrType,\n" +
                        "    s.svrsrid AS svrSrid,\n" +
                        "    s.cx AS cx,\n" +
                        "    s.cy AS cy,\n" +
                        "    s.west AS west,\n" +
                        "    s.south AS south,\n" +
                        "    s.east AS east,\n" +
                        "    s.north AS north,\n" +
                        "    s.svrurl AS svrUrl,\n" +
                        "    DATE_FORMAT(s.svrDate, '%Y-%m-%d %H:%i:%s') AS svrDate,\n" +
                        "    s.svrprovider AS svrProvider,\n" +
                        "    s.svrproviderid AS svrProviderId,\n" +
                        "    s.svrdescription AS svrDesc\n" +
                        "FROM\n" +
                        "    cloudcatalog c\n" +
                        "        LEFT JOIN\n" +
                        "    cloudservice s ON s.catalog_id = c.catalog_id\n" +
                        "ORDER BY c.node_level";
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
                                String columnName = rsm.getColumnLabel(i);
                                jsonObject.put(columnName, (obj == null) ? "" : obj);
                            }
                            jsonArray.add(jsonObject);
                        }
                    }

                }
            } catch (SQLException e) {
                logger.error(e.getMessage() + "服务目录获取失败！");
            } finally {
                release(conn, stmt, rs);
            }
        }
        return jsonArray;
    }

    /**
     * 新建服务分组项
     *
     * @param svrCatalog 分组项目配置实例
     * @return
     */
    public boolean addCatalog(SvrCatalog svrCatalog) {
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        if (jdbcTemplate != null) {
            try {
                int effectedRow = jdbcTemplate.update("INSERT INTO cloudcatalog (catalog_id, node_id, node_pid, nodename, node_level) VALUES (?, ?, ?, ?, ?)", new Object[]{svrCatalog.getCatalogId(), svrCatalog.getNodeId(), svrCatalog.getNodePid(), svrCatalog.getNodeName(), svrCatalog.getNodeLevel()});
                return effectedRow > 0;
            } catch (Exception e) {
                logger.error("服务分组配置失败");
            } finally {
                release(jdbcTemplate);
            }
        }
        return false;
    }

    /**
     * 编辑服务组名称
     *
     * @param svrCatalog
     * @return
     */
    public boolean editCatalog(SvrCatalog svrCatalog) {
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        if (jdbcTemplate != null) {
            try {
                int effectedRow = jdbcTemplate.update("UPDATE cloudcatalog SET nodename=? WHERE catalog_id=?", new Object[]{svrCatalog.getNodeName(), svrCatalog.getCatalogId()});
                return effectedRow > 0;
            } catch (Exception e) {
                logger.error("服务分组编辑失败");
            } finally {
                release(jdbcTemplate);
            }
        }
        return false;
    }

    /**
     * 删除服务组
     *
     * @param svrCatalog
     * @return
     */
    public boolean deleteCatalog(SvrCatalog svrCatalog) {
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        if (jdbcTemplate != null) {
            try {
                int effectedRow = jdbcTemplate.update("DELETE FROM cloudcatalog WHERE catalog_id=?", svrCatalog.getCatalogId());
                return effectedRow > 0;
            } catch (Exception e) {
                logger.error("服务分组删除失败");
            } finally {
                release(jdbcTemplate);
            }
        }
        return false;
    }

    /**
     * 新建服务
     *
     * @param svrInfo
     * @return
     */
    public boolean addService(SvrInfo svrInfo) {
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        if (jdbcTemplate != null) {
            try {
                int effectedRow = jdbcTemplate.update("INSERT INTO cloudservice (services_id, catalog_id, svrname, svrlayername, svrtype, svrsrid, cx, cy, west, south, east, north, svrurl, svrDate, svrprovider, svrproviderid, svrdescription) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?)", new Object[]{svrInfo.getServiceId(), svrInfo.getCatalogId(), svrInfo.getSvrName(), svrInfo.getSvrLayerName(), svrInfo.getSvrType(), svrInfo.getSvrSrid(), svrInfo.getCx(), svrInfo.getCy(), svrInfo.getWest(), svrInfo.getSouth(), svrInfo.getEast(), svrInfo.getNorth(), svrInfo.getSvrUrl(), svrInfo.getSvrDate(), svrInfo.getSvrProvider(), svrInfo.getSvrProviderid(), svrInfo.getSvrDescription()});
                return effectedRow > 0;
            } catch (Exception e) {
                logger.error("新建服务失败");
            } finally {
                release(jdbcTemplate);
            }
        }
        return false;
    }

    /**
     * 编辑服务
     *
     * @param svrInfo
     * @return
     */
    public boolean editService(SvrInfo svrInfo) {
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        if (jdbcTemplate != null) {
            try {
                int effectedRow = jdbcTemplate.update("UPDATE cloudservice SET svrname=?, svrlayername=?, svrtype=?, svrsrid=?, cx=?,cy=?,west=?,south=?,east=?,north=?, svrurl=?, svrDate=str_to_date(?, '%Y-%m-%d %H:%i:%s'), svrprovider=?, svrproviderid=?, svrdescription=? WHERE services_id=?", new Object[]{svrInfo.getSvrName(), svrInfo.getSvrLayerName(), svrInfo.getSvrType(), svrInfo.getSvrSrid(), svrInfo.getCx(), svrInfo.getCy(), svrInfo.getWest(), svrInfo.getSouth(), svrInfo.getEast(), svrInfo.getNorth(), svrInfo.getSvrUrl(), svrInfo.getSvrDate(), svrInfo.getSvrProvider(), svrInfo.getSvrProviderid(), svrInfo.getSvrDescription(), svrInfo.getServiceId()});
                return effectedRow > 0;
            } catch (Exception e) {
                logger.error("服务编辑失败");
            } finally {
                release(jdbcTemplate);
            }
        }
        return false;
    }

    /**
     * 删除服务
     *
     * @param svrInfo
     * @return
     */
    public boolean deleteService(SvrInfo svrInfo) {
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        if (jdbcTemplate != null) {
            try {
                int effectedRow = jdbcTemplate.update("DELETE FROM cloudservice WHERE services_id=?", svrInfo.getServiceId());
                return effectedRow > 0;
            } catch (Exception e) {
                logger.error("服务删除失败");
            } finally {
                release(jdbcTemplate);
            }
        }

        return false;
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
