/**
 * 根据数据库创建代码
 */
package com.acs.controller;

import com.acs.common.AcsConstants;
import com.acs.model.database.DBConnectInfo;
import com.acs.model.project.ProjectConfigInfo;
import com.acs.model.project.ProjectParams;
import com.acs.model.project.WebProjectConfigInfo;
import com.acs.model.project.WebProjectPaths;
import com.acs.service.IBaseService;
import com.acs.util.FileUtil;
import com.acs.util.JSONParserUtil;
import com.acs.util.SQLParseUtil;
import com.acs.util.Sort4JSONArrayUtil;
import com.acs.util.db.Table;
import com.acs.util.redis.RedisCacheUtil;
import com.acs.util.velocity.VelocityUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author LBM
 * @time 2017年12月22日
 * @project acs
 * @type AutoCoderController
 * @desc 代码创建工具类
 */
@Api(value = "coding", description = "根据数据库表生成SSM代码", tags = "代码生成工具类")
@Controller
@Scope("session") // 将bean 的范围设置成session，表示当前bean是会话级别且线程安全的变量。
@RequestMapping(value = "/coding", method = {RequestMethod.GET, RequestMethod.POST})
public class AutoCoderController {
    private Logger logger = LogManager.getLogger(AutoCoderController.class);

    // Session范围全局变量,工程配置类
    private ProjectParams pps;
    private FileUtil fu;
    private VelocityUtils vu;
    private VelocityUtils vu4coding;
    private SQLParseUtil spu;
    private JSONParserUtil jpu;

    // @PostConstruct--当bean加载完之后，就会执行init方法，并且将pps实例化；
    // @PostConstruct
    // @ModelAttribute--进入控制器方法之前先执行的代码块（此处为init方法）
    @ModelAttribute
    public void init() {
        pps = new ProjectParams();
        fu = new FileUtil();
        spu = new SQLParseUtil();
        jpu = new JSONParserUtil();

        // System.out.println("AutoCoderController控制器调用开启");
    }

    /*
     * 资源注入方案一：
     *
     * @Resource(name = "dbs", description = "数据源处理类，如：获取表结构信息。")
     */

    /*
     * 资源注入方案二： 采用指定的名称注入资源
     */
    @Autowired
    @Qualifier("dbs") //
    private IBaseService dbs;

    //注入redis操作工具实例
    @Resource
    private RedisCacheUtil redisCache;

    @RequestMapping(value = "/testdb")
    @ResponseBody
    @ApiOperation(value = "测试数据库连接", httpMethod = "POST", notes = "测试数据库连接", tags = "数据库连接管理工具")
    public boolean testDB(
            @ApiParam(required = true, value = "数据库驱动类型", name = "dbType", defaultValue = "oracle.jdbc.driver.OracleDriver") @RequestParam(name = "dbType") String dbType,
            @ApiParam(required = true, value = "数据库名称", name = "dbName", defaultValue = "orcl") @RequestParam(name = "dbName") String dbName,
            @ApiParam(required = true, value = "数据库连接IP地址", name = "dbAddress", defaultValue = "10.1.4.22") @RequestParam(name = "dbAddress") String dbAddress,
            @ApiParam(required = true, value = "数据库连接端口", name = "dbPort", defaultValue = "1521") @RequestParam(name = "dbPort") String dbPort,
            @ApiParam(required = true, value = "数据库连接用户名", name = "dbUser", defaultValue = "root") @RequestParam(name = "dbUser") String dbUser,
            @ApiParam(required = true, value = "数据库连接密码", name = "dbPassword", defaultValue = "root") @RequestParam(name = "dbPassword") String dbPassword,
            @ApiParam(required = true, value = "JZ测试数据库", name = "dbDesc", defaultValue = "JZ测试数据库") @RequestParam(name = "dbDesc") String dbDesc,
            @ApiParam(value = "是否启用Redis配置", name = "isNeedRedis", defaultValue = "on") @RequestParam(required = false, name = "isNeedRedis") String isNeedRedis,
            @ApiParam(value = "Redis地址", name = "redisAddress", defaultValue = "localhost") @RequestParam(required = false, name = "redisAddress") String redisAddress,
            @ApiParam(value = "Redis端口", name = "redisPort", defaultValue = "3306") @RequestParam(required = false, name = "redisPort") String redisPort,
            @ApiParam(value = "Redis密码", name = "redisPassword", defaultValue = "redis") @RequestParam(required = false, name = "redisPassword") String redisPassword) {
        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);
        dbci.setDbDesc(dbDesc);
        dbci.setNeedRedis(isNeedRedis);
        dbci.setRedisAddress(redisAddress);
        dbci.setRedisPort(redisPort);
        dbci.setRedisPassword(redisPassword);

