package com.satellite.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.amsacode.predict4java.*;
import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;
import com.satellite.entitis.SatPosEx;

import java.util.Date;

/**
 * 卫星相关操作工具类,
 * 位置数据返回结果结构
 * 星下点是地球中心与卫星的连线在地球表面上的交点，用地理经、纬度表示。卫星正下方的地面点称为星下点。星下点的集合称为星下点轨迹。
 */
/*Azimuth:    261.64305846848504 deg.
Elevation:  -69.81020218211603 deg.
Latitude:   -5.17893259903369 deg.
Longitude:  217.8293117221259 deg.
Date:       Tue Jul 23 14:24:48 CST 2019
Range:        12786.876867652023 km.
Range rate:   -1.8241275272743966 m/S.
Phase:        3.5898699673458623 /(256)
Altitude:     770.0777490575856 km
Theta:        -1.8371156930907733 rad/sec
Eclipsed:     true
Eclipse depth:0.24982649717491212 radians*/
/*
*
方位:261.64305846848504度。
海拔:-69.81020218211603度。
纬度:-5.17893259903369度。
经度:217.8293117221259度。
日期:2019年7月23日星期二14:24:48 CST
范围:12786.876867652023公里。
射程:-1.8241275272743966米/秒。
阶段:3.5898699673458623 / (256)
高度:770.0777490575856公里
θ:-1.8371156930907733 rad /秒
是否椭圆: 是
椭圆深度:0.24982649717491212弧度*/
public class SatelliteUtil {
    public JSONArray calcSatePos(JSONArray tles, Date date, double latitude,
                                 double longitude,
                                 double heightAMSL) {
        if (tles != null && tles.size() > 0) {
            int size = tles.size();
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < size; i++) {
                JSONObject tlesJSONObject = tles.getJSONObject(i);
                String sateId = tlesJSONObject.getString("tid");
                String sateName = tlesJSONObject.getString("text");
                String tlefr = tlesJSONObject.getString("tfr");
                String tlesr = tlesJSONObject.getString("tsr");
                String[] tleParam = {
                        sateName, tlefr, tlesr

                        /*"ZHUHAI-1 01 (CAS 4A)",
                        "1 43439U 18040A   19213.89923785  .00000267  00000-0  16152-4 0  9997",
                        "2 43439  97.3655 290.6238 0012558 102.9042 346.4727 15.18444978 70202"*/
                };
                //实例化TLE
                TLE tle = new TLE(tleParam);
                //构建卫星
                Satellite satellite = SatelliteFactory.createSatellite(tle);
                // 如果不清楚基站位置，全设置为0也可
                GroundStationPosition groundStationPosition = new GroundStationPosition(latitude, longitude, heightAMSL);
                SatPos satPos;
                if (date == null) {
                    // 若无传入时间则直接计算当前时刻卫星位置
                    satPos = satellite.getPosition(groundStationPosition, new Date());
                } else {
                    // 根据传入时间预测卫星位置
                    satPos = satellite.getPosition(groundStationPosition, date);
                }
                //方案二：原算法(经纬度需要传弧度)
                //JSONArray cpots = calculateRangeCirclePoints(satPos, 2.0);
                //弧度转经度并矫正到-180 -90 180 90区间内
                double lon = satPos.getLongitude() * rad2deg;
                double lat = satPos.getLatitude() * rad2deg;
                double azi = satPos.getAzimuth() * rad2deg;
                if (lon > 180.0) {
                    lon = lon - 360;
                }

                if (lat > 90.0) {
                    lat = lat - 180;
                }

                satPos.setLongitude(lon);
                satPos.setLatitude(lat);
                satPos.setAzimuth(azi);
                System.out.println(sateName + ": lon=" + lon + " lat=" + lat + " range= " + satPos.getRange() + " raduis= " + satPos.getRangeCircleRadiusKm() + " radius1=" + getRangeCircleRadiusKm(satPos) + " azi= " + satPos.getAzimuth());

                //方案一：自定义算法（目前看来扫描区域会比较准确）（经纬度为正常的度数）
                JSONArray cpots = calcRangeCircle(satPos, 2.0);

                StringBuffer sb = new StringBuffer();
                if (cpots != null && cpots.size() > 0) {
                    double rangelong;
                    double rangelat;
                    //格式化为wkt
                    sb.append("POLYGON ((");
                    for (int j = 0; j < cpots.size(); j++) {
                        JSONObject pos = cpots.getJSONObject(j);
                        rangelong = pos.getDoubleValue("lon");
                        rangelat = pos.getDoubleValue("lat");
                        String lonlat = rangelong + " " + rangelat + ",";
                        sb.append(lonlat);
                    }
                    //追加多边形闭合点
                    JSONObject closePos = cpots.getJSONObject(0);
                    rangelong = closePos.getDoubleValue("lon");
                    rangelat = closePos.getDoubleValue("lat");
                    String clonlat = rangelong + " " + rangelat;
                    sb.append(clonlat);
                    sb.append("))");
                }

                SatPosEx satPosEx = new SatPosEx();
                //卫星ID
                satPosEx.setSateId(sateId);
                //卫星名称
                satPosEx.setSateName(sateName);
                //卫星位置参数
                satPosEx.setSatPos(satPos);
                //卫星足迹圆WKT
                satPosEx.setRangeCircleWkt(sb.toString());
                //卫星足迹圆点集
                satPosEx.setRangeCircle(cpots);
                jsonArray.add(satPosEx);
            }
            return jsonArray;
        }

