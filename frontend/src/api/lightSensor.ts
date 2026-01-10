import request from './request'

export interface LightSensorData {
  id: number
  deviceId: number
  lightValue: number
  timestamp: string
  dataSource: string
}

export const lightSensorApi = {
  // 采集光感数据
  collectLightData: (deviceId: number, lightValue: number, metadata?: any) =>
    request.post('/light-sensor/collect', { deviceId, lightValue, metadata }),

  // 获取光感数据
  getLightData: (deviceId: number, limit?: number) =>
    request.get<LightSensorData[]>(`/light-sensor/${deviceId}/data`, {
      params: { limit: limit || 100 },
    }),

  // 获取最新数据
  getLatestData: (deviceId: number) =>
    request.get<LightSensorData>(`/light-sensor/${deviceId}/latest`),
}
