/**
 *
 */
package com.acs.model.database;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author LBM
 * @time 2017年12月18日
 * @project： codecreator
 * @type： DBConnectInfo
 * @desc： 数据库连接信息类
 */
@ApiModel(value = "DBConnectInfo", description = "数据库连接信息")
public class DBConnectInfo {
    @ApiModelProperty(value = "数据库描述")
    private String dbDesc;

    @ApiModelProperty(value = "数据库类型")
    private String dbType;

    @ApiModelProperty(value = "数据库地址")
    private String dbAddress;

    @ApiModelProperty(value = "数据库端口")
    private String dbPort;

    @ApiModelProperty(value = "用户名")
    private String dbUser;

    @ApiModelProperty(value = "密码")
    private String dbPassword;

    @ApiModelProperty(value = "数据库名")
    private String dbName;

    @ApiModelProperty(value = "是否需要Redis")
    private String needRedis;

    @ApiModelProperty(value = "Redis地址")
    private String redisAddress;

    @ApiModelProperty(value = "Redis端口")
    private String redisPort;

    @ApiModelProperty(value = "Redis密码")
    private String redisPassword;

    public String getNeedRedis() {
        return needRedis;
    }

    public void setNeedRedis(String needRedis) {
        this.needRedis = needRedis;
    }

    public String getRedisAddress() {
        return redisAddress;
    }

    public void setRedisAddress(String redisAddress) {
        this.redisAddress = redisAddress;
    }

    public String getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(String redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public String getDbDesc() {
        return dbDesc;
    }

    public void setDbDesc(String dbDesc) {
        this.dbDesc = dbDesc;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDbAddress() {
        return dbAddress;
    }

    public void setDbAddress(String dbAddress) {
        this.dbAddress = dbAddress;
    }

    public String getDbPort() {
        return dbPort;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
