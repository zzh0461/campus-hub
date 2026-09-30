-- CampusHub 微服务数据库初始化脚本
-- 数据库: campus_content
-- 来源: campushub.sql
-- 自动拆分：保留原表结构、索引和种子数据

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `campus_content`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE `campus_content`;

-- ==================== campus_announcement ====================
CREATE TABLE `campus_announcement` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` VARCHAR(100) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `category` VARCHAR(20) NOT NULL COMMENT '分类:通知公告/教务信息/后勤服务/校园活动',
  `author` VARCHAR(50) NOT NULL COMMENT '发布单位',
  `published` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否发布:1 是 / 0 否',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量(映射 viewCount)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间(映射 createdAt)',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(映射 updatedAt)',
  PRIMARY KEY (`id`),
  KEY `idx_category_published` (`category`, `published`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园公告表';

INSERT INTO `campus_announcement` (`title`, `content`, `category`, `author`, `published`, `view_count`, `created_at`) VALUES
  ('关于校园网出口带宽升级的通知', '各位师生:\n\n为改善校园网络高峰期体验,信息化中心将于本周六 0:00-6:00 对校园网出口带宽进行升级,期间校园网可能出现短暂中断,请提前安排好学习与工作。\n\n升级完成后,校园网上下行带宽将在现有基础上提升 50%。\n\n信息化中心', '后勤服务', '信息化中心', 1, 1204, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  ('2026 秋季学期选课安排', '各位同学:\n\n2026 秋季学期选课共分三轮:\n\n第一轮(预选):8 月 20 日 10:00 - 8 月 22 日 22:00\n第二轮(正选):8 月 26 日 10:00 - 8 月 28 日 22:00\n第三轮(补退选):9 月 6 日 10:00 - 9 月 10 日 22:00\n\n请同学们及时登录教务系统查看培养方案,合理规划课程。', '教务信息', '教务处', 1, 3421, DATE_SUB(NOW(), INTERVAL 2 DAY)),
  ('图书馆暑期开放时间调整', '各位读者:\n\n暑期图书馆开放时间为每日 8:30-21:30,其中二层自习区 7:30 开放。8 月 20 日起恢复常规开放时间。\n\n三楼教师阅览室暑期暂停开放,四楼电子阅览室正常开放。', '后勤服务', '图书馆', 1, 876, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  ('校园卡补办流程优化上线', '为减少排队,校园卡挂失与补办已全面支持线上办理。登录校园卡服务平台即可自助申请,补办成功后短信通知领取地点。\n\n首次补办免费,再次补办收取工本费 20 元。', '后勤服务', '后勤服务中心', 1, 653, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  ('校医院疫苗接种安排(九价 HPV 第二轮)', '各位同学:\n\n校医院第二轮九价 HPV 疫苗预约将于 8 月 15 日 12:00 开放,共 300 个名额。预约成功后按短信通知时间接种。\n\n接种请携带身份证与校园卡。', '通知公告', '校医院', 1, 2910, DATE_SUB(NOW(), INTERVAL 7 DAY)),
  ('第二课堂学分认定办法更新', '新版《第二课堂学分认定办法》已经校务会审议通过,自 2026 秋季学期起执行。主要变化:志愿服务学分上限提高,竞赛获奖可折算双倍学分。\n\n详细条款见附件。', '教务信息', '校团委', 1, 1877, DATE_SUB(NOW(), INTERVAL 9 DAY)),
  ('电动车充电桩新增点位启用', '为满足电动车充电需求,后勤保障部在东区宿舍 7 号楼、西区教学楼地下停车场新增充电桩 40 个,现已投入使用。\n\n充电费用按用电量计费,支持校园卡与微信扫码支付。', '后勤服务', '后勤保障部', 1, 998, DATE_SUB(NOW(), INTERVAL 11 DAY)),
  ('学生宿舍热水供应时间调整', '自 9 月起,宿舍热水供应时间调整为每日 6:30-8:30 与 17:00-24:00。暑期非整点时段为 17:00-23:00。\n\n如遇供水异常,请拨打宿舍服务热线 8225 0000。', '后勤服务', '后勤服务中心', 1, 1245, DATE_SUB(NOW(), INTERVAL 14 DAY)),
  ('校园文创纪念品上新', '校史馆文创店上新:银杏书签、校门积木、四季明信片等 12 款新品,开学季全场 88 折。毕业季限定帆布袋同步发售。', '校园活动', '校史馆', 1, 534, DATE_SUB(NOW(), INTERVAL 16 DAY)),
  ('关于开展 2026 年度学生医保参保的通知', '各位同学:\n\n2026 年度学生医保参保工作已启动,集中办理时间为 8 月 25 日至 9 月 15 日。请按学院通知在校园卡服务平台完成参保登记与缴费。\n\n低保家庭学生可申请减免,具体请咨询学院辅导员。', '通知公告', '校医院', 1, 1620, DATE_SUB(NOW(), INTERVAL 18 DAY)),
  ('图书馆新增 24 小时自助还书机', '为方便同学随时还书,图书馆在正门东侧新增 24 小时自助还书机,支持扫描枪快速归还,欢迎使用。', '后勤服务', '图书馆', 1, 742, DATE_SUB(NOW(), INTERVAL 21 DAY)),
  ('关于规范校园电动自行车停放的通知', '为保障消防通道畅通,即日起电动车须停放在划定区域并上锁,严禁进楼充电。违者将按《校园安全管理规定》处理。', '通知公告', '保卫处', 1, 1105, DATE_SUB(NOW(), INTERVAL 25 DAY)),
  ('创新创业训练营招募(30 个名额)', '为期四周的创新创业训练营开始招募,涵盖商业模式、路演技巧与融资基础,结营项目可入驻孵化基地。\n\n报名方式:登录第二课堂系统搜索「训练营」报名。', '校园活动', '创新创业学院', 1, 986, DATE_SUB(NOW(), INTERVAL 28 DAY)),
  ('食堂二楼窗口调整公告', '为提升就餐体验,食堂二楼部分窗口于 9 月 1 日起调整:新增麻辣香锅窗口,清真窗口迁至一楼东侧。', '后勤服务', '后勤服务中心', 1, 689, DATE_SUB(NOW(), INTERVAL 30 DAY)),
  ('关于国家奖学金申请的通知', '2026 年国家奖学金申请通道已开放,截止时间为 9 月 20 日。符合条件的同学请登录学工系统提交申请,并按要求准备证明材料。', '教务信息', '学生资助管理中心', 1, 2308, DATE_SUB(NOW(), INTERVAL 33 DAY));

-- ==================== campus_lost_found ====================
CREATE TABLE `campus_lost_found` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `type` VARCHAR(10) NOT NULL COMMENT '类型:LOST 失物 / FOUND 招领',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `description` TEXT COMMENT '详细描述',
  `location` VARCHAR(100) NOT NULL COMMENT '地点',
  `contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
  `images` JSON COMMENT '图片 URL 数组',
  `status` VARCHAR(10) NOT NULL DEFAULT 'OPEN' COMMENT '状态:OPEN / RESOLVED',
  `publisher_id` BIGINT UNSIGNED NOT NULL COMMENT '发布者用户ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间(映射 createdAt)',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(映射 updatedAt)',
  PRIMARY KEY (`id`),
  KEY `idx_type_status` (`type`, `status`),
  KEY `idx_publisher` (`publisher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='失物招领表';

INSERT INTO `campus_lost_found` (`type`, `title`, `description`, `location`, `contact`, `images`, `status`, `publisher_id`, `created_at`) VALUES
  ('LOST', '黑色钱包(内含校园卡)遗失在图书馆三楼', '黑色短款钱包,里面有校园卡、身份证和少量现金。有捡到的同学请联系我,感谢!', '图书馆三楼中文期刊区', '电话:138 2091 4775 · 图书馆前台可代收', NULL, 'OPEN', 2, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  ('FOUND', '拾到 AirPods 耳机盒(东操场)', '周二晚在东操场看台拾到 AirPods 耳机盒,已在失物招领处登记。请描述盒内刻字以认领。', '东区操场看台', '失物招领处(学生活动中心 101)', '["https://images.pexels.com/photos/3921887/pexels-photo-3921887.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/3921831/pexels-photo-3921831.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/7424282/pexels-photo-7424282.jpeg?auto=compress&cs=tinysrgb&w=800"]', 'OPEN', 5, DATE_SUB(NOW(), INTERVAL 2 DAY)),
  ('LOST', '蓝白条纹雨伞遗失在教三', '蓝色白条纹折叠伞,伞柄有「C」字挂坠,下午 3 点落在教室后排。', '教三 201', 'QQ:841027563', NULL, 'OPEN', 8, DATE_SUB(NOW(), INTERVAL 3 DAY)),
  ('FOUND', '拾到学生卡一张(姓名:陈屿)', '在食堂二楼靠窗位置拾到学生卡,已交给一卡通服务中心。', '食堂二楼', '一卡通服务中心(东门旁)', '["https://images.pexels.com/photos/7108127/pexels-photo-7108127.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/7108126/pexels-photo-7108126.jpeg?auto=compress&cs=tinysrgb&w=800"]', 'RESOLVED', 10, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  ('LOST', '灰色书包遗落在快递站', '灰色双肩包,里面有教材和 iPad 保护套。监控显示下午 5 点后还在。', '菜鸟驿站', '电话:186 7712 0394', NULL, 'OPEN', 12, DATE_SUB(NOW(), INTERVAL 5 DAY)),
  ('FOUND', '拾到钥匙串(带蓝色挂件)', '一串钥匙,带蓝色星球挂件和门禁卡套。请描述钥匙数量以认领。', '湖畔草坪', '湖畔巡逻岗亭', '["https://images.pexels.com/photos/1194027/pexels-photo-1194027.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/109361/pexels-photo-109361.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/16329644/pexels-photo-16329644.jpeg?auto=compress&cs=tinysrgb&w=800"]', 'OPEN', 14, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  ('LOST', '银色保温杯遗失在食堂二楼', '银色 500ml 保温杯,杯身贴了「明天也要加油」贴纸。', '食堂二楼', '电话:150 6634 8821', NULL, 'OPEN', 16, DATE_SUB(NOW(), INTERVAL 7 DAY)),
  ('FOUND', '拾到校园卡(工商银行卡联名)', '工商银行卡联名校园卡,卡号尾号 1024,已交至保卫处。', '教学楼 B 栋电梯口', '保卫处值班室(行政楼 110)', '["https://images.pexels.com/photos/50987/money-card-business-credit-card-50987.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5717969/pexels-photo-5717969.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6214152/pexels-photo-6214152.jpeg?auto=compress&cs=tinysrgb&w=800"]', 'RESOLVED', 18, DATE_SUB(NOW(), INTERVAL 8 DAY)),
  ('LOST', '黑色单反相机包(内含镜头)', '黑色相机包,内有佳能套机镜头。摄影课作业需要,捡到的同学麻烦联系,必有酬谢。', '校史馆门口', '电话:188 9034 2216', NULL, 'OPEN', 20, DATE_SUB(NOW(), INTERVAL 9 DAY)),
  ('FOUND', '拾到米色围巾一条', '米色针织围巾,挂在椅背上,已交给前台保管。', '图书馆四楼电子阅览室', '图书馆四楼前台', '["https://images.pexels.com/photos/5710117/pexels-photo-5710117.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5603205/pexels-photo-5603205.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6045254/pexels-photo-6045254.jpeg?auto=compress&cs=tinysrgb&w=800"]', 'OPEN', 21, DATE_SUB(NOW(), INTERVAL 10 DAY)),
  ('LOST', '白色蓝牙耳机(右耳)遗失在东操场', '右耳单独遗失,白色入耳式,耳机仓还在。拾到请私聊,感谢!', '东区操场', 'QQ:772041865', NULL, 'OPEN', 22, DATE_SUB(NOW(), INTERVAL 11 DAY)),
  ('FOUND', '拾到复习资料一摞(考研数学)', '一摞考研数学复习资料,封面写着「上岸!」,已交至教一楼管室。', '教一 105', '教一楼管室', '["https://images.pexels.com/photos/9291615/pexels-photo-9291615.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/7034647/pexels-photo-7034647.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5009160/pexels-photo-5009160.jpeg?auto=compress&cs=tinysrgb&w=800"]', 'OPEN', 24, DATE_SUB(NOW(), INTERVAL 12 DAY)),
  ('LOST', '银灰色拉杆箱遗落在校门口', '银灰色 20 寸拉杆箱,箱面有贴纸。下午三点半下车忘拿,很重要!', '学校东门', '电话:176 9088 3312', NULL, 'OPEN', 25, DATE_SUB(NOW(), INTERVAL 13 DAY)),
  ('FOUND', '拾到校园卡(尾号 6619)', '在收餐台附近拾到校园卡一张,姓名拼音缩写 LWC。', '西区食堂', '西区食堂服务台', '["https://images.pexels.com/photos/7821483/pexels-photo-7821483.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/583881/pexels-photo-583881.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/7821730/pexels-photo-7821730.jpeg?auto=compress&cs=tinysrgb&w=800"]', 'RESOLVED', 27, DATE_SUB(NOW(), INTERVAL 14 DAY)),
  ('LOST', '黑色皮质手套遗失在行政楼', '左手一只黑色皮手套,开会时落的,有捡到的同学麻烦联系。', '行政楼 2 层', '电话:151 8832 7409', NULL, 'OPEN', 28, DATE_SUB(NOW(), INTERVAL 15 DAY)),
  ('FOUND', '拾到充电宝(白色 20000mAh)', '白色充电宝,带一根数据线,已交校车站值班室。', '校车站', '校车站值班室', '["https://images.pexels.com/photos/34338614/pexels-photo-34338614.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/3921704/pexels-photo-3921704.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8137310/pexels-photo-8137310.jpeg?auto=compress&cs=tinysrgb&w=800"]', 'OPEN', 30, DATE_SUB(NOW(), INTERVAL 16 DAY)),
  ('LOST', 'kindle 阅读器遗失在图书馆', '黑色 Kindle,带蓝色官方壳,屏幕贴了磨砂膜。自习时放在桌上忘了拿。', '图书馆五楼自习区', '电话:159 4420 6197', NULL, 'OPEN', 32, DATE_SUB(NOW(), INTERVAL 17 DAY)),
  ('FOUND', '拾到羽毛球拍(黑色拍套)', '黑色拍套,里面一支球拍,球拍柄缠了白色手胶。', '体育馆羽毛球馆', '体育馆前台', '["https://images.pexels.com/photos/6307231/pexels-photo-6307231.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8286363/pexels-photo-8286363.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/9804653/pexels-photo-9804653.jpeg?auto=compress&cs=tinysrgb&w=800"]', 'OPEN', 34, DATE_SUB(NOW(), INTERVAL 18 DAY)),
  ('LOST', '学士服(借用后未归还)', '毕业典礼借用的学士服,归还时拿错了,蓝色 S 码。请拿错的同学联系我换回来。', '大学生活动中心', 'QQ:663102947', NULL, 'OPEN', 36, DATE_SUB(NOW(), INTERVAL 19 DAY)),
  ('FOUND', '拾到课本《管理学原理》', '书里夹着一张手写笔记,应该是不小心落在抽屉里的。', '教二 306', '教二 306 讲台', '["https://images.pexels.com/photos/32046500/pexels-photo-32046500.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/29171841/pexels-photo-29171841.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/240163/pexels-photo-240163.jpeg?auto=compress&cs=tinysrgb&w=800"]', 'RESOLVED', 38, DATE_SUB(NOW(), INTERVAL 20 DAY)),
  ('LOST', '绿色保温杯遗失在操场看台', '绿色 350ml 保温杯,杯盖有划痕,经常用来接热水。', '东区操场看台', '电话:133 9266 1054', NULL, 'OPEN', 40, DATE_SUB(NOW(), INTERVAL 21 DAY)),
  ('FOUND', '拾到相机存储卡一张', 'SD 卡一张,里面应该是摄影作业,已交摄影协会保管。', '中心花园长椅', '摄影协会(学生活动中心 302)', '["https://images.pexels.com/photos/30758131/pexels-photo-30758131.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/18166725/pexels-photo-18166725.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/193057/pexels-photo-193057.jpeg?auto=compress&cs=tinysrgb&w=800"]', 'OPEN', 42, DATE_SUB(NOW(), INTERVAL 22 DAY)),
  ('LOST', '自行车钥匙串遗失在车棚', '一把自行车钥匙+门禁卡,蓝色硅胶套。', '东区宿舍 7 号楼车棚', 'QQ:992107846', NULL, 'OPEN', 44, DATE_SUB(NOW(), INTERVAL 23 DAY)),
  ('FOUND', '拾到近视眼镜一副(黑色框)', '黑色方框眼镜,度数大概 300 度,已交失物招领处。', '教四 201', '失物招领处', '["https://images.pexels.com/photos/32398468/pexels-photo-32398468.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/9328548/pexels-photo-9328548.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/13430471/pexels-photo-13430471.jpeg?auto=compress&cs=tinysrgb&w=800"]', 'OPEN', 46, DATE_SUB(NOW(), INTERVAL 24 DAY)),
  ('LOST', '校园卡(卡套是皮卡丘)', '校园卡掉了,卡套是黄色皮卡丘,补办要 20 块,捡到的同学拜托了!', '全校范围', '电话:180 1158 7432', NULL, 'OPEN', 48, DATE_SUB(NOW(), INTERVAL 25 DAY));

SET FOREIGN_KEY_CHECKS = 1;
