package com.example.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * EditorSeekBar的增强Compose版本
 * 包含回弹效果、完整磁吸逻辑等功能
 */
@Composable
fun EditorSeekBarEnhanced(
    minValue: Int = -100,
    maxValue: Int = 100,
    initialPosition: Int = 0,
    modifier: Modifier = Modifier,
    dotEnable: Boolean = false,
    dotCenter: Boolean = true,
    isMute: Boolean = false,
    enableOverscroll: Boolean = true,
    overscrollDamping: Float = 0.3f,
    onPositionChanged: ((Int) -> Unit)? = null,
    onStateChanged: ((ScrollState) -> Unit)? = null
) {
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    
    // 状态管理
    var currentPosition by remember { mutableIntStateOf(initialPosition) }
    var scrollState by remember { mutableStateOf(ScrollState.IDLE) }
    var isAnimating by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var containerWidth by remember { mutableIntStateOf(0) }
    var lastVibrationPosition by remember { mutableIntStateOf(0) }
    var lastPosition by remember { mutableIntStateOf(initialPosition) }
    
    // 计算相关
    val totalParts = maxValue - minValue
    val scrollOffsetX = remember { Animatable(0f) }
    
    // 圆点可见性动画
    var showDot by remember { mutableStateOf(false) }
    val dotAlpha by animateFloatAsState(
        targetValue = if (showDot && dotEnable) 1f else 0f,
        animationSpec = tween(200),
        label = "dotAlpha"
    )
    
    // 回弹动画
    val bounceAnimation = remember { Animatable(0f) }
    
    // 更新圆点可见性
    LaunchedEffect(currentPosition, dotEnable, dotCenter, isDragging) {
        if (!dotEnable) {
            showDot = false
            return@LaunchedEffect
        }
        
        val isInZero = if (dotCenter) {
            currentPosition == (maxValue + minValue) / 2
        } else {
            currentPosition == minValue
        }
        
        // 拖动状态下的逻辑：一旦显示就保持显示直到松手
        if (isDragging) {
            if (!isInZero) {
                showDot = true
            }
            // 拖动过程中不隐藏
        } else {
            // 非拖动状态：按位置决定显示
            showDot = !isInZero
        }
    }
    
    // 位置变化处理
    LaunchedEffect(currentPosition) {
        onPositionChanged?.invoke(currentPosition)
        
        // 震动反馈
        if (!isMute && currentPosition % 5 == 0 && currentPosition != lastVibrationPosition) {
            hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            lastVibrationPosition = currentPosition
        }
        
        // 磁吸检查
        checkMagneticSnap(lastPosition, currentPosition) { targetPosition ->
            coroutineScope.launch {
                scrollToPosition(targetPosition, scrollOffsetX, containerWidth, totalParts, minValue, maxValue) {
                    currentPosition = it
                }
            }
        }
        
        lastPosition = currentPosition
    }
    
    // 状态变化处理
    LaunchedEffect(scrollState) {
        onStateChanged?.invoke(scrollState)
    }
    
    // 公开的滚动到位置函数
    fun scrollToPositionAnimated(position: Int) {
        if (position == currentPosition) return
        if (position < minValue || position > maxValue) return
        
        coroutineScope.launch {
            isAnimating = true
            scrollToPosition(position, scrollOffsetX, containerWidth, totalParts, minValue, maxValue) {
                currentPosition = it
            }
            delay(300)
            isAnimating = false
        }
    }
    
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
    ) {
        containerWidth = with(density) { maxWidth.toPx() }.toInt()
        
        // 背景层（双倍宽度，带回弹偏移）
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(maxWidth * 2)
                .offset { 
                    IntOffset(
                        (scrollOffsetX.value + bounceAnimation.value).toInt(), 
                        0
                    ) 
                }
                .background(Color.Red)
        )
        
        // 背景图像（带回弹偏移）
        Image(
            painter = painterResource(id = android.R.drawable.ic_media_play),
            contentDescription = "seek bar background",
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .offset { 
                    IntOffset(
                        (scrollOffsetX.value + bounceAnimation.value - with(density) { 1.dp.toPx() }).toInt(), 
                        0
                    ) 
                }
                .align(Alignment.Center),
            contentScale = ContentScale.FillBounds
        )
        
        // 圆点指示器（固定位置，不跟随滚动）
        if (dotEnable) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(if (dotCenter) Alignment.Center else Alignment.CenterStart)
                    .offset(x = if (!dotCenter) 16.dp else 0.dp)
            ) {
                Image(
                    painter = painterResource(id = android.R.drawable.ic_media_pause),
                    contentDescription = "position indicator",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    alpha = dotAlpha
                )
            }
        }
        
        // 触摸处理层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            scrollState = ScrollState.DRAGGING
                            bounceAnimation.stop()
                        },
                        onDragEnd = {
                            isDragging = false
                            scrollState = ScrollState.IDLE
                            
                            coroutineScope.launch {
                                // 处理回弹
                                if (enableOverscroll) {
                                    handleOverscrollBounce(scrollOffsetX, bounceAnimation, containerWidth)
                                }
                                
                                // 吸附调整
                                delay(500)
                                adjustScrollToNearestStep(scrollOffsetX, containerWidth, totalParts, minValue, maxValue) {
                                    currentPosition = it
                                }
                            }
                        },
                        onDrag = { dragAmount ->
                            if (!isAnimating) {
                                coroutineScope.launch {
                                    val currentOffset = scrollOffsetX.value
                                    val newOffset = currentOffset - dragAmount.x
                                    
                                    if (enableOverscroll) {
                                        // 启用超滚动时应用阻尼
                                        val dampedOffset = if (newOffset > 0f) {
                                            // 超出右边界
                                            newOffset * overscrollDamping
                                        } else if (newOffset < -containerWidth.toFloat()) {
                                            // 超出左边界
                                            val excess = newOffset + containerWidth.toFloat()
                                            -containerWidth.toFloat() + excess * overscrollDamping
                                        } else {
                                            // 正常范围内
                                            newOffset
                                        }
                                        scrollOffsetX.snapTo(dampedOffset)
                                    } else {
                                        // 不启用超滚动时严格限制范围
                                        val clampedOffset = newOffset.coerceIn(-containerWidth.toFloat(), 0f)
                                        scrollOffsetX.snapTo(clampedOffset)
                                    }
                                    
                                    // 计算新位置（仅在正常范围内）
                                    val effectiveOffset = scrollOffsetX.value.coerceIn(-containerWidth.toFloat(), 0f)
                                    val progress = (-effectiveOffset / containerWidth) * totalParts
                                    val newPosition = (progress - totalParts / 2).roundToInt() + (minValue + maxValue) / 2
                                    val clampedPosition = newPosition.coerceIn(minValue, maxValue)
                                    
                                    if (clampedPosition != currentPosition) {
                                        currentPosition = clampedPosition
                                    }
                                }
                            }
                        }
                    )
                }
        )
    }
    
    // 初始化位置
    LaunchedEffect(containerWidth, initialPosition) {
        if (containerWidth > 0) {
            val targetOffset = calculateScrollOffset(initialPosition, containerWidth, totalParts, minValue, maxValue)
            scrollOffsetX.snapTo(targetOffset)
            currentPosition = initialPosition
        }
    }
    
    // 暴露滚动函数给外部使用
    LaunchedEffect(Unit) {
        // 这里可以暴露一个方法给外部调用
    }
}

