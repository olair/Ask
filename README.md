# CenterScrollableRow - 可横向滑动并自动居中的Compose组件

这是一个功能强大的Compose组件，支持横向滑动、自动居中子View、点击居中以及事件回调。

## 功能特性

- ✅ **横向滑动**: 支持流畅的横向滑动操作
- ✅ **自动居中**: 滑动结束后自动将最近的子View居中
- ✅ **点击居中**: 点击任意子View会自动滑动使其居中
- ✅ **边缘居中**: 开头和结尾的子View也可以完全居中
- ✅ **事件回调**: 当子View居中时触发回调事件
- ✅ **流畅动画**: 使用300ms的缓动动画
- ✅ **灵活配置**: 支持自定义间距、内边距等参数

## 快速开始

### 基本使用

```kotlin
CenterScrollableRow(
    modifier = Modifier.height(80.dp),
    onItemCentered = { index ->
        println("Item $index is centered")
    }
) {
    repeat(10) { index ->
        item {
            Card(
                modifier = Modifier.size(60.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "${index + 1}")
                }
            }
        }
    }
}
```

### 高级配置

```kotlin
CenterScrollableRow(
    modifier = Modifier.height(100.dp),
    itemSpacing = 16.dp,                              // 子View间距
    contentPadding = PaddingValues(horizontal = 24.dp), // 内容内边距
    onItemCentered = { index ->
        // 处理居中事件
        handleItemCentered(index)
    }
) {
    items.forEachIndexed { index, item ->
        item(modifier = Modifier.clickable { /* 额外的点击处理 */ }) {
            // 你的子View内容
            CustomItemView(item)
        }
    }
}
```

## API 参考

### CenterScrollableRow

| 参数 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `modifier` | `Modifier` | `Modifier` | 组件修饰符 |
| `itemSpacing` | `Dp` | `16.dp` | 子View之间的间距 |
| `contentPadding` | `PaddingValues` | `PaddingValues(horizontal = 16.dp)` | 内容的内边距 |
| `onItemCentered` | `(Int) -> Unit` | `{}` | 当子View居中时的回调，参数为子View索引 |
| `content` | `@Composable CenterScrollableRowScope.() -> Unit` | - | 子View内容构建器 |

### CenterScrollableRowScope

在`content`块中，您可以使用以下方法：

#### item()

```kotlin
item(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

添加一个可点击的子View项目。

## 使用示例

### 示例1：数字卡片

```kotlin
CenterScrollableRow(
    modifier = Modifier.height(80.dp),
    onItemCentered = { index ->
        selectedIndex = index
    }
) {
    repeat(10) { index ->
        item {
            NumberCard(
                number = index + 1,
                isSelected = index == selectedIndex
            )
        }
    }
}
```

### 示例2：不同大小的元素

```kotlin
CenterScrollableRow(
    modifier = Modifier.height(100.dp),
    itemSpacing = 16.dp
) {
    val sizes = listOf(60.dp, 80.dp, 70.dp, 90.dp, 65.dp)
    sizes.forEachIndexed { index, size ->
        item {
            Card(modifier = Modifier.size(size)) {
                // 卡片内容
            }
        }
    }
}
```

### 示例3：文本标签

```kotlin
CenterScrollableRow(
    modifier = Modifier.height(60.dp),
    itemSpacing = 8.dp
) {
    val tags = listOf("设计", "开发", "测试", "部署", "维护")
    tags.forEach { tag ->
        item {
            TagChip(text = tag)
        }
    }
}
```

## 工作原理

1. **布局测量**: 使用`SubcomposeLayout`动态测量所有子View
2. **位置计算**: 计算每个子View的绝对位置
3. **居中逻辑**: 根据容器中心点和子View位置计算最接近的item
4. **动画滑动**: 使用`Animatable`实现流畅的滑动动画
5. **边缘支持**: 通过添加额外padding确保边缘item可以居中

## 注意事项

- 建议为组件设置固定高度以获得最佳效果
- 子View的宽度会自动测量，支持不同大小的元素
- 滑动动画持续300ms，可以根据需要在源码中调整
- 组件会自动处理开头和结尾item的居中显示

## 完整示例

查看 `CenterScrollableRowExample.kt` 文件获取完整的使用示例，包含多种不同类型的子View演示。
