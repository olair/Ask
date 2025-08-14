package com.example.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 可横向滑动并自动居中子View的组件
 * 
 * @param modifier 修饰符
 * @param itemSpacing 子View之间的间距
 * @param contentPadding 内容的内边距
 * @param onItemCentered 当子View居中时的回调，参数为居中的子View索引
 * @param content 子View内容的构建器
 */
@Composable
fun CenterScrollableRow(
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    onItemCentered: (Int) -> Unit = {},
    content: @Composable CenterScrollableRowScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
    // 存储子View的位置和大小信息
    var itemPositions by remember { mutableStateOf<List<ItemPosition>>(emptyList()) }
    var containerWidth by remember { mutableFloatStateOf(0f) }
    var currentCenteredIndex by remember { mutableIntStateOf(-1) }
    
    // 滑动动画
    val scrollAnimation = remember { Animatable(0f) }
    
    // 用于跟踪是否正在进行自动滑动
    var isAutoScrolling by remember { mutableStateOf(false) }
    
    // 计算当前应该居中的item索引
    fun calculateCenteredIndex(): Int {
        if (itemPositions.isEmpty() || containerWidth == 0f) return -1
        
        val centerPosition = scrollState.value + containerWidth / 2f
        
        return itemPositions.indexOfFirst { item ->
            centerPosition >= item.left && centerPosition <= item.right
        }.takeIf { it != -1 } ?: run {
            // 如果没有找到完全包含的item，找最接近的
            itemPositions.minByOrNull { item ->
                val itemCenter = (item.left + item.right) / 2f
                abs(itemCenter - centerPosition)
            }?.let { itemPositions.indexOf(it) } ?: -1
        }
    }
    
    // 自动居中指定的item
    fun centerItem(index: Int, animate: Boolean = true) {
        if (index < 0 || index >= itemPositions.size || containerWidth == 0f) return
        
        val item = itemPositions[index]
        val itemCenter = (item.left + item.right) / 2f
        val containerCenter = containerWidth / 2f
        val targetScrollValue = (itemCenter - containerCenter)
            .coerceAtLeast(0f)
            .coerceAtMost(scrollState.maxValue.toFloat())
        
        coroutineScope.launch {
            isAutoScrolling = true
            if (animate) {
                scrollAnimation.animateTo(
                    targetValue = targetScrollValue,
                    animationSpec = tween(durationMillis = 300)
                ) { 
                    scrollState.scrollTo(value.roundToInt())
                }
            } else {
                scrollState.scrollTo(targetScrollValue.roundToInt())
            }
            isAutoScrolling = false
        }
    }
    
    // 监听滑动状态变化
    LaunchedEffect(scrollState.value, itemPositions, containerWidth) {
        if (!isAutoScrolling && itemPositions.isNotEmpty()) {
            val newCenteredIndex = calculateCenteredIndex()
            if (newCenteredIndex != currentCenteredIndex && newCenteredIndex != -1) {
                currentCenteredIndex = newCenteredIndex
                onItemCentered(newCenteredIndex)
            }
        }
    }
    
    // 处理滑动结束后的自动居中
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress && !isAutoScrolling && itemPositions.isNotEmpty()) {
            val centeredIndex = calculateCenteredIndex()
            if (centeredIndex != -1) {
                centerItem(centeredIndex, animate = true)
            }
        }
    }
    
    val density = LocalDensity.current
    val startPaddingPx = with(density) { 
        contentPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr).toPx() 
    }
    val endPaddingPx = with(density) { 
        contentPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr).toPx() 
    }
    val spacingPx = with(density) { itemSpacing.toPx() }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .onGloballyPositioned { coordinates ->
                containerWidth = coordinates.size.width.toFloat()
            }
    ) {
        CenterScrollableRowLayout(
            scrollState = scrollState,
            itemSpacing = spacingPx,
            startPadding = startPaddingPx,
            endPadding = endPaddingPx,
            containerWidth = containerWidth,
            onItemPositionsChanged = { positions ->
                itemPositions = positions
            },
            onItemClick = { index ->
                centerItem(index, animate = true)
            },
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

/**
 * CenterScrollableRow的作用域，提供给子组件使用
 */
interface CenterScrollableRowScope {
    /**
     * 添加一个可点击的item
     */
    @Composable
    fun item(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    )
}

/**
 * 存储子View位置信息的数据类
 */
private data class ItemPosition(
    val left: Float,
    val right: Float,
    val width: Float
)

/**
 * 使用SubcomposeLayout实现的布局组件
 */
@Composable
private fun CenterScrollableRowLayout(
    scrollState: androidx.compose.foundation.ScrollState,
    itemSpacing: Float,
    startPadding: Float,
    endPadding: Float,
    containerWidth: Float,
    onItemPositionsChanged: (List<ItemPosition>) -> Unit,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable CenterScrollableRowScope.() -> Unit
) {
    // 收集所有子项内容
    val items = remember { mutableStateListOf<@Composable () -> Unit>() }
    
    // 创建scope来收集items
    val scope = remember {
        object : CenterScrollableRowScope {
            @Composable
            override fun item(modifier: Modifier, itemContent: @Composable () -> Unit) {
                val itemIndex = items.size
                items.add {
                    Box(
                        modifier = modifier.clickable { 
                            onItemClick(itemIndex) 
                        }
                    ) {
                        itemContent()
                    }
                }
            }
        }
    }
    
    // 重置并重新收集items
    items.clear()
    scope.content()
    
    SubcomposeLayout(
        modifier = modifier.horizontalScroll(scrollState)
    ) { constraints ->
        // 测量所有子View
        val placeables = items.mapIndexed { index, itemContent ->
            subcompose("item_$index") {
                itemContent()
            }.first().measure(constraints.copy(minWidth = 0))
        }
        
        if (placeables.isEmpty()) {
            return@SubcomposeLayout layout(0, 0) {}
        }
        
        // 计算总宽度和每个item的位置
        var totalContentWidth = startPadding + endPadding
        placeables.forEachIndexed { index, placeable ->
            totalContentWidth += placeable.width
            if (index < placeables.size - 1) {
                totalContentWidth += itemSpacing
            }
        }
        
        // 确保内容宽度至少等于容器宽度，这样开头和结尾的item都可以居中
        val minContentWidth = constraints.maxWidth.toFloat()
        val finalWidth = maxOf(totalContentWidth, minContentWidth)
        val extraPadding = if (finalWidth > totalContentWidth) (finalWidth - totalContentWidth) / 2 else 0f
        
        // 计算每个item的位置
        val itemPositions = mutableListOf<ItemPosition>()
        var currentX = startPadding + extraPadding
        
        placeables.forEach { placeable ->
            itemPositions.add(
                ItemPosition(
                    left = currentX,
                    right = currentX + placeable.width,
                    width = placeable.width.toFloat()
                )
            )
            currentX += placeable.width + itemSpacing
        }
        
        // 通知位置变化
        onItemPositionsChanged(itemPositions)
        
        layout(finalWidth.roundToInt(), constraints.maxHeight) {
            var xPosition = (startPadding + extraPadding).roundToInt()
            
            placeables.forEach { placeable ->
                placeable.placeRelative(
                    x = xPosition,
                    y = (constraints.maxHeight - placeable.height) / 2
                )
                xPosition += placeable.width + itemSpacing.roundToInt()
            }
        }
    }
}