        return dbs.doTestDB(dbci);
    }

    /**
     * 获取已创建的数据库连接列表
     *
     * @return
     */
    @RequestMapping(value = "/getdatabase")
    @ResponseBody
    @ApiOperation(value = "获取已创建数据库配置列表", httpMethod = "POST", notes = "获取已创建数据库配置列表", tags = "数据库连接管理工具", response = DBConnectInfo.class)
    public JSONObject getDataBases() {
        JSONArray ja = dbs.getDataBases();
        JSONObject jo = new JSONObject();
        if (ja != null && ja.size() > 0) {
            jo.put("success", true);
            jo.put("dbs", ja);
        } else {
            jo.put("success", false);
            jo.put("dbs", "[]");
        }
        return jo;
    }

    /**
     * 获取已创建的接口分组列表
     *
     * @return
     */
    @RequestMapping(value = "/getapigroup")
    @ResponseBody
    @ApiOperation(value = "获取已创建的接口分组列表", httpMethod = "GET", notes = "获取已创建的接口分组列表", tags = "业务接口管理")
    public JSONObject getApiGroup() {
        JSONArray ja = dbs.getApiGroups();
        JSONObject jo = new JSONObject();
        if (ja != null && ja.size() > 0) {
            jo.put("success", true);
            jo.put("ags", ja);
        } else {
            jo.put("success", false);
            jo.put("ags", "[]");
        }
        return jo;
    }

    @RequestMapping(value = "/gettables")
    @ResponseBody
    @ApiOperation(value = "根据前端配置获取连接数据库表信息", httpMethod = "POST", notes = "根据前端配置获取连接数据库表信息", tags = "数据库连接管理工具")
    public JSONObject getDBTables(
            @ApiParam(required = true, value = "是否重载数据库表信息", name = "isReload", defaultValue = "false") @RequestParam(name = "isReload") String isReload,
            @ApiParam(required = true, value = "数据库驱动类型", name = "dbType", defaultValue = "com.mysql.jdbc.Driver") @RequestParam(name = "dbType") String dbType,
            @ApiParam(required = true, value = "数据库名称", name = "dbName", defaultValue = "acdb") @RequestParam(name = "dbName") String dbName,
            @ApiParam(required = true, value = "数据库连接IP地址", name = "dbAddress", defaultValue = "10.1.4.40") @RequestParam(name = "dbAddress") String dbAddress,
            @ApiParam(required = true, value = "数据库连接端口", name = "dbPort", defaultValue = "3306") @RequestParam(name = "dbPort") String dbPort,
            @ApiParam(required = true, value = "数据库连接用户名", name = "dbUser", defaultValue = "rpps") @RequestParam(name = "dbUser") String dbUser,
            @ApiParam(required = true, value = "数据库连接密码", name = "dbPassword", defaultValue = "rpps") @RequestParam(name = "dbPassword") String dbPassword) {
        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);

        List<Table> tbs = null;
        String key = dbci.getDbName() + "-" + dbci.getDbAddress() + "-" + dbci.getDbPort() + "-" + dbci.getDbType() + "-" + dbci.getDbUser();
        String field = "tables";
        String tbstr = redisCache.hashGet(key, field);
        boolean reload = Boolean.valueOf(isReload);
        //默认重载
        if (reload || tbstr == null) {
            tbs = dbs.getTableInfoEx(dbci);

            // 将数据库表信息写入Redis数据库中
            String value = JSONObject.toJSON(tbs).toString();
            redisCache.hashSet(key, field, value);
        } else {
            tbs = JSONObject.parseArray(tbstr, Table.class);
        }

        JSONObject jo = new JSONObject();
        if (tbs != null && tbs.size() > 0) {
            jo.put("success", true);
            jo.put("tbs", JSONObject.toJSON(tbs));
        } else {
            jo.put("success", false);
            jo.put("tbs", "[]");
        }
        return jo;
    }

    @RequestMapping(value = "/makeproject")
    @ResponseBody
    @ApiOperation(value = "通过配置创建工程", httpMethod = "POST", notes = "通过配置创建工程", response = ProjectParams.class)
    public JSONObject makeProject(
            @ApiParam(required = true, value = "数据库配置信息(测试值：{\\\"dbType\\\":\\\"com.mysql.jdbc.Driver\\\",\\\"dbAddress\\\":\\\"localhost\\\",\\\"dbPort\\\":\\\"8888\\\",\\\"dbUser\\\":\\\"root\\\",\\\"dbPassword\\\":\\\"998991lbm\\\",\\\"dbName\\\":\\\"youyamvc\\\"})", name = "dbConfig") @RequestParam(name = "dbConfig") String dbConfig,
            @ApiParam(required = true, value = "工程名称", name = "projectName") @RequestParam(name = "projectName") String projectName,
            @ApiParam(required = true, value = "工程描述", name = "projectDesc") @RequestParam(name = "projectDesc") String projectDesc,
            @ApiParam(required = true, value = "顶级包名,如：com", name = "fpName") @RequestParam(name = "fpName") String fpName,
            @ApiParam(required = true, value = "次级包名，如：项目名称", name = "spName") @RequestParam(name = "spName") String spName,
            @ApiParam(required = true, value = "业务层包名", name = "servName") @RequestParam(name = "servName") String servName,
            @ApiParam(required = true, value = "Java实体模型包名，如：entity、models等", name = "epName") @RequestParam(name = "epName") String epName,
            @ApiParam(required = true, value = "Dao接口包名，如：idao、dao等", name = "dpName") @RequestParam(name = "dpName") String dpName,
            @ApiParam(required = true, value = "Controller包名，如：controller", name = "cpName") @RequestParam(name = "cpName") String cpName,
            @ApiParam(required = true, value = "interceptor包名，如：interceptor", name = "interceptorName") @RequestParam(name = "interceptorName") String interceptorName,
            @ApiParam(required = true, value = "Mapping映射文件目录名", name = "mfdName") @RequestParam(name = "mfdName") String mfdName,
            @ApiParam(required = true, value = "JS文件目录名，如：jsentity、jsmodel", name = "jsfdName") @RequestParam(name = "jsfdName") String jsfdName,
            @ApiParam(required = true, value = "JS文件名，如：JsEntity、JsModel", name = "jsfName") @RequestParam(name = "jsfName") String jsfName,
            @ApiParam(required = true, value = "Swagger后台服务器IP及端口，如：localhost:8080", name = "swaggerServer") @RequestParam(name = "swaggerServer") String swaggerServer) {
        JSONObject jo = new JSONObject();

        ProjectConfigInfo pci = new ProjectConfigInfo();
        pci.setProjectName(projectName);
        pci.setProjectDesc(projectDesc);
        pci.setInterceptorName(interceptorName);
        pci.setFpName(fpName);
        pci.setSpName(spName);
        pci.setServName(servName);
        pci.setServNameImpl("impl");
        pci.setEpName(epName);
        pci.setDpName(dpName);
        pci.setDpNameImpl("impl");
        pci.setCpName(cpName);
        pci.setMfdName(mfdName);
        pci.setJsfdName(jsfdName);
        pci.setJsfName(jsfName);
        pci.setSwaggerServer(swaggerServer);
        // Server工程模版文件目录
        String sfp = Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1)
                + "projecttemplate/";
        // Web工程模版文件目录
        String wsfp = Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1)
                + "webprojecttemplate/";

        String disk = (String) sfp.subSequence(0, 2);// 获取盘符
        // 工程存放目录(该目录还需要存放工程配置及类配置文件)
        String rootPath = disk + "\\" + AcsConstants.PROJECTWS;
        String tfp = rootPath + "\\" + pci.getProjectName();
        String wRootPath = rootPath + "\\web\\";
        String wtfp = wRootPath + pci.getProjectName();
        // fu = new FileUtil();
        try {
            // 0、重复创建则删除之前的文件
            File oldFile = new File(tfp);
            fu.deleteDir(oldFile);
            File wOldFile = new File(wtfp);
            fu.deleteDir(wOldFile);

            // 1、模版工程拷贝,根目录必须要创建
            File rootDir = new File(rootPath);
            if (!rootDir.exists()) {
                rootDir.mkdir();
            }

            File wRootDir = new File(wRootPath);
            if (!wRootDir.exists()) {
                wRootDir.mkdir();
            }

            // 拷贝工程文件
            File stpf = new File(sfp);
            File tfpf = new File(tfp);
            // 拷贝Server工程文件
            fu.copyFolder(stpf, tfpf);
            // FileUtils.copyDirectory(stpf, tfpf);

            // 创建web目录
            File wstpf = new File(wsfp);
            File wtfpf = new File(wtfp);
            // 拷贝Web工程文件
            fu.copyFolder(wstpf, wtfpf);
            // FileUtils.copyDirectory(wstpf, wtfpf);

            // ******************Web工程目录初始化***************************
            // 已经生成工程的根目录
            String wProjectPath = wtfp + "\\";
            // app目录,config目录,全局Application.js文件所在目录等
            String appPath = wProjectPath + "\\app\\";
            // controller目录
            String wControllerPath = appPath + "\\controller\\";
            // model目录
            String modelPath = appPath + "\\model\\";
            // store目录
            String storePath = appPath + "\\store\\";
            // view目录
            String viewPath = appPath + "\\view\\";
            //module目录
            String modulePath = viewPath + "\\module\\";
            // util目录
            String webUtilPath = appPath + "\\util\\";
            // resources目录
            String wResPath = wProjectPath + "\\resources\\";
            //ModuleConfig文件路径
            String moduleConfigPath = modulePath + "\\SystemModule.json";
            // libs目录
            String libsPath = wResPath + "\\libs\\";
            // Java实体合并打包为JS对象文件路径
            String jsDirPath = wProjectPath + pci.getJsfdName() + "\\";

            // =====Web工程配置=====
            WebProjectConfigInfo wpci = new WebProjectConfigInfo();
            // Web工程名称
            wpci.setWebName(projectName);
            // Web工程应用入口文件名称
            wpci.setApplicationName("Application");
            // app目录名称
            wpci.setApplicationName("app");
            // controller目录名称
            wpci.setControllerName("controller");
            // =====Web工程目录信息=====
            WebProjectPaths wpp = new WebProjectPaths();
            wpp.setwProjectPath(wProjectPath);
            wpp.setAppPath(appPath);
            wpp.setwControllerPath(wControllerPath);
            wpp.setModelPath(modelPath);
            wpp.setStorePath(storePath);
            wpp.setViewPath(viewPath);
            wpp.setwResPath(wResPath);
            wpp.setLibsPath(libsPath);
            wpp.setUtilsPath(webUtilPath);
            wpp.setWebModulePath(modulePath);
            wpp.setWebConfigPath(moduleConfigPath);//module配置json文件存放目录

            // 追加到整个工程配置参数类中
            pps.setWpci(wpci);
            pps.setWpp(wpp);

            // 2、==============Java工程目录初始化===============
            // 2.0、工程根路径
            String projectPath = tfp + "\\";
            // 2.1、源代码路径
            String javaPath = projectPath + "src\\main\\java\\";
            // 2.1.1、顶级包路径
            String topPackPath = javaPath + pci.getFpName() + "\\" + pci.getSpName() + "\\";
            // 2.1.2、实体模型包路径
            String entityPath = topPackPath + pci.getEpName() + "\\";
            // 2.1.3、Dao接口类路径
            String daoPath = topPackPath + pci.getDpName() + "\\";
            // 2.1.3-1、Dao接口实现类文件路径(框架中暂时不需要针对Dao接口的实现类)
            //String daoImplPath = topPackPath + pci.getDpName() + "\\" + pci.getDpNameImpl() + "\\";
            // 2.1.4、Controller控制器路径
            String controllerPath = topPackPath + pci.getCpName() + "\\";
            // 2.1.6、Java实体合并打包为JS对象文件路径（此处代码移到上面的web工程配置中）
            // String jsDirPath = topPackPath + pci.getJsfdName() + "\\";
            // 2.1.7、 环境监听器文件路径
            String listenerPath = topPackPath + "listener\\";
            // 2.1.8、 Swagger配置类文件路径
            String swaggerConfigPath = topPackPath + "swagger\\";
            // 2.1.9、Service业务层路径
            String servicePath = topPackPath + pci.getServName() + "\\";
            // 2.1.9-1、Service业务层实现类文件路径
            String serviceImplPath = topPackPath + pci.getServName() + "\\" + pci.getServNameImpl() + "\\";
            // 2.1.10、Interceptor层路径
            String interceptorPath = topPackPath + pci.getInterceptorName() + "\\";
            // 2.1.11、Utils工具集路径
            String utilPath = topPackPath + "utils\\";

            // 3、资源路径
            String resPath = tfp + "\\src\\main\\resources\\";
            // 2.1.5、Mapper映射文件路径（修改MyBatis映射文件路径）
            String mapperPath = resPath + pci.getMfdName() + "\\";
            // 4、WEB-INF路径
            String webinfPath = tfp + "\\src\\main\\webapp\\WEB-INF\\";
            // 5、Swagger站点文件路径
            String swaggerPath = tfp + "\\src\\main\\webapp\\WEB-INF\\swagger\\";

            // 6、执行目录创建
            String[] paths = new String[]{entityPath, daoPath, controllerPath, mapperPath, jsDirPath, listenerPath,
                    swaggerConfigPath, servicePath, interceptorPath, /*daoImplPath,*/ serviceImplPath, utilPath};
            int len = paths.length;
            for (int i = 0; i < len; i++) {
                String fp = paths[i];
                if (!fp.equalsIgnoreCase("") && !fp.equals(null)) {
                    File dirPath = new File(fp);
                    if (!dirPath.exists()) {
                        dirPath.mkdirs();
                    }
                }
            }

            // 解析Redis配置参数 -- start
            String dbConfigJson = URLDecoder.decode(dbConfig, "UTF-8");
            JSONObject dbConfigObject = JSONObject.parseObject(dbConfigJson);
            DBConnectInfo dbci = JSONObject.toJavaObject(dbConfigObject, DBConnectInfo.class);
            pps.setDbci(dbci);
            //---redis config end

            pps.setPci(pci);
            pps.setProjectDesc(projectDesc);
            pps.setRootDir(rootPath);
            pps.setProjectPath(projectPath);
            pps.setJavaPath(javaPath);
            pps.setTopPackPath(topPackPath);
            pps.setEntityPath(entityPath);
            pps.setDaoPath(daoPath);
            pps.setControllerPath(controllerPath);
            pps.setInterceptorPath(interceptorPath);
            pps.setUtilsPath(utilPath);
            pps.setServPath(servicePath);
            pps.setServImplPath(serviceImplPath);
            pps.setMapperPath(mapperPath);
            pps.setJsDirPath(jsDirPath);
            pps.setListenerPath(listenerPath);
            pps.setSwaggerConfigPath(swaggerConfigPath);
            pps.setSwaggerPath(swaggerPath);
            pps.setResPath(resPath);
            pps.setWebinfPath(webinfPath);
            pps.setLogPath(disk + "\\logs\\" + pci.getProjectName());
            pps.setLogName(pci.getProjectName());// 日志名为工程名称

            // 6、工程配置参数注入,还需要注入工程日志目录及日志名称配置等
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("pps", pps);
            // 获取并注入数据库类型
            String dbType = dbci.getDbType();
            if (dbType.toLowerCase().indexOf("oracle") > -1) {
                m.put("dbType", "oracle");
            } else if (dbType.toLowerCase().indexOf("mysql") > -1) {
                m.put("dbType", "mysql");
            } else if (dbType.toLowerCase().indexOf("postgresql") > -1) {
                m.put("dbType", "postgresql");
                //m.put("dbType", "mysql");//todo 20191008----这里完全按照MySQL的接口方式生成PostgreSQL接口，是否完全兼容待验证，后续逐步完善。
            }

            // 6.X **************根据模版创建Web工程其他必要的文件，如App.js等***********************
            //todo 2018-09-05 Web模版工程待重构。
            vu = new VelocityUtils(
                    Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1)
                            + "webprojectfiles/");
            /*
             * vu = new VelocityUtils(
             * StringUtils.class.getClassLoader().getResource("/").getPath().substring(1) +
             * "webprojectfiles/");
             */
            // 6.x.1 Application.js
            String applicationStr = vu.mergeTemplate(m, "Application.vm");
            File applicationDir = new File(pps.getWpp().getAppPath());
            if (!applicationDir.exists()) {
                applicationDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getAppPath(), "Application", ".js", applicationStr);

            // 6.x.2 App.js
            String appStr = vu.mergeTemplate(m, "App.js.vm");
            File appDir = new File(pps.getWpp().getwProjectPath());
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getwProjectPath(), "app", ".js", appStr);

            // 6.x.3 AppController.js
            String appControllerStr = vu.mergeTemplate(m, "AppController.vm");
            File appControllerDir = new File(pps.getWpp().getwControllerPath());
            if (!appControllerDir.exists()) {
                appControllerDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getwControllerPath(), "AppController", ".js", appControllerStr);

            // 6.x.4 AppConfig.js
            String appConfigStr = vu.mergeTemplate(m, "AppConfig.vm");
            File appConfigDir = new File(pps.getWpp().getAppPath());
            if (!appConfigDir.exists()) {
                appConfigDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getAppPath(), "AppConfig", ".js", appConfigStr);

            // 6.x.5 AjaxUtil.js
            String ajaxUtilStr = vu.mergeTemplate(m, "AjaxUtil.vm");
            File ajaxUtilDir = new File(pps.getWpp().getUtilsPath());
            if (!ajaxUtilDir.exists()) {
                ajaxUtilDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getUtilsPath(), "AjaxUtil", ".js", ajaxUtilStr);

            // 6.x.6 CustomPageToolBar.js
            String pageUtilStr = vu.mergeTemplate(m, "CustomPageToolBar.vm");
            File pageUtilDir = new File(pps.getWpp().getUtilsPath());
            if (!pageUtilDir.exists()) {
                pageUtilDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getUtilsPath(), "CustomPageToolBar", ".js", pageUtilStr);

            //7 ChartUtil.js
            String chartUtilStr = vu.mergeTemplate(m, "ChartUtil.vm");
            File chartUtilDir = new File(pps.getWpp().getUtilsPath());
            if (!chartUtilDir.exists()) {
                chartUtilDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getUtilsPath(), "ChartUtil", ".js", chartUtilStr);

            //8 LayoutUtil.js
            String layoutUtilStr = vu.mergeTemplate(m, "LayoutUtil.vm");
            File layoutUtilDir = new File(pps.getWpp().getUtilsPath());
            if (!layoutUtilDir.exists()) {
                layoutUtilDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getUtilsPath(), "LayoutUtil", ".js", layoutUtilStr);

            //9 ModuleUtil.js
            String moduleUtilStr = vu.mergeTemplate(m, "ModuleUtil.vm");
            File moduleUtilDir = new File(pps.getWpp().getUtilsPath());
            if (!moduleUtilDir.exists()) {
                moduleUtilDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getUtilsPath(), "ModuleUtil", ".js", moduleUtilStr);

            //10 WrapUtil.js
            String wrapUtilStr = vu.mergeTemplate(m, "WrapUtil.vm");
            File wrapUtilDir = new File(pps.getWpp().getUtilsPath());
            if (!wrapUtilDir.exists()) {
                wrapUtilDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getUtilsPath(), "WrapUtil", ".js", wrapUtilStr);

            //11 CustomWindow.js
            String customWindowStr = vu.mergeTemplate(m, "CustomWindow.vm");
            File customWindowDir = new File(pps.getWpp().getUtilsPath());
            if (!customWindowDir.exists()) {
                customWindowDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getUtilsPath(), "CustomWindow", ".js", customWindowStr);

            //12 SearchFieldUtil.js
            String searchFieldUtilStr = vu.mergeTemplate(m, "SearchFieldUtil.vm");
            File searchFieldUtilDir = new File(pps.getWpp().getUtilsPath());
            if (!searchFieldUtilDir.exists()) {
                searchFieldUtilDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getUtilsPath(), "SearchFieldUtil", ".js", searchFieldUtilStr);

            //13 SearchTextUtil.js
            String searchTextUtilStr = vu.mergeTemplate(m, "SearchTextUtil.vm");
            File searchTextUtilDir = new File(pps.getWpp().getUtilsPath());
            if (!searchTextUtilDir.exists()) {
                searchTextUtilDir.mkdirs();
            }
            fu.writeFile(pps.getWpp().getUtilsPath(), "SearchTextUtil", ".js", searchTextUtilStr);

            // 6.x.7 框架基础模块left-module & main-module & welcome-module生成
            String leftModulePath = pps.getWpp().getViewPath() + "left\\";
            String mainModulePath = pps.getWpp().getViewPath() + "main\\";
            String welcomeModulePath = pps.getWpp().getViewPath() + "welcome\\";
            File leftModuleDir = new File(leftModulePath);
            if (!leftModuleDir.exists()) {
                leftModuleDir.mkdirs();
            }
            File mainModuleDir = new File(mainModulePath);
            if (!mainModuleDir.exists()) {
                mainModuleDir.mkdirs();
            }
            /*File welcomeModuleDir = new File(welcomeModulePath);
            if (!welcomeModuleDir.exists()) {
                welcomeModuleDir.mkdirs();
            }*/
            String leftViewStr = vu.mergeTemplate(m, "LeftView.vm");
            String leftControllerStr = vu.mergeTemplate(m, "LeftController.vm");
            String leftModelStr = vu.mergeTemplate(m, "LeftModel.vm");

            String mainViewStr = vu.mergeTemplate(m, "MainView.vm");
            String mainControllerStr = vu.mergeTemplate(m, "MainController.vm");
            String mainModelStr = vu.mergeTemplate(m, "MainModel.vm");

            fu.writeFile(leftModulePath, "Left", ".js", leftViewStr);
            fu.writeFile(leftModulePath, "LeftController", ".js", leftControllerStr);
            fu.writeFile(leftModulePath, "LeftModel", ".js", leftModelStr);

            fu.writeFile(mainModulePath, "Main", ".js", mainViewStr);
            fu.writeFile(mainModulePath, "MainController", ".js", mainControllerStr);
            fu.writeFile(mainModulePath, "MainModel", ".js", mainModelStr);

            // 6.x.8 更新系统配置文件(SystemConfig.json)  todo web框架经过调整之后，不再需要生成改配置文件，暂时注释。
            /*org.json.JSONObject content = new org.json.JSONObject();
            content.put("title", pps.getPci().getProjectDesc());
            content.put("serviceUrl",
                    "http://" + pps.getPci().getSwaggerServer() + "/" + pps.getPci().getProjectName() + "/");
            jpu.updateWebConfig(pps.getWpp().getWebConfigPath(), content);*/

            // ************************************************************************
            // 7、根据模版创建Java工程其他必要的文件，如web.xml,pom.xml等
            // 初始化模版引擎
            vu = new VelocityUtils(
                    Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1)
                            + "projectfiles/");
            /*
             * vu = new VelocityUtils(
             * StringUtils.class.getClassLoader().getResource("/").getPath().substring(1) +
             * "projectfiles/");
             */

            // 7.1、spring上下文配置
            String appContextStr = vu.mergeTemplate(m, "spring-application.vm");
            File appContextDir = new File(pps.getResPath());
            if (!appContextDir.exists())
                appContextDir.mkdirs();
            fu.writeFile(pps.getResPath(), "spring-application", ".xml", appContextStr);
            // 7.2、数据库配置
            String dbcStr = vu.mergeTemplate(m, "jdbc.properties.vm");
            File dbcStrDir = new File(pps.getResPath());
            if (!dbcStrDir.exists())
                dbcStrDir.mkdirs();
            fu.writeFile(pps.getResPath(), "jdbc", ".properties", dbcStr);
            // 7.2-1、 MyBatis数据库配置
            String mbcStr = vu.mergeTemplate(m, "mybatis-config.vm");
            File mbcStrDir = new File(pps.getResPath());
            if (!mbcStrDir.exists())
                mbcStrDir.mkdirs();
            fu.writeFile(pps.getResPath(), "mybatis-config", ".xml", mbcStr);

            // 7.3、SpringMVC配置
            String smvcStr = vu.mergeTemplate(m, "spring-mvc.vm");
            File smvcDir = new File(pps.getResPath());
            if (!smvcDir.exists())
                smvcDir.mkdirs();
            fu.writeFile(pps.getResPath(), "spring-mvc", ".xml", smvcStr);

            // 7.4、环境监听器类
            String eilStr = vu.mergeTemplate(m, "EnvironmentInitListener.vm");
            File eilDir = new File(pps.getListenerPath());
            if (!eilDir.exists())
                eilDir.mkdirs();
            fu.writeFile(pps.getListenerPath(), "EnvironmentInitListener", ".java", eilStr);
            // 7.5、POM文件
            String pomStr = vu.mergeTemplate(m, "pom.vm");
            File pomDir = new File(pps.getProjectPath());
            if (!pomDir.exists())
                pomDir.mkdirs();
            fu.writeFile(pps.getProjectPath(), "pom", ".xml", pomStr);
            // 7.6、Project配置文件
            String projStr = vu.mergeTemplate(m, "project.vm");
            File projDir = new File(pps.getProjectPath());
            if (!projDir.exists())
                projDir.mkdirs();
            fu.writeFile(pps.getProjectPath(), "", ".project", projStr);
            // 7.7、Swagger配置类
            String swaggerStr = vu.mergeTemplate(m, "SwaggerConfig.vm");
            File swaggerDir = new File(pps.getSwaggerConfigPath());
            if (!swaggerDir.exists())
                swaggerDir.mkdirs();
            fu.writeFile(pps.getSwaggerConfigPath(), "SwaggerConfig", ".java", swaggerStr);
            // 7.8、Web.xml配置类
            String webXmlStr = vu.mergeTemplate(m, "web.vm");
            File webXmlDir = new File(pps.getWebinfPath());
            if (!webXmlDir.exists())
                webXmlDir.mkdirs();
            fu.writeFile(pps.getWebinfPath(), "web", ".xml", webXmlStr);
            // 7.9、index.html-Swagger站点首页
            String indexStr = vu.mergeTemplate(m, "index.vm");
            File indexDir = new File(pps.getSwaggerPath());
            if (!indexDir.exists())
                indexDir.mkdirs();
            fu.writeFile(pps.getSwaggerPath(), "index", ".html", indexStr);
            // 7.10、特殊处理settings文件
            String settingStr = vu.mergeTemplate(m, "org.eclipse.wst.common.component.vm");
            File settingDir = new File(pps.getProjectPath() + "\\.settings\\");
            if (!settingDir.exists())
                settingDir.mkdirs();
            fu.writeFile(pps.getProjectPath() + "\\.settings\\", "org.eclipse.wst.common.component", "", settingStr);
            // 根据配置判断是否需要生成redis相关文件
            // 7.11、redis新增的文件
            // 7.11.1 RedisCacheUtil文件生成
            String rcuStr = vu.mergeTemplate(m, "RedisCacheUtil.vm");
            File rcuDir = new File(pps.getUtilsPath() + "redis\\");
            if (!rcuDir.exists()) {
                rcuDir.mkdirs();
            }
            fu.writeFile(pps.getUtilsPath() + "redis\\", "RedisCacheUtil", ".java", rcuStr);

            // 7.11.2 redis.properties 文件生成
            String rpropStr = vu.mergeTemplate(m, "redisproperties.vm");
            File rpropDir = new File(pps.getResPath());
            if (!rpropDir.exists()) {
                rpropDir.mkdirs();
            }
            fu.writeFile(pps.getResPath(), "redis", ".properties", rpropStr);

            // 7.11.3 spring-redis.xml文件生成
            String srStr = vu.mergeTemplate(m, "spring-redis.vm");
            File srDir = new File(pps.getResPath());
            if (!srDir.exists())
                srDir.mkdirs();
            fu.writeFile(pps.getResPath(), "spring-redis", ".xml", srStr);

            // 7.12、通用拦截器类
            String interceptorStr = vu.mergeTemplate(m, "CommonInterceptor.vm");
            File interceptorDir = new File(pps.getInterceptorPath());
            if (!interceptorDir.exists())
                interceptorDir.mkdirs();
            fu.writeFile(pps.getInterceptorPath(), "CommonInterceptor", ".java", interceptorStr);

            // 7.12-1、接口执行时间监听器类
            String ilsStr = vu.mergeTemplate(m, "SqlCostInterceptor.vm");
            File ilsDir = new File(pps.getInterceptorPath());
            if (!ilsDir.exists())
                ilsDir.mkdirs();
            fu.writeFile(pps.getInterceptorPath(), "SqlCostInterceptor", ".java", ilsStr);

            // 8、保存工程配置信息
            boolean isSaved = dbs.saveProjectConfig(projectName, JSONObject.toJSON(pps).toString());

            // 9、返回工程目录结构
            jo.put("success", isSaved);
            jo.put("pps", JSONObject.toJSON(pps));

            logger.info("工程创建成功");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            jo.put("success", false);
            jo.put("pps", "[]");
            logger.error("工程创建失败，错误信息：" + e.getMessage());
            e.printStackTrace();
        }
        return jo;
    }

    @RequestMapping(value = "/coding4customize")
    @ResponseBody
    @ApiOperation(value = "根据配置生成接口", httpMethod = "POST", notes = "自定义接口生成")
    public JSONObject coding4Customize(@ApiParam(required = true, value = "工程配置整合参数", name = "pps") @RequestParam(name = "pps") String pps, @ApiParam(required = true, value = "接口关联表", name = "table") @RequestParam(name = "table") String table, @ApiParam(required = true, value = "查询字段", name = "queryFields") @RequestParam(name = "queryFields") String queryFields, @ApiParam(required = true, value = "条件字段", name = "conditionFields") @RequestParam(name = "conditionFields") String conditionFields, @ApiParam(required = true, value = "自定义接口参数配置", name = "apiConfig") @RequestParam(name = "apiConfig") String apiConfig, @ApiParam(required = true, value = "当前查询是否分页", name = "isPagging") @RequestParam(name = "isPagging") boolean isPagging, @ApiParam(required = true, value = "当前操作是否生成对应的JS模块", name = "isModuling") @RequestParam(name = "isModuling") boolean isModuling, @ApiParam(required = true, value = "当前操作是否生成web表格记录管理代码,如增删改", name = "isManaging") @RequestParam(name = "isManaging") boolean isManaging) throws IOException {
        String ppsJson = URLDecoder.decode(pps, "UTF-8");
        JSONObject ppsObject = JSONObject.parseObject(ppsJson);
        ProjectParams ppsc = JSONObject.toJavaObject(ppsObject, ProjectParams.class);
        String tableJson = URLDecoder.decode(table, "UTF-8");
        JSONObject tableObject = JSONObject.parseObject(tableJson);
        String queryFieldsJson = URLDecoder.decode(queryFields, "UTF-8");
        JSONArray queryFieldsArray = JSONArray.parseArray(queryFieldsJson);
        String conditionFieldsJson = URLDecoder.decode(conditionFields, "UTF-8");
        JSONArray conditionFieldsArray = JSONArray.parseArray(conditionFieldsJson);
        String apiConfigJson = URLDecoder.decode(apiConfig, "UTF-8");
        JSONObject apiConfigObject = JSONObject.parseObject(apiConfigJson);
        String apiClassName = apiConfigObject.getString("apiName4C");//接口类名
        apiConfigObject.put("apiNameLow4C", apiClassName.substring(0, 1).toLowerCase() + apiClassName.substring(1));
        apiConfigObject.put("apiNameFullLow4C", apiClassName.toLowerCase());
        String apiRequestName = apiConfigObject.getString("requestName4C");//请求类名
        String apiResponseName = apiConfigObject.getString("responseName4C");//响应类名
        apiConfigObject.put("requestNameLow4C", apiRequestName.substring(0, 1).toLowerCase() + apiRequestName.substring(1));
        apiConfigObject.put("responseNameLow4C", apiResponseName.substring(0, 1).toLowerCase() + apiResponseName.substring(1));
        //返回结果对象
        JSONObject jo = new JSONObject();

        //至少需要一个查询字段
        if (queryFieldsArray != null && queryFieldsArray.size() > 0) {
            // 初始化模版引擎
            vu = new VelocityUtils(
                    Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1) + "templates/acs4customize");

            // 初始化模版引擎 for customize WebModule生成
            vu4coding = new VelocityUtils(
                    Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1)
                            + "templates/web/module4customize");

            //配置模版参数注入
            Map<String, Object> m = new HashMap<String, Object>();
            //工程+表参数注入
            // 获取分页设置
            ppsc.setPagging(isPagging);
            // 获取JS模块生成设置
            ppsc.setModuling(isModuling);
            // 获取web表单管理设置
            ppsc.setManaging(isManaging);
            //根据生成方案不同放置在特定的包中
            m.put("acsId", AcsConstants.ACS4CUSTOMIZE);
            // 获取并注入数据库类型
            String dbType = ppsc.getDbci().getDbType();
            if (dbType.toLowerCase().indexOf("oracle") > -1) {
                m.put("dbType", "oracle");
            } else if (dbType.toLowerCase().indexOf("mysql") > -1) {
                m.put("dbType", "mysql");
            } else if (dbType.toLowerCase().indexOf("postgresql") > -1) {
                m.put("dbType", "postgresql");
                //m.put("dbType", "mysql");//todo 20191008----这里完全按照MySQL的接口方式生成PostgreSQL接口，是否完全兼容待验证，后续逐步完善。
            }
            m.put("ppsc", ppsc);// 工程配置
            m.put("pagging", ppsc.isPagging());// 分页设置
            m.put("managing", ppsc.isManaging());// 记录管理设置
            m.put("ppn", ppsc.getPci().getFpName() + "." + ppsc.getPci().getSpName());// 包名根目录（顶级包+次级包）
            m.put("colList", tableObject.getJSONArray("columns"));
            m.put("projectPackageName", ppsc.getPci().getFpName() + "." + ppsc.getPci().getSpName());
            m.put("schemaName", tableObject.getString("schemaname"));//todo 20200102----仅针对PG数据库模式，只需要根据数据库类型调整相应的Mapper文件
            m.put("className", tableObject.getString("upperCaseName"));
            m.put("classLowName", tableObject.getString("lowerCaseName"));
            m.put("nameSpace",
                    ppsc.getPci().getFpName() + "." + ppsc.getPci().getSpName() + "." + ppsc.getPci().getMfdName());
            m.put("tableName", tableObject.getString("dbtablename"));
            m.put("objectPkJavaType", tableObject.getString("objectPkJavaType"));// Java接口模版中用到
            m.put("pkJavaType", tableObject.getString("pkJavaType"));// Mapper映射文件中用到
            //查询字段参数注入
            m.put("resCols", queryFieldsArray);

            //条件字段参数注入
            if (conditionFieldsArray != null && conditionFieldsArray.size() > 0) {
                m.put("reqCols", conditionFieldsArray);
            }

            //接口参数注入
            m.put("ao", apiConfigObject);

            //模版解析及代码生成
            // 实体存放目录
            String javaFolder = ppsc.getEntityPath() + AcsConstants.ACS4CUSTOMIZE + "/";
            File javaDir = new File(javaFolder);
            if (!javaDir.exists())
                javaDir.mkdirs();
            // 请求实体
            String reqName = null;
            if (conditionFieldsArray != null && conditionFieldsArray.size() > 0) {
                reqName = apiConfigObject.getString("requestName4C");
            }
            if (reqName != null) {
                String jsFilePath = ppsc.getJsDirPath() + AcsConstants.ACS4CUSTOMIZE + "/" + reqName + ".js";
                // 如js对象文件已经存在则删除
                File jsFile = new File(jsFilePath);
                if (jsFile.exists()) {
                    jsFile.delete();
                }

                // Java请求实体模版解释
                String javaStr4Req = vu.mergeTemplate(m, "Entity4Request.vm");
                fu.writeFile(javaFolder, reqName, ".java", javaStr4Req);

                // JavaScript存放目录
                File jsDir = new File(ppsc.getJsDirPath() + AcsConstants.ACS4CUSTOMIZE + "/");
                if (!jsDir.exists())
                    jsDir.mkdirs();

                // JavaScript模版解释
                String jsStr4Js = vu.mergeTemplate(m, "Javascript.vm");
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(jsFilePath, true), "UTF-8"));
                writer.write(jsStr4Js);
                writer.close();
            }

            // Java响应实体模版解释
            String resName = apiConfigObject.getString("responseName4C");
            ;
            String javaStr4Res = vu.mergeTemplate(m, "Entity4Response.vm");
            fu.writeFile(javaFolder, resName, ".java", javaStr4Res);

            // 单表Mapper模版解释
            String mapperStr = vu.mergeTemplate(m, "Mapper.vm");
            String mapperFolder = ppsc.getMapperPath() + AcsConstants.ACS4CUSTOMIZE + "/";
            File mapperDir = new File(mapperFolder);
            if (!mapperDir.exists())
                mapperDir.mkdirs();
            // 覆盖相同文件内容
            fu.writeFile(mapperFolder, apiClassName + "Mapper", ".xml", mapperStr);

            // Dao接口模版解释
            String daoStr = vu.mergeTemplate(m, "DaoInterface.vm");
            String daoFolder = ppsc.getDaoPath() + AcsConstants.ACS4CUSTOMIZE + "/";
            File daoDir = new File(daoFolder);
            if (!daoDir.exists())
                daoDir.mkdirs();
            // 覆盖相同文件内容
            fu.writeFile(daoFolder, "I" + apiClassName + "Dao", ".java", daoStr);

            // Service接口模版解释
            String serviceStr = vu.mergeTemplate(m, "ServiceInterface.vm");
            String serviceFolder = ppsc.getServPath() + AcsConstants.ACS4CUSTOMIZE + "/";
            File serviceDir = new File(serviceFolder);
            if (!serviceDir.exists())
                serviceDir.mkdirs();
            // 覆盖相同文件内容
            fu.writeFile(serviceFolder, "I" + apiClassName + "Service", ".java", serviceStr);

            // Service接口实现类模版解释
            String serviceImplStr = vu.mergeTemplate(m, "Service.vm");
            String serviceImplFolder = ppsc.getServImplPath() + AcsConstants.ACS4CUSTOMIZE + "/";
            File serviceImplDir = new File(serviceImplFolder);
            if (!serviceImplDir.exists())
                serviceImplDir.mkdirs();
            // 覆盖相同文件内容
            fu.writeFile(serviceImplFolder, apiClassName + "Service", ".java", serviceImplStr);

            // Controller控制器模版解释
            String controllerStr = vu.mergeTemplate(m, "Controller.vm");
            String controllerFolder = ppsc.getControllerPath() + AcsConstants.ACS4CUSTOMIZE + "/";
            File controllerDir = new File(controllerFolder);
            if (!controllerDir.exists())
                controllerDir.mkdirs();
            // 覆盖相同文件内容
            fu.writeFile(controllerFolder, apiClassName + "Controller", ".java", controllerStr);

            // JavaScript模版解释
            String jsFilePath = ppsc.getJsDirPath() + ppsc.getPci().getJsfdName() + ".js";
            // 如js对象文件已经存在则删除
            File jsFile = new File(jsFilePath);
            if (jsFile.exists()) {
                jsFile.delete();
            }
            String jsStr = vu.mergeTemplate(m, "Javascript.vm");
            File jsDir = new File(ppsc.getJsDirPath() + AcsConstants.ACS4CUSTOMIZE + "/");
            if (!jsDir.exists())
                jsDir.mkdirs();
            // 在相关文件末尾追加内容 --根据模版创建js实体对象
            // FileWriter writer = new FileWriter(jsFilePath,
            // true);//无法设置编码为：UTF-8，默认为：ISO-8859-1 or US-ASCII
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(jsFilePath, true), "UTF-8"));
            writer.write(jsStr);
            writer.close();

            //----------------同步生成WEB模块--Start-------------------------
            // 1、根据模版生成JS模块文件
            String apiModuleName = apiConfigObject.getString("apiName4C");//模块名称
            String apiModulePath = ppsc.getWpp().getWebModulePath() + apiModuleName.toLowerCase() + "\\";
            File apiModuleDir = new File(apiModulePath);
            if (!apiModuleDir.exists()) {
                apiModuleDir.mkdirs();
            }

            if (ppsc.isPagging()) {
                // 1.1查询分页
                // moduleview.js
                String apiModuleViewStr = vu4coding.mergeTemplate(m, "Page-ModuleView.vm");
                fu.writeFile(apiModulePath, apiModuleName, ".js", apiModuleViewStr);
                // modulecontroller.js
                String apiModuleControllerStr = vu4coding.mergeTemplate(m, "Page-ModuleController.vm");
                fu.writeFile(apiModulePath, apiModuleName + "Controller", ".js", apiModuleControllerStr);
                // modulemodel.js
                String apiModuleModelStr = vu4coding.mergeTemplate(m, "Page-ModuleModel.vm");
                fu.writeFile(apiModulePath, apiModuleName + "Model", ".js", apiModuleModelStr);
            } else {
                // 1.2查询不分页
                // moduleview.js
                String apiModuleViewStr = vu4coding.mergeTemplate(m, "NoPage-ModuleView.vm");
                fu.writeFile(apiModulePath, apiModuleName, ".js", apiModuleViewStr);
                // modulecontroller.js
                String apiModuleControllerStr = vu4coding.mergeTemplate(m, "NoPage-ModuleController.vm");
                fu.writeFile(apiModulePath, apiModuleName + "Controller", ".js", apiModuleControllerStr);
                // modulemodel.js
                String apiModuleModelStr = vu4coding.mergeTemplate(m, "NoPage-ModuleModel.vm");
                fu.writeFile(apiModulePath, apiModuleName + "Model", ".js", apiModuleModelStr);
            }

            // 2、更新WebModule配置文件
            org.json.JSONObject content = new org.json.JSONObject();
            content.put("text", apiConfigObject.getString("apiDesc4C"));// 模块名称
            content.put("id", apiModuleName + "Id");// 模块ID
            content.put("routeId", apiModuleName.toLowerCase());// 模块xtype
            content.put("leaf", true);// 是否叶子节点，非必须字段
            jpu.updateWebModule(ppsc.getWpp().getWebConfigPath(), content);
            //----------------同步生成WEB模块--End-------------------------

            jo.put("content", "自定义接口：" + apiClassName + "创建成功。");
        }
        // 返回解译内容
        if (jo.containsKey("content")) {
            logger.info("自定义接口：" + apiClassName + "创建成功。");
            jo.put("success", true);
        } else {
            logger.info("自定义接口：" + apiClassName + "创建失败。");
            jo.put("success", false);
        }

        return jo;
    }

    @RequestMapping(value = "/getprojects")
    @ResponseBody
    @ApiOperation(value = "获取已创建工程配置列表", httpMethod = "POST", notes = "获取已创建工程配置列表", response = ProjectParams.class)
    public JSONObject getProjects() {
        JSONArray ja = dbs.getProjects();
        JSONObject jo = new JSONObject();
        if (ja != null && ja.size() > 0) {
            jo.put("success", true);
            jo.put("pps", ja);
        } else {
            jo.put("success", false);
            jo.put("pps", "[]");
        }
        return jo;
    }

    @RequestMapping(value = "/deleteproject")
    @ResponseBody
    @ApiOperation(value = "根据工程名称删除工程文件", httpMethod = "GET", notes = "根据工程名称删除工程文件")
    public JSONObject deleteProject(
            @ApiParam(required = true, value = "工程名称", name = "projectName") @RequestParam(name = "projectName") String projectName) {
        boolean isSuccess = dbs.deleteProject(projectName);
        JSONObject jo = new JSONObject();
        if (isSuccess) {
            /**
             * 数据库配置信息删除之后，需要清空对应的工程文件。
             */
            boolean isDelJava = true;
            boolean isDelWeb = true;
            String sfp = Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1);
            String disk = (String) sfp.subSequence(0, 2);// 获取盘符
            String rootPath = disk + "\\" + AcsConstants.PROJECTWS;
            String dirPath = rootPath + "\\" + projectName;
            String wRootPath = rootPath + "\\web\\";
            String wDirPath = wRootPath + projectName;
            // 删除Java工程文件
            File projectFile = new File(dirPath);
            if (projectFile.exists()) {
                isDelJava = fu.deleteDir(projectFile);
            }

            // 删除Web工程文件
            File wProjectFile = new File(wDirPath);
            if (wProjectFile.exists()) {
                isDelWeb = fu.deleteDir(wProjectFile);
            }

            if (isDelJava && isDelWeb) {
                jo.put("success", true);
                jo.put("message", "工程删除成功!");
            } else {
                jo.put("success", false);
                jo.put("message", "因一些原因，工程配置删除成功，工程文件删除失败，请联系管理员，可能需要手动删除工程文件!");
            }
        } else {
            jo.put("success", false);
            jo.put("messsage", "工程删除失败，请检查传入的工程名称!");
        }
        return jo;
    }

    @RequestMapping(value = "/deletedatabase")
    @ResponseBody
    @ApiOperation(value = "根据数据库名称删除数据库配置", httpMethod = "GET", notes = "根据数据库名称删除数据库配置", tags = "数据库连接管理工具")
    public JSONObject deleteDataBase(
            @ApiParam(required = true, value = "数据库名称", name = "dbName") @RequestParam(name = "dbName") String dbName
    ) {
        boolean isSuccess = dbs.deleteDataBase(dbName);
        JSONObject jo = new JSONObject();
        if (isSuccess) {
            jo.put("success", true);
            jo.put("message", "数据库删除成功!");
        } else {
            jo.put("success", false);
            jo.put("messsage", "数据库删除失败，请检查传入的数据库名称!");
        }
        return jo;
    }

    @RequestMapping(value = "/deleteapigroup")
    @ResponseBody
    @ApiOperation(value = "根据组名删除接口分组", httpMethod = "GET", notes = "根据组名删除接口分组", tags = "业务接口管理")
    public JSONObject deleteApiGroup(
            @ApiParam(required = true, value = "接口分组名", name = "groupName") @RequestParam(name = "groupName") String groupName
    ) {
        boolean isSuccess = dbs.deleteApiGroup(groupName);
        JSONObject jo = new JSONObject();
        if (isSuccess) {
            jo.put("success", true);
            jo.put("message", "接口组名删除成功!");
        } else {
            jo.put("success", false);
            jo.put("messsage", "分组删除失败，请检查传入的接口组名!");
        }
        return jo;
    }

    @RequestMapping(value = "/getprojecttree")
    @ResponseBody
    @ApiOperation(value = "获取已创建工程文件目录树", httpMethod = "GET", notes = "获取已创建工程文件目录树")
    public JSONObject getProjectTree(
            @ApiParam(required = true, value = "工程名称", name = "projectName") @RequestParam(name = "projectName") String projectName) {

        String sfp = Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1);
        String disk = (String) sfp.subSequence(0, 2);// 获取盘符
        // 工程存放目录(该目录还需要存放工程配置及类配置文件)
        String rootPath = disk + "\\" + AcsConstants.PROJECTWS;
        String dirPath = rootPath + "\\" + projectName;
        JSONObject dirTree = new JSONObject();
        fu.scanDirectory(dirPath, dirTree);
        return dirTree;
    }

    @RequestMapping(value = "/coding4onekey")
    @ResponseBody
    @ApiOperation(value = "根据数据库表一键生成代码（非SQL解译版本，目前正在使用。）", httpMethod = "POST", notes = "根据数据库表一键生成代码")
    public JSONObject coding4OneKey(
            @ApiParam(required = true, value = "工程配置整合参数", name = "pps") @RequestParam(name = "pps") String pps,
            @ApiParam(required = true, value = "当前查询是否分页", name = "isPagging") @RequestParam(name = "isPagging") boolean isPagging,
            @ApiParam(required = true, value = "当前操作是否生成对应的JS模块", name = "isModuling") @RequestParam(name = "isModuling") boolean isModuling,
            @ApiParam(required = true, value = "当前操作是否生成web表格记录管理代码,如增删改", name = "isManaging") @RequestParam(name = "isManaging") boolean isManaging)
            throws IOException {
        JSONObject jo = new JSONObject();

        String ppsJson = URLDecoder.decode(pps, "UTF-8");
        JSONObject ppsObject = JSONObject.parseObject(ppsJson);
        ProjectParams ppsc = JSONObject.toJavaObject(ppsObject, ProjectParams.class);
        if (ppsc != null) {
            // 获取分页设置
            ppsc.setPagging(isPagging);

            // 获取JS模块生成设置
            ppsc.setModuling(isModuling);

            // 获取web表单管理设置
            ppsc.setManaging(isManaging);

            // 初始化模版引擎
            vu = new VelocityUtils(
                    Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1)
                            + "templates/acs4onekey");

            // 初始化模版引擎 for onekey WebModule生成
            vu4coding = new VelocityUtils(
                    Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1)
                            + "templates/web/module4onekey");

            String jsFilePath = ppsc.getJsDirPath() + ppsc.getPci().getJsfdName() + ".js";
            // 如js对象文件已经存在则删除
            File jsFile = new File(jsFilePath);
            if (jsFile.exists()) {
                jsFile.delete();
            }

            // 获取配置数据库表结构数据
            JSONArray ja = new JSONArray();

            // 获取库表信息
            List<Table> tbs;
            // 首先判断redis中是否已经存储了数据库表结构(key必须唯一)
            String key = ppsc.getDbci().getDbName() + "-" + ppsc.getDbci().getDbAddress() + "-"
                    + ppsc.getDbci().getDbPort() + "-" + ppsc.getDbci().getDbType() + "-" + ppsc.getDbci().getDbUser();
            String field = "tables";
            String tbstr = redisCache.hashGet(key, field);
            if (tbstr != null) {
                tbs = JSONObject.parseArray(tbstr, Table.class);
            } else {
                tbs = dbs.getTableInfoEx(ppsc.getDbci());
            }
            Iterator<Table> it = tbs.iterator();
            while (it.hasNext()) {
                JSONObject joItem = new JSONObject();
                Table t = it.next();

                // 模版参数注入(这里应该可以直接传入Table实例，是否可行后续测试)
                Map<String, Object> m = new HashMap<String, Object>();
                //根据生成方案不同放置在特定的包中,此处为根据库表一键生成接口
                m.put("acsId", AcsConstants.ACS4ONEKEY);
                // 获取并注入数据库类型
                String dbType = ppsc.getDbci().getDbType();
                if (dbType.toLowerCase().indexOf("oracle") > -1) {
                    m.put("dbType", "oracle");
                } else if (dbType.toLowerCase().indexOf("mysql") > -1) {
                    m.put("dbType", "mysql");
                } else if (dbType.toLowerCase().indexOf("postgresql") > -1) {
                    m.put("dbType", "postgresql");
                    //m.put("dbType", "mysql");//todo 20191008----这里完全按照MySQL的接口方式生成PostgreSQL接口，是否完全兼容待验证，后续逐步完善。
                }
                //m.put("dbType", ppsc.getDbci().getDbType().indexOf("oracle") > 0 ? "oracle" : "mysql");
                m.put("ppsc", ppsc);
                m.put("pagging", ppsc.isPagging());// 分页设置
                m.put("managing", ppsc.isManaging());// 记录管理设置
                m.put("colList", t.getColumns());
                m.put("projectPackageName", ppsc.getPci().getFpName() + "." + ppsc.getPci().getSpName());
                m.put("schemaName", t.getSchemaname());//todo 20200102----仅针对PG数据库模式，只需要根据数据库类型调整相应的Mapper文件
                m.put("className", t.getUpperCaseName());
                m.put("classLowName", t.getLowerCaseName());
                m.put("classAllLowName", t.getTablename().toLowerCase());
                m.put("nameSpace",
                        ppsc.getPci().getFpName() + "." + ppsc.getPci().getSpName() + "." + ppsc.getPci().getMfdName());
                m.put("tableName", t.getDbtablename());
                m.put("objectPkJavaType", t.getObjectPkJavaType());// Java接口模版中用到
                m.put("pkJavaType", t.getPkJavaType());// Mapper映射文件中用到
                joItem.put("apiName", t.getDbtablename() + "接口");
                joItem.put("apiDesc", t.getUpperCaseName() + "类");

                // Java实体模版解释
                String javaStr = vu.mergeTemplate(m, "Entity.vm");
                String javaFolder = ppsc.getEntityPath() + AcsConstants.ACS4ONEKEY + "/";
                File javaDir = new File(javaFolder);
                if (!javaDir.exists())
                    javaDir.mkdirs();
                // 覆盖相同文件内容
                fu.writeFile(javaFolder, t.getUpperCaseName(), ".java", javaStr);

                // 单表Mapper模版解释
                String mapperStr = vu.mergeTemplate(m, "Mapper.vm");
                String mapperFolder = ppsc.getMapperPath() + AcsConstants.ACS4ONEKEY + "/";
                File mapperDir = new File(mapperFolder);
                if (!mapperDir.exists())
                    mapperDir.mkdirs();
                // 覆盖相同文件内容
                fu.writeFile(mapperFolder, t.getUpperCaseName() + "Mapper", ".xml", mapperStr);

                // Dao接口模版解释
                String daoStr = vu.mergeTemplate(m, "DaoInterface.vm");
                String daoFolder = ppsc.getDaoPath() + AcsConstants.ACS4ONEKEY + "/";
                File daoDir = new File(daoFolder);
                if (!daoDir.exists())
                    daoDir.mkdirs();
                // 覆盖相同文件内容
                fu.writeFile(daoFolder, "I" + t.getUpperCaseName() + "Dao", ".java", daoStr);

                // Service接口模版解释
                String serviceStr = vu.mergeTemplate(m, "ServiceInterface.vm");
                String serviceFolder = ppsc.getServPath() + AcsConstants.ACS4ONEKEY + "/";
                File serviceDir = new File(serviceFolder);
                if (!serviceDir.exists())
                    serviceDir.mkdirs();
                // 覆盖相同文件内容
                fu.writeFile(serviceFolder, "I" + t.getUpperCaseName() + "Service", ".java", serviceStr);

                // Service接口实现类模版解释
                String serviceImplStr = vu.mergeTemplate(m, "Service.vm");
                String serviceImplFolder = ppsc.getServImplPath() + AcsConstants.ACS4ONEKEY + "/";
                File serviceImplDir = new File(serviceImplFolder);
                if (!serviceImplDir.exists())
                    serviceImplDir.mkdirs();
                // 覆盖相同文件内容
                fu.writeFile(serviceImplFolder, t.getUpperCaseName() + "Service", ".java", serviceImplStr);

                // Controller控制器模版解释
                String controllerStr = vu.mergeTemplate(m, "Controller.vm");
                String controllerFolder = ppsc.getControllerPath() + AcsConstants.ACS4ONEKEY + "/";
                File controllerDir = new File(controllerFolder);
                if (!controllerDir.exists())
                    controllerDir.mkdirs();
                // 覆盖相同文件内容
                fu.writeFile(controllerFolder, t.getUpperCaseName() + "Controller", ".java", controllerStr);

                // JavaScript模版解释
                String jsStr = vu.mergeTemplate(m, "Javascript.vm");
                File jsDir = new File(ppsc.getJsDirPath() + AcsConstants.ACS4ONEKEY + "/");
                if (!jsDir.exists())
                    jsDir.mkdirs();
                // 在相关文件末尾追加内容 --根据模版创建js实体对象
                // FileWriter writer = new FileWriter(jsFilePath,
                // true);//无法设置编码为：UTF-8，默认为：ISO-8859-1 or US-ASCII
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(jsFilePath, true), "UTF-8"));
                writer.write(jsStr);
                writer.close();

                // 代码创建文成
                ja.add(joItem);

                //----------------同步生成WEB模块--Start-------------------------
                // 1、根据模版生成JS模块文件
                String apiModuleName = t.getUpperCaseName();
                String apiModulePath = ppsc.getWpp().getWebModulePath() + apiModuleName.toLowerCase() + "\\";
                File apiModuleDir = new File(apiModulePath);
                if (!apiModuleDir.exists()) {
                    apiModuleDir.mkdirs();
                }

                if (ppsc.isPagging()) {
                    // 1.1查询分页
                    // moduleview.js
                    String apiModuleViewStr = vu4coding.mergeTemplate(m, "Page-ModuleView.vm");
                    fu.writeFile(apiModulePath, apiModuleName, ".js", apiModuleViewStr);
                    // modulecontroller.js
                    String apiModuleControllerStr = vu4coding.mergeTemplate(m, "Page-ModuleController.vm");
                    fu.writeFile(apiModulePath, apiModuleName + "Controller", ".js", apiModuleControllerStr);
                    // modulemodel.js
                    String apiModuleModelStr = vu4coding.mergeTemplate(m, "Page-ModuleModel.vm");
                    fu.writeFile(apiModulePath, apiModuleName + "Model", ".js", apiModuleModelStr);
                } else {
                    // 1.2查询不分页
                    // moduleview.js
                    String apiModuleViewStr = vu4coding.mergeTemplate(m, "NoPage-ModuleView.vm");
                    fu.writeFile(apiModulePath, apiModuleName, ".js", apiModuleViewStr);
                    // modulecontroller.js
                    String apiModuleControllerStr = vu4coding.mergeTemplate(m, "NoPage-ModuleController.vm");
                    fu.writeFile(apiModulePath, apiModuleName + "Controller", ".js", apiModuleControllerStr);
                    // modulemodel.js
                    String apiModuleModelStr = vu4coding.mergeTemplate(m, "NoPage-ModuleModel.vm");
                    fu.writeFile(apiModulePath, apiModuleName + "Model", ".js", apiModuleModelStr);
                }

                // 2、更新WebModule配置文件
                org.json.JSONObject content = new org.json.JSONObject();
                content.put("text", apiModuleName);// 模块名称
                content.put("id", apiModuleName + "Id");// 模块ID
                content.put("routeId", apiModuleName.toLowerCase());// 模块xtype
                content.put("leaf", true);// 是否叶子节点，非必须字段
                jpu.updateWebModule(ppsc.getWpp().getWebConfigPath(), content);
                //----------------同步生成WEB模块--End-------------------------

                logger.info(t.getDbtablename() + "--接口创建完成");
            }

            if (ja.size() > 0) {
                logger.info("提示：单表接口全部创建完成");
                jo.put("success", true);
                jo.put("es", JSONObject.toJSON(ja));
                return jo;
            }
        }
        jo.put("success", false);
        jo.put("es", "[]");
        return jo;
    }

    /*@RequestMapping(value = "/onekeycoder4all")
    @ResponseBody
    @ApiOperation(value = "根据数据库表一键生成代码(根据SQL解译改进版，目前暂停使用。)", httpMethod = "POST", notes = "根据数据库表一键生成代码(改进版)")
    public JSONObject oneKeyCoder4All(
            @ApiParam(required = true, value = "工程配置整合参数", name = "pps") @RequestParam(name = "pps") String pps)
            throws IOException {
        JSONObject jo = new JSONObject();

        String ppsJson = URLDecoder.decode(pps, "UTF-8");
        JSONObject ppsObject = JSONObject.parseObject(ppsJson);
        ProjectParams ppsc = JSONObject.toJavaObject(ppsObject, ProjectParams.class);
        if (ppsc != null) {
            // 获取配置数据库表结构数据
            JSONArray ja = new JSONArray();
            List<Table> dbts = null;
            // 首先判断redis中是否已经存储了数据库表结构
            String key = ppsc.getDbci().getDbName() + "-" + ppsc.getDbci().getDbAddress() + "-"
                    + ppsc.getDbci().getDbPort() + "-" + ppsc.getDbci().getDbType() + "-" + ppsc.getDbci().getDbUser();
            String field = "tables";
            String tbs = redisCache.hashGet(key, field);
            if (tbs != null) {
                dbts = JSONObject.parseArray(tbs, Table.class);
            } else {
                dbts = dbs.getTableInfoEx(ppsc.getDbci());
            }
            Iterator<Table> it = dbts.iterator();
            while (it.hasNext()) {
                JSONObject joItem = null;
                Table t = (Table) it.next();
                if (t != null) {
                    String sqlType = ppsc.getDbci().getDbType().toLowerCase().indexOf("oracle") > -1 ? "Oracle"
                            : "MySQL";
                    String tableName = t.getDbtablename();
                    String entityName = t.getUpperCaseName();// 接口类型名
                    String tableAlias = tableName.substring(0, 1).toString();
                    String apiName, apiDesc, requestName, requestDesc, responseName, responseDesc, operation, sqlText;
                    boolean isPagging, isModuling, isManaging;
                    String pkName = null;

                    // 获取当前表主键字段
                    List<Column> cols = t.getColumns();
                    String dbFields = "", dbFieldValues = "", exprFieldValues = "";
                    int colIndex = 0;
                    if (cols != null && cols.size() > 0) {
                        Iterator<Column> ic = cols.iterator();
                        while (ic.hasNext()) {
                            Column c = (Column) ic.next();
                            if (c != null) {
                                if ("1".equals(c.getPk())) {
                                    pkName = c.getDbname();
                                }

                                if (colIndex != cols.size() - 1) {
                                    dbFields += c.getDbname() + ", ";
                                    dbFieldValues += "'" + c.getDbname() + "', ";
                                    exprFieldValues += tableAlias + "." + c.getDbname() + "=" + "'" + c.getDbname()
                                            + "', ";
                                } else {
                                    dbFields += c.getDbname() + "";
                                    dbFieldValues += "'" + c.getDbname() + "'";
                                    exprFieldValues += tableAlias + "." + c.getDbname() + "=" + "'" + c.getDbname()
                                            + "'";
                                }

                                colIndex++;
                            }
                        }
                    }

                    // 待解译的SQL语句
                    // 1、select 所有记录,需要表别名的方式编写
                    operation = "Select";
                    apiName = operation + entityName;
                    apiDesc = apiName + "接口";
                    requestName = apiName + "Request";
                    requestDesc = apiName + "接口请求类";
                    responseName = apiName + "Response";
                    responseDesc = apiName + "接口响应类";
                    sqlText = operation + " " + tableAlias + ".* from " + tableName + " " + tableAlias;
                    isPagging = true;
                    isModuling = true;
                    isManaging = true;

                    JSONObject parseResult = this.sqlParse(sqlType, apiName, apiDesc, requestName, requestDesc,
                            responseName, responseDesc, sqlText, pps, isPagging, isModuling, isManaging, dbts);
                    boolean isSuccess = parseResult.getBooleanValue("success");
                    if (isSuccess) {
                        joItem = new JSONObject();
                        joItem.put("apiName", apiName);
                        joItem.put("apiDesc", apiDesc);
                        ja.add(joItem);
                    }

                    // 2、selcet 记录By ID,需要表别名的方式编写
                    apiName = operation + entityName + "ByID";
                    apiDesc = apiName + "接口";
                    requestName = apiName + "Request";
                    requestDesc = apiName + "接口请求类";
                    responseName = apiName + "Response";
                    responseDesc = apiName + "接口响应类";
                    sqlText = operation + " " + tableAlias + ".* from " + tableName + " " + tableAlias + " where "
                            + tableAlias + "." + pkName + "= 'ID值'";
                    isPagging = false;
                    isModuling = true;
                    isManaging = false;
                    parseResult = this.sqlParse(sqlType, apiName, apiDesc, requestName, requestDesc, responseName,
                            responseDesc, sqlText, pps, isPagging, isModuling, isManaging, dbts);
                    isSuccess = parseResult.getBooleanValue("success");
                    if (isSuccess) {
                        joItem = new JSONObject();
                        joItem.put("apiName", apiName);
                        joItem.put("apiDesc", apiDesc);
                        ja.add(joItem);
                    }

                    // 3、delete 记录By ID,不需要表别名的方式编写
                    operation = "Delete";
                    apiName = operation + entityName + "ByID";
                    apiDesc = apiName + "接口";
                    requestName = apiName + "Request";
                    requestDesc = apiName + "接口请求类";
                    responseName = apiName + "Response";
                    responseDesc = apiName + "接口响应类";
                    sqlText = operation + " from " + tableName + " where " + pkName + " = 'ID值'";
                    isPagging = false;
                    isModuling = false;
                    isManaging = false;
                    parseResult = this.sqlParse(sqlType, apiName, apiDesc, requestName, requestDesc, responseName,
                            responseDesc, sqlText, pps, isPagging, isModuling, isManaging, dbts);
                    isSuccess = parseResult.getBooleanValue("success");
                    if (isSuccess) {
                        joItem = new JSONObject();
                        joItem.put("apiName", apiName);
                        joItem.put("apiDesc", apiDesc);
                        ja.add(joItem);
                    }

                    // 4、 insert 记录,不需要表别名的方式编写
                    operation = "Insert";
                    apiName = operation + entityName;
                    apiDesc = apiName + "接口";
                    requestName = apiName + "Request";
                    requestDesc = apiName + "接口请求类";
                    responseName = apiName + "Response";
                    responseDesc = apiName + "接口响应类";
                    sqlText = operation + " into " + tableName + "(" + dbFields + ") values (" + dbFieldValues + ")";
                    isPagging = false;
                    isModuling = false;
                    isManaging = false;
                    parseResult = this.sqlParse(sqlType, apiName, apiDesc, requestName, requestDesc, responseName,
                            responseDesc, sqlText, pps, isPagging, isModuling, isManaging, dbts);
                    isSuccess = parseResult.getBooleanValue("success");
                    if (isSuccess) {
                        joItem = new JSONObject();
                        joItem.put("apiName", apiName);
                        joItem.put("apiDesc", apiDesc);
                        ja.add(joItem);
                    }

                    // 5、update 记录By ID,需要表别名的方式编写
                    operation = "Update";
                    apiName = operation + entityName + "ByID";
                    apiDesc = apiName + "接口";
                    requestName = apiName + "Request";
                    requestDesc = apiName + "接口请求类";
                    responseName = apiName + "Response";
                    responseDesc = apiName + "接口响应类";
                    sqlText = operation + " " + tableName + " " + tableAlias + " set " + exprFieldValues + " where "
                            + tableAlias + "." + pkName + " = 'ID值'";
                    isPagging = false;
                    isModuling = false;
                    isManaging = false;
                    parseResult = this.sqlParse(sqlType, apiName, apiDesc, requestName, requestDesc, responseName,
                            responseDesc, sqlText, pps, isPagging, isModuling, isManaging, dbts);
                    isSuccess = parseResult.getBooleanValue("success");
                    if (isSuccess) {
                        joItem = new JSONObject();
                        joItem.put("apiName", apiName);
                        joItem.put("apiDesc", apiDesc);
                        ja.add(joItem);
                    }
                }
            }

            if (ja != null && ja.size() > 0) {
                jo.put("success", true);
                jo.put("es", JSONObject.toJSON(ja));
                return jo;
            }
        }

        jo.put("success", false);
        jo.put("es", "[]");
        return jo;
    }*/

    @RequestMapping(value = "/sqlparse4all")
    @ResponseBody
    @ApiOperation(value = "解译SQL语句并一键生成代码(升级版)", httpMethod = "POST", notes = "解译SQL语句并一键生成代码(升级版)")
    public JSONObject sqlParse4All(
            @ApiParam(required = true, value = "接口分组标签", name = "apiGroup") @RequestParam(name = "apiGroup") String apiGroup,
            @ApiParam(required = true, value = "当前连接的数据库类型", name = "sqlType") @RequestParam(name = "sqlType") String sqlType,
            @ApiParam(required = true, value = "即将生成代码请求接口名称", name = "apiName") @RequestParam(name = "apiName") String apiName,
            @ApiParam(required = true, value = "即将生成代码请求接口描述", name = "apiDesc") @RequestParam(name = "apiDesc") String apiDesc,
            @ApiParam(required = true, value = "即将生成代码请求类型名称", name = "requestName") @RequestParam(name = "requestName") String requestName,
            @ApiParam(required = true, value = "即将生成代码请求类型描述", name = "requestDesc") @RequestParam(name = "requestDesc") String requestDesc,
            @ApiParam(required = true, value = "即将生成代码响应类型名称", name = "responseName") @RequestParam(name = "responseName") String responseName,
            @ApiParam(required = true, value = "即将生成代码响应类型描述", name = "responseDesc") @RequestParam(name = "responseDesc") String responseDesc,
            @ApiParam(required = true, value = "当前输入的SQL语句", name = "sqlText") @RequestParam(name = "sqlText") String sqlText,
            @ApiParam(required = true, value = "工程配置整合参数", name = "pps") @RequestParam(name = "pps") String pps,
            @ApiParam(required = true, value = "当前查询是否分页", name = "isPagging") @RequestParam(name = "isPagging") boolean isPagging,
            @ApiParam(required = true, value = "当前操作是否生成对应的JS模块", name = "isModuling") @RequestParam(name = "isModuling") boolean isModuling,
            @ApiParam(required = true, value = "当前操作是否生成web表格记录管理代码,如增删改", name = "isManaging") @RequestParam(name = "isManaging") boolean isManaging)
            throws IOException {
        return this.sqlParse(apiGroup, sqlType, apiName, apiDesc, requestName, requestDesc, responseName, responseDesc, sqlText,
                pps, isPagging, isModuling, isManaging, null);
    }

    @RequestMapping(value = "/sqlparse")
    @ResponseBody
    @ApiOperation(value = "解译SQL语句并一键生成代码", httpMethod = "POST", notes = "解译SQL语句并一键生成代码")
    public JSONObject sqlParse(
            @ApiParam(required = true, value = "接口分组标签", name = "apiGroup") @RequestParam(name = "apiGroup") String apiGroup,
            @ApiParam(required = true, value = "当前连接的数据库类型", name = "sqlType") @RequestParam(name = "sqlType") String sqlType,
            @ApiParam(required = true, value = "即将生成代码请求接口名称", name = "apiName") @RequestParam(name = "apiName") String apiName,
            @ApiParam(required = true, value = "即将生成代码请求接口描述", name = "apiDesc") @RequestParam(name = "apiDesc") String apiDesc,
            @ApiParam(required = true, value = "即将生成代码请求类型名称", name = "requestName") @RequestParam(name = "requestName") String requestName,
            @ApiParam(required = true, value = "即将生成代码请求类型描述", name = "requestDesc") @RequestParam(name = "requestDesc") String requestDesc,
            @ApiParam(required = true, value = "即将生成代码响应类型名称", name = "responseName") @RequestParam(name = "responseName") String responseName,
            @ApiParam(required = true, value = "即将生成代码响应类型描述", name = "responseDesc") @RequestParam(name = "responseDesc") String responseDesc,
            @ApiParam(required = true, value = "当前输入的SQL语句", name = "sqlText") @RequestParam(name = "sqlText") String sqlText,
            @ApiParam(required = true, value = "工程配置整合参数", name = "pps") @RequestParam(name = "pps") String pps,
            @ApiParam(required = true, value = "当前查询是否分页", name = "isPagging") @RequestParam(name = "isPagging") boolean isPagging,
            @ApiParam(required = true, value = "当前操作是否生成对应的JS模块", name = "isModuling") @RequestParam(name = "isModuling") boolean isModuling,
            @ApiParam(required = true, value = "当前操作是否生成web表格记录管理代码,如增删改", name = "isManaging") @RequestParam(name = "isManaging") boolean isManaging,
            @ApiParam(required = false, value = "获取当前数据库表", name = "tableList") @RequestParam(name = "tableList") List<Table> tableList)
            throws IOException {
        // 默认先保存接口分组标签
        boolean isSaved = dbs.saveApiGroup(apiGroup);
        logger.info(isSaved ? "接口分组标签保存成功" : "接口分组标签保存失败");

        JSONObject jo = new JSONObject();

        String ppsJson = URLDecoder.decode(pps, "UTF-8");
        JSONObject ppsObject = JSONObject.parseObject(ppsJson);
        ProjectParams ppsc = JSONObject.toJavaObject(ppsObject, ProjectParams.class);

        if (ppsc != null) {
            // 获取分页设置
            ppsc.setPagging(isPagging);

            // 获取JS模块生成设置
            ppsc.setModuling(isModuling);

            // 获取web表单管理设置
            ppsc.setManaging(isManaging);

            // 获取库表信息
            List<Table> tbs = null;
            if (tableList == null) {
                // 首先判断redis中是否已经存储了数据库表结构
                String key = ppsc.getDbci().getDbName() + "-" + ppsc.getDbci().getDbAddress() + "-"
                        + ppsc.getDbci().getDbPort() + "-" + ppsc.getDbci().getDbType() + "-" + ppsc.getDbci().getDbUser();
                String field = "tables";
                String tbstr = redisCache.hashGet(key, field);
                if (tbstr != null) {
                    tbs = JSONObject.parseArray(tbstr, Table.class);
                } else {
                    tbs = dbs.getTableInfoEx(ppsc.getDbci());
                }
            } else {
                tbs = tableList;
            }

            logger.info("当前数据库类型： " + sqlType.toLowerCase());
            // 解析SQL语句
            JSONObject sqlObj = spu.sqlParser(sqlText, sqlType.toLowerCase());

            // 根据库表结构解析sql语句
            if (tbs != null && tbs.size() > 0 && sqlObj != null) {
                JSONObject sqlDecodeObject = new JSONObject();

                JSONArray tbsArray = (JSONArray) JSONObject.toJSON(tbs);
                // 请求体
                JSONArray rqArray = sqlObj.getJSONArray("requestJa");
                // 响应体
                JSONArray rpArray = sqlObj.getJSONArray("responseJa");
                // 查询表
                JSONArray qtArray = sqlObj.getJSONArray("tableJa");
                // 处理后SQL
                String formatedSql = sqlObj.getString("formatedSql");
                String[] operationTypes = {"select", "delete", "update", "insert"};
                // 计算操作类型
                String operation = "";
                for (int i = 0; i < operationTypes.length; i++) {
                    operation = operationTypes[i];
                    if (formatedSql.toLowerCase().indexOf(operation) == 0)
                        break;
                }

                // 0、接口信息
                JSONObject apiModel = new JSONObject();
                apiModel.put("apiGroup", apiGroup.trim().equalsIgnoreCase("") ? "未分组" : apiGroup.trim());//接口分组
                apiModel.put("apiOperation", operation);//接口操作
                apiModel.put("apiName", apiName);//接口名
                apiModel.put("apiNameLow", apiName.substring(0, 1).toLowerCase() + apiName.substring(1));
                apiModel.put("apiNameFullLow", apiName.toLowerCase());
                apiModel.put("apiDesc", apiDesc);
                sqlDecodeObject.put("apiModel", apiModel);

                // 1、需要生成的请求实体模型信息
                JSONObject reqModel = new JSONObject();
                reqModel.put("requestDesc", requestDesc);
                reqModel.put("upperCaseName", requestName);
                reqModel.put("lowerCaseName",
                        requestName.subSequence(0, 1).toString().toLowerCase() + requestName.substring(1));
                reqModel.put("columns", new JSONArray());

                // 获取请求体数组
                JSONArray reqModelCols = reqModel.getJSONArray("columns");

                // 请求体解译
                if (rqArray != null && rqArray.size() > 0) {
                    int rqLen = rqArray.size();
                    for (int i = 0; i < rqLen; i++) {
                        JSONObject rqObj = rqArray.getJSONObject(i);
                        String name = rqObj.getString("name");// 请求体名称
                        String type = rqObj.getString("type");// 请求体类型
                        int index = rqObj.getInteger("index");// 请求体所在格式化语句中索引
                        String body = rqObj.getString("body");// 请求体符号

                        // 有.号表示：表别名+字段真名
                        if (name.contains(".")) {
                            String[] ns = name.split("\\.");
                            String ta = ns[0].trim();// 表别名
                            String fn = ns[1].trim();// 字段真名
                            // 根据当前字段对应的表别名查找对应的表真名
                            if (qtArray != null && qtArray.size() > 0) {
                                for (int j = 0; j < qtArray.size(); j++) {
                                    JSONObject qtObj = qtArray.getJSONObject(j);
                                    String alias = qtObj.getString("alias");// 表别名
                                    String tn = qtObj.getString("name");// 表真名
                                    if (ta.equalsIgnoreCase(alias)) {
                                        // 根据表真名查找数据库对应的表信息
                                        for (int k = 0; k < tbsArray.size(); k++) {
                                            JSONObject tbObj = tbsArray.getJSONObject(k);
                                            String tbName = tbObj.getString("dbtablename");
                                            String tbSchema = tbObj.getString("schemaname");
                                            String tbNameInSchema = tbSchema != null ? tbSchema + "." + tbName : tbName;
                                            if (tbName.equalsIgnoreCase(tn) || tbNameInSchema.equalsIgnoreCase(tn)) {
                                                JSONArray cols = tbObj.getJSONArray("columns");// 当前表所有字段信息
                                                for (int m = 0; m < cols.size(); m++) {
                                                    JSONObject colObj = cols.getJSONObject(m);
                                                    String fbdName = colObj.getString("dbname");
                                                    // 获取当前字段信息
                                                    if (fbdName.equalsIgnoreCase(fn)) {
                                                        if (type.equalsIgnoreCase("between")) {
                                                            JSONObject betweenColObj = (JSONObject) colObj.clone();
                                                            betweenColObj.put("upname",
                                                                    "Between" + colObj.getString("upname") + ta.toUpperCase());// 大写字母开头,可作为Java类型名
                                                            betweenColObj.put("javaname",
                                                                    "between" + colObj.getString("upname") + ta.toUpperCase());// 小写字母开头，可作为Java变量名
                                                            betweenColObj.put("reqIndex", index);// 表达式开始索引
                                                            betweenColObj.put("reqBody", " between ?");// 请求体符号，ba特殊处理
                                                            betweenColObj.put("reqName", name);// 一般有表别名+字段名组成
                                                            betweenColObj.put("desc",
                                                                    "[" + colObj.getString("desc")
                                                                            + "]Between{1}and{2}第1个值" + "("
                                                                            + colObj.getString("javaType") + ")");// 请求字段描述

                                                            JSONObject andColObj = (JSONObject) colObj.clone();
                                                            andColObj.put("upname", "And" + colObj.getString("upname"));
                                                            andColObj.put("javaname",
                                                                    "and" + colObj.getString("upname"));
                                                            andColObj.put("reqIndex", index + 11);// 此处+11表示定位到and索引位置
                                                            andColObj.put("reqBody", " and ?");// 请求体符号，ba特殊处理
                                                            andColObj.put("reqName", name);
                                                            andColObj.put("desc",
                                                                    "[" + colObj.getString("desc")
                                                                            + "]Between{1}and{2}第2个值" + "("
                                                                            + colObj.getString("javaType") + ")");

                                                            reqModel.getJSONArray("columns").add(betweenColObj);
                                                            reqModel.getJSONArray("columns").add(andColObj);
                                                        } else if (type.equalsIgnoreCase("like")) {
                                                            JSONObject likeColObj = (JSONObject) colObj.clone();
                                                            likeColObj.put("upname",
                                                                    "Like" + colObj.getString("upname") + ta.toUpperCase());
                                                            likeColObj.put("javaname",
                                                                    "like" + colObj.getString("upname") + ta.toUpperCase());
                                                            likeColObj.put("reqIndex", index);
                                                            likeColObj.put("reqBody", body);
                                                            likeColObj.put("reqName", name);
                                                            likeColObj.put("desc", "[" + colObj.getString("desc")
                                                                    + "]模糊查询(" + colObj.getString("javaType") + ")");
                                                            likeColObj.put("javaType", "String");// 这里默认采用String类型，需要传入%暂时忽略字段原本的类型
                                                            likeColObj.put("like", true);// 这里特殊标注为模糊查询
                                                            reqModel.getJSONArray("columns").add(likeColObj);
                                                        } else if (type.equalsIgnoreCase("in")) {
                                                            JSONObject inColObj = (JSONObject) colObj.clone();
                                                            inColObj.put("upname",
                                                                    "In" + colObj.getString("upname") + ta.toUpperCase());
                                                            inColObj.put("javaname",
                                                                    "in" + colObj.getString("upname") + ta.toUpperCase());
                                                            inColObj.put("reqIndex", index);
                                                            inColObj.put("reqBody", body);
                                                            inColObj.put("reqName", name);
                                                            inColObj.put("desc", "[" + colObj.getString("desc")
                                                                    + "]in or not in查询(" + colObj.getString("javaType") + ")");
                                                            inColObj.put("javaType", "String");
                                                            inColObj.put("in", true);
                                                            reqModel.getJSONArray("columns").add(inColObj);
                                                        } else {
                                                            //判断是否存在重复的字段
                                                            int count = spu.judgeFieldExist(colObj.getString("upname"),
                                                                    reqModelCols);
                                                            JSONObject normalColObj = (JSONObject) colObj.clone();
                                                            if (count > 0) {
                                                                normalColObj.put("upname",
                                                                        ta.substring(0, 1).toString().toUpperCase()
                                                                                + ta.substring(1)
                                                                                + colObj.getString("upname") + count);
                                                                normalColObj.put("javaname",
                                                                        ta + colObj.getString("upname") + count);
                                                            } else {
                                                                normalColObj.put("upname",
                                                                        ta.substring(0, 1).toString().toUpperCase()
                                                                                + ta.substring(1)
                                                                                + colObj.getString("upname"));
                                                                normalColObj.put("javaname",
                                                                        ta + colObj.getString("upname"));
                                                            }
                                                            normalColObj.put("reqIndex", index);
                                                            normalColObj.put("reqBody", body);
                                                            normalColObj.put("reqName", name);
                                                            normalColObj.put("desc", colObj.getString("desc") + "("
                                                                    + colObj.getString("javaType") + ")");
                                                            reqModel.getJSONArray("columns").add(normalColObj);
                                                        }
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                        } else {
                            // 情况1：SQL语句没有采用别名的方式编写，默认为单表操作，这里的字段为真实字段名称，这里强制要求是单表查询
                            if (qtArray != null && qtArray.size() == 1) {
                                for (int j = 0; j < qtArray.size(); j++) {
                                    JSONObject qtObj = qtArray.getJSONObject(j);
                                    String alias = qtObj.getString("alias");// 表别名
                                    String tn = qtObj.getString("name");// 表真名
                                    if (alias == null) {
                                        // 根据表真名查找数据库对应的表信息
                                        for (int k = 0; k < tbsArray.size(); k++) {
                                            JSONObject tbObj = tbsArray.getJSONObject(k);
                                            String tbName = tbObj.getString("dbtablename");
                                            String tbSchema = tbObj.getString("schemaname");
                                            String tbNameInSchema = tbSchema != null ? tbSchema + "." + tbName : tbName;
                                            if (tbName.equalsIgnoreCase(tn) || tbNameInSchema.equalsIgnoreCase(tn)) {
                                                JSONArray cols = tbObj.getJSONArray("columns");// 当前表所有字段信息
                                                for (int m = 0; m < cols.size(); m++) {
                                                    JSONObject colObj = cols.getJSONObject(m);
                                                    String fbdName = colObj.getString("dbname");
                                                    // 获取当前字段信息
                                                    if (fbdName.equalsIgnoreCase(name)) {
                                                        int count = spu.judgeFieldExist(colObj.getString("upname"),
                                                                reqModelCols);
                                                        JSONObject normalColObj = (JSONObject) colObj.clone();
                                                        if (count > 0) {
                                                            normalColObj.put("upname",
                                                                    colObj.getString("upname") + count);
                                                            normalColObj.put("javaname",
                                                                    colObj.getString("javaname") + count);
                                                        } else {
                                                            normalColObj.put("upname", colObj.getString("upname"));
                                                            normalColObj.put("javaname", colObj.getString("javaname"));
                                                        }
                                                        normalColObj.put("reqIndex", index);
                                                        normalColObj.put("reqBody", body);
                                                        normalColObj.put("reqName", name);
                                                        normalColObj.put("desc", colObj.getString("desc") + "("
                                                                + colObj.getString("javaType") + ")");
                                                        reqModel.getJSONArray("columns").add(normalColObj);
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                // 情况2：此字段为别名，与具体的表无关.
                                JSONObject aliasColObj = new JSONObject();
                                int count = spu.judgeFieldExist(name, reqModelCols);
                                if (count > 0) {
                                    aliasColObj.put("upname",
                                            name.substring(0, 1).toString().toUpperCase() + name.substring(1) + count);
                                    aliasColObj.put("javaname",
                                            name.substring(0, 1).toString().toLowerCase() + name.substring(1) + count);
                                } else {
                                    aliasColObj.put("upname",
                                            name.substring(0, 1).toString().toUpperCase() + name.substring(1));
                                    aliasColObj.put("javaname",
                                            name.substring(0, 1).toString().toLowerCase() + name.substring(1));
                                }

                                aliasColObj.put("javaType", "String");// 这里默认采用String类型，暂时忽略具体类型
                                aliasColObj.put("dbname", name);
                                aliasColObj.put("reqIndex", index);
                                aliasColObj.put("reqBody", body);
                                aliasColObj.put("reqName", name);
                                aliasColObj.put("pk", 0);// 0表示非主键
                                aliasColObj.put("desc", name + ",这是一个别名变量。");
                                reqModel.getJSONArray("columns").add(aliasColObj);
                            }
                        }
                    }
                    sqlDecodeObject.put("reqModel", reqModel);
                    // System.out.println(reqModel.toJSONString());
                }

                // 2、需要生成的响应实体模型信息
                JSONObject resModel = new JSONObject();
                resModel.put("responseDesc", responseDesc);
                resModel.put("upperCaseName", responseName);
                resModel.put("lowerCaseName",
                        responseName.subSequence(0, 1).toString().toLowerCase() + responseName.substring(1));
                resModel.put("columns", new JSONArray());

                // 响应体解译
                if (rpArray != null && rpArray.size() > 0) {
                    int rpLen = rpArray.size();
                    for (int i = 0; i < rpLen; i++) {
                        JSONObject rqObj = rpArray.getJSONObject(i);
                        boolean isAlias = rqObj.getBooleanValue("isAlias");// 响应体是否为别名
                        String name = rqObj.getString("name");// 响应体名称
                        // 响应实体为别名
                        if (isAlias) {
                            // 此时字段为别名，与具体的表无关
                            JSONObject aliasColObj = new JSONObject();
                            aliasColObj.put("upname",
                                    name.substring(0, 1).toString().toUpperCase() + name.substring(1));
                            aliasColObj.put("javaname",
                                    name.substring(0, 1).toString().toLowerCase() + name.substring(1));
                            aliasColObj.put("javaType", "String");// 这里默认采用String类型，暂时忽略具体类型
                            aliasColObj.put("dbType", "VARCHAR");// 这里默认采用VARCHAR类型，暂时忽略具体类型
                            aliasColObj.put("dbname", name);
                            aliasColObj.put("desc", name + ",这是一个别名变量。");
                            resModel.getJSONArray("columns").add(aliasColObj);
                        } else {
                            if (name.indexOf(".") > -1) {
                                String[] ns = name.split("\\.");
                                String ta = ns[0].trim();// 表别名
                                String fn = ns[1].trim();// 字段真名(可能含有通配符 *)
                                // 根据当前字段对应的表别名查找对应的表真名
                                if (qtArray != null && qtArray.size() > 0) {
                                    for (int j = 0; j < qtArray.size(); j++) {
                                        JSONObject qtObj = qtArray.getJSONObject(j);
                                        String alias = qtObj.getString("alias");// 表别名
                                        String tn = qtObj.getString("name");// 表真名
                                        if (ta.equalsIgnoreCase(alias)) {
                                            // 根据表真名查找数据库对应的表信息
                                            for (int k = 0; k < tbsArray.size(); k++) {
                                                JSONObject tbObj = tbsArray.getJSONObject(k);
                                                String tbName = tbObj.getString("dbtablename");
                                                String tbSchema = tbObj.getString("schemaname");
                                                String tbNameInSchema = tbSchema != null ? tbSchema + "." + tbName : tbName;
                                                if (tbName.equalsIgnoreCase(tn) || tbNameInSchema.equalsIgnoreCase(tn)) {
                                                    JSONArray cols = tbObj.getJSONArray("columns");// 当前表所有字段信息
                                                    if (!fn.equalsIgnoreCase("*")) {
                                                        // 不含通配符，仅需要匹配当前字段
                                                        for (int m = 0; m < cols.size(); m++) {
                                                            JSONObject colObj = cols.getJSONObject(m);
                                                            String fbdName = colObj.getString("dbname");
                                                            if (fbdName.equalsIgnoreCase(fn)) {
                                                                JSONObject normalColObj = (JSONObject) colObj.clone();
                                                                normalColObj.put("upname",
                                                                        ta.substring(0, 1).toString().toUpperCase()
                                                                                + ta.substring(1)
                                                                                + colObj.getString("upname"));
                                                                normalColObj.put("javaname",
                                                                        ta + colObj.getString("upname"));
                                                                resModel.getJSONArray("columns").add(normalColObj);
                                                                break;
                                                            }
                                                        }
                                                    } else {
                                                        // 若为通配符*，则响应体包含该表所有字段
                                                        for (int m = 0; m < cols.size(); m++) {
                                                            JSONObject colObj = cols.getJSONObject(m);
                                                            JSONObject normalColObj = (JSONObject) colObj.clone();
                                                            normalColObj.put("upname",
                                                                    ta.substring(0, 1).toString().toUpperCase()
                                                                            + ta.substring(1)
                                                                            + colObj.getString("upname"));
                                                            normalColObj.put("javaname",
                                                                    ta + colObj.getString("upname"));
                                                            resModel.getJSONArray("columns").add(normalColObj);
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    sqlDecodeObject.put("resModel", resModel);
                    // System.out.println(resModel.toJSONString());
                }

                // 3、MyBatis映射文件SQL语句参数化，可结合请求体及已经预处理的SQL语句合并解译
                if (!formatedSql.isEmpty()) {
                    // 获取请求实体中变量
                    /*JSONArray reqArr = reqModel.getJSONArray("columns");*/
                    JSONArray reqArr = Sort4JSONArrayUtil.JsonArraySort(reqModel.getJSONArray("columns"));//通过排序修正请求参数解译顺序错乱问题
                    if (reqArr != null && reqArr.size() > 0) {
                        int reqColsLen = reqArr.size();
                        for (int i = 0; i < reqColsLen; i++) {
                            JSONObject colObj = reqArr.getJSONObject(i);
                            if (colObj != null) {
                                int index = colObj.getIntValue("reqIndex");
                                String pName = colObj.getString("javaname");
                                boolean isLike = colObj.getBooleanValue("like");
                                boolean isIn = colObj.getBooleanValue("in");
                                // 从当前索引开始第一个遇到的？用MyBatis映射文件标准参数书写方式替换
                                // @TODO like子句mybatis映射
                                if (isLike) {
                                    formatedSql = formatedSql.substring(0, index) + formatedSql.substring(index)
                                            .replaceFirst("\\?", "CONCAT(CONCAT('%', #{" + pName + "}),'%')");
                                } else if (isIn) {
                                    formatedSql = formatedSql.substring(0, index) + formatedSql.substring(index)
                                            .replaceFirst("\\(\\?\\)", "<foreach collection=\"" + pName + "List\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach>");
                                } else {
                                    formatedSql = formatedSql.substring(0, index)
                                            + formatedSql.substring(index).replaceFirst("\\?", "#{" + pName + "}");
                                }
                            }
                        }
                        sqlDecodeObject.put("formatedSql", formatedSql);
                        // System.out.println("MyBatis映射SQL体：" + formatedSql);
                    } else {
                        // 没有请求体无需做基于MyBatis的映射处理，保持原样赋值。
                        sqlDecodeObject.put("formatedSql", formatedSql);
                        // System.out.println("MyBatis映射SQL体：" + formatedSql);
                    }
                }

                // 4、根据SQL解译信息采用VM模板引擎生成SMVC代码
                this.sql4JavaInterface(sqlDecodeObject, ppsc);

                // 是否生成JS模块代码
                if (isModuling && operation.equalsIgnoreCase("select")) {
                    // 5、根据SQL解译信息采用VM模版引擎生成JMVC代码,只有查询时才生成JS模块
                    this.sql4WebModule(sqlDecodeObject, ppsc);
                }

                jo.put("content", sqlDecodeObject);
            }
        }

        // 返回解译内容
        if (jo.containsKey("content")) {
            jo.put("success", true);
        } else {
            jo.put("success", false);
        }

        return jo;
    }

    /**
     * 一键生成Web模块代码
     *
     * @param sqlObject 待生成处理的sql对象
     * @param ppsc      工程配置信息
     * @throws IOException
     */
    private void sql4WebModule(JSONObject sqlObject, ProjectParams ppsc) throws IOException {
        if (sqlObject != null && ppsc != null) {
            // 初始化模版引擎
            vu = new VelocityUtils(
                    Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1)
                            + "templates/web/module4sql");
            // System.out.println(StringUtils.class.getClassLoader().getResource("/").getPath());
            /*
             * vu = new VelocityUtils(
             * StringUtils.class.getClassLoader().getResource("/").getPath().substring(1) +
             * "templates/js");
             */
            // API信息(其中包含了操作信息)
            JSONObject apiObj = sqlObject.getJSONObject("apiModel");
            // Request信息
            JSONObject reqObj = sqlObject.getJSONObject("reqModel");
            // Response信息
            JSONObject resObj = sqlObject.getJSONObject("resModel");

            // 模版参数注入
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("so", sqlObject);// sql信息
            m.put("ao", apiObj);// api信息
            if (reqObj != null) {
                m.put("req", reqObj);// request信息
                m.put("reqCols", reqObj.getJSONArray("columns"));// request columns信息
            } else {
                // 当不存在请求条件时，请求体为空，此时需要特殊处理
                m.put("req", null);// request信息
                m.put("reqCols", null);// request columns信息
            }
            if (resObj != null) {
                m.put("res", resObj);// response信息
                m.put("resCols", resObj.getJSONArray("columns"));// response columns信息
            } else {
                // 当更新、删除、插入操作时，响应体可能为空，此时需要特殊处理。
                m.put("res", null);// response信息
                m.put("resCols", null);// response columns信息
            }
            m.put("ppsc", ppsc);// 工程配置
            m.put("pagging", ppsc.isPagging());// 分页设置
            m.put("managing", ppsc.isManaging());// 记录管理设置
            m.put("ppn", ppsc.getPci().getFpName() + "." + ppsc.getPci().getSpName());// 包名根目录（顶级包+次级包）

            // 1、根据模版生成JS模块文件
            String apiModuleName = apiObj.getString("apiName");
            String apiModulePath = ppsc.getWpp().getWebModulePath() + apiModuleName.toLowerCase() + "\\";
            File apiModuleDir = new File(apiModulePath);
            if (!apiModuleDir.exists()) {
                apiModuleDir.mkdirs();
            }

            if (ppsc.isPagging()) {
                // 1.1查询分页
                // moduleview.js
                String apiModuleViewStr = vu.mergeTemplate(m, "Page-ModuleView.vm");
                fu.writeFile(apiModulePath, apiModuleName, ".js", apiModuleViewStr);
                // modulecontroller.js
                String apiModuleControllerStr = vu.mergeTemplate(m, "Page-ModuleController.vm");
                fu.writeFile(apiModulePath, apiModuleName + "Controller", ".js", apiModuleControllerStr);
                // modulemodel.js
                String apiModuleModelStr = vu.mergeTemplate(m, "Page-ModuleModel.vm");
                fu.writeFile(apiModulePath, apiModuleName + "Model", ".js", apiModuleModelStr);
            } else {
                // 1.2查询不分页
                // moduleview.js
                String apiModuleViewStr = vu.mergeTemplate(m, "NoPage-ModuleView.vm");
                fu.writeFile(apiModulePath, apiModuleName, ".js", apiModuleViewStr);
                // modulecontroller.js
                String apiModuleControllerStr = vu.mergeTemplate(m, "NoPage-ModuleController.vm");
                fu.writeFile(apiModulePath, apiModuleName + "Controller", ".js", apiModuleControllerStr);
                // modulemodel.js
                String apiModuleModelStr = vu.mergeTemplate(m, "NoPage-ModuleModel.vm");
                fu.writeFile(apiModulePath, apiModuleName + "Model", ".js", apiModuleModelStr);
            }

            // 2、更新WebModule配置文件
            org.json.JSONObject content = new org.json.JSONObject();
            content.put("text", apiObj.getString("apiDesc"));// 模块名称
            content.put("id", apiObj.getString("apiName") + "Id");// 模块ID
            content.put("routeId", apiObj.getString("apiName").toLowerCase());// 模块xtype
            content.put("leaf", true);// 是否叶子节点，非必须字段
            jpu.updateWebModule(ppsc.getWpp().getWebConfigPath(), content);
        }
    }

    /**
     * 采用SQL一键生成Java代码
     *
     * @throws IOException
     */
    private void sql4JavaInterface(JSONObject sqlObject, ProjectParams ppsc) throws IOException {
        if (sqlObject != null && ppsc != null) {
            // 初始化模版引擎
            vu = new VelocityUtils(
                    Thread.currentThread().getContextClassLoader().getResource("/").getPath().substring(1)
                            + "templates/acs4sql");// 如 E:/T9/webapps/codecreator/WEB-INF/classes/
            /*
             * vu = new VelocityUtils(
             * StringUtils.class.getClassLoader().getResource("/").getPath().substring(1) +
             * "templates/acs4sql");
             */
            // API信息
            JSONObject apiObj = sqlObject.getJSONObject("apiModel");
            // Request信息
            JSONObject reqObj = sqlObject.getJSONObject("reqModel");
            // Response信息
            JSONObject resObj = sqlObject.getJSONObject("resModel");

            // 模版参数注入
            Map<String, Object> m = new HashMap<String, Object>();
            //根据生成方案不同放置在特定的包中
            m.put("acsId", AcsConstants.ACS4SQL);
            m.put("so", sqlObject);// sql信息
            m.put("ao", apiObj);// api信息
            if (reqObj != null) {
                m.put("req", reqObj);// request信息
                m.put("reqCols", reqObj.getJSONArray("columns"));// request columns信息
            } else {
                // 当不存在请求条件时，请求体为空，此时需要特殊处理
                m.put("req", null);// request信息
                m.put("reqCols", null);// request columns信息
            }
            if (resObj != null) {
                m.put("res", resObj);// response信息
                m.put("resCols", resObj.getJSONArray("columns"));// response columns信息
            } else {
                // 当更新、删除、插入操作时，响应体可能为空，此时需要特殊处理。
                m.put("res", null);// response信息
                m.put("resCols", null);// response columns信息
            }
            m.put("ppsc", ppsc);// 工程配置
            m.put("pagging", ppsc.isPagging());// 分页设置
            m.put("ppn", ppsc.getPci().getFpName() + "." + ppsc.getPci().getSpName());// 包名根目录（顶级包+次级包）

            // 实体存放目录
            String javaFolder = ppsc.getEntityPath() + AcsConstants.ACS4SQL + "/";
            File javaDir = new File(javaFolder);
            if (!javaDir.exists())
                javaDir.mkdirs();

            // 请求实体
            String reqName = null;
            if (reqObj != null) {
                reqName = reqObj.getString("upperCaseName");
            }
            if (reqName != null) {
                String jsFilePath = ppsc.getJsDirPath() + AcsConstants.ACS4SQL + "/" + reqName + ".js";
                // 如js对象文件已经存在则删除
                File jsFile = new File(jsFilePath);
                if (jsFile.exists()) {
                    jsFile.delete();
                }

                // Java请求实体模版解释
                String javaStr4Req = vu.mergeTemplate(m, "Entity4Request.vm");
                fu.writeFile(javaFolder, reqName, ".java", javaStr4Req);

                // JavaScript存放目录
                File jsDir = new File(ppsc.getJsDirPath() + AcsConstants.ACS4SQL + "/");
                if (!jsDir.exists())
                    jsDir.mkdirs();

                // JavaScript模版解释
                String jsStr4Js = vu.mergeTemplate(m, "Javascript.vm");
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(jsFilePath, true), "UTF-8"));
                writer.write(jsStr4Js);
                writer.close();
            }

            // Java响应实体模版解释
            String resName = null;
            if (resObj != null) {
                resName = resObj.getString("upperCaseName");
                String javaStr4Res = vu.mergeTemplate(m, "Entity4Response.vm");
                fu.writeFile(javaFolder, resName, ".java", javaStr4Res);
            }

            // Mapping存放目录
            String mapperFolder = ppsc.getMapperPath() + AcsConstants.ACS4SQL + "/";
            File mapperDir = new File(mapperFolder);
            if (!mapperDir.exists())
                mapperDir.mkdirs();

            // Mapper模版解释
            String mapperName = apiObj.getString("apiName");
            String mapperStr4Mapper = vu.mergeTemplate(m, "Mapper.vm");
            fu.writeFile(mapperFolder, mapperName + "Mapper", ".xml", mapperStr4Mapper);

            // Dao接口存放目录
            String daoFolder = ppsc.getDaoPath() + AcsConstants.ACS4SQL + "/";
            File daoDir = new File(daoFolder);
            if (!daoDir.exists())
                daoDir.mkdirs();

            // Dao接口模版解释
            String daoName = apiObj.getString("apiName");
            String daoStr4Dao = vu.mergeTemplate(m, "DaoInterface.vm");
            fu.writeFile(daoFolder, "I" + daoName + "Dao", ".java", daoStr4Dao);

            // Service接口模版解释
            String serviceName = apiObj.getString("apiName");
            String serviceStr = vu.mergeTemplate(m, "ServiceInterface.vm");
            String serviceFolder = ppsc.getServPath() + AcsConstants.ACS4SQL + "/";
            File serviceDir = new File(serviceFolder);
            if (!serviceDir.exists())
                serviceDir.mkdirs();
            // 覆盖相同文件内容
            fu.writeFile(serviceFolder, "I" + serviceName + "Service", ".java", serviceStr);

            // Service接口实现类模版解释
            String serviceImplStr = vu.mergeTemplate(m, "Service.vm");
            String serviceImplFolder = ppsc.getServImplPath() + AcsConstants.ACS4SQL + "/";
            File serviceImplDir = new File(serviceImplFolder);
            if (!serviceImplDir.exists())
                serviceImplDir.mkdirs();
            // 覆盖相同文件内容
            fu.writeFile(serviceImplFolder, serviceName + "Service", ".java", serviceImplStr);

            // Controller存放目录
            String controllerFolder = ppsc.getControllerPath() + AcsConstants.ACS4SQL + "/";
            File controllerDir = new File(controllerFolder);
            if (!controllerDir.exists())
                controllerDir.mkdirs();

            // Controller模版解释
            String controllerName = apiObj.getString("apiName");
            String controllerStr4Api = vu.mergeTemplate(m, "Controller.vm");
            fu.writeFile(controllerFolder, controllerName + "Controller", ".java", controllerStr4Api);
        }
    }
}
