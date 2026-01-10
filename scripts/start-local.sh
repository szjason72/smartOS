#!/bin/bash

# SmartOS 本地开发环境启动脚本

echo "=========================================="
echo "SmartOS 本地开发环境启动"
echo "=========================================="

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker未运行，请先启动Docker"
    exit 1
fi

# 启动Docker服务（PostgreSQL + Mosquitto）
echo "正在启动Docker服务..."
docker-compose up -d

# 等待服务就绪
echo "等待服务就绪..."
sleep 5

# 检查PostgreSQL
if docker exec smartos-postgres pg_isready -U smartos > /dev/null 2>&1; then
    echo "✅ PostgreSQL 已就绪"
else
    echo "❌ PostgreSQL 启动失败"
fi

# 检查Mosquitto
if docker exec smartos-mosquitto mosquitto_sub -h localhost -t '$SYS/#' -C 1 > /dev/null 2>&1; then
    echo "✅ Mosquitto MQTT Broker 已就绪"
else
    echo "⚠️  Mosquitto 可能未完全就绪，请稍后重试"
fi

echo ""
echo "=========================================="
echo "服务状态："
echo "  PostgreSQL: localhost:5432"
echo "  MQTT Broker: localhost:1883"
echo "  WebSocket:   localhost:9001"
echo "=========================================="
echo ""
echo "下一步："
echo "  1. 启动后端服务: cd backend && mvn spring-boot:run"
echo "  2. 启动前端服务: cd frontend && npm run dev"
echo ""
