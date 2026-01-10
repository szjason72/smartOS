# SmartOS - BC4 Pro 光感智能窗帘控制系统

## 📋 项目概述

基于 BC4 Pro 设备的光感功能，实现智能窗帘的自动开合控制。

**核心功能**：
- BC4 Pro 设备管理和通信（本地模拟器）
- 光感数据采集和分析
- 智能窗帘自动控制
- 自动化规则配置
- 实时监控和统计

## 🏗️ 技术架构

### 后端
- **框架**：Spring Boot 3.x
- **数据库**：PostgreSQL（或 MySQL）
- **消息队列**：MQTT (Mosquitto)
- **缓存**：Redis（可选）
- **API文档**：Swagger/OpenAPI

### 前端
- **框架**：Vue 3 + TypeScript
- **UI组件**：Element Plus
- **状态管理**：Pinia
- **路由**：Vue Router
- **HTTP客户端**：Axios

### 设备通信
- **BC4 Pro**：HTTP/MQTT（本地模拟器）
- **智能窗帘**：MQTT/ESPHome API

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- PostgreSQL 14+（或使用Docker）
- Maven 3.8+
- Docker（可选，用于运行PostgreSQL和MQTT Broker）

### 本地化开发（推荐）

**SmartOS采用完全本地化开发方案，不依赖外部服务器！**

#### 1. 启动本地服务（Docker）

```bash
# 启动PostgreSQL和Mosquitto MQTT Broker
docker-compose up -d

# 或使用启动脚本
./scripts/start-local.sh
```

#### 2. 初始化数据库

```bash
./scripts/init-db.sh
```

#### 3. 启动后端服务

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动，包含：
- REST API：`http://localhost:8080/api`
- Mock BC4 Pro API：`http://localhost:8080/mock/bc4`
- Swagger文档：`http://localhost:8080/swagger-ui.html`

#### 4. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

前端将在 `http://localhost:5173` 启动

### 测试Mock API

```bash
# 获取设备状态
curl http://localhost:8080/mock/bc4/BC4_001/status

# 获取光感数据
curl http://localhost:8080/mock/bc4/BC4_001/lightSensor
```

## 📁 项目结构

```
smartOS/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/smartos/
│   │   ├── SmartOsApplication.java
│   │   ├── mock/           # BC4 Pro Mock设备模拟器
│   │   ├── config/         # 配置类
│   │   ├── controller/     # 控制器
│   │   ├── service/        # 业务服务
│   │   ├── repository/     # 数据访问
│   │   ├── model/          # 数据模型
│   │   └── dto/            # 数据传输对象
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/   # 数据库迁移脚本
│
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── api/            # API接口
│   │   ├── components/     # 组件
│   │   ├── views/          # 页面
│   │   ├── stores/         # 状态管理
│   │   └── router/         # 路由
│   └── package.json
│
├── docs/                   # 文档
│   ├── 本地化开发方案.md
│   ├── 本地化开发指南.md
│   ├── shenmou_通信协议分析.md
│   └── zervi.test_微服务集成方案.md
│
├── scripts/                 # 脚本
│   ├── init-db.sh         # 数据库初始化
│   └── start-local.sh     # 本地服务启动
│
├── docker-compose.yml      # Docker编排
└── README.md
```

## 📚 模块说明

### 1. BC4 Pro 设备管理模块
- 设备注册和认证
- 设备状态监控
- 设备配置管理
- **本地Mock模拟器**：无需真实设备即可开发

### 2. 光感数据采集模块
- 光感数据采集（Mock/真实）
- 数据存储和查询
- 数据统计分析
- 根据时间自动生成模拟光照数据

### 3. 智能窗帘控制模块
- 窗帘设备管理
- 窗帘控制指令
- 位置反馈
- 双窗帘独立控制

### 4. 自动化规则模块
- 规则配置
- 规则执行引擎
- 规则日志
- 光感阈值控制

## 🎯 本地化开发优势

- ✅ **完全本地化**：不依赖外部服务器（如 superacme.com）
- ✅ **BC4 Pro模拟器**：内置设备模拟，支持HTTP API和MQTT
- ✅ **光感数据生成**：根据时间自动生成模拟光照数据
- ✅ **灵活可控**：可以模拟各种场景和异常情况
- ✅ **易于测试**：支持自动化测试和压力测试
- ✅ **快速切换**：通过配置即可切换到真实设备

## 🔗 相关文档

- [本地化开发指南](./docs/本地化开发指南.md) - **推荐先阅读**
- [本地化开发方案](./docs/本地化开发方案.md) - 本地化架构设计
- [shenmou通信协议分析](./docs/shenmou_通信协议分析.md) - 协议分析
- [zervi.test微服务集成方案](./docs/zervi.test_微服务集成方案.md) - 微服务集成方案
- [BC4 Pro 光感窗帘集成方案](./BC4_Pro_光感窗帘集成方案.md) - 集成方案设计
- [智能窗帘差异化控制方案调研](./智能窗帘差异化控制方案调研.md) - 技术调研

## 🛠️ 开发状态

### ✅ 已完成
- [x] 项目框架搭建（Spring Boot + Vue 3）
- [x] 数据库设计和迁移脚本
- [x] BC4 Pro Mock设备模拟器
- [x] 本地化开发方案
- [x] Docker Compose配置
- [x] 文档完善

### 🚧 进行中
- [ ] 光感数据采集模块
- [ ] 智能窗帘控制模块
- [ ] 自动化规则引擎
- [ ] 前端界面开发

### 📋 计划中
- [ ] 集成到 zervi.test 微服务架构
- [ ] 真实设备通信实现
- [ ] 性能优化
- [ ] 生产环境部署

## 📝 许可证

本项目采用 MIT 许可证。

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

---

**项目状态**：框架搭建完成，准备开始功能开发 🚀
