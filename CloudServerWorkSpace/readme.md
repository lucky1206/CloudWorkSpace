##ACS(Auto Code System)
###代码自动生成系统

[在线演示系统](http://114.116.231.97:8080/acsweb/#projectmanager)

###系统简介

* 前端采用sencha extjs
* 后端采用Spring Mvc、Redis、Druid、Velocity模版引擎
* 支持MySQL、Oracle、PostgresSQL
* 支持数据库以及Spring工程配置管理
* 支持单表增删改查接口一键生成
* 支持单表自定义增删改查接口一键生成
* 支持SQL语句解译生成接口，包括单表或多表,较为完美解决多表关联查询无法自动生成接口的问题
* 生成的Java代码可同时适配SpringMVC和SpringBoot工程

###系统功能

1. 数据库配置： 数据库连接配置、Redis连接配置
2. 工程配置：Spring工程包名配置、Swagger地址配置
3. 加载数据库：表结构加载及字段合规性检查（必须包含主键）
4. 单表接口创建：数据库所有表增删改查接口一键生成
5. 业务接口创建：根据编写的SQL语句解译生成相应的接口
6. 自定义接口创建：单表增删改查接口定制化生成
7. 代码管理：前后端代码下载

###开发计划
1. 前端采用Vue重写，后端采用SpringBoot改造
2. Velocity模版引擎升级
3. 前端代码生成Vue module，现在生成extjs module
4. 集成SVN

