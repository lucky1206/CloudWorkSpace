package com.acs.controller;

import com.acs.model.satellite.TleGroupInfo;
import com.acs.util.UUIDUtil;
import com.acs.util.db.SatelliteSupport;
import com.acs.util.gdal.GdalUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.satellite.utils.SatelliteUtil;
import com.satellite.utils.TleUtil;
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

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author LBM
 * @time 2019年07月22日
 * @project cloudserver
 * @type SatelliteController
 * @desc 卫星轨道预测分析工具类
 */
@Api(value = "satellite", description = "卫星轨道预测分析工具类", tags = "卫星轨道预测分析工具类")
@Controller
@Scope("session") // 将bean 的范围设置成session，表示当前bean是会话级别且线程安全的变量。
@RequestMapping(value = "/satellite", method = {RequestMethod.GET, RequestMethod.POST})
public class SatelliteController {
    private Logger logger = LogManager.getLogger(SatelliteController.class);

    @RequestMapping(value = "/initenvironment")
    @ResponseBody
    @ApiOperation(value = "初始化卫星轨迹预测运行环境", httpMethod = "GET", notes = "初始化卫星轨迹预测运行环境", tags = "卫星轨道预测分析工具类")
    public JSONObject initEnvironment() {
        JSONObject jsonObject = new JSONObject();
        SatelliteSupport satelliteSupport = new SatelliteSupport(null);
        boolean isSuccess = satelliteSupport.initSatellite();
        if (isSuccess) {
            jsonObject.put("msg", "初始化环境成功");
        } else {
            jsonObject.put("msg", "初始化环境失败或表已创建");
        }
        return jsonObject;
    }