/**
 * 处理超滚动回弹效果
 */
private suspend fun handleOverscrollBounce(
    scrollOffsetX: Animatable<Float, AnimationVector1D>,
    bounceAnimation: Animatable<Float, AnimationVector1D>,
    containerWidth: Int
) {
    val currentOffset = scrollOffsetX.value
    val targetOffset = currentOffset.coerceIn(-containerWidth.toFloat(), 0f)
    
    if (currentOffset != targetOffset) {
        // 需要回弹
        val bounceDistance = currentOffset - targetOffset
        
        // 先做一个小的反向弹跳
        bounceAnimation.animateTo(
            targetValue = bounceDistance * 0.2f,
            animationSpec = tween(150, easing = FastOutSlowInEasing)
        )
        
        // 然后回弹到正确位置
        launch {
            scrollOffsetX.animateTo(
                targetValue = targetOffset,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        
        bounceAnimation.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
}

/**
 * 磁吸检查 - 增强版本
 */
private fun checkMagneticSnap(lastPosition: Int, currentPosition: Int, onMagneticSnap: (Int) -> Unit) {
    // 实现原版的磁吸逻辑：从3到2或从-3到-2时吸附到0
    if ((lastPosition == 3 && currentPosition == 2) ||
        (lastPosition == -3 && currentPosition == -2)) {
        onMagneticSnap(0)
    }
}

/**
 * 调整滚动位置到最近的刻度 - 增强版本
 */
private suspend fun adjustScrollToNearestStep(
    scrollOffsetX: Animatable<Float, AnimationVector1D>,
    containerWidth: Int,
    totalParts: Int,
    minValue: Int,
    maxValue: Int,
    onPositionUpdate: (Int) -> Unit
) {
    val scrollStep = containerWidth.toFloat() / totalParts
    val currentOffset = scrollOffsetX.value
    val nearestStepIndex = round(-currentOffset / scrollStep)
    val nearestStepOffset = -nearestStepIndex * scrollStep
    
    // 计算对应的位置值
    val position = (nearestStepIndex - totalParts / 2).toInt() + (minValue + maxValue) / 2
    val clampedPosition = position.coerceIn(minValue, maxValue)
    
    scrollOffsetX.animateTo(
        targetValue = nearestStepOffset,
        animationSpec = tween(150, easing = FastOutSlowInEasing)
    )
    
    onPositionUpdate(clampedPosition)
}

/**
 * 计算滚动偏移量
 */
private fun calculateScrollOffset(
    position: Int,
    containerWidth: Int,
    totalParts: Int,
    minValue: Int,
    maxValue: Int
): Float {
    val normalizedPosition = (position - (minValue + maxValue) / 2 + totalParts / 2)
    return -(normalizedPosition * containerWidth.toFloat() / totalParts)
}

/**
 * 滚动到指定位置
 */
private suspend fun scrollToPosition(
    position: Int,
    scrollOffsetX: Animatable<Float, AnimationVector1D>,
    containerWidth: Int,
    totalParts: Int,
    minValue: Int,
    maxValue: Int,
    onPositionUpdate: (Int) -> Unit
) {
    val targetOffset = calculateScrollOffset(position, containerWidth, totalParts, minValue, maxValue)
    scrollOffsetX.animateTo(
        targetValue = targetOffset,
        animationSpec = tween(250, easing = FastOutSlowInEasing)
    )
    onPositionUpdate(position)
}

/**
 * 滚动状态枚举
 */
enum class ScrollState {
    IDLE,
    DRAGGING,
    SETTLING
}

/**
 * 使用示例 - 增强版本
 */
@Composable
fun EditorSeekBarEnhancedDemo() {
    var position by remember { mutableIntStateOf(0) }
    var scrollState by remember { mutableStateOf(ScrollState.IDLE) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("当前位置: $position", style = MaterialTheme.typography.headlineSmall)
        Text("滚动状态: $scrollState", style = MaterialTheme.typography.bodyLarge)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("基础版本", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                EditorSeekBar(
                    minValue = -100,
                    maxValue = 100,
                    initialPosition = position,
                    dotEnable = true,
                    dotCenter = true,
                    isMute = false,
                    onPositionChanged = { position = it },
                    onStateChanged = { scrollState = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("增强版本（带回弹）", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                EditorSeekBarEnhanced(
                    minValue = -100,
                    maxValue = 100,
                    initialPosition = position,
                    dotEnable = true,
                    dotCenter = true,
                    isMute = false,
                    enableOverscroll = true,
                    overscrollDamping = 0.3f,
                    onPositionChanged = { position = it },
                    onStateChanged = { scrollState = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { position = -100 }) {
                Text("最小")
            }
            Button(onClick = { position = -50 }) {
                Text("-50")
            }
            Button(onClick = { position = 0 }) {
                Text("中心")
            }
            Button(onClick = { position = 50 }) {
                Text("50")
            }
            Button(onClick = { position = 100 }) {
                Text("最大")
            }
        }
    }
}