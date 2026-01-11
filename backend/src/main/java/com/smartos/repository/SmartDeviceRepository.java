package com.smartos.repository;

import com.smartos.model.SmartDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 智能设备仓库接口
 */
@Repository
public interface SmartDeviceRepository extends JpaRepository<SmartDevice, Long> {
    
    /**
     * 根据设备编码查询设备
     */
    Optional<SmartDevice> findByDeviceCode(String deviceCode);
    
    /**
     * 根据设备编码和租户ID查询设备
     */
    Optional<SmartDevice> findByDeviceCodeAndTenantId(String deviceCode, Long tenantId);
    
    /**
     * 根据租户ID查询所有设备
     */
    List<SmartDevice> findByTenantId(Long tenantId);
    
    /**
     * 根据设备类型编码查询设备
     */
    List<SmartDevice> findByDeviceTypeCode(String deviceTypeCode);
    
    /**
     * 根据租户ID和设备类型编码查询设备
     */
    List<SmartDevice> findByTenantIdAndDeviceTypeCode(Long tenantId, String deviceTypeCode);
    
    /**
     * 查询支持光感传感器的设备
     */
    @Query("SELECT d FROM SmartDevice d WHERE d.deviceTypeCode = 'CINMOORE_BC4' AND d.deletedAt IS NULL")
    List<SmartDevice> findLightSensorDevices();
    
    /**
     * 根据租户ID查询支持光感传感器的设备
     */
    @Query("SELECT d FROM SmartDevice d WHERE d.tenantId = :tenantId " +
           "AND d.deviceTypeCode = 'CINMOORE_BC4' AND d.deletedAt IS NULL")
    List<SmartDevice> findLightSensorDevicesByTenantId(@Param("tenantId") Long tenantId);
}
