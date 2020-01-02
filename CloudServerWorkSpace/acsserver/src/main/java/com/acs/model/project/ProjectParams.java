/**
 * 
 */
package com.acs.model.project;

import com.acs.model.database.DBConnectInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author LBM
 * @time 2018年1月19日
 * @project acs
 * @type ProjectParams
 * @desc 【这里描述类型功能】
 */
@ApiModel(value = "ProjectParams", description = "ProjectParams实体模型")
public class ProjectParams {
	@ApiModelProperty(value = "工程工作空间目录")
	private String rootDir;
	@ApiModelProperty(value = "工程根目录")
	private String projectPath;
	@ApiModelProperty(value = "源代码路径")
	private String javaPath;
	@ApiModelProperty(value = "顶级包路径")
	private String topPackPath;
	@ApiModelProperty(value = "实体模型路径")
	private String entityPath;
	@ApiModelProperty(value = "Dao接口文件路径")
	private String daoPath;
	@ApiModelProperty(value = "Dao接口实现文件路径")
	private String daoImplPath;
	@ApiModelProperty(value = "Service接口文件路径")
	private String servPath;
	@ApiModelProperty(value = "Service接口实现文件路径")
	private String servImplPath;
	@ApiModelProperty(value = "Controller文件路径")
	private String controllerPath;
	@ApiModelProperty(value = "Mapper文件路径")
	private String mapperPath;
	@ApiModelProperty(value = "Java实体合并打包为JS对象文件路径")
	private String jsDirPath;
	@ApiModelProperty(value = "环境监听器文件路径")
	private String listenerPath;
	@ApiModelProperty(value = "拦截器器文件路径")
	private String interceptorPath;
	@ApiModelProperty(value = "Utils工具集文件路径")
	private String utilsPath;
	@ApiModelProperty(value = "Swagger配置类文件路径")
	private String swaggerConfigPath;
	@ApiModelProperty(value = "工程资源路径")
	private String resPath;
	@ApiModelProperty(value = "WEB-INF路径")
	private String webinfPath;
	@ApiModelProperty(value = "Swagger站点文件路径")
	private String swaggerPath;
	@ApiModelProperty(value = "日志路径")
	private String logPath;
	@ApiModelProperty(value = "日志名称")
	private String logName;
	@ApiModelProperty(value = "数据库配置信息")
	private DBConnectInfo dbci;
	@ApiModelProperty(value = "工程描述")
	private String projectDesc;
	@ApiModelProperty(value = "Server工程配置信息")
	private ProjectConfigInfo pci;
	@ApiModelProperty(value = "Web工程配置信息")
	private WebProjectConfigInfo wpci;
	@ApiModelProperty(value = "Web工程目录路径信息")
	private WebProjectPaths wpp;
	@ApiModelProperty(value = "查询是否分页")
	private boolean pagging;
	@ApiModelProperty(value = "是否生成JS模块")
	private boolean moduling;
	@ApiModelProperty(value = "是否启用web表格记录管理功能")
	private boolean managing;

	// 版本管理，后续完善
	/**
	 * 获取工程工作空间目录
	 */
	public String getRootDir() {
		return rootDir;
	}

