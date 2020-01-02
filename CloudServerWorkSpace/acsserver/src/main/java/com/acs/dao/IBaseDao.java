/**
 *
 */
package com.acs.dao;

import com.acs.model.database.DBConnectInfo;
import com.acs.util.db.Table;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.dbcp.BasicDataSource;

import java.util.List;

/**
 * @author LBM
 * @time 2017年11月30日
 * @project： codecreator
 * @type： IBaseDao
 * @desc：
 */
public interface IBaseDao {

    void setDbSource(BasicDataSource bds);

    List<Table> queryTableEx(DBConnectInfo dbci);

    boolean testDb(DBConnectInfo dbConnectInfo);

    JSONArray selectDataBase();

    boolean insertProject(String projectName, String projectConfig);

    boolean insertApiGroup(String apiGroupName);

    JSONArray selectProject();

    JSONArray selectApiGroup();

    boolean deleteProject(String projectName);

    boolean deleteDataBase(String dbDesc);

    boolean deleteApiGroup(String groupName);
}
