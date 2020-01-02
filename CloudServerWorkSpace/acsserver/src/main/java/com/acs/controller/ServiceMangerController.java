package com.acs.controller;

import com.acs.model.database.DBConnectInfo;
import com.acs.model.svrreg.SvrCatalog;
import com.acs.model.svrreg.SvrInfo;
import com.acs.util.db.DataProcessSupport;
import com.acs.util.db.SvrManagerSupport;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

/**
 * @author LBM
 * @time 2019年06月24日
 * @project acs
 * @type DataProcessController
 * @desc 数据处理工具类
 */
@Api(value = "servicemanager", description = "服务注册工具类", tags = "服务注册工具类")
@Controller
@Scope("session") // 将bean 的范围设置成session，表示当前bean是会话级别且线程安全的变量。
@RequestMapping(value = "/servicemanager", method = {RequestMethod.GET, RequestMethod.POST})
public class ServiceMangerController {
    private Logger logger = LogManager.getLogger(this.getClass());

    @RequestMapping(value = "/svrtypelist")
    @ResponseBody
    @ApiOperation(value = "获取服务类型列表", httpMethod = "GET", notes = "获取服务类型列表", tags = "服务注册工具类")
    public JSONObject getSvrTypeList(
            @ApiParam(required = true, value = "字典项类型", name = "dictType") @RequestParam(name = "dictType") String dictType) {
        //初始化数据处理工具类
        DataProcessSupport dataProcessSupport = new DataProcessSupport(null);
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        JSONArray jsonArray = dataProcessSupport.getDictInfoList(dictType);
        //数据字段分析
        if (jsonArray != null) {
            int size = jsonArray.size();
            for (int i = 0; i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject tempJo = new JSONObject();
                tempJo.put("svrTypeName", jsonObject.getString("dictname"));
                tempJo.put("svrTypeValue", jsonObject.getString("dictvalue"));
                ja.add(tempJo);
            }
        }
        jo.put("data", ja);
        logger.info("服务类型列表获取成功");
        return jo;
    }

    @RequestMapping(value = "/svrcrslist")
    @ResponseBody
    @ApiOperation(value = "获取服务坐标系列表", httpMethod = "GET", notes = "获取服务坐标系列表", tags = "服务注册工具类")
    public JSONObject getSvrCRSList(
            @ApiParam(required = true, value = "字典项类型", name = "dictType") @RequestParam(name = "dictType") String dictType) {
        //初始化数据处理工具类
        DataProcessSupport dataProcessSupport = new DataProcessSupport(null);
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        JSONArray jsonArray = dataProcessSupport.getDictInfoList(dictType);
        //数据字段分析
        if (jsonArray != null) {
            int size = jsonArray.size();
            for (int i = 0; i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject tempJo = new JSONObject();
                tempJo.put("svrCRSName", jsonObject.getString("dictname"));
                tempJo.put("svrCRSValue", jsonObject.getString("dictvalue"));
                ja.add(tempJo);
            }
        }
        jo.put("data", ja);
        logger.info("服务坐标系列表获取成功");
        return jo;
    }

    @RequestMapping(value = "/initsvrregedit")
    @ResponseBody
    @ApiOperation(value = "初始化服务注册表", httpMethod = "GET", notes = "初始化服务注册表", tags = "服务注册工具类")
    public boolean initSvrRegedit(
            @ApiParam(required = true, value = "数据库驱动类型", name = "dbType") @RequestParam(name = "dbType") String dbType,
            @ApiParam(required = true, value = "数据库名称", name = "dbName") @RequestParam(name = "dbName") String dbName,
            @ApiParam(required = true, value = "数据库连接IP地址", name = "dbAddress") @RequestParam(name = "dbAddress") String dbAddress,
            @ApiParam(required = true, value = "数据库连接端口", name = "dbPort") @RequestParam(name = "dbPort") String dbPort,
            @ApiParam(required = true, value = "数据库连接用户名", name = "dbUser") @RequestParam(name = "dbUser") String dbUser,
            @ApiParam(required = true, value = "数据库连接密码", name = "dbPassword") @RequestParam(name = "dbPassword") String dbPassword) {
        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);

