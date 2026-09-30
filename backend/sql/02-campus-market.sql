-- CampusHub 微服务数据库初始化脚本
-- 数据库: campus_market
-- 来源: campushub.sql
-- 自动拆分：保留原表结构、索引和种子数据

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `campus_market`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE `campus_market`;

-- ==================== campus_product_category ====================
CREATE TABLE `campus_product_category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序(越小越靠前)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

INSERT INTO `campus_product_category` (`id`, `name`, `sort_order`) VALUES
  (1, '数码电子', 1),
  (2, '图书教材', 2),
  (3, '服饰鞋包', 3),
  (4, '运动户外', 4),
  (5, '生活好物', 5),
  (6, '美妆个护', 6),
  (7, '其他', 7);

-- ==================== campus_product ====================
CREATE TABLE `campus_product` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `title` VARCHAR(100) NOT NULL COMMENT '商品标题',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价/参考价',
  `description` TEXT COMMENT '商品描述',
  `images` JSON COMMENT '图片 URL 数组,如 ["https://...","https://..."]',
  `seller_id` BIGINT UNSIGNED NOT NULL COMMENT '卖家用户ID',
  `status` VARCHAR(10) NOT NULL DEFAULT 'ON_SALE' COMMENT '状态:ON_SALE / OFF_SHELF / SOLD',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数(冗余,收藏/取消时同步)',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量(详情接口 +1)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间(映射 createdAt)',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(映射 updatedAt)',
  PRIMARY KEY (`id`),
  KEY `idx_status_category` (`status`, `category_id`),
  KEY `idx_seller` (`seller_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二手商品表';

