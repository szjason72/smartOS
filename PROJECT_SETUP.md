# SmartOS 项目搭建完成指南

## ✅ 已完成的工作

### 1. 项目架构选择

**选择方案**：基于 **Spring Boot + Vue 3** 的自定义架构

**选择理由**：
- ✅ 前后端分离，易于开发和维护
- ✅ 完全可控，可以根据BC4 Pro需求深度定制
- ✅ 技术栈成熟，社区支持好
- ✅ 参考了 wumei-smart 的架构设计，但更灵活

### 2. 项目结构

```
smartOS/
├── backend/              # Spring Boot 后端
│   ├── src/main/java/com/smartos/
│   │   ├── SmartOsApplication.java  # 主应用类
│   │   ├── common/                  # 通用工具类
│   │   ├── config/                  # 配置类
│   │   ├── controller/              # 控制器
│   │   ├── service/                 # 业务服务
│   │   ├── repository/              # 数据访问
│   │   ├── model/                   # 数据模型
│   │   └── dto/                     # 数据传输对象
│   └── src/main/resources/
│       ├── application.yml          # 应用配置
│       └── db/migration/            # 数据库迁移脚本
│
├── frontend/            # Vue 3 前端
│   ├── src/
│   │   ├── api/                     # API接口
│   │   ├── components/              # 组件
│   │   ├── views/                   # 页面
│   │   ├── stores/                  # 状态管理
│   │   ├── router/                  # 路由
│   │   └── utils/                   # 工具函数
│   └── package.json
│
├── docs/                # 文档
├── scripts/             # 脚本
└── README.md
```

### 3. 技术栈

**后端**：
- Spring Boot 3.2.0
- PostgreSQL（数据库）
- Flyway（数据库迁移）
- Redis（缓存，可选）
- MQTT Client（设备通信）
- Swagger/OpenAPI（API文档）

**前端**：
- Vue 3 + TypeScript
- Element Plus（UI组件）
- Pinia（状态管理）
- Vue Router（路由）
- Axios（HTTP客户端）
- ECharts（图表）

### 4. 数据库设计

已创建以下表结构：
- `device_types` - 设备类型表
- `smart_devices` - 智能设备表
- `light_sensor_data` - 光感数据记录表
- `light_curtain_automation` - 光感窗帘联动配置表
- `light_sensor_stats` - 光感数据统计表

### 5. 配置文件

- ✅ `backend/pom.xml` - Maven依赖配置
- ✅ `backend/src/main/resources/application.yml` - Spring Boot配置
- ✅ `frontend/package.json` - NPM依赖配置
- ✅ `frontend/vite.config.ts` - Vite构建配置
- ✅ `frontend/tsconfig.json` - TypeScript配置

## 🚀 快速开始

### 1. 环境准备

```bash
# 检查Java版本（需要17+）
java -version

# 检查Node.js版本（需要18+）
node -v

# 检查PostgreSQL（需要14+）
psql --version
```

### 2. 数据库初始化

```bash
# 运行数据库初始化脚本
./scripts/init-db.sh

# 或者手动创建数据库
createdb smartos
```

### 3. 后端启动

```bash
cd backend

# 安装依赖（首次运行）
mvn clean install

# 启动服务
mvn spring-boot:run

# 后端将在 http://localhost:8080 启动
# Swagger文档: http://localhost:8080/swagger-ui.html
```

### 4. 前端启动

```bash
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev

# 前端将在 http://localhost:5173 启动
```

### 5. 一键启动（推荐）

```bash
# 使用启动脚本（需要先配置好环境）
./scripts/start.sh
```

## 📋 下一步开发计划

### 阶段一：BC4 Pro 设备管理模块（优先级：高）

1. **设备注册服务**
   - [ ] 实现设备注册接口
   - [ ] 实现设备认证机制
   - [ ] 实现设备状态监控

2. **BC4 Pro 通信客户端**
   - [ ] 分析 shenmou APP 通信协议
   - [ ] 实现HTTP/WebSocket客户端
   - [ ] 实现设备命令发送

### 阶段二：光感数据采集模块（优先级：高）

1. **数据采集服务**
   - [ ] 实现光感数据采集接口
   - [ ] 实现定时采集任务
   - [ ] 实现数据存储和查询

2. **图像分析（备选方案）**
   - [ ] 如果BC4 Pro不提供光感API，实现图像亮度分析
   - [ ] 集成图像处理库

### 阶段三：智能窗帘控制模块（优先级：中）

1. **窗帘设备管理**
   - [ ] 实现窗帘设备注册
   - [ ] 实现窗帘控制接口
   - [ ] 实现位置反馈

2. **控制逻辑**
   - [ ] 实现双窗帘独立控制
   - [ ] 实现精确位置控制
   - [ ] 实现场景预设

### 阶段四：自动化规则模块（优先级：中）

1. **规则管理**
   - [ ] 实现规则CRUD接口
   - [ ] 实现规则执行引擎
   - [ ] 实现防抖和延迟控制

2. **前端界面**
   - [ ] 实现规则配置界面
   - [ ] 实现实时数据展示
   - [ ] 实现统计图表

## 🔧 配置说明

### 后端配置（application.yml）

```yaml
smartos:
  bc4:
    api:
      base-url: http://localhost:8081  # BC4 Pro API地址
      timeout: 10000
    light-sensor:
      collection-interval: 60  # 采集间隔（秒）
      default-open-threshold: 200.0  # 开窗帘阈值（lux）
      default-close-threshold: 500.0  # 关窗帘阈值（lux）
  
  curtain:
    mqtt:
      broker-url: tcp://localhost:1883  # MQTT Broker地址
    control:
      debounce-seconds: 30  # 防抖时间（秒）
```

### 数据库配置

默认配置：
- 数据库名：`smartos`
- 用户名：`smartos`
- 密码：`smartos123`
- 端口：`5432`

可以在 `application.yml` 中修改。

## 📚 相关文档

- [BC4 Pro 光感窗帘集成方案](./BC4_Pro_光感窗帘集成方案.md)
- [智能窗帘差异化控制方案调研](./智能窗帘差异化控制方案调研.md)
- [BC4 Pro 设备适配文档](../TimesSquare/BC4_PRO_DEVICE_ADAPTATION.md)

## 🐛 常见问题

### 1. 数据库连接失败

**问题**：无法连接到PostgreSQL数据库

**解决**：
- 检查PostgreSQL服务是否启动
- 检查数据库用户名和密码是否正确
- 检查数据库是否存在

### 2. 端口被占用

**问题**：8080或5173端口被占用

**解决**：
- 修改 `application.yml` 中的 `server.port`
- 修改 `vite.config.ts` 中的 `server.port`

### 3. Maven依赖下载失败

**问题**：Maven依赖下载缓慢或失败

**解决**：
- 配置Maven镜像源（阿里云镜像）
- 检查网络连接

## 📝 开发规范

### 代码风格

- **后端**：遵循Spring Boot最佳实践
- **前端**：遵循Vue 3 Composition API规范
- **命名**：使用驼峰命名法

### Git提交规范

- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具相关

## 🎯 项目目标

1. ✅ 完成项目框架搭建
2. ⏳ 实现BC4 Pro设备管理
3. ⏳ 实现光感数据采集
4. ⏳ 实现智能窗帘控制
5. ⏳ 实现自动化规则引擎
6. ⏳ 完成前端界面开发

---

**项目状态**：框架搭建完成，准备开始功能开发 🚀
