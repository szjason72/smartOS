-- 插入测试设备数据
-- 用于测试光感数据采集功能

-- 插入BC4 Pro测试设备
INSERT INTO smart_devices 
(tenant_id, device_code, device_name, device_type_code, device_model, manufacturer, 
 status, location, created_at, updated_at)
VALUES
(0, 'BC4_001', 'BC4 Pro 测试设备1', 'CINMOORE_BC4', 'BC4_PRO', 'SuperAcme',
 'online', '客厅', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(0, 'BC4_002', 'BC4 Pro 测试设备2', 'CINMOORE_BC4', 'BC4_PRO', 'SuperAcme',
 'online', '卧室', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (device_code) DO NOTHING;