INSERT INTO `campus_product` (`title`, `category_id`, `price`, `original_price`, `description`, `images`, `seller_id`, `status`, `favorite_count`, `view_count`, `created_at`) VALUES
  ('ikbc C87 红轴机械键盘 95 新', 1, 199.00, 259.00, '毕业出,自用两年,红轴手感舒服,键帽换了 PBT 侧刻,带原装数据线,可当面验货。', '["https://images.pexels.com/photos/5152261/pexels-photo-5152261.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/4792720/pexels-photo-4792720.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/9020272/pexels-photo-9020272.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 45, 743, DATE_SUB(NOW(), INTERVAL 27 DAY)),
  ('Sony WH-1000XM4 降噪耳机 国行在保', 1, 1299.00, 1999.00, '去年双十一入手,戴的少,成色接近全新,箱说齐全,保修到明年。', '["https://images.pexels.com/photos/7772548/pexels-photo-7772548.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/3394650/pexels-photo-3394650.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5269726/pexels-photo-5269726.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 37, 357, DATE_SUB(NOW(), INTERVAL 33 DAY)),
  ('AirPods Pro 2 国行带 AC+', 1, 1399.00, 1899.00, '换了 Android 手机用不上,支持序列号验机,发票可提供。', '["https://images.pexels.com/photos/3921872/pexels-photo-3921872.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/3921846/pexels-photo-3921846.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/3921827/pexels-photo-3921827.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'ON_SALE', 12, 426, DATE_SUB(NOW(), INTERVAL 18 DAY)),
  ('罗技 G502 Hero 游戏鼠标', 1, 169.00, 299.00, '可调配重,手感好,打游戏和写代码都合适,功能完好。', '["https://images.pexels.com/photos/29259392/pexels-photo-29259392.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/2115256/pexels-photo-2115256.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/1486294/pexels-photo-1486294.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 23, 700, DATE_SUB(NOW(), INTERVAL 39 DAY)),
  ('富士拍立得 mini12 含 20 张相纸', 1, 449.00, 599.00, '拍了一阵子闲置了,相纸还有 3 盒一起出,校园活动合影神器。', '["https://images.pexels.com/photos/10806074/pexels-photo-10806074.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/2438300/pexels-photo-2438300.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/4062180/pexels-photo-4062180.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 45, 568, DATE_SUB(NOW(), INTERVAL 32 DAY)),
  ('极米 Z6X 智能投影仪', 1, 1880.00, 2899.00, '宿舍观影神器,1080P 物理分辨率,哈曼卡顿音响,无拆无修。', '["https://images.pexels.com/photos/6062806/pexels-photo-6062806.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/16699711/pexels-photo-16699711.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/11092278/pexels-photo-11092278.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'OFF_SHELF', 9, 315, DATE_SUB(NOW(), INTERVAL 19 DAY)),
  ('Nintendo Switch OLED 日版', 1, 1599.00, 2399.00, '研究生毕业出,吃灰已久,带底座、Joy-Con 和两张卡带。', '["https://images.pexels.com/photos/371924/pexels-photo-371924.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6993180/pexels-photo-6993180.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/4523029/pexels-photo-4523029.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 22, 38, DATE_SUB(NOW(), INTERVAL 25 DAY)),
  ('小米手环 8 NFC 版', 1, 149.00, 249.00, '可以刷校园卡和公交,跑步计步精准,屏幕无划痕。', '["https://images.pexels.com/photos/4429155/pexels-photo-4429155.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/374619/pexels-photo-374619.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5036927/pexels-photo-5036927.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'SOLD', 3, 553, DATE_SUB(NOW(), INTERVAL 38 DAY)),
  ('惠普 27 寸 2K 显示器', 1, 699.00, 1099.00, '考研上岸出,无坏点漏光,带 DP/HDMI 线。', '["https://images.pexels.com/photos/12509206/pexels-photo-12509206.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8353774/pexels-photo-8353774.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/115655/pexels-photo-115655.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'ON_SALE', 4, 149, DATE_SUB(NOW(), INTERVAL 2 DAY)),
  ('雷蛇黑寡妇蜘蛛 V3 竞技版', 1, 289.00, 499.00, '绿轴段落感强,键帽无打油,宿舍打字建议换红轴。', '["https://images.pexels.com/photos/5380584/pexels-photo-5380584.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/841228/pexels-photo-841228.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/28993111/pexels-photo-28993111.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 26, 719, DATE_SUB(NOW(), INTERVAL 19 DAY)),
  ('考研数学复习全书 2027 版', 2, 45.00, 89.00, '上岸出书,只有零星笔记,附赠真题分类讲解电子版。', '["https://images.pexels.com/photos/1438044/pexels-photo-1438044.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/23656142/pexels-photo-23656142.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/159866/books-book-pages-read-literature-159866.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 50, 194, DATE_SUB(NOW(), INTERVAL 55 DAY)),
  ('同济高数第七版 上下册', 2, 30.00, 68.00, '经典教材,书页无破损,封面微磨损,习题答案册一起送。', '["https://images.pexels.com/photos/9791429/pexels-photo-9791429.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/34923529/pexels-photo-34923529.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6958552/pexels-photo-6958552.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'ON_SALE', 27, 126, DATE_SUB(NOW(), INTERVAL 27 DAY)),
  ('计算机网络教材(谢希仁版)', 2, 25.00, 55.00, '期末复习必备,划了重点,适合冲刺阶段参考。', '["https://images.pexels.com/photos/1181573/pexels-photo-1181573.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8534382/pexels-photo-8534382.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/34991783/pexels-photo-34991783.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 21, 391, DATE_SUB(NOW(), INTERVAL 15 DAY)),
  ('雅思剑 16-18 真题+听力音频', 2, 60.00, 128.00, '考过出书,九成新,听力音频网盘链接同步赠送。', '["https://images.pexels.com/photos/11565595/pexels-photo-11565595.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5676740/pexels-photo-5676740.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/3248644/pexels-photo-3248644.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 31, 464, DATE_SUB(NOW(), INTERVAL 22 DAY)),
  ('法考客观题精讲卷全套', 2, 88.00, 168.00, '去年过考,书基本全新,重点笔记都在。', '["https://images.pexels.com/photos/8849335/pexels-photo-8849335.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8731037/pexels-photo-8731037.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6077296/pexels-photo-6077296.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'SOLD', 31, 300, DATE_SUB(NOW(), INTERVAL 57 DAY)),
  ('考研英语黄皮书 2010-2026 真题', 2, 55.00, 108.00, '逐句精解版,笔迹很少,适合二轮刷题。', '["https://images.pexels.com/photos/37778746/pexels-photo-37778746.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6929186/pexels-photo-6929186.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5386466/pexels-photo-5386466.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 37, 538, DATE_SUB(NOW(), INTERVAL 26 DAY)),
  ('CPA 会计+财管教材与轻一', 2, 120.00, 240.00, '毕业出全套,轻一上有完整笔记,能省不少时间。', '["https://images.pexels.com/photos/8962469/pexels-photo-8962469.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8296970/pexels-photo-8296970.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8296998/pexels-photo-8296998.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 30, 682, DATE_SUB(NOW(), INTERVAL 31 DAY)),
  ('护理资格考试全套资料', 2, 40.00, 90.00, '含真题与知识点总结,压箱底资料打包出。', '["https://images.pexels.com/photos/32115953/pexels-photo-32115953.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/37406585/pexels-photo-37406585.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/37407208/pexels-photo-37407208.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'OFF_SHELF', 51, 298, DATE_SUB(NOW(), INTERVAL 53 DAY)),
  ('数据结构与算法(严蔚敏版)', 2, 20.00, 42.00, '经典教材,考研复试和面试复习都合适。', '["https://images.pexels.com/photos/1181281/pexels-photo-1181281.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/10826689/pexels-photo-10826689.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/1181568/pexels-photo-1181568.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 56, 423, DATE_SUB(NOW(), INTERVAL 29 DAY)),
  ('大学物理学习指导(上下册)', 2, 28.00, 60.00, '工科必买,例题解析非常全,适合期末突击。', '["https://images.pexels.com/photos/6579277/pexels-photo-6579277.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/1430796/pexels-photo-1430796.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6344231/pexels-photo-6344231.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 39, 343, DATE_SUB(NOW(), INTERVAL 47 DAY)),
  ('森马连帽卫衣 全新带吊牌', 3, 89.00, 159.00, '买错尺码,全新未拆吊牌,藏青色 M 码。', '["https://images.pexels.com/photos/5840463/pexels-photo-5840463.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5319513/pexels-photo-5319513.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/11340657/pexels-photo-11340657.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'ON_SALE', 34, 139, DATE_SUB(NOW(), INTERVAL 22 DAY)),
  ('北面冲锋衣 三合一可拆卸内胆', 3, 680.00, 1299.00, '冬季通勤上课都合适,防风防水,穿过一季。', '["https://images.pexels.com/photos/19736559/pexels-photo-19736559.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/11344358/pexels-photo-11344358.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/28758132/pexels-photo-28758132.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 42, 724, DATE_SUB(NOW(), INTERVAL 18 DAY)),
  ('复古帆布邮差包 牛皮', 3, 120.00, 220.00, '上课背电脑刚好,皮质越用越有味道,肩带可调。', '["https://images.pexels.com/photos/36492563/pexels-photo-36492563.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/36492469/pexels-photo-36492469.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5950017/pexels-photo-5950017.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 47, 148, DATE_SUB(NOW(), INTERVAL 46 DAY)),
  ('优衣库摇粒绒外套 M 码', 3, 79.00, 149.00, '只穿过几次,洗过无污渍,奶白色很百搭。', '["https://images.pexels.com/photos/19771863/pexels-photo-19771863.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/20103948/pexels-photo-20103948.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/17492503/pexels-photo-17492503.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'ON_SALE', 32, 265, DATE_SUB(NOW(), INTERVAL 18 DAY)),
  ('匡威帆布鞋 42 码 高帮', 3, 99.00, 199.00, '穿了两季,鞋底磨损不大,刷干净了。', '["https://images.pexels.com/photos/27015417/pexels-photo-27015417.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/4271563/pexels-photo-4271563.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/4296075/pexels-photo-4296075.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 12, 892, DATE_SUB(NOW(), INTERVAL 41 DAY)),
  ('南极人羽绒服 男款 L 码', 3, 149.00, 329.00, '北方过冬够用,含绒量不错,出给有缘人。', '["https://images.pexels.com/photos/8974263/pexels-photo-8974263.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8830576/pexels-photo-8830576.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/10786118/pexels-photo-10786118.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'OFF_SHELF', 31, 666, DATE_SUB(NOW(), INTERVAL 57 DAY)),
  ('双肩电脑包 17 寸 防泼水', 3, 85.00, 169.00, '毕业出,容量大,出差通勤都能背。', '["https://images.pexels.com/photos/7054778/pexels-photo-7054778.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/18269634/pexels-photo-18269634.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/15059375/pexels-photo-15059375.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'SOLD', 45, 826, DATE_SUB(NOW(), INTERVAL 9 DAY)),
  ('JBL 头戴式耳机 黑色', 3, 99.00, 249.00, '蓝牙有线双模,包耳舒服,自习党友好。', '["https://images.pexels.com/photos/210927/pexels-photo-210927.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8356854/pexels-photo-8356854.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/210926/pexels-photo-210926.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 49, 727, DATE_SUB(NOW(), INTERVAL 44 DAY)),
  ('捷安特 ATX 山地车 24 速', 4, 850.00, 1399.00, '大一买的,刹车变速都调过,送车锁和头盔。', '["https://images.pexels.com/photos/36450314/pexels-photo-36450314.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/16082647/pexels-photo-16082647.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/37897835/pexels-photo-37897835.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 34, 253, DATE_SUB(NOW(), INTERVAL 38 DAY)),
  ('李宁雷霆 80 羽毛球拍', 4, 420.00, 720.00, '进攻型球拍,适合有点基础的球友,无伤无裂。', '["https://images.pexels.com/photos/12630113/pexels-photo-12630113.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8286363/pexels-photo-8286363.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6307231/pexels-photo-6307231.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'ON_SALE', 37, 618, DATE_SUB(NOW(), INTERVAL 37 DAY)),
  ('哑铃套装 20kg 可拆卸', 4, 180.00, 320.00, '宿舍健身够用,杠铃片防滑处理,闲置出自提。', '["https://images.pexels.com/photos/37887283/pexels-photo-37887283.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/669580/pexels-photo-669580.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/3931367/pexels-photo-3931367.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 55, 206, DATE_SUB(NOW(), INTERVAL 44 DAY)),
  ('露营帐篷+防潮垫套装', 4, 260.00, 480.00, '用过两次,无破损,适合 2-3 人,毕业清仓。', '["https://images.pexels.com/photos/33102150/pexels-photo-33102150.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/15925118/pexels-photo-15925118.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/4268094/pexels-photo-4268094.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 34, 223, DATE_SUB(NOW(), INTERVAL 11 DAY)),
  ('瑜伽垫 10mm 加厚防滑', 4, 45.00, 89.00, '健身房宿舍都能用,洗过晒过,无异味。', '["https://images.pexels.com/photos/8539005/pexels-photo-8539005.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/28821063/pexels-photo-28821063.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8436578/pexels-photo-8436578.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'ON_SALE', 49, 74, DATE_SUB(NOW(), INTERVAL 24 DAY)),
  ('羽毛球拍 尤尼克斯天斧 99', 4, 560.00, 880.00, '挥速快,进攻利器,换过手胶,可小刀。', '["https://images.pexels.com/photos/8007419/pexels-photo-8007419.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6878017/pexels-photo-6878017.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8007173/pexels-photo-8007173.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'SOLD', 35, 435, DATE_SUB(NOW(), INTERVAL 30 DAY)),
  ('篮球 威尔胜 7 号 室内外通用', 4, 89.00, 169.00, '手感还在,室内外都打过,没鼓包。', '["https://images.pexels.com/photos/12954255/pexels-photo-12954255.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6076409/pexels-photo-6076409.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/1462618/pexels-photo-1462618.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 48, 512, DATE_SUB(NOW(), INTERVAL 58 DAY)),
  ('椭圆机 家用静音款', 4, 699.00, 1299.00, '宿舍楼有氧神器,占地方所以出,功能正常。', '["https://images.pexels.com/photos/47084/gym-exercise-fitness-workout-47084.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6285198/pexels-photo-6285198.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6285181/pexels-photo-6285181.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'OFF_SHELF', 9, 122, DATE_SUB(NOW(), INTERVAL 45 DAY)),
  ('小米手环充电器+表带若干', 4, 20.00, 45.00, '手环退役后留下的配件,需要可带走。', '["https://images.pexels.com/photos/18662969/pexels-photo-18662969.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5237704/pexels-photo-5237704.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8217438/pexels-photo-8217438.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 21, 185, DATE_SUB(NOW(), INTERVAL 21 DAY)),
  ('雅马哈 F310 民谣吉他', 5, 520.00, 899.00, '初学练手足够,面板无磕碰,弦距调过好按,送琴包变调夹。', '["https://images.pexels.com/photos/7558112/pexels-photo-7558112.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/4688751/pexels-photo-4688751.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/9057766/pexels-photo-9057766.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 57, 624, DATE_SUB(NOW(), INTERVAL 33 DAY)),
  ('宜家落地灯 暖光', 5, 75.00, 129.00, '宿舍床边看书用,亮度三档可调,自提优先。', '["https://images.pexels.com/photos/6078545/pexels-photo-6078545.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/12648787/pexels-photo-12648787.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/18873546/pexels-photo-18873546.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'ON_SALE', 52, 723, DATE_SUB(NOW(), INTERVAL 42 DAY)),
  ('迷你宿舍冰箱 45L', 5, 320.00, 599.00, '冷藏冷冻双温区,声音小功率低,毕业出自提。', '["https://images.pexels.com/photos/37637045/pexels-photo-37637045.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/34872425/pexels-photo-34872425.png?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/34872428/pexels-photo-34872428.png?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 23, 899, DATE_SUB(NOW(), INTERVAL 9 DAY)),
  ('尤克里里 23 寸 桃花心木', 5, 180.00, 320.00, '社团活动奖品,全新未使用,送调音器和包。', '["https://images.pexels.com/photos/6564512/pexels-photo-6564512.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6564513/pexels-photo-6564513.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5586494/pexels-photo-5586494.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 10, 818, DATE_SUB(NOW(), INTERVAL 59 DAY)),
  ('小米体脂秤 2 代', 5, 59.00, 99.00, '宿舍称重好帮手,蓝牙连接米家 App,电池还有电。', '["https://images.pexels.com/photos/6975463/pexels-photo-6975463.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/53404/scale-diet-fat-health-53404.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/29422934/pexels-photo-29422934.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'ON_SALE', 17, 777, DATE_SUB(NOW(), INTERVAL 37 DAY)),
  ('美的台灯 护眼无频闪', 5, 55.00, 99.00, '三档色温,陪伴了整个考研期,功能完好。', '["https://images.pexels.com/photos/7439756/pexels-photo-7439756.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/32543097/pexels-photo-32543097.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/28461166/pexels-photo-28461166.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 13, 744, DATE_SUB(NOW(), INTERVAL 33 DAY)),
  ('宿舍床上桌 可折叠', 5, 39.00, 79.00, '冬日起床困难户必备,桌面无划痕。', '["https://images.pexels.com/photos/31421652/pexels-photo-31421652.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/18267776/pexels-photo-18267776.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/11502135/pexels-photo-11502135.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 60, 807, DATE_SUB(NOW(), INTERVAL 40 DAY)),
  ('电煮锅 宿舍小功率', 5, 69.00, 129.00, '小功率不跳闸,煮面煮粥都行,锅体很干净。', '["https://images.pexels.com/photos/29479438/pexels-photo-29479438.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/29479440/pexels-photo-29479440.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/30915727/pexels-photo-30915727.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'OFF_SHELF', 55, 396, DATE_SUB(NOW(), INTERVAL 13 DAY)),
  ('兰蔻小黑瓶 50ml 全新', 6, 520.00, 850.00, '朋友从免税店带多了一瓶,全新未拆封,有效期到 2028。', '["https://images.pexels.com/photos/35899861/pexels-photo-35899861.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8054400/pexels-photo-8054400.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8100691/pexels-photo-8100691.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 38, 553, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  ('SK-II 神仙水 230ml 余 8 成', 6, 780.00, 1280.00, '用了不到两成,肤质不合适出,可小刀。', '["https://images.pexels.com/photos/4735904/pexels-photo-4735904.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5632335/pexels-photo-5632335.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/27544680/pexels-photo-27544680.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 24, 211, DATE_SUB(NOW(), INTERVAL 7 DAY)),
  ('戴森吹风机 HD08 国行', 6, 1580.00, 2499.00, '成色 95 新,包装盒和风嘴都在,自用两年。', '["https://images.pexels.com/photos/32641724/pexels-photo-32641724.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/10892870/pexels-photo-10892870.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/3993328/pexels-photo-3993328.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'ON_SALE', 47, 726, DATE_SUB(NOW(), INTERVAL 35 DAY)),
  ('雅诗兰黛小棕瓶 100ml', 6, 680.00, 1080.00, '朋友回国帮带,用了一周觉得不合适,余 9 成。', '["https://images.pexels.com/photos/8101512/pexels-photo-8101512.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/7670680/pexels-photo-7670680.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8167164/pexels-photo-8167164.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 37, 727, DATE_SUB(NOW(), INTERVAL 15 DAY)),
  ('科颜氏高保湿面霜 125ml', 6, 199.00, 349.00, '秋冬干皮救星,开封两月,用了三分之一。', '["https://images.pexels.com/photos/27544670/pexels-photo-27544670.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/6690232/pexels-photo-6690232.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/29652923/pexels-photo-29652923.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 11, 322, DATE_SUB(NOW(), INTERVAL 28 DAY)),
  ('电动牙刷 飞利浦 HX6730', 6, 149.00, 399.00, '刷牙模式三种,配两支新刷头,已消毒。', '["https://images.pexels.com/photos/33215341/pexels-photo-33215341.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/4045552/pexels-photo-4045552.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/28407751/pexels-photo-28407751.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'SOLD', 4, 774, DATE_SUB(NOW(), INTERVAL 45 DAY)),
  ('电动自行车 学生代步 48V', 7, 1180.00, 1999.00, '校园通勤利器,电池可拆卸充电,有正规发票。', '["https://images.pexels.com/photos/16435192/pexels-photo-16435192.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/15884318/pexels-photo-15884318.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8811401/pexels-photo-8811401.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 12, 471, DATE_SUB(NOW(), INTERVAL 47 DAY)),
  ('毕业季出:宿舍收纳全套', 7, 40.00, 89.00, '置物架、收纳箱、挂篮一整套,自提打包价。', '["https://images.pexels.com/photos/7309200/pexels-photo-7309200.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/10938208/pexels-photo-10938208.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/3912293/pexels-photo-3912293.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 5, 119, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  ('Kindle Paperwhite 5', 7, 399.00, 698.00, '墨水屏看书不伤眼,带官方保护壳,屏幕无划痕。', '["https://images.pexels.com/photos/1329571/pexels-photo-1329571.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/1475296/pexels-photo-1475296.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/844734/pexels-photo-844734.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'ON_SALE', 18, 511, DATE_SUB(NOW(), INTERVAL 30 DAY)),
  ('三脚架 相机手机通用', 7, 89.00, 169.00, '摄影作业用,铝合金材质,很稳。', '["https://images.pexels.com/photos/11748183/pexels-photo-11748183.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/32717685/pexels-photo-32717685.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 53, 702, DATE_SUB(NOW(), INTERVAL 36 DAY)),
  ('行李箱 24 寸 万向轮', 7, 129.00, 299.00, '毕业旅行用了几次,轮子顺滑,拉链完好。', '["https://images.pexels.com/photos/34629933/pexels-photo-34629933.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5705068/pexels-photo-5705068.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/36933446/pexels-photo-36933446.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 32, 621, DATE_SUB(NOW(), INTERVAL 11 DAY)),
  ('路由器 小米 AX3000', 7, 99.00, 199.00, '宿舍网络神器,双千兆,发热小。', '["https://images.pexels.com/photos/4218546/pexels-photo-4218546.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/29711663/pexels-photo-29711663.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/18071864/pexels-photo-18071864.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'ON_SALE', 51, 177, DATE_SUB(NOW(), INTERVAL 17 DAY)),
  ('桌面增高架 带抽屉', 7, 35.00, 69.00, '显示器垫高,颈椎友好,桌面收纳好物。', '["https://images.pexels.com/photos/34502055/pexels-photo-34502055.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/4792712/pexels-photo-4792712.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/8316281/pexels-photo-8316281.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 24, 712, DATE_SUB(NOW(), INTERVAL 57 DAY)),
  ('宿舍小风扇 夹式充电款', 7, 45.00, 89.00, '停电和夏天午睡都用得上,静音,电量足。', '["https://images.pexels.com/photos/3675622/pexels-photo-3675622.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/5850340/pexels-photo-5850340.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/4389919/pexels-photo-4389919.jpeg?auto=compress&cs=tinysrgb&w=800"]', 15, 'ON_SALE', 20, 170, DATE_SUB(NOW(), INTERVAL 25 DAY)),
  ('电吉他 Squier Affinity', 7, 899.00, 1499.00, '乐队排练用,琴颈状态好,配音箱一起出。', '["https://images.pexels.com/photos/194011/pexels-photo-194011.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/12393614/pexels-photo-12393614.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/2049414/pexels-photo-2049414.jpeg?auto=compress&cs=tinysrgb&w=800"]', 28, 'OFF_SHELF', 11, 91, DATE_SUB(NOW(), INTERVAL 54 DAY)),
  ('自行车头盔 白色 M 码', 7, 49.00, 99.00, '骑行必备,只戴过几次,内衬洗过。', '["https://images.pexels.com/photos/12956080/pexels-photo-12956080.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/37625583/pexels-photo-37625583.jpeg?auto=compress&cs=tinysrgb&w=800","https://images.pexels.com/photos/13673124/pexels-photo-13673124.jpeg?auto=compress&cs=tinysrgb&w=800"]', 2, 'ON_SALE', 4, 641, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ==================== campus_favorite ====================
CREATE TABLE `campus_favorite` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏表';

INSERT INTO `campus_favorite` (`user_id`, `product_id`, `created_at`) VALUES
  (24, 33, DATE_SUB(NOW(), INTERVAL 29 DAY)),
  (30, 39, DATE_SUB(NOW(), INTERVAL 29 DAY)),
  (16, 21, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (27, 50, DATE_SUB(NOW(), INTERVAL 18 DAY)),
  (23, 2, DATE_SUB(NOW(), INTERVAL 19 DAY)),
  (17, 13, DATE_SUB(NOW(), INTERVAL 9 DAY)),
  (2, 14, DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (31, 27, DATE_SUB(NOW(), INTERVAL 12 DAY)),
  (4, 4, DATE_SUB(NOW(), INTERVAL 27 DAY)),
  (11, 49, DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (22, 38, DATE_SUB(NOW(), INTERVAL 15 DAY)),
  (14, 45, DATE_SUB(NOW(), INTERVAL 24 DAY)),
  (16, 31, DATE_SUB(NOW(), INTERVAL 22 DAY)),
  (16, 26, DATE_SUB(NOW(), INTERVAL 21 DAY)),
  (16, 47, DATE_SUB(NOW(), INTERVAL 21 DAY)),
  (21, 21, DATE_SUB(NOW(), INTERVAL 18 DAY)),
  (36, 15, DATE_SUB(NOW(), INTERVAL 21 DAY)),
  (2, 44, DATE_SUB(NOW(), INTERVAL 16 DAY)),
  (23, 45, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  (10, 56, DATE_SUB(NOW(), INTERVAL 24 DAY)),
  (14, 6, DATE_SUB(NOW(), INTERVAL 5 DAY)),
  (7, 37, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (30, 19, DATE_SUB(NOW(), INTERVAL 5 DAY)),
  (30, 43, DATE_SUB(NOW(), INTERVAL 8 DAY)),
  (11, 20, DATE_SUB(NOW(), INTERVAL 27 DAY)),
  (11, 60, DATE_SUB(NOW(), INTERVAL 14 DAY)),
  (26, 19, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  (33, 57, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  (4, 13, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (38, 8, DATE_SUB(NOW(), INTERVAL 13 DAY)),
  (6, 33, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  (35, 14, DATE_SUB(NOW(), INTERVAL 10 DAY)),
  (21, 33, DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (36, 23, DATE_SUB(NOW(), INTERVAL 25 DAY)),
  (30, 52, DATE_SUB(NOW(), INTERVAL 22 DAY)),
  (29, 41, DATE_SUB(NOW(), INTERVAL 28 DAY)),
  (37, 47, DATE_SUB(NOW(), INTERVAL 22 DAY)),
  (13, 2, DATE_SUB(NOW(), INTERVAL 7 DAY)),
  (30, 37, DATE_SUB(NOW(), INTERVAL 15 DAY)),
  (14, 14, DATE_SUB(NOW(), INTERVAL 10 DAY)),
  (10, 6, DATE_SUB(NOW(), INTERVAL 12 DAY)),
  (19, 39, DATE_SUB(NOW(), INTERVAL 8 DAY)),
  (18, 53, DATE_SUB(NOW(), INTERVAL 5 DAY)),
  (40, 20, DATE_SUB(NOW(), INTERVAL 12 DAY)),
  (21, 13, DATE_SUB(NOW(), INTERVAL 17 DAY));

SET FOREIGN_KEY_CHECKS = 1;
