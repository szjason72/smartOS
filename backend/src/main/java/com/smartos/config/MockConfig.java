package com.smartos.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Mock设备配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "smartos.local")
public class MockConfig {
    
    /**
     * 是否启用本地模拟模式
     */
    private boolean enabled = true;
    
    /**
     * Mock设备配置
     */
    private MockDevice mockDevice = new MockDevice();
    
    @Data
    public static class MockDevice {
        /**
         * 是否启用Mock设备
         */
        private boolean enabled = true;
        
        /**
         * Mock API基础URL
         */
        private String baseUrl = "http://localhost:8080/mock/bc4";
    }
}
