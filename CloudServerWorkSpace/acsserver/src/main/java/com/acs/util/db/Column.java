package com.acs.util.db;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Column", description = "数据表字段模型")
public class Column {
	@ApiModelProperty(value = "字段Java名称")
	private String javaname;
	@ApiModelProperty(value = "字段大写名称")
	private String upname;
	@ApiModelProperty(value = "数据表字段名称")
	private String dbname;
	@ApiModelProperty(value = "字段Java类型")
	private String javaType;
	@ApiModelProperty(value = "数据表字段类型")
	private String dbType;
	@ApiModelProperty(value = "字段描述")
	private String desc;

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getJavaType() {
		return javaType;
	}

	public String getJavaname() {
		return javaname;
	}

	public void setJavaname(String javaname) {
		this.javaname = javaname;
	}

	/**
	 * @return the upname
	 */
	public String getUpname() {
		return upname;
	}

	/**
	 * @param upname
	 *            the upname to set
	 */
	public void setUpname(String upname) {
		this.upname = upname;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	private String pk = "0";

	public String getPk() {
		return pk;
	}

	public void setPk(String pk) {
		this.pk = pk;
	}

	public String getUpperCol(String col) {
		return col.substring(0, 1).toUpperCase() + col.substring(1);
	}

}
