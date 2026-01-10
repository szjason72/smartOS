# BC4 Pro 光感窗帘集成方案

## 📋 项目概述

**目标**：集成 BC4 Pro 设备的光感功能，实现基于光照强度的智能窗帘自动开合控制

**设备信息**：
- **设备型号**：BC4 Pro（神眸太空人）
- **设备类型**：CINMOORE_BC4
- **设备类别**：4G摄像机
- **参考项目**：shenmou（神眸摄像头APP）

---

## 🔍 BC4 Pro 设备分析

### 1. 设备基本信息

根据 `YJSDeviceTypesConfig.json` 配置：
```json
{
    "deviceType": "CINMOORE_BC4",
    "deviceTypeName": "神眸太空人",
    "deviceName": "BC4",
    "deviceTypeIcon": "cameraIconBC4",
    "defaultLinkType": "ALI_SOFT_AP",
    "netType": "4G"
}
```

### 2. 光照相关功能

从 `LightSettingV3.json` 和 `DeviceLightSetting.json` 分析：
- **StatusLightSwitch**：状态指示灯开关
- **AlarmLightSwitch**：报警闪光灯开关

**注意**：BC4 Pro 作为摄像头设备，可能具备：
1. **环境光传感器**：用于自动调节摄像头曝光
2. **图像亮度分析**：通过图像处理获取环境光照信息
3. **专用光感模块**：如果硬件支持

### 3. 通信协议

- **连接方式**：ALI_SOFT_AP（阿里云软AP）
- **网络类型**：4G
- **通信协议**：需要进一步分析 shenmou APP 的通信协议

---

## 🏗️ 集成架构设计

### 架构图

```
BC4 Pro 设备（光感数据源）
    ↓ HTTP/WebSocket/MQTT
光感数据采集服务
    ↓ 数据处理和阈值判断
智能窗帘控制服务
    ↓ 设备控制指令
智能窗帘设备（ESP32/ESPHome）
    ↓ 反馈状态
用户界面（Home Assistant / 自定义前端）
```

### 数据流

1. **光感数据采集**：BC4 Pro → 光感数据采集服务
2. **数据处理**：光感数据 → 阈值判断 → 控制决策
3. **设备控制**：控制决策 → 窗帘控制指令 → 智能窗帘设备
4. **状态反馈**：窗帘状态 → 用户界面

---

## 💾 数据库设计

### 1. 光感数据表

```sql
-- 光感数据记录表
CREATE TABLE IF NOT EXISTS zervigo_light_sensor_data (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES zervigo_smart_devices(id),
    tenant_id BIGINT NOT NULL REFERENCES zervigo_tenants(id),
    light_value DECIMAL(10, 2) NOT NULL, -- 光照强度值（单位：lux）
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_source VARCHAR(50) DEFAULT 'bc4_pro', -- 数据来源
    metadata JSONB, -- 额外元数据（如：图像亮度、曝光值等）
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_light_sensor_device_id ON zervigo_light_sensor_data(device_id);
CREATE INDEX idx_light_sensor_timestamp ON zervigo_light_sensor_data(timestamp);
CREATE INDEX idx_light_sensor_tenant_id ON zervigo_light_sensor_data(tenant_id);
```

### 2. 光感窗帘联动配置表

```sql
-- 光感窗帘联动配置表
CREATE TABLE IF NOT EXISTS zervigo_light_curtain_automation (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES zervigo_tenants(id),
    name VARCHAR(255) NOT NULL, -- 自动化规则名称
    light_sensor_device_id BIGINT NOT NULL REFERENCES zervigo_smart_devices(id), -- BC4 Pro设备ID
    curtain_device_id BIGINT NOT NULL REFERENCES zervigo_smart_devices(id), -- 窗帘设备ID
    enabled BOOLEAN DEFAULT true, -- 是否启用
    -- 光照阈值配置
    open_threshold DECIMAL(10, 2), -- 开窗帘阈值（低于此值开窗帘）
    close_threshold DECIMAL(10, 2), -- 关窗帘阈值（高于此值关窗帘）
    -- 窗帘控制配置
    open_position INTEGER DEFAULT 100, -- 开窗帘位置（0-100）
    close_position INTEGER DEFAULT 0, -- 关窗帘位置（0-100）
    -- 时间限制
    start_time TIME, -- 开始时间（如：08:00）
    end_time TIME, -- 结束时间（如：20:00）
    -- 延迟和防抖
    debounce_seconds INTEGER DEFAULT 30, -- 防抖时间（秒）
    check_interval_seconds INTEGER DEFAULT 60, -- 检查间隔（秒）
    -- 其他配置
    config JSONB, -- 其他配置项
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_light_curtain_tenant_id ON zervigo_light_curtain_automation(tenant_id);
CREATE INDEX idx_light_curtain_enabled ON zervigo_light_curtain_automation(enabled);
```

