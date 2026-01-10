#!/bin/bash

# SmartOS GitHub 推送脚本

echo "=========================================="
echo "SmartOS - 推送到 GitHub"
echo "=========================================="

# 检查是否已添加远程仓库
if git remote | grep -q origin; then
    echo "✅ 远程仓库已配置"
    git remote -v
else
    echo "❌ 未配置远程仓库"
    echo ""
    echo "请先创建 GitHub 仓库，然后运行："
    echo "  git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git"
    echo ""
    echo "或者使用此脚本的交互模式："
    read -p "请输入 GitHub 仓库 URL: " REPO_URL
    if [ -n "$REPO_URL" ]; then
        git remote add origin "$REPO_URL"
        echo "✅ 已添加远程仓库: $REPO_URL"
    else
        echo "❌ 未输入仓库URL，退出"
        exit 1
    fi
fi

# 检查当前分支
CURRENT_BRANCH=$(git branch --show-current)
echo ""
echo "当前分支: $CURRENT_BRANCH"

# 确认推送
read -p "是否推送到 GitHub? (y/n): " CONFIRM
if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
    echo "已取消推送"
    exit 0
fi

# 推送代码
echo ""
echo "正在推送代码..."
git branch -M main
git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✅ 推送成功！"
    echo "=========================================="
    echo ""
    echo "访问仓库:"
    git remote get-url origin
else
    echo ""
    echo "=========================================="
    echo "❌ 推送失败"
    echo "=========================================="
    echo ""
    echo "可能的原因："
    echo "1. 未配置 GitHub 认证（Personal Access Token 或 SSH Key）"
    echo "2. 仓库不存在或无权限"
    echo "3. 网络问题"
    echo ""
    echo "请参考: docs/GITHUB_PUSH_GUIDE.md"
    exit 1
fi
