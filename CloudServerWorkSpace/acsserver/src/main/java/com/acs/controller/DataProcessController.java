package com.acs.controller;

import com.acs.model.database.DBConnectInfo;
import com.acs.model.file.FileInfo;
import com.acs.util.PinyinUtil;
import com.acs.util.db.DataProcessSupport;
import com.acs.util.gdal.GdalUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author LBM
 * @time 2019年06月24日
 * @project acs
 * @type DataProcessController
 * @desc 数据处理工具类
 */
@Api(value = "dataprocess", description = "数据处理工具类", tags = "数据处理工具类")
@Controller
@Scope("session") // 将bean 的范围设置成session，表示当前bean是会话级别且线程安全的变量。
@RequestMapping(value = "/dataprocess", method = {RequestMethod.GET, RequestMethod.POST})
public class DataProcessController {
    private Logger logger = LogManager.getLogger(DataProcessController.class);

    //wkt所采用的坐标系ID
    @Value("#{sysConfig[srid]}")
    private int srid;

    //文件上传存放目录
    @Value("#{sysConfig[upload_dir]}")
    private String uploadDir;

    //每一批次入库记录数
    @Value("#{sysConfig[max_commit]}")
    private long maxCommit;

    @RequestMapping(value = "/fileupload")
    @ResponseBody
    @ApiOperation(value = "文件上传", httpMethod = "POST", notes = "文件上传", tags = "数据处理工具类")
    public void fileUploadHandler(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTf-8");
        //根据request header获取数据库连接配置信息
        String dbName = request.getHeader("dbName");
        String dbType = request.getHeader("dbType");
        String dbAddress = request.getHeader("dbAddress");
        String dbPort = request.getHeader("dbPort");
        String dbUser = request.getHeader("dbUser");
        String dbPassword = request.getHeader("dbPassword");
        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);

        //初始化数据处理工具类
        DataProcessSupport dataProcessSupport = new DataProcessSupport(dbci);

        PinyinUtil pinyinUtil = new PinyinUtil();