### 3. 光感数据统计表

```sql
-- 光感数据统计表（用于分析和优化）
CREATE TABLE IF NOT EXISTS zervigo_light_sensor_stats (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES zervigo_smart_devices(id),
    tenant_id BIGINT NOT NULL REFERENCES zervigo_tenants(id),
    date DATE NOT NULL,
    avg_light_value DECIMAL(10, 2), -- 平均光照值
    min_light_value DECIMAL(10, 2), -- 最小光照值
    max_light_value DECIMAL(10, 2), -- 最大光照值
    sample_count INTEGER DEFAULT 0, -- 采样次数
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(device_id, date)
);

CREATE INDEX idx_light_stats_device_date ON zervigo_light_sensor_stats(device_id, date);
```

---

## 🔌 服务实现

### 1. BC4 Pro 光感数据采集服务

```go
// services/core/device/bc4_light_sensor_service.go
package device

import (
    "context"
    "database/sql"
    "encoding/json"
    "fmt"
    "time"
)

type BC4LightSensorService struct {
    db *sql.DB
}

// LightSensorData 光感数据结构
type LightSensorData struct {
    ID          int64                  `json:"id"`
    DeviceID    int64                  `json:"device_id"`
    TenantID    int64                  `json:"tenant_id"`
    LightValue  float64                `json:"light_value"` // lux
    Timestamp   time.Time              `json:"timestamp"`
    DataSource  string                 `json:"data_source"`
    Metadata    map[string]interface{} `json:"metadata"`
    CreatedAt   time.Time              `json:"created_at"`
}

// CollectLightData 采集BC4 Pro光感数据
func (s *BC4LightSensorService) CollectLightData(ctx context.Context, deviceID int64, lightValue float64, metadata map[string]interface{}) (*LightSensorData, error) {
    // 1. 获取设备信息
    device, err := s.getDevice(ctx, deviceID)
    if err != nil {
        return nil, fmt.Errorf("获取设备信息失败: %w", err)
    }
    
    // 2. 插入光感数据
    query := `
        INSERT INTO zervigo_light_sensor_data 
        (device_id, tenant_id, light_value, timestamp, data_source, metadata, created_at)
        VALUES ($1, $2, $3, NOW(), $4, $5, NOW())
        RETURNING id, created_at
    `
    
    metadataJSON, _ := json.Marshal(metadata)
    
    var dataID int64
    var createdAt time.Time
    err = s.db.QueryRowContext(ctx, query,
        deviceID, device.TenantID, lightValue, "bc4_pro", metadataJSON).Scan(&dataID, &createdAt)
    if err != nil {
        return nil, fmt.Errorf("插入光感数据失败: %w", err)
    }
    
    // 3. 触发自动化规则检查
    go s.checkAutomationRules(ctx, deviceID, device.TenantID, lightValue)
    
    return &LightSensorData{
        ID:         dataID,
        DeviceID:   deviceID,
        TenantID:   device.TenantID,
        LightValue: lightValue,
        Timestamp:  createdAt,
        DataSource: "bc4_pro",
        Metadata:   metadata,
        CreatedAt:  createdAt,
    }, nil
}

// GetLatestLightData 获取最新的光感数据
func (s *BC4LightSensorService) GetLatestLightData(ctx context.Context, deviceID int64, limit int) ([]*LightSensorData, error) {
    query := `
        SELECT id, device_id, tenant_id, light_value, timestamp, data_source, metadata, created_at
        FROM zervigo_light_sensor_data
        WHERE device_id = $1
        ORDER BY timestamp DESC
        LIMIT $2
    `
    
    rows, err := s.db.QueryContext(ctx, query, deviceID, limit)
    if err != nil {
        return nil, err
    }
    defer rows.Close()
    
    var dataList []*LightSensorData
    for rows.Next() {
        var data LightSensorData
        var metadataJSON []byte
        
        err := rows.Scan(
            &data.ID, &data.DeviceID, &data.TenantID, &data.LightValue,
            &data.Timestamp, &data.DataSource, &metadataJSON, &data.CreatedAt,
        )
        if err != nil {
            continue
        }
        
        json.Unmarshal(metadataJSON, &data.Metadata)
        dataList = append(dataList, &data)
    }
    
    return dataList, nil
}

// checkAutomationRules 检查自动化规则
func (s *BC4LightSensorService) checkAutomationRules(ctx context.Context, deviceID, tenantID int64, lightValue float64) {
    // 查询所有启用的光感窗帘联动规则
    query := `
        SELECT id, curtain_device_id, open_threshold, close_threshold,
               open_position, close_position, start_time, end_time,
               debounce_seconds, check_interval_seconds
        FROM zervigo_light_curtain_automation
        WHERE light_sensor_device_id = $1 
          AND tenant_id = $2 
          AND enabled = true
    `
    
    rows, err := s.db.QueryContext(ctx, query, deviceID, tenantID)
    if err != nil {
        return
    }
    defer rows.Close()
    
    now := time.Now()
    currentTime := time.Date(0, 1, 1, now.Hour(), now.Minute(), now.Second(), 0, time.UTC)
    
    for rows.Next() {
        var automation LightCurtainAutomation
        var startTime, endTime sql.NullTime
        
        err := rows.Scan(
            &automation.ID, &automation.CurtainDeviceID,
            &automation.OpenThreshold, &automation.CloseThreshold,
            &automation.OpenPosition, &automation.ClosePosition,
            &startTime, &endTime,
            &automation.DebounceSeconds, &automation.CheckIntervalSeconds,
        )
        if err != nil {
            continue
        }
        
        // 检查时间限制
        if startTime.Valid && endTime.Valid {
            start := time.Date(0, 1, 1, startTime.Time.Hour(), startTime.Time.Minute(), 0, 0, time.UTC)
            end := time.Date(0, 1, 1, endTime.Time.Hour(), endTime.Time.Minute(), 0, 0, time.UTC)
            
            if currentTime.Before(start) || currentTime.After(end) {
                continue // 不在时间范围内，跳过
            }
        }
        
        // 判断是否需要控制窗帘
        var targetPosition int
        var shouldControl bool
        
        if lightValue < automation.OpenThreshold {
            // 光照不足，开窗帘
            targetPosition = automation.OpenPosition
            shouldControl = true
        } else if lightValue > automation.CloseThreshold {
            // 光照充足，关窗帘
            targetPosition = automation.ClosePosition
            shouldControl = true
        }
        
        if shouldControl {
            // 防抖检查：检查上次控制时间
            if s.shouldDebounce(ctx, automation.ID, automation.DebounceSeconds) {
                continue
            }
            
            // 执行窗帘控制
            s.controlCurtain(ctx, automation.CurtainDeviceID, targetPosition)
            
            // 记录控制时间
            s.recordControlTime(ctx, automation.ID)
        }
    }
}

// shouldDebounce 防抖检查
func (s *BC4LightSensorService) shouldDebounce(ctx context.Context, automationID int64, debounceSeconds int) bool {
    // 查询上次控制时间
    query := `
        SELECT last_control_time 
        FROM zervigo_light_curtain_automation
        WHERE id = $1
    `
    
    var lastControlTime sql.NullTime
    err := s.db.QueryRowContext(ctx, query, automationID).Scan(&lastControlTime)
    if err != nil {
        return false
    }
    
    if !lastControlTime.Valid {
        return false
    }
    
    // 检查是否在防抖时间内
    elapsed := time.Since(lastControlTime.Time)
    return elapsed < time.Duration(debounceSeconds)*time.Second
}

// controlCurtain 控制窗帘
func (s *BC4LightSensorService) controlCurtain(ctx context.Context, curtainDeviceID int64, position int) {
    // 调用窗帘控制服务
    // 这里需要集成到实际的窗帘控制服务
    // curtainService.SetPosition(ctx, curtainDeviceID, position)
    
    // 临时实现：记录日志
    fmt.Printf("控制窗帘设备 %d 到位置 %d\n", curtainDeviceID, position)
}

// recordControlTime 记录控制时间
func (s *BC4LightSensorService) recordControlTime(ctx context.Context, automationID int64) {
    query := `
        UPDATE zervigo_light_curtain_automation
        SET last_control_time = NOW(), updated_at = NOW()
        WHERE id = $1
    `
    s.db.ExecContext(ctx, query, automationID)
}

// getDevice 获取设备信息
func (s *BC4LightSensorService) getDevice(ctx context.Context, deviceID int64) (*BC4Device, error) {
    // 实现设备查询逻辑
    // 这里可以复用 BC4DeviceService 的方法
    return nil, fmt.Errorf("未实现")
}

// LightCurtainAutomation 光感窗帘自动化规则
type LightCurtainAutomation struct {
    ID                    int64
    CurtainDeviceID       int64
    OpenThreshold         float64
    CloseThreshold        float64
    OpenPosition          int
    ClosePosition         int
    DebounceSeconds       int
    CheckIntervalSeconds  int
}
```

