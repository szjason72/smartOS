-- SmartOS 数据库初始化脚本
-- BC4 Pro 光感智能窗帘控制系统

-- 设备类型表
CREATE TABLE IF NOT EXISTS device_types (
    id BIGSERIAL PRIMARY KEY,
    device_type_code VARCHAR(50) NOT NULL UNIQUE,
    device_type_name VARCHAR(255) NOT NULL,
    device_series VARCHAR(50),
    manufacturer VARCHAR(100) DEFAULT 'SuperAcme',
    device_category VARCHAR(50) DEFAULT 'camera',
    icon_resource VARCHAR(255),
    default_config JSONB,
    capabilities JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_device_types_code ON device_types(device_type_code);
CREATE INDEX idx_device_types_series ON device_types(device_series);

-- 插入 BC4 Pro 设备类型
INSERT INTO device_types 
(device_type_code, device_type_name, device_series, icon_resource, default_config, capabilities)
VALUES
('CINMOORE_BC4', 'BC4 Pro', 'BC4_PRO_SERIES', 'cameraIconBC4', 
 '{"reset_timeout": 30, "heartbeat_interval": 30}'::jsonb,
 '{"supports_light_sensor": true, "supports_image_analysis": true}'::jsonb)
ON CONFLICT (device_type_code) DO NOTHING;

-- 智能设备表
CREATE TABLE IF NOT EXISTS smart_devices (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    device_code VARCHAR(100) NOT NULL UNIQUE,
    device_name VARCHAR(255) NOT NULL,
    device_type_code VARCHAR(50) NOT NULL REFERENCES device_types(device_type_code),
    device_model VARCHAR(100),
    manufacturer VARCHAR(100) DEFAULT 'SuperAcme',
    firmware_version VARCHAR(50),
    ip_address VARCHAR(50),
    mac_address VARCHAR(50),
    location VARCHAR(255),
    status VARCHAR(20) DEFAULT 'offline',
    config JSONB,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE(tenant_id, device_code)
);

CREATE INDEX idx_devices_tenant_id ON smart_devices(tenant_id);
CREATE INDEX idx_devices_device_type ON smart_devices(device_type_code);
CREATE INDEX idx_devices_status ON smart_devices(status);

-- 光感数据记录表
CREATE TABLE IF NOT EXISTS light_sensor_data (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES smart_devices(id),
    tenant_id BIGINT NOT NULL,
    light_value DECIMAL(10, 2) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_source VARCHAR(50) DEFAULT 'bc4_pro',
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_light_sensor_device_id ON light_sensor_data(device_id);
CREATE INDEX idx_light_sensor_timestamp ON light_sensor_data(timestamp);
CREATE INDEX idx_light_sensor_tenant_id ON light_sensor_data(tenant_id);

-- 光感窗帘联动配置表
CREATE TABLE IF NOT EXISTS light_curtain_automation (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    light_sensor_device_id BIGINT NOT NULL REFERENCES smart_devices(id),
    curtain_device_id BIGINT NOT NULL REFERENCES smart_devices(id),
    enabled BOOLEAN DEFAULT true,
    open_threshold DECIMAL(10, 2),
    close_threshold DECIMAL(10, 2),
    open_position INTEGER DEFAULT 100,
    close_position INTEGER DEFAULT 0,
    start_time TIME,
    end_time TIME,
    debounce_seconds INTEGER DEFAULT 30,
    check_interval_seconds INTEGER DEFAULT 60,
    last_control_time TIMESTAMP,
    config JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_light_curtain_tenant_id ON light_curtain_automation(tenant_id);
CREATE INDEX idx_light_curtain_enabled ON light_curtain_automation(enabled);

-- 光感数据统计表
CREATE TABLE IF NOT EXISTS light_sensor_stats (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES smart_devices(id),
    tenant_id BIGINT NOT NULL,
    date DATE NOT NULL,
    avg_light_value DECIMAL(10, 2),
    min_light_value DECIMAL(10, 2),
    max_light_value DECIMAL(10, 2),
    sample_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(device_id, date)
);

CREATE INDEX idx_light_stats_device_date ON light_sensor_stats(device_id, date);
