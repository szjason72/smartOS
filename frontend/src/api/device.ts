import request from './request'

export interface Device {
  id: number
  deviceCode: string
  deviceName: string
  deviceTypeCode: string
  status: string
  ipAddress?: string
  location?: string
}

export const deviceApi = {
  // 获取设备列表
  getDeviceList: () => request.get<Device[]>('/devices'),

  // 获取设备详情
  getDeviceDetail: (id: number) => request.get<Device>(`/devices/${id}`),

  // 注册设备
  registerDevice: (data: any) => request.post<Device>('/devices', data),

  // 更新设备
  updateDevice: (id: number, data: any) => request.put<Device>(`/devices/${id}`, data),

  // 删除设备
  deleteDevice: (id: number) => request.delete(`/devices/${id}`),
}
