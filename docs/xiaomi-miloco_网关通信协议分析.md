# Xiaomi Miloco 网关通信协议分析

## 📋 项目概述

**项目名称**：Xiaomi Miloco（小米本地 Copilot）  
**项目路径**：`/Users/szjason72/Downloads/xiaomi-miloco-main`  
**项目类型**：智能家居本地 Copilot 系统  
**开源时间**：2025-11

**核心功能**：
- 以米家摄像机为视觉信息来源
- 使用自研 LLM 作为核心
- 打通全屋 IoT 设备
- 支持自然语言定义家庭规则和设备控制

---

## 🔍 网关通信协议模块分析

### 1. 项目结构

```
xiaomi-miloco-main/
├── miot_kit/              # MIoT 客户端库（核心）
│   ├── miot/
│   │   ├── client.py      # MIoT 客户端（主要入口）
│   │   ├── cloud.py       # 云端 HTTP API 客户端
│   │   ├── lan.py         # 局域网设备通信
│   │   ├── network.py     # 网络工具类
│   │   ├── camera.py      # 摄像头相关
│   │   ├── types.py       # 类型定义
│   │   └── specs/         # 设备规格定义
│   │       ├── spec_modify.yaml
│   │       └── spec_filter.yaml
│   └── tests/
│
├── miloco_server/         # 服务器端
│   ├── service/
│   │   └── miot_service.py
│   └── proxy/
│       └── miot_proxy.py
│
└── miloco_ai_engine/     # AI 引擎
```

---

### 2. 网关相关代码位置

#### ✅ 找到的网关相关内容

**位置1：设备规格定义**
- `miot_kit/miot/specs/spec_modify.yaml`
- `miot_kit/miot/specs/spec_filter.yaml`

**网关设备定义**：
```yaml
urn:miot-spec-v2:device:gateway:0000A019:xiaomi-hub1:1:
  prop.2.1:
    name: access-mode
    access:
      - read
      - notify
  prop.2.2:
    name: ip-address
    icon: mdi:ip
  prop.2.3:
    name: wifi-ssid
    access:
      - read
      - notify
```

**位置2：测试代码**
- `miot_kit/tests/test_cloud.py`

**测试代码片段**：
```python
# Find central hub gateway, control its indicator light switch
if dev["model"] == "xiaomi.gateway.hub1":
    # 控制网关指示灯开关
```

**位置3：云端 API**
- `miot_kit/miot/cloud.py`

**网关相关 API**：
```python
# AppGateway API
url_path="/app/appgateway/miot/appsceneservice/AppSceneService/GetManualSceneList"
url_path="/app/appgateway/miot/appsceneservice/AppSceneService/NewRunScene"
```

---

### 3. 通信协议架构

#### 3.1 通信方式

**方式一：云端 HTTP API（主要方式）**

```python
# miot_kit/miot/cloud.py
class MIoTHttpClient:
    """MIoT HTTP 客户端"""
    
    # 设备控制
    async def get_property_async(...)
    async def set_property_async(...)
    async def call_action_async(...)
    
    # 场景控制
    async def get_manual_scene_list_async(...)
    async def new_run_scene_async(...)
```

**特点**：
- ✅ 使用 OAuth2 认证
- ✅ 支持加密通信（RSA + AES）
- ✅ 通过 AppGateway 访问设备
- ✅ 支持设备属性读写、动作调用

**方式二：局域网通信（LAN）**

```python
# miot_kit/miot/lan.py
class MIoTLan:
    """MIoT 局域网设备通信"""
    
    # 设备发现
    async def register_device_async(...)
    async def unregister_device_async(...)
    
    # 设备状态监听
    async def register_device_status_changed_async(...)
```

**特点**：
- ✅ 局域网内直接通信
- ✅ 支持设备自动发现
- ✅ 支持设备状态实时监听
- ✅ 使用 UDP 多播发现设备

**方式三：摄像头流媒体**

```python
# miot_kit/miot/camera.py
class MIoTCamera:
    """MIoT 摄像头客户端"""
    
    # 视频流
    async def get_video_stream_async(...)
```

