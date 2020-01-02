/**
 *
 */
package com.acs.service.impl;

import com.acs.common.AcsConstants;
import com.acs.dao.IBaseDao;
import com.acs.model.database.DBConnectInfo;
import com.acs.service.IBaseService;
import com.acs.util.db.DbUtils;
import com.acs.util.db.Table;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author LBM
 * @time 2017年12月1日
 * @project codecreator
 * @type DataBaseService
 * @desc 数据库操作组件类
 */
@Service("dbs")
@Transactional(rollbackFor = Exception.class)
public class DataBaseServiceImpl implements IBaseService {

    @Autowired
    @Qualifier("dbDao")
    private IBaseDao dbDao;

    /*
     * (non-Javadoc)
     *
     * @see codecreator.common.IBaseService#getTableInfoEx()
     */
    @Override
    public List<Table> getTableInfoEx(DBConnectInfo dbci) {
        // 初始化连接池
        BasicDataSource bds = new BasicDataSource();
        // 将JDBC建立连接所需要的信息设置到连接池中
        bds.setDriverClassName(dbci.getDbType());
        if (dbci.getDbType().indexOf("oracle") > -1) {
            bds.setUrl(DbUtils.buildUrl(dbci.getDbAddress(), dbci.getDbPort(), dbci.getDbName(), AcsConstants.ORACLE));
            bds.setConnectionProperties("remarksReporting=true");
        } else if (dbci.getDbType().indexOf("mysql") > -1) {
            bds.setUrl(DbUtils.buildUrl(dbci.getDbAddress(), dbci.getDbPort(), dbci.getDbName(), AcsConstants.MYSQL));
        } else if (dbci.getDbType().indexOf("postgresql") > -1) {
            bds.setUrl(DbUtils.buildUrl(dbci.getDbAddress(), dbci.getDbPort(), dbci.getDbName(), AcsConstants.POSTGRESQL));
        }
        bds.setUsername(dbci.getDbUser());
        bds.setPassword(dbci.getDbPassword());
        bds.setRemoveAbandonedTimeout(300);// 单位秒
        bds.setMaxActive(100);
        bds.setMaxWait(60000);

        //设置数据源
        dbDao.setDbSource(bds);
        return dbDao.queryTableEx(dbci);
    }

    /*
     * (non-Javadoc)
     *
     * @see codecreator.common.IBaseService#doTestDB()
     */
    @Override
    public boolean doTestDB(DBConnectInfo dbci) {
        // 初始化连接池
        BasicDataSource bds = new BasicDataSource();
        // 将JDBC建立连接所需要的信息设置到连接池中
        bds.setDriverClassName(dbci.getDbType());
        if (dbci.getDbType().indexOf("oracle") > -1) {
            bds.setUrl(DbUtils.buildUrl(dbci.getDbAddress(), dbci.getDbPort(), dbci.getDbName(), AcsConstants.ORACLE));
        } else if (dbci.getDbType().indexOf("mysql") > -1) {
            bds.setUrl(DbUtils.buildUrl(dbci.getDbAddress(), dbci.getDbPort(), dbci.getDbName(), AcsConstants.MYSQL));
        } else if (dbci.getDbType().indexOf("postgresql") > -1) {
            bds.setUrl(DbUtils.buildUrl(dbci.getDbAddress(), dbci.getDbPort(), dbci.getDbName(), AcsConstants.POSTGRESQL));
        }
        bds.setUsername(dbci.getDbUser());
        bds.setPassword(dbci.getDbPassword());
        bds.setMaxActive(100);
        bds.setMaxWait(60000);

        dbDao.setDbSource(bds);
        return dbDao.testDb(dbci);
    }

    /*
     * (non-Javadoc)
     *
     * @see codecreator.common.IBaseService#saveProjectConfig(java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean saveProjectConfig(String projectName, String projectConfig) {
        dbDao.setDbSource(null);
        return dbDao.insertProject(projectName, projectConfig);
    }

    @Override
    public JSONArray getDataBases() {
        dbDao.setDbSource(null);
        return dbDao.selectDataBase();
    }

    /*
     * (non-Javadoc)
     *
     * @see codecreator.common.IBaseService#getProjects()
     */
    @Override
    public JSONArray getProjects() {
        dbDao.setDbSource(null);
        return dbDao.selectProject();
    }

    @Override
    public JSONArray getApiGroups() {
        dbDao.setDbSource(null);
        return dbDao.selectApiGroup();
    }

    @Override
    public boolean saveApiGroup(String apiGroupName) {
        dbDao.setDbSource(null);
        return dbDao.insertApiGroup(apiGroupName);
    }

    /* (non-Javadoc)
     * @see com.jzac.service.IBaseService#deleteProject(java.lang.String)
     */
    @Override
    public boolean deleteProject(String projectName) {
        dbDao.setDbSource(null);
        return dbDao.deleteProject(projectName);
    }

    @Override
    public boolean deleteDataBase(String dbDesc) {
        dbDao.setDbSource(null);
        return dbDao.deleteDataBase(dbDesc);
    }

    @Override
    public boolean deleteApiGroup(String groupName) {
        dbDao.setDbSource(null);
        return dbDao.deleteApiGroup(groupName);
    }
}
