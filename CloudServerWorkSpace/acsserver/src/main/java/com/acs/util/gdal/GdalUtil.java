/**
 *
 */
package com.acs.util.gdal;

import com.acs.model.database.DBConnectInfo;
import com.acs.util.PinyinUtil;
import com.acs.util.db.DataProcessSupport;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vividsolutions.jts.io.WKTReader;
/*import net.sf.json.JSONArray;
import net.sf.json.JSONObject;*/
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gdal.gdal.gdal;
import org.gdal.ogr.*;
import org.geotools.geojson.geom.GeometryJSON;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

/**shp文件限制，可查看站点：https://www.cnblogs.com/longcsy/p/8759755.html
 * @author winnerlbm
 * @date 2018年6月5日
 * @desc
 */
public class GdalUtil {
    private Logger logger = LogManager.getLogger(GdalUtil.class);
    //小数位数
    private static final int dotNum = 12;

    /**
     * @param args
     */
    public void main(String[] args) {
        // TODO Auto-generated method stub
    }

    /** wkt转json
     * @param wkt
     * @return
     */
    private String geoToJson(String wkt) {
        String json = null;
        try {
            WKTReader reader = new WKTReader();
            com.vividsolutions.jts.geom.Geometry geometry = reader.read(wkt);
            StringWriter writer = new StringWriter();
            GeometryJSON g = new GeometryJSON(dotNum);
            g.write(geometry, writer);
            json = writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**json转wkt
     * @param geoJson
     * @return
     */
    private String jsonToWkt(String geoJson) {
        String wkt = null;
        GeometryJSON gjson = new GeometryJSON();
        Reader reader = new StringReader(geoJson);
        try {
            com.vividsolutions.jts.geom.Geometry geometry = gjson.read(reader);
            wkt = geometry.toText();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wkt;
    }

    /**
     * GDAL字段类型转MYSQL字段类型
     * @param name
     * @param gdalType
     * @return
     */
    private JSONObject gdalType2MySqlType(String name, int gdalType) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        String mysqlType;
        String sqlFragment;
        switch (gdalType) {
            case ogr.OFTInteger: {
                mysqlType = "int(32)";
                break;
            }
            case ogr.OFTReal: {
                mysqlType = "double";
                break;
            }
            case ogr.OFTString: {
                mysqlType = "varchar(200)";
                break;
            }
            case ogr.OFTWideString: {
                mysqlType = "mediumtext";
                break;
            }
            case ogr.OFTDate:
            case ogr.OFTTime:
            case ogr.OFTDateTime: {
                mysqlType = "datetime";
                break;
            }
            case ogr.OFTInteger64: {
                mysqlType = "int(64)";
                break;
            }
            default: {
                //默认字符串类型
                mysqlType = "varchar(200)";
                break;
            }
        }

        sqlFragment = name + " " + mysqlType + " DEFAULT NULL COMMENT '" + name + "'";
        jsonObject.put("type", mysqlType);
        jsonObject.put("sql", sqlFragment);
        return jsonObject;
    }

    /**
     * 根据字段信息组装建表SQL文本
     * @param fileName 文件名
     * @param tabelName 表名
     * @param sqlJson 字段信息列表
     * @return
     */
    public String generateSqlText(String fileName, String tabelName, JSONArray sqlJson) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("CREATE TABLE IF NOT EXISTS " + tabelName + " (\n");
        List fieldNames = new ArrayList<String>();
        for (int i = 0; i < sqlJson.size(); i++) {
            JSONObject sj = sqlJson.getJSONObject(i);
            stringBuffer.append("\t" + sj.getString("sql") + ",\n");
            fieldNames.add(sj.getString("name"));
        }
        if (!fieldNames.contains("AREA")) {
            stringBuffer.append("\tAREA double DEFAULT NULL COMMENT '面积（k㎡）',\n");
        }
        if (!fieldNames.contains("X")) {
            stringBuffer.append("\tX double DEFAULT NULL COMMENT '中心经度(°)',\n");
        }
        if (!fieldNames.contains("Y")) {
            stringBuffer.append("\tY double DEFAULT NULL COMMENT '中心纬度(°)',\n");
        }
        if (!fieldNames.contains("FUID")) {
            stringBuffer.append("\tFUID varchar(100) NOT NULL COMMENT '要素唯一标识',\n");
        }
        stringBuffer.append("\tGEOM geometry NOT NULL COMMENT '点/线/面WKT几何对象',\n");
        stringBuffer.append("\tPRIMARY KEY (FUID),\n");
        stringBuffer.append("\tUNIQUE KEY FUID_UNIQUE (FUID),\n");
        stringBuffer.append("\tKEY DATAINDEX (FUID)\n");
        stringBuffer.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='" + fileName + "'");
        return stringBuffer.toString();
    }

    /**
     * 获取shp文件元数据
     * @param path
     * @return
     */
    public JSONArray metadata4Shp(String path) {
        gdal.AllRegister();
        // 为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
        // 为了使属性表字段支持中文，请添加下面这句
        gdal.SetConfigOption("SHAPE_ENCODING", "CP936");
        // 打开数据
        DataSource ds = null;
        PinyinUtil pinyinUtil = new PinyinUtil();
        try {
            ds = ogr.Open(path, 0);
            if (ds != null) {
                // 获取该数据源中的图层个数，一般shp数据图层只有一个，如果是mdb、dxf等图层就会有多个
                int iLayerCount = ds.GetLayerCount();
                if (iLayerCount > 0) {
                    // 获取第一个图层
                    Layer oLayer = ds.GetLayerByIndex(0);
                    if (oLayer == null) {
                        System.out.println("获取第0个图层失败！\n");
                    }

                    // 对图层进行初始化，如果对图层进行了过滤操作，执行这句后，之前的过滤全部清空
                    oLayer.ResetReading();

                    // 获取图层中的属性表表头并输出
                    FeatureDefn oDefn = oLayer.GetLayerDefn();
                    int iFieldCount = oDefn.GetFieldCount();

                    // 通过json对象封装所有要素信息,遍历要素提取数据
                    JSONArray jArray = new JSONArray();
                    for (int iField = 0; iField < iFieldCount; iField++) {
                        FieldDefn oFieldDefn = oDefn.GetFieldDefn(iField);
                        // 获取要素表头信息
                        String headerName = oFieldDefn.GetNameRef();
                        int type = oFieldDefn.GetFieldType();

                        jArray.add(gdalType2MySqlType(pinyinUtil.getPinYin4All(headerName), type));
                    }
                    return jArray;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            // 资源清理
            ds.delete();
            gdal.GDALDestroyDriverManager();
        }

        return null;
    }

    /**
     * 解析shp数据
     */
    public boolean decodeShapeFile(DBConnectInfo dbci, String path, long maxCommit, String tableName, String fileName, String fileDesc, int srid) {
        boolean isImported = false;
        //初始化数据处理工具类
        DataProcessSupport dataProcessSupport = new DataProcessSupport(dbci);
        gdal.AllRegister();
        // 为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
        // 为了使属性表字段支持中文，请添加下面这句
        gdal.SetConfigOption("SHAPE_ENCODING", "CP936");
        // 打开数据
        DataSource ds = null;
        PinyinUtil pinyinUtil = new PinyinUtil();
        try {
            ds = ogr.Open(path, 0);
            if (ds != null) {
                // 获取该数据源中的图层个数，一般shp数据图层只有一个，如果是mdb、dxf等图层就会有多个
                int iLayerCount = ds.GetLayerCount();
                if (iLayerCount > 0) {
                    // 获取第一个图层
                    Layer oLayer = ds.GetLayerByIndex(0);
                    if (oLayer == null) {
                        System.out.println("获取第0个图层失败！\n");
                    }

                    // 对图层进行初始化，如果对图层进行了过滤操作，执行这句后，之前的过滤全部清空
                    oLayer.ResetReading();

                    // 获取图层中的属性表表头并输出
                    FeatureDefn oDefn = oLayer.GetLayerDefn();
                    int iFieldCount = oDefn.GetFieldCount();

                    // 通过json对象封装所有要素信息,遍历要素提取数据
                    JSONArray jArray = new JSONArray();
                    JSONObject jObject;
                    Feature oFeature;
                    List fieldNames = new ArrayList<String>();

                    // 输出图层中的要素个数
                    // 下面开始遍历图层中的要素
                    long counter = 1;
                    while ((oFeature = oLayer.GetNextFeature()) != null) {
                        logger.info("正在处理第" + counter + "个要素");
                        jObject = new JSONObject();

                        // 获取要素中的属性表内容
                        for (int iField = 0; iField < iFieldCount; iField++) {
                            FieldDefn oFieldDefn = oDefn.GetFieldDefn(iField);
                            // 获取要素表头信息
                            String headerName = oFieldDefn.GetNameRef();
                            // 字段转换
                            headerName = pinyinUtil.getPinYin4All(headerName);
                            Object headerValue;
                            int type = oFieldDefn.GetFieldType();

                            switch (type) {
                                case ogr.OFTString:
                                    headerValue = oFeature.GetFieldAsString(iField);
                                    break;
                                case ogr.OFTReal:
                                    headerValue = oFeature.GetFieldAsDouble(iField);
                                    break;
                                case ogr.OFTInteger:
                                    headerValue = oFeature.GetFieldAsInteger(iField);
                                    break;
                                default:
                                    headerValue = oFeature.GetFieldAsString(iField);
                                    break;
                            }

                            jObject.put(headerName, headerValue);
                            fieldNames.add(headerName);
                        }

                        // 获取要素中的几何体
                        Geometry oGeometry = oFeature.GetGeometryRef();
                        //判断要素是否为单个几何图形，若不是则遍历几何图形（图斑），并拓扑分析
                        /*if(!oGeometry.IsSimple()){
                            int gCount = oGeometry.GetGeometryCount();
                            for (int i = 0; i < gCount; i++) {
                                Geometry geom =  oGeometry.GetGeometryRef(i);
                            }
                        }*/

                        //判断几何体是否包含高程信息
                        int is3d = oGeometry.Is3D();
                        //is3d=2包含高程信息，is3d=0不包含高程信息
                        if (is3d > 0) {
                            //要素包含高程信息，因mysql空间数据引擎限制，需要去掉该高程信息
                            oGeometry.FlattenTo2D();
                        }
                        String wkt = oGeometry.ExportToWkt();
                        jObject.put("GEOM", wkt);

                        //获取面对象质心坐标
                        if (!fieldNames.contains("X")) {
                            jObject.put("X", oGeometry.Centroid().GetX());
                        }
                        if (!fieldNames.contains("Y")) {
                            jObject.put("Y", oGeometry.Centroid().GetY());
                        }
                        if (!fieldNames.contains("AREA")) {
                            //几何要素为多边形时才计算面积
                            if (oGeometry.GetGeometryName().toLowerCase().contains("polygon")) {
                                double area = oGeometry.GetArea();
                                jObject.put("AREA", String.format("%.12f", area * 10000.0));//单位：平方千米,保留6位小数
                            } else {
                                jObject.put("AREA", 0);
                            }
                        }

                        //转码为json
                        if (!fieldNames.contains("FUID")) {
                            //默认增加随时间变化的唯一fid字符串
                            //jObject.put("FUID", UUID.randomUUID() + Long.toString(new Date().getTime()));
                            jObject.put("FUID", UUID.randomUUID().toString().replaceAll("-", ""));
                        }
                        jArray.add(jObject);
                        fieldNames.clear();

                        if (counter % maxCommit == 0) {
                            //中间批处理入库操作
                            isImported = dataProcessSupport.recordFileData(tableName, fileDesc, jArray, counter - maxCommit, srid);
                            jArray.clear();
                            if (!isImported) {
                                logger.info(fileName + "--入库失败");
                                return isImported;
                            }
                        }
                        counter++;
                    }

                    //执行剩余数据入库
                    isImported = dataProcessSupport.recordFileData(tableName, fileDesc, jArray, counter - (counter % maxCommit), srid);
                    jArray.clear();
                    if (!isImported) {
                        logger.info(fileName + "--入库失败");
                        return isImported;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("shp入库异常，" + e.getMessage());
        } finally {
            // 资源清理
            ds.delete();
            gdal.GDALDestroyDriverManager();
            if (isImported) {
                logger.info(fileName + "--入库成功");
                //文件处理状态更新
                if (dataProcessSupport.updateFileState(fileName)) {
                    logger.info(fileName + "--入库状态更新完成");
                } else {
                    logger.info(fileName + "--入库状态更新失败");
                }
            } else {
                logger.info(fileName + "--入库失败");
            }
            return isImported;
        }
    }

    /**
     * 卫星足迹圆落区分析（行政省）
     * @param satePosList 卫星星历数组
     * @param regionList 行政省界数组
     * @return
     */
    public JSONObject spatialAnalysis4Sate(JSONArray satePosList, JSONArray regionList) {
        //gdal组件注册
        gdal.AllRegister();
        // 为了支持中文路径，请添加下面这句代码
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
        // 为了使属性表字段支持中文，请添加下面这句
        gdal.SetConfigOption("SHAPE_ENCODING", "CP936");

        JSONObject jsonObject4Predict = new JSONObject();
        //存放空间分析结果
        JSONArray jsonArray4Regions = new JSONArray();
        try {
            if (satePosList != null && satePosList.size() > 0 && regionList != null && regionList.size() > 0) {
                int regionSize = regionList.size();
                int satPosSize = satePosList.size();
                //遍历省并与所有卫星足迹圆进行空间关联分析
                for (int i = 0; i < regionSize; i++) {
                    //存放单个省过境卫星
                    JSONArray passedSates4Province = new JSONArray();

                    JSONObject region = regionList.getJSONObject(i);
                    String regionName = region.getString("name");
                    double rx = region.getDouble("x");
                    double ry = region.getDouble("y");
                    String regionWkt = region.getString("geom");
                    Geometry regionGeom = ogr.CreateGeometryFromWkt(regionWkt);
                    for (int j = 0; j < satPosSize; j++) {
                        JSONObject sate = satePosList.getJSONObject(j);
                        //卫星ID
                        String sateId = sate.getString("sateId");
                        //卫星名称
                        String sateName = sate.getString("sateName");
                        String rangeCircleWkt = sate.getString("rangeCircleWkt");
                        Geometry sateGeom = ogr.CreateGeometryFromWkt(rangeCircleWkt);
                        if (sateGeom.Intersect(regionGeom)) {
                            JSONObject passedSate4Region = new JSONObject();
                            passedSate4Region.put("sid", sateId);
                            passedSate4Region.put("sn", sateName);
                            passedSates4Province.add(passedSate4Region);
                        }
                    }
                    //省
                    JSONObject passedRegion = new JSONObject();
                    passedRegion.put("rn", regionName);
                    passedRegion.put("rx", rx);
                    passedRegion.put("ry", ry);
                    passedRegion.put("rs", passedSates4Province);
                    if (passedSates4Province.size() > 0) {
                        jsonArray4Regions.add(passedRegion);
                    }
                    logger.info(regionName + " 分析完成");
                }
                //组装分析结果
                jsonObject4Predict.put("regions", jsonArray4Regions);
            }
        } catch (Exception e) {
            logger.error(e.getMessage() + " 卫星过省分析异常");
        } finally {
            gdal.GDALDestroyDriverManager();
        }

        return jsonObject4Predict;
    }
}
