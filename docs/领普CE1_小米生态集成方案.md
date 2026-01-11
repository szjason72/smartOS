# 领普 CE1 小米生态集成方案

## 📋 项目概述

**目标**：将领普 CE1 窗帘电机集成到 SmartOS 项目，通过小米多模网关实现控制

**确认信息**：
- ✅ **领普 CE1 支持小米生态**（参考：[什么值得买评测](https://post.smzdm.com/zz/p/apwvr66x/)）
- ✅ **可以通过小米多模网关控制**
- ✅ **支持米家 APP 和 Home Assistant**

---

## 🎯 集成架构

### 推荐架构（已验证可行）⭐

```
BC4 Pro (光感数据源)
    ↓ HTTP/MQTT
SmartOS 后端
    ↓ MQTT / Telnet
小米多模网关 (Gateway 3/2 或 Aqara Hub E1)
    ↓ 蓝牙 Mesh / BLE
领普 CE1 (窗帘控制) ✅ 已确认支持
```

### 数据流

1. **光感数据采集**：BC4 Pro → SmartOS 后端
2. **数据处理**：SmartOS 后端 → 阈值判断 → 控制决策
3. **网关通信**：SmartOS 后端 → 小米网关（MQTT/Telnet）
4. **设备控制**：小米网关 → 领普 CE1（蓝牙 Mesh/BLE）
5. **状态反馈**：领普 CE1 → 小米网关 → SmartOS 后端

---

## 🔧 技术实现方案

### 方案一：参考 XiaomiGateway3 实现（推荐）⭐

**优势**：
- ✅ 有完整的参考代码
- ✅ 已验证可行（Home Assistant 使用）
- ✅ 支持 Mesh 和 BLE 设备
- ✅ 支持设备发现和状态监听

**实施步骤**：

#### 1. 提取核心代码

**需要提取的模块**：
- `core/gate/mesh.py` - Mesh 网关通信
- `core/gate/ble.py` - BLE 网关通信
- `core/mini_mqtt.py` - MQTT 客户端
- `core/shell/shell_mgw.py` - Telnet/Shell 通信
- `cover.py` - 窗帘设备控制

#### 2. 适配到 SmartOS

**Java 实现方案**：
```java
// 网关通信接口
public interface XiaomiGatewayClient {
    // 连接网关
    void connect(String gatewayIp, String token, String key);
    
    // 读取设备列表
    List<Device> readMeshDevices();
    List<Device> readBLEDevices();
    
    // 控制设备
    void controlCurtain(String deviceId, CurtainCommand command);
    
    // 监听设备状态
    void subscribeDeviceStatus(DeviceStatusListener listener);
}

// MQTT 客户端
public class XiaomiGatewayMQTTClient {
    // 连接到网关 MQTT
    void connect(String broker, int port, String username, String password);
    
    // 订阅主题
    void subscribe(String topic, MessageHandler handler);
    
    // 发布消息
    void publish(String topic, String message);
}

// 窗帘控制服务
@Service
public class CurtainControlService {
    @Autowired
    private XiaomiGatewayClient gatewayClient;
    
    public void openCurtain(String deviceId) {
        gatewayClient.controlCurtain(deviceId, CurtainCommand.OPEN);
    }
    
    public void closeCurtain(String deviceId) {
        gatewayClient.controlCurtain(deviceId, CurtainCommand.CLOSE);
    }
    
    public void setPosition(String deviceId, int position) {
        gatewayClient.controlCurtain(deviceId, 
            CurtainCommand.setPosition(position));
    }
}
```

**Python 实现方案**（如果使用 Python 服务）：
```python
# 直接复用 XiaomiGateway3 的代码
from xiaomi_gateway3.core.gate.mesh import MeshGateway
from xiaomi_gateway3.core.mini_mqtt import MiniMQTT

class SmartOSGatewayClient:
    def __init__(self, gateway_ip, token, key):
        self.gateway = MeshGateway()
        self.mqtt = MiniMQTT()
        # 初始化连接
        
    def control_curtain(self, device_id, command):
        # 控制窗帘
        pass
```

#### 3. 集成到 SmartOS 后端

**目录结构**：
```
smartOS/backend/src/main/java/com/smartos/
├── gateway/
│   ├── XiaomiGatewayClient.java      # 网关客户端接口
│   ├── XiaomiGatewayMQTTClient.java  # MQTT 客户端
│   ├── XiaomiGatewayShellClient.java # Shell/Telnet 客户端
│   └── device/
│       ├── MeshDevice.java            # Mesh 设备
│       ├── BLEDevice.java             # BLE 设备
│       └── CurtainDevice.java         # 窗帘设备
├── service/
│   ├── GatewayService.java            # 网关服务
│   └── CurtainControlService.java     # 窗帘控制服务
└── controller/
    └── CurtainController.java         # 窗帘控制器
```

---

### 方案二：使用 Home Assistant 作为中间层

**架构**：
```
BC4 Pro → SmartOS → MQTT → Home Assistant → 小米网关 → 领普 CE1
```

**优势**：
- ✅ 无需实现网关通信代码
- ✅ 使用 Home Assistant 的 XiaomiGateway3 组件
- ✅ 通过 MQTT 与 Home Assistant 通信

**劣势**：
- ⚠️ 需要部署 Home Assistant
- ⚠️ 增加了一层中间件

---

## 📡 网关通信协议

### MQTT 通信

**网关 MQTT Broker**：
- 地址：网关 IP（通常是 `192.168.x.x`）
- 端口：`1883`（默认）
- 认证：Token 和 Key

**MQTT Topic**：
- `miio/report` - 设备报告消息
- `central/report` - 中央设备报告

**消息格式**：
```json
{
  "method": "_sync.ble_mesh_keep_alive",
  "params": [
    {
      "did": "device_id",
      "rssi": -52,
      "hops": 0,
      "ts": 1234567890
    }
  ]
}
```

### Telnet/Shell 通信

**连接方式**：
- 通过 Telnet 连接到网关
- 执行 Shell 命令读取数据库

**数据库表**：
- `mesh_device_v3` - Mesh 设备表
- `mesh_group_v3` - Mesh 组表
- `gateway_authed_table` - BLE 设备授权表

---

## 🎛️ 窗帘控制实现

### 控制命令

**打开窗帘**：
```json
{
  "method": "set_properties",
  "params": [
    {
      "did": "curtain_device_id",
      "siid": 2,
      "piid": 1,
      "value": 100
    }
  ]
}
```

**关闭窗帘**：
```json
{
  "method": "set_properties",
  "params": [
    {
      "did": "curtain_device_id",
      "siid": 2,
      "piid": 1,
      "value": 0
    }
  ]
}
```

**设置位置（0-100%）**：
```json
{
  "method": "set_properties",
  "params": [
    {
      "did": "curtain_device_id",
      "siid": 2,
      "piid": 1,
      "value": 50
    }
  ]
}
```

### 状态监听

**订阅设备状态**：
```json
{
  "method": "_sync.ble_mesh_keep_alive",
  "params": [
    {
      "did": "curtain_device_id",
      "rssi": -52,
      "hops": 0,
      "ts": 1234567890
    }
  ]
}
```

---

## 🔐 网关认证

### 获取 Token 和 Key

**Token 获取方式**：
1. 使用米家 APP 获取设备 Token
2. 使用 [XiaomiGateway3 文档](https://github.com/AlexxIT/XiaomiGateway3) 中的方法

**Key 获取方式**：
- 固件 1.5.4 及以下：集成会自动获取并保存
- 固件 1.5.5 及以上：需要手动获取（参考 XiaomiGateway3 文档）

### 配置文件

```yaml
smartos:
  gateway:
    xiaomi:
      enabled: true
      ip: 192.168.1.100
      token: your_token_here
      key: your_key_here  # 可选，某些固件需要
      mqtt:
        broker: 192.168.1.100
        port: 1883
```

---

## 📊 实施计划

### 阶段一：网关通信基础（1-2周）

**任务**：
- [ ] 提取 XiaomiGateway3 核心代码
- [ ] 实现 MQTT 客户端
- [ ] 实现 Telnet/Shell 客户端
- [ ] 实现设备发现功能
- [ ] 测试网关连接

### 阶段二：设备控制（1周）

**任务**：
- [ ] 实现窗帘设备识别
- [ ] 实现窗帘控制命令
- [ ] 实现设备状态监听
- [ ] 测试窗帘控制功能

### 阶段三：集成到 SmartOS（1周）

**任务**：
- [ ] 集成到 SmartOS 后端
- [ ] 实现自动化规则引擎
- [ ] 实现光感数据联动
- [ ] 测试完整流程

### 阶段四：优化和测试（1周）

**任务**：
- [ ] 性能优化
- [ ] 错误处理
- [ ] 日志记录
- [ ] 文档完善

---

## 🧪 测试计划

### 1. 网关连接测试

**测试项**：
- [ ] 网关 IP 连接测试
- [ ] MQTT 连接测试
- [ ] Telnet 连接测试
- [ ] Token 和 Key 验证

### 2. 设备发现测试

**测试项**：
- [ ] Mesh 设备发现
- [ ] BLE 设备发现
- [ ] 领普 CE1 设备识别
- [ ] 设备状态获取

### 3. 窗帘控制测试

**测试项**：
- [ ] 打开窗帘
- [ ] 关闭窗帘
- [ ] 位置控制（0-100%）
- [ ] 缓开功能
- [ ] 状态反馈

### 4. 集成测试

**测试项**：
- [ ] BC4 Pro 光感数据采集
- [ ] 自动化规则触发
- [ ] 窗帘自动控制
- [ ] 双窗帘独立控制（如果支持）

---

## 📝 注意事项

### 1. 网关要求

**支持的网关**：
- ✅ 小米多模网关（Gateway 3）CN/EU
- ✅ 小米多模网关 2（Gateway 2）CN/EU
- ✅ Aqara Hub E1 CN

**固件要求**：
- Gateway 3: 1.5.0 - 1.5.6（推荐 1.5.4 - 1.5.6）
- Gateway 2: 1.0.3 - 1.0.7（推荐 1.0.6 - 1.0.7）
- Hub E1: 4.0.1

### 2. 网络要求

**要求**：
- ✅ 网关和 SmartOS 后端在同一局域网
- ✅ 网关 IP 地址固定（建议配置静态 IP）
- ✅ 防火墙允许 MQTT（1883）和 Telnet（23）端口

### 3. 安全考虑

**建议**：
- ✅ Token 和 Key 存储在配置文件中，不要提交到 Git
- ✅ 使用环境变量或密钥管理服务
- ✅ 限制网关访问权限

---

## 🔗 参考资源

- **XiaomiGateway3 项目**：[https://github.com/AlexxIT/XiaomiGateway3](https://github.com/AlexxIT/XiaomiGateway3)
- **领普 CE1 评测**：[什么值得买](https://post.smzdm.com/zz/p/apwvr66x/)
- **小米网关文档**：参考 XiaomiGateway3 README

---

## ✅ 总结

### 确认的优势

1. ✅ **领普 CE1 支持小米生态**（已验证）
2. ✅ **可以使用小米多模网关控制**
3. ✅ **有完整的参考代码**（XiaomiGateway3）
4. ✅ **架构清晰，实施可行**

### 实施建议

**推荐方案**：参考 XiaomiGateway3 实现网关通信

**优势**：
- ✅ 代码成熟，已验证可行
- ✅ 支持 Mesh 和 BLE 设备
- ✅ 支持设备发现和状态监听
- ✅ 可以直接参考窗帘控制逻辑

**下一步**：
1. 提取 XiaomiGateway3 核心代码
2. 适配到 SmartOS 架构
3. 实现网关通信接口
4. 测试设备控制功能

---

**创建日期**：2025-01-03  
**状态**：方案已确认，准备开始实施
