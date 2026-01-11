package com.smartos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 光感数据实体
 */
@Entity
@Table(name = "light_sensor_data", indexes = {
    @Index(name = "idx_light_sensor_device_id", columnList = "device_id"),
    @Index(name = "idx_light_sensor_timestamp", columnList = "timestamp"),
    @Index(name = "idx_light_sensor_tenant_id", columnList = "tenant_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LightSensorData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 设备ID（关联 smart_devices 表）
     */
    @Column(name = "device_id", nullable = false)
    private Long deviceId;
    
    /**
     * 租户ID
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    /**
     * 光照强度值（单位：lux）
     */
    @Column(name = "light_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal lightValue;
    
    /**
     * 数据时间戳
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * 数据来源（如：bc4_pro, mock, image_analysis）
     */
    @Column(name = "data_source", length = 50)
    @Builder.Default
    private String dataSource = "bc4_pro";
    
    /**
     * 额外元数据（JSON格式）
     * 可存储：图像亮度、曝光值、采集方法等
     */
    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