        return null;
    }

    /**
     * WGS84，根据定位，距离，方位角计算点
     *
     * @param satPos
     * @param incrementDegrees
     * @return
     */
    private JSONArray calcRangeCircle(SatPos satPos, double incrementDegrees) {
        double radiusKm = satPos.getRangeCircleRadiusKm();
        double latitude = satPos.getLatitude();
        double longitude = satPos.getLongitude();
        //Kew
        Coordinate lat = Coordinate.fromDegrees(latitude);
        Coordinate lng = Coordinate.fromDegrees(longitude);
        Point kew = Point.at(lat, lng);
        JSONArray rcArray = new JSONArray();
        for (int azi = 0; azi < 360; azi += incrementDegrees) {
            Point footprintPoint = EarthCalc.pointAt(kew, azi, radiusKm * 1000);
            JSONObject fpp = new JSONObject();
            fpp.put("lon", footprintPoint.longitude);
            fpp.put("lat", footprintPoint.latitude);
            rcArray.add(fpp);
        }

        return rcArray;
    }

    //原始方法
    private static final String NL = "\n";
    private static final String DEG_CR = " deg.\n";
    private static final double rad2deg = 180 / Math.PI;

    /* WGS 84 Earth radius km */
    private static final double EARTH_RADIUS_KM = 6.378137E3;
    private static final double R0 = 6378.16;

    public static double getRangeCircleRadiusKm(SatPos pos) {
        return 0.5 * (12756.33 * Math.acos(EARTH_RADIUS_KM
                / (EARTH_RADIUS_KM + pos.getAltitude())));
    }

    /**
     * 卫星足迹圆推荐算法
     * Calculates the footprint range circle using the given increment.
     *
     * @param pos
     * @return a list of {@link Position}
     */
    private static JSONArray calculateRangeCirclePoints(SatPos pos,
                                                        double incrementDegrees) {
        final double radiusKm = getRangeCircleRadiusKm(pos);

        final double latitude = pos.getLatitude();
        final double longitude = pos.getLongitude();
        final double beta = radiusKm / R0;
        JSONArray result = new JSONArray();
        for (int azi = 0; azi < 360; azi += incrementDegrees) {
            final double azimuth = (azi / 360.0) * 2.0 * Math.PI;
            double rangelat = Math.asin(Math.sin(latitude) * Math.cos(beta)
                    + Math.cos(azimuth) * Math.sin(beta) * Math.cos(latitude));
            final double num = Math.cos(beta)
                    - (Math.sin(latitude) * Math.sin(rangelat));
            final double den = Math.cos(latitude) * Math.cos(rangelat);
            double rangelong;

            if (azi == 0 && (beta > ((Math.PI / 2.0) - latitude))) {
                rangelong = longitude + Math.PI;
            } else if (azi == 180 && (beta > ((Math.PI / 2.0) - latitude))) {
                rangelong = longitude + Math.PI;
            } else if (Math.abs(num / den) > 1.0) {
                rangelong = longitude;
            } else {
                if ((180 - azi) >= 0) {
                    rangelong = longitude - Math.acos(num / den);
                } else {
                    rangelong = longitude + Math.acos(num / den);
                }
            }

            while (rangelong < 0.0) {
                rangelong += Math.PI * 2.0;
            }

            while (rangelong > Math.PI * 2.0) {
                rangelong -= Math.PI * 2.0;
            }

            rangelat = (rangelat / (2.0 * Math.PI)) * 360.0;
            rangelong = (rangelong / (2.0 * Math.PI)) * 360.0;

            //将经纬度范围限定为（-180，-90）-（180，90）之间
            /*if (rangelong < 180.0) {
                rangelong = -rangelong;
            } else if (rangelong > 180.0) {
                rangelong = 360.0 - rangelong;
            }

            if (rangelat < 90.0) {
                rangelat = -rangelat;
            } else if (rangelat > 90.0) {
                rangelat = 180.0 - rangelat;
            }*/
            if (rangelong > 180.0) {
                rangelong = rangelong - 360;
            }

            if (rangelat > 90.0) {
                rangelat = rangelat - 180;
            }

            result.add(new Position(rangelat, rangelong));
        }
        return result;
    }
}



