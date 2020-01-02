package com.acs.model.svrreg;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "SvrRegInfoResponse", description = "SvrRegInfo接口响应类")
public class SvrInfo {
    @ApiModelProperty(value = "ID")
    private String serviceId;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @ApiModelProperty(value = "服务父节点ID")
    private String catalogId;

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    @ApiModelProperty(value = "服务名称")
    private String svrName;

    public String getSvrName() {
        return svrName;
    }

    public void setSvrName(String svrName) {
        this.svrName = svrName;
    }

    @ApiModelProperty(value = "服务图层名称")
    private String svrLayerName;

    public String getSvrLayerName() {
        return svrLayerName;
    }

    public void setSvrLayerName(String svrLayerName) {
        this.svrLayerName = svrLayerName;
    }

    @ApiModelProperty(value = "服务类型")
    private String svrType;

    public String getSvrType() {
        return svrType;
    }

    public void setSvrType(String svrType) {
        this.svrType = svrType;
    }

    @ApiModelProperty(value = "坐标系ID")
    private String svrSrid;

    public String getSvrSrid() {
        return svrSrid;
    }

    public void setSvrSrid(String svrSrid) {
        this.svrSrid = svrSrid;
    }

    @ApiModelProperty(value = "地图服务范围中心经度")
    private double cx = 0;
    @ApiModelProperty(value = "地图服务范围中心纬度")
    private double cy = 0;
    @ApiModelProperty(value = "地图服务范围最小经度")
    private double west = 0;
    @ApiModelProperty(value = "地图服务范围最小纬度")
    private double south = 0;
    @ApiModelProperty(value = "地图服务范围最大经度")
    private double east = 0;
    @ApiModelProperty(value = "地图服务范围最大纬度")
    private double north = 0;

    public double getCx() {
        return cx;
    }

    public void setCx(double cx) {
        this.cx = cx;
    }

    public double getCy() {
        return cy;
    }

    public void setCy(double cy) {
        this.cy = cy;
    }

    public double getWest() {
        return west;
    }

    public void setWest(double west) {
        this.west = west;
    }

    public double getSouth() {
        return south;
    }

    public void setSouth(double south) {
        this.south = south;
    }

    public double getEast() {
        return east;
    }

    public void setEast(double east) {
        this.east = east;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(double north) {
        this.north = north;
    }

    @ApiModelProperty(value = "服务地址")
    private String svrUrl;

    public String getSvrUrl() {
        return svrUrl;
    }

    public void setSvrUrl(String svrUrl) {
        this.svrUrl = svrUrl;
    }

    @ApiModelProperty(value = "服务注册时间")
    private String svrDate;

    public String getSvrDate() {
        return svrDate;
    }

    public void setSvrDate(String svrDate) {
        this.svrDate = svrDate;
    }

    @ApiModelProperty(value = "服务注册者")
    private String svrProvider;

    public String getSvrProvider() {
        return svrProvider;
    }

    public void setSvrProvider(String svrProvider) {
        this.svrProvider = svrProvider;
    }

    @ApiModelProperty(value = "服务注册者ID")
    private String svrProviderid;

    public String getSvrProviderid() {
        return svrProviderid;
    }

    public void setSvrProviderid(String svrProviderid) {
        this.svrProviderid = svrProviderid;
    }

    @ApiModelProperty(value = "服务描述")
    private String svrDescription;

    public String getSvrDescription() {
        return svrDescription;
    }

    public void setSvrDescription(String svrDescription) {
        this.svrDescription = svrDescription;
    }

}
