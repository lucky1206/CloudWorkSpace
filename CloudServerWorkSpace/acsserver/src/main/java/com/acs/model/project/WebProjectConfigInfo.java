/**
 * 
 */
package com.acs.model.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Web工程相关配置
 * 
 * @author LBM
 * @time 2018年2月5日
 * @project acs
 * @type WebProjectConfigInfo
 * @desc 【这里描述类型功能】
 */
@ApiModel(value = "WebProjectConfigInfo", description = "WebProjectConfigInfo实体模型")
public class WebProjectConfigInfo {
	@ApiModelProperty(value = "Web工程名称")
	private String webName;

	@ApiModelProperty(value = "Application类名称")
	private String applicationName = "Application";

	@ApiModelProperty(value = "模块名称")
	private String moduleName;

	@ApiModelProperty(value = "模块描述")
	private String moduleDesc;

	@ApiModelProperty(value = "Web工程全局控制文件名称")
	private String controllerName = "GlobalController";

	@ApiModelProperty(value = "Web工程配置文件名称")
	private String configName = "SystemConfig";

	/**
	 * @return the webName
	 */
	public String getWebName() {
		return webName;
	}

	/**
	 * @param webName
	 *            the webName to set
	 */
	public void setWebName(String webName) {
		this.webName = webName;
	}

	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}

	/**
	 * @param applicationName
	 *            the applicationName to set
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * @return the moduleName
	 */
	public String getModuleName() {
		return moduleName;
	}

	/**
	 * @param moduleName
	 *            the moduleName to set
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	/**
	 * @return the controllerName
	 */
	public String getControllerName() {
		return controllerName;
	}

	/**
	 * @param controllerName
	 *            the controllerName to set
	 */
	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}

	/**
	 * @return the configName
	 */
	public String getConfigName() {
		return configName;
	}

	/**
	 * @param configName
	 *            the configName to set
	 */
	public void setConfigName(String configName) {
		this.configName = configName;
	}

	/**
	 * @return the moduleDesc
	 */
	public String getModuleDesc() {
		return moduleDesc;
	}

	/**
	 * @param moduleDesc the moduleDesc to set
	 */
	public void setModuleDesc(String moduleDesc) {
		this.moduleDesc = moduleDesc;
	}
}
