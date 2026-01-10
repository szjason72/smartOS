# SSH 连接故障排查

## ✅ 当前状态

- ✅ SSH Key 已生成：`~/.ssh/id_ed25519_szjason72`
- ✅ SSH Config 已配置：`github-szjason72` 主机配置正确
- ✅ 可以连接到 GitHub（不再显示 "Could not resolve hostname"）
- ❌ 公钥认证失败：需要将公钥添加到 GitHub

## 🔍 问题分析

**错误信息**：`Permission denied (publickey)`

**原因**：SSH 公钥还没有添加到 GitHub 账户，或者添加的公钥不匹配。

## 📋 解决步骤

### 1. 确认您的 SSH 公钥

```bash
cat ~/.ssh/id_ed25519_szjason72.pub
```

**公钥内容**：
```
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIOtBol+guqSD2tmh9U3nik50ssk0Y+6vEkbPF6CvESmk szjason72@github.com
```

### 2. 添加到 GitHub

#### 方法一：使用网页界面

1. **登录 GitHub**（使用 szjason72 账户）
2. **访问 SSH Keys 设置**：
   - 直接访问：https://github.com/settings/keys
   - 或：Settings → SSH and GPG keys
3. **添加新的 SSH Key**：
   - 点击 "New SSH key"
   - **Title**: `SmartOS Project` 或 `MacBook - szjason72`
   - **Key type**: Authentication Key
   - **Key**: 粘贴上面的公钥内容
   - 点击 "Add SSH key"
4. **确认添加**：输入 GitHub 密码确认

#### 方法二：使用 GitHub CLI（如果已安装）

```bash
gh auth login
gh ssh-key add ~/.ssh/id_ed25519_szjason72.pub --title "SmartOS Project"
```

### 3. 验证添加

添加完成后，测试连接：

```bash
ssh -T git@github-szjason72
```

**成功输出**：
```
Hi szjason72! You've successfully authenticated, but GitHub does not provide shell access.
```

### 4. 推送代码

```bash
cd /Users/szjason72/smartOS
git push -u origin main
```

## 🔧 常见问题

### 问题1：仍然显示 "Permission denied"

**检查清单**：
- [ ] 确认使用 szjason72 账户登录 GitHub
- [ ] 确认公钥内容完全一致（包括开头和结尾）
- [ ] 确认公钥已保存（刷新页面检查）
- [ ] 等待几秒钟让 GitHub 更新（有时需要几秒）

**解决方法**：
```bash
# 重新测试，添加详细输出
ssh -vT git@github-szjason72

# 检查使用的密钥
ssh -vT git@github-szjason72 2>&1 | grep "Offering public key"
```

### 问题2：连接超时或 "Connection reset"

**可能原因**：
- 网络问题
- 防火墙阻止
- GitHub 服务问题

**解决方法**：
```bash
# 测试 GitHub 连接
ping github.com

# 测试 SSH 端口
nc -zv github.com 22

# 使用详细模式查看连接过程
ssh -vT git@github-szjason72
```

### 问题3：SSH Config 不生效

**检查**：
```bash
# 检查配置文件权限
ls -la ~/.ssh/config

# 应该是 600 权限
chmod 600 ~/.ssh/config

# 检查配置语法
ssh -F ~/.ssh/config -T git@github-szjason72
```

## 📝 快速命令参考

```bash
# 显示公钥
cat ~/.ssh/id_ed25519_szjason72.pub

# 测试 SSH 连接
ssh -T git@github-szjason72

# 详细测试（查看使用的密钥）
ssh -vT git@github-szjason72 2>&1 | grep -E "identity|Offering|Successfully"

# 推送代码
cd /Users/szjason72/smartOS
git push -u origin main
```

## 🎯 下一步

1. ✅ 将公钥添加到 GitHub
2. ✅ 测试 SSH 连接
3. ✅ 推送代码到 GitHub

---

**完成公钥添加后，运行 `ssh -T git@github-szjason72` 测试连接！**
