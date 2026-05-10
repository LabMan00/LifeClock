package com.example.lifeclock.ui.theme // 主题包

import androidx.compose.material3.Typography // Material3排版配置
import androidx.compose.ui.text.TextStyle // 文本样式
import androidx.compose.ui.text.font.FontFamily // 字体族
import androidx.compose.ui.text.font.FontWeight // 字体粗细
import androidx.compose.ui.unit.sp // sp字体单位

// 应用排版配置：定义各文本级别的默认样式
val Typography = Typography(
    bodyLarge = TextStyle(              // 大号正文
        fontFamily = FontFamily.Default, // 系统默认字体
        fontWeight = FontWeight.Normal,  // 常规粗细
        fontSize = 16.sp,               // 16sp字号
        lineHeight = 24.sp,             // 24sp行高
        letterSpacing = 0.5.sp          // 0.5sp字间距
    )
    // titleLarge = TextStyle(...)      // 大标题（未自定义，使用Material3默认值）
    // labelSmall = TextStyle(...)      // 小标签（未自定义，使用Material3默认值）
)
