#!/bin/bash

# Mac 记事本构建脚本

echo "🚀 开始构建 Mac 记事本应用..."

# 检查 Xcode 是否安装
if ! command -v xcodebuild &> /dev/null; then
    echo "❌ 错误: 未找到 Xcode，请先安装 Xcode"
    exit 1
fi

# 清理之前的构建
echo "🧹 清理之前的构建文件..."
xcodebuild clean -project MacNotepad.xcodeproj -scheme MacNotepad

# 构建项目
echo "🔨 构建项目..."
xcodebuild build -project MacNotepad.xcodeproj -scheme MacNotepad -configuration Release

if [ $? -eq 0 ]; then
    echo "✅ 构建成功！"
    echo "📁 构建文件位置: build/Release/"
    
    # 可选：打开构建目录
    read -p "是否要打开构建目录? (y/n): " open_folder
    if [[ $open_folder == "y" || $open_folder == "Y" ]]; then
        open build/Release/
    fi
else
    echo "❌ 构建失败！请检查错误信息"
    exit 1
fi

echo "🎉 完成！现在你可以在 Xcode 中运行应用，或者从构建目录中启动 MacNotepad.app"