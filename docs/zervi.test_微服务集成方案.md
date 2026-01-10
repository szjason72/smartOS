# BC4 Pro 光感窗帘控制 - zervi.test 微服务集成方案

## 📋 概述

将 BC4 Pro 光感智能窗帘控制功能集成到现有的 **zervi.test** 微服务架构中。

**目标**：
- 利用现有的微服务基础设施（Eureka、Gateway、Docker等）
- 创建新的微服务或扩展现有服务
- 保持架构一致性和可扩展性

---

## 🏗️ zervi.test 架构分析

### 现有架构

```
Nginx (80/443)
    ↓
Spring Cloud Gateway (9000)
    ↓
Eureka Server (8761)
    ↓
┌─────────────────────────────────────┐
│  11个微服务                          │
│  - resume-service (9205)            │
│  - personal-service (9207)          │
│  - enterprise-service (9206)        │
│  - admin-service (8085)             │
│  - points-service (9203)            │
│  - statistics-service (9202)        │
│  - resource-service (9201)          │
│  - blockchain-service (9210)        │
│  - open-api-service (9208)          │
│  - api-gateway-service (9000)       │
│  - eureka-server (8761)             │
└─────────────────────────────────────┘
    ↓
MySQL + Redis
```

### 技术栈

- **Java 17**
- **Spring Boot 2.7.14**
- **Spring Cloud 2021.0.9**
- **MyBatis-Plus 3.5.2**
- **MySQL 8.0**
- **Redis**
- **Eureka**（服务注册与发现）
- **Spring Cloud Gateway**（API网关）

---

## 🎯 集成方案

### 方案一：创建新微服务（推荐）⭐

**创建 `smart-home-service` 微服务**

**优势**：
- ✅ 功能独立，职责清晰
- ✅ 易于扩展和维护
- ✅ 不影响现有服务
- ✅ 可以独立部署和扩展

**端口分配**：`9211`

**功能模块**：
1. BC4 Pro 设备管理
2. 光感数据采集
3. 智能窗帘控制
4. 自动化规则引擎

### 方案二：扩展现有服务

**选项A：集成到 `resource-service`**
- 理由：资源管理相关
- 缺点：职责不够清晰

**选项B：集成到 `open-api-service`**
- 理由：开放API服务
- 缺点：可能影响现有API

**不推荐方案二**，因为：
- 职责混乱
- 耦合度高
- 不利于扩展

---

## 🔧 实施方案（方案一）

### 1. 创建 smart-home-service 微服务

#### 目录结构

```
zervi.test/backend/smart-home-service/
├── src/
│   ├── main/
│   │   ├── java/com/zervi/smarthome/
│   │   │   ├── SmartHomeApplication.java
│   │   │   ├── config/              # 配置类
│   │   │   ├── controller/          # 控制器
│   │   │   │   ├── BC4DeviceController.java
│   │   │   │   ├── LightSensorController.java
│   │   │   │   ├── CurtainController.java
│   │   │   │   └── AutomationController.java
│   │   │   ├── service/             # 业务服务
│   │   │   │   ├── BC4DeviceService.java
│   │   │   │   ├── LightSensorService.java
│   │   │   │   ├── CurtainService.java
│   │   │   │   └── AutomationService.java
│   │   │   ├── mapper/              # MyBatis Mapper
│   │   │   ├── model/               # 数据模型
│   │   │   └── dto/                 # 数据传输对象
│   │   └── resources/
│   │       ├── application.yml
│   │       └── mapper/              # MyBatis XML
│   └── test/
├── Dockerfile
└── pom.xml
```

#### 数据库设计

**创建数据库**：
```sql
CREATE DATABASE zervi_smarthome DEFAULT CHARSET utf8mb4;
```

**表结构**（复用之前的设计）：
- `device_types` - 设备类型表
- `smart_devices` - 智能设备表
- `light_sensor_data` - 光感数据记录表
- `light_curtain_automation` - 光感窗帘联动配置表
- `light_sensor_stats` - 光感数据统计表

#### 服务配置

**application.yml**：
```yaml
server:
  port: 9211

spring:
  application:
    name: smart-home-service
  
  datasource:
    url: jdbc:mysql://mysql:3306/zervi_smarthome?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  # Eureka配置
  cloud:
    nacos:
      discovery:
        server-addr: eureka-server:8761

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90

# 应用配置
smartos:
  local:
    enabled: true
    mock-device:
      enabled: true
      base-url: http://localhost:9211/mock/bc4
  
  bc4:
    api:
      base-url: http://localhost:9211/mock/bc4
      timeout: 10000
  
  mqtt:
    broker-url: tcp://mosquitto:1883
    client-id: smart-home-service
```

#### pom.xml 依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <parent>
        <groupId>com.zervi</groupId>
        <artifactId>zervi-parent</artifactId>
        <version>1.0.0</version>
    </parent>
    
    <artifactId>smart-home-service</artifactId>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Eureka Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        
        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>
        
        <!-- MySQL -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        
        <!-- MQTT Client -->
        <dependency>
            <groupId>org.eclipse.paho</groupId>
            <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
            <version>1.2.5</version>
        </dependency>
        
        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
    </dependencies>
