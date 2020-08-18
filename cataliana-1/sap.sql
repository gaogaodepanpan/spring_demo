/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 80016
Source Host           : localhost:3306
Source Database       : sap

Target Server Type    : MYSQL
Target Server Version : 80016
File Encoding         : 65001

Date: 2020-08-16 00:04:36
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for address
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `ad_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ad_em_id` int(11) NOT NULL,
  `ad_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ad_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of address
-- ----------------------------
INSERT INTO `address` VALUES ('1', '1', '河北');
INSERT INTO `address` VALUES ('2', '2', '西安');
INSERT INTO `address` VALUES ('3', '3', '北京');
INSERT INTO `address` VALUES ('4', '1', '湖南');

-- ----------------------------
-- Table structure for employee
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `em_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `em_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `age` int(11) NOT NULL,
  PRIMARY KEY (`em_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of employee
-- ----------------------------
INSERT INTO `employee` VALUES ('1', '李四', '59');
INSERT INTO `employee` VALUES ('2', '张三', '35');
INSERT INTO `employee` VALUES ('3', '王五', '18');
