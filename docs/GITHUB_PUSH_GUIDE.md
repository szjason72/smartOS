# GitHub 推送指南

## 📋 推送步骤

### 1. 配置 Git 用户信息（如果未配置）

```bash
git config user.name "Your Name"
git config user.email "your.email@example.com"
```

### 2. 在 GitHub 上创建仓库

1. 登录 GitHub
2. 点击右上角的 "+" → "New repository"
3. 填写仓库信息：
   - **Repository name**: `smartOS` 或 `smartos-bc4-curtain`
   - **Description**: BC4 Pro 光感智能窗帘控制系统
   - **Visibility**: Public 或 Private（根据需求）
   - **不要**勾选 "Initialize this repository with a README"（因为我们已经有了）

### 3. 添加远程仓库并推送

```bash
cd /Users/szjason72/smartOS

# 添加远程仓库（替换 YOUR_USERNAME 和 REPO_NAME）
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git

# 或者使用 SSH（如果配置了 SSH key）
# git remote add origin git@github.com:YOUR_USERNAME/REPO_NAME.git

# 推送代码
git branch -M main
git push -u origin main
```

### 4. 验证推送

访问 GitHub 仓库页面，确认所有文件都已上传。

---

## 🔐 认证方式

### 方式一：HTTPS + Personal Access Token（推荐）

1. 在 GitHub 创建 Personal Access Token：
   - Settings → Developer settings → Personal access tokens → Tokens (classic)
   - 生成新 token，勾选 `repo` 权限

2. 推送时使用 token 作为密码：
```bash
git push -u origin main
# Username: 你的GitHub用户名
# Password: 你的Personal Access Token
```

### 方式二：SSH Key

1. 生成 SSH key（如果还没有）：
```bash
ssh-keygen -t ed25519 -C "your.email@example.com"
```

2. 添加 SSH key 到 GitHub：
   - 复制 `~/.ssh/id_ed25519.pub` 内容
   - GitHub → Settings → SSH and GPG keys → New SSH key

3. 使用 SSH URL：
```bash
git remote set-url origin git@github.com:YOUR_USERNAME/REPO_NAME.git
git push -u origin main
```

---

## 📝 常用 Git 命令

```bash
# 查看状态
git status

# 查看提交历史
git log --oneline

# 查看远程仓库
git remote -v

# 推送代码
git push origin main

# 拉取代码
git pull origin main

# 添加文件
git add .
git commit -m "提交信息"
git push
```

---

## 🎯 快速推送脚本

创建 `scripts/push-to-github.sh`：

```bash
#!/bin/bash

# 设置 GitHub 仓库地址
GITHUB_REPO="https://github.com/YOUR_USERNAME/REPO_NAME.git"

# 检查是否已添加远程仓库
if ! git remote | grep -q origin; then
    echo "添加远程仓库..."
    git remote add origin $GITHUB_REPO
fi

# 推送代码
echo "推送代码到 GitHub..."
git branch -M main
git push -u origin main

echo "完成！"
```

---

**注意**：请将 `YOUR_USERNAME` 和 `REPO_NAME` 替换为实际的 GitHub 用户名和仓库名。
