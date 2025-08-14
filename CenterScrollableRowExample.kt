package com.example.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * CenterScrollableRow使用示例
 */
@Composable
fun CenterScrollableRowExample() {
    var centeredIndex by remember { mutableIntStateOf(-1) }
    var eventLog by remember { mutableStateOf("等待事件...") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 标题
        Text(
            text = "CenterScrollableRow 示例",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        // 状态显示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "当前居中项: ${if (centeredIndex >= 0) "Item ${centeredIndex + 1}" else "无"}",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "事件日志: $eventLog",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 示例1：数字卡片
        Text(
            text = "示例 1: 数字卡片 (点击任意卡片自动居中)",
            fontWeight = FontWeight.Medium
        )
        
        CenterScrollableRow(
            modifier = Modifier.height(80.dp),
            itemSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 20.dp),
            onItemCentered = { index ->
                centeredIndex = index
                eventLog = "数字卡片 ${index + 1} 已居中"
            }
        ) {
            repeat(10) { index ->
                item {
                    NumberCard(
                        number = index + 1,
                        isSelected = index == centeredIndex
                    )
                }
            }
        }
        
        // 示例2：不同大小的卡片
        Text(
            text = "示例 2: 不同大小的卡片",
            fontWeight = FontWeight.Medium
        )
        
        var selectedColorIndex by remember { mutableIntStateOf(-1) }
        
        CenterScrollableRow(
            modifier = Modifier.height(100.dp),
            itemSpacing = 16.dp,
            contentPadding = PaddingValues(horizontal = 24.dp),
            onItemCentered = { index ->
                selectedColorIndex = index
                val colors = listOf("红色", "蓝色", "绿色", "橙色", "紫色", "青色", "粉色")
                eventLog = "${colors.getOrElse(index) { "颜色${index + 1}" }}卡片已居中"
            }
        ) {
            val colors = listOf(
                Color(0xFFFF5722), Color(0xFF2196F3), Color(0xFF4CAF50),
                Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFFE91E63)
            )
            val sizes = listOf(60.dp, 80.dp, 70.dp, 90.dp, 65.dp, 75.dp, 85.dp)
            
            colors.forEachIndexed { index, color ->
                item {
                    ColorCard(
                        color = color,
                        size = sizes[index],
                        isSelected = index == selectedColorIndex
                    )
                }
            }
        }
        
        // 示例3：文本标签
        Text(
            text = "示例 3: 文本标签 (边缘项也可居中)",
            fontWeight = FontWeight.Medium
        )
        
        var selectedTagIndex by remember { mutableIntStateOf(-1) }
        
        CenterScrollableRow(
            modifier = Modifier.height(60.dp),
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            onItemCentered = { index ->
                selectedTagIndex = index
                val tags = listOf("设计", "开发", "测试", "部署", "维护", "优化", "重构", "文档")
                eventLog = "标签 \"${tags.getOrElse(index) { "标签${index + 1}" }}\" 已居中"
            }
        ) {
            val tags = listOf("设计", "开发", "测试", "部署", "维护", "优化", "重构", "文档")
            
            tags.forEachIndexed { index, tag ->
                item {
                    TagChip(
                        text = tag,
                        isSelected = index == selectedTagIndex
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 使用说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "使用说明:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "• 滑动列表，松手时自动居中最近的项目",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "• 点击任意项目，该项目会自动滑动到中心",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "• 边缘的项目也可以完全居中显示",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "• 支持回调事件，获取当前居中的项目索引",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * 数字卡片组件
 */
@Composable
private fun NumberCard(
    number: Int,
    isSelected: Boolean
) {
    Card(
        modifier = Modifier.size(60.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 颜色卡片组件
 */
@Composable
private fun ColorCard(
    color: Color,
    size: Dp,
    isSelected: Boolean
) {
    Card(
        modifier = Modifier.size(size),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 12.dp else 6.dp
        ),
        shape = RoundedCornerShape(if (isSelected) 16.dp else 12.dp)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 标签芯片组件
 */
@Composable
private fun TagChip(
    text: String,
    isSelected: Boolean
) {
    Surface(
        modifier = Modifier.height(40.dp),
        color = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CenterScrollableRowExamplePreview() {
    MaterialTheme {
        CenterScrollableRowExample()
    }
}