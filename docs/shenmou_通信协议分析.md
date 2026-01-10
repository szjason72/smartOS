# shenmou APP 通信协议分析报告

## 📋 概述

本文档基于对 shenmou APP v2.7.5（神眸摄像头APP）的反编译分析，总结其与 BC4 Pro 设备的通信协议和架构。

---

## 🔍 技术栈分析

### 1. 网络通信库

从反编译的代码中识别出以下网络通信库：

#### HTTP/REST API
- **Retrofit2**：用于 RESTful API 调用
- **OkHttp**：HTTP 客户端（Retrofit2 的底层实现）
- **RxJava**：响应式编程，用于异步处理

#### MQTT 协议
- **Eclipse Paho MQTT Client**：用于 MQTT 消息通信
- 支持 MQTT 3.1.1 协议

#### WebSocket
- **Java WebSocket**：用于实时双向通信

#### CoAP 协议
- **Eclipse Californium**：用于 CoAP 设备发现和通信

### 2. 云平台集成

#### 阿里云 IoT 平台（Alink）
- **设备配网**：支持多种配网方式
  - SoftAP（软AP）
  - BLE（蓝牙）
  - Mesh（Mesh网络）
  - QR Code（二维码）
  - Broadcast（广播）
  - Zero Config（零配置）

- **设备发现**：
  - CoAP 发现
  - Cloud 发现
  - BLE 发现
  - Mesh 发现

#### 华为云服务
- **GRS（Global Route Service）**：全球路由服务
- **Analytics**：数据分析服务

---

## 🌐 API 端点分析

### 1. 服务器地址

#### SuperAcme 服务器（主要服务器）

**开发环境**：
- `https://mall-cn-dev.superacme.com`
- `https://app-cn-dev.superacme.com`

**测试环境**：
- `https://mall-cn-test.superacme.com`
- `https://app-cn-test.superacme.com`

**生产环境**（推测）：
- `https://mall-cn.superacme.com`
- `https://app-cn.superacme.com`

#### 其他服务

**视频流服务**：
- `https://stream.feedcoopapi.com/user/event/log/video/v1/`

**华为云服务**：
- `https://metrics1-drcn.dt.dbankcloud.cn:443`（中国区）
- `https://metrics-dra.dt.hicloud.com:6447`（亚洲/非洲/拉美）
- `https://metrics2.data.hicloud.com:6447`（欧洲）
- `https://metrics5.data.hicloud.com:6447`（俄罗斯）

### 2. API 接口（从配置文件推断）

#### 设备管理相关

**设备状态查询**：
```
GET /cloudservicestatus?deviceId={deviceId}
```

**设备分享**：
```
GET /sharedevice?deviceId={deviceId}
```

**电池分析**：
```
GET /analysisbattery?deviceId={deviceId}
```

**WiFi配置**：
```
POST /wificonfig/activity?pk={productKey}&dn={deviceName}
```

---

## 📡 通信协议架构

### 1. 设备配网流程

```
APP
  ↓
[配网策略选择]
  ├─ SoftAP（软AP）
  ├─ BLE（蓝牙）
  ├─ Mesh（Mesh网络）
  ├─ QR Code（二维码）
  └─ Broadcast（广播）
  ↓
[设备发现]
  ├─ CoAP 发现
  ├─ Cloud 发现
  └─ BLE 发现
  ↓
[设备注册]
  ↓
[云端绑定]
```

### 2. 设备通信方式

#### 方式一：HTTP/REST API（主要方式）

**用途**：
- 设备状态查询
- 设备配置管理
- 设备控制命令

**特点**：
- 使用 Retrofit2 + OkHttp
- 支持 JSON 格式
- 需要认证（Token）

**示例请求**：
```http
GET /api/device/status?deviceId=BC4_XXXXX
Authorization: Bearer {token}
Content-Type: application/json
```

#### 方式二：MQTT（实时通信）

**用途**：
- 设备状态推送
- 实时控制命令
- 事件通知

**特点**：
- 使用 Eclipse Paho MQTT Client
- 支持 QoS 0/1/2
- 支持保留消息

**MQTT Topic 结构**（推测）：
```
/superacme/{productKey}/{deviceName}/property/set    # 设备属性设置
/superacme/{productKey}/{deviceName}/property/get   # 设备属性获取
/superacme/{productKey}/{deviceName}/event          # 设备事件
/superacme/{productKey}/{deviceName}/service        # 设备服务调用
```

#### 方式三：WebSocket（实时双向通信）

**用途**：
- 视频流控制
- 实时数据推送
- 双向通信

#### 方式四：CoAP（设备发现）

**用途**：
- 局域网设备发现
- 本地设备通信

---

## 🔐 认证机制

### 1. Token 认证

**HTTP 请求头**：
```http
Authorization: Bearer {access_token}
```

### 2. 设备认证

**设备Token**：
- 设备注册时生成
- 用于设备与云端通信

**设备密钥**：
- 用于加密通信
- 存储在设备本地

---

## 📊 数据格式

### 1. 请求格式

**HTTP 请求**：
```json
{
  "deviceId": "BC4_XXXXX",
  "command": "getStatus",
  "params": {
    "property": "lightSensor"
  }
}
```

