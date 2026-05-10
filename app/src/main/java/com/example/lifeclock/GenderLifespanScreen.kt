package com.example.lifeclock // 应用包名

import androidx.compose.foundation.BorderStroke // 边框描边
import androidx.compose.foundation.background // 背景色
import androidx.compose.foundation.border // 边框
import androidx.compose.foundation.clickable // 点击修饰符
import androidx.compose.foundation.layout.Column // 垂直排列布局
import androidx.compose.foundation.layout.Row // 水平排列布局
import androidx.compose.foundation.layout.Spacer // 间距占位
import androidx.compose.foundation.layout.fillMaxSize // 填充最大尺寸
import androidx.compose.foundation.layout.fillMaxWidth // 填充最大宽度
import androidx.compose.foundation.layout.height // 固定高度
import androidx.compose.foundation.layout.padding // 内边距
import androidx.compose.foundation.rememberScrollState // 记住滚动状态
import androidx.compose.foundation.shape.RoundedCornerShape // 圆角形状
import androidx.compose.foundation.verticalScroll // 垂直滚动
import androidx.compose.material.icons.Icons // Material图标
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 返回箭头图标
import androidx.compose.material3.ExperimentalMaterial3Api // Material3实验API标记
import androidx.compose.material3.Icon // 图标组件
import androidx.compose.material3.IconButton // 图标按钮
import androidx.compose.material3.MaterialTheme // Material3主题
import androidx.compose.material3.Scaffold // 脚手架布局
import androidx.compose.material3.Text // 文本组件
import androidx.compose.material3.TopAppBar // 顶部导航栏
import androidx.compose.material3.TopAppBarDefaults // 导航栏默认配置
import androidx.compose.runtime.Composable // Composable函数标记
import androidx.compose.ui.Alignment // 对齐方式
import androidx.compose.ui.Modifier // UI修饰符
import androidx.compose.ui.draw.clip // 裁剪形状
import androidx.compose.ui.graphics.Color // 颜色
import androidx.compose.ui.text.font.FontWeight // 字体粗细
import androidx.compose.ui.unit.dp // dp密度单位
import androidx.compose.ui.unit.sp // sp字体单位

// 中国人均预期寿命页面：展示总体、男性、女性平均寿命，点击卡片可设为倒计时基准
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderLifespanScreen(
    gender: String,                                   // 用户性别
    onBack: () -> Unit,                               // 返回回调
    onSelectLifespan: (Double, String) -> Unit        // 选中寿命回调（寿命值, 来源）
) {
    val isFemale = gender == UserPreferences.GENDER_FEMALE // 判断是否为女性
    val isMale = gender == UserPreferences.GENDER_MALE     // 判断是否为男性

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("中国人均预期寿命") },    // 页面标题
                navigationIcon = {
                    IconButton(onClick = onBack) {       // 返回按钮
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface // 导航栏背景色
                )
            )
        }
    ) { padding -> // Scaffold的内边距
        Column(
            modifier = Modifier
                .fillMaxSize()                         // 填充屏幕
                .padding(padding)                      // 避开导航栏
                .padding(horizontal = 32.dp)            // 左右32dp边距
                .verticalScroll(rememberScrollState()), // 可垂直滚动
            horizontalAlignment = Alignment.CenterHorizontally // 子项水平居中
        ) {
            Spacer(modifier = Modifier.height(16.dp))  // 顶部间距

            Text(
                text = "中国人均预期寿命",                // 大标题
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))   // 小间距

            Text(
                text = "点击卡片可将该预期寿命设为你的倒计时", // 操作提示
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "数据来源：国家卫健委（2026年）",    // 数据来源标注
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(36.dp))  // 与卡片区的间距

            // 人均预期寿命卡片（始终高亮）
            LifespanCard(
                label = "人均",                          // 卡片标签
                lifespan = UserPreferences.CHINA_OVERALL_LIFESPAN, // 79.08岁
                color = Color(0xFF9C27B0),               // 紫色
                highlighted = true,                      // 始终高亮
                onClick = { onSelectLifespan(UserPreferences.CHINA_OVERALL_LIFESPAN, UserPreferences.SOURCE_OVERALL) }
            )

            Spacer(modifier = Modifier.height(16.dp))  // 卡片间距

            // 男性预期寿命卡片
            LifespanCard(
                label = "男性",
                lifespan = UserPreferences.CHINA_MALE_LIFESPAN, // 76.05岁
                color = Color(0xFF2196F3),               // 蓝色
                highlighted = isMale,                    // 男用户高亮
                onClick = { onSelectLifespan(UserPreferences.CHINA_MALE_LIFESPAN, UserPreferences.SOURCE_MALE) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 女性预期寿命卡片
            LifespanCard(
                label = "女性",
                lifespan = UserPreferences.CHINA_FEMALE_LIFESPAN, // 82.11岁
                color = Color(0xFFE91E63),               // 粉色
                highlighted = isFemale,                  // 女用户高亮
                onClick = { onSelectLifespan(UserPreferences.CHINA_FEMALE_LIFESPAN, UserPreferences.SOURCE_FEMALE) }
            )

            Spacer(modifier = Modifier.height(32.dp))  // 底部间距
        }
    }
}

// 预期寿命展示卡片（精确值 + 换算值）
@Composable
private fun LifespanCard(
    label: String,          // 卡片标签（人均/男性/女性）
    lifespan: Double,       // 寿命值（精确到两位小数）
    color: Color,           // 主题色
    highlighted: Boolean,   // 是否高亮显示
    onClick: () -> Unit     // 点击回调
) {
    val borderColor = if (highlighted) color else Color.Transparent // 高亮时有彩色边框
    val bgColor = if (highlighted) color.copy(alpha = 0.1f) else Color(0xFF1A1A1A) // 高亮时半透明背景
    val borderMod = if (highlighted) Modifier.border(BorderStroke(2.dp, borderColor), RoundedCornerShape(16.dp)) else Modifier // 边框或空

    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)                       // 85%宽度
            .clip(RoundedCornerShape(16.dp))            // 裁剪16dp圆角
            .background(bgColor, RoundedCornerShape(16.dp)) // 圆角背景
            .then(borderMod)                            // 应用边框
            .clickable(onClick = onClick)               // 点击整张卡片
            .padding(horizontal = 24.dp, vertical = 20.dp) // 内边距
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically // 垂直居中对齐
        ) {
            Text(
                text = label,                           // 标签文字
                fontSize = 18.sp,
                fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal, // 高亮加粗
                color = if (highlighted) color else Color(0xFFCCCCCC), // 高亮用主题色
                modifier = Modifier.weight(1f)           // 占用剩余空间
            )
            Text(
                text = "%.2f".format(lifespan),         // 精确值（如"79.08"）
                fontSize = 36.sp,                        // 大号字体
                fontWeight = FontWeight.Bold,
                color = if (highlighted) color else Color.White
            )
            Text(
                text = "岁",                             // 单位
                fontSize = 16.sp,
                color = if (highlighted) color else Color(0xFF808080), // 高亮用主题色
                modifier = Modifier.padding(bottom = 4.dp) // 与数字底线对齐
            )
        }

        Spacer(modifier = Modifier.height(4.dp))       // 行间距

        Text(
            text = "约 ${UserPreferences.formatLifespan(lifespan)}", // 换算值（如"约 79年1个月22天"）
            fontSize = 13.sp,
            color = if (highlighted) color.copy(alpha = 0.8f) else Color(0xFF808080) // 高亮半透明
        )
    }
}
