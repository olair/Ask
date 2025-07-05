# Mac 记事本 安装指南

## 快速开始

### 方法 1: 使用 Xcode（推荐）

1. **下载项目**
   ```bash
   # 如果你有 git，可以克隆项目
   git clone [项目地址]
   # 或者直接下载项目文件
   ```

2. **打开项目**
   - 启动 Xcode
   - 选择 "Open a project or file"
   - 找到并选择 `MacNotepad.xcodeproj` 文件

3. **运行应用**
   - 在 Xcode 中按 `Cmd + R` 运行
   - 或者点击左上角的播放按钮 ▶️

### 方法 2: 使用命令行构建

1. **使用提供的构建脚本**
   ```bash
   cd MacNotepad
   ./build.sh
   ```

2. **手动构建**
   ```bash
   cd MacNotepad
   xcodebuild build -project MacNotepad.xcodeproj -scheme MacNotepad -configuration Release
   ```

## 首次使用

1. **启动应用后**
   - 应用会在菜单栏（屏幕顶部右侧）显示一个记事本图标 📝
   - 不会有 Dock 图标（这是菜单栏应用的特点）

2. **开始使用**
   - 点击菜单栏的记事本图标
   - 弹出记事本界面
   - 在输入框中输入你的文本
   - 点击"保存"按钮保存

3. **查看历史记录**
   - 保存的记录会显示在下方的历史列表中
   - 双击任意历史记录可以重新编辑

## 常见问题

### Q: 为什么看不到应用图标？
A: 这是一个菜单栏应用，图标显示在屏幕顶部的菜单栏中，而不是在 Dock 中。

### Q: 如何卸载应用？
A: 删除应用文件即可。数据保存在系统的 UserDefaults 中，如需完全清理，可以运行：
```bash
defaults delete com.macnotepad.app
```

### Q: 应用无法启动怎么办？
A: 
1. 确保系统版本为 macOS 11.0 或更高
2. 检查是否有代码签名问题
3. 尝试在 Xcode 中重新构建

### Q: 如何添加应用到开机启动？
A: 在系统偏好设置 → 用户与群组 → 登录项中添加应用。

## 系统要求

- macOS 11.0 或更高版本
- 约 5MB 磁盘空间

## 权限说明

应用使用了以下权限：
- **App Sandbox**: 确保应用安全运行
- **文件读取**: 用于保存和读取记事数据（仅限应用沙盒内）

## 数据存储

- 记事数据保存在系统的 UserDefaults 中
- 位置：`~/Library/Preferences/com.macnotepad.app.plist`
- 最多保存 50 条历史记录
- 自动按时间排序（最新的在最前面）

---

如果遇到问题，请检查 Xcode 控制台的错误信息，或者重新构建项目。