### 2. 响应格式

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "lightValue": 350.5,
    "timestamp": "2024-01-01T12:00:00Z"
  }
}
```

**错误响应**：
```json
{
  "code": 500,
  "message": "device offline",
  "data": null
}
```

---

## 🎯 BC4 Pro 特定功能

### 1. 设备类型

**设备标识**：
- `deviceType`: `CINMOORE_BC4`
- `deviceTypeCode`: `cameraIconBC4`
- `deviceName`: `BC4`

### 2. 配网方式

**默认配网方式**：`ALI_SOFT_AP`（阿里云软AP）

**配网流程**：
1. 设备进入配网模式
2. APP 连接设备热点
3. 发送 WiFi 配置信息
4. 设备连接 WiFi
5. 设备注册到云端

### 3. 光感相关功能

**配置项**（从 `LightSettingV3.json` 分析）：
- `StatusLightSwitch`：状态指示灯开关
- `AlarmLightSwitch`：报警闪光灯开关

**光感数据获取方式**（推测）：
1. **直接API获取**：如果BC4 Pro提供光感API
   ```
   GET /api/device/{deviceId}/lightSensor
   ```

2. **MQTT订阅**：
   ```
   Topic: /superacme/{productKey}/{deviceName}/property/lightSensor
   ```

3. **图像分析**：通过图像亮度分析获取光照信息

---

## 🔧 实现建议

### 1. 通信客户端实现

#### HTTP 客户端

```java
// 使用 Retrofit2 实现
public interface BC4DeviceApi {
    @GET("/api/device/{deviceId}/status")
    Call<DeviceStatus> getDeviceStatus(@Path("deviceId") String deviceId);
    
    @POST("/api/device/{deviceId}/command")
    Call<CommandResponse> sendCommand(
        @Path("deviceId") String deviceId,
        @Body CommandRequest request
    );
    
    @GET("/api/device/{deviceId}/lightSensor")
    Call<LightSensorData> getLightSensorValue(@Path("deviceId") String deviceId);
}
```

#### MQTT 客户端

```java
// 使用 Eclipse Paho MQTT Client
MqttClient client = new MqttClient(
    "tcp://mqtt.superacme.com:1883",
    "smartos-client-" + UUID.randomUUID()
);

MqttConnectOptions options = new MqttConnectOptions();
options.setUserName("your-username");
options.setPassword("your-password".toCharArray());
client.connect(options);

// 订阅光感数据
String topic = "/superacme/{productKey}/{deviceName}/property/lightSensor";
client.subscribe(topic, (topic, message) -> {
    // 处理光感数据
    LightSensorData data = parseMessage(message);
});
```

### 2. 认证实现

```java
// Token 管理
public class AuthManager {
    private String accessToken;
    
    public void setAccessToken(String token) {
        this.accessToken = token;
    }
    
    public String getAuthHeader() {
        return "Bearer " + accessToken;
    }
}

// Retrofit 拦截器
public class AuthInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
            .header("Authorization", AuthManager.getInstance().getAuthHeader());
        return chain.proceed(builder.build());
    }
}
```

### 3. 光感数据采集

```java
// 定时采集任务
@Scheduled(fixedRate = 60000) // 每分钟采集一次
public void collectLightSensorData() {
    List<BC4Device> devices = deviceService.getBC4Devices();
    
    for (BC4Device device : devices) {
        try {
            // 方式1：HTTP API
            LightSensorData data = bc4Api.getLightSensorValue(device.getDeviceId());
            lightSensorService.save(data);
            
            // 方式2：MQTT订阅（已在连接时订阅）
            // 数据通过回调接收
            
        } catch (Exception e) {
            log.error("采集光感数据失败: {}", device.getDeviceId(), e);
        }
    }
}
```

---

## 📝 注意事项

### 1. 服务器地址

- **开发/测试环境**：使用 `dev` 或 `test` 域名
- **生产环境**：需要实际测试确定域名
- **MQTT Broker**：需要确定实际的MQTT服务器地址

### 2. 认证Token

- Token 获取方式需要进一步分析
- Token 有效期和刷新机制需要确认
- 设备Token和设备密钥的生成方式需要确认

### 3. 协议版本

- API 版本可能随APP更新而变化
- 需要兼容性测试
- 建议实现版本检测机制

### 4. 加密和安全

- 通信可能使用HTTPS/TLS加密
- 敏感数据可能需要额外加密
- 需要分析证书验证机制

---

## 🔗 相关资源

### 配置文件位置

- `assets/config.conf` - 配网策略配置
- `assets/YJSDeviceTypesConfig.json` - 设备类型配置
- `assets/LightSettingV3.json` - 光照设置配置
- `assets/grs_sdk_server_config.json` - 服务器路由配置

### 参考文档

- [阿里云 IoT 平台文档](https://help.aliyun.com/product/30520.html)
- [Eclipse Paho MQTT 文档](https://www.eclipse.org/paho/)
- [Retrofit2 文档](https://square.github.io/retrofit/)

---

## 🎯 下一步行动

1. **实际抓包分析**
   - 使用 Charles/Fiddler 抓取APP的网络请求
   - 分析实际的API端点和数据格式
   - 确认认证机制

2. **MQTT连接测试**
   - 尝试连接MQTT服务器
   - 分析Topic结构
   - 测试消息订阅和发布

3. **设备通信测试**
   - 使用实际BC4 Pro设备测试
   - 验证光感数据获取方式
   - 测试设备控制命令

4. **SDK集成**
   - 集成阿里云IoT SDK（如果需要）
   - 实现设备配网功能
   - 实现设备管理功能

---

**分析日期**：2024-01-XX  
**APP版本**：v2.7.5  
**分析状态**：初步分析完成，需要实际测试验证