### 2. BC4 Pro 设备通信接口

```go
// services/core/device/bc4_device_client.go
package device

import (
    "bytes"
    "encoding/json"
    "fmt"
    "io"
    "net/http"
    "time"
)

type BC4DeviceClient struct {
    BaseURL    string
    DeviceID   string
    Token      string
    HTTPClient *http.Client
}

// NewBC4DeviceClient 创建BC4 Pro设备客户端
func NewBC4DeviceClient(baseURL, deviceID, token string) *BC4DeviceClient {
    return &BC4DeviceClient{
        BaseURL:  baseURL,
        DeviceID: deviceID,
        Token:    token,
        HTTPClient: &http.Client{
            Timeout: 10 * time.Second,
        },
    }
}

// GetLightSensorValue 获取光感数据
// 注意：这个方法需要根据BC4 Pro的实际API接口来实现
func (c *BC4DeviceClient) GetLightSensorValue() (float64, error) {
    // 方案1：如果BC4 Pro提供光感API
    url := fmt.Sprintf("%s/api/device/%s/light_sensor", c.BaseURL, c.DeviceID)
    
    req, err := http.NewRequest("GET", url, nil)
    if err != nil {
        return 0, err
    }
    
    req.Header.Set("Authorization", "Bearer "+c.Token)
    
    resp, err := c.HTTPClient.Do(req)
    if err != nil {
        return 0, err
    }
    defer resp.Body.Close()
    
    body, err := io.ReadAll(resp.Body)
    if err != nil {
        return 0, err
    }
    
    var result struct {
        LightValue float64 `json:"light_value"`
    }
    
    if err := json.Unmarshal(body, &result); err != nil {
        return 0, err
    }
    
    return result.LightValue, nil
}

// GetImageBrightness 通过图像亮度分析获取光照信息
// 方案2：如果BC4 Pro不提供光感API，可以通过图像分析获取
func (c *BC4DeviceClient) GetImageBrightness() (float64, error) {
    // 获取当前图像
    imageURL := fmt.Sprintf("%s/api/device/%s/current_image", c.BaseURL, c.DeviceID)
    
    req, err := http.NewRequest("GET", imageURL, nil)
    if err != nil {
        return 0, err
    }
    
    req.Header.Set("Authorization", "Bearer "+c.Token)
    
    resp, err := c.HTTPClient.Do(req)
    if err != nil {
        return 0, err
    }
    defer resp.Body.Close()
    
    // 分析图像亮度
    // 这里需要实现图像处理逻辑
    // 可以使用图像处理库（如Go的image包）计算平均亮度
    
    // 临时返回示例值
    return 500.0, nil
}

// SendCommand 发送设备命令
func (c *BC4DeviceClient) SendCommand(command string, params map[string]interface{}) error {
    url := fmt.Sprintf("%s/api/device/%s/command", c.BaseURL, c.DeviceID)
    
    payload := map[string]interface{}{
        "command": command,
        "params":  params,
    }
    
    jsonData, err := json.Marshal(payload)
    if err != nil {
        return err
    }
    
    req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonData))
    if err != nil {
        return err
    }
    
    req.Header.Set("Content-Type", "application/json")
    req.Header.Set("Authorization", "Bearer "+c.Token)
    
    resp, err := c.HTTPClient.Do(req)
    if err != nil {
        return err
    }
    defer resp.Body.Close()
    
    if resp.StatusCode != http.StatusOK {
        return fmt.Errorf("设备命令执行失败: %d", resp.StatusCode)
    }
    
    return nil
}
```

