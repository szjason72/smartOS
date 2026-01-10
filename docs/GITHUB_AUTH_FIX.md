# GitHub 认证问题解决方案

## 🔍 问题分析

当前 Git 配置使用的是 `xiajason` 账户，但仓库属于 `szjason72` 账户，导致权限被拒绝。

## ✅ 解决方案

### 方案一：使用 HTTPS + Personal Access Token（推荐）

#### 1. 创建 Personal Access Token

1. 登录 GitHub（szjason72 账户）
2. 访问：https://github.com/settings/tokens
3. 点击 "Generate new token" → "Generate new token (classic)"
4. 填写信息：
   - **Note**: SmartOS Project
   - **Expiration**: 根据需要选择（建议 90 days 或 No expiration）
   - **Scopes**: 勾选 `repo`（完整仓库访问权限）
5. 点击 "Generate token"
6. **重要**：复制生成的 token（只显示一次）

#### 2. 配置 Git 使用 Token

```bash
cd /Users/szjason72/smartOS

# 切换回 HTTPS
git remote set-url origin https://github.com/szjason72/smartOS.git

# 推送时使用 token 作为密码
git push -u origin main
# Username: szjason72
# Password: <粘贴您的Personal Access Token>
```

#### 3. 保存凭据（可选）

```bash
# macOS Keychain
git config --global credential.helper osxkeychain

# 推送一次后，凭据会保存
git push -u origin main
```

### 方案二：配置 szjason72 账户的 SSH Key

#### 1. 检查现有 SSH Key

```bash
ls -la ~/.ssh/
```

#### 2. 生成新的 SSH Key（如果还没有）

```bash
ssh-keygen -t ed25519 -C "szjason72@github.com" -f ~/.ssh/id_ed25519_szjason72
```

#### 3. 添加 SSH Key 到 GitHub

```bash
# 复制公钥内容
cat ~/.ssh/id_ed25519_szjason72.pub
```

然后：
1. 登录 GitHub（szjason72 账户）
2. 访问：https://github.com/settings/keys
3. 点击 "New SSH key"
4. 粘贴公钥内容
5. 保存

#### 4. 配置 SSH Config

创建或编辑 `~/.ssh/config`：

```bash
# xiajason 账户（默认）
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/id_ed25519

# szjason72 账户
Host github-szjason72
    HostName github.com
    User git
    IdentityFile ~/.ssh/id_ed25519_szjason72
```

#### 5. 更新远程仓库 URL

```bash
cd /Users/szjason72/smartOS
git remote set-url origin git@github-szjason72:szjason72/smartOS.git
git push -u origin main
```

### 方案三：使用 Git Credential Manager

```bash
# 清除旧的凭据
git credential-osxkeychain erase
host=github.com
protocol=https
[按两次回车]

# 重新推送，会提示输入用户名和密码（token）
git push -u origin main
```

---

## 🚀 快速推送（推荐使用方案一）

如果您已经创建了 Personal Access Token，可以直接运行：

```bash
cd /Users/szjason72/smartOS

# 确保使用 HTTPS
git remote set-url origin https://github.com/szjason72/smartOS.git

# 推送（会提示输入用户名和密码）
git push -u origin main
# Username: szjason72
# Password: <您的Personal Access Token>
```

---

## 📝 验证推送

推送成功后，访问以下地址验证：

https://github.com/szjason72/smartOS

应该能看到所有文件都已上传。

---

## 🔧 故障排查

### 问题1：仍然提示权限被拒绝

**解决**：
- 确认 token 有 `repo` 权限
- 确认 token 未过期
- 清除旧的凭据缓存

### 问题2：SSH 连接失败

**解决**：
- 测试 SSH 连接：`ssh -T git@github.com`
- 检查 SSH key 是否正确添加到 GitHub
- 检查 `~/.ssh/config` 配置

### 问题3：HTTPS 推送失败

**解决**：
- 确认使用 token 而不是密码
- 检查 token 权限
- 尝试清除凭据缓存后重试

---

**推荐使用方案一（HTTPS + Token）**，最简单可靠！
