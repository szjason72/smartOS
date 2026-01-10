#!/bin/bash

# SmartOS 启动脚本

echo "=========================================="
echo "SmartOS - BC4 Pro 光感智能窗帘控制系统"
echo "=========================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装JDK 17+"
    exit 1
fi

# 检查Node.js环境
if ! command -v node &> /dev/null; then
    echo "错误: 未找到Node.js环境，请先安装Node.js 18+"
    exit 1
fi

# 启动后端
echo "正在启动后端服务..."
cd backend
mvn spring-boot:run > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
echo "后端服务已启动，PID: $BACKEND_PID"

# 等待后端启动
sleep 10

# 启动前端
echo "正在启动前端服务..."
cd ../frontend
npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "前端服务已启动，PID: $FRONTEND_PID"

echo "=========================================="
echo "服务启动完成！"
echo "后端服务: http://localhost:8080"
echo "前端服务: http://localhost:5173"
echo "Swagger文档: http://localhost:8080/swagger-ui.html"
echo "=========================================="
echo "按 Ctrl+C 停止服务"

# 等待中断信号
trap "kill $BACKEND_PID $FRONTEND_PID; exit" INT TERM
wait
