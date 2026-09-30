-- CampusHub 微服务数据库初始化脚本
-- 数据库: campus_notification
-- 来源: campushub.sql
-- 自动拆分：保留原表结构、索引和种子数据

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `campus_notification`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE `campus_notification`;

-- ==================== campus_notification ====================
CREATE TABLE `campus_notification` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `type` VARCHAR(20) NOT NULL DEFAULT 'SYSTEM' COMMENT '类型:SYSTEM / MARKET / ACTIVITY / LOST_FOUND',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读:0 未读 / 1 已读(实体映射为 read)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(映射 createdAt)',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知表';

INSERT INTO `campus_notification` (`user_id`, `title`, `content`, `type`, `is_read`, `created_at`) VALUES
  (2, '报名成功:校园公益旧衣回收', '你已成功报名「深夜自习室打卡挑战」,请提前 30 分钟到场签到。', 'ACTIVITY', 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
  (9, '你发布的商品被收藏', '商品「戴森吹风机 HD08 国行」新增收藏,当前共 46 人收藏。', 'MARKET', 1, DATE_SUB(NOW(), INTERVAL 7 HOUR)),
  (16, 'CampusHub 服务升级公告', '平台将于本周六 02:00-04:00 进行例行维护,期间部分功能可能短暂不可用。', 'SYSTEM', 1, DATE_SUB(NOW(), INTERVAL 13 HOUR)),
  (23, '你发布的失物有新进展', '「拾到校园卡(工商银行卡联名)」的状态有更新,点击查看详情。', 'LOST_FOUND', 1, DATE_SUB(NOW(), INTERVAL 19 HOUR)),
  (30, '商品降价提醒', '你关注的「雷蛇黑寡妇蜘蛛 V3 竞技版」降价至 ¥729,比收藏时低 ¥100。', 'MARKET', 0, DATE_SUB(NOW(), INTERVAL 25 HOUR)),
  (37, '活动提醒:周末读书会:当我们谈论校园时', '「新生破冰定向越野」即将开始,记得提前到场。', 'ACTIVITY', 1, DATE_SUB(NOW(), INTERVAL 31 HOUR)),
  (5, '账号安全提醒', '检测到你的账号在新设备登录,如非本人操作请及时修改密码。', 'SYSTEM', 1, DATE_SUB(NOW(), INTERVAL 37 HOUR)),
  (12, '失物招领动态', '「黑色钱包(内含校园卡)遗失在图书馆三楼」有新评论,点击查看详情。', 'LOST_FOUND', 1, DATE_SUB(NOW(), INTERVAL 43 HOUR)),
  (19, '报名成功:新生破冰定向越野', '你已成功报名「校园公益旧衣回收」,请提前 30 分钟到场签到。', 'ACTIVITY', 0, DATE_SUB(NOW(), INTERVAL 49 HOUR)),
  (26, '你发布的商品被收藏', '商品「电煮锅 宿舍小功率」新增收藏,当前共 41 人收藏。', 'MARKET', 1, DATE_SUB(NOW(), INTERVAL 55 HOUR)),
  (33, 'CampusHub 服务升级公告', '平台将于本周六 02:00-04:00 进行例行维护,期间部分功能可能短暂不可用。', 'SYSTEM', 1, DATE_SUB(NOW(), INTERVAL 61 HOUR)),
  (40, '你发布的失物有新进展', '「拾到米色围巾一条」的状态有更新,点击查看详情。', 'LOST_FOUND', 1, DATE_SUB(NOW(), INTERVAL 67 HOUR)),
  (8, '商品降价提醒', '你关注的「宜家落地灯 暖光」降价至 ¥965,比收藏时低 ¥100。', 'MARKET', 0, DATE_SUB(NOW(), INTERVAL 73 HOUR)),
  (15, '活动提醒:AI 大模型入门分享会', '「校园歌手大赛海选」即将开始,记得提前到场。', 'ACTIVITY', 1, DATE_SUB(NOW(), INTERVAL 79 HOUR)),
  (22, '账号安全提醒', '检测到你的账号在新设备登录,如非本人操作请及时修改密码。', 'SYSTEM', 1, DATE_SUB(NOW(), INTERVAL 85 HOUR)),
  (29, '失物招领动态', '「黑色单反相机包(内含镜头)」有新评论,点击查看详情。', 'LOST_FOUND', 1, DATE_SUB(NOW(), INTERVAL 91 HOUR)),
  (36, '报名成功:AI 大模型入门分享会', '你已成功报名「汉服游园会」,请提前 30 分钟到场签到。', 'ACTIVITY', 0, DATE_SUB(NOW(), INTERVAL 97 HOUR)),
  (4, '你发布的商品被收藏', '商品「宜家落地灯 暖光」新增收藏,当前共 48 人收藏。', 'MARKET', 1, DATE_SUB(NOW(), INTERVAL 103 HOUR)),
  (11, 'CampusHub 服务升级公告', '平台将于本周六 02:00-04:00 进行例行维护,期间部分功能可能短暂不可用。', 'SYSTEM', 1, DATE_SUB(NOW(), INTERVAL 109 HOUR)),
  (18, '你发布的失物有新进展', '「白色蓝牙耳机(右耳)遗失在东操场」的状态有更新,点击查看详情。', 'LOST_FOUND', 1, DATE_SUB(NOW(), INTERVAL 115 HOUR)),
  (25, '商品降价提醒', '你关注的「行李箱 24 寸 万向轮」降价至 ¥1476,比收藏时低 ¥100。', 'MARKET', 0, DATE_SUB(NOW(), INTERVAL 121 HOUR)),
  (32, '活动提醒:英语角:Travel Around the World', '「编程马拉松 HackNight 36h」即将开始,记得提前到场。', 'ACTIVITY', 1, DATE_SUB(NOW(), INTERVAL 127 HOUR)),
  (39, '账号安全提醒', '检测到你的账号在新设备登录,如非本人操作请及时修改密码。', 'SYSTEM', 1, DATE_SUB(NOW(), INTERVAL 133 HOUR)),
  (7, '失物招领动态', '「黑色单反相机包(内含镜头)」有新评论,点击查看详情。', 'LOST_FOUND', 1, DATE_SUB(NOW(), INTERVAL 139 HOUR)),
  (14, '报名成功:英语角:Travel Around the World', '你已成功报名「深夜自习室打卡挑战」,请提前 30 分钟到场签到。', 'ACTIVITY', 0, DATE_SUB(NOW(), INTERVAL 145 HOUR)),
  (21, '你发布的商品被收藏', '商品「科颜氏高保湿面霜 125ml」新增收藏,当前共 37 人收藏。', 'MARKET', 1, DATE_SUB(NOW(), INTERVAL 151 HOUR)),
  (28, 'CampusHub 服务升级公告', '平台将于本周六 02:00-04:00 进行例行维护,期间部分功能可能短暂不可用。', 'SYSTEM', 1, DATE_SUB(NOW(), INTERVAL 157 HOUR)),
  (35, '你发布的失物有新进展', '「拾到课本《管理学原理》」的状态有更新,点击查看详情。', 'LOST_FOUND', 1, DATE_SUB(NOW(), INTERVAL 163 HOUR)),
  (3, '商品降价提醒', '你关注的「电煮锅 宿舍小功率」降价至 ¥960,比收藏时低 ¥100。', 'MARKET', 0, DATE_SUB(NOW(), INTERVAL 169 HOUR)),
  (10, '活动提醒:急救知识培训(CPR+AED)', '「职业生涯规划讲座:简历与面试」即将开始,记得提前到场。', 'ACTIVITY', 1, DATE_SUB(NOW(), INTERVAL 175 HOUR)),
  (17, '账号安全提醒', '检测到你的账号在新设备登录,如非本人操作请及时修改密码。', 'SYSTEM', 1, DATE_SUB(NOW(), INTERVAL 181 HOUR)),
  (24, '失物招领动态', '「银灰色拉杆箱遗落在校门口」有新评论,点击查看详情。', 'LOST_FOUND', 1, DATE_SUB(NOW(), INTERVAL 187 HOUR)),
  (31, '报名成功:AI 大模型入门分享会', '你已成功报名「考研经验分享会(上岸学长学姐团)」,请提前 30 分钟到场签到。', 'ACTIVITY', 0, DATE_SUB(NOW(), INTERVAL 193 HOUR)),
  (38, '你发布的商品被收藏', '商品「三脚架 相机手机通用」新增收藏,当前共 36 人收藏。', 'MARKET', 1, DATE_SUB(NOW(), INTERVAL 199 HOUR)),
  (6, 'CampusHub 服务升级公告', '平台将于本周六 02:00-04:00 进行例行维护,期间部分功能可能短暂不可用。', 'SYSTEM', 1, DATE_SUB(NOW(), INTERVAL 205 HOUR)),
  (13, '你发布的失物有新进展', '「拾到学生卡一张(姓名:陈屿)」的状态有更新,点击查看详情。', 'LOST_FOUND', 1, DATE_SUB(NOW(), INTERVAL 211 HOUR)),
  (20, '商品降价提醒', '你关注的「电吉他 Squier Affinity」降价至 ¥74,比收藏时低 ¥100。', 'MARKET', 0, DATE_SUB(NOW(), INTERVAL 217 HOUR)),
  (27, '活动提醒:手作工作坊:皮革卡包 DIY', '「英语角:Travel Around the World」即将开始,记得提前到场。', 'ACTIVITY', 1, DATE_SUB(NOW(), INTERVAL 223 HOUR)),
  (34, '账号安全提醒', '检测到你的账号在新设备登录,如非本人操作请及时修改密码。', 'SYSTEM', 1, DATE_SUB(NOW(), INTERVAL 229 HOUR)),
  (2, '失物招领动态', '「拾到校园卡(工商银行卡联名)」有新评论,点击查看详情。', 'LOST_FOUND', 1, DATE_SUB(NOW(), INTERVAL 235 HOUR));

SET FOREIGN_KEY_CHECKS = 1;
