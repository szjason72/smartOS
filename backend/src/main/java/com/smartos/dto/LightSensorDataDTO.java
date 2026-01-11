package com.smartos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 光感数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LightSensorDataDTO {
    
    private Long id;
    private Long deviceId;
    private String deviceCode;
    private String deviceName;
    private Long tenantId;
    private BigDecimal lightValue;
    private LocalDateTime timestamp;
    private String dataSource;
    private String metadata;
    private LocalDateTime createdAt;
}
