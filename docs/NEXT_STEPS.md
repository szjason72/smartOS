# SmartOS 项目下一步开发计划

## 📊 当前项目状态

### ✅ 已完成（框架搭建阶段）

1. **项目架构**
   - ✅ Spring Boot 3 + Vue 3 框架搭建
   - ✅ 数据库设计和迁移脚本（PostgreSQL）
   - ✅ Docker Compose 配置（PostgreSQL + Mosquitto）
   - ✅ 本地化开发方案（不依赖外部服务器）

2. **BC4 Pro Mock 设备模拟器**
   - ✅ HTTP API 模拟（`BC4MockController`）
   - ✅ 光感数据生成（根据时间自动生成）
   - ✅ 设备状态管理
   - ✅ 设备命令接口

3. **基础设施**
   - ✅ Git 仓库初始化并推送到 GitHub
   - ✅ 文档完善（开发指南、集成方案等）
   - ✅ 启动脚本和初始化脚本

4. **设备兼容性确认** ⭐
   - ✅ 确认领普 CE1 支持小米生态（参考：[什么值得买评测](https://post.smzdm.com/zz/p/apwvr66x/)）
   - ✅ 下载 XiaomiGateway3 源码（参考代码）
   - ✅ 制定小米网关集成方案
   - ✅ 完成技术方案分析和文档

### 📋 待完成（功能开发阶段）

---

## 🎯 下一步开发计划（按优先级）

### 🔴 阶段一：核心功能开发（高优先级）

#### 1. 光感数据采集模块 ⭐⭐⭐

**目标**：实现光感数据的采集、存储和查询

**任务清单**：
- [ ] **数据模型层**
  - [ ] 创建 `LightSensorData` Entity（JPA）
  - [ ] 创建 `LightSensorDataRepository`
  - [ ] 创建 DTO 类

- [ ] **服务层**
  - [ ] 创建 `LightSensorService`
  - [ ] 实现从 BC4 Mock API 采集数据
  - [ ] 实现定时采集任务（@Scheduled）
  - [ ] 实现数据存储逻辑
  - [ ] 实现数据查询接口（按时间范围、设备ID等）

- [ ] **控制器层**
  - [ ] 创建 `LightSensorController`
  - [ ] 实现获取最新光感数据 API
  - [ ] 实现历史数据查询 API
  - [ ] 实现统计数据 API

- [ ] **MQTT 集成**（可选）
  - [ ] 实现 MQTT 订阅光感数据
  - [ ] 实现 MQTT 消息处理

**预计工作量**：2-3 天

**依赖**：
- ✅ BC4 Mock API（已完成）
- ✅ 数据库表结构（已完成）

---

#### 2. 小米网关集成模块 ⭐⭐⭐（新增）

**目标**：实现与小米多模网关的通信，控制领普 CE1 窗帘

**任务清单**：
- [ ] **网关通信基础**
  - [ ] 提取 XiaomiGateway3 核心代码（参考 `/Users/szjason72/Downloads/xiaomi-gateway3`）
  - [ ] 实现 MQTT 客户端（连接到小米网关）
  - [ ] 实现 Telnet/Shell 客户端（读取网关数据库）
  - [ ] 实现网关认证（Token 和 Key）

- [ ] **设备发现**
  - [ ] 实现 Mesh 设备发现（读取 `mesh_device_v3` 表）
  - [ ] 实现 BLE 设备发现（读取 `gateway_authed_table` 表）
  - [ ] 实现设备状态监听（MQTT 订阅）
  - [ ] 实现设备心跳处理

- [ ] **网关服务层**
  - [ ] 创建 `XiaomiGatewayService`
  - [ ] 实现网关连接管理
  - [ ] 实现设备列表查询
  - [ ] 实现设备状态同步

**预计工作量**：3-4 天

**依赖**：
- ✅ XiaomiGateway3 源码（已下载）
- ✅ 小米多模网关（Gateway 3/2 或 Aqara Hub E1）
- ⏳ 网关 Token 和 Key（需要获取）

**参考文档**：
- `docs/XiaomiGateway3_源码分析.md`
- `docs/领普CE1_小米生态集成方案.md`

---

#### 3. 智能窗帘控制模块 ⭐⭐⭐

**目标**：实现领普 CE1 窗帘的设备管理和控制（通过小米网关）

**任务清单**：
- [ ] **数据模型层**
  - [ ] 创建 `CurtainDevice` Entity
  - [ ] 创建 `CurtainDeviceRepository`
  - [ ] 创建窗帘控制命令 DTO

- [ ] **服务层**
  - [ ] 创建 `CurtainService`
  - [ ] 实现窗帘设备识别（从网关设备列表筛选）
  - [ ] 实现窗帘控制逻辑（开/关/位置控制）
  - [ ] 实现双窗帘独立控制
  - [ ] 实现缓开功能
  - [ ] 通过小米网关发送控制指令

- [ ] **控制器层**
  - [ ] 创建 `CurtainController`
  - [ ] 实现设备列表 API
  - [ ] 实现控制指令 API（打开/关闭/位置）
  - [ ] 实现状态查询 API

- [ ] **网关集成**
  - [ ] 集成小米网关服务
  - [ ] 实现控制指令发送（MQTT 到网关）
  - [ ] 实现状态订阅（从网关接收状态）

**预计工作量**：3-4 天

**依赖**：
- ⏳ 小米网关集成模块（必须先完成）
- ⏳ 光感数据采集模块（建议先完成）

**参考代码**：
- `xiaomi-gateway3/custom_components/xiaomi_gateway3/cover.py`

---

#### 4. 自动化规则引擎 ⭐⭐

**目标**：实现基于光感数据的自动化控制规则

**任务清单**：
- [ ] **数据模型层**
  - [ ] 创建 `AutomationRule` Entity
  - [ ] 创建 `AutomationHistory` Entity
  - [ ] 创建相关 Repository

- [ ] **服务层**
  - [ ] 创建 `AutomationService`
  - [ ] 实现规则 CRUD 操作
  - [ ] 实现规则执行引擎
  - [ ] 实现阈值判断逻辑
  - [ ] 实现防抖机制（避免频繁开关）
  - [ ] 实现规则执行日志记录

- [ ] **控制器层**
  - [ ] 创建 `AutomationController`
  - [ ] 实现规则管理 API
  - [ ] 实现规则执行历史查询 API

- [ ] **定时任务**
  - [ ] 实现规则检查定时任务
  - [ ] 集成光感数据采集和窗帘控制

**预计工作量**：3-4 天

**依赖**：
- ⏳ 光感数据采集模块
- ⏳ 智能窗帘控制模块

---

### 🟡 阶段二：前端界面开发（中优先级）

#### 5. 前端界面开发 ⭐⭐

**目标**：实现用户界面，展示数据和控制设备

**任务清单**：
- [ ] **设备管理页面**
  - [ ] BC4 Pro 设备列表
  - [ ] 设备状态展示
  - [ ] 设备配置界面

- [ ] **光感数据展示**
  - [ ] 实时光感数据展示
  - [ ] 历史数据图表（ECharts）
  - [ ] 数据统计面板

- [ ] **窗帘控制页面**
  - [ ] 窗帘设备列表
  - [ ] 窗帘控制面板（开/关/位置）
  - [ ] 双窗帘独立控制界面

- [ ] **自动化规则配置**
  - [ ] 规则列表
  - [ ] 规则创建/编辑表单
  - [ ] 规则执行历史

- [ ] **仪表盘**
  - [ ] 概览数据展示
  - [ ] 快速操作面板

**预计工作量**：5-7 天

**依赖**：
- ⏳ 后端 API 完成

---

### 🟢 阶段三：集成和优化（低优先级）

#### 6. 集成到 zervi.test 微服务架构 ⭐

**目标**：将功能集成到现有的微服务架构

**任务清单**：
- [ ] **创建 smart-home-service 微服务**
  - [ ] 在 zervi.test 项目中创建新服务
  - [ ] 适配代码到 MyBatis-Plus（从 JPA）
  - [ ] 配置 Eureka 服务注册
  - [ ] 配置 Gateway 路由

- [ ] **数据库迁移**
  - [ ] 将 PostgreSQL 迁移到 MySQL
  - [ ] 适配 SQL 语法差异

- [ ] **Docker Compose 集成**
  - [ ] 添加 smart-home-service 到 docker-compose.yml
  - [ ] 配置服务依赖

**预计工作量**：4-5 天

**依赖**：
- ✅ zervi.test 项目（已完成）
- ⏳ 核心功能开发完成

---

#### 7. 测试和优化 ⭐

**任务清单**：
- [ ] **单元测试**
  - [ ] Service 层测试
  - [ ] Controller 层测试
  - [ ] Repository 层测试

- [ ] **集成测试**
  - [ ] API 集成测试
  - [ ] MQTT 集成测试
  - [ ] 端到端测试

- [ ] **性能优化**
  - [ ] 数据库查询优化
  - [ ] 缓存策略（Redis）
  - [ ] 并发控制

- [ ] **文档完善**
  - [ ] API 文档（Swagger）
  - [ ] 部署文档
  - [ ] 用户手册

**预计工作量**：3-4 天

---

## 📅 开发时间线建议

### 第一周：核心功能开发
- **Day 1-2**：光感数据采集模块
- **Day 3-4**：小米网关集成模块（新增）
- **Day 5**：智能窗帘控制模块（基础）

### 第二周：继续核心功能
- **Day 1-2**：智能窗帘控制模块（完成）
- **Day 3-4**：自动化规则引擎
- **Day 5**：集成测试和调试

### 第三周：前端和集成
- **Day 1-3**：前端界面开发
- **Day 4-5**：集成到 zervi.test（可选）

### 第四周：测试和优化
- **Day 1-2**：测试
- **Day 3-4**：优化和文档
- **Day 5**：部署准备

---

## 🎯 立即可以开始的任务

### 推荐从以下任务开始：

#### 1. 光感数据采集模块（最优先）

**为什么优先**：
- ✅ 依赖最少（只需要 Mock API）
- ✅ 是其他模块的基础
- ✅ 可以快速验证架构设计

**第一步**：创建数据模型和服务层

```bash
# 创建 Entity
backend/src/main/java/com/smartos/model/LightSensorData.java

# 创建 Repository
backend/src/main/java/com/smartos/repository/LightSensorDataRepository.java

# 创建 Service
backend/src/main/java/com/smartos/service/LightSensorService.java
```

#### 2. 小米网关集成模块（新增）

**为什么第二**：
- ✅ 是窗帘控制的基础（必须先完成网关通信）
- ✅ 有完整的参考代码（XiaomiGateway3）
- ✅ 已确认领普 CE1 支持小米生态

**第一步**：提取和适配 XiaomiGateway3 核心代码

```bash
# 参考源码位置
/Users/szjason72/Downloads/xiaomi-gateway3

# 需要提取的核心文件
custom_components/xiaomi_gateway3/core/gate/mesh.py
custom_components/xiaomi_gateway3/core/gate/ble.py
custom_components/xiaomi_gateway3/core/mini_mqtt.py
custom_components/xiaomi_gateway3/core/shell/shell_mgw.py
```

#### 3. 智能窗帘控制模块

**为什么第三**：
- ✅ 依赖小米网关集成模块（必须先完成）
- ✅ 依赖光感数据（但可以先实现基础功能）
- ✅ 可以独立测试（通过小米网关控制真实设备）

---

## 🔧 技术债务和注意事项

### 需要注意的事项：

1. **数据库选择**
   - 当前使用 PostgreSQL
   - 集成到 zervi.test 时需要迁移到 MySQL
   - 建议：先完成功能，再迁移

2. **小米网关集成** ⭐（新增）
   - 已确认领普 CE1 支持小米生态
   - 需要实现与小米多模网关的通信
   - 需要获取网关 Token 和 Key
   - 建议：参考 XiaomiGateway3 代码实现
   - 参考文档：`docs/领普CE1_小米生态集成方案.md`

3. **MQTT 集成**
   - 当前配置了 Mosquitto（用于内部通信）
   - 需要实现 MQTT 客户端（连接到小米网关）
   - 建议：使用 Spring Integration MQTT 或 Eclipse Paho

4. **真实设备通信**
   - 当前使用 Mock 设备（BC4 Pro）
   - 后续需要实现真实 BC4 Pro 通信
   - 建议：保持 Mock 和真实设备的接口一致

---

## 📋 快速开始检查清单

### 开始开发前：

- [ ] 确认开发环境已配置
- [ ] 数据库已初始化
- [ ] Docker 服务已启动
- [ ] 后端服务可以正常启动
- [ ] Mock API 可以正常访问

### 开始第一个功能模块：

- [ ] 阅读相关文档
- [ ] 理解数据库表结构
- [ ] 创建分支：`git checkout -b feature/light-sensor-service`
- [ ] 开始实现数据模型层

---

## 🎯 总结

### 当前状态：
- ✅ **框架搭建完成**：可以开始功能开发
- ✅ **Mock 设备就绪**：可以独立开发，不依赖真实设备
- ✅ **文档完善**：有清晰的开发指南

### 下一步重点：
1. **光感数据采集模块**（最优先，2-3天）
2. **小米网关集成模块**（高优先级，3-4天）⭐ 新增
3. **智能窗帘控制模块**（高优先级，3-4天）
4. **自动化规则引擎**（中优先级，3-4天）

### 重要更新：
- ✅ **领普 CE1 支持小米生态已确认**
- ✅ **XiaomiGateway3 源码已下载**
- ✅ **集成方案已制定**
- 📋 **需要获取小米网关 Token 和 Key**

### 建议：
- 🎯 **先完成核心功能**，再考虑前端和集成
- 🎯 **保持代码质量**，编写测试和文档
- 🎯 **使用分支开发**，通过 PR 合并代码

---

**准备好开始开发了吗？建议从光感数据采集模块开始！** 🚀

---

## 📚 相关文档

### 集成方案文档
- [领普 CE1 适配性分析](./领普CE1_BC4Pro_适配性分析.md) - 设备兼容性分析
- [XiaomiGateway3 源码分析](./XiaomiGateway3_源码分析.md) - 参考代码分析
- [领普 CE1 小米生态集成方案](./领普CE1_小米生态集成方案.md) - 详细集成方案 ⭐

### 技术文档
- [BC4 Pro 光感窗帘集成方案](../BC4_Pro_光感窗帘集成方案.md) - 整体集成方案
- [shenmou 通信协议分析](./shenmou_通信协议分析.md) - BC4 Pro 通信协议
- [本地化开发指南](./本地化开发指南.md) - 开发环境设置

### 参考资源
- **XiaomiGateway3 源码**：`/Users/szjason72/Downloads/xiaomi-gateway3`
- **XiaomiGateway3 GitHub**：[https://github.com/AlexxIT/XiaomiGateway3](https://github.com/AlexxIT/XiaomiGateway3)
- **领普 CE1 评测**：[什么值得买](https://post.smzdm.com/zz/p/apwvr66x/)
