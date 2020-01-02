/**
 *
 */
package com.acs.service;

import com.acs.model.database.DBConnectInfo;
import com.acs.util.db.Table;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author LBM<br       />
 * @time 2017年11月30日 下午3:58:58 <br/>
 * 项目名称：codecreator <br/>
 * 类名称：IBaseService <br/>
 * 类描述：
 */
public interface IBaseService {

    List<Table> getTableInfoEx(DBConnectInfo dbci);

    boolean doTestDB(DBConnectInfo dbConnectInfo);

    boolean saveProjectConfig(String projectName, String projectConfig);

    JSONArray getDataBases();

    JSONArray getProjects();

    JSONArray getApiGroups();

    boolean saveApiGroup(String apiGroupName);

    boolean deleteProject(String projectName);

    boolean deleteDataBase(String dbDesc);

    boolean deleteApiGroup(String groupName);
}
