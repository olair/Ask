# EditorSeekBar Compose版本

这是原Android View版本的EditorSeekBar在Jetpack Compose中的实现，保持了原版本的所有核心功能。

## 功能特性

### 🎯 核心功能
- **自定义范围**：支持任意整数范围（默认-100到100）
- **双倍背景滚动**：背景宽度为容器的2倍，实现扩展滚动区域
- **精确位置控制**：基于像素级别的位置计算
- **平滑动画**：支持程序化滚动到指定位置

### ✨ 交互特性
- **触摸拖拽**：流畅的手势拖拽体验
- **磁吸效果**：滚动结束时自动吸附到最近刻度
- **震动反馈**：每5个单位提供触觉反馈（可关闭）
- **状态回调**：实时位置变化和滚动状态监听

### 🎨 视觉特性
- **可配置圆点**：支持显示/隐藏位置指示器
- **圆点定位**：可选择居中或左对齐显示
- **圆点动画**：平滑的显示/隐藏动画效果
- **回弹效果**：增强版本支持超出边界的回弹动画

## 使用方式

### 基础版本

```kotlin
@Composable
fun MyScreen() {
    var position by remember { mutableIntStateOf(0) }
    
    EditorSeekBar(
        minValue = -100,
        maxValue = 100,
        initialPosition = position,
        dotEnable = true,
        dotCenter = true,
        isMute = false,
        onPositionChanged = { newPosition ->
            position = newPosition
        },
        onStateChanged = { state ->
            // 处理滚动状态变化
        },
        modifier = Modifier.fillMaxWidth()
    )
}
```

### 增强版本（带回弹效果）

```kotlin
@Composable
fun MyEnhancedScreen() {
    var position by remember { mutableIntStateOf(0) }
    
    EditorSeekBarEnhanced(
        minValue = -100,
        maxValue = 100,
        initialPosition = position,
        dotEnable = true,
        dotCenter = true,
        isMute = false,
        enableOverscroll = true,      // 启用超滚动
        overscrollDamping = 0.3f,     // 超滚动阻尼系数
        onPositionChanged = { newPosition ->
            position = newPosition
        },
        onStateChanged = { state ->
            // 处理滚动状态变化
        },
        modifier = Modifier.fillMaxWidth()
    )
}
```

## 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `minValue` | Int | -100 | 最小值 |
| `maxValue` | Int | 100 | 最大值 |
| `initialPosition` | Int | 0 | 初始位置 |
| `modifier` | Modifier | Modifier | 修饰符 |
| `dotEnable` | Boolean | false | 是否显示圆点指示器 |
| `dotCenter` | Boolean | true | 圆点是否居中（false为左对齐） |
| `isMute` | Boolean | false | 是否禁用震动反馈 |
| `enableOverscroll` | Boolean | true | 是否启用超滚动（仅增强版） |
| `overscrollDamping` | Float | 0.3f | 超滚动阻尼系数（仅增强版） |
| `onPositionChanged` | (Int) -> Unit | null | 位置变化回调 |
| `onStateChanged` | (ScrollState) -> Unit | null | 状态变化回调 |

## 状态说明

```kotlin
enum class ScrollState {
    IDLE,        // 空闲状态
    DRAGGING,    // 拖拽中
    SETTLING     // 结算中（自动滚动）
}
```

## 圆点显示逻辑

圆点的显示遵循以下规则：

1. **非拖动状态**：
   - 位置在零点时隐藏
   - 其他位置显示

2. **拖动状态**：
   - 一旦显示就保持显示
   - 直到手指松开后重新按规则1判断

3. **零点定义**：
   - `dotCenter = true`：零点为 `(maxValue + minValue) / 2`
   - `dotCenter = false`：零点为 `minValue`

## 磁吸效果

实现了原版的磁吸逻辑：
- 从位置3滑动到2时，自动吸附到0
- 从位置-3滑动到-2时，自动吸附到0

您可以根据需要扩展或修改磁吸逻辑。

## 震动反馈

- 默认在位置为5的倍数时触发震动
- 可通过 `isMute = true` 禁用
- 使用系统触觉反馈API

## 注意事项

1. **资源替换**：示例中使用了系统默认图标，请替换为您的实际资源：
   ```kotlin
   // 替换这些资源引用
   painterResource(id = R.drawable.your_seek_bar_bg)
   painterResource(id = R.drawable.your_circle_indicator)
   ```

2. **性能考虑**：
   - 使用`remember`缓存动画对象
   - 避免在高频回调中执行重操作

3. **布局兼容**：
   - 组件会自动适应父容器宽度
   - 高度固定为58dp，与原版本保持一致

## 与原版对比

| 特性 | 原版（View） | Compose版本 |
|------|-------------|-------------|
| 基础滚动 | ✅ | ✅ |
| 双倍背景 | ✅ | ✅ |
| 圆点指示器 | ✅ | ✅ |
| 震动反馈 | ✅ | ✅ |
| 磁吸效果 | ✅ | ✅ |
| 回弹效果 | ✅（第三方库） | ✅（原生实现） |
| 状态回调 | ✅ | ✅ |
| 动画滚动 | ✅ | ✅ |

Compose版本在保持原有功能的基础上，提供了更现代的API设计和更好的动画体验。