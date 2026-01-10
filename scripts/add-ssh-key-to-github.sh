#!/bin/bash

# 显示 SSH 公钥并打开 GitHub 添加页面

echo "=========================================="
echo "GitHub SSH Key 添加助手"
echo "=========================================="
echo ""

# 显示公钥
echo "您的 SSH 公钥内容："
echo "----------------------------------------"
cat ~/.ssh/id_ed25519_szjason72.pub
echo "----------------------------------------"
echo ""

# 复制到剪贴板（macOS）
if command -v pbcopy &> /dev/null; then
    cat ~/.ssh/id_ed25519_szjason72.pub | pbcopy
    echo "✅ 公钥已复制到剪贴板！"
    echo ""
fi

echo "下一步操作："
echo "1. 访问 GitHub: https://github.com/settings/keys"
echo "2. 点击 'New SSH key'"
echo "3. Title: SmartOS Project"
echo "4. Key: 粘贴上面的公钥（已复制到剪贴板）"
echo "5. 点击 'Add SSH key'"
echo ""

# 询问是否打开浏览器
read -p "是否打开 GitHub SSH Keys 页面? (y/n): " OPEN_BROWSER
if [ "$OPEN_BROWSER" = "y" ] || [ "$OPEN_BROWSER" = "Y" ]; then
    open "https://github.com/settings/keys"
    echo "✅ 已在浏览器中打开 GitHub SSH Keys 页面"
fi

echo ""
echo "添加完成后，运行以下命令测试："
echo "  ssh -T git@github-szjason72"
echo ""
echo "如果看到 'Hi szjason72! You've successfully authenticated'，说明配置成功！"
echo "然后可以运行："
echo "  git push -u origin main"
