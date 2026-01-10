#!/bin/bash

# SmartOS 数据库初始化脚本

DB_NAME="smartos"
DB_USER="smartos"
DB_PASSWORD="smartos123"

echo "正在初始化数据库..."

# 创建数据库（如果不存在）
psql -U postgres -c "CREATE DATABASE $DB_NAME;" 2>/dev/null || echo "数据库已存在"

# 创建用户（如果不存在）
psql -U postgres -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';" 2>/dev/null || echo "用户已存在"

# 授予权限
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;"

echo "数据库初始化完成！"
echo "数据库名: $DB_NAME"
echo "用户名: $DB_USER"
