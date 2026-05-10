package com.example.lifeclock.ui.theme // 主题包

import android.app.Activity // Activity类（未直接使用）
import android.os.Build // Android版本判断
import androidx.compose.foundation.isSystemInDarkTheme // 系统深色主题检测
import androidx.compose.material3.MaterialTheme // Material3主题
import androidx.compose.material3.darkColorScheme // 深色配色方案构建器
import androidx.compose.material3.dynamicDarkColorScheme // 动态深色配色（Android 12+）
import androidx.compose.material3.dynamicLightColorScheme // 动态浅色配色（Android 12+）
import androidx.compose.material3.lightColorScheme // 浅色配色方案构建器
import androidx.compose.runtime.Composable // Composable标记
import androidx.compose.ui.platform.LocalContext // 获取上下文（动态配色需要）

// 深色配色方案（手动定义）
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,          // 主色：浅紫色
    secondary = PurpleGrey80,    // 次色：浅紫灰色
    tertiary = Pink80            // 第三色：浅粉色
)

// 浅色配色方案（手动定义）
private val LightColorScheme = lightColorScheme(
    primary = Purple40,          // 主色：深紫色
    secondary = PurpleGrey40,    // 次色：深紫灰色
    tertiary = Pink40            // 第三色：深粉色
)

// 应用主题：支持动态配色（Android 12+）和手动静/深色切换
@Composable
fun LifeClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // 是否深色主题（默认跟随系统）
    dynamicColor: Boolean = true,               // 是否启用动态配色（Android 12+从壁纸提取颜色）
    content: @Composable () -> Unit             // 主题包裹的内容
) {
    val colorScheme = when {
        // 条件1：动态配色可用且Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current                               // 获取上下文
            if (darkTheme) dynamicDarkColorScheme(context)                   // 动态深色
            else dynamicLightColorScheme(context)                            // 动态浅色
        }
        // 条件2：使用手动深色配色
        darkTheme -> DarkColorScheme
        // 条件3：使用手动浅色配色
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme, // 配色方案
        typography = Typography,   // 字体排版
        content = content          // 内容
    )
}
