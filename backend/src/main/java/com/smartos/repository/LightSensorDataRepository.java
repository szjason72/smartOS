package com.smartos.repository;

import com.smartos.model.LightSensorData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 光感数据仓库接口
 */
@Repository
public interface LightSensorDataRepository extends JpaRepository<LightSensorData, Long> {
    
    /**
     * 根据设备ID查询最新的光感数据
     */
    Optional<LightSensorData> findFirstByDeviceIdOrderByTimestampDesc(Long deviceId);
    
    /**
     * 根据设备ID和租户ID查询最新的光感数据
     */
    Optional<LightSensorData> findFirstByDeviceIdAndTenantIdOrderByTimestampDesc(
            Long deviceId, Long tenantId);
    
    /**
     * 根据设备ID查询指定时间范围内的数据
     */
    List<LightSensorData> findByDeviceIdAndTimestampBetween(
            Long deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据设备ID和租户ID查询指定时间范围内的数据（分页）
     */
    Page<LightSensorData> findByDeviceIdAndTenantIdAndTimestampBetween(
            Long deviceId, Long tenantId, 
            LocalDateTime startTime, LocalDateTime endTime, 
            Pageable pageable);
    
    /**
     * 根据租户ID查询指定时间范围内的数据
     */
    Page<LightSensorData> findByTenantIdAndTimestampBetween(
            Long tenantId, LocalDateTime startTime, LocalDateTime endTime, 
            Pageable pageable);
    
    /**
     * 根据设备ID查询最近N条数据
     */
    @Query("SELECT l FROM LightSensorData l WHERE l.deviceId = :deviceId " +
           "ORDER BY l.timestamp DESC")
    List<LightSensorData> findRecentByDeviceId(
            @Param("deviceId") Long deviceId, 
            org.springframework.data.domain.Pageable pageable);
    
    /**
     * 统计指定时间范围内的数据量
     */
    long countByDeviceIdAndTimestampBetween(
            Long deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询指定时间范围内的平均光照值
     */
    @Query("SELECT AVG(l.lightValue) FROM LightSensorData l " +
           "WHERE l.deviceId = :deviceId AND l.timestamp BETWEEN :startTime AND :endTime")
    Double findAverageLightValueByDeviceIdAndTimestampBetween(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询指定时间范围内的最小光照值
     */
    @Query("SELECT MIN(l.lightValue) FROM LightSensorData l " +
           "WHERE l.deviceId = :deviceId AND l.timestamp BETWEEN :startTime AND :endTime")
    Double findMinLightValueByDeviceIdAndTimestampBetween(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询指定时间范围内的最大光照值
     */
    @Query("SELECT MAX(l.lightValue) FROM LightSensorData l " +
           "WHERE l.deviceId = :deviceId AND l.timestamp BETWEEN :startTime AND :endTime")
    Double findMaxLightValueByDeviceIdAndTimestampBetween(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