</project>
```

### 2. 集成到 API Gateway

#### Gateway 路由配置

在 `api-gateway-service` 中添加路由：

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Smart Home Service 路由
        - id: smart-home-service
          uri: lb://smart-home-service
          predicates:
            - Path=/api/smart-home/**
          filters:
            - StripPrefix=2
```

### 3. Docker Compose 配置

在 `docker-compose.yml` 中添加服务：

```yaml
  # Smart Home Service
  smart-home-service:
    build: ./backend/smart-home-service
    container_name: zervi-smart-home
    ports:
      - "9211:9211"
    environment:
      - JAVA_OPTS=-Xmx512m -Xms256m
      - EUREKA_SERVER=http://eureka-server:8761/eureka
      - MYSQL_HOST=mysql
      - MQTT_BROKER=mosquitto
    networks:
      - zervi-network
    depends_on:
      - eureka-server
      - mysql
      - mosquitto
    restart: unless-stopped
```

### 4. 添加 MQTT Broker（Mosquitto）

在 `docker-compose.yml` 中添加：

```yaml
  # Mosquitto MQTT Broker
  mosquitto:
    image: eclipse-mosquitto:latest
    container_name: zervi-mosquitto
    ports:
      - "1883:1883"
    volumes:
      - ./mosquitto/config:/mosquitto/config
      - ./mosquitto/data:/mosquitto/data
    networks:
      - zervi-network
    restart: unless-stopped
```

---

## 📊 服务间通信

### 1. 服务发现（Eureka）

所有服务通过 Eureka 注册和发现：

```java
@RestController
public class BC4DeviceController {
    
    @Autowired
    private EurekaClient eurekaClient;
    
    // 通过服务名调用其他服务
    @GetMapping("/devices/{id}")
    public DeviceInfo getDevice(@PathVariable String id) {
        // 使用 Eureka 服务发现
        InstanceInfo instance = eurekaClient.getNextServerFromEureka("resource-service", false);
        String url = instance.getHomePageUrl();
        // 调用其他服务...
    }
}
```

### 2. Feign 客户端（推荐）

使用 Feign 进行服务间调用：

```java
@FeignClient(name = "statistics-service")
public interface StatisticsServiceClient {
    
    @PostMapping("/api/statistics/record")
    void recordEvent(@RequestBody EventData data);
}
```

### 3. MQTT 消息通信

用于设备实时通信：

```java
@Component
public class MqttMessageHandler {
    
    @Autowired
    private MqttClient mqttClient;
    
    public void publishLightSensorData(String deviceId, double lightValue) {
        String topic = "/smart-home/bc4/" + deviceId + "/lightSensor";
        // 发布消息...
    }
}
```

---

## 🔄 数据流设计

### 光感数据采集流程

```
BC4 Pro设备（Mock/真实）
    ↓ HTTP/MQTT
smart-home-service (9211)
    ↓ 数据存储
MySQL (zervi_smarthome)
    ↓ 触发自动化规则
AutomationService
    ↓ 控制指令
CurtainService
    ↓ MQTT
智能窗帘设备
```

### 服务调用流程

```
前端请求
    ↓
Nginx (80)
    ↓
Spring Cloud Gateway (9000)
    ↓ 路由到
smart-home-service (9211)
    ↓ 需要调用其他服务时
Feign Client
    ↓
其他微服务（如 statistics-service）
```

---

## 📝 实施步骤

### 阶段一：创建微服务基础结构（1-2天）

1. ✅ 创建 `smart-home-service` 目录结构
2. ✅ 配置 `pom.xml` 和 `application.yml`
3. ✅ 创建主应用类
4. ✅ 配置 Eureka 客户端

### 阶段二：实现核心功能（3-5天）

1. ✅ BC4 Pro 设备管理模块
2. ✅ 光感数据采集模块
3. ✅ 智能窗帘控制模块
4. ✅ 自动化规则引擎

### 阶段三：集成和测试（2-3天）

1. ✅ 集成到 API Gateway
2. ✅ 配置 Docker Compose
3. ✅ 服务间通信测试
4. ✅ 端到端测试

### 阶段四：部署和优化（1-2天）

1. ✅ Docker 镜像构建
2. ✅ 部署脚本更新
3. ✅ 性能优化
4. ✅ 文档完善

---

## 🎯 API 设计

### RESTful API 端点