### 3. 光感数据采集定时任务

```go
// services/core/device/bc4_light_collector.go
package device

import (
    "context"
    "log"
    "time"
)

type BC4LightCollector struct {
    lightSensorService *BC4LightSensorService
    deviceClient       *BC4DeviceClient
    deviceID           int64
    interval           time.Duration
    stopChan           chan bool
}

// NewBC4LightCollector 创建光感数据采集器
func NewBC4LightCollector(
    lightSensorService *BC4LightSensorService,
    deviceClient *BC4DeviceClient,
    deviceID int64,
    interval time.Duration,
) *BC4LightCollector {
    return &BC4LightCollector{
        lightSensorService: lightSensorService,
        deviceClient:       deviceClient,
        deviceID:           deviceID,
        interval:           interval,
        stopChan:           make(chan bool),
    }
}

// Start 启动采集器
func (c *BC4LightCollector) Start(ctx context.Context) {
    ticker := time.NewTicker(c.interval)
    defer ticker.Stop()
    
    for {
        select {
        case <-ticker.C:
            c.collect(ctx)
        case <-c.stopChan:
            log.Printf("光感数据采集器已停止: device_id=%d", c.deviceID)
            return
        case <-ctx.Done():
            log.Printf("光感数据采集器已停止: device_id=%d", c.deviceID)
            return
        }
    }
}

// Stop 停止采集器
func (c *BC4LightCollector) Stop() {
    close(c.stopChan)
}

// collect 采集光感数据
func (c *BC4LightCollector) collect(ctx context.Context) {
    // 尝试获取光感数据
    lightValue, err := c.deviceClient.GetLightSensorValue()
    if err != nil {
        // 如果直接获取光感数据失败，尝试通过图像分析
        log.Printf("获取光感数据失败，尝试图像分析: %v", err)
        lightValue, err = c.deviceClient.GetImageBrightness()
        if err != nil {
            log.Printf("图像分析失败: %v", err)
            return
        }
    }
    
    // 保存光感数据
    metadata := map[string]interface{}{
        "collection_method": "api",
        "timestamp":         time.Now().Unix(),
    }
    
    _, err = c.lightSensorService.CollectLightData(ctx, c.deviceID, lightValue, metadata)
    if err != nil {
        log.Printf("保存光感数据失败: %v", err)
        return
    }
    
    log.Printf("光感数据采集成功: device_id=%d, light_value=%.2f lux", c.deviceID, lightValue)
}
```