        DefaultMultipartHttpServletRequest dmhsq = (DefaultMultipartHttpServletRequest) request;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String today = formatter.format(new Date().getTime());
        //根据配置设定文件上传目录
        String path = uploadDir + "/" + today;//request.getServletContext().getRealPath("/up");
        File folder = new File(path);
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }
        //检查我们是否有文件上传请求
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            try {
                Map<String, MultipartFile> fileMap = dmhsq.getFileMap();
                for (String key : fileMap.keySet()) {
                    MultipartFile m = fileMap.get(key);
                    String fileName = m.getOriginalFilename();
                    String filePath = path + "/" + fileName;
                    long fileSize = m.getSize();
                    File newfile = new File(filePath);
                    //保存文件
                    m.transferTo(newfile);

                    //显示数据
                    if (fileName != null && fileName.indexOf(".shp") > -1 && fileName.indexOf(".xml") == -1) {
                        FileInfo fi = new FileInfo();
                        String fileNameNoSuffix = fileName.substring(0, fileName.indexOf(".shp"));

                        fi.setDbName(dbName);
                        fi.setFileName(fileNameNoSuffix);
                        fi.setTableName(pinyinUtil.getPinYin4All(fileNameNoSuffix));
                        fi.setFileType("shapefile");//todo 目前只处理shapefile文件
                        fi.setFileSize(fileSize);//todo 此处仅记录shp文件的大小
                        fi.setFilePath(filePath);
                        fi.setUploadDate(String.valueOf(new Date().getTime()));
                        fi.setFileState(-1);

                        //需要将记录保存到关联数据库表中
                        if (dataProcessSupport.createUploadTable()) {
                            boolean isUpload = dataProcessSupport.recordFileInfo(fi);
                            if (isUpload) {
                                logger.info(fileName.substring(0, fileName.indexOf(".shp")) + "::上传信息记录成功");
                            } else {
                                logger.info(fileName.substring(0, fileName.indexOf(".shp")) + "::上传信息记录失败");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/filelist")
    @ResponseBody
    @ApiOperation(value = "文件上传记录列表", httpMethod = "GET", notes = "文件上传记录列表", tags = "数据处理工具类")
    public JSONObject fileInfoListHandler(
            @ApiParam(required = true, value = "数据库驱动类型", name = "dbType") @RequestParam(name = "dbType") String dbType,
            @ApiParam(required = true, value = "数据库名称", name = "dbName") @RequestParam(name = "dbName") String dbName,
            @ApiParam(required = true, value = "数据库连接IP地址", name = "dbAddress") @RequestParam(name = "dbAddress") String dbAddress,
            @ApiParam(required = true, value = "数据库连接端口", name = "dbPort") @RequestParam(name = "dbPort") String dbPort,
            @ApiParam(required = true, value = "数据库连接用户名", name = "dbUser") @RequestParam(name = "dbUser") String dbUser,
            @ApiParam(required = true, value = "数据库连接密码", name = "dbPassword") @RequestParam(name = "dbPassword") String dbPassword,
            @ApiParam(required = true, value = "文件处理状态", name = "state", example = "123") @RequestParam(name = "state") int state
    ) {
        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);

        //初始化数据处理工具类
        DataProcessSupport dataProcessSupport = new DataProcessSupport(dbci);
        JSONObject jo = new JSONObject();
        JSONArray jsonArray = dataProcessSupport.getFileInfoList(state);
        //数据字段分析
        if (jsonArray != null) {
            int size = jsonArray.size();
            for (int i = 0; i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //执行元数据解析
                String fileDesc = jsonObject.getString("filedesc");
                if (fileDesc.equalsIgnoreCase("")) {
                    String fileName = jsonObject.getString("filename");
                    String tableName = jsonObject.getString("tablename");
                    String filePath = jsonObject.getString("filepath");
                    if (!filePath.equalsIgnoreCase("")) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            GdalUtil gdalUtil = new GdalUtil();
                            JSONArray fsqlList = gdalUtil.metadata4Shp(filePath);
                            String metaDatas = fsqlList.toString();
                            //组装创建表sql
                            String ctSql = gdalUtil.generateSqlText(fileName, tableName, fsqlList);
                            jsonObject.put("ctsql", ctSql);
                            boolean isSuccess = dataProcessSupport.updateFileMeta(fileName, metaDatas, ctSql);
                            if (isSuccess) {
                                //组装shp元数据
                                jsonObject.put("filedesc", metaDatas);
                            }
                        }
                    }
                }
            }
            jo.put("data", jsonArray);
        }

        return jo;
    }

    @RequestMapping(value = "/shp2db")
    @ResponseBody
    @ApiOperation(value = "shp文件入库", httpMethod = "GET", notes = "shp文件入库", tags = "数据处理工具类")
    public boolean shp2db(
            @ApiParam(required = true, value = "数据库驱动类型", name = "dbType") @RequestParam(name = "dbType") String dbType,
            @ApiParam(required = true, value = "数据库名称", name = "dbName") @RequestParam(name = "dbName") String dbName,
            @ApiParam(required = true, value = "数据库连接IP地址", name = "dbAddress") @RequestParam(name = "dbAddress") String dbAddress,
            @ApiParam(required = true, value = "数据库连接端口", name = "dbPort") @RequestParam(name = "dbPort") String dbPort,
            @ApiParam(required = true, value = "数据库连接用户名", name = "dbUser") @RequestParam(name = "dbUser") String dbUser,
            @ApiParam(required = true, value = "数据库连接密码", name = "dbPassword") @RequestParam(name = "dbPassword") String dbPassword,
            @ApiParam(required = true, value = "要素处理状态", name = "state", example = "123") @RequestParam(name = "state") int state
    ) {
        //0、获取待处理文件清单
        //1、根据shp文件元数据创建关联表
        //2、文件解析入库
        //3、状态变更或删除记录和文件

        long startTime = System.currentTimeMillis();

        boolean isImported = false;

        DBConnectInfo dbci = new DBConnectInfo();
        dbci.setDbType(dbType);
        dbci.setDbName(dbName);
        dbci.setDbAddress(dbAddress);
        dbci.setDbPort(dbPort);
        dbci.setDbUser(dbUser);
        dbci.setDbPassword(dbPassword);
        //初始化数据处理工具类
        DataProcessSupport dataProcessSupport = new DataProcessSupport(dbci);

        //数据处理工具类
        GdalUtil gdalUtil = new GdalUtil();

        //0、清单
        JSONArray jsonArray = dataProcessSupport.getFileInfoList(state);
        if (jsonArray != null) {
            int size = jsonArray.size();
            for (int i = 0; i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String ctsql = jsonObject.getString("ctsql");
                //1、建表
                isImported = dataProcessSupport.createFeatureTable(ctsql);
                if (isImported) {
                    //2、解析
                    String filePath = jsonObject.getString("filepath");
                    String fileName = jsonObject.getString("filename");
                    String tableName = jsonObject.getString("tablename");
                    String fileDesc = jsonObject.getString("filedesc");
                    logger.info(fileName + "--入库中...");
                    isImported = gdalUtil.decodeShapeFile(dbci, filePath, maxCommit, tableName, fileName, fileDesc, srid);
                    if (isImported) {
                        long endTime = System.currentTimeMillis();
                        long handlerCost = endTime - startTime;
                        logger.info(fileName + "--入库完成," + "耗时: [" + handlerCost + "ms ]");
                    } else {
                        logger.info(fileName + "--入库失败");
                    }
                }
            }
        }
        return isImported;
    }
}
