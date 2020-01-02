package com.acs.util.db;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "Table", description = "数据表模型")
public class Table {
	@ApiModelProperty(value = "PG表所属Schema名")
	private String schemaname;
	@ApiModelProperty(value = "表名")
	private String tablename;
	@ApiModelProperty(value = "数据库表名")
	private String dbtablename;
	@ApiModelProperty(value = "Java类型")
	private String pkJavaType;

	public String getSchemaname() {
		return schemaname;
	}

	public void setSchemaname(String schemaname) {
		this.schemaname = schemaname;
	}

	public String getPkJavaType() {
		return pkJavaType;
	}

	public void setPkJavaType(String pkJavaType) {
		this.pkJavaType = pkJavaType;
	}

	public String getObjectPkJavaType(){ 
		if(this.getPkJavaType()==null){
			return "";
		}
		if(this.getPkJavaType() .equalsIgnoreCase("int")){
			return "Integer";
		}else if(this.getPkJavaType().equalsIgnoreCase("long")){
			return "Long";
		}
		return this.getPkJavaType();
	}
	public String getDbtablename() {
		return dbtablename;
	}

	public void setDbtablename(String dbtablename) {
		this.dbtablename = dbtablename;
	}

	private List<Column> columns=new ArrayList<Column>();
	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public List<Column> getColumns() {
		return columns;
	}


	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public String getFormatTablename(String name) {
		try {
			// 按照规定所有的tablename 均已t_开始，转换后所有的表名称全部去掉t_并且转换成小写
			return name.replaceFirst("t_", "").toLowerCase();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return name;
	}
	
	public String getUpperCaseName(){
		return tablename.substring(0,1).toUpperCase()+tablename.substring(1);
	}
	public String getLowerCaseName(){
		return tablename.substring(0,1).toLowerCase()+tablename.substring(1);
	}
	
	public String getPackagingName(){
		if("int".equals(pkJavaType))
			return Integer.class.getSimpleName();
		else if ("Long".equals(pkJavaType))
			return Long.class.getSimpleName();
		
		else if ("double".equals(pkJavaType))
			return Double.class.getSimpleName();
		
		else if ("short".equals(pkJavaType))
			return Short.class.getSimpleName();
		
		else if ("float".equals(pkJavaType))
			return Float.class.getSimpleName();
		
		else if ("byte".equals(pkJavaType))
			return Byte.class.getSimpleName();
		
		else if ("boolean".equals(pkJavaType))
			return Boolean.class.getSimpleName();
		else if ("string".equals(pkJavaType))
			return String.class.getSimpleName();
		else
			return "";
	}
}
