package com.smartos.mock.device;

import com.smartos.common.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BC4 Pro 设备模拟器
 * 提供本地化的BC4 Pro设备API模拟，不依赖外部服务器
 */
@Slf4j
@RestController
@RequestMapping("/mock/bc4")
public class BC4MockController {
    
    private final Random random = new Random();
    private final Map<String, DeviceState> deviceStates = new ConcurrentHashMap<>();
    
    /**
     * 获取设备状态
     */
    @GetMapping("/{deviceId}/status")
    public Result<DeviceStatus> getDeviceStatus(@PathVariable String deviceId) {
        log.debug("获取设备状态: {}", deviceId);
        
        DeviceState state = getOrCreateDeviceState(deviceId);
        
        DeviceStatus status = new DeviceStatus();
        status.setDeviceId(deviceId);
        status.setOnline(true);
        status.setBatteryLevel(state.getBatteryLevel());
        status.setSignalStrength(state.getSignalStrength());
        status.setFirmwareVersion("1.0.0");
        status.setTimestamp(LocalDateTime.now());
        
        return Result.success(status);
    }
    
    /**
     * 获取光感数据
     */
    @GetMapping("/{deviceId}/lightSensor")
    public Result<LightSensorData> getLightSensor(@PathVariable String deviceId) {
        log.debug("获取光感数据: {}", deviceId);
        
        // 生成光感数据（根据时间模拟）
        double lightValue = generateLightValue();
        
        LightSensorData data = new LightSensorData();
        data.setDeviceId(deviceId);
        data.setLightValue(lightValue);
        data.setTimestamp(LocalDateTime.now());
        data.setDataSource("mock");
        data.setUnit("lux");
        
        return Result.success(data);
    }
    
    /**
     * 发送设备命令
     */
    @PostMapping("/{deviceId}/command")
    public Result<CommandResponse> sendCommand(
            @PathVariable String deviceId,
            @RequestBody CommandRequest request) {
        
        log.debug("发送设备命令: {} -> {}", deviceId, request.getCommand());
        
        DeviceState state = getOrCreateDeviceState(deviceId);
        
        // 模拟命令执行延迟
        try {
            Thread.sleep(100 + random.nextInt(200)); // 100-300ms延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 模拟命令执行
        CommandResponse response = new CommandResponse();
        response.setDeviceId(deviceId);
        response.setCommand(request.getCommand());
        response.setSuccess(true);
        response.setMessage("命令执行成功");
        response.setTimestamp(LocalDateTime.now());
        response.setData(request.getParams());
        
        return Result.success(response);
    }
    
    /**
     * 获取设备配置
     */
    @GetMapping("/{deviceId}/config")
    public Result<DeviceConfig> getDeviceConfig(@PathVariable String deviceId) {
        log.debug("获取设备配置: {}", deviceId);
        
        DeviceConfig config = new DeviceConfig();
        config.setDeviceId(deviceId);
        config.setStatusLightSwitch(true);
        config.setAlarmLightSwitch(false);
        config.setVideoQuality("1080p");
        config.setNightVision(true);
        config.setMotionDetection(true);
        config.setTimestamp(LocalDateTime.now());
        
        return Result.success(config);
    }
    
    /**
     * 更新设备配置
     */
    @PutMapping("/{deviceId}/config")
    public Result<DeviceConfig> updateDeviceConfig(
            @PathVariable String deviceId,
            @RequestBody DeviceConfig config) {
        
        log.debug("更新设备配置: {}", deviceId);
        
        config.setDeviceId(deviceId);
        config.setTimestamp(LocalDateTime.now());
        
        return Result.success(config);
    }
    
    /**
     * 生成光感数据（根据时间模拟真实光照变化）
     */
    private double generateLightValue() {
        int hour = LocalDateTime.now().getHour();
        int minute = LocalDateTime.now().getMinute();
        double timeFactor = hour + minute / 60.0;
        
        // 模拟一天的光照变化曲线
        // 6:00-18:00 为白天，光照强度较高
        // 18:00-6:00 为夜晚，光照强度较低
        
        double baseValue;
        if (hour >= 6 && hour < 18) {
            // 白天：模拟光照强度变化（中午12:00最高）
            double noonFactor = Math.abs(timeFactor - 12) / 6.0; // 0-1之间
            baseValue = 800 - (noonFactor * 600); // 200-800 lux
        } else {
            // 夜晚：0-50 lux
            baseValue = random.nextDouble() * 50;
        }
        
        // 添加随机波动（±10%）
        double variation = baseValue * 0.1 * (random.nextDouble() * 2 - 1);
        double lightValue = baseValue + variation;
        
        // 确保值在合理范围内
        return Math.max(0, Math.min(1000, lightValue));
    }
    
    private DeviceState getOrCreateDeviceState(String deviceId) {
        return deviceStates.computeIfAbsent(deviceId, k -> {
            DeviceState state = new DeviceState();
            state.setDeviceId(deviceId);
            state.setBatteryLevel(80 + random.nextInt(20)); // 80-100%
            state.setSignalStrength(-50 - random.nextInt(30)); // -50 to -80 dBm
            return state;
        });
    }
    
    @Data
    static class DeviceState {
        private String deviceId;
        private int batteryLevel;
        private int signalStrength;
    }
    
    @Data
    static class DeviceStatus {
        private String deviceId;
        private boolean online;
        private int batteryLevel;
        private int signalStrength;
        private String firmwareVersion;
        private LocalDateTime timestamp;
    }
    
    @Data
    static class LightSensorData {
        private String deviceId;
        private double lightValue;
        private LocalDateTime timestamp;
        private String dataSource;
        private String unit;
    }
    
    @Data
    static class CommandRequest {
        private String command;
        private Map<String, Object> params = new HashMap<>();
    }
    
    @Data
    static class CommandResponse {
        private String deviceId;
        private String command;
        private boolean success;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, Object> data;
    }
    
    @Data
    static class DeviceConfig {
        private String deviceId;
        private Boolean statusLightSwitch;
        private Boolean alarmLightSwitch;
        private String videoQuality;
        private Boolean nightVision;
        private Boolean motionDetection;
        private LocalDateTime timestamp;
    }
}