---

### 4. 网关设备支持

#### 4.1 支持的网关类型

**小米网关 Hub1**：
- **设备模型**：`xiaomi.gateway.hub1`
- **设备 URN**：`urn:miot-spec-v2:device:gateway:0000A019:xiaomi-hub1:1`
- **支持属性**：
  - `access-mode`：访问模式（读写、通知）
  - `ip-address`：IP 地址
  - `wifi-ssid`：WiFi SSID（读写、通知）

#### 4.2 网关功能

**通过网关控制子设备**：
- ✅ 网关作为中继，控制蓝牙 Mesh 设备
- ✅ 网关作为中继，控制 ZigBee 设备
- ✅ 网关作为中继，控制 WiFi 设备

**网关自身功能**：
- ✅ 指示灯控制
- ✅ 网络配置查询
- ✅ 场景触发

---

### 5. 蓝牙 Mesh 支持情况

#### ⚠️ 未找到直接的蓝牙 Mesh 协议实现

**搜索结果**：
- ❌ 未找到 `bluetooth`、`mesh`、`BLE` 相关的协议实现代码
- ❌ 未找到蓝牙 Mesh 设备直接控制的代码
- ✅ 找到网关设备定义（`xiaomi-hub1`）

**结论**：
- ✅ **网关设备支持**：支持小米网关 Hub1 作为设备
- ⚠️ **蓝牙 Mesh 协议**：**未直接实现蓝牙 Mesh 协议栈**
- ✅ **间接支持**：通过网关控制蓝牙 Mesh 子设备（网关作为中继）

---

### 6. 设备控制流程

#### 6.1 云端控制流程

```
SmartOS 后端
    ↓ HTTP/HTTPS
小米云端 API (AppGateway)
    ↓ 网关中继
小米网关 Hub1
    ↓ 蓝牙 Mesh / ZigBee
子设备（如：领普 CE1）
```

#### 6.2 局域网控制流程

```
SmartOS 后端
    ↓ UDP 多播发现
局域网内设备
    ↓ 直接 TCP/UDP 通信
设备（如果支持 LAN 协议）
```

---

### 7. 对 SmartOS 项目的价值

#### ✅ 可用部分

**1. MIoT 客户端库**
- ✅ 可以复用 `miot_kit` 作为小米设备控制客户端
- ✅ 支持 OAuth2 认证
- ✅ 支持设备属性读写、动作调用
- ✅ 支持场景控制

**2. 网关控制**
- ✅ 可以通过小米网关 Hub1 控制蓝牙 Mesh 设备
- ✅ 支持网关属性查询和控制

**3. 设备发现**
- ✅ 支持局域网设备自动发现
- ✅ 支持设备状态实时监听

#### ⚠️ 限制

**1. 蓝牙 Mesh 协议**
- ⚠️ **未直接实现蓝牙 Mesh 协议栈**
- ⚠️ 需要通过网关中继控制蓝牙 Mesh 设备
- ⚠️ 无法直接与领普 CE1 通信（如果领普 CE1 不支持小米生态）

**2. 协议兼容性**
- ⚠️ 领普 CE1 使用**蓝牙 Mesh 2.0**协议
- ⚠️ 小米网关可能只支持**蓝牙 Mesh 1.0**或特定协议
- ⚠️ 需要确认领普 CE1 是否支持小米生态

---

### 8. 集成建议

#### 方案一：通过小米网关集成（如果兼容）⭐

**架构**：
```
BC4 Pro (光感数据源)
    ↓ HTTP/MQTT
SmartOS 后端
    ↓ MIoT 客户端库
小米云端 API / 局域网
    ↓ 网关中继
小米网关 Hub1
    ↓ 蓝牙 Mesh
领普 CE1 (如果支持小米生态)
```

**优势**：
- ✅ 使用现有的 MIoT 客户端库
- ✅ 无需实现蓝牙 Mesh 协议栈
- ✅ 支持云端和局域网两种方式