```
# BC4 Pro 设备管理
GET    /api/smart-home/bc4/devices              # 设备列表
GET    /api/smart-home/bc4/devices/{id}         # 设备详情
POST   /api/smart-home/bc4/devices              # 注册设备
PUT    /api/smart-home/bc4/devices/{id}         # 更新设备
DELETE /api/smart-home/bc4/devices/{id}         # 删除设备

# 光感数据
GET    /api/smart-home/light-sensor/{deviceId}  # 获取光感数据
POST   /api/smart-home/light-sensor/collect     # 采集光感数据
GET    /api/smart-home/light-sensor/stats       # 统计数据

# 智能窗帘
GET    /api/smart-home/curtain/devices          # 窗帘设备列表
POST   /api/smart-home/curtain/{id}/control     # 控制窗帘
GET    /api/smart-home/curtain/{id}/status      # 窗帘状态

# 自动化规则
GET    /api/smart-home/automation/rules         # 规则列表
POST   /api/smart-home/automation/rules         # 创建规则
PUT    /api/smart-home/automation/rules/{id}    # 更新规则
DELETE /api/smart-home/automation/rules/{id}    # 删除规则
```

---

## 🔧 配置更新清单

### 1. 数据库

```sql
-- 创建数据库
CREATE DATABASE zervi_smarthome DEFAULT CHARSET utf8mb4;

-- 执行迁移脚本（从 smartOS 项目复制）
-- V1__init_schema.sql
```

### 2. API Gateway 路由

在 `api-gateway-service/src/main/resources/application.yml` 中添加：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: smart-home-service
          uri: lb://smart-home-service
          predicates:
            - Path=/api/smart-home/**
          filters:
            - StripPrefix=2
```

### 3. Docker Compose

在 `docker-compose.yml` 中添加：
- `smart-home-service` 服务
- `mosquitto` MQTT Broker（如果还没有）

### 4. Nginx 配置（如果需要）

在 `nginx/conf.d/api.conf` 中确保路由正确。

---

## 📚 代码复用

### 从 smartOS 项目复用

可以直接复用的代码：

1. **数据库迁移脚本**
   - `backend/src/main/resources/db/migration/V1__init_schema.sql`

2. **Mock Controller**
   - `backend/src/main/java/com/smartos/mock/device/BC4MockController.java`
   - 适配到 zervi.test 的包结构

3. **Service 层逻辑**
   - 业务逻辑可以复用，但需要适配 MyBatis-Plus

4. **DTO 和 Model**
   - 数据模型可以直接复用

### 需要适配的部分

1. **数据库访问层**
   - smartOS 使用 JPA，zervi.test 使用 MyBatis-Plus
   - 需要重写 Repository/Mapper

2. **配置类**
   - 适配到 Spring Cloud 配置

3. **服务发现**
   - 添加 Eureka 客户端配置

---

## ✅ 优势总结

### 1. 架构一致性
- ✅ 遵循现有微服务架构模式
- ✅ 使用相同的技术栈
- ✅ 统一的配置管理

### 2. 可扩展性
- ✅ 独立服务，易于扩展
- ✅ 可以独立部署和扩展
- ✅ 不影响现有服务

### 3. 可维护性
- ✅ 职责清晰
- ✅ 代码复用
- ✅ 统一的管理和监控

### 4. 生产就绪
- ✅ 利用现有的 Docker 部署
- ✅ 利用现有的监控和日志
- ✅ 利用现有的网关和负载均衡

---

## 🚀 快速开始

### 1. 创建微服务

```bash
cd /Users/szjason72/poetry-watchtow/zervi.test/backend

# 复制微服务模板（如果有）
# 或手动创建 smart-home-service 目录
```

### 2. 复制代码

```bash
# 从 smartOS 项目复制相关代码
cp -r /Users/szjason72/smartOS/backend/src/main/java/com/smartos/mock \
  /Users/szjason72/poetry-watchtow/zervi.test/backend/smart-home-service/src/main/java/com/zervi/smarthome/
```

### 3. 适配代码

- 修改包名：`com.smartos` → `com.zervi.smarthome`
- 适配数据库访问：JPA → MyBatis-Plus
- 添加 Eureka 客户端配置

### 4. 启动服务

```bash
# 启动所有服务（包括新的 smart-home-service）
docker-compose up -d

# 或单独启动
cd backend/smart-home-service
mvn spring-boot:run
```

---

## 📝 注意事项

### 1. 数据库差异

- **smartOS**：PostgreSQL + Flyway
- **zervi.test**：MySQL + MyBatis-Plus

需要：
- 修改 SQL 语法（PostgreSQL → MySQL）
- 使用 MyBatis-Plus 替代 JPA

### 2. 配置管理

- **smartOS**：单应用配置
- **zervi.test**：Spring Cloud 配置中心（可选）

建议：
- 先使用本地配置
- 后续可以迁移到配置中心

### 3. 服务发现

- 确保 `smart-home-service` 注册到 Eureka
- 确保 Gateway 路由配置正确

---

## 🎯 总结

**完全可以在 zervi.test 微服务架构上实现 BC4 Pro 光感窗帘控制功能！**

**推荐方案**：
- ✅ 创建新的 `smart-home-service` 微服务
- ✅ 复用 smartOS 项目的业务逻辑
- ✅ 适配到 zervi.test 的技术栈
- ✅ 集成到现有的微服务生态

**预计工作量**：7-12天（分阶段实施）

---

**下一步**：开始创建 `smart-home-service` 微服务！
