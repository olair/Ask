# HorizontalCenterScrollView 使用说明

## 概述

`HorizontalCenterScrollView` 是一个自定义的Android组件，继承自 `HorizontalScrollView`，提供以下功能：

1. **横向滑动**：支持水平方向的滑动操作
2. **自动居中**：滑动结束后自动将最近的子视图居中
3. **点击居中**：点击任意子视图时，该视图会自动滑动到中心位置
4. **事件回调**：当子视图居中时，会通过回调接口通知上层处理事件

## 主要特性

### 核心功能
- 继承自 `HorizontalScrollView`，保持原有的滑动特性
- 内置 `LinearLayout` 作为容器，方便管理子视图
- 智能检测滑动结束，自动居中最近的子视图
- 支持平滑动画过渡

### 交互体验
- 用户手动滑动后，会自动居中到最近的子视图
- 点击任意子视图，该视图会平滑滑动到中心
- 提供实时的居中状态回调

## 使用方法

### 1. 基本用法

```java
// 创建HorizontalCenterScrollView
HorizontalCenterScrollView scrollView = new HorizontalCenterScrollView(context);

// 添加子视图
TextView textView1 = new TextView(context);
textView1.setText("项目1");
scrollView.addChildView(textView1);

TextView textView2 = new TextView(context);
textView2.setText("项目2");
scrollView.addChildView(textView2);

// 设置居中变化监听器
scrollView.setOnCenterChangeListener(new HorizontalCenterScrollView.OnCenterChangeListener() {
    @Override
    public void onCenterChanged(View centerView, int centerIndex) {
        // 处理居中事件
        Log.d("ScrollView", "居中视图索引: " + centerIndex);
    }
});
```

### 2. 添加和移除子视图

```java
// 添加子视图到末尾
scrollView.addChildView(newView);

// 添加子视图到指定位置
scrollView.addChildView(newView, 2);

// 移除指定的子视图
scrollView.removeChildView(targetView);

// 移除指定位置的子视图
scrollView.removeChildViewAt(1);
```

### 3. 程序控制居中

```java
// 将指定的子视图居中（平滑滚动）
scrollView.centerToChild(targetView, true);

// 将指定索引的子视图居中（立即滚动）
scrollView.centerToChildAt(2, false);

// 获取当前居中的子视图索引
int centerIndex = scrollView.getCurrentCenterIndex();

// 获取当前居中的子视图
View centerView = scrollView.getCurrentCenterView();
```

### 4. 事件监听

```java
scrollView.setOnCenterChangeListener(new HorizontalCenterScrollView.OnCenterChangeListener() {
    @Override
    public void onCenterChanged(View centerView, int centerIndex) {
        // 当子视图居中时的处理逻辑
        if (centerView instanceof TextView) {
            String text = ((TextView) centerView).getText().toString();
            Toast.makeText(context, "当前居中: " + text, Toast.LENGTH_SHORT).show();
        }
        
        // 可以在这里处理其他业务逻辑
        // 例如：更新其他UI组件、发送网络请求等
    }
});
```

## API 参考

### 主要方法

| 方法 | 描述 |
|------|------|
| `addChildView(View child)` | 添加子视图到末尾 |
| `addChildView(View child, int index)` | 添加子视图到指定位置 |
| `removeChildView(View child)` | 移除指定的子视图 |
| `removeChildViewAt(int index)` | 移除指定位置的子视图 |
| `getChildViewCount()` | 获取子视图数量 |
| `getChildViewAt(int index)` | 获取指定位置的子视图 |
| `centerToChild(View child, boolean smooth)` | 将指定子视图居中 |
| `centerToChildAt(int index, boolean smooth)` | 将指定索引的子视图居中 |
| `getCurrentCenterIndex()` | 获取当前居中的子视图索引 |
| `getCurrentCenterView()` | 获取当前居中的子视图 |
| `setOnCenterChangeListener(OnCenterChangeListener listener)` | 设置居中变化监听器 |

### 回调接口

```java
public interface OnCenterChangeListener {
    /**
     * 当子视图居中时调用
     * @param centerView 居中的视图
     * @param centerIndex 居中视图的索引
     */
    void onCenterChanged(View centerView, int centerIndex);
}
```

## 实现原理

### 自动居中机制
1. 监听触摸事件，检测滑动开始和结束
2. 在滑动结束后，计算距离中心最近的子视图
3. 使用 `smoothScrollTo()` 方法平滑滚动到目标位置

### 点击居中功能
1. 为每个添加的子视图自动设置点击监听器
2. 点击时计算该视图的中心位置
3. 平滑滚动到使该视图居中的位置

### 事件回调时机
1. 程序控制居中时立即触发
2. 自动居中完成时触发
3. 确保每次居中变化都会通知上层

## 注意事项

1. **布局参数**：子视图需要设置合适的布局参数，建议使用固定宽度
2. **性能考虑**：避免添加过多的子视图，以免影响滑动性能
3. **线程安全**：所有方法都应在主线程中调用
4. **内存管理**：及时移除不需要的子视图，避免内存泄漏

## 示例项目

参考 `HorizontalCenterScrollDemoActivity` 类，它展示了完整的使用方法和最佳实践。

## 扩展建议

根据具体需求，您可以考虑以下扩展：

1. **添加指示器**：在底部添加圆点指示器显示当前位置
2. **支持循环滚动**：实现无限循环的滚动效果
3. **自定义动画**：自定义滚动动画效果和时长
4. **支持垂直滚动**：创建对应的垂直版本组件
5. **边距控制**：添加左右边距配置，控制居中的精确位置