    @RequestMapping(value = "/reloadtles")
    @ResponseBody
    @ApiOperation(value = "重载全部tle数据", httpMethod = "GET", notes = "重载全部tle数据", tags = "卫星轨道预测分析工具类")
    public boolean reloadTles() {
        boolean isSuccess = false;
        try {
            SatelliteSupport satelliteSupport = new SatelliteSupport(null);
            //若无tle表则创建
            satelliteSupport.initSatellite();
            //删除已有数据
            isSuccess = satelliteSupport.clearTles();
            if (isSuccess) {
                TleUtil tleUtil = new TleUtil();
                ClassLoader classLoader = SatelliteController.class.getClassLoader();
                URL resource = classLoader.getResource("tles.txt");
                if (resource != null) {
                    String path = resource.getPath();
                    String[] tles = tleUtil.loadTle(path);
                    if (tles != null && tles.length > 0) {
                        isSuccess = satelliteSupport.saveTles(tles);
                    } else {
                        isSuccess = false;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("TLE重置失败");
            isSuccess = false;
        } finally {
            return isSuccess;
        }
    }

    @RequestMapping(value = "/predict4satellite")
    @ResponseBody
    @ApiOperation(value = "根据卫星足迹圆分析过境区域", httpMethod = "POST", notes = "单颗卫星预测", tags = "卫星轨道预测分析工具类")
    public JSONObject predict4satellite(
            @ApiParam(value = "卫星足迹圆wkt", name = "fpcs") @RequestParam(name = "fpcs") String fpcs
    ) {
        SatelliteSupport satelliteSupport = new SatelliteSupport(null);
        //计算单颗卫星过境（省）信息
        return satelliteSupport.calcPos4SingleSate(fpcs);
    }

    @RequestMapping(value = "/predict4region")
    @ResponseBody
    @ApiOperation(value = "按照行政省预测卫星轨迹,若设置tleId则遍历全部tle数据进行计算", httpMethod = "GET", notes = "分组卫星批量预测", tags = "卫星轨道预测分析工具类")
    public JSONObject predict4region(
            @ApiParam(value = "卫星分组 Group ID", name = "groupId") @RequestParam(name = "groupId") String groupId,
            @ApiParam(value = "预测时间(长整型, 0表示当前时刻)", name = "date", defaultValue = "0", example = "123") @RequestParam(name = "date") long date,
            @ApiParam(value = "地面基站纬度", name = "latitude", defaultValue = "0", example = "123") @RequestParam(required = false, name = "latitude") double latitude,
            @ApiParam(value = "地面基站经度", name = "longitude", defaultValue = "0", example = "123") @RequestParam(required = false, name = "longitude") double longitude,
            @ApiParam(value = "地面站的高度高于平均海平面，以米为单位", name = "heightAMSL", defaultValue = "0", example = "123") @RequestParam(required = false, name = "heightAMSL") double heightAMSL
    ) {
        JSONObject jsonObject = new JSONObject();
        SatelliteUtil satelliteUtil = new SatelliteUtil();
        SatelliteSupport satelliteSupport = new SatelliteSupport(null);
        long st = System.currentTimeMillis();
        try {
            //1、tle数组
            JSONArray tleArray = satelliteSupport.getTlesByGroupId(groupId);
            //2、卫星位置信息数组
            JSONArray satPosArray;
            if (date > 0) {
                Date predictDate = new Date();
                predictDate.setTime(date);
                satPosArray = satelliteUtil.calcSatePos(tleArray, predictDate, latitude, longitude, heightAMSL);
            } else {
                satPosArray = satelliteUtil.calcSatePos(tleArray, null, latitude, longitude, heightAMSL);
            }

            //基于gdal的内存空间分析,该方案效率优于数据库分析，
            //3、行政省界
            JSONArray regionArray = satelliteSupport.getProvinces();

            //4、计算行政省当前时间过境卫星
            GdalUtil gdalUtil = new GdalUtil();
            JSONObject predictJsonObject = gdalUtil.spatialAnalysis4Sate(satPosArray, regionArray);

            //基于mysql数据库的空间分析
            //JSONObject predictJsonObject = satelliteSupport.spatialAnalysis4Sate(satPosArray);

            jsonObject.put("data", predictJsonObject);
            jsonObject.put("msg", "位置预测成功");
        } catch (Exception e) {
            logger.error("位置预测失败");
            jsonObject.put("msg", "位置预测失败");
        } finally {
            long et = System.currentTimeMillis();
            logger.info("本次计算耗时: " + (et - st) + " 毫秒");
            return jsonObject;
        }
    }

    @RequestMapping(value = "/gettlegrouplist")
    @ResponseBody
    @ApiOperation(value = "获取tle分组列表", httpMethod = "GET", notes = "获取tle分组列表", tags = "卫星轨道预测分析工具类")
    public JSONObject getTleGroupList() {
        JSONObject jsonObject = new JSONObject();
        SatelliteSupport satelliteSupport = new SatelliteSupport(null);
        JSONArray groupList = satelliteSupport.getTlesGroup();
        jsonObject.put("data", groupList);
        jsonObject.put("msg", "数组获取成功");
        return jsonObject;
    }

    @RequestMapping(value = "/gettlesbygroup")
    @ResponseBody
    @ApiOperation(value = "根据分组查询tles列表", httpMethod = "GET", notes = "根据分组查询tles列表", tags = "卫星轨道预测分析工具类")
    public JSONObject getTlesByGroup(
            @ApiParam(value = "卫星分组Group ID", name = "groupId") @RequestParam(required = true, name = "groupId") String groupId
    ) {
        JSONObject jsonObject = new JSONObject();
        SatelliteSupport satelliteSupport = new SatelliteSupport(null);
        JSONArray tleList = satelliteSupport.getTlesByGroupId(groupId);
        jsonObject.put("data", tleList);
        jsonObject.put("msg", "卫星列表获取成功");
        return jsonObject;
    }

    //根据关键字查询tle数据
    @RequestMapping(value = "/gettlesbyname")
    @ResponseBody
    @ApiOperation(value = "根据关键字查询tles列表", httpMethod = "GET", notes = "根据关键字查询tles列表", tags = "卫星轨道预测分析工具类")
    public JSONObject getTlesByName(
            @ApiParam(value = "卫星名称关键字，支持模糊查询", name = "tleName") @RequestParam(name = "tleName") String tleName
    ) {
        JSONObject jsonObject = new JSONObject();
        SatelliteSupport satelliteSupport = new SatelliteSupport(null);
        JSONArray tleList = satelliteSupport.getTlesByName(tleName);
        jsonObject.put("data", tleList);
        jsonObject.put("msg", "卫星列表获取成功");
        return jsonObject;
    }

    //分组管理接口，如新建、编辑保存、
    @RequestMapping(value = "/addsategroup")
    @ResponseBody
    @ApiOperation(value = "新建Tles Group配置", httpMethod = "POST", notes = "新建Tles Group配置", tags = "卫星轨道预测分析工具类")
    public JSONObject addSateGroup(
            @ApiParam(value = "分组名称", name = "groupName") @RequestParam(name = "groupName") String groupName,
            @ApiParam(value = "用户名称", name = "groupUser") @RequestParam(name = "groupUser") String groupUser,
            @ApiParam(value = "用户ID", name = "groupUserId") @RequestParam(name = "groupUserId") String groupUserId,
            @ApiParam(value = "分组描述", name = "groupDesc") @RequestParam(name = "groupDesc") String groupDesc,
            @ApiParam(value = "分组tleid集合", name = "groupTles") @RequestParam(name = "groupTles") String groupTles
    ) {
        JSONObject jsonObject = new JSONObject();
        SatelliteSupport satelliteSupport = new SatelliteSupport(null);
        TleGroupInfo tleGroupInfo = new TleGroupInfo();
        tleGroupInfo.setGroupName(groupName);
        tleGroupInfo.setGroupUser(groupUser);
        tleGroupInfo.setGroupUserId(groupUserId);
        tleGroupInfo.setGroupId(UUIDUtil.getUUID());
        tleGroupInfo.setGroupDesc(groupDesc);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowDate = formatter.format(new Date().getTime());
        tleGroupInfo.setGroupDate(nowDate);
        String[] tleIds = groupTles.split(",");
        tleGroupInfo.setRefTleIds(tleIds);
        if (satelliteSupport.addTlesGroup(tleGroupInfo)) {
            jsonObject.put("state", 1);
        } else {
            jsonObject.put("state", -1);
        }
        return jsonObject;
    }

    @RequestMapping(value = "/editsategroup")
    @ResponseBody
    @ApiOperation(value = "编辑Tles Group配置", httpMethod = "POST", notes = "编辑Tles Group配置", tags = "卫星轨道预测分析工具类")
    public JSONObject editSateGroup(
            @ApiParam(value = "分组ID", name = "groupId") @RequestParam(name = "groupId") String groupId,
            @ApiParam(value = "分组名称", name = "groupName") @RequestParam(name = "groupName") String groupName,
            @ApiParam(value = "用户名称", name = "groupUser") @RequestParam(name = "groupUser") String groupUser,
            @ApiParam(value = "用户ID", name = "groupUserId") @RequestParam(name = "groupUserId") String groupUserId,
            @ApiParam(value = "分组描述", name = "groupDesc") @RequestParam(name = "groupDesc") String groupDesc,
            @ApiParam(value = "分组tleid集合", name = "groupTles") @RequestParam(name = "groupTles") String groupTles
    ) {
        JSONObject jsonObject = new JSONObject();
        SatelliteSupport satelliteSupport = new SatelliteSupport(null);
        TleGroupInfo tleGroupInfo = new TleGroupInfo();
        tleGroupInfo.setGroupName(groupName);
        tleGroupInfo.setGroupUser(groupUser);
        tleGroupInfo.setGroupUserId(groupUserId);
        tleGroupInfo.setGroupId(groupId);
        tleGroupInfo.setGroupDesc(groupDesc);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowDate = formatter.format(new Date().getTime());
        tleGroupInfo.setGroupDate(nowDate);
        String[] tleIds = groupTles.split(",");
        tleGroupInfo.setRefTleIds(tleIds);
        if (satelliteSupport.editTlesGroup(tleGroupInfo)) {
            jsonObject.put("state", 1);
        } else {
            jsonObject.put("state", -1);
        }
        return jsonObject;
    }

    @RequestMapping(value = "/deletesategroup")
    @ResponseBody
    @ApiOperation(value = "删除Tles Group配置", httpMethod = "POST", notes = "删除Tles Group配置", tags = "卫星轨道预测分析工具类")
    public JSONObject deleteSateGroup(
            @ApiParam(value = "分组ID", name = "groupId") @RequestParam(name = "groupId") String groupId
    ) {
        JSONObject jsonObject = new JSONObject();
        SatelliteSupport satelliteSupport = new SatelliteSupport(null);
        if (satelliteSupport.deleteTlesGroup(groupId)) {
            jsonObject.put("state", 1);
        } else {
            jsonObject.put("state", -1);
        }
        return jsonObject;
    }
}
