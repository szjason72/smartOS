package com.smartos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 智能设备实体
 */
@Entity
@Table(name = "smart_devices", indexes = {
    @Index(name = "idx_devices_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_devices_device_type", columnList = "device_type_code"),
    @Index(name = "idx_devices_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartDevice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 租户ID
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    /**
     * 设备编码（唯一）
     */
    @Column(name = "device_code", nullable = false, unique = true, length = 100)
    private String deviceCode;
    
    /**
     * 设备名称
     */
    @Column(name = "device_name", nullable = false, length = 255)
    private String deviceName;
    
    /**
     * 设备类型编码（关联 device_types 表）
     */
    @Column(name = "device_type_code", nullable = false, length = 50)
    private String deviceTypeCode;
    
    /**
     * 设备型号
     */
    @Column(name = "device_model", length = 100)
    private String deviceModel;
    
    /**
     * 制造商
     */
    @Column(name = "manufacturer", length = 100)
    @Builder.Default
    private String manufacturer = "SuperAcme";
    
    /**
     * 固件版本
     */
    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;
    
    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    /**
     * MAC地址
     */
    @Column(name = "mac_address", length = 50)
    private String macAddress;
    
    /**
     * 位置信息
     */
    @Column(name = "location", length = 255)
    private String location;
    
    /**
     * 设备状态（online, offline, error）
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "offline";
    
    /**
     * 设备配置（JSON格式）
     */
    @Column(name = "config", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String config;
    
    /**
     * 设备元数据（JSON格式）
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
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 删除时间（软删除）
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