---

## 📡 API接口设计

### 1. 光感数据API

```go
// services/core/device/bc4_light_api.go
package device

import (
    "github.com/gin-gonic/gin"
    "net/http"
    "strconv"
)

type BC4LightAPI struct {
    lightSensorService *BC4LightSensorService
    deviceClient       *BC4DeviceClient
}

// RegisterRoutes 注册光感数据路由
func (api *BC4LightAPI) RegisterRoutes(r *gin.RouterGroup) {
    lightGroup := r.Group("/bc4-light")
    lightGroup.Use(middleware.TenantMiddleware())
    lightGroup.Use(middleware.AuthMiddleware())
    
    // 光感数据
    lightGroup.POST("/collect", api.collectLightData)        // 采集光感数据
    lightGroup.GET("/:device_id/data", api.getLightData)    // 获取光感数据
    lightGroup.GET("/:device_id/latest", api.getLatestData) // 获取最新数据
    
    // 自动化规则
    lightGroup.POST("/automation", api.createAutomation)     // 创建自动化规则
    lightGroup.GET("/automation", api.listAutomations)       // 列出自动化规则
    lightGroup.PUT("/automation/:id", api.updateAutomation) // 更新自动化规则
    lightGroup.DELETE("/automation/:id", api.deleteAutomation) // 删除自动化规则
}

// collectLightData 采集光感数据
func (api *BC4LightAPI) collectLightData(c *gin.Context) {
    tenantID := context.GetTenantID(c.Request.Context())
    
    var req struct {
        DeviceID   int64   `json:"device_id" binding:"required"`
        LightValue float64 `json:"light_value" binding:"required"`
        Metadata   map[string]interface{} `json:"metadata"`
    }
    
    if err := c.ShouldBindJSON(&req); err != nil {
        response.ErrorResponse(c, http.StatusBadRequest, "参数错误", err.Error())
        return
    }
    
    data, err := api.lightSensorService.CollectLightData(
        c.Request.Context(),
        req.DeviceID,
        req.LightValue,
        req.Metadata,
    )
    if err != nil {
        response.ErrorResponse(c, http.StatusInternalServerError, "采集失败", err.Error())
        return
    }
    
    response.SuccessResponse(c, "采集成功", data)
}

// getLightData 获取光感数据
func (api *BC4LightAPI) getLightData(c *gin.Context) {
    deviceIDStr := c.Param("device_id")
    deviceID, err := strconv.ParseInt(deviceIDStr, 10, 64)
    if err != nil {
        response.ErrorResponse(c, http.StatusBadRequest, "设备ID无效", err.Error())
        return
    }
    
    limitStr := c.DefaultQuery("limit", "100")
    limit, _ := strconv.Atoi(limitStr)
    
    dataList, err := api.lightSensorService.GetLatestLightData(c.Request.Context(), deviceID, limit)
    if err != nil {
        response.ErrorResponse(c, http.StatusInternalServerError, "查询失败", err.Error())
        return
    }
    
    response.SuccessResponse(c, "查询成功", dataList)
}
```

