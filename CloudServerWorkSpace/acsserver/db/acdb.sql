/*
 Navicat Premium Data Transfer

 Source Server         : HwyMySQL5
 Source Server Type    : MySQL
 Source Server Version : 50722
 Source Host           : localhost:3306
 Source Schema         : acdb

 Target Server Type    : MySQL
 Target Server Version : 50722
 File Encoding         : 65001

 Date: 13/07/2020 08:51:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for apigroups
-- ----------------------------
DROP TABLE IF EXISTS `apigroups`;
CREATE TABLE `apigroups`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `groupname` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '接口分组标签名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `groupname_UNIQUE`(`groupname`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '接口分组标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chinaprovincecgcs
-- ----------------------------
DROP TABLE IF EXISTS `chinaprovincecgcs`;
CREATE TABLE `chinaprovincecgcs`  (
  `ENTIID` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'ENTIID',
  `CLASID` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'CLASID',
  `ID` double NULL DEFAULT NULL COMMENT 'ID',
  `GB` double NULL DEFAULT NULL COMMENT 'GB',
  `PROVINCE` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'PROVINCE',
  `AREA` double NULL DEFAULT NULL COMMENT 'AREA',
  `X` double NULL DEFAULT NULL COMMENT '中心经度(°)',
  `Y` double NULL DEFAULT NULL COMMENT '中心纬度(°)',
  `FUID` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '要素唯一标识',
  `GEOM` geometry NOT NULL COMMENT '点/线/面WKT几何对象',
  PRIMARY KEY (`FUID`) USING BTREE,
  UNIQUE INDEX `FUID_UNIQUE`(`FUID`) USING BTREE,
  INDEX `DATAINDEX`(`FUID`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'CHINA_Province_CGCS2000' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cloudcatalog
-- ----------------------------
DROP TABLE IF EXISTS `cloudcatalog`;
CREATE TABLE `cloudcatalog`  (
  `catalog_id` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'ID',
  `node_id` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '节点ID',
  `node_pid` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '父节点ID',
  `nodename` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '节点名称',
  `node_level` int(11) NULL DEFAULT NULL COMMENT '节点级别',
  PRIMARY KEY (`catalog_id`) USING BTREE,
  UNIQUE INDEX `catalog_id_UNIQUE`(`catalog_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '服务目录树' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cloudservice
-- ----------------------------
DROP TABLE IF EXISTS `cloudservice`;
CREATE TABLE `cloudservice`  (
  `services_id` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'ID',
  `catalog_id` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务父节点ID',
  `svrname` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务名称',
  `svrlayername` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务图层名',
  `svrtype` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务类型',
  `svrurl` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '服务地址',
  `svrsrid` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '坐标系ID',
  `cx` double NULL DEFAULT NULL COMMENT '地图服务范围中心经度',
  `cy` double NULL DEFAULT NULL COMMENT '地图服务范围中心纬度',
  `west` double NULL DEFAULT NULL COMMENT '地图服务范围最小经度',
  `south` double NULL DEFAULT NULL COMMENT '地图服务范围最小纬度',
  `east` double NULL DEFAULT NULL COMMENT '地图服务范围最大经度',
  `north` double NULL DEFAULT NULL COMMENT '地图服务范围最大纬度',
  `svrDate` datetime(0) NULL DEFAULT NULL COMMENT '服务注册时间',
  `svrprovider` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务注册者',
  `svrproviderid` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务注册者ID',
  `svrdescription` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '服务描述',
  PRIMARY KEY (`services_id`) USING BTREE,
  UNIQUE INDEX `services_id_UNIQUE`(`services_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '数据服务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dbconfigs
-- ----------------------------
DROP TABLE IF EXISTS `dbconfigs`;
CREATE TABLE `dbconfigs`  (
  `databases_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '表ID（主键）',
  `dbname` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据库名',
  `dbtype` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据库类型',
  `dbaddress` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据库地址',
  `dbuser` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据库用户',
  `dbpwd` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户登录密码',
  `dbport` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '数据库端口',
  `dbdesc` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `hasredis` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否存在Redis配置',
  `redisaddress` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Redis地址',
  `redisport` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Redis端口',
  `redispwd` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Redis密码',
  PRIMARY KEY (`databases_id`) USING BTREE,
  UNIQUE INDEX `dbdesc_UNIQUE`(`dbdesc`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '数据库连接信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dictionarys
-- ----------------------------
DROP TABLE IF EXISTS `dictionarys`;
CREATE TABLE `dictionarys`  (
  `dictname` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '字典项名称',
  `dictvalue` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '字典项值',
  `dicttype` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '字典项类型',
  `dictdescription` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '字典项描述',
  PRIMARY KEY (`dictvalue`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for projects
-- ----------------------------
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `projectname` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `projectconfig` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tles
-- ----------------------------
DROP TABLE IF EXISTS `tles`;
CREATE TABLE `tles`  (
  `tle_id` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `tle_name` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '卫星名称',
  `tle_frow` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '第一行根数',
  `tle_srow` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '第二行根数',
  `tle_desc` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`tle_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '卫星两行根数表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tlesgroup
-- ----------------------------
DROP TABLE IF EXISTS `tlesgroup`;
CREATE TABLE `tlesgroup`  (
  `group_id` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '分组ID',
  `groupname` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '分组名称',
  `groupdate` datetime(0) NULL DEFAULT NULL COMMENT '分组创建日期',
  `groupuser` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '分组用户',
  `groupuserid` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '分组用户ID',
  `groupdesc` mediumtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '分组描述',
  PRIMARY KEY (`group_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '卫星分组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tlesgroupref
-- ----------------------------
DROP TABLE IF EXISTS `tlesgroupref`;
CREATE TABLE `tlesgroupref`  (
  `group_id` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '分组ID',
  `tle_id` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '两行根数ID'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '分组与TLE关联表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
