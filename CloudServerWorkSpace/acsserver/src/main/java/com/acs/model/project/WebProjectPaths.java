/**
 *
 */
package com.acs.model.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Web工程目录路径类
 *
 * @author LBM
 * @time 2018年2月6日
 * @project acs
 * @type WebProjectPaths
 * @desc 【这里描述类型功能】
 */
@ApiModel(value = "WebProjectPaths", description = "WebProjectPaths实体模型")
public class WebProjectPaths {
    @ApiModelProperty(value = "Web工程根名称")
    private String wProjectPath;

    @ApiModelProperty(value = "app目录")
    private String appPath;

    @ApiModelProperty(value = "controller目录")
    private String wControllerPath;

    @ApiModelProperty(value = "model目录")
    private String modelPath;

    @ApiModelProperty(value = "store目录")
    private String storePath;

    @ApiModelProperty(value = "view目录")
    private String viewPath;

    @ApiModelProperty(value = "resources目录")
    private String wResPath;

    @ApiModelProperty(value = "config目录")
    private String configPath;

    @ApiModelProperty(value = "libs目录")
    private String libsPath;

    @ApiModelProperty(value = "sass目录")
    private String sassPath;

    @ApiModelProperty(value = "utils目录")
    private String utilsPath;

    @ApiModelProperty(value = "webconfig文件路径")
    private String webConfigPath;

    @ApiModelProperty(value = "webmodule目录")
    private String webModulePath;

    /**
     * @return the wProjectPath
     */
    public String getwProjectPath() {
        return wProjectPath;
    }

    /**
     * @param wProjectPath the wProjectPath to set
     */
    public void setwProjectPath(String wProjectPath) {
        this.wProjectPath = wProjectPath;
    }

    /**
     * @return the appPath
     */
    public String getAppPath() {
        return appPath;
    }

    /**
     * @param appPath the appPath to set
     */
    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    /**
     * @return the wControllerPath
     */
    public String getwControllerPath() {
        return wControllerPath;
    }

    /**
     * @param wControllerPath the wControllerPath to set
     */
    public void setwControllerPath(String wControllerPath) {
        this.wControllerPath = wControllerPath;
    }

    /**
     * @return the modelPath
     */
    public String getModelPath() {
        return modelPath;
    }

    /**
     * @param modelPath the modelPath to set
     */
    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    /**
     * @return the storePath
     */
    public String getStorePath() {
        return storePath;
    }

    /**
     * @param storePath the storePath to set
     */
    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    /**
     * @return the viewPath
     */
    public String getViewPath() {
        return viewPath;
    }

    /**
     * @param viewPath the viewPath to set
     */
    public void setViewPath(String viewPath) {
        this.viewPath = viewPath;
    }

    /**
     * @return the wResPath
     */
    public String getwResPath() {
        return wResPath;
    }

    /**
     * @param wResPath the wResPath to set
     */
    public void setwResPath(String wResPath) {
        this.wResPath = wResPath;
    }

    /**
     * @return the configPath
     */
    public String getConfigPath() {
        return configPath;
    }

    /**
     * @param configPath the configPath to set
     */
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    /**
     * @return the sassPath
     */
    public String getSassPath() {
        return sassPath;
    }

    /**
     * @param sassPath the sassPath to set
     */
    public void setSassPath(String sassPath) {
        this.sassPath = sassPath;
    }

    /**
     * @return the libsPath
     */
    public String getLibsPath() {
        return libsPath;
    }

    /**
     * @param libsPath the libsPath to set
     */
    public void setLibsPath(String libsPath) {
        this.libsPath = libsPath;
    }

    /**
     * @return the utilsPath
     */
    public String getUtilsPath() {
        return utilsPath;
    }

    /**
     * @param utilsPath the utilsPath to set
     */
    public void setUtilsPath(String utilsPath) {
        this.utilsPath = utilsPath;
    }

    /**
     * @return the webConfigPath
     */
    public String getWebConfigPath() {
        return webConfigPath;
    }

    /**
     * @param webConfigPath the webConfigPath to set
     */
    public void setWebConfigPath(String webConfigPath) {
        this.webConfigPath = webConfigPath;
    }

    /**
     * @return the webModulePath
     */
    public String getWebModulePath() {
        return webModulePath;
    }

    /**
     * @param webModulePath the webModulePath to set
     */
    public void setWebModulePath(String webModulePath) {
        this.webModulePath = webModulePath;
    }
}
