/**
 * CampusHub 种子数据生成器
 * 运行:node backend/sql/generate_seed.js
 * 输出:backend/sql/campushub.sql(DDL + DML)
 *
 * 设计要点:
 * - 所有时间均相对 NOW() 生成,导入后活动/公告/失物的时间线依然正确
 * - 活动 current_participants 与 campus_activity_registration 行数严格一致
 * - 种子账号密码统一为 123456(BCrypt)
 */
const fs = require('fs')
const path = require('path')

const PASSWORD_HASH = '$2b$10$bCv8Itl9USUQ2cKPgoUmOuF1mTvbCLPuWq.cbdylNV26vsKtY/C3K'

// 头像:24 个人像插画循环使用(DiceBear avataaars 在线服务,不依赖本地文件)
// 编号规则保持不变:第 id 个用户用 avatar-((id-1)%24+1)
const avatarUrl = (id) => {
  const n = String(((id - 1) % 24) + 1).padStart(2, '0')
  return `https://api.dicebear.com/8.x/avataaars/svg?seed=avatar-${n}`
}

const esc = (value) =>
  String(value === null || value === undefined ? '' : value)
    .replace(/\\/g, '\\\\')
    .replace(/'/g, "''")
    .replace(/\r\n/g, '\n')
    .replace(/\n/g, '\\n')

const jsonArray = (items) => {
  const raw = JSON.stringify(items)
  return `'${esc(raw)}'`
}

// 确定性随机数,保证每次生成结果一致
function mulberry32(seed) {
  let state = seed
  return () => {
    state |= 0
    state = (state + 0x6d2b79f5) | 0
    let t = Math.imul(state ^ (state >>> 15), 1 | state)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}
const rand = mulberry32(20260817)
const pick = (list) => list[Math.floor(rand() * list.length)]

const out = []
const line = (text = '') => out.push(text)

// ---------------------------------------------------------------
// 用户(1 管理员 + 319 普通用户,共 320;前 40 个手工写实,其余按姓名库组合生成)
// ---------------------------------------------------------------
const userSeeds = [
  ['admin', '平台管理员', 'ADMIN', 'ACTIVE', 'CampusHub 平台运营同学,负责内容审核与系统维护。'],
  ['student', '林晚晴', 'USER', 'ACTIVE', '计算机学院大三,二手数码爱好者,常在图书馆出没。'],
  ['campus_003', '陈屿', 'USER', 'ACTIVE', '图书馆常驻选手,考研备考中,资料可互通有无。'],
  ['campus_004', '周子墨', 'USER', 'ACTIVE', '校辩论队成员,喜欢读书和桌游。'],
  ['campus_005', '苏以安', 'USER', 'ACTIVE', '摄影协会干事,相机常伴,周末去城郊扫街。'],
  ['campus_006', '江澈', 'USER', 'ACTIVE', '骑行爱好者,环湖路线熟得很。'],
  ['campus_007', '陆知夏', 'USER', 'ACTIVE', '设计学院,接插画单,常蹲二手绘图板。'],
  ['campus_008', '叶青梧', 'USER', 'ACTIVE', '中文系,爱逛旧书摊,宿舍书架快放不下了。'],
  ['campus_009', '许星辞', 'USER', 'ACTIVE', '电竞社社长,键盘鼠标配件换得很勤。'],
  ['campus_010', '温言', 'USER', 'ACTIVE', '外国语学院,雅思备考中,出过一批教材。'],
  ['campus_011', '顾北笙', 'USER', 'ACTIVE', '体育生,运动装备一大堆,毕业清仓。'],
  ['campus_012', '沈知行', 'USER', 'ACTIVE', '物理系,手工小达人,宿舍工具齐全。'],
  ['campus_013', '姜南星', 'USER', 'ACTIVE', '经管学院,喜欢露营和户外徒步。'],
  ['campus_014', '白鹭', 'USER', 'ACTIVE', '音乐学院,吉他社团主力,收过两把二手琴。'],
  ['campus_015', '程一诺', 'USER', 'ACTIVE', '新闻学院,实习常出差,经常出闲置。'],
  ['campus_016', '何以安', 'USER', 'ACTIVE', '医学院,作息规律,出过一批护考资料。'],
  ['campus_017', '唐疏影', 'USER', 'ACTIVE', '美术学院,画材消耗大户,经常回购颜料。'],
  ['campus_018', '谢晚舟', 'USER', 'ACTIVE', '法学院,备考法考,教材笔记成色很好。'],
  ['campus_019', '宋清欢', 'USER', 'ACTIVE', '学前教育,手工材料多,也出过玩偶。'],
  ['campus_020', '莫北', 'USER', 'ACTIVE', '土木学院,工地实习过,安全帽都留着。'],
  ['campus_021', '纪南絮', 'USER', 'ACTIVE', '生物系,养过绿植,宿舍小阳台绿意盎然。'],
  ['campus_022', '黎安歌', 'USER', 'ACTIVE', '计算机学院,攒机达人,配件更新快。'],
  ['campus_023', '傅清野', 'USER', 'ACTIVE', '航天学院,模型爱好者,出过航模。'],
  ['campus_024', '徐晚棠', 'USER', 'ACTIVE', '汉语言文学,收藏了全套古籍影印本。'],
  ['campus_025', '贺云舟', 'USER', 'ACTIVE', '自动化,智能小车社团,出过开发板。'],
  ['campus_026', '秦挽星', 'USER', 'ACTIVE', '化学学院,实验室常客,实验服都出过。'],
  ['campus_027', '陆拾壹', 'USER', 'ACTIVE', '数学系,学霸笔记全网求购,毕业整理笔记出。'],
  ['campus_028', '楚千凝', 'USER', 'ACTIVE', '服装设计,布料和缝纫机都在宿舍。'],
  ['campus_029', '季风眠', 'USER', 'ACTIVE', '地理系,户外装备齐全,帐篷睡袋都有。'],
  ['campus_030', '容晚', 'USER', 'ACTIVE', '历史系,喜欢逛博物馆,出过纪念品。'],
  ['campus_031', '温屿舟', 'USER', 'ACTIVE', '软件工程,显示器键盘外设都是好东西。'],
  ['campus_032', '云栖', 'USER', 'ACTIVE', '文学院,手账爱好者,出过整套文具。'],
  ['campus_033', '韩溯', 'USER', 'ACTIVE', '机械学院,动手能力max,宿舍工具箱全。'],
  ['campus_034', '沈星回', 'USER', 'ACTIVE', '艺术学院,钢琴十级,数码钢琴闲置中。'],
  ['campus_035', '顾清和', 'USER', 'ACTIVE', '会计学院,CPA 备考中,出过旧教材。'],
  ['campus_036', '林知意', 'USER', 'ACTIVE', '护理学院,出过一批练习器材。'],
  ['campus_037', '江晚吟', 'USER', 'ACTIVE', '表演系,礼服演出服几套闲置。'],
  ['campus_038', '许南乔', 'USER', 'ACTIVE', '金融学院,计算器是同学眼中的传说。'],
  ['campus_039', '叶之南', 'USER', 'ACTIVE', '园艺系,多肉小盆栽常年在宿舍窗台。'],
  ['campus_040', '陆离', 'USER', 'ACTIVE', '计算机学院,毕业季正在出四年家当。'],
]

const phonePool = [
  '138 2091 4775', '139 6642 0183', '150 3377 9024', '158 2241 6690',
  '176 9088 3312', '186 7712 0394', '188 9034 2216', '199 4187 5520',
  '137 5560 2841', '151 8832 7409', '159 4420 6197', '133 9266 1054',
  '180 1158 7432', '182 6674 2908', '135 2098 4471', '177 3316 8250',
  '181 7703 6924', '185 2460 9138', '136 8852 0714', '134 9906 3827',
]

const users = userSeeds.map(([username, nickname, role, status, bio], index) => {
  const id = index + 1
  return {
    id,
    username,
    nickname,
    role,
    // index 0 是唯一的管理员账号,不能参与「每 17 个禁用 1 个」的随机禁用
    status: status === 'DISABLED' ? 'DISABLED' : (index > 0 && index % 17 === 0 ? 'DISABLED' : 'ACTIVE'),
    bio,
    phone: id === 1 ? '139 0000 1024' : id === 2 ? '138 2091 4775' : phonePool[index % phonePool.length],
    daysAgo: 8 + ((index * 37) % 300),
  }
})

// 姓名库:组合生成大量写实的校园昵称(保证唯一)
const surnames = [
  '林', '陈', '周', '苏', '江', '陆', '叶', '许', '温', '顾',
  '沈', '姜', '白', '程', '何', '唐', '谢', '宋', '莫', '纪',
  '黎', '傅', '徐', '贺', '秦', '楚', '季', '容', '云', '韩',
  '方', '罗', '杜', '冯', '潘', '董', '袁', '于', '蒋', '彭',
]
const givenNames = [
  '晚晴', '屿', '子墨', '以安', '澈', '知夏', '青梧', '星辞', '言', '北笙',
  '知行', '南星', '鹭', '一诺', '以安', '疏影', '晚舟', '清欢', '北', '南絮',
  '安歌', '清野', '晚棠', '云舟', '挽星', '拾壹', '千凝', '风眠', '晚', '屿舟',
  '栖', '溯', '星回', '清和', '知意', '晚吟', '南乔', '之南', '离', '见山',
  '听澜', '望舒', '既白', '未名', '拾光', '栖迟', '行止', '知微', '晏然', '清越',
]
const bioPool = [
  '常在图书馆和操场出没,佛系逛校园。',
  '喜欢骑行和露营,周末常去郊区,装备齐全。',
  '毕业年级,正在整理四年家当,出闲置为主。',
  '摄影爱好者,作品常出现在校园展。',
  '二手电子产品淘换专业户,东西都保养得很好。',
  '备考党,教材笔记成色不错,可交换。',
  '社团活跃分子,活动消息灵通。',
  '安静型选手,宿舍宿舍两点一线。',
  '动手能力强,宿舍工具齐全,可代修小电器。',
  '音乐爱好者,宿舍常年有琴声。',
]
const generatedUsers = []
let gIndex = 0
for (let s = 0; s < surnames.length && generatedUsers.length < 280; s += 1) {
  for (let g = 0; g < givenNames.length && generatedUsers.length < 280; g += 1) {
    const nickname = surnames[s] + givenNames[g]
    if (users.some((u) => u.nickname === nickname) || generatedUsers.some((u) => u.nickname === nickname)) {
      continue
    }
    gIndex += 1
    const id = 40 + gIndex
    generatedUsers.push({
      id,
      username: `campus_${String(id).padStart(3, '0')}`,
      nickname,
      role: 'USER',
      status: gIndex % 53 === 0 ? 'DISABLED' : 'ACTIVE',
      bio: pick(bioPool),
      phone: `${pick(['138', '139', '150', '158', '176', '186', '188', '199'])} ${1000 + Math.floor(rand() * 9000)} ${1000 + Math.floor(rand() * 9000)}`,
      daysAgo: 8 + ((gIndex * 41) % 300),
    })
  }
}
users.push(...generatedUsers)

// ---------------------------------------------------------------
// 商品分类 / 商品
// ---------------------------------------------------------------
const productCategories = [
  [1, '数码电子', 1],
  [2, '图书教材', 2],
  [3, '服饰鞋包', 3],
  [4, '运动户外', 4],
  [5, '生活好物', 5],
  [6, '美妆个护', 6],
  [7, '其他', 7],
]

const productSeeds = [
  ['ikbc C87 红轴机械键盘 95 新', 1, 199, 259, '毕业出,自用两年,红轴手感舒服,键帽换了 PBT 侧刻,带原装数据线,可当面验货。', 'ON_SALE'],
  ['Sony WH-1000XM4 降噪耳机 国行在保', 1, 1299, 1999, '去年双十一入手,戴的少,成色接近全新,箱说齐全,保修到明年。', 'ON_SALE'],
  ['AirPods Pro 2 国行带 AC+', 1, 1399, 1899, '换了 Android 手机用不上,支持序列号验机,发票可提供。', 'ON_SALE'],
  ['罗技 G502 Hero 游戏鼠标', 1, 169, 299, '可调配重,手感好,打游戏和写代码都合适,功能完好。', 'ON_SALE'],
  ['富士拍立得 mini12 含 20 张相纸', 1, 449, 599, '拍了一阵子闲置了,相纸还有 3 盒一起出,校园活动合影神器。', 'ON_SALE'],
  ['极米 Z6X 智能投影仪', 1, 1880, 2899, '宿舍观影神器,1080P 物理分辨率,哈曼卡顿音响,无拆无修。', 'OFF_SHELF'],
  ['Nintendo Switch OLED 日版', 1, 1599, 2399, '研究生毕业出,吃灰已久,带底座、Joy-Con 和两张卡带。', 'ON_SALE'],
  ['小米手环 8 NFC 版', 1, 149, 249, '可以刷校园卡和公交,跑步计步精准,屏幕无划痕。', 'SOLD'],
  ['惠普 27 寸 2K 显示器', 1, 699, 1099, '考研上岸出,无坏点漏光,带 DP/HDMI 线。', 'ON_SALE'],
  ['雷蛇黑寡妇蜘蛛 V3 竞技版', 1, 289, 499, '绿轴段落感强,键帽无打油,宿舍打字建议换红轴。', 'ON_SALE'],
  ['考研数学复习全书 2027 版', 2, 45, 89, '上岸出书,只有零星笔记,附赠真题分类讲解电子版。', 'ON_SALE'],
  ['同济高数第七版 上下册', 2, 30, 68, '经典教材,书页无破损,封面微磨损,习题答案册一起送。', 'ON_SALE'],
  ['计算机网络教材(谢希仁版)', 2, 25, 55, '期末复习必备,划了重点,适合冲刺阶段参考。', 'ON_SALE'],
  ['雅思剑 16-18 真题+听力音频', 2, 60, 128, '考过出书,九成新,听力音频网盘链接同步赠送。', 'ON_SALE'],
  ['法考客观题精讲卷全套', 2, 88, 168, '去年过考,书基本全新,重点笔记都在。', 'SOLD'],
  ['考研英语黄皮书 2010-2026 真题', 2, 55, 108, '逐句精解版,笔迹很少,适合二轮刷题。', 'ON_SALE'],
  ['CPA 会计+财管教材与轻一', 2, 120, 240, '毕业出全套,轻一上有完整笔记,能省不少时间。', 'ON_SALE'],
  ['护理资格考试全套资料', 2, 40, 90, '含真题与知识点总结,压箱底资料打包出。', 'OFF_SHELF'],
  ['数据结构与算法(严蔚敏版)', 2, 20, 42, '经典教材,考研复试和面试复习都合适。', 'ON_SALE'],
  ['大学物理学习指导(上下册)', 2, 28, 60, '工科必买,例题解析非常全,适合期末突击。', 'ON_SALE'],
  ['森马连帽卫衣 全新带吊牌', 3, 89, 159, '买错尺码,全新未拆吊牌,藏青色 M 码。', 'ON_SALE'],
  ['北面冲锋衣 三合一可拆卸内胆', 3, 680, 1299, '冬季通勤上课都合适,防风防水,穿过一季。', 'ON_SALE'],
  ['复古帆布邮差包 牛皮', 3, 120, 220, '上课背电脑刚好,皮质越用越有味道,肩带可调。', 'ON_SALE'],
  ['优衣库摇粒绒外套 M 码', 3, 79, 149, '只穿过几次,洗过无污渍,奶白色很百搭。', 'ON_SALE'],
  ['匡威帆布鞋 42 码 高帮', 3, 99, 199, '穿了两季,鞋底磨损不大,刷干净了。', 'ON_SALE'],
  ['南极人羽绒服 男款 L 码', 3, 149, 329, '北方过冬够用,含绒量不错,出给有缘人。', 'OFF_SHELF'],
  ['双肩电脑包 17 寸 防泼水', 3, 85, 169, '毕业出,容量大,出差通勤都能背。', 'SOLD'],
  ['JBL 头戴式耳机 黑色', 3, 99, 249, '蓝牙有线双模,包耳舒服,自习党友好。', 'ON_SALE'],
  ['捷安特 ATX 山地车 24 速', 4, 850, 1399, '大一买的,刹车变速都调过,送车锁和头盔。', 'ON_SALE'],
  ['李宁雷霆 80 羽毛球拍', 4, 420, 720, '进攻型球拍,适合有点基础的球友,无伤无裂。', 'ON_SALE'],
  ['哑铃套装 20kg 可拆卸', 4, 180, 320, '宿舍健身够用,杠铃片防滑处理,闲置出自提。', 'ON_SALE'],
  ['露营帐篷+防潮垫套装', 4, 260, 480, '用过两次,无破损,适合 2-3 人,毕业清仓。', 'ON_SALE'],
  ['瑜伽垫 10mm 加厚防滑', 4, 45, 89, '健身房宿舍都能用,洗过晒过,无异味。', 'ON_SALE'],
  ['羽毛球拍 尤尼克斯天斧 99', 4, 560, 880, '挥速快,进攻利器,换过手胶,可小刀。', 'SOLD'],
  ['篮球 威尔胜 7 号 室内外通用', 4, 89, 169, '手感还在,室内外都打过,没鼓包。', 'ON_SALE'],
  ['椭圆机 家用静音款', 4, 699, 1299, '宿舍楼有氧神器,占地方所以出,功能正常。', 'OFF_SHELF'],
  ['小米手环充电器+表带若干', 4, 20, 45, '手环退役后留下的配件,需要可带走。', 'ON_SALE'],
  ['雅马哈 F310 民谣吉他', 5, 520, 899, '初学练手足够,面板无磕碰,弦距调过好按,送琴包变调夹。', 'ON_SALE'],
  ['宜家落地灯 暖光', 5, 75, 129, '宿舍床边看书用,亮度三档可调,自提优先。', 'ON_SALE'],
  ['迷你宿舍冰箱 45L', 5, 320, 599, '冷藏冷冻双温区,声音小功率低,毕业出自提。', 'ON_SALE'],
  ['尤克里里 23 寸 桃花心木', 5, 180, 320, '社团活动奖品,全新未使用,送调音器和包。', 'ON_SALE'],
  ['小米体脂秤 2 代', 5, 59, 99, '宿舍称重好帮手,蓝牙连接米家 App,电池还有电。', 'ON_SALE'],
  ['美的台灯 护眼无频闪', 5, 55, 99, '三档色温,陪伴了整个考研期,功能完好。', 'ON_SALE'],
  ['宿舍床上桌 可折叠', 5, 39, 79, '冬日起床困难户必备,桌面无划痕。', 'ON_SALE'],
  ['电煮锅 宿舍小功率', 5, 69, 129, '小功率不跳闸,煮面煮粥都行,锅体很干净。', 'OFF_SHELF'],
  ['兰蔻小黑瓶 50ml 全新', 6, 520, 850, '朋友从免税店带多了一瓶,全新未拆封,有效期到 2028。', 'ON_SALE'],
  ['SK-II 神仙水 230ml 余 8 成', 6, 780, 1280, '用了不到两成,肤质不合适出,可小刀。', 'ON_SALE'],
  ['戴森吹风机 HD08 国行', 6, 1580, 2499, '成色 95 新,包装盒和风嘴都在,自用两年。', 'ON_SALE'],
  ['雅诗兰黛小棕瓶 100ml', 6, 680, 1080, '朋友回国帮带,用了一周觉得不合适,余 9 成。', 'ON_SALE'],
  ['科颜氏高保湿面霜 125ml', 6, 199, 349, '秋冬干皮救星,开封两月,用了三分之一。', 'ON_SALE'],
  ['电动牙刷 飞利浦 HX6730', 6, 149, 399, '刷牙模式三种,配两支新刷头,已消毒。', 'SOLD'],
  ['电动自行车 学生代步 48V', 7, 1180, 1999, '校园通勤利器,电池可拆卸充电,有正规发票。', 'ON_SALE'],
  ['毕业季出:宿舍收纳全套', 7, 40, 89, '置物架、收纳箱、挂篮一整套,自提打包价。', 'ON_SALE'],
  ['Kindle Paperwhite 5', 7, 399, 698, '墨水屏看书不伤眼,带官方保护壳,屏幕无划痕。', 'ON_SALE'],
  ['三脚架 相机手机通用', 7, 89, 169, '摄影作业用,铝合金材质,很稳。', 'ON_SALE'],
  ['行李箱 24 寸 万向轮', 7, 129, 299, '毕业旅行用了几次,轮子顺滑,拉链完好。', 'ON_SALE'],
  ['路由器 小米 AX3000', 7, 99, 199, '宿舍网络神器,双千兆,发热小。', 'ON_SALE'],
  ['桌面增高架 带抽屉', 7, 35, 69, '显示器垫高,颈椎友好,桌面收纳好物。', 'ON_SALE'],
  ['宿舍小风扇 夹式充电款', 7, 45, 89, '停电和夏天午睡都用得上,静音,电量足。', 'ON_SALE'],
  ['电吉他 Squier Affinity', 7, 899, 1499, '乐队排练用,琴颈状态好,配音箱一起出。', 'OFF_SHELF'],
  ['自行车头盔 白色 M 码', 7, 49, 99, '骑行必备,只戴过几次,内衬洗过。', 'ON_SALE'],
]

// 主题图片映射(标题 -> 图片,与 campushub.sql 一致;缺失时回退 picsum)
// 由 backend/sql 的 campushub.sql 反查生成,新增数据时同步维护,避免重新生成时丢图
const THEME_IMAGES = {
  p: {
    'ikbc C87 红轴机械键盘 95 新': ['https://images.pexels.com/photos/5152261/pexels-photo-5152261.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/4792720/pexels-photo-4792720.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/9020272/pexels-photo-9020272.jpeg?auto=compress&cs=tinysrgb&w=800'],
    'Sony WH-1000XM4 降噪耳机 国行在保': ['https://images.pexels.com/photos/7772548/pexels-photo-7772548.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/3394650/pexels-photo-3394650.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5269726/pexels-photo-5269726.jpeg?auto=compress&cs=tinysrgb&w=800'],
    'AirPods Pro 2 国行带 AC+': ['https://images.pexels.com/photos/3921872/pexels-photo-3921872.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/3921846/pexels-photo-3921846.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/3921827/pexels-photo-3921827.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '罗技 G502 Hero 游戏鼠标': ['https://images.pexels.com/photos/29259392/pexels-photo-29259392.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/2115256/pexels-photo-2115256.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/1486294/pexels-photo-1486294.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '富士拍立得 mini12 含 20 张相纸': ['https://images.pexels.com/photos/10806074/pexels-photo-10806074.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/2438300/pexels-photo-2438300.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/4062180/pexels-photo-4062180.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '极米 Z6X 智能投影仪': ['https://images.pexels.com/photos/6062806/pexels-photo-6062806.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/16699711/pexels-photo-16699711.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/11092278/pexels-photo-11092278.jpeg?auto=compress&cs=tinysrgb&w=800'],
    'Nintendo Switch OLED 日版': ['https://images.pexels.com/photos/371924/pexels-photo-371924.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6993180/pexels-photo-6993180.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/4523029/pexels-photo-4523029.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '小米手环 8 NFC 版': ['https://images.pexels.com/photos/4429155/pexels-photo-4429155.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/374619/pexels-photo-374619.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5036927/pexels-photo-5036927.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '惠普 27 寸 2K 显示器': ['https://images.pexels.com/photos/12509206/pexels-photo-12509206.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8353774/pexels-photo-8353774.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/115655/pexels-photo-115655.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '雷蛇黑寡妇蜘蛛 V3 竞技版': ['https://images.pexels.com/photos/5380584/pexels-photo-5380584.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/841228/pexels-photo-841228.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/28993111/pexels-photo-28993111.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '考研数学复习全书 2027 版': ['https://images.pexels.com/photos/1438044/pexels-photo-1438044.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/23656142/pexels-photo-23656142.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/159866/books-book-pages-read-literature-159866.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '同济高数第七版 上下册': ['https://images.pexels.com/photos/9791429/pexels-photo-9791429.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/34923529/pexels-photo-34923529.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6958552/pexels-photo-6958552.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '计算机网络教材(谢希仁版)': ['https://images.pexels.com/photos/1181573/pexels-photo-1181573.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8534382/pexels-photo-8534382.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/34991783/pexels-photo-34991783.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '雅思剑 16-18 真题+听力音频': ['https://images.pexels.com/photos/11565595/pexels-photo-11565595.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5676740/pexels-photo-5676740.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/3248644/pexels-photo-3248644.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '法考客观题精讲卷全套': ['https://images.pexels.com/photos/8849335/pexels-photo-8849335.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8731037/pexels-photo-8731037.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6077296/pexels-photo-6077296.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '考研英语黄皮书 2010-2026 真题': ['https://images.pexels.com/photos/37778746/pexels-photo-37778746.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6929186/pexels-photo-6929186.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5386466/pexels-photo-5386466.jpeg?auto=compress&cs=tinysrgb&w=800'],
    'CPA 会计+财管教材与轻一': ['https://images.pexels.com/photos/8962469/pexels-photo-8962469.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8296970/pexels-photo-8296970.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8296998/pexels-photo-8296998.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '护理资格考试全套资料': ['https://images.pexels.com/photos/32115953/pexels-photo-32115953.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/37406585/pexels-photo-37406585.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/37407208/pexels-photo-37407208.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '数据结构与算法(严蔚敏版)': ['https://images.pexels.com/photos/1181281/pexels-photo-1181281.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/10826689/pexels-photo-10826689.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/1181568/pexels-photo-1181568.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '大学物理学习指导(上下册)': ['https://images.pexels.com/photos/6579277/pexels-photo-6579277.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/1430796/pexels-photo-1430796.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6344231/pexels-photo-6344231.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '森马连帽卫衣 全新带吊牌': ['https://images.pexels.com/photos/5840463/pexels-photo-5840463.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5319513/pexels-photo-5319513.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/11340657/pexels-photo-11340657.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '北面冲锋衣 三合一可拆卸内胆': ['https://images.pexels.com/photos/19736559/pexels-photo-19736559.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/11344358/pexels-photo-11344358.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/28758132/pexels-photo-28758132.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '复古帆布邮差包 牛皮': ['https://images.pexels.com/photos/36492563/pexels-photo-36492563.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/36492469/pexels-photo-36492469.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5950017/pexels-photo-5950017.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '优衣库摇粒绒外套 M 码': ['https://images.pexels.com/photos/19771863/pexels-photo-19771863.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/20103948/pexels-photo-20103948.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/17492503/pexels-photo-17492503.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '匡威帆布鞋 42 码 高帮': ['https://images.pexels.com/photos/27015417/pexels-photo-27015417.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/4271563/pexels-photo-4271563.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/4296075/pexels-photo-4296075.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '南极人羽绒服 男款 L 码': ['https://images.pexels.com/photos/8974263/pexels-photo-8974263.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8830576/pexels-photo-8830576.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/10786118/pexels-photo-10786118.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '双肩电脑包 17 寸 防泼水': ['https://images.pexels.com/photos/7054778/pexels-photo-7054778.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/18269634/pexels-photo-18269634.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/15059375/pexels-photo-15059375.jpeg?auto=compress&cs=tinysrgb&w=800'],
    'JBL 头戴式耳机 黑色': ['https://images.pexels.com/photos/210927/pexels-photo-210927.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8356854/pexels-photo-8356854.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/210926/pexels-photo-210926.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '捷安特 ATX 山地车 24 速': ['https://images.pexels.com/photos/36450314/pexels-photo-36450314.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/16082647/pexels-photo-16082647.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/37897835/pexels-photo-37897835.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '李宁雷霆 80 羽毛球拍': ['https://images.pexels.com/photos/12630113/pexels-photo-12630113.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8286363/pexels-photo-8286363.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6307231/pexels-photo-6307231.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '哑铃套装 20kg 可拆卸': ['https://images.pexels.com/photos/37887283/pexels-photo-37887283.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/669580/pexels-photo-669580.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/3931367/pexels-photo-3931367.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '露营帐篷+防潮垫套装': ['https://images.pexels.com/photos/33102150/pexels-photo-33102150.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/15925118/pexels-photo-15925118.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/4268094/pexels-photo-4268094.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '瑜伽垫 10mm 加厚防滑': ['https://images.pexels.com/photos/8539005/pexels-photo-8539005.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/28821063/pexels-photo-28821063.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8436578/pexels-photo-8436578.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '羽毛球拍 尤尼克斯天斧 99': ['https://images.pexels.com/photos/8007419/pexels-photo-8007419.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6878017/pexels-photo-6878017.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8007173/pexels-photo-8007173.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '篮球 威尔胜 7 号 室内外通用': ['https://images.pexels.com/photos/12954255/pexels-photo-12954255.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6076409/pexels-photo-6076409.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/1462618/pexels-photo-1462618.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '椭圆机 家用静音款': ['https://images.pexels.com/photos/47084/gym-exercise-fitness-workout-47084.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6285198/pexels-photo-6285198.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6285181/pexels-photo-6285181.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '小米手环充电器+表带若干': ['https://images.pexels.com/photos/18662969/pexels-photo-18662969.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5237704/pexels-photo-5237704.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8217438/pexels-photo-8217438.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '雅马哈 F310 民谣吉他': ['https://images.pexels.com/photos/7558112/pexels-photo-7558112.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/4688751/pexels-photo-4688751.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/9057766/pexels-photo-9057766.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '宜家落地灯 暖光': ['https://images.pexels.com/photos/6078545/pexels-photo-6078545.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/12648787/pexels-photo-12648787.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/18873546/pexels-photo-18873546.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '迷你宿舍冰箱 45L': ['https://images.pexels.com/photos/37637045/pexels-photo-37637045.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/34872425/pexels-photo-34872425.png?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/34872428/pexels-photo-34872428.png?auto=compress&cs=tinysrgb&w=800'],
    '尤克里里 23 寸 桃花心木': ['https://images.pexels.com/photos/6564512/pexels-photo-6564512.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6564513/pexels-photo-6564513.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5586494/pexels-photo-5586494.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '小米体脂秤 2 代': ['https://images.pexels.com/photos/6975463/pexels-photo-6975463.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/53404/scale-diet-fat-health-53404.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/29422934/pexels-photo-29422934.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '美的台灯 护眼无频闪': ['https://images.pexels.com/photos/7439756/pexels-photo-7439756.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/32543097/pexels-photo-32543097.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/28461166/pexels-photo-28461166.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '宿舍床上桌 可折叠': ['https://images.pexels.com/photos/31421652/pexels-photo-31421652.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/18267776/pexels-photo-18267776.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/11502135/pexels-photo-11502135.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '电煮锅 宿舍小功率': ['https://images.pexels.com/photos/29479438/pexels-photo-29479438.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/29479440/pexels-photo-29479440.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/30915727/pexels-photo-30915727.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '兰蔻小黑瓶 50ml 全新': ['https://images.pexels.com/photos/35899861/pexels-photo-35899861.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8054400/pexels-photo-8054400.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8100691/pexels-photo-8100691.jpeg?auto=compress&cs=tinysrgb&w=800'],
    'SK-II 神仙水 230ml 余 8 成': ['https://images.pexels.com/photos/4735904/pexels-photo-4735904.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5632335/pexels-photo-5632335.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/27544680/pexels-photo-27544680.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '戴森吹风机 HD08 国行': ['https://images.pexels.com/photos/32641724/pexels-photo-32641724.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/10892870/pexels-photo-10892870.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/3993328/pexels-photo-3993328.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '雅诗兰黛小棕瓶 100ml': ['https://images.pexels.com/photos/8101512/pexels-photo-8101512.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/7670680/pexels-photo-7670680.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8167164/pexels-photo-8167164.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '科颜氏高保湿面霜 125ml': ['https://images.pexels.com/photos/27544670/pexels-photo-27544670.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6690232/pexels-photo-6690232.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/29652923/pexels-photo-29652923.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '电动牙刷 飞利浦 HX6730': ['https://images.pexels.com/photos/33215341/pexels-photo-33215341.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/4045552/pexels-photo-4045552.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/28407751/pexels-photo-28407751.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '电动自行车 学生代步 48V': ['https://images.pexels.com/photos/16435192/pexels-photo-16435192.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/15884318/pexels-photo-15884318.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8811401/pexels-photo-8811401.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '毕业季出:宿舍收纳全套': ['https://images.pexels.com/photos/7309200/pexels-photo-7309200.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/10938208/pexels-photo-10938208.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/3912293/pexels-photo-3912293.jpeg?auto=compress&cs=tinysrgb&w=800'],
    'Kindle Paperwhite 5': ['https://images.pexels.com/photos/1329571/pexels-photo-1329571.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/1475296/pexels-photo-1475296.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/844734/pexels-photo-844734.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '三脚架 相机手机通用': ['https://images.pexels.com/photos/11748183/pexels-photo-11748183.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/32717685/pexels-photo-32717685.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '行李箱 24 寸 万向轮': ['https://images.pexels.com/photos/34629933/pexels-photo-34629933.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5705068/pexels-photo-5705068.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/36933446/pexels-photo-36933446.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '路由器 小米 AX3000': ['https://images.pexels.com/photos/4218546/pexels-photo-4218546.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/29711663/pexels-photo-29711663.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/18071864/pexels-photo-18071864.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '桌面增高架 带抽屉': ['https://images.pexels.com/photos/34502055/pexels-photo-34502055.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/4792712/pexels-photo-4792712.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8316281/pexels-photo-8316281.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '宿舍小风扇 夹式充电款': ['https://images.pexels.com/photos/3675622/pexels-photo-3675622.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5850340/pexels-photo-5850340.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/4389919/pexels-photo-4389919.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '电吉他 Squier Affinity': ['https://images.pexels.com/photos/194011/pexels-photo-194011.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/12393614/pexels-photo-12393614.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/2049414/pexels-photo-2049414.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '自行车头盔 白色 M 码': ['https://images.pexels.com/photos/12956080/pexels-photo-12956080.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/37625583/pexels-photo-37625583.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/13673124/pexels-photo-13673124.jpeg?auto=compress&cs=tinysrgb&w=800'],
  },
  a: {
    '校运会 5 公里欢乐跑': 'https://images.pexels.com/photos/32327116/pexels-photo-32327116.jpeg?auto=compress&cs=tinysrgb&w=800',
    '周末读书会:当我们谈论校园时': 'https://images.pexels.com/photos/32751996/pexels-photo-32751996.jpeg?auto=compress&cs=tinysrgb&w=800',
    '秋季校园二手市集': 'https://images.pexels.com/photos/19152576/pexels-photo-19152576.jpeg?auto=compress&cs=tinysrgb&w=800',
    'AI 大模型入门分享会': 'https://images.pexels.com/photos/34774347/pexels-photo-34774347.jpeg?auto=compress&cs=tinysrgb&w=800',
    '新生破冰定向越野': 'https://images.pexels.com/photos/4314209/pexels-photo-4314209.jpeg?auto=compress&cs=tinysrgb&w=800',
    '校园摄影展作品征集': 'https://images.pexels.com/photos/5466870/pexels-photo-5466870.jpeg?auto=compress&cs=tinysrgb&w=800',
    '编程马拉松 HackNight 36h': 'https://images.pexels.com/photos/8128190/pexels-photo-8128190.jpeg?auto=compress&cs=tinysrgb&w=800',
    '吉他社草地音乐节': 'https://images.pexels.com/photos/16803238/pexels-photo-16803238.jpeg?auto=compress&cs=tinysrgb&w=800',
    '考研经验分享会(上岸学长学姐团)': 'https://images.pexels.com/photos/37811241/pexels-photo-37811241.jpeg?auto=compress&cs=tinysrgb&w=800',
    '校园公益旧衣回收': 'https://images.pexels.com/photos/6994940/pexels-photo-6994940.jpeg?auto=compress&cs=tinysrgb&w=800',
    '校辩论队表演赛:人工智能应否进入课堂': 'https://images.pexels.com/photos/17558082/pexels-photo-17558082.jpeg?auto=compress&cs=tinysrgb&w=800',
    '新生杯篮球联赛揭幕战': 'https://images.pexels.com/photos/32600343/pexels-photo-32600343.jpeg?auto=compress&cs=tinysrgb&w=800',
    '英语角:Travel Around the World': 'https://images.pexels.com/photos/7972561/pexels-photo-7972561.jpeg?auto=compress&cs=tinysrgb&w=800',
    '手作工作坊:皮革卡包 DIY': 'https://images.pexels.com/photos/4452508/pexels-photo-4452508.jpeg?auto=compress&cs=tinysrgb&w=800',
    '校园歌手大赛海选': 'https://images.pexels.com/photos/16108227/pexels-photo-16108227.jpeg?auto=compress&cs=tinysrgb&w=800',
    '急救知识培训(CPR+AED)': 'https://images.pexels.com/photos/33317471/pexels-photo-33317471.jpeg?auto=compress&cs=tinysrgb&w=800',
    '秋日定向越野挑战赛': 'https://images.pexels.com/photos/14137287/pexels-photo-14137287.jpeg?auto=compress&cs=tinysrgb&w=800',
    '职业生涯规划讲座:简历与面试': 'https://images.pexels.com/photos/8761330/pexels-photo-8761330.jpeg?auto=compress&cs=tinysrgb&w=800',
    '汉服游园会': 'https://images.pexels.com/photos/37664482/pexels-photo-37664482.jpeg?auto=compress&cs=tinysrgb&w=800',
    '深夜自习室打卡挑战': 'https://images.pexels.com/photos/9572540/pexels-photo-9572540.jpeg?auto=compress&cs=tinysrgb&w=800',
  },
  lf: {
    '拾到 AirPods 耳机盒(东操场)': ['https://images.pexels.com/photos/3921887/pexels-photo-3921887.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/3921831/pexels-photo-3921831.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/7424282/pexels-photo-7424282.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '拾到学生卡一张(姓名:陈屿)': ['https://images.pexels.com/photos/7108127/pexels-photo-7108127.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/7108126/pexels-photo-7108126.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '拾到钥匙串(带蓝色挂件)': ['https://images.pexels.com/photos/1194027/pexels-photo-1194027.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/109361/pexels-photo-109361.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/16329644/pexels-photo-16329644.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '拾到校园卡(工商银行卡联名)': ['https://images.pexels.com/photos/50987/money-card-business-credit-card-50987.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5717969/pexels-photo-5717969.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6214152/pexels-photo-6214152.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '拾到米色围巾一条': ['https://images.pexels.com/photos/5710117/pexels-photo-5710117.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5603205/pexels-photo-5603205.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/6045254/pexels-photo-6045254.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '拾到复习资料一摞(考研数学)': ['https://images.pexels.com/photos/9291615/pexels-photo-9291615.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/7034647/pexels-photo-7034647.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/5009160/pexels-photo-5009160.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '拾到校园卡(尾号 6619)': ['https://images.pexels.com/photos/7821483/pexels-photo-7821483.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/583881/pexels-photo-583881.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/7821730/pexels-photo-7821730.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '拾到充电宝(白色 20000mAh)': ['https://images.pexels.com/photos/34338614/pexels-photo-34338614.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/3921704/pexels-photo-3921704.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8137310/pexels-photo-8137310.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '拾到羽毛球拍(黑色拍套)': ['https://images.pexels.com/photos/6307231/pexels-photo-6307231.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/8286363/pexels-photo-8286363.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/9804653/pexels-photo-9804653.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '拾到课本《管理学原理》': ['https://images.pexels.com/photos/32046500/pexels-photo-32046500.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/29171841/pexels-photo-29171841.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/240163/pexels-photo-240163.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '拾到相机存储卡一张': ['https://images.pexels.com/photos/30758131/pexels-photo-30758131.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/18166725/pexels-photo-18166725.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/193057/pexels-photo-193057.jpeg?auto=compress&cs=tinysrgb&w=800'],
    '拾到近视眼镜一副(黑色框)': ['https://images.pexels.com/photos/32398468/pexels-photo-32398468.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/9328548/pexels-photo-9328548.jpeg?auto=compress&cs=tinysrgb&w=800', 'https://images.pexels.com/photos/13430471/pexels-photo-13430471.jpeg?auto=compress&cs=tinysrgb&w=800'],
  },
}

const products = productSeeds.map(([title, categoryId, price, originalPrice, description, status], index) => {
  const id = index + 1
  const sellerId = 2 + ((index * 13) % 39)
  return {
    id,
    title,
    categoryId,
    price,
    originalPrice,
    description,
    status,
    sellerId,
    favoriteCount: 1 + Math.floor(rand() * 60),
    viewCount: 15 + Math.floor(rand() * 900),
    daysAgo: Math.floor(rand() * 60),
    images:
      THEME_IMAGES.p[title] || [
        `https://picsum.photos/seed/campus-p${id}-1/800/600`,
        `https://picsum.photos/seed/campus-p${id}-2/800/600`,
        `https://picsum.photos/seed/campus-p${id}-3/800/600`,
      ],
  }
})

// ---------------------------------------------------------------
// 收藏(user_id 2..40 x product_id 1..60,45 条不重复)
// ---------------------------------------------------------------
const favorites = []
const favSeen = new Set()
while (favorites.length < 45) {
  const userId = 2 + Math.floor(rand() * 39)
  const productId = 1 + Math.floor(rand() * 60)
  const key = `${userId}-${productId}`
  if (!favSeen.has(key)) {
    favSeen.add(key)
    favorites.push([userId, productId])
  }
}

// ---------------------------------------------------------------
// 活动分类 / 活动 / 报名
// ---------------------------------------------------------------
const activityCategories = [
  [1, '讲座分享', 1],
  [2, '体育竞技', 2],
  [3, '社团活动', 3],
  [4, '志愿服务', 4],
  [5, '文娱演出', 5],
  [6, '其他', 6],
]

const activitySeeds = [
  ['校运会 5 公里欢乐跑', 2, '东区操场', '一年一度的校园欢乐跑,不设成绩门槛,完赛即得纪念奖牌与第二课堂学分。路线途经图书馆、银杏大道与湖畔。', 3, 200, 156, '校团委·学生会'],
  ['周末读书会:当我们谈论校园时', 1, '图书馆一楼报告厅', '本期共读《你当像鸟飞往你的山》,由文学院老师领读。现场提供茶水,带自己的书即可参加。', 6, 60, 38, '图书馆·文学院'],
  ['秋季校园二手市集', 3, '学生活动中心广场', '毕业季版二手市集回归!报名成为摊主处理闲置好物,逛集的同学还能参与抽奖赢取文创周边。', 10, 150, 103, '校团委·社联'],
  ['AI 大模型入门分享会', 1, '教三 402', '计算机学院学长带你从零认识大模型:原理、Prompt 技巧、本地部署实战,零基础友好。', 1, 80, 66, '计算机学院科协'],
  ['新生破冰定向越野', 4, '校园全域', '分组完成校园地标打卡任务,赢积分换礼品。认识新朋友的第一站,欢迎所有年级参加。', 14, 120, 66, '校学生会'],
  ['校园摄影展作品征集', 5, '线上投稿+图书馆展出', '以「我眼里的四季校园」为主题征集摄影作品,入选作品将在图书馆走廊展出并收录进校园年鉴。', 4, 100, 47, '摄影协会'],
  ['编程马拉松 HackNight 36h', 6, '创新创业学院 3F', '36 小时极限开发,主题现场揭晓。提供场地、夜宵与导师辅导,优秀团队直通校级竞赛。', 20, 60, 41, '创新创业学院'],
  ['吉他社草地音乐节', 5, '湖畔草坪', '夏夜草坪音乐会,社团乐队与个人弹唱轮番登场,观众可自由点歌,现场提供荧光棒。', 6, 300, 212, '吉他社'],
  ['考研经验分享会(上岸学长学姐团)', 1, '行政楼 201', '数学、英语、专业课三场分论坛并行,上岸学长学姐面对面答疑,会后资料包分享。', -9, 200, 200, '校团委·学习部'],
  ['校园公益旧衣回收', 4, '各宿舍楼下', '与市慈善总会合作的旧衣回收活动,每回收 1kg 将配捐 1 元至山区图书角项目。', -2, 500, 318, '青年志愿者协会'],
  ['校辩论队表演赛:人工智能应否进入课堂', 1, '学生活动中心报告厅', '正反双方各 20 分钟立论与质询,现场观众可参与自由辩论环节。', -1, 260, 260, '校辩论队'],
  ['新生杯篮球联赛揭幕战', 2, '东区体育馆', '十二支学院队伍抽签对阵,揭幕战由计算机学院对阵经济学院,欢迎到场助威。', 2, 400, 122, '体育部'],
  ['英语角:Travel Around the World', 1, '外语楼 301', '外教主持,每期一个国家主题,本期聊聊东南亚背包旅行。口语薄弱也能大胆开口。', 5, 50, 31, '外国语学院'],
  ['手作工作坊:皮革卡包 DIY', 3, '创新创业学院 1F', '两小时做出自己的卡包,材料费 15 元,成品带走。限 30 人,手慢无。', 8, 30, 19, '手作社'],
  ['校园歌手大赛海选', 5, '学生活动中心 K 吧', '一年一度的校园歌手大赛开始报名!海选清唱 90 秒,晋级即进复赛。', 12, 150, 88, '学生会文艺部'],
  ['急救知识培训(CPR+AED)', 4, '校医院 3 楼教室', '红十字会认证讲师授课,通过考核可获急救员证书,适合有支教和志愿计划的同学。', 16, 40, 28, '红十字协会'],
  ['秋日定向越野挑战赛', 2, '西区山地公园', '双人组队,山地+公园混合赛道,完赛率最高的前三组有奖。', 22, 90, 52, '户外运动协会'],
  ['职业生涯规划讲座:简历与面试', 1, '就业指导中心 210', '资深 HR 现场改简历,模拟面试环节,应届生必听。', 26, 120, 74, '就业指导中心'],
  ['汉服游园会', 5, '中心花园', '汉服走秀+传统游戏+手工集市,穿汉服入场可领纪念书签。', 30, 200, 135, '汉服社'],
  ['深夜自习室打卡挑战', 6, '图书馆 24 小时自习区', '连续 7 天晚 10 点后打卡满 5 次即可获得图书馆文创奖品,一起卷起来。', 0, 300, 96, '图书馆'],
]

const activities = activitySeeds.map((seed, index) => {
  const [title, categoryId, location, description, startDays, max, current, organizer] = seed
  const id = index + 1
  const startHours = startDays * 24 + Math.floor(rand() * 8)
  const durationHours = 2 + Math.floor(rand() * 7)
  const status = startHours < -durationHours ? 'FINISHED' : startHours <= 0 ? 'ONGOING' : 'UPCOMING'
  const participants = Math.min(current, max)
  return {
    id,
    title,
    categoryId,
    location,
    description,
    organizer,
    status,
    startHours,
    durationHours,
    max,
    participants,
  }
})

// 报名记录:按活动参与人数取 user 2..N+1(用户池 319 人,保证唯一)
const registrations = []
activities.forEach((activity) => {
  for (let i = 0; i < activity.participants; i += 1) {
    const userId = 2 + (i % (users.length - 1))
    registrations.push([activity.id, userId])
  }
})

// ---------------------------------------------------------------
// 公告
// ---------------------------------------------------------------
const announcements = [
  ['关于校园网出口带宽升级的通知', '后勤服务', '信息化中心', 1204, 1, '各位师生:\n\n为改善校园网络高峰期体验,信息化中心将于本周六 0:00-6:00 对校园网出口带宽进行升级,期间校园网可能出现短暂中断,请提前安排好学习与工作。\n\n升级完成后,校园网上下行带宽将在现有基础上提升 50%。\n\n信息化中心'],
  ['2026 秋季学期选课安排', '教务信息', '教务处', 3421, 2, '各位同学:\n\n2026 秋季学期选课共分三轮:\n\n第一轮(预选):8 月 20 日 10:00 - 8 月 22 日 22:00\n第二轮(正选):8 月 26 日 10:00 - 8 月 28 日 22:00\n第三轮(补退选):9 月 6 日 10:00 - 9 月 10 日 22:00\n\n请同学们及时登录教务系统查看培养方案,合理规划课程。'],
  ['图书馆暑期开放时间调整', '后勤服务', '图书馆', 876, 4, '各位读者:\n\n暑期图书馆开放时间为每日 8:30-21:30,其中二层自习区 7:30 开放。8 月 20 日起恢复常规开放时间。\n\n三楼教师阅览室暑期暂停开放,四楼电子阅览室正常开放。'],
  ['校园卡补办流程优化上线', '后勤服务', '后勤服务中心', 653, 6, '为减少排队,校园卡挂失与补办已全面支持线上办理。登录校园卡服务平台即可自助申请,补办成功后短信通知领取地点。\n\n首次补办免费,再次补办收取工本费 20 元。'],
  ['校医院疫苗接种安排(九价 HPV 第二轮)', '通知公告', '校医院', 2910, 7, '各位同学:\n\n校医院第二轮九价 HPV 疫苗预约将于 8 月 15 日 12:00 开放,共 300 个名额。预约成功后按短信通知时间接种。\n\n接种请携带身份证与校园卡。'],
  ['第二课堂学分认定办法更新', '教务信息', '校团委', 1877, 9, '新版《第二课堂学分认定办法》已经校务会审议通过,自 2026 秋季学期起执行。主要变化:志愿服务学分上限提高,竞赛获奖可折算双倍学分。\n\n详细条款见附件。'],
  ['电动车充电桩新增点位启用', '后勤服务', '后勤保障部', 998, 11, '为满足电动车充电需求,后勤保障部在东区宿舍 7 号楼、西区教学楼地下停车场新增充电桩 40 个,现已投入使用。\n\n充电费用按用电量计费,支持校园卡与微信扫码支付。'],
  ['学生宿舍热水供应时间调整', '后勤服务', '后勤服务中心', 1245, 14, '自 9 月起,宿舍热水供应时间调整为每日 6:30-8:30 与 17:00-24:00。暑期非整点时段为 17:00-23:00。\n\n如遇供水异常,请拨打宿舍服务热线 8225 0000。'],
  ['校园文创纪念品上新', '校园活动', '校史馆', 534, 16, '校史馆文创店上新:银杏书签、校门积木、四季明信片等 12 款新品,开学季全场 88 折。毕业季限定帆布袋同步发售。'],
  ['关于开展 2026 年度学生医保参保的通知', '通知公告', '校医院', 1620, 18, '各位同学:\n\n2026 年度学生医保参保工作已启动,集中办理时间为 8 月 25 日至 9 月 15 日。请按学院通知在校园卡服务平台完成参保登记与缴费。\n\n低保家庭学生可申请减免,具体请咨询学院辅导员。'],
  ['图书馆新增 24 小时自助还书机', '后勤服务', '图书馆', 742, 21, '为方便同学随时还书,图书馆在正门东侧新增 24 小时自助还书机,支持扫描枪快速归还,欢迎使用。'],
  ['关于规范校园电动自行车停放的通知', '通知公告', '保卫处', 1105, 25, '为保障消防通道畅通,即日起电动车须停放在划定区域并上锁,严禁进楼充电。违者将按《校园安全管理规定》处理。'],
  ['创新创业训练营招募(30 个名额)', '校园活动', '创新创业学院', 986, 28, '为期四周的创新创业训练营开始招募,涵盖商业模式、路演技巧与融资基础,结营项目可入驻孵化基地。\n\n报名方式:登录第二课堂系统搜索「训练营」报名。'],
  ['食堂二楼窗口调整公告', '后勤服务', '后勤服务中心', 689, 30, '为提升就餐体验,食堂二楼部分窗口于 9 月 1 日起调整:新增麻辣香锅窗口,清真窗口迁至一楼东侧。'],
  ['关于国家奖学金申请的通知', '教务信息', '学生资助管理中心', 2308, 33, '2026 年国家奖学金申请通道已开放,截止时间为 9 月 20 日。符合条件的同学请登录学工系统提交申请,并按要求准备证明材料。'],
]

// ---------------------------------------------------------------
// 失物招领
// ---------------------------------------------------------------
const lostFoundSeeds = [
  ['LOST', '黑色钱包(内含校园卡)遗失在图书馆三楼', '图书馆三楼中文期刊区', '黑色短款钱包,里面有校园卡、身份证和少量现金。有捡到的同学请联系我,感谢!', '电话:138 2091 4775 · 图书馆前台可代收', 'OPEN', 2],
  ['FOUND', '拾到 AirPods 耳机盒(东操场)', '东区操场看台', '周二晚在东操场看台拾到 AirPods 耳机盒,已在失物招领处登记。请描述盒内刻字以认领。', '失物招领处(学生活动中心 101)', 'OPEN', 5],
  ['LOST', '蓝白条纹雨伞遗失在教三', '教三 201', '蓝色白条纹折叠伞,伞柄有「C」字挂坠,下午 3 点落在教室后排。', 'QQ:841027563', 'OPEN', 8],
  ['FOUND', '拾到学生卡一张(姓名:陈屿)', '食堂二楼', '在食堂二楼靠窗位置拾到学生卡,已交给一卡通服务中心。', '一卡通服务中心(东门旁)', 'RESOLVED', 10],
  ['LOST', '灰色书包遗落在快递站', '菜鸟驿站', '灰色双肩包,里面有教材和 iPad 保护套。监控显示下午 5 点后还在。', '电话:186 7712 0394', 'OPEN', 12],
  ['FOUND', '拾到钥匙串(带蓝色挂件)', '湖畔草坪', '一串钥匙,带蓝色星球挂件和门禁卡套。请描述钥匙数量以认领。', '湖畔巡逻岗亭', 'OPEN', 14],
  ['LOST', '银色保温杯遗失在食堂二楼', '食堂二楼', '银色 500ml 保温杯,杯身贴了「明天也要加油」贴纸。', '电话:150 6634 8821', 'OPEN', 16],
  ['FOUND', '拾到校园卡(工商银行卡联名)', '教学楼 B 栋电梯口', '工商银行卡联名校园卡,卡号尾号 1024,已交至保卫处。', '保卫处值班室(行政楼 110)', 'RESOLVED', 18],
  ['LOST', '黑色单反相机包(内含镜头)', '校史馆门口', '黑色相机包,内有佳能套机镜头。摄影课作业需要,捡到的同学麻烦联系,必有酬谢。', '电话:188 9034 2216', 'OPEN', 20],
  ['FOUND', '拾到米色围巾一条', '图书馆四楼电子阅览室', '米色针织围巾,挂在椅背上,已交给前台保管。', '图书馆四楼前台', 'OPEN', 21],
  ['LOST', '白色蓝牙耳机(右耳)遗失在东操场', '东区操场', '右耳单独遗失,白色入耳式,耳机仓还在。拾到请私聊,感谢!', 'QQ:772041865', 'OPEN', 22],
  ['FOUND', '拾到复习资料一摞(考研数学)', '教一 105', '一摞考研数学复习资料,封面写着「上岸!」,已交至教一楼管室。', '教一楼管室', 'OPEN', 24],
  ['LOST', '银灰色拉杆箱遗落在校门口', '学校东门', '银灰色 20 寸拉杆箱,箱面有贴纸。下午三点半下车忘拿,很重要!', '电话:176 9088 3312', 'OPEN', 25],
  ['FOUND', '拾到校园卡(尾号 6619)', '西区食堂', '在收餐台附近拾到校园卡一张,姓名拼音缩写 LWC。', '西区食堂服务台', 'RESOLVED', 27],
  ['LOST', '黑色皮质手套遗失在行政楼', '行政楼 2 层', '左手一只黑色皮手套,开会时落的,有捡到的同学麻烦联系。', '电话:151 8832 7409', 'OPEN', 28],
  ['FOUND', '拾到充电宝(白色 20000mAh)', '校车站', '白色充电宝,带一根数据线,已交校车站值班室。', '校车站值班室', 'OPEN', 30],
  ['LOST', 'kindle 阅读器遗失在图书馆', '图书馆五楼自习区', '黑色 Kindle,带蓝色官方壳,屏幕贴了磨砂膜。自习时放在桌上忘了拿。', '电话:159 4420 6197', 'OPEN', 32],
  ['FOUND', '拾到羽毛球拍(黑色拍套)', '体育馆羽毛球馆', '黑色拍套,里面一支球拍,球拍柄缠了白色手胶。', '体育馆前台', 'OPEN', 34],
  ['LOST', '学士服(借用后未归还)', '大学生活动中心', '毕业典礼借用的学士服,归还时拿错了,蓝色 S 码。请拿错的同学联系我换回来。', 'QQ:663102947', 'OPEN', 36],
  ['FOUND', '拾到课本《管理学原理》', '教二 306', '书里夹着一张手写笔记,应该是不小心落在抽屉里的。', '教二 306 讲台', 'RESOLVED', 38],
  ['LOST', '绿色保温杯遗失在操场看台', '东区操场看台', '绿色 350ml 保温杯,杯盖有划痕,经常用来接热水。', '电话:133 9266 1054', 'OPEN', 40],
  ['FOUND', '拾到相机存储卡一张', '中心花园长椅', 'SD 卡一张,里面应该是摄影作业,已交摄影协会保管。', '摄影协会(学生活动中心 302)', 'OPEN', 42],
  ['LOST', '自行车钥匙串遗失在车棚', '东区宿舍 7 号楼车棚', '一把自行车钥匙+门禁卡,蓝色硅胶套。', 'QQ:992107846', 'OPEN', 44],
  ['FOUND', '拾到近视眼镜一副(黑色框)', '教四 201', '黑色方框眼镜,度数大概 300 度,已交失物招领处。', '失物招领处', 'OPEN', 46],
  ['LOST', '校园卡(卡套是皮卡丘)', '全校范围', '校园卡掉了,卡套是黄色皮卡丘,补办要 20 块,捡到的同学拜托了!', '电话:180 1158 7432', 'OPEN', 48],
]

// ---------------------------------------------------------------
// 通知
// ---------------------------------------------------------------
const notificationTemplates = [
  ['ACTIVITY', '报名成功:{activity}', '你已成功报名「{activity}」,请提前 30 分钟到场签到。'],
  ['MARKET', '你发布的商品被收藏', '商品「{product}」新增收藏,当前共 {count} 人收藏。'],
  ['SYSTEM', 'CampusHub 服务升级公告', '平台将于本周六 02:00-04:00 进行例行维护,期间部分功能可能短暂不可用。'],
  ['LOST_FOUND', '你发布的失物有新进展', '「{title}」的状态有更新,点击查看详情。'],
  ['MARKET', '商品降价提醒', '你关注的「{product}」降价至 ¥{price},比收藏时低 ¥100。'],
  ['ACTIVITY', '活动提醒:{activity}', '「{activity}」即将开始,记得提前到场。'],
  ['SYSTEM', '账号安全提醒', '检测到你的账号在新设备登录,如非本人操作请及时修改密码。'],
  ['LOST_FOUND', '失物招领动态', '「{title}」有新评论,点击查看详情。'],
]

const activityTitles = activities.map((a) => a.title)
const productTitles = products.map((p) => p.title)

const notifications = Array.from({ length: 40 }, (_, index) => {
  const [type, titleTpl, contentTpl] = notificationTemplates[index % notificationTemplates.length]
  const userId = 2 + ((index * 7) % 39)
  const title = titleTpl
    .replace('{activity}', pick(activityTitles))
    .replace('{product}', pick(productTitles))
    .replace('{title}', pick(lostFoundSeeds.map((s) => s[1])))
  const content = contentTpl
    .replace('{activity}', pick(activityTitles))
    .replace('{product}', pick(productTitles))
    .replace('{count}', String(5 + Math.floor(rand() * 60)))
    .replace('{price}', String(50 + Math.floor(rand() * 1500)))
    .replace('{title}', pick(lostFoundSeeds.map((s) => s[1])))
  return {
    userId,
    type,
    title,
    content,
    read: index % 4 === 0 ? 0 : 1,
    hoursAgo: index * 6 + 1,
  }
})

// ---------------------------------------------------------------
// SQL 组装
// ---------------------------------------------------------------
line('-- ============================================================')
line('-- CampusHub 校园综合服务平台 · 初始化脚本(建库 + 建表 + 种子数据)')
line('-- 生成时间:2026-08-17(日期字段相对 NOW() 动态生成)')
line('-- MySQL 8.0+ / utf8mb4')
line('--')
line('-- 执行方式:')
line('--   mysql -uroot -p < campushub.sql')
line('--   或登录 MySQL 后执行 source campushub.sql;')
line('--')
line('-- 演示账号(密码均为 123456,BCrypt 加密):')
line('--   管理员:admin / 123456')
line('--   普通用户:student / 123456')
line('-- ============================================================')
line()
line('SET NAMES utf8mb4;')
line('SET FOREIGN_KEY_CHECKS = 0;')
line()
line('DROP DATABASE IF EXISTS `campushub`;')
line('CREATE DATABASE IF NOT EXISTS `campushub`')
line('  DEFAULT CHARACTER SET utf8mb4')
line('  COLLATE utf8mb4_unicode_ci;')
line('USE `campushub`;')
line()

line('-- ------------------------------------------------------------')
line('-- 1. 用户表')
line('-- ------------------------------------------------------------')
line(`CREATE TABLE \`campus_user\` (
  \`id\` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  \`username\` VARCHAR(50) NOT NULL COMMENT '登录用户名',
  \`password_hash\` VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码(种子数据统一为 123456)',
  \`nickname\` VARCHAR(50) NOT NULL COMMENT '昵称',
  \`avatar\` VARCHAR(500) DEFAULT NULL COMMENT '头像 URL',
  \`phone\` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  \`role\` VARCHAR(10) NOT NULL DEFAULT 'USER' COMMENT '角色:USER / ADMIN',
  \`status\` VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态:ACTIVE / DISABLED',
  \`bio\` VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
  \`created_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间(映射 createdAt)',
  \`updated_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(映射 updatedAt)',
  PRIMARY KEY (\`id\`),
  UNIQUE KEY \`uk_username\` (\`username\`),
  KEY \`idx_role_status\` (\`role\`, \`status\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';`)
line()

line('-- ------------------------------------------------------------')
line('-- 2. 商品分类表')
line('-- ------------------------------------------------------------')
line(`CREATE TABLE \`campus_product_category\` (
  \`id\` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  \`name\` VARCHAR(50) NOT NULL COMMENT '分类名称',
  \`sort_order\` INT NOT NULL DEFAULT 0 COMMENT '排序(越小越靠前)',
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';`)
line()

line('-- ------------------------------------------------------------')
line('-- 3. 商品表')
line('-- ------------------------------------------------------------')
line(`CREATE TABLE \`campus_product\` (
  \`id\` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  \`title\` VARCHAR(100) NOT NULL COMMENT '商品标题',
  \`category_id\` BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
  \`price\` DECIMAL(10,2) NOT NULL COMMENT '售价',
  \`original_price\` DECIMAL(10,2) DEFAULT NULL COMMENT '原价/参考价',
  \`description\` TEXT COMMENT '商品描述',
  \`images\` JSON COMMENT '图片 URL 数组,如 ["https://...","https://..."]',
  \`seller_id\` BIGINT UNSIGNED NOT NULL COMMENT '卖家用户ID',
  \`status\` VARCHAR(10) NOT NULL DEFAULT 'ON_SALE' COMMENT '状态:ON_SALE / OFF_SHELF / SOLD',
  \`favorite_count\` INT NOT NULL DEFAULT 0 COMMENT '收藏数(冗余,收藏/取消时同步)',
  \`view_count\` INT NOT NULL DEFAULT 0 COMMENT '浏览量(详情接口 +1)',
  \`created_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间(映射 createdAt)',
  \`updated_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(映射 updatedAt)',
  PRIMARY KEY (\`id\`),
  KEY \`idx_status_category\` (\`status\`, \`category_id\`),
  KEY \`idx_seller\` (\`seller_id\`),
  KEY \`idx_created_at\` (\`created_at\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二手商品表';`)
line()

line('-- ------------------------------------------------------------')
line('-- 4. 收藏表')
line('-- ------------------------------------------------------------')
line(`CREATE TABLE \`campus_favorite\` (
  \`id\` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  \`user_id\` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  \`product_id\` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  \`created_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (\`id\`),
  UNIQUE KEY \`uk_user_product\` (\`user_id\`, \`product_id\`),
  KEY \`idx_product\` (\`product_id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏表';`)
line()

line('-- ------------------------------------------------------------')
line('-- 5. 活动分类表')
line('-- ------------------------------------------------------------')
line(`CREATE TABLE \`campus_activity_category\` (
  \`id\` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  \`name\` VARCHAR(50) NOT NULL COMMENT '分类名称',
  \`sort_order\` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动分类表';`)
line()

line('-- ------------------------------------------------------------')
line('-- 6. 活动表')
line('-- ------------------------------------------------------------')
line(`CREATE TABLE \`campus_activity\` (
  \`id\` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  \`title\` VARCHAR(100) NOT NULL COMMENT '活动标题',
  \`description\` TEXT COMMENT '活动介绍',
  \`category_id\` BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
  \`location\` VARCHAR(100) NOT NULL COMMENT '活动地点',
  \`cover\` VARCHAR(500) DEFAULT NULL COMMENT '封面图 URL',
  \`start_time\` DATETIME NOT NULL COMMENT '开始时间(映射 startTime)',
  \`end_time\` DATETIME NOT NULL COMMENT '结束时间(映射 endTime)',
  \`max_participants\` INT NOT NULL COMMENT '人数上限(映射 maxParticipants)',
  \`current_participants\` INT NOT NULL DEFAULT 0 COMMENT '当前报名人数(映射 currentParticipants)',
  \`organizer\` VARCHAR(50) NOT NULL COMMENT '主办方',
  \`organizer_id\` BIGINT UNSIGNED DEFAULT NULL COMMENT '主办方用户ID',
  \`status\` VARCHAR(10) NOT NULL DEFAULT 'UPCOMING' COMMENT '状态:UPCOMING / ONGOING / FINISHED',
  \`created_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(映射 createdAt)',
  \`updated_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(映射 updatedAt)',
  PRIMARY KEY (\`id\`),
  KEY \`idx_status_start\` (\`status\`, \`start_time\`),
  KEY \`idx_category\` (\`category_id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园活动表';`)
line()

line('-- ------------------------------------------------------------')
line('-- 7. 活动报名表')
line('-- ------------------------------------------------------------')
line(`CREATE TABLE \`campus_activity_registration\` (
  \`id\` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  \`activity_id\` BIGINT UNSIGNED NOT NULL COMMENT '活动ID',
  \`user_id\` BIGINT UNSIGNED NOT NULL COMMENT '报名用户ID',
  \`created_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  PRIMARY KEY (\`id\`),
  UNIQUE KEY \`uk_activity_user\` (\`activity_id\`, \`user_id\`),
  KEY \`idx_user\` (\`user_id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表(报名/取消时同步 campus_activity.current_participants)';`)
line()

line('-- ------------------------------------------------------------')
line('-- 8. 公告表')
line('-- ------------------------------------------------------------')
line(`CREATE TABLE \`campus_announcement\` (
  \`id\` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  \`title\` VARCHAR(100) NOT NULL COMMENT '公告标题',
  \`content\` TEXT NOT NULL COMMENT '公告内容',
  \`category\` VARCHAR(20) NOT NULL COMMENT '分类:通知公告/教务信息/后勤服务/校园活动',
  \`author\` VARCHAR(50) NOT NULL COMMENT '发布单位',
  \`published\` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否发布:1 是 / 0 否',
  \`view_count\` INT NOT NULL DEFAULT 0 COMMENT '浏览量(映射 viewCount)',
  \`created_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间(映射 createdAt)',
  \`updated_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(映射 updatedAt)',
  PRIMARY KEY (\`id\`),
  KEY \`idx_category_published\` (\`category\`, \`published\`),
  KEY \`idx_created_at\` (\`created_at\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园公告表';`)
line()

line('-- ------------------------------------------------------------')
line('-- 9. 失物招领表')
line('-- ------------------------------------------------------------')
line(`CREATE TABLE \`campus_lost_found\` (
  \`id\` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  \`type\` VARCHAR(10) NOT NULL COMMENT '类型:LOST 失物 / FOUND 招领',
  \`title\` VARCHAR(100) NOT NULL COMMENT '标题',
  \`description\` TEXT COMMENT '详细描述',
  \`location\` VARCHAR(100) NOT NULL COMMENT '地点',
  \`contact\` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
  \`images\` JSON COMMENT '图片 URL 数组',
  \`status\` VARCHAR(10) NOT NULL DEFAULT 'OPEN' COMMENT '状态:OPEN / RESOLVED',
  \`publisher_id\` BIGINT UNSIGNED NOT NULL COMMENT '发布者用户ID',
  \`created_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间(映射 createdAt)',
  \`updated_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(映射 updatedAt)',
  PRIMARY KEY (\`id\`),
  KEY \`idx_type_status\` (\`type\`, \`status\`),
  KEY \`idx_publisher\` (\`publisher_id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='失物招领表';`)
line()

line('-- ------------------------------------------------------------')
line('-- 10. 通知表')
line('-- ------------------------------------------------------------')
line(`CREATE TABLE \`campus_notification\` (
  \`id\` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  \`user_id\` BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID',
  \`title\` VARCHAR(100) NOT NULL COMMENT '通知标题',
  \`content\` TEXT NOT NULL COMMENT '通知内容',
  \`type\` VARCHAR(20) NOT NULL DEFAULT 'SYSTEM' COMMENT '类型:SYSTEM / MARKET / ACTIVITY / LOST_FOUND',
  \`is_read\` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读:0 未读 / 1 已读(实体映射为 read)',
  \`created_at\` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(映射 createdAt)',
  PRIMARY KEY (\`id\`),
  KEY \`idx_user_read\` (\`user_id\`, \`is_read\`),
  KEY \`idx_created_at\` (\`created_at\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知表';`)
line()
line('SET FOREIGN_KEY_CHECKS = 1;')
line()

// 用户
line('-- ------------------------------------------------------------')
line(`-- 种子数据:用户(${users.length})`)
line('-- ------------------------------------------------------------')
line('INSERT INTO `campus_user` (`username`, `password_hash`, `nickname`, `avatar`, `phone`, `role`, `status`, `bio`, `created_at`) VALUES')
users.forEach((user, index) => {
  const comma = index < users.length - 1 ? ',' : ';'
  line(
    `  ('${esc(user.username)}', '${PASSWORD_HASH}', '${esc(user.nickname)}', ` +
      `'${avatarUrl(user.id)}', '${esc(user.phone)}', ` +
      `'${user.role}', '${user.status}', '${esc(user.bio)}', DATE_SUB(NOW(), INTERVAL ${user.daysAgo} DAY))${comma}`
  )
})
line()

// 商品分类
line('-- ------------------------------------------------------------')
line('-- 种子数据:商品分类(7)')
line('-- ------------------------------------------------------------')
line('INSERT INTO `campus_product_category` (`id`, `name`, `sort_order`) VALUES')
productCategories.forEach(([id, name, order], index) => {
  line(`  (${id}, '${name}', ${order})${index < productCategories.length - 1 ? ',' : ';'}`)
})
line()

// 商品
line('-- ------------------------------------------------------------')
line(`-- 种子数据:商品(${products.length})`)
line('-- ------------------------------------------------------------')
line('INSERT INTO `campus_product` (`title`, `category_id`, `price`, `original_price`, `description`, `images`, `seller_id`, `status`, `favorite_count`, `view_count`, `created_at`) VALUES')
products.forEach((product, index) => {
  const comma = index < products.length - 1 ? ',' : ';'
  line(
    `  ('${esc(product.title)}', ${product.categoryId}, ${product.price.toFixed(2)}, ${product.originalPrice.toFixed(2)}, ` +
      `'${esc(product.description)}', ${jsonArray(product.images)}, ${product.sellerId}, ` +
      `'${product.status}', ${product.favoriteCount}, ${product.viewCount}, ` +
      `DATE_SUB(NOW(), INTERVAL ${product.daysAgo} DAY))${comma}`
  )
})
line()

// 收藏
line('-- ------------------------------------------------------------')
line(`-- 种子数据:收藏(${favorites.length})`)
line('-- ------------------------------------------------------------')
line('INSERT INTO `campus_favorite` (`user_id`, `product_id`, `created_at`) VALUES')
favorites.forEach(([userId, productId], index) => {
  const comma = index < favorites.length - 1 ? ',' : ';'
  line(`  (${userId}, ${productId}, DATE_SUB(NOW(), INTERVAL ${1 + Math.floor(rand() * 30)} DAY))${comma}`)
})
line()

// 活动分类
line('-- ------------------------------------------------------------')
line('-- 种子数据:活动分类(6)')
line('-- ------------------------------------------------------------')
line('INSERT INTO `campus_activity_category` (`id`, `name`, `sort_order`) VALUES')
activityCategories.forEach(([id, name, order], index) => {
  line(`  (${id}, '${name}', ${order})${index < activityCategories.length - 1 ? ',' : ';'}`)
})
line()

// 活动
line('-- ------------------------------------------------------------')
line(`-- 种子数据:活动(${activities.length})(时间相对 NOW() 生成,状态自动正确)`)
line('-- ------------------------------------------------------------')
line('INSERT INTO `campus_activity` (`title`, `description`, `category_id`, `location`, `cover`, `start_time`, `end_time`, `max_participants`, `current_participants`, `organizer`, `organizer_id`, `status`, `created_at`) VALUES')
activities.forEach((activity, index) => {
  const comma = index < activities.length - 1 ? ',' : ';'
  line(
    `  ('${esc(activity.title)}', '${esc(activity.description)}', ${activity.categoryId}, ` +
      `'${esc(activity.location)}', '${THEME_IMAGES.a[activity.title] || `https://picsum.photos/seed/campus-a${activity.id}/960/540`}', ` +
      `DATE_ADD(NOW(), INTERVAL ${activity.startHours} HOUR), DATE_ADD(NOW(), INTERVAL ${activity.startHours + activity.durationHours} HOUR), ` +
      `${activity.max}, ${activity.participants}, '${esc(activity.organizer)}', 1, ` +
      `'${activity.status}', DATE_SUB(NOW(), INTERVAL ${5 + Math.floor(rand() * 20)} DAY))${comma}`
  )
})
line()

// 报名
line('-- ------------------------------------------------------------')
line(`-- 种子数据:活动报名(${registrations.length} 条,与 current_participants 一致)`)
line('-- ------------------------------------------------------------')
line('INSERT INTO `campus_activity_registration` (`activity_id`, `user_id`, `created_at`) VALUES')
registrations.forEach(([activityId, userId], index) => {
  const comma = index < registrations.length - 1 ? ',' : ';'
  line(`  (${activityId}, ${userId}, DATE_SUB(NOW(), INTERVAL ${1 + Math.floor(rand() * 7)} DAY))${comma}`)
})
line()

// 公告
line('-- ------------------------------------------------------------')
line(`-- 种子数据:公告(${announcements.length})`)
line('-- ------------------------------------------------------------')
line('INSERT INTO `campus_announcement` (`title`, `content`, `category`, `author`, `published`, `view_count`, `created_at`) VALUES')
announcements.forEach(([title, category, author, viewCount, daysAgo, content], index) => {
  const comma = index < announcements.length - 1 ? ',' : ';'
  line(
    `  ('${esc(title)}', '${esc(content)}', '${category}', '${esc(author)}', 1, ${viewCount}, ` +
      `DATE_SUB(NOW(), INTERVAL ${daysAgo} DAY))${comma}`
  )
})
line()

// 失物招领
line('-- ------------------------------------------------------------')
line(`-- 种子数据:失物招领(${lostFoundSeeds.length})`)
line('-- ------------------------------------------------------------')
line('INSERT INTO `campus_lost_found` (`type`, `title`, `description`, `location`, `contact`, `images`, `status`, `publisher_id`, `created_at`) VALUES')
lostFoundSeeds.forEach(([type, title, location, description, contact, status, publisherId], index) => {
  const comma = index < lostFoundSeeds.length - 1 ? ',' : ';'
  const images =
    type === 'FOUND'
      ? jsonArray(THEME_IMAGES.lf[title] || [`https://picsum.photos/seed/campus-lf${index + 1}/800/600`])
      : 'NULL'
  line(
    `  ('${type}', '${esc(title)}', '${esc(description)}', '${esc(location)}', '${esc(contact)}', ` +
      `${images}, '${status}', ${publisherId}, DATE_SUB(NOW(), INTERVAL ${1 + index} DAY))${comma}`
  )
})
line()

// 通知
line('-- ------------------------------------------------------------')
line(`-- 种子数据:通知(${notifications.length})`)
line('-- ------------------------------------------------------------')
line('INSERT INTO `campus_notification` (`user_id`, `title`, `content`, `type`, `is_read`, `created_at`) VALUES')
notifications.forEach((notice, index) => {
  const comma = index < notifications.length - 1 ? ',' : ';'
  line(
    `  (${notice.userId}, '${esc(notice.title)}', '${esc(notice.content)}', '${notice.type}', ${notice.read}, ` +
      `DATE_SUB(NOW(), INTERVAL ${notice.hoursAgo} HOUR))${comma}`
  )
})
line()

line('-- ============================================================')
line('-- 完成:共 10 张表,含用户/分类/商品/收藏/活动/报名/公告/失物/通知种子数据')
line('-- 提示:manage 后台的 Dashboard 指标由以上表聚合而来,无需单独建表')
line('-- ============================================================')

const target = path.join(__dirname, 'campushub.sql')
fs.writeFileSync(target, out.join('\n'), 'utf8')
console.log(`SQL 已生成:${target}`)
console.log(`行数:${out.length}`)
