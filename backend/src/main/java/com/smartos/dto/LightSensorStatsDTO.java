package com.smartos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 光感数据统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LightSensorStatsDTO {
    
    private Long deviceId;
    private String deviceCode;
    private String deviceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long totalCount;
    private BigDecimal avgLightValue;
    private BigDecimal minLightValue;
    private BigDecimal maxLightValue;
    private BigDecimal latestLightValue;
    private LocalDateTime latestTimestamp;
}