	/**
	 * 设置工程工作空间目录
	 */
	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}

	/**
	 * 获取工程根目录
	 */
	public String getProjectPath() {
		return projectPath;
	}

	/**
	 * 设置工程根目录
	 */
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	/**
	 * 获取源代码路径
	 */
	public String getJavaPath() {
		return javaPath;
	}

	/**
	 * 设置源代码路径
	 */
	public void setJavaPath(String javaPath) {
		this.javaPath = javaPath;
	}

	/**
	 * 获取顶级包路径
	 */
	public String getTopPackPath() {
		return topPackPath;
	}

	/**
	 * 设置顶级包路径
	 */
	public void setTopPackPath(String topPackPath) {
		this.topPackPath = topPackPath;
	}

	/**
	 * 获取实体模型包路径
	 */
	public String getEntityPath() {
		return entityPath;
	}

	/**
	 * 设置实体模型包路径
	 */
	public void setEntityPath(String entityPath) {
		this.entityPath = entityPath;
	}

	/**
	 * 获取Dao接口类路径
	 */
	public String getDaoPath() {
		return daoPath;
	}

	/**
	 * 设置Dao接口类路径
	 */
	public void setDaoPath(String daoPath) {
		this.daoPath = daoPath;
	}

	/**
	 * 获取Controller控制器路径
	 */
	public String getControllerPath() {
		return controllerPath;
	}

	/**
	 * 设置Controller控制器路径
	 */
	public void setControllerPath(String controllerPath) {
		this.controllerPath = controllerPath;
	}

	/**
	 * 获取Mapper映射文件路径
	 */
	public String getMapperPath() {
		return mapperPath;
	}

	/**
	 * 设置Mapper映射文件路径
	 */
	public void setMapperPath(String mapperPath) {
		this.mapperPath = mapperPath;
	}

	/**
	 * 获取Java实体合并打包为JS对象文件路径
	 */
	public String getJsDirPath() {
		return jsDirPath;
	}

	/**
	 * 设置Java实体合并打包为JS对象文件路径
	 */
	public void setJsDirPath(String jsDirPath) {
		this.jsDirPath = jsDirPath;
	}

	/**
	 * 获取环境监听器文件路径
	 */
	public String getListenerPath() {
		return listenerPath;
	}

	/**
	 * 设置环境监听器文件路径
	 */
	public void setListenerPath(String listenerPath) {
		this.listenerPath = listenerPath;
	}

	/**
	 * 获取Swagger配置类文件路径
	 */
	public String getSwaggerConfigPath() {
		return swaggerConfigPath;
	}

	/**
	 * 设置Swagger配置类文件路径
	 */
	public void setSwaggerConfigPath(String swaggerConfigPath) {
		this.swaggerConfigPath = swaggerConfigPath;
	}

	/**
	 * 获取资源路径
	 */
	public String getResPath() {
		return resPath;
	}

	/**
	 * 设置资源路径
	 */
	public void setResPath(String resPath) {
		this.resPath = resPath;
	}

	/**
	 * 获取WEB-INF路径
	 */
	public String getWebinfPath() {
		return webinfPath;
	}

	/**
	 * 设置WEB-INF路径
	 */
	public void setWebinfPath(String webinfPath) {
		this.webinfPath = webinfPath;
	}

	/**
	 * 获取Swagger站点文件路径
	 */
	public String getSwaggerPath() {
		return swaggerPath;
	}

	/**
	 * 设置Swagger站点文件路径
	 */
	public void setSwaggerPath(String swaggerPath) {
		this.swaggerPath = swaggerPath;
	}

	/**
	 * 获取日志路径
	 */
	public String getLogPath() {
		return logPath;
	}

	/**
	 * 设置日志路径
	 */
	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

	/**
	 * 获取日志名称
	 */
	public String getLogName() {
		return logName;
	}

	/**
	 * 设置日志名称
	 */
	public void setLogName(String logName) {
		this.logName = logName;
	}

	/**
	 * 获取数据库配置信息
	 */
	public DBConnectInfo getDbci() {
		return dbci;
	}

	/**
	 * 设置设置数据库配置信息
	 */
	public void setDbci(DBConnectInfo dbci) {
		this.dbci = dbci;
	}

	/**
	 * @return the projectDesc
	 */
	public String getProjectDesc() {
		return projectDesc;
	}

	/**
	 * @param projectDesc
	 *            the projectDesc to set
	 */
	public void setProjectDesc(String projectDesc) {
		this.projectDesc = projectDesc;
	}

	/**
	 * @return the pci
	 */
	public ProjectConfigInfo getPci() {
		return pci;
	}

	/**
	 * @param pci
	 *            the pci to set
	 */
	public void setPci(ProjectConfigInfo pci) {
		this.pci = pci;
	}

	/**
	 * @return the servPath
	 */
	public String getServPath() {
		return servPath;
	}

	/**
	 * @param servPath
	 *            the servPath to set
	 */
	public void setServPath(String servPath) {
		this.servPath = servPath;
	}

	/**
	 * @return the daoImplPath
	 */
	public String getDaoImplPath() {
		return daoImplPath;
	}

	/**
	 * @param daoImplPath
	 *            the daoImplPath to set
	 */
	public void setDaoImplPath(String daoImplPath) {
		this.daoImplPath = daoImplPath;
	}

	/**
	 * @return the servImplPath
	 */
	public String getServImplPath() {
		return servImplPath;
	}

	/**
	 * @param servImplPath
	 *            the servImplPath to set
	 */
	public void setServImplPath(String servImplPath) {
		this.servImplPath = servImplPath;
	}

	/**
	 * @return the interceptorPath
	 */
	public String getInterceptorPath() {
		return interceptorPath;
	}

	/**
	 * @param interceptorPath
	 *            the interceptorPath to set
	 */
	public void setInterceptorPath(String interceptorPath) {
		this.interceptorPath = interceptorPath;
	}

	/**
	 * @return the pagging
	 */
	public boolean isPagging() {
		return pagging;
	}

	/**
	 * @param pagging
	 *            the pagging to set
	 */
	public void setPagging(boolean pagging) {
		this.pagging = pagging;
	}

	/**
	 * @return the moduling
	 */
	public boolean isModuling() {
		return moduling;
	}

	/**
	 * @param moduling
	 *            the moduling to set
	 */
	public void setModuling(boolean moduling) {
		this.moduling = moduling;
	}

	/**
	 * @return the wpci
	 */
	public WebProjectConfigInfo getWpci() {
		return wpci;
	}

	/**
	 * @param wpci
	 *            the wpci to set
	 */
	public void setWpci(WebProjectConfigInfo wpci) {
		this.wpci = wpci;
	}

	/**
	 * @return the wpp
	 */
	public WebProjectPaths getWpp() {
		return wpp;
	}

	/**
	 * @param wpp the wpp to set
	 */
	public void setWpp(WebProjectPaths wpp) {
		this.wpp = wpp;
	}

	public boolean isManaging() {
		return managing;
	}

	public void setManaging(boolean managing) {
		this.managing = managing;
	}

	public String getUtilsPath() {
		return utilsPath;
	}

	public void setUtilsPath(String utilsPath) {
		this.utilsPath = utilsPath;
	}
}
