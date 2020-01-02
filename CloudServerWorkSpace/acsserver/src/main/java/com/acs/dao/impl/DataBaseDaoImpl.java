/**
 *
 */
package com.acs.dao.impl;

import com.acs.dao.IBaseDao;
import com.acs.model.database.DBConnectInfo;
import com.acs.util.db.DbUtils;
import com.acs.util.db.Table;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author LBM<br               />
 * @time 2017年12月1日<br               />
 * @project： codecreator<br               />
 * @type： DataBaseDaoImpl <br/>
 * @desc： 【这里描述类型功能】
 */
@Repository("dbDao")
public class DataBaseDaoImpl extends JdbcDaoSupport implements IBaseDao {
    private DataSource dataSource;
    private JdbcTemplate template;

    //---★---JdbcDaoSupport 通过spring配置文件自动注入相关类，注意这里的变量名必须与配置文件中id保持一致，系统可通过变量名查找相同id配置的类型进行注入---★---
    @Autowired
    public DataBaseDaoImpl(DataSource acsDataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = acsDataSource;
        this.template = jdbcTemplate;
        super.setDataSource(this.dataSource);
        super.setJdbcTemplate(this.template);
    }

    /**
     * 数据库连接工具
     * 若需要连接其他数据库，通过该方法设置
     *
     * @param bds
     */
    @Override
    public void setDbSource(BasicDataSource bds) {
        if (bds == null) {
            //BasicDataSource dataSource = (BasicDataSource) EnvironmentInitListener.getTarget(IConstants.DATA_BASE);
            super.setDataSource(this.dataSource);
        } else {
            super.setDataSource(bds);
        }
    }

    /*
     * 根据客户端传入的数据库信息查找库表信息
     *
     * @see codecreator.common.IBaseDao#queryTableEx()
     */
    @Override
    public List<Table> queryTableEx(DBConnectInfo dbci) {
        if (this.getDataSource() != null) {
            DbUtils du = new DbUtils((BasicDataSource) this.getDataSource());
            if (dbci.getDbType().indexOf("mysql") > -1) {
                return du.getAllTable4MySQL(dbci);
            } else if (dbci.getDbType().indexOf("oracle") > -1) {
                return du.getAllTable4Oracle(dbci);
            } else if (dbci.getDbType().indexOf("postgresql") > -1) {
                return du.getAllTable4PostgreSQL(dbci);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see codecreator.common.IBaseDao#testDb()
     */
    @Override
    public boolean testDb(DBConnectInfo dbConnectInfo) {
        // TODO Auto-generated method stub
        if (this.getDataSource() != null) {
            DbUtils du = new DbUtils((BasicDataSource) this.getDataSource());
            if (du.testConnect()) {
                //保存数据库连接信息
                DbUtils du4save = new DbUtils(null);
                du4save.insertDataBase(dbConnectInfo);
                return true;
            }
        }
        return false;
    }

    /**
     * 查询数据连接信息
     *
     * @return
     */
    @Override
    public JSONArray selectDataBase() {
        if (this.getDataSource() != null) {
            DbUtils du = new DbUtils((BasicDataSource) this.getDataSource());
            return du.getDataBase();
        }
        return null;
    }

    /*
     * 保存工程配置信息
     *
     * @see codecreator.common.IBaseDao#insertProject(java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean insertProject(String projectName, String projectConfig) {
        if (this.getDataSource() != null) {
            DbUtils du = new DbUtils((BasicDataSource) this.getDataSource());
            return du.insertProject(projectName, projectConfig);
        }
        return false;
    }

    @Override
    public boolean insertApiGroup(String apiGroupName) {
        if (this.getDataSource() != null) {
            DbUtils du = new DbUtils((BasicDataSource) this.getDataSource());
            return du.insertApiGroup(apiGroupName);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see codecreator.common.IBaseDao#selectProject()
     */
    @Override
    public JSONArray selectProject() {
        if (this.getDataSource() != null) {
            DbUtils du = new DbUtils((BasicDataSource) this.getDataSource());
            return du.getProjects();
        }
        return null;
    }

    @Override
    public JSONArray selectApiGroup() {
        if (this.getDataSource() != null) {
            DbUtils du = new DbUtils((BasicDataSource) this.getDataSource());
            return du.getApiGroups();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.jzac.dao.IBaseDao#deleteProject(java.lang.String)
     */
    @Override
    public boolean deleteProject(String projectName) {
        // TODO Auto-generated method stub
        if (this.getDataSource() != null) {
            DbUtils du = new DbUtils((BasicDataSource) this.getDataSource());
            return du.deleteProject(projectName);
        }
        return false;
    }

    @Override
    public boolean deleteDataBase(String dbDesc) {
        // TODO Auto-generated method stub
        if (this.getDataSource() != null) {
            DbUtils du = new DbUtils((BasicDataSource) this.getDataSource());
            return du.deleteDataBase(dbDesc);
        }
        return false;
    }

    @Override
    public boolean deleteApiGroup(String groupName) {
        // TODO Auto-generated method stub
        if (this.getDataSource() != null) {
            DbUtils du = new DbUtils((BasicDataSource) this.getDataSource());
            return du.deleteApiGroup(groupName);
        }
        return false;
    }

}
