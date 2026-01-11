package com.smartos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartos.dto.LightSensorDataDTO;
import com.smartos.model.SmartDevice;
import com.smartos.repository.SmartDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 光感数据采集服务
 * 定时从BC4 Pro设备采集光感数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LightSensorCollectionService {
    
    private final LightSensorService lightSensorService;
    private final SmartDeviceRepository smartDeviceRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${smartos.bc4.api.base-url:http://localhost:8080/mock/bc4}")
    private String bc4ApiBaseUrl;
    
    @Value("${smartos.bc4.light-sensor.collection-interval:3600}")
    private int collectionInterval;
    
    /**
     * 定时采集光感数据
     * 默认每1小时采集一次
     */
    @Scheduled(fixedDelayString = "${smartos.bc4.light-sensor.collection-interval:3600}000")
    public void collectLightSensorData() {
        log.debug("开始定时采集光感数据");
        
        try {
            // 查询所有BC4 Pro设备
            List<SmartDevice> devices = smartDeviceRepository.findLightSensorDevices();
            
            if (devices.isEmpty()) {
                log.debug("没有找到BC4 Pro设备，跳过采集");
                return;
            }
            
            // 遍历设备采集数据
            for (SmartDevice device : devices) {
                try {
                    collectDeviceLightSensorData(device);
                } catch (Exception e) {
                    log.error("采集设备光感数据失败: deviceCode={}", device.getDeviceCode(), e);
                }
            }
            
            log.debug("光感数据采集完成，共处理{}个设备", devices.size());
        } catch (Exception e) {
            log.error("定时采集光感数据失败", e);
        }
    }
    
    /**
     * 采集指定设备的光感数据
     */
    public LightSensorDataDTO collectDeviceLightSensorData(SmartDevice device) {
        log.debug("采集设备光感数据: deviceCode={}", device.getDeviceCode());
        
        try {
            // 调用BC4 Mock API获取光感数据
            String url = bc4ApiBaseUrl + "/" + device.getDeviceCode() + "/lightSensor";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("获取光感数据失败: " + response.getStatusCode());
            }
            
            Map<String, Object> responseBody = response.getBody();
            
            // 解析响应数据
            Map<String, Object> dataMap = (Map<String, Object>) responseBody.get("data");
            if (dataMap == null) {
                throw new RuntimeException("响应数据格式错误: data字段为空");
            }
            
            Double lightValue = ((Number) dataMap.get("lightValue")).doubleValue();
            String dataSource = (String) dataMap.getOrDefault("dataSource", "bc4_pro");
            
            // 构建元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("collection_method", "api");
            metadata.put("collection_time", LocalDateTime.now().toString());
            metadata.put("device_code", device.getDeviceCode());
            metadata.put("device_name", device.getDeviceName());
            
            String metadataJson = objectMapper.writeValueAsString(metadata);
            
            // 保存光感数据
            LightSensorDataDTO saved = lightSensorService.saveLightSensorDataByDeviceCode(
                    device.getDeviceCode(),
                    device.getTenantId(),
                    BigDecimal.valueOf(lightValue),
                    dataSource,
                    metadataJson
            );
            
            log.info("成功采集光感数据: deviceCode={}, lightValue={} lux", 
                    device.getDeviceCode(), lightValue);
            
            return saved;
        } catch (Exception e) {
            log.error("采集设备光感数据失败: deviceCode={}", device.getDeviceCode(), e);
            throw new RuntimeException("采集光感数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 手动触发采集（用于测试）
     */
    public LightSensorDataDTO manualCollect(String deviceCode, Long tenantId) {
        SmartDevice device = smartDeviceRepository
                .findByDeviceCodeAndTenantId(deviceCode, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "设备不存在: deviceCode=" + deviceCode));
        
        return collectDeviceLightSensorData(device);
    }
}