**前提条件**：
- ⚠️ 领普 CE1 必须支持小米生态
- ⚠️ 小米网关 Hub1 必须支持蓝牙 Mesh 2.0

#### 方案二：直接实现蓝牙 Mesh 客户端（如果不兼容）

**架构**：
```
BC4 Pro (光感数据源)
    ↓ HTTP/MQTT
SmartOS 后端
    ↓ 蓝牙 Mesh 客户端
领普 CE1 (蓝牙 Mesh 2.0)
```

**需要实现**：
- ⚠️ 蓝牙 Mesh 2.0 协议栈
- ⚠️ 设备配网和绑定
- ⚠️ 设备控制协议

**参考资源**：
- ESP-BLE-MESH SDK
- Nordic nRF Mesh SDK
- Zephyr RTOS Mesh

---

### 9. 代码示例

#### 9.1 使用 MIoT 客户端控制网关

```python
from miot import MIoTClient

# 初始化客户端
client = MIoTClient(
    uuid="your-uuid",
    redirect_uri="http://127.0.0.1",
    oauth_info=oauth_info
)

await client.init_async()

# 获取网关设备
devices = await client.get_device_list_async()
gateway = [d for d in devices if d.model == "xiaomi.gateway.hub1"][0]

# 控制网关指示灯
await client.set_property_async(
    did=gateway.did,
    siid=2,  # 服务 ID
    piid=1,  # 属性 ID
    value=True  # 开灯
)

# 通过网关控制子设备（需要网关支持）
# 具体实现取决于网关的 API
```

#### 9.2 局域网设备发现

```python
from miot import MIoTClient

client = MIoTClient(...)
await client.init_async()

# 注册设备状态变化回调
async def on_device_status_changed(did: str, info: MIoTLanDeviceInfo):
    print(f"Device {did} status changed: {info}")

await client.register_lan_device_status_changed_async(
    key="my-key",
    handler=on_device_status_changed
)

# 开始监听
await client.start_lan_ping_async()
```

---

### 10. 总结

#### ✅ 发现的内容

1. **MIoT 客户端库**：完整的设备控制客户端
2. **网关设备支持**：支持小米网关 Hub1
3. **云端和局域网通信**：两种通信方式都支持
4. **设备发现和状态监听**：支持自动发现和实时监听

#### ⚠️ 未找到的内容

1. **蓝牙 Mesh 协议栈**：未直接实现
2. **蓝牙 Mesh 2.0 支持**：未找到相关代码
3. **直接蓝牙设备控制**：需要通过网关中继

#### 🎯 对 SmartOS 项目的建议

**如果领普 CE1 支持小米生态**：
- ✅ 使用 `miot_kit` 作为设备控制客户端
- ✅ 通过小米网关 Hub1 控制领普 CE1
- ✅ 无需实现蓝牙 Mesh 协议栈

**如果领普 CE1 不支持小米生态**：
- ⚠️ 需要实现蓝牙 Mesh 2.0 客户端
- ⚠️ 或使用其他网关方案（如 Home Assistant）
- ⚠️ 或直接实现蓝牙 Mesh 协议栈

---

## 📝 下一步行动

1. **确认领普 CE1 是否支持小米生态**
   - 查看领普 CE1 的产品文档
   - 确认是否可以通过小米网关控制

2. **测试小米网关 Hub1 的蓝牙 Mesh 支持**
   - 确认是否支持蓝牙 Mesh 2.0
   - 测试是否可以通过网关控制领普 CE1

3. **如果兼容，集成 MIoT 客户端库**
   - 将 `miot_kit` 集成到 SmartOS 项目
   - 实现网关控制接口
   - 实现设备控制逻辑

4. **如果不兼容，考虑其他方案**
   - Home Assistant 蓝牙 Mesh 集成
   - 直接实现蓝牙 Mesh 客户端
   - 使用第三方网关

---

**分析日期**：2025-01-03  
**项目版本**：xiaomi-miloco-main (2025-11)  
**分析状态**：初步分析完成，需要实际测试验证
