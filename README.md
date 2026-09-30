# CampusHub 校园综合服务平台
![首页预览](https://github.com/user-attachments/assets/44399c98-1e8f-4303-bd63-8fdaa0d90e6e)

<p align="center">
  <b>基于 Spring Cloud Alibaba 微服务 + Vue 3 的校园综合服务平台</b>
  <br />
  二手交易 · 活动报名 · 失物招领 · 校园公告
</p>

---

## 项目简介

CampusHub 是一个面向高校的综合性校园服务平台，采用**前后端分离 + 微服务**架构。
前端只允许通过 API 网关访问后端，禁止直连微服务；后端按业务域拆分为 7 个独立服务，
统一注册到 Nacos 做服务发现与配置管理。

平台覆盖四块核心业务：

| 业务域 | 说明 |
| --- | --- |
| 🛒 二手交易 | 商品发布、分类浏览、收藏、搜索（Elasticsearch） |
| 🎯 活动报名 | 活动发布、在线报名、名额管理 |
| 🔍 失物招领 | 丢物/拾物信息发布、状态流转 |
| 📢 校园公告 | 公告发布与浏览 |

配套一个**管理后台**（`campus-admin-service`），提供用户、商品、活动、
公告、失物招领的统一管理与数据看板。

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| Java | 21 | 开发语言 |
| Spring Boot | 3.2.12 | 基础框架 |
| Spring Cloud | 2023.0.5 | 微服务套件 |
| Spring Cloud Alibaba | 2023.0.3.2 | Nacos 集成 |
| Nacos | — | 服务发现 + 配置中心 |
| Spring Cloud Gateway | — | API 网关 |
| OpenFeign | — | 服务间调用 |
| Resilience4j | — | 熔断降级 |
| MyBatis-Plus | 3.5.16 | ORM |
| MySQL | 8.x | 关系型数据库（按服务分库） |
| Redis | — | 缓存 / Token 黑名单 |
| RabbitMQ | — | 异步消息（事件驱动） |
| Elasticsearch | — | 商品全文检索 |
| JJWT | 0.12.6 | JWT 认证 |
| SpringDoc OpenAPI | 2.3.0 | 接口文档聚合 |

### 前端

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| Vue | 3.5 | 前端框架 |
| TypeScript | 5.8 | 类型系统 |
| Vite | 6.x | 构建工具 |
| Naive UI | 2.41 | UI 组件库 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 4.5 | 路由 |
| Axios | 1.8 | HTTP 客户端 |
| ECharts | 5.6 | 数据可视化 |
| SCSS | — | 样式预处理 |

---

## 系统架构

```
                         ┌──────────────┐
                         │  Vue 3 前端   │
                         └──────┬───────┘
                                │ HTTP /api/**
                                ▼
                    ┌───────────────────────┐
                    │  campus-gateway :8080 │  路由转发 + JWT 鉴权
                    └───────────┬───────────┘
                                │
        ┌───────────┬───────────┼───────────┬───────────┬───────────┐
        ▼           ▼           ▼           ▼           ▼           ▼
   ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
   │  auth  │ │  user  │ │content │ │ market │ │activity│ │notify  │
   │  :8081 │ │  :8083 │ │  :8082 │ │  :8084 │ │  :8086 │ │  :8085 │
   └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘
       │          │          │          │          │          │
       └──────────┴──────────┴────┬─────┴──────────┴──────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │       Nacos 注册中心       │
                    └───────────────────────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              ▼                   ▼                   ▼
          ┌────────┐          ┌────────┐         ┌────────┐
          │ MySQL  │          │ Redis  │         │RabbitMQ│
          └────────┘          └────────┘         └────────┘

                    ┌───────────────────────┐
                    │ campus-admin-service  │  BFF 聚合层（无数据库）
                    │        :8087          │  经 Feign 调各服务内部接口
                    └───────────────────────┘
```

### 微服务职责

| 服务 | 端口 | 数据库 | 职责 |
| --- | --- | --- | --- |
| `campus-gateway` | 8080 | Redis | 统一入口、路由转发、JWT 全局鉴权、CORS |
| `campus-auth-service` | 8081 | `campus_user` | 注册、登录、Token 签发与刷新 |
| `campus-user-service` | 8083 | `campus_user` | 用户资料、管理、统计 |
| `campus-content-service` | 8082 | `campus_content` | 校园公告 + 失物招领 |
| `campus-market-service` | 8084 | `campus_market` | 二手商品、分类、收藏、ES 搜索 |
| `campus-activity-service` | 8086 | `campus_activity` | 活动发布、报名 |
| `campus-notification-service` | 8085 | `campus_notification` | 站内通知（MQ 消费者） |
| `campus-admin-service` | 8087 | — | 管理后台聚合门面（BFF） |
| `campus-common` | — | — | 公共模块：统一响应、异常、JWT 工具 |

**事件驱动**：市场服务的"商品被收藏"、活动服务的"活动被报名"事件通过
RabbitMQ 发布，通知服务作为消费者落库为站内通知。

---

## 项目结构

```
campus-hub/
├── backend/                          # 后端（Maven 多模块）
│   ├── pom.xml                       # 父 POM，统一版本管理
│   ├── campus-common/                # 公共模块
│   ├── campus-gateway/               # API 网关          :8080
│   ├── campus-auth-service/          # 认证服务          :8081
│   ├── campus-content-service/       # 内容服务          :8082
│   ├── campus-user-service/          # 用户服务          :8083
│   ├── campus-market-service/        # 二手市场服务      :8084
│   ├── campus-notification-service/  # 通知服务          :8085
│   ├── campus-activity-service/      # 活动服务          :8086
│   ├── campus-admin-service/         # 管理后台服务      :8087
│   ├── nacos-config/                 # Nacos 配置中心配置文件
│   └── sql/                          # 数据库初始化脚本
│
└── frontend/                         # 前端（Vue 3 + TS）
    ├── src/
    │   ├── api/                      # 数据访问层（唯一出口）
    │   ├── components/               # 通用组件
    │   ├── layouts/                  # 布局
    │   ├── stores/                   # Pinia 状态
    │   ├── types/                    # 类型定义（前后端契约）
    │   ├── utils/                    # 工具函数
    │   └── views/                    # 页面
    └── package.json
```

---

## 快速开始

### 环境要求

| 依赖 | 版本 |
| --- | --- |
| JDK | 21+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.x |
| Redis | 6+ |
| Nacos | 2.x |
| RabbitMQ | 3.x |
| Elasticsearch | 7.x / 8.x |

### 1. 启动中间件

请先确保以下中间件已启动：**MySQL、Redis、Nacos、RabbitMQ、Elasticsearch**。

Nacos 单机启动示例：

```bash
sh startup.sh -m standalone
# 访问 http://localhost:8848/nacos （默认账号密码 nacos/nacos）
```

### 2. 初始化数据库

按服务分库，**二选一**执行（不要同时导入）：

**方案 A — 分库（推荐）**

```bash
mysql -uroot -p < backend/sql/01-campus-user.sql
mysql -uroot -p < backend/sql/02-campus-market.sql
mysql -uroot -p < backend/sql/03-campus-activity.sql
mysql -uroot -p < backend/sql/04-campus-content.sql
mysql -uroot -p < backend/sql/05-campus-notification.sql
```

**方案 B — 单库汇总**

```bash
mysql -uroot -p < backend/sql/campushub.sql
```

### 3. 导入 Nacos 配置

将 `backend/nacos-config/` 下的每个 `.yaml` 文件，按 **Data ID = 文件名**、
**Group = `DEFAULT_GROUP`** 导入 Nacos 配置中心。

| Data ID | 服务 |
| --- | --- |
| `campus-gateway.yaml` | 网关 |
| `campus-auth-service.yaml` | 认证服务 |
| `campus-content-service.yaml` | 内容服务 |
| `campus-user-service.yaml` | 用户服务 |
| `campus-market-service.yaml` | 市场服务 |
| `campus-notification-service.yaml` | 通知服务 |
| `campus-activity-service.yaml` | 活动服务 |
| `campus-admin-service.yaml` | 管理服务 |

> 配置中的连接地址、账号密码均支持环境变量覆盖，
> 如 `MYSQL_HOST`、`REDIS_HOST`、`RABBITMQ_HOST`、`NACOS_ADDR` 等。
> 默认值仅为占位，部署前请按实际环境注入真实地址。

### 4. 启动后端

```bash
cd backend
mvn clean install -DskipTests      # 先安装公共模块

mvn spring-boot:run -pl campus-gateway           # 网关 :8080
mvn spring-boot:run -pl campus-auth-service      # 认证 :8081
mvn spring-boot:run -pl campus-content-service   # 内容 :8082
mvn spring-boot:run -pl campus-user-service      # 用户 :8083
mvn spring-boot:run -pl campus-market-service    # 市场 :8084
mvn spring-boot:run -pl campus-notification-service  # 通知 :8085
mvn spring-boot:run -pl campus-activity-service  # 活动 :8086
mvn spring-boot:run -pl campus-admin-service     # 后台 :8087
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173

其他命令：

```bash
npm run build        # 生产构建（含类型检查）
npm run type-check   # 仅类型检查
```

---

## 环境变量

前端环境变量（`.env.development` / `.env.production`）：

| 变量 | 开发值 | 生产值 | 说明 |
| --- | --- | --- | --- |
| `VITE_API_BASE_URL` | `http://localhost:8080` | `/api` | 仅通过网关访问后端 |
| `VITE_USE_MOCK` | `false` | `false` | 是否启用 Mock 数据 |

后端支持的环境变量：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `NACOS_ADDR` | `127.0.0.1:8848` | Nacos 地址 |
| `NACOS_USERNAME` | `nacos` | Nacos 账号 |
| `NACOS_PASSWORD` | `nacos` | Nacos 密码 |
| `NACOS_REGISTER_IP` | `0.0.0.0` | 注册到 Nacos 的 IP；`0.0.0.0` 表示由框架自动探测，多网卡环境需显式指定 |
| `MYSQL_HOST` | `0.0.0.0` | MySQL 地址 |
| `MYSQL_USERNAME` | `root` | MySQL 账号 |
| `MYSQL_PASSWORD` | `123456` | MySQL 密码 |
| `REDIS_HOST` | `0.0.0.0` | Redis 地址 |
| `REDIS_PASSWORD` | `123456` | Redis 密码 |
| `RABBITMQ_HOST` | `0.0.0.0` | RabbitMQ 地址 |
| `RABBITMQ_USERNAME` | `admin` | RabbitMQ 账号 |
| `RABBITMQ_PASSWORD` | `123456` | RabbitMQ 密码 |
| `ES_URIS` | `http://0.0.0.0:9200` | Elasticsearch 地址 |
| `JWT_SECRET` | 开发用默认值 | JWT 签名密钥（生产必须修改） |

> **关于 `0.0.0.0`**：中间件地址的默认值统一写为 `0.0.0.0`（或 `127.0.0.1`）作为占位，
> 实际部署时请通过环境变量注入真实地址，例如：
>
> ```bash
> export MYSQL_HOST=10.0.0.10
> export REDIS_HOST=10.0.0.11
> ```
>
> 注意 `NACOS_REGISTER_IP` 留空（`0.0.0.0`）时由 Spring Cloud 自动探测本机地址；
> 若机器有多张网卡（如同时存在 Wi-Fi、虚拟网卡），建议显式指定可被其他服务回连的地址。

---

## 配置说明

项目采用**分层配置**：本地 `application.yml` 只保留"启动引导"配置
（服务名、端口、如何连 Nacos），业务配置全部放在 Nacos 配置中心。

**必须留在本地**（否则形成"连 Nacos 才能拿到 Nacos 地址"的死锁）：

- `spring.application.name`
- `server.port`
- Nacos 的 `server-addr` / `username` / `password`
- `spring.config.import` 导入语句

**应放在 Nacos**：数据库、Redis、RabbitMQ、Elasticsearch、
JWT 密钥、网关路由与白名单、日志级别等。

---

## 关于仓库中的默认凭据

> ⚠️ **重要提示**

仓库中 `nacos-config/` 与 `application.yml` 里出现的 `123456`、`nacos`、
`admin` 等，均为**本地开发环境的示例默认值**（`${ENV_VAR:default}` 形式），
方便克隆后快速跑通。

**部署到生产环境前，请务必：**

1. 通过环境变量或独立的 Nacos 配置覆盖所有默认凭据；
2. 更换 `JWT_SECRET` 为高强度随机密钥；
3. 在各服务的 Nacos 配置中关闭 SQL 日志输出（`log-impl`）；
4. 关闭或限制 SpringDoc / Swagger 端点访问。

---

## 数据库设计

按业务域分库，服务间不直接跨库访问：

| 数据库 | 归属服务 | 核心表 |
| --- | --- | --- |
| `campus_user` | auth / user | 用户、角色 |
| `campus_market` | market | 商品、分类、收藏 |
| `campus_activity` | activity | 活动、分类、报名记录 |
| `campus_content` | content | 公告、失物招领 |
| `campus_notification` | notification | 站内通知 |

---

## 接口约定

**统一响应格式**

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

**统一分页格式**

```json
{
  "records": [],
  "total": 0,
  "pageNum": 1,
  "pageSize": 10,
  "pages": 0
}
```

- 所有分页接口统一使用 `pageNum` / `pageSize`
- 认证方式：`Authorization: Bearer {token}`
- 前端 TypeScript 类型定义（`frontend/src/types/`）即前后端共同遵守的接口契约

---

## 项目统计

- 后端 Java 文件：**204** 个
- 前端 Vue 组件：**50** 个
- 前端 TypeScript 文件：**34** 个
- 前端页面路由：**35** 个

---

## 许可证

本项目采用 [MIT License](LICENSE) 开源。

---

<p align="center">Made with ❤️ for Campus</p>
