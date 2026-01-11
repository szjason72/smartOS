package com.smartos.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 光感数据查询请求
 */
@Data
public class LightSensorQueryRequest {
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 设备编码
     */
    private String deviceCode;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 数据来源
     */
    private String dataSource;
    
    /**
     * 页码（从0开始）
     */
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    private Integer size = 20;
}