---

## 🎯 实施步骤

### 阶段一：BC4 Pro 光感数据采集（3-5天）

1. **分析BC4 Pro通信协议**
   - 分析 shenmou APP 的通信协议
   - 确定光感数据获取方式（API/图像分析）
   - 实现设备通信客户端

2. **实现光感数据采集服务**
   - 创建光感数据表
   - 实现数据采集和存储
   - 实现定时采集任务

3. **测试数据采集**
   - 验证数据采集功能
   - 测试数据存储和查询

### 阶段二：光感窗帘自动化（3-5天）

1. **实现自动化规则管理**
   - 创建自动化配置表
   - 实现规则CRUD接口
   - 实现规则执行逻辑

2. **集成窗帘控制**
   - 集成到现有的窗帘控制服务
   - 实现防抖和延迟控制
   - 实现时间限制功能

3. **测试自动化功能**
   - 测试自动化规则触发
   - 测试窗帘控制响应
   - 优化控制逻辑

### 阶段三：前端界面（2-3天）

1. **光感数据展示**
   - 实时数据图表
   - 历史数据查询
   - 数据统计分析

2. **自动化规则配置**
   - 规则创建和编辑界面
   - 规则启用/禁用
   - 规则执行日志

### 阶段四：优化和测试（2-3天）

1. **性能优化**
   - 数据采集频率优化
   - 数据库查询优化
   - 自动化规则执行优化

2. **稳定性测试**
   - 长时间运行测试
   - 异常情况处理
   - 错误恢复机制

---

## 📝 注意事项

### 1. BC4 Pro 光感数据获取方式

**方案A：直接API获取**（如果BC4 Pro提供光感API）
- 优点：准确、实时
- 缺点：需要BC4 Pro固件支持

**方案B：图像亮度分析**（通过图像处理获取）
- 优点：不需要硬件支持
- 缺点：需要图像处理，可能不够准确

**方案C：混合方案**
- 优先使用API，失败时使用图像分析

### 2. 光照阈值设置

建议的默认阈值：
- **开窗帘阈值**：< 200 lux（光照不足）
- **关窗帘阈值**：> 500 lux（光照充足）

用户可以根据实际环境调整。

### 3. 防抖机制

避免频繁控制窗帘：
- **防抖时间**：30-60秒
- **检查间隔**：60秒
- **阈值死区**：避免在阈值附近频繁切换

### 4. 时间限制

建议只在白天启用：
- **默认时间**：08:00 - 20:00
- 用户可自定义时间范围

---

## 🔗 相关文档

1. **BC4 Pro设备适配文档**：`/Users/szjason72/TimesSquare/BC4_PRO_DEVICE_ADAPTATION.md`
2. **智能窗帘差异化控制方案**：`智能窗帘差异化控制方案调研.md`
3. **shenmou项目**：`/Users/szjason72/shenmou_v275/`

---

## 📊 总结

通过集成 BC4 Pro 的光感功能，可以实现：

1. **自动光感数据采集**：定时采集环境光照数据
2. **智能窗帘控制**：根据光照强度自动控制窗帘开合
3. **灵活配置**：支持自定义阈值、时间限制等
4. **数据统计**：记录和分析光照数据，优化控制策略

**预计总时间**：10-16天（分阶段实施）
