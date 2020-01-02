/**
 * 
 */
package com.acs.model.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**工程配置类
 * @author    LBM
 * @time      2018年1月19日
 * @project   acs
 * @type      ProjectConfigInfo
 * @desc     【这里描述类型功能】
 */
@ApiModel(value = "ProjectConfigInfo", description = "ProjectConfigInfo实体模型")
public class ProjectConfigInfo{
	@ApiModelProperty(value = "工程名称")
	private String projectName;
	
	@ApiModelProperty(value = "工程描述")
	private String projectDesc;
	
	@ApiModelProperty(value = "顶级包名,如：com")
	private String fpName;
	
	@ApiModelProperty(value = "次级包名，如：项目名称")
	private String spName;
	
	@ApiModelProperty(value = "Service接口包名，如：service等")
	private String servName;
	
	@ApiModelProperty(value = "Service接口实现类包名")
	private String servNameImpl;
	
	@ApiModelProperty(value = "Java实体模型包名，如：entity、models等")
	private String epName;
	
	@ApiModelProperty(value = "Dao接口包名，如：idao、dao等")
	private String dpName;
	
	@ApiModelProperty(value = "Dao接口实现类包名")
	private String dpNameImpl;
	
	@ApiModelProperty(value = "Controller包名，如：controller")
	private String cpName;
	
	@ApiModelProperty(value = "Mapping映射文件目录名")
	private String mfdName;
	
	@ApiModelProperty(value = "拦截器文件目录名")
	private String interceptorName;
	
	@ApiModelProperty(value = "JS文件目录名，如：jsentity、jsmodel")
	private String jsfdName;
	
	@ApiModelProperty(value = "JS文件名，如：JsEntity、JsModel")
	private String jsfName;
	
	@ApiModelProperty(value = "Swagger对应的后台服务地址（IP+Port）")
	private String swaggerServer;

	/**
	 * @return the projectName
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * @param projectName the projectName to set
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * @return the fpName
	 */
	public String getFpName() {
		return fpName;
	}

	/**
	 * @param fpName the fpName to set
	 */
	public void setFpName(String fpName) {
		this.fpName = fpName;
	}

	/**
	 * @return the spName
	 */
	public String getSpName() {
		return spName;
	}

	/**
	 * @param spName the spName to set
	 */
	public void setSpName(String spName) {
		this.spName = spName;
	}

	/**
	 * @return the epName
	 */
	public String getEpName() {
		return epName;
	}

	/**
	 * @param epName the epName to set
	 */
	public void setEpName(String epName) {
		this.epName = epName;
	}

	/**
	 * @return the dpName
	 */
	public String getDpName() {
		return dpName;
	}

	/**
	 * @param dpName the dpName to set
	 */
	public void setDpName(String dpName) {
		this.dpName = dpName;
	}

	/**
	 * @return the cpName
	 */
	public String getCpName() {
		return cpName;
	}

	/**
	 * @param cpName the cpName to set
	 */
	public void setCpName(String cpName) {
		this.cpName = cpName;
	}

	/**
	 * @return the mfdName
	 */
	public String getMfdName() {
		return mfdName;
	}

	/**
	 * @param mfdName the mfdName to set
	 */
	public void setMfdName(String mfdName) {
		this.mfdName = mfdName;
	}

	/**
	 * @return the jsfdName
	 */
	public String getJsfdName() {
		return jsfdName;
	}

	/**
	 * @param jsfdName the jsfdName to set
	 */
	public void setJsfdName(String jsfdName) {
		this.jsfdName = jsfdName;
	}

	/**
	 * @return the jsfName
	 */
	public String getJsfName() {
		return jsfName;
	}

	/**
	 * @param jsfName the jsfName to set
	 */
	public void setJsfName(String jsfName) {
		this.jsfName = jsfName;
	}

	/**
	 * @return the projectDesc
	 */
	public String getProjectDesc() {
		return projectDesc;
	}

	/**
	 * @param projectDesc the projectDesc to set
	 */
	public void setProjectDesc(String projectDesc) {
		this.projectDesc = projectDesc;
	}

	/**
	 * @return the swaggerServer
	 */
	public String getSwaggerServer() {
		return swaggerServer;
	}

	/**
	 * @param swaggerServer the swaggerServer to set
	 */
	public void setSwaggerServer(String swaggerServer) {
		this.swaggerServer = swaggerServer;
	}

	/**
	 * @return the servName
	 */
	public String getServName() {
		return servName;
	}

	/**
	 * @param servName the servName to set
	 */
	public void setServName(String servName) {
		this.servName = servName;
	}

	/**
	 * @return the servNameImpl
	 */
	public String getServNameImpl() {
		return servNameImpl;
	}

	/**
	 * @param servNameImpl the servNameImpl to set
	 */
	public void setServNameImpl(String servNameImpl) {
		this.servNameImpl = servNameImpl;
	}

	/**
	 * @return the dpNameImpl
	 */
	public String getDpNameImpl() {
		return dpNameImpl;
	}

	/**
	 * @param dpNameImpl the dpNameImpl to set
	 */
	public void setDpNameImpl(String dpNameImpl) {
		this.dpNameImpl = dpNameImpl;
	}

	/**
	 * @return the interceptorName
	 */
	public String getInterceptorName() {
		return interceptorName;
	}

	/**
	 * @param interceptorName the interceptorName to set
	 */
	public void setInterceptorName(String interceptorName) {
		this.interceptorName = interceptorName;
	}
}
