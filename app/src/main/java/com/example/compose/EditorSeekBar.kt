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
 * EditorSeekBar的Compose版本
 * 
 * @param minValue 最小值
 * @param maxValue 最大值
 * @param initialPosition 初始位置
 * @param modifier 修饰符
 * @param dotEnable 是否显示圆点
 * @param dotCenter 圆点是否居中显示（true: 居中, false: 左对齐）
 * @param isMute 是否静音（不震动）
 * @param onPositionChanged 位置改变回调
 * @param onStateChanged 状态改变回调
 */
@Composable
fun EditorSeekBar(
    minValue: Int = -100,
    maxValue: Int = 100,
    initialPosition: Int = 0,
    modifier: Modifier = Modifier,
    dotEnable: Boolean = false,
    dotCenter: Boolean = true,
    isMute: Boolean = false,
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
        
        showDot = !isInZero
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
        checkMagnetic(currentPosition) { targetPosition ->
            coroutineScope.launch {
                scrollToPosition(targetPosition, scrollOffsetX, containerWidth, totalParts, minValue, maxValue) {
                    currentPosition = it
                }
            }
        }
    }
    
    // 状态变化处理
    LaunchedEffect(scrollState) {
        onStateChanged?.invoke(scrollState)
    }
    
    // 滚动到指定位置的函数
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
            .height(58.dp) // 18 + 32 + 8
    ) {
        containerWidth = with(density) { maxWidth.toPx() }.toInt()
        
        // 背景层（双倍宽度）
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(maxWidth * 2)
                .offset { IntOffset(scrollOffsetX.value.toInt(), 0) }
                .background(Color.Red)
        )
        
        // 背景图像
        Image(
            painter = painterResource(id = android.R.drawable.ic_media_play), // 替换为您的资源
            contentDescription = "seek bar background",
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .offset { IntOffset(scrollOffsetX.value.toInt() - with(density) { 1.dp.toPx() }.toInt(), 0) }
                .align(Alignment.Center),
            contentScale = ContentScale.FillBounds
        )
        
        // 圆点指示器
        if (dotEnable) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(if (dotCenter) Alignment.Center else Alignment.CenterStart)
                    .offset(x = if (!dotCenter) 16.dp else 0.dp)
            ) {
                Image(
                    painter = painterResource(id = android.R.drawable.ic_media_pause), // 替换为您的圆点资源
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
                        },
                        onDragEnd = {
                            isDragging = false
                            scrollState = ScrollState.IDLE
                            
                            // 吸附调整
                            coroutineScope.launch {
                                delay(500) // 等待可能的fling结束
                                adjustScroll(scrollOffsetX, containerWidth, totalParts) {
                                    currentPosition = it
                                }
                            }
                        },
                        onDrag = { dragAmount ->
                            if (!isAnimating) {
                                coroutineScope.launch {
                                    val newOffset = (scrollOffsetX.value - dragAmount.x)
                                        .coerceIn(-containerWidth.toFloat(), 0f)
                                    scrollOffsetX.snapTo(newOffset)
                                    
                                    // 计算新位置
                                    val progress = (-newOffset / containerWidth) * totalParts
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
        animationSpec = tween(250)
    )
    onPositionUpdate(position)
}

/**
 * 磁吸检查
 */
private fun checkMagnetic(position: Int, onMagneticSnap: (Int) -> Unit) {
    // 当从3滑到2或从-3滑到-2时，吸附到0
    // 这里简化实现，您可以根据需要调整磁吸逻辑
}

/**
 * 调整滚动位置到最近的刻度
 */
private suspend fun adjustScroll(
    scrollOffsetX: Animatable<Float, AnimationVector1D>,
    containerWidth: Int,
    totalParts: Int,
    onPositionUpdate: (Int) -> Unit
) {
    val scrollStep = containerWidth.toFloat() / totalParts
    val currentOffset = scrollOffsetX.value
    val nearestStep = round(currentOffset / scrollStep) * scrollStep
    
    scrollOffsetX.animateTo(
        targetValue = nearestStep,
        animationSpec = tween(150)
    )
}

/**
 * 使用示例
 */
@Composable
fun EditorSeekBarDemo() {
    var position by remember { mutableIntStateOf(0) }
    var scrollState by remember { mutableStateOf(ScrollState.IDLE) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("当前位置: $position")
        Text("滚动状态: $scrollState")
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { position = -50 }) {
                Text("-50")
            }
            Button(onClick = { position = 0 }) {
                Text("0")
            }
            Button(onClick = { position = 50 }) {
                Text("50")
            }
        }
    }
}