        //初始化数据处理工具类
        SvrManagerSupport svrManagerSupport = new SvrManagerSupport(dbci);
        boolean isSuccess = svrManagerSupport.initSvrRegedit();
        logger.info(isSuccess ? "初始化服务注册表成功" : "初始化服务注册表失败");

        return isSuccess;
    }

    @RequestMapping(value = "/getsvrtree")
    @ResponseBody
    @ApiOperation(value = "获取注册服务目录树", httpMethod = "GET", notes = "获取注册服务目录树", tags = "服务注册工具类")
    public JSONObject getSvrTree(
            @ApiParam(required = true, value = "数据库驱动类型", name = "dbType", defaultValue = "com.mysql.jdbc.Driver") @RequestParam(name = "dbType") String dbType,
            @ApiParam(required = true, value = "数据库名称", name = "dbName", defaultValue = "clouddb") @RequestParam(name = "dbName") String dbName,
            @ApiParam(required = true, value = "数据库连接IP地址", name = "dbAddress", defaultValue = "localhost") @RequestParam(name = "dbAddress") String dbAddress,
            @ApiParam(required = true, value = "数据库连接端口", name = "dbPort", defaultValue = "8888") @RequestParam(name = "dbPort") String dbPort,
            @ApiParam(required = true, value = "数据库连接用户名", name = "dbUser", defaultValue = "root") @RequestParam(name = "dbUser") String dbUser,
            @ApiParam(required = true, value = "数据库连接密码", name = "dbPassword", defaultValue = "root") @RequestParam(name = "dbPassword") String dbPassword) {
        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);
        JSONObject jsonObject = new JSONObject();
        //初始化数据处理工具类
        SvrManagerSupport svrManagerSupport = new SvrManagerSupport(dbci);
        //树型结构最大节点级数
        int maxNodeLevel = svrManagerSupport.getMaxTreeNodeLevel();
        if (maxNodeLevel > 0) {
            JSONArray jsonArray = svrManagerSupport.getAllTreeNodes();
            //树形节点组装
            if (jsonArray != null && jsonArray.size() > 0) {
                //记录树节点
                Hashtable hashtable4Nodes = new Hashtable();
                //树型数据集合
                JSONArray treeNodes = new JSONArray();
                assembleTree(jsonArray, 1, hashtable4Nodes, treeNodes, maxNodeLevel);
                hashtable4Nodes.clear();
                jsonObject.put("data", treeNodes);
            } else {
                jsonObject.put("data", null);
            }
        } else {
            jsonObject.put("data", null);
        }

        return jsonObject;
    }

    @RequestMapping(value = "/addcatalog")
    @ResponseBody
    @ApiOperation(value = "创建分组", httpMethod = "GET", notes = "创建分组", tags = "服务注册工具类")
    public JSONObject addCatalog(
            @ApiParam(required = true, value = "数据库驱动类型", name = "dbType") @RequestParam(name = "dbType") String dbType,
            @ApiParam(required = true, value = "数据库名称", name = "dbName") @RequestParam(name = "dbName") String dbName,
            @ApiParam(required = true, value = "数据库连接IP地址", name = "dbAddress") @RequestParam(name = "dbAddress") String dbAddress,
            @ApiParam(required = true, value = "数据库连接端口", name = "dbPort") @RequestParam(name = "dbPort") String dbPort,
            @ApiParam(required = true, value = "数据库连接用户名", name = "dbUser") @RequestParam(name = "dbUser") String dbUser,
            @ApiParam(required = true, value = "数据库连接密码", name = "dbPassword") @RequestParam(name = "dbPassword") String dbPassword,
            @ApiParam(required = true, value = "分组目录ID", name = "catalogId") @RequestParam(name = "catalogId") String catalogId,
            @ApiParam(required = true, value = "分组项对应Tree节点ID", name = "nodeId") @RequestParam(name = "nodeId") String nodeId,
            @ApiParam(required = true, value = "分组项对应Tree节点项父节点ID", name = "nodePid") @RequestParam(name = "nodePid") String nodePid,
            @ApiParam(required = true, value = "分组项名称", name = "nodeName") @RequestParam(name = "nodeName") String nodeName,
            @ApiParam(required = true, value = "分组项级别", name = "nodeLevel", example = "123") @RequestParam(name = "nodeLevel") int nodeLevel
    ) {
        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);
        JSONObject jsonObject = new JSONObject();
        //初始化数据处理工具类
        SvrManagerSupport svrManagerSupport = new SvrManagerSupport(dbci);
        SvrCatalog svrCatalog = new SvrCatalog();
        svrCatalog.setCatalogId(catalogId);
        svrCatalog.setNodeId(nodeId);
        svrCatalog.setNodePid(nodePid);
        svrCatalog.setNodeName(nodeName);
        svrCatalog.setNodeLevel(nodeLevel);

        boolean isSuccess = svrManagerSupport.addCatalog(svrCatalog);
        if (isSuccess) {
            jsonObject.put("state", 1);
            jsonObject.put("msg", "分组创建成功");
        } else {
            jsonObject.put("state", 0);
            jsonObject.put("msg", "分组创建失败");
        }
        return jsonObject;
    }

    @RequestMapping(value = "/editcatalog")
    @ResponseBody
    @ApiOperation(value = "编辑分组", httpMethod = "GET", notes = "编辑分组", tags = "服务注册工具类")
    public JSONObject editCatalog(
            @ApiParam(required = true, value = "数据库驱动类型", name = "dbType") @RequestParam(name = "dbType") String dbType,
            @ApiParam(required = true, value = "数据库名称", name = "dbName") @RequestParam(name = "dbName") String dbName,
            @ApiParam(required = true, value = "数据库连接IP地址", name = "dbAddress") @RequestParam(name = "dbAddress") String dbAddress,
            @ApiParam(required = true, value = "数据库连接端口", name = "dbPort") @RequestParam(name = "dbPort") String dbPort,
            @ApiParam(required = true, value = "数据库连接用户名", name = "dbUser") @RequestParam(name = "dbUser") String dbUser,
            @ApiParam(required = true, value = "数据库连接密码", name = "dbPassword") @RequestParam(name = "dbPassword") String dbPassword,
            @ApiParam(required = true, value = "分组目录ID", name = "catalogId") @RequestParam(name = "catalogId") String catalogId,
            @ApiParam(required = true, value = "分组项名称", name = "nodeName") @RequestParam(name = "nodeName") String nodeName
    ) {
        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);
        JSONObject jsonObject = new JSONObject();
        //初始化数据处理工具类
        SvrManagerSupport svrManagerSupport = new SvrManagerSupport(dbci);
        SvrCatalog svrCatalog = new SvrCatalog();
        svrCatalog.setCatalogId(catalogId);
        svrCatalog.setNodeName(nodeName);

        boolean isSuccess = svrManagerSupport.editCatalog(svrCatalog);
        if (isSuccess) {
            jsonObject.put("state", 1);
            jsonObject.put("msg", "分组编辑成功");
        } else {
            jsonObject.put("state", 0);
            jsonObject.put("msg", "分组编辑失败");
        }
        return jsonObject;
    }

    @RequestMapping(value = "/deletecatalog")
    @ResponseBody
    @ApiOperation(value = "删除分组", httpMethod = "GET", notes = "删除分组", tags = "服务注册工具类")
    public JSONObject deleteCatalog(
            @ApiParam(required = true, value = "数据库驱动类型", name = "dbType") @RequestParam(name = "dbType") String dbType,
            @ApiParam(required = true, value = "数据库名称", name = "dbName") @RequestParam(name = "dbName") String dbName,
            @ApiParam(required = true, value = "数据库连接IP地址", name = "dbAddress") @RequestParam(name = "dbAddress") String dbAddress,
            @ApiParam(required = true, value = "数据库连接端口", name = "dbPort") @RequestParam(name = "dbPort") String dbPort,
            @ApiParam(required = true, value = "数据库连接用户名", name = "dbUser") @RequestParam(name = "dbUser") String dbUser,
            @ApiParam(required = true, value = "数据库连接密码", name = "dbPassword") @RequestParam(name = "dbPassword") String dbPassword,
            @ApiParam(required = true, value = "分组目录ID", name = "catalogId") @RequestParam(name = "catalogId") String catalogId
    ) {
        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);
        JSONObject jsonObject = new JSONObject();
        //初始化数据处理工具类
        SvrManagerSupport svrManagerSupport = new SvrManagerSupport(dbci);
        SvrCatalog svrCatalog = new SvrCatalog();
        svrCatalog.setCatalogId(catalogId);

        boolean isSuccess = svrManagerSupport.deleteCatalog(svrCatalog);
        if (isSuccess) {
            jsonObject.put("state", 1);
            jsonObject.put("msg", "分组删除成功");
        } else {
            jsonObject.put("state", 0);
            jsonObject.put("msg", "分组删除失败");
        }
        return jsonObject;
    }

    @RequestMapping(value = "/addservice")
    @ResponseBody
    @ApiOperation(value = "新建服务", httpMethod = "GET", notes = "新建服务", tags = "服务注册工具类")
    public JSONObject addService(
            @ApiParam(required = true, value = "数据库驱动类型", name = "dbType") @RequestParam(name = "dbType") String dbType,
            @ApiParam(required = true, value = "数据库名称", name = "dbName") @RequestParam(name = "dbName") String dbName,
            @ApiParam(required = true, value = "数据库连接IP地址", name = "dbAddress") @RequestParam(name = "dbAddress") String dbAddress,
            @ApiParam(required = true, value = "数据库连接端口", name = "dbPort") @RequestParam(name = "dbPort") String dbPort,
            @ApiParam(required = true, value = "数据库连接用户名", name = "dbUser") @RequestParam(name = "dbUser") String dbUser,
            @ApiParam(required = true, value = "数据库连接密码", name = "dbPassword") @RequestParam(name = "dbPassword") String dbPassword,

            @ApiParam(required = true, value = "分组目录ID", name = "catalogId") @RequestParam(name = "catalogId") String catalogId,
            @ApiParam(required = true, value = "服务ID", name = "serviceId") @RequestParam(name = "serviceId") String serviceId,
            @ApiParam(required = true, value = "服务名称", name = "svrName") @RequestParam(name = "svrName") String svrName,
            @ApiParam(required = true, value = "服务图层名称", name = "svrLayerName") @RequestParam(name = "svrLayerName") String svrLayerName,
            @ApiParam(required = true, value = "服务类型", name = "svrType") @RequestParam(name = "svrType") String svrType,
            @ApiParam(required = true, value = "服务采用坐标系", name = "svrSrid") @RequestParam(name = "svrSrid") String svrSrid,
            @ApiParam(required = true, value = "服务地址", name = "svrUrl") @RequestParam(name = "svrUrl") String svrUrl,

            @ApiParam(required = false, value = "服务范围中心经度", name = "cx", example = "0") @RequestParam(name = "cx") double cx,
            @ApiParam(required = false, value = "服务范围中心纬度", name = "cy", example = "0") @RequestParam(name = "cy") double cy,
            @ApiParam(required = false, value = "服务范围最小经度", name = "west", example = "0") @RequestParam(name = "west") double west,
            @ApiParam(required = false, value = "服务范围最小纬度", name = "south", example = "0") @RequestParam(name = "south") double south,
            @ApiParam(required = false, value = "服务范围最大经度", name = "east", example = "0") @RequestParam(name = "east") double east,
            @ApiParam(required = false, value = "服务范围最大纬度", name = "north", example = "0") @RequestParam(name = "north") double north,

            @ApiParam(required = false, value = "服务注册者", name = "svrProvider") @RequestParam(name = "svrProvider") String svrProvider,
            @ApiParam(required = false, value = "服务注册者ID", name = "svrProviderId") @RequestParam(name = "svrProviderId") String svrProviderId,
            @ApiParam(required = false, value = "服务描述", name = "svrDescription") @RequestParam(name = "svrDescription") String svrDescription
    ) {
        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);
        JSONObject jsonObject = new JSONObject();
        //初始化数据处理工具类
        SvrManagerSupport svrManagerSupport = new SvrManagerSupport(dbci);
        SvrInfo svrInfo = new SvrInfo();
        svrInfo.setCatalogId(catalogId);
        svrInfo.setServiceId(serviceId);
        svrInfo.setSvrName(svrName);
        svrInfo.setSvrLayerName(svrLayerName);
        svrInfo.setSvrType(svrType);
        svrInfo.setSvrSrid(svrSrid);
        svrInfo.setCx(cx);
        svrInfo.setCy(cy);
        svrInfo.setWest(west);
        svrInfo.setSouth(south);
        svrInfo.setEast(east);
        svrInfo.setNorth(north);
        svrInfo.setSvrUrl(svrUrl);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        svrInfo.setSvrDate(formatter.format(new Date().getTime()));
        svrInfo.setSvrProvider(svrProvider);
        svrInfo.setSvrProviderid(svrProviderId);
        svrInfo.setSvrDescription(svrDescription);

        boolean isSuccess = svrManagerSupport.addService(svrInfo);
        if (isSuccess) {
            jsonObject.put("state", 1);
            jsonObject.put("msg", "服务创建成功");
        } else {
            jsonObject.put("state", 0);
            jsonObject.put("msg", "服务创建失败");
        }
        return jsonObject;
    }

    @RequestMapping(value = "/editservice")
    @ResponseBody
    @ApiOperation(value = "编辑服务", httpMethod = "GET", notes = "编辑服务", tags = "服务注册工具类")
    public JSONObject editService(
            @ApiParam(required = true, value = "数据库驱动类型", name = "dbType") @RequestParam(name = "dbType") String dbType,
            @ApiParam(required = true, value = "数据库名称", name = "dbName") @RequestParam(name = "dbName") String dbName,
            @ApiParam(required = true, value = "数据库连接IP地址", name = "dbAddress") @RequestParam(name = "dbAddress") String dbAddress,
            @ApiParam(required = true, value = "数据库连接端口", name = "dbPort") @RequestParam(name = "dbPort") String dbPort,
            @ApiParam(required = true, value = "数据库连接用户名", name = "dbUser") @RequestParam(name = "dbUser") String dbUser,
            @ApiParam(required = true, value = "数据库连接密码", name = "dbPassword") @RequestParam(name = "dbPassword") String dbPassword,
            @ApiParam(required = true, value = "服务ID", name = "serviceId") @RequestParam(name = "serviceId") String serviceId,
            @ApiParam(required = true, value = "服务名称", name = "svrName") @RequestParam(name = "svrName") String svrName,
            @ApiParam(required = true, value = "服务图层名称", name = "svrLayerName") @RequestParam(name = "svrLayerName") String svrLayerName,
            @ApiParam(required = true, value = "服务类型", name = "svrType") @RequestParam(name = "svrType") String svrType,
            @ApiParam(required = true, value = "服务采用坐标系", name = "svrSrid") @RequestParam(name = "svrSrid") String svrSrid,
            @ApiParam(required = true, value = "服务地址", name = "svrUrl") @RequestParam(name = "svrUrl") String svrUrl,

            @ApiParam(required = false, value = "服务范围中心经度", name = "cx", example = "0") @RequestParam(name = "cx") double cx,
            @ApiParam(required = false, value = "服务范围中心纬度", name = "cy", example = "0") @RequestParam(name = "cy") double cy,
            @ApiParam(required = false, value = "服务范围最小经度", name = "west", example = "0") @RequestParam(name = "west") double west,
            @ApiParam(required = false, value = "服务范围最小纬度", name = "south", example = "0") @RequestParam(name = "south") double south,
            @ApiParam(required = false, value = "服务范围最大经度", name = "east", example = "0") @RequestParam(name = "east") double east,
            @ApiParam(required = false, value = "服务范围最大纬度", name = "north", example = "0") @RequestParam(name = "north") double north,

            @ApiParam(required = false, value = "服务注册者", name = "svrProvider") @RequestParam(name = "svrProvider") String svrProvider,
            @ApiParam(required = false, value = "服务注册者ID", name = "svrProviderId") @RequestParam(name = "svrProviderId") String svrProviderId,
            @ApiParam(required = false, value = "服务描述", name = "svrDescription") @RequestParam(name = "svrDescription") String svrDescription
    ) {
        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);
        JSONObject jsonObject = new JSONObject();
        //初始化数据处理工具类
        SvrManagerSupport svrManagerSupport = new SvrManagerSupport(dbci);
        SvrInfo svrInfo = new SvrInfo();
        svrInfo.setServiceId(serviceId);
        svrInfo.setSvrName(svrName);
        svrInfo.setSvrLayerName(svrLayerName);
        svrInfo.setSvrType(svrType);
        svrInfo.setSvrSrid(svrSrid);
        svrInfo.setSvrUrl(svrUrl);
        svrInfo.setCx(cx);
        svrInfo.setCy(cy);
        svrInfo.setWest(west);
        svrInfo.setSouth(south);
        svrInfo.setEast(east);
        svrInfo.setNorth(north);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        svrInfo.setSvrDate(formatter.format(new Date().getTime()));
        svrInfo.setSvrProvider(svrProvider);
        svrInfo.setSvrProviderid(svrProviderId);
        svrInfo.setSvrDescription(svrDescription);
        boolean isSuccess = svrManagerSupport.editService(svrInfo);
        if (isSuccess) {
            jsonObject.put("state", 1);
            jsonObject.put("msg", "服务编辑成功");
        } else {
            jsonObject.put("state", 0);
            jsonObject.put("msg", "服务编辑失败");
        }
        return jsonObject;
    }

    @RequestMapping(value = "/deleteservice")
    @ResponseBody
    @ApiOperation(value = "删除服务", httpMethod = "GET", notes = "删除服务", tags = "服务注册工具类")
    public JSONObject deleteService(
            @ApiParam(required = true, value = "数据库驱动类型", name = "dbType") @RequestParam(name = "dbType") String dbType,
            @ApiParam(required = true, value = "数据库名称", name = "dbName") @RequestParam(name = "dbName") String dbName,
            @ApiParam(required = true, value = "数据库连接IP地址", name = "dbAddress") @RequestParam(name = "dbAddress") String dbAddress,
            @ApiParam(required = true, value = "数据库连接端口", name = "dbPort") @RequestParam(name = "dbPort") String dbPort,
            @ApiParam(required = true, value = "数据库连接用户名", name = "dbUser") @RequestParam(name = "dbUser") String dbUser,
            @ApiParam(required = true, value = "数据库连接密码", name = "dbPassword") @RequestParam(name = "dbPassword") String dbPassword,
            @ApiParam(required = true, value = "服务ID", name = "serviceId") @RequestParam(name = "serviceId") String serviceId
    ) {
        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);
        JSONObject jsonObject = new JSONObject();
        //初始化数据处理工具类
        SvrManagerSupport svrManagerSupport = new SvrManagerSupport(dbci);
        SvrInfo svrInfo = new SvrInfo();
        svrInfo.setServiceId(serviceId);

        boolean isSuccess = svrManagerSupport.deleteService(svrInfo);
        if (isSuccess) {
            jsonObject.put("state", 1);
            jsonObject.put("msg", "服务删除成功");
        } else {
            jsonObject.put("state", 0);
            jsonObject.put("msg", "服务删除失败");
        }
        return jsonObject;
    }


    /**
     * 组装树形结构
     *
     * @param nodes
     * @param level
     * @return
     */
    private void assembleTree(JSONArray nodes, int level, Hashtable hashtable4Nodes, JSONArray treeNodes, int maxNodeLevel) {
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            JSONObject jo = nodes.getJSONObject(i);
            //1、分组节点
            int nodeLevel = jo.getIntValue("nodeLevel");
            String catalogId = jo.getString("catalogId");
            String nodeName = jo.getString("nodeName");
            String nId = jo.getString("nodeId");
            String pnId = jo.getString("parentNodeId");
            if (!nId.equalsIgnoreCase("") && !hashtable4Nodes.containsKey(nId)) {
                JSONObject treeNode = new JSONObject();
                treeNode.put("text", nodeName);
                treeNode.put("level", nodeLevel);
                treeNode.put("nId", nId);
                treeNode.put("pnId", pnId);
                treeNode.put("catalogId", catalogId);
                treeNode.put("isGroup", true);
                treeNode.put("iconCls", "cloud icon-layergroup");
                treeNode.put("children", new JSONArray());
                treeNode.put("leaf", true);
                if (nodeLevel == 1) {
                    treeNodes.add(treeNode);
                } else {
                    JSONObject parentTreeNode = (JSONObject) hashtable4Nodes.get(pnId);
                    if (parentTreeNode.getBooleanValue("leaf")) {
                        parentTreeNode.put("leaf", false);
                    }
                    parentTreeNode.getJSONArray("children").add(treeNode);
                }
                hashtable4Nodes.put(nId, treeNode);
            }

            //1、服务节点
            String svrId = jo.getString("svrId");
            if (!svrId.equalsIgnoreCase("") && !hashtable4Nodes.containsKey(svrId)) {
                String svrName = jo.getString("svrName");
                String svrLayerName = jo.getString("svrLayerName");
                String svrType = jo.getString("svrType");
                String svrSrid = jo.getString("svrSrid");
                String svrUrl = jo.getString("svrUrl");
                String svrDate = jo.getString("svrDate");
                String svrProvider = jo.getString("svrProvider");
                String svrProviderId = jo.getString("svrProviderId");
                String svrDesc = jo.getString("svrDesc");
                double cx = jo.getDoubleValue("cx");
                double cy = jo.getDoubleValue("cy");
                double west = jo.getDoubleValue("west");
                double south = jo.getDoubleValue("south");
                double east = jo.getDoubleValue("east");
                double north = jo.getDoubleValue("north");
                //1、通过catalogId查找分组节点
                //2、将叶子节点追加到该分组节点
                JSONObject leafNode = new JSONObject();
                leafNode.put("svrId", svrId);
                leafNode.put("text", svrName);
                leafNode.put("svrLayerName", svrLayerName);
                leafNode.put("svrType", svrType);
                leafNode.put("svrSrid", svrSrid);
                leafNode.put("svrUrl", svrUrl);

                leafNode.put("cx", cx);
                leafNode.put("cy", cy);
                leafNode.put("west", west);
                leafNode.put("south", south);
                leafNode.put("east", east);
                leafNode.put("north", north);

                leafNode.put("svrDate", svrDate);
                leafNode.put("svrProvider", svrProvider);
                leafNode.put("svrProviderId", svrProviderId);
                leafNode.put("svrDesc", svrDesc);
                leafNode.put("iconCls", "cloud icon-map");
                leafNode.put("leaf", true);
                JSONObject treeNode = (JSONObject) hashtable4Nodes.get(nId);
                if (treeNode.getBooleanValue("leaf")) {
                    treeNode.put("leaf", false);
                }
                treeNode.getJSONArray("children").add(leafNode);
                hashtable4Nodes.put(svrId, leafNode);
            }
        }
        level = level + 1;
        if (level <= maxNodeLevel) {
            assembleTree(nodes, level, hashtable4Nodes, treeNodes, maxNodeLevel);
        }
    }
}
