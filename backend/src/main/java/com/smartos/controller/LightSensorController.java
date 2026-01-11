package com.smartos.controller;

import com.smartos.common.Result;
import com.smartos.dto.LightSensorDataDTO;
import com.smartos.dto.LightSensorQueryRequest;
import com.smartos.dto.LightSensorStatsDTO;
import com.smartos.service.LightSensorCollectionService;
import com.smartos.service.LightSensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 光感数据控制器
 */
@Slf4j
@RestController
@RequestMapping("/light-sensor")
@RequiredArgsConstructor
@Tag(name = "光感数据", description = "光感数据采集和查询API")
public class LightSensorController {
    
    private final LightSensorService lightSensorService;
    private final LightSensorCollectionService collectionService;
    
    /**
     * 获取最新的光感数据
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新光感数据", description = "根据设备ID或设备编码获取最新的光感数据")
    public Result<LightSensorDataDTO> getLatest(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String deviceCode,
            @RequestParam(defaultValue = "0") Long tenantId) {
        
        Optional<LightSensorDataDTO> data;
        
        if (deviceId != null) {
            data = lightSensorService.getLatestLightSensorData(deviceId, tenantId);
        } else if (deviceCode != null) {
            data = lightSensorService.getLatestLightSensorDataByDeviceCode(deviceCode, tenantId);
        } else {
            return Result.error("deviceId 或 deviceCode 必须提供一个");
        }
        
        return data.map(Result::success)
                .orElse(Result.error("未找到光感数据"));
    }
    
    /**
     * 查询光感数据（分页）
     */
    @PostMapping("/query")
    @Operation(summary = "查询光感数据", description = "根据条件查询光感数据，支持分页")
    public Result<Page<LightSensorDataDTO>> query(@RequestBody LightSensorQueryRequest request) {
        try {
            Page<LightSensorDataDTO> page = lightSensorService.queryLightSensorData(request);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询光感数据失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取最近N条光感数据
     */
    @GetMapping("/recent")
    @Operation(summary = "获取最近N条光感数据", description = "根据设备ID获取最近N条光感数据")
    public Result<List<LightSensorDataDTO>> getRecent(
            @RequestParam Long deviceId,
            @RequestParam(defaultValue = "20") int limit) {
        
        List<LightSensorDataDTO> data = lightSensorService.getRecentLightSensorData(deviceId, limit);
        return Result.success(data);
    }
    
    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计数据", description = "根据时间范围获取光感数据统计信息")
    public Result<LightSensorStatsDTO> getStatistics(
            @RequestParam Long deviceId,
            @RequestParam(defaultValue = "0") Long tenantId,
            @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        // 默认查询最近7天的数据
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        
        try {
            LightSensorStatsDTO stats = lightSensorService.getStatistics(
                    deviceId, tenantId, startTime, endTime);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动触发采集
     */
    @PostMapping("/collect")
    @Operation(summary = "手动触发采集", description = "手动触发指定设备的光感数据采集")
    public Result<LightSensorDataDTO> manualCollect(
            @RequestParam String deviceCode,
            @RequestParam(defaultValue = "0") Long tenantId) {
        
        try {
            LightSensorDataDTO data = collectionService.manualCollect(deviceCode, tenantId);
            return Result.success(data);
        } catch (Exception e) {
            log.error("手动采集失败", e);
            return Result.error("采集失败: " + e.getMessage());
        }
    }
}
