package com.smartos.service;

import com.smartos.dto.LightSensorDataDTO;
import com.smartos.dto.LightSensorQueryRequest;
import com.smartos.dto.LightSensorStatsDTO;
import com.smartos.model.LightSensorData;
import com.smartos.model.SmartDevice;
import com.smartos.repository.LightSensorDataRepository;
import com.smartos.repository.SmartDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 光感数据服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LightSensorService {
    
    private final LightSensorDataRepository lightSensorDataRepository;
    private final SmartDeviceRepository smartDeviceRepository;
    
    /**
     * 保存光感数据
     */
    @Transactional
    public LightSensorDataDTO saveLightSensorData(Long deviceId, Long tenantId, 
                                                   BigDecimal lightValue, 
                                                   String dataSource, 
                                                   String metadata) {
        log.debug("保存光感数据: deviceId={}, tenantId={}, lightValue={}", 
                 deviceId, tenantId, lightValue);
        
        LightSensorData data = LightSensorData.builder()
                .deviceId(deviceId)
                .tenantId(tenantId)
                .lightValue(lightValue)
                .timestamp(LocalDateTime.now())
                .dataSource(dataSource != null ? dataSource : "bc4_pro")
                .metadata(metadata)
                .createdAt(LocalDateTime.now())
                .build();
        
        LightSensorData saved = lightSensorDataRepository.save(data);
        return convertToDTO(saved);
    }
    
    /**
     * 根据设备编码保存光感数据
     */
    @Transactional
    public LightSensorDataDTO saveLightSensorDataByDeviceCode(String deviceCode, 
                                                               Long tenantId,
                                                               BigDecimal lightValue,
                                                               String dataSource,
                                                               String metadata) {
        SmartDevice device = smartDeviceRepository.findByDeviceCodeAndTenantId(deviceCode, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "设备不存在: deviceCode=" + deviceCode + ", tenantId=" + tenantId));
        
        return saveLightSensorData(device.getId(), tenantId, lightValue, dataSource, metadata);
    }
    
    /**
     * 获取最新的光感数据
     */
    public Optional<LightSensorDataDTO> getLatestLightSensorData(Long deviceId, Long tenantId) {
        return lightSensorDataRepository
                .findFirstByDeviceIdAndTenantIdOrderByTimestampDesc(deviceId, tenantId)
                .map(this::convertToDTO);
    }
    
    /**
     * 根据设备编码获取最新的光感数据
     */
    public Optional<LightSensorDataDTO> getLatestLightSensorDataByDeviceCode(String deviceCode, 
                                                                              Long tenantId) {
        return smartDeviceRepository.findByDeviceCodeAndTenantId(deviceCode, tenantId)
                .flatMap(device -> lightSensorDataRepository
                        .findFirstByDeviceIdAndTenantIdOrderByTimestampDesc(device.getId(), tenantId)
                        .map(this::convertToDTO));
    }
    
    /**
     * 查询光感数据（分页）
     */
    public Page<LightSensorDataDTO> queryLightSensorData(LightSensorQueryRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 20,
                Sort.by(Sort.Direction.DESC, "timestamp"));
        
        Page<LightSensorData> page;
        
        if (request.getDeviceId() != null) {
            // 根据设备ID查询
            LocalDateTime startTime = request.getStartTime() != null ? 
                    request.getStartTime() : LocalDateTime.now().minusDays(7);
            LocalDateTime endTime = request.getEndTime() != null ? 
                    request.getEndTime() : LocalDateTime.now();
            
            page = lightSensorDataRepository.findByDeviceIdAndTenantIdAndTimestampBetween(
                    request.getDeviceId(), 
                    request.getTenantId() != null ? request.getTenantId() : 0L,
                    startTime, 
                    endTime, 
                    pageable);
        } else if (request.getDeviceCode() != null) {
            // 根据设备编码查询
            SmartDevice device = smartDeviceRepository
                    .findByDeviceCodeAndTenantId(request.getDeviceCode(), 
                            request.getTenantId() != null ? request.getTenantId() : 0L)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "设备不存在: deviceCode=" + request.getDeviceCode()));
            
            LocalDateTime startTime = request.getStartTime() != null ? 
                    request.getStartTime() : LocalDateTime.now().minusDays(7);
            LocalDateTime endTime = request.getEndTime() != null ? 
                    request.getEndTime() : LocalDateTime.now();
            
            page = lightSensorDataRepository.findByDeviceIdAndTenantIdAndTimestampBetween(
                    device.getId(),
                    request.getTenantId() != null ? request.getTenantId() : 0L,
                    startTime,
                    endTime,
                    pageable);
        } else {
            // 根据租户ID查询
            LocalDateTime startTime = request.getStartTime() != null ? 
                    request.getStartTime() : LocalDateTime.now().minusDays(7);
            LocalDateTime endTime = request.getEndTime() != null ? 
                    request.getEndTime() : LocalDateTime.now();
            
            page = lightSensorDataRepository.findByTenantIdAndTimestampBetween(
                    request.getTenantId() != null ? request.getTenantId() : 0L,
                    startTime,
                    endTime,
                    pageable);
        }
        
        return page.map(this::convertToDTO);
    }
    
    /**
     * 获取最近N条光感数据
     */
    public List<LightSensorDataDTO> getRecentLightSensorData(Long deviceId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return lightSensorDataRepository.findRecentByDeviceId(deviceId, pageable)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取统计数据
     */
    public LightSensorStatsDTO getStatistics(Long deviceId, Long tenantId,
                                             LocalDateTime startTime, 
                                             LocalDateTime endTime) {
        SmartDevice device = smartDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: deviceId=" + deviceId));
        
        // 获取统计数据
        Double avgValue = lightSensorDataRepository
                .findAverageLightValueByDeviceIdAndTimestampBetween(deviceId, startTime, endTime);
        Double minValue = lightSensorDataRepository
                .findMinLightValueByDeviceIdAndTimestampBetween(deviceId, startTime, endTime);
        Double maxValue = lightSensorDataRepository
                .findMaxLightValueByDeviceIdAndTimestampBetween(deviceId, startTime, endTime);
        Long totalCount = lightSensorDataRepository
                .countByDeviceIdAndTimestampBetween(deviceId, startTime, endTime);
        
        // 获取最新数据
        Optional<LightSensorData> latest = lightSensorDataRepository
                .findFirstByDeviceIdAndTenantIdOrderByTimestampDesc(deviceId, tenantId);
        
        return LightSensorStatsDTO.builder()
                .deviceId(deviceId)
                .deviceCode(device.getDeviceCode())
                .deviceName(device.getDeviceName())
                .startTime(startTime)
                .endTime(endTime)
                .totalCount(totalCount)
                .avgLightValue(avgValue != null ? BigDecimal.valueOf(avgValue) : null)
                .minLightValue(minValue != null ? BigDecimal.valueOf(minValue) : null)
                .maxLightValue(maxValue != null ? BigDecimal.valueOf(maxValue) : null)
                .latestLightValue(latest.map(LightSensorData::getLightValue).orElse(null))
                .latestTimestamp(latest.map(LightSensorData::getTimestamp).orElse(null))
                .build();
    }
    
    /**
     * 转换为DTO
     */
    private LightSensorDataDTO convertToDTO(LightSensorData data) {
        SmartDevice device = smartDeviceRepository.findById(data.getDeviceId())
                .orElse(null);
        
        return LightSensorDataDTO.builder()
                .id(data.getId())
                .deviceId(data.getDeviceId())
                .deviceCode(device != null ? device.getDeviceCode() : null)
                .deviceName(device != null ? device.getDeviceName() : null)
                .tenantId(data.getTenantId())
                .lightValue(data.getLightValue())
                .timestamp(data.getTimestamp())
                .dataSource(data.getDataSource())
                .metadata(data.getMetadata())
                .createdAt(data.getCreatedAt())
                .build();
    }
}
