package com.satellite.entitis;

import com.alibaba.fastjson.JSONArray;
import com.github.amsacode.predict4java.SatPos;

import java.util.List;

public class SatPosEx {
    /*
     * 卫星ID*/
    private String sateId;
    public String getSateId() {
        return sateId;
    }

    public void setSateId(String sateId) {
        this.sateId = sateId;
    }

    /**
     * 卫星名称
     */
    private String sateName;

    public String getSateName() {
        return sateName;
    }

    public void setSateName(String sateName) {
        this.sateName = sateName;
    }

    /**
     * 卫星位置信息
     */
    private SatPos satPos = null;

    public SatPos getSatPos() {
        return satPos;
    }

    public void setSatPos(SatPos satPos) {
        this.satPos = satPos;
    }

    /**
     * 卫星当前时间信息覆盖省
     */
    private List<String> passRegions;

    public List<String> getPassRegions() {
        return passRegions;
    }

    public void setPassRegions(List<String> passRegions) {
        this.passRegions = passRegions;
    }

    /**
     * 范围圆 WKT 格式
     */
    private String rangeCircleWkt;

    public String getRangeCircleWkt() {
        return rangeCircleWkt;
    }

    public void setRangeCircleWkt(String rangeCircleWkt) {
        this.rangeCircleWkt = rangeCircleWkt;
    }

    /**
     * 足迹圆
     */
    private JSONArray rangeCircle;
    public JSONArray getRangeCircle() {
        return rangeCircle;
    }

    public void setRangeCircle(JSONArray rangeCircle) {
        this.rangeCircle = rangeCircle;
    }
}
