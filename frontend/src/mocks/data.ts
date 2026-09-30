import type {
  Activity,
  ActivityCategory,
  ActivityStatus,
} from '@/types/activity'
import type { Announcement } from '@/types/announcement'
import type { DashboardData } from '@/types/dashboard'
import type { LostFoundItem, LostFoundType } from '@/types/lostFound'
import type { Product, ProductCategory, ProductStatus } from '@/types/market'
import type { Notification, NotificationType } from '@/types/notification'
import type { User } from '@/types/user'

/** 确定性伪随机数,保证每次刷新 Mock 数据一致 */
function mulberry32(seed: number): () => number {
  let state = seed
  return () => {
    state |= 0
    state = (state + 0x6d2b79f5) | 0
    let t = Math.imul(state ^ (state >>> 15), 1 | state)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

const rand = mulberry32(20260813)
const pick = <T>(list: T[]): T => list[Math.floor(rand() * list.length)]
const between = (min: number, max: number): number => Math.floor(rand() * (max - min + 1)) + min

// ---------------------------------------------------------------
// 图片素材:与数据库种子(backend/sql/02~04)使用同一批 Pexels 真实图片,
// 按标题关键词匹配主题(键盘配键盘图、考研书配教材图……),避免随机风景图。
// ---------------------------------------------------------------
const PEXELS = (id: number, w = 800): string =>
  `https://images.pexels.com/photos/${id}/pexels-photo-${id}.jpeg?auto=compress&cs=tinysrgb&w=${w}`

interface ImageRule {
  match: RegExp
  ids: number[]
}

const PRODUCT_IMAGE_RULES: ImageRule[] = [
  { match: /手环充电|表带/, ids: [18662969, 5237704, 8217438] },
  { match: /键盘/, ids: [5152261, 4792720, 9020272] },
  { match: /降噪耳机|头戴/, ids: [7772548, 3394650, 5269726] },
  { match: /AirPods|耳机盒/, ids: [3921872, 3921846, 3921827] },
  { match: /鼠标/, ids: [29259392, 2115256, 1486294] },
  { match: /拍立得/, ids: [10806074, 2438300, 4062180] },
  { match: /投影/, ids: [6062806, 16699711, 11092278] },
  { match: /Switch|游戏机/, ids: [371924, 6993180, 4523029] },
  { match: /手环/, ids: [4429155, 374619, 5036927] },
  { match: /显示器/, ids: [12509206, 8353774, 115655] },
  { match: /考研|数学|复习/, ids: [1438044, 23656142, 9791429] },
  { match: /高数|物理/, ids: [9791429, 6579277, 1430796] },
  { match: /计算机网络|数据结构|教材|编程/, ids: [1181573, 1181281, 10826689] },
  { match: /雅思|英语/, ids: [11565595, 5676740, 3248644] },
  { match: /法考|法律/, ids: [8849335, 8731037, 6077296] },
  { match: /黄皮书|笔记/, ids: [37778746, 6929186, 5386466] },
  { match: /CPA|会计/, ids: [8962469, 8296970, 8296998] },
  { match: /护理|医学/, ids: [32115953, 37406585, 37407208] },
  { match: /卫衣|摇粒绒/, ids: [5840463, 5319513, 19771863] },
  { match: /冲锋衣|羽绒服/, ids: [19736559, 11344358, 8974263] },
  { match: /邮差包|电脑包|背包/, ids: [36492563, 7054778, 15059375] },
  { match: /头盔/, ids: [12956080, 37625583, 13673124] },
  { match: /山地车|自行车/, ids: [36450314, 16082647, 37897835] },
  { match: /羽毛球拍/, ids: [12630113, 8007419, 6878017] },
  { match: /哑铃/, ids: [37887283, 669580, 3931367] },
  { match: /帐篷/, ids: [33102150, 15925118, 4268094] },
  { match: /瑜伽垫/, ids: [8539005, 28821063, 8436578] },
  { match: /篮球/, ids: [12954255, 6076409, 1462618] },
  { match: /椭圆机/, ids: [6285198, 6285181] },
  { match: /电吉他/, ids: [194011, 12393614, 2049414] },
  { match: /尤克里里/, ids: [6564512, 6564513, 5586494] },
  { match: /吉他/, ids: [7558112, 4688751, 9057766] },
  { match: /落地灯|台灯/, ids: [6078545, 7439756, 32543097] },
  { match: /冰箱/, ids: [37637045, 6996088] },
  { match: /电煮锅/, ids: [29479438, 29479440, 30915727] },
  { match: /体脂秤|体重秤/, ids: [6975463, 29422934] },
  { match: /吹风机/, ids: [32641724, 10892870, 3993328] },
  { match: /小黑瓶|神仙水|小棕瓶|面霜|精华/, ids: [35899861, 4735904, 27544670] },
  { match: /护肤品|化妆品/, ids: [8101512, 7670680, 8167164] },
  { match: /电动牙刷|牙刷/, ids: [33215341, 4045552, 28407751] },
  { match: /电动自行车/, ids: [16435192, 15884318, 8811401] },
  { match: /收纳/, ids: [7309200, 10938208, 3912293] },
  { match: /床上桌/, ids: [31421652, 18267776, 11502135] },
  { match: /Kindle|阅读器/, ids: [1329571, 1475296, 844734] },
  { match: /三脚架/, ids: [11748183, 32717685] },
  { match: /行李箱/, ids: [34629933, 5705068, 36933446] },
  { match: /路由器/, ids: [4218546, 29711663, 18071864] },
  { match: /增高架|置物架/, ids: [34502055, 4792712, 8316281] },
  { match: /风扇/, ids: [3675622, 5850340, 4389919] },
]

const FALLBACK_PRODUCT_IDS = [5840463, 7772548, 1438044]

const ACTIVITY_IMAGE_RULES: ImageRule[] = [
  { match: /欢乐跑|5 公里|5公里/, ids: [32327116] },
  { match: /读书会/, ids: [32751996] },
  { match: /旧衣/, ids: [6994940] },
  { match: /市集/, ids: [19152576] },
  { match: /分享会|AI|大模型/, ids: [34774347] },
  { match: /秋日/, ids: [14137287] },
  { match: /定向越野/, ids: [4314209] },
  { match: /摄影/, ids: [5466870] },
  { match: /编程|Hack/, ids: [8128190] },
  { match: /音乐/, ids: [16803238] },
  { match: /考研/, ids: [37811241] },
  { match: /辩论/, ids: [17558082] },
  { match: /篮球/, ids: [32600343] },
  { match: /英语角/, ids: [7972561] },
  { match: /皮革|手作/, ids: [4452508] },
  { match: /歌手|唱歌/, ids: [16108227] },
  { match: /急救|CPR/, ids: [33317471] },
  { match: /讲座|职业/, ids: [8761330] },
  { match: /汉服/, ids: [37664482] },
  { match: /自习/, ids: [9572540] },
]

const FALLBACK_ACTIVITY_IDS = [37811241]

const LOST_FOUND_IMAGE_RULES: ImageRule[] = [
  { match: /耳机/, ids: [3921887, 3921831, 7424282] },
  { match: /学生卡|校园卡/, ids: [7108127, 7821483] },
  { match: /银行卡/, ids: [5717969, 6214152] },
  { match: /钥匙/, ids: [1194027, 109361, 16329644] },
  { match: /围巾|衣物/, ids: [5710117, 5603205, 6045254] },
  { match: /资料|课本|书|复习/, ids: [9291615, 32046500, 240163] },
  { match: /充电宝/, ids: [34338614, 3921704, 8137310] },
  { match: /羽毛球/, ids: [6307231, 8286363, 9804653] },
  { match: /存储卡/, ids: [30758131, 18166725, 193057] },
  { match: /眼镜/, ids: [32398468, 9328548, 13430471] },
  { match: /卡/, ids: [7821483, 583881, 7821730] },
]

const FALLBACK_LOST_FOUND_IDS = [7108127]

function imageIdsFor(rules: ImageRule[], fallback: number[], title: string): number[] {
  const hit = rules.find((rule) => rule.match.test(title))
  return hit ? hit.ids : fallback
}

function productImages(title: string): string[] {
  return imageIdsFor(PRODUCT_IMAGE_RULES, FALLBACK_PRODUCT_IDS, title).map((id) => PEXELS(id))
}

function activityCover(title: string): string {
  return PEXELS(imageIdsFor(ACTIVITY_IMAGE_RULES, FALLBACK_ACTIVITY_IDS, title)[0], 960)
}

function lostFoundImages(title: string): string[] {
  return imageIdsFor(LOST_FOUND_IMAGE_RULES, FALLBACK_LOST_FOUND_IDS, title).map((id) => PEXELS(id))
}

const HOUR = 3600 * 1000
const DAY = 24 * HOUR
const now = Date.now()

function iso(ts: number): string {
  return new Date(ts).toISOString()
}

function daysAgo(n: number): string {
  return iso(now - n * DAY)
}

function hoursFromNow(n: number): string {
  return iso(now + n * HOUR)
}

const nicknames = [
  '林晚晴',
  '陈屿',
  '周子墨',
  '苏以安',
  '江澈',
  '陆知夏',
  '叶青梧',
  '许星辞',
  '温言',
  '顾北笙',
  '沈知行',
  '姜南星',
  '白鹭',
  '程一诺',
]

function fakePhone(): string {
  const prefixes = ['138', '139', '150', '158', '176', '186', '188', '199']
  const body = Array.from({ length: 8 }, () => between(0, 9)).join('')
  return `${pick(prefixes)} ${body.slice(0, 4)} ${body.slice(4)}`
}

// 头像统一用 DiceBear avataaars 在线服务(与后端种子数据同一套 URL 规则):
// 原先的 /avatars/avatar-NN.png 是相对路径,但 frontend/public 下并没有 avatars 目录,
// 只有构建产物 dist/ 里才有,dev 模式下必然 404 裂图。
const avatarUrl = (id: number): string =>
  `https://api.dicebear.com/8.x/avataaars/svg?seed=avatar-${String(((id - 1) % 24) + 1).padStart(2, '0')}`

export const users: User[] = [
  {
    id: 1,
    username: 'admin',
    nickname: '平台管理员',
    avatar: avatarUrl(1),
    phone: '139 0000 1024',
    role: 'ADMIN',
    status: 'ACTIVE',
    bio: 'CampusHub 平台运营同学,负责内容审核与系统维护。',
    createdAt: daysAgo(240),
  },
  {
    id: 2,
    username: 'student',
    nickname: '林晚晴',
    avatar: avatarUrl(2),
    phone: '138 2091 4775',
    role: 'USER',
    status: 'ACTIVE',
    bio: '计算机学院大三,二手数码爱好者。',
    createdAt: daysAgo(160),
  },
  ...nicknames.map((nickname, index) => {
    const id = index + 3
    return {
      id,
      username: `campus_${String(id).padStart(3, '0')}`,
      nickname,
      avatar: avatarUrl(id),
      phone: fakePhone(),
      role: 'USER' as const,
      status: (rand() < 0.12 ? 'DISABLED' : 'ACTIVE') as User['status'],
      bio: pick([
        '常在图书馆和操场出没。',
        '喜欢骑行和露营,周末常去郊区。',
        '毕业年级,正在整理四年家当。',
        '摄影爱好者,作品常出现在校园展。',
        '二手电子产品淘换专业户。',
      ]),
      createdAt: daysAgo(between(20, 220)),
    }
  }),
]

export const productCategories: ProductCategory[] = [
  { id: 1, name: '数码电子' },
  { id: 2, name: '图书教材' },
  { id: 3, name: '服饰鞋包' },
  { id: 4, name: '运动户外' },
  { id: 5, name: '生活好物' },
  { id: 6, name: '美妆个护' },
  { id: 7, name: '其他' },
]

const productSeeds: Array<{
  title: string
  categoryId: number
  price: number
  description: string
}> = [
  {
    title: 'ikbc C87 红轴机械键盘 95 新',
    categoryId: 1,
    price: 199,
    description:
      '毕业出,自用两年,红轴手感舒服,键帽换了 PBT 侧刻。带原装数据线,无盒。可当面验货。\n优点:全键无冲,Type-C 接口,宿舍打字安静。',
  },
  {
    title: 'Sony WH-1000XM4 降噪耳机 国行在保',
    categoryId: 1,
    price: 1299,
    description:
      '去年双十一入手,戴的少,成色接近全新。降噪效果不用多说,图书馆自习神器。箱说齐全,保修到明年。',
  },
  {
    title: 'AirPods Pro 2 国行带 AC+',
    categoryId: 1,
    price: 1399,
    description:
      '换了 Android 手机用不上,出给需要的同学。AC+ 还有一年,发票可提供,支持序列号验机。',
  },
  {
    title: '罗技 G502 Hero 游戏鼠标',
    categoryId: 1,
    price: 169,
    description: '可调配重,手感好,打游戏和写代码都合适。正常使用痕迹,功能完好。',
  },
  {
    title: '富士拍立得 mini12 含 20 张相纸',
    categoryId: 1,
    price: 449,
    description: '买来拍了一阵子,闲置了。相纸还有 3 盒,一起出。校园活动合影神器。',
  },
  {
    title: '极米 Z6X 智能投影仪',
    categoryId: 1,
    price: 1880,
    description: '宿舍观影神器,1080P 物理分辨率,哈曼卡顿音响。无拆无修,带遥控器。',
  },
  {
    title: 'Nintendo Switch OLED 日版',
    categoryId: 1,
    price: 1599,
    description: '研究生毕业出,吃灰已久。带原装底座、Joy-Con、两张卡带(塞尔达/马力欧赛车)。',
  },
  {
    title: '考研数学复习全书 2027 版(基础+强化)',
    categoryId: 2,
    price: 45,
    description: '上岸出书,只有零星笔记,不影响使用。附赠真题分类讲解电子版。',
  },
  {
    title: '同济高数第七版 上下册',
    categoryId: 2,
    price: 30,
    description: '经典教材,书页无破损,封面微磨损。习题答案册一起送。',
  },
  {
    title: '全套计算机网络教材(谢希仁版)',
    categoryId: 2,
    price: 25,
    description: '期末复习必备,划了重点,适合冲刺阶段参考。',
  },
  {
    title: '雅思剑 16-18 真题+听力音频',
    categoryId: 2,
    price: 60,
    description: '考过出书,九成新。听力音频网盘链接同步赠送。',
  },
  {
    title: '森马连帽卫衣 全新带吊牌',
    categoryId: 3,
    price: 89,
    description: '买错尺码,全新未拆吊牌。藏青色 M 码,适合身高 165-175。',
  },
  {
    title: '北面冲锋衣 三合一可拆卸内胆',
    categoryId: 3,
    price: 680,
    description: '冬季通勤上课都合适,防风防水。穿过一季,内胆洗过无污渍。',
  },
  {
    title: '复古帆布邮差包 牛皮',
    categoryId: 3,
    price: 120,
    description: '上课背电脑刚好,皮质越用越有味道。肩带可调。',
  },
  {
    title: '捷安特 ATX 山地车 24 速',
    categoryId: 4,
    price: 850,
    description: '大一买的,现在出。刹车变速都调过,骑起来很顺。送车锁和头盔。',
  },
  {
    title: '李宁雷霆 80 羽毛球拍',
    categoryId: 4,
    price: 420,
    description: '进攻型球拍,适合有点基础的球友。无伤无裂,换过手胶。',
  },
  {
    title: '哑铃套装 20kg 可拆卸',
    categoryId: 4,
    price: 180,
    description: '宿舍健身够用,杠铃片防滑处理。闲置出,自提。',
  },
  {
    title: '小米手环 8 NFC 版',
    categoryId: 4,
    price: 149,
    description: '可以刷校园卡和公交,跑步计步精准。屏幕无划痕。',
  },
  {
    title: '雅马哈 F310 民谣吉他',
    categoryId: 5,
    price: 520,
    description: '初学练手足够,面板无磕碰,弦距调过好按。送琴包、变调夹、调音器。',
  },
  {
    title: '宜家落地灯 暖光',
    categoryId: 5,
    price: 75,
    description: '宿舍床边看书用,亮度三档可调。自提优先。',
  },
  {
    title: '迷你宿舍冰箱 45L',
    categoryId: 5,
    price: 320,
    description: '冷藏冷冻双温区,声音小,功率低。毕业出,可自提。',
  },
  {
    title: '尤克里里 23 寸 桃花心木',
    categoryId: 5,
    price: 180,
    description: '社团活动奖品,全新未使用。送调音器和包。',
  },
  {
    title: '兰蔻小黑瓶 50ml 全新',
    categoryId: 6,
    price: 520,
    description: '朋友从免税店带多了一瓶,全新未拆封,有效期到 2028。',
  },
  {
    title: 'SK-II 神仙水 230ml 余 8 成',
    categoryId: 6,
    price: 780,
    description: '用了不到两成,肤质不合适出。可小刀,假一赔十。',
  },
  {
    title: '戴森吹风机 HD08 国行',
    categoryId: 6,
    price: 1580,
    description: '成色 95 新,包装盒和风嘴都在。自用两年,无明显划痕。',
  },
  {
    title: '电动自行车 学生代步 48V',
    categoryId: 7,
    price: 1180,
    description: '校园通勤利器,电池可拆卸充电。有正规发票,手续齐全。',
  },
  {
    title: '露营帐篷+防潮垫套装',
    categoryId: 7,
    price: 260,
    description: '用过两次,无破损。适合 2-3 人,毕业清仓。',
  },
  {
    title: '毕业季出:宿舍收纳全套',
    categoryId: 7,
    price: 40,
    description: '置物架、收纳箱、挂篮一整套,自提打包价。',
  },
]

export const products: Product[] = productSeeds.map((seed, index) => {
  const id = index + 1
  const seller = pick(users.filter((u) => u.role === 'USER'))
  const statusRoll = rand()
  const status: ProductStatus = statusRoll < 0.72 ? 'ON_SALE' : statusRoll < 0.88 ? 'OFF_SHELF' : 'SOLD'
  return {
    id,
    title: seed.title,
    categoryId: seed.categoryId,
    categoryName: productCategories.find((c) => c.id === seed.categoryId)?.name ?? '其他',
    price: seed.price,
    originalPrice: Math.round(seed.price * (1.25 + rand() * 0.5)),
    description: seed.description,
    images: productImages(seed.title),
    sellerId: seller.id,
    sellerName: seller.nickname,
    sellerAvatar: seller.avatar,
    status,
    favoriteCount: between(0, 60),
    viewCount: between(20, 900),
    favorite: [1, 2, 5].includes(id),
    createdAt: daysAgo(between(0, 60)),
  }
})

export const activityCategories: ActivityCategory[] = [
  { id: 1, name: '讲座分享' },
  { id: 2, name: '体育竞技' },
  { id: 3, name: '社团活动' },
  { id: 4, name: '志愿服务' },
  { id: 5, name: '文娱演出' },
  { id: 6, name: '其他' },
]

const activitySeeds: Array<{
  title: string
  categoryId: number
  location: string
  description: string
  startOffset: number
  maxParticipants: number
  currentParticipants: number
}> = [
  {
    title: '校运会 5 公里欢乐跑',
    categoryId: 2,
    location: '东区操场',
    description:
      '一年一度的校园欢乐跑,不设成绩门槛,完赛即得纪念奖牌与第二课堂学分。路线途经图书馆、银杏大道与湖畔。',
    startOffset: 26,
    maxParticipants: 200,
    currentParticipants: 156,
  },
  {
    title: '周末读书会:当我们谈论校园时',
    categoryId: 1,
    location: '图书馆一楼报告厅',
    description:
      '本期共读《你当像鸟飞往你的山》,由文学院老师领读。现场提供茶水,带自己的书即可参加。',
    startOffset: 50,
    maxParticipants: 60,
    currentParticipants: 38,
  },
  {
    title: '秋季校园二手市集',
    categoryId: 3,
    location: '学生活动中心广场',
    description:
      '毕业季版二手市集回归!报名成为摊主,处理闲置好物;逛集的同学还能参与抽奖,赢取文创周边。',
    startOffset: 72,
    maxParticipants: 150,
    currentParticipants: 103,
  },
  {
    title: 'AI 大模型入门分享会',
    categoryId: 1,
    location: '教三 402',
    description:
      '计算机学院学长带你从零认识大模型:原理、Prompt 技巧、本地部署实战。零基础友好,带电脑可现场动手。',
    startOffset: 8,
    maxParticipants: 80,
    currentParticipants: 80,
  },
  {
    title: '新生破冰定向越野',
    categoryId: 4,
    location: '校园全域',
    description:
      '分组完成校园地标打卡任务,赢积分换礼品。认识新朋友的第一站,欢迎所有年级参加。',
    startOffset: 96,
    maxParticipants: 120,
    currentParticipants: 66,
  },
  {
    title: '校园摄影展作品征集',
    categoryId: 5,
    location: '线上投稿+图书馆展出',
    description:
      '以「我眼里的四季校园」为主题征集摄影作品,入选作品将在图书馆走廊展出并收录进校园年鉴。',
    startOffset: 30,
    maxParticipants: 100,
    currentParticipants: 47,
  },
  {
    title: '编程马拉松 HackNight 36h',
    categoryId: 6,
    location: '创新创业学院 3F',
    description:
      '36 小时极限开发,主题现场揭晓。提供场地、夜宵与导师辅导,优秀团队直通校级竞赛。',
    startOffset: 120,
    maxParticipants: 60,
    currentParticipants: 60,
  },
  {
    title: '吉他社草地音乐节',
    categoryId: 5,
    location: '湖畔草坪',
    description:
      '夏夜草坪音乐会,社团乐队与个人弹唱轮番登场。观众可自由点歌,现场提供荧光棒。',
    startOffset: 44,
    maxParticipants: 300,
    currentParticipants: 212,
  },
  {
    title: '考研经验分享会(上岸学长学姐团)',
    categoryId: 1,
    location: '行政楼 201',
    description:
      '数学、英语、专业课三场分论坛并行,上岸学长学姐面对面答疑,会后资料包分享。',
    startOffset: -20,
    maxParticipants: 200,
    currentParticipants: 200,
  },
  {
    title: '校园公益旧衣回收',
    categoryId: 4,
    location: '各宿舍楼下',
    description:
      '与市慈善总会合作的旧衣回收活动,每回收 1kg 将配捐 1 元至山区图书角项目。',
    startOffset: -6,
    maxParticipants: 500,
    currentParticipants: 318,
  },
  {
    title: '校辩论队表演赛:人工智能应否进入课堂',
    categoryId: 1,
    location: '学生活动中心报告厅',
    description:
      '正反双方各 20 分钟立论与质询,现场观众可参与自由辩论环节。',
    startOffset: -2,
    maxParticipants: 260,
    currentParticipants: 260,
  },
]

function activityStatus(startTs: number, endTs: number): ActivityStatus {
  if (now < startTs) return 'UPCOMING'
  if (now <= endTs) return 'ONGOING'
  return 'FINISHED'
}

export const activities: Activity[] = activitySeeds.map((seed, index) => {
  const id = index + 1
  const startTime = hoursFromNow(seed.startOffset)
  const endTime = hoursFromNow(seed.startOffset + between(2, 6))
  return {
    id,
    title: seed.title,
    description: seed.description,
    categoryId: seed.categoryId,
    categoryName: activityCategories.find((c) => c.id === seed.categoryId)?.name ?? '其他',
    location: seed.location,
    cover: activityCover(seed.title),
    startTime,
    endTime,
    maxParticipants: seed.maxParticipants,
    currentParticipants: seed.currentParticipants,
    remainingParticipants: Math.max(0, seed.maxParticipants - seed.currentParticipants),
    registered: false,
    organizer: '校团委·学生社团联合会',
    organizerId: 1,
    status: activityStatus(new Date(startTime).getTime(), new Date(endTime).getTime()),
    createdAt: daysAgo(between(3, 20)),
  }
})

/** 活动报名记录:activityId -> 报名用户 id 列表 */
export const activityRegistrations: Record<number, number[]> = {
  1: [2],
  4: [2],
}

export const announcements: Announcement[] = [
  {
    id: 1,
    title: '关于校园网出口带宽升级的通知',
    category: '后勤服务',
    content:
      '各位师生:\n\n为改善校园网络高峰期体验,信息化中心将于本周六 0:00-6:00 对校园网出口带宽进行升级,期间校园网可能出现短暂中断,请提前安排好学习与工作。\n\n升级完成后,校园网上下行带宽将在现有基础上提升 50%,具体覆盖范围以校内通知为准。\n\n信息化中心\n2026 年 8 月',
    author: '信息化中心',
    published: true,
    viewCount: 1204,
    createdAt: daysAgo(1),
    updatedAt: daysAgo(1),
  },
  {
    id: 2,
    title: '2026 秋季学期选课安排',
    category: '教务信息',
    content:
      '各位同学:\n\n2026 秋季学期选课共分三轮:\n\n第一轮(预选):8 月 20 日 10:00 - 8 月 22 日 22:00\n第二轮(正选):8 月 26 日 10:00 - 8 月 28 日 22:00\n第三轮(补退选):9 月 6 日 10:00 - 9 月 10 日 22:00\n\n请同学们及时登录教务系统查看培养方案,合理规划课程。',
    author: '教务处',
    published: true,
    viewCount: 3421,
    createdAt: daysAgo(2),
    updatedAt: daysAgo(2),
  },
  {
    id: 3,
    title: '图书馆暑期开放时间调整',
    category: '后勤服务',
    content:
      '各位读者:\n\n暑期图书馆开放时间为每日 8:30-21:30,其中二层自习区 7:30 开放。8 月 20 日起恢复常规开放时间。\n\n三楼教师阅览室暑期暂停开放,四楼电子阅览室正常开放。',
    author: '图书馆',
    published: true,
    viewCount: 876,
    createdAt: daysAgo(4),
    updatedAt: daysAgo(4),
  },
  {
    id: 4,
    title: '校园卡补办流程优化上线',
    category: '后勤服务',
    content:
      '为减少排队,校园卡挂失与补办已全面支持线上办理。登录校园卡服务平台即可自助申请,补办成功后短信通知领取地点。\n\n首次补办免费,再次补办收取工本费 20 元。',
    author: '后勤服务中心',
    published: true,
    viewCount: 653,
    createdAt: daysAgo(6),
    updatedAt: daysAgo(6),
  },
  {
    id: 5,
    title: '校医院疫苗接种安排(九价 HPV 第二轮)',
    category: '通知公告',
    content:
      '各位同学:\n\n校医院第二轮九价 HPV 疫苗预约将于 8 月 15 日 12:00 开放,共 300 个名额。预约成功后按短信通知时间接种。\n\n接种请携带身份证与校园卡。',
    author: '校医院',
    published: true,
    viewCount: 2910,
    createdAt: daysAgo(7),
    updatedAt: daysAgo(7),
  },
  {
    id: 6,
    title: '第二课堂学分认定办法更新',
    category: '教务信息',
    content:
      '新版《第二课堂学分认定办法》已经校务会审议通过,自 2026 秋季学期起执行。主要变化:志愿服务学分上限提高,竞赛获奖可折算双倍学分。\n\n详细条款见附件。',
    author: '校团委',
    published: true,
    viewCount: 1877,
    createdAt: daysAgo(9),
    updatedAt: daysAgo(9),
  },
  {
    id: 7,
    title: '电动车充电桩新增点位启用',
    category: '后勤服务',
    content:
      '为满足电动车充电需求,后勤保障部在东区宿舍 7 号楼、西区教学楼地下停车场新增充电桩 40 个,现已投入使用。\n\n充电费用按用电量计费,支持校园卡与微信扫码支付。',
    author: '后勤保障部',
    published: true,
    viewCount: 998,
    createdAt: daysAgo(11),
    updatedAt: daysAgo(11),
  },
  {
    id: 8,
    title: '学生宿舍热水供应时间调整',
    category: '后勤服务',
    content:
      '自 9 月起,宿舍热水供应时间调整为每日 6:30-8:30 与 17:00-24:00。暑期非整点时段为 17:00-23:00。\n\n如遇供水异常,请拨打宿舍服务热线 8225 0000。',
    author: '后勤服务中心',
    published: true,
    viewCount: 1245,
    createdAt: daysAgo(14),
    updatedAt: daysAgo(14),
  },
  {
    id: 9,
    title: '校园文创纪念品上新',
    category: '校园活动',
    content:
      '校史馆文创店上新:银杏书签、校门积木、四季明信片等 12 款新品,开学季全场 88 折。毕业季限定帆布袋同步发售。',
    author: '校史馆',
    published: true,
    viewCount: 534,
    createdAt: daysAgo(16),
    updatedAt: daysAgo(16),
  },
]

const lostFoundSeeds: Array<{
  type: LostFoundType
  title: string
  location: string
  description: string
  contact: string
}> = [
  {
    type: 'LOST',
    title: '黑色钱包(内含校园卡)遗失在图书馆三楼',
    location: '图书馆三楼中文期刊区',
    description:
      '黑色短款钱包,里面有校园卡、身份证和少量现金。有捡到的同学请联系我,感谢!',
    contact: '电话:138 2091 4775 · 图书馆前台可代收',
  },
  {
    type: 'FOUND',
    title: '拾到 AirPods 耳机盒(东操场)',
    location: '东区操场看台',
    description:
      '周二晚在东操场看台拾到 AirPods 耳机盒,已在失物招领处登记。请描述盒内刻字以认领。',
    contact: '失物招领处(学生活动中心 101)',
  },
  {
    type: 'LOST',
    title: '蓝白条纹雨伞遗失在教三',
    location: '教三 201',
    description: '蓝色白条纹折叠伞,伞柄有「C」字挂坠。下午 3 点落在教室后排。',
    contact: 'QQ:841027563',
  },
  {
    type: 'FOUND',
    title: '拾到学生卡一张(姓名:陈屿)',
    location: '食堂二楼',
    description: '在食堂二楼靠窗位置拾到学生卡,已交给一卡通服务中心。',
    contact: '一卡通服务中心(东门旁)',
  },
  {
    type: 'LOST',
    title: '灰色书包遗落在快递站',
    location: '菜鸟驿站',
    description: '灰色双肩包,里面有教材和 iPad 保护套。监控显示下午 5 点后还在。',
    contact: '电话:186 7712 0394',
  },
  {
    type: 'FOUND',
    title: '拾到钥匙串(带蓝色挂件)',
    location: '湖畔草坪',
    description: '一串钥匙,带蓝色星球挂件和门禁卡套。请描述钥匙数量以认领。',
    contact: '湖畔巡逻岗亭',
  },
  {
    type: 'LOST',
    title: '银色保温杯遗失在食堂二楼',
    location: '食堂二楼',
    description: '银色 500ml 保温杯,杯身贴了「明天也要加油」贴纸。',
    contact: '电话:150 6634 8821',
  },
  {
    type: 'FOUND',
    title: '拾到校园卡(工商银行卡联名)',
    location: '教学楼 B 栋电梯口',
    description: '工商银行卡联名校园卡,卡号尾号 1024,已交至保卫处。',
    contact: '保卫处值班室(行政楼 110)',
  },
  {
    type: 'LOST',
    title: '黑色单反相机包(内含镜头)',
    location: '校史馆门口',
    description:
      '黑色相机包,内有佳能套机镜头。摄影课作业需要,比较着急,捡到的同学麻烦联系我,必有酬谢。',
    contact: '电话:188 9034 2216',
  },
]

export const lostFoundItems: LostFoundItem[] = lostFoundSeeds.map((seed, index) => {
  const id = index + 1
  const publisher = pick(users.filter((u) => u.role === 'USER'))
  return {
    id,
    type: seed.type,
    title: seed.title,
    description: seed.description,
    location: seed.location,
    contact: seed.contact,
    images: seed.type === 'FOUND' ? lostFoundImages(seed.title) : [],
    status: rand() < 0.18 ? 'RESOLVED' : 'OPEN',
    publisherId: publisher.id,
    publisherName: publisher.nickname,
    publisherAvatar: publisher.avatar,
    createdAt: daysAgo(between(0, 20)),
  }
})

const notificationSeeds: Array<{
  type: NotificationType
  title: string
  content: string
  hoursAgo: number
}> = [
  {
    type: 'ACTIVITY',
    title: '报名成功:校运会 5 公里欢乐跑',
    content: '你已成功报名「校运会 5 公里欢乐跑」,活动时间为 8 月 20 日,请提前 30 分钟到场签到。',
    hoursAgo: 2,
  },
  {
    type: 'MARKET',
    title: '你发布的商品被收藏',
    content: '商品「ikbc C87 红轴机械键盘 95 新」新增 3 次收藏,当前共 18 人收藏。',
    hoursAgo: 5,
  },
  {
    type: 'SYSTEM',
    title: 'CampusHub 服务升级公告',
    content: '平台将于 8 月 16 日 02:00-04:00 进行例行维护,期间部分功能可能短暂不可用。',
    hoursAgo: 9,
  },
  {
    type: 'LOST_FOUND',
    title: '你发布的失物有新进展',
    content: '「黑色钱包(内含校园卡)遗失在图书馆三楼」的状态已更新为已解决,感谢大家的转发。',
    hoursAgo: 26,
  },
  {
    type: 'MARKET',
    title: '商品降价提醒',
    content: '你关注的「Sony WH-1000XM4 降噪耳机」降价至 ¥1299,比收藏时低 ¥100。',
    hoursAgo: 30,
  },
  {
    type: 'ACTIVITY',
    title: '活动提醒:AI 大模型入门分享会',
    content: '「AI 大模型入门分享会」将于 1 小时后在教三 402 开始,记得带电脑。',
    hoursAgo: 47,
  },
  {
    type: 'SYSTEM',
    title: '账号安全提醒',
    content: '检测到你的账号在新设备登录,如非本人操作请及时修改密码。',
    hoursAgo: 72,
  },
  {
    type: 'MARKET',
    title: '你的商品已售出',
    content: '商品「考研数学复习全书 2027 版」已被买家确认收货,交易完成。',
    hoursAgo: 96,
  },
  {
    type: 'LOST_FOUND',
    title: '失物招领动态',
    content: '「拾到 AirPods 耳机盒(东操场)」有新评论,点击查看详情。',
    hoursAgo: 120,
  },
  {
    type: 'SYSTEM',
    title: '欢迎加入 CampusHub',
    content: '完善个人资料并上传头像,更容易获得同学信任。发布商品与报名活动都会实时通知你。',
    hoursAgo: 160,
  },
]

export const notifications: Notification[] = notificationSeeds.map((seed, index) => ({
  id: index + 1,
  type: seed.type,
  title: seed.title,
  content: seed.content,
  read: index > 2,
  createdAt: hoursFromNow(-seed.hoursAgo),
}))

function trend(days: number, base: number, variance: number): Array<{ date: string; value: number }> {
  return Array.from({ length: days }, (_, i) => {
    const date = new Date(now - (days - 1 - i) * DAY)
    const pad = (n: number): string => String(n).padStart(2, '0')
    return {
      date: `${date.getMonth() + 1}-${pad(date.getDate())}`,
      value: Math.max(0, Math.round(base + Math.sin(i / 2.4) * variance + rand() * variance)),
    }
  })
}

export const dashboardData: DashboardData = {
  stats: {
    userTotal: users.length + 1286,
    productTotal: products.length + 342,
    activityTotal: activities.length + 47,
    lostFoundTotal: lostFoundItems.length + 118,
    todayNewUsers: between(6, 30),
    todayNewProducts: between(3, 18),
    todayRegistrations: between(4, 24),
  },
  userGrowth: trend(14, 90, 26),
  productTrend: trend(14, 32, 12),
  registrationTrend: trend(14, 44, 18),
  lostFoundStats: [
    { type: 'LOST', count: 63 },
    { type: 'FOUND', count: 76 },
  ],
}

export const demoAccounts = [
  { username: 'student', password: '123456', label: '普通用户' },
  { username: 'admin', password: '123456', label: '管理员' },
]
