package com.example.lifeclock // 应用包名

import androidx.compose.foundation.BorderStroke // 边框描边
import androidx.compose.foundation.background // 背景色
import androidx.compose.foundation.border // 边框修饰符
import androidx.compose.foundation.clickable // 点击修饰符
import androidx.compose.foundation.layout.Arrangement // 排列方式
import androidx.compose.foundation.layout.Column // 垂直布局
import androidx.compose.foundation.layout.Row // 水平布局
import androidx.compose.foundation.layout.Spacer // 间距占位
import androidx.compose.foundation.layout.fillMaxSize // 填充最大尺寸
import androidx.compose.foundation.layout.fillMaxWidth // 填充最大宽度
import androidx.compose.foundation.layout.height // 固定高度
import androidx.compose.foundation.layout.padding // 内边距
import androidx.compose.foundation.layout.width // 固定宽度
import androidx.compose.foundation.shape.RoundedCornerShape // 圆角形状
import androidx.compose.material3.Button // 按钮组件
import androidx.compose.material3.ButtonDefaults // 按钮默认样式
import androidx.compose.material3.MaterialTheme // Material3主题
import androidx.compose.material3.Text // 文本组件
import androidx.compose.runtime.Composable // Composable标记
import androidx.compose.runtime.getValue // 委托属性读取
import androidx.compose.runtime.mutableStateOf // 可观察状态
import androidx.compose.runtime.remember // 跨重组记忆
import androidx.compose.runtime.setValue // 委托属性写入
import androidx.compose.ui.Alignment // 对齐方式
import androidx.compose.ui.Modifier // UI修饰符
import androidx.compose.ui.draw.clip // 裁剪
import androidx.compose.ui.graphics.Color // 颜色
import androidx.compose.ui.platform.LocalContext // 获取上下文
import androidx.compose.ui.text.font.FontWeight // 字体粗细
import androidx.compose.ui.text.style.TextAlign // 文本对齐
import androidx.compose.ui.unit.dp // dp单位
import androidx.compose.ui.unit.sp // sp单位
import java.time.LocalDate // Java日期
import java.time.format.DateTimeFormatter // 日期格式化

// 首次安装时的初始设置页面：选择生日、预期寿命和性别
@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current                        // 获取上下文
    var birthday by remember { mutableStateOf<LocalDate?>(null) } // 选中的生日（null=未选）
    var lifespan by remember { mutableStateOf(80f) }           // 选中的预期寿命（默认80）
    var gender by remember { mutableStateOf<String?>(null) }    // 选中的性别（null=未选）
    var showBirthdayDialog by remember { mutableStateOf(false) } // 生日选择弹窗可见性
    var showLifespanDialog by remember { mutableStateOf(false) } // 寿命选择弹窗可见性
    var errorMessage by remember { mutableStateOf<String?>(null) } // 错误提示文字

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日") // 日期显示格式

    // 生日滚动选择器弹窗
    if (showBirthdayDialog) {
        BirthdayPickerDialog(
            currentBirthday = birthday ?: LocalDate.of(2000, 1, 1), // 当前生日或默认1990-01-01
            onDismiss = { showBirthdayDialog = false },              // 关闭弹窗
            onConfirm = { newDate ->                                 // 确认选择
                birthday = newDate                                    // 更新生日
                errorMessage = null                                   // 清除错误
                showBirthdayDialog = false                            // 关闭弹窗
            }
        )
    }

    // 预期寿命滚动选择器弹窗（1-120岁）
    if (showLifespanDialog) {
        LifespanPickerDialog(
            currentLifespan = lifespan,           // 当前寿命值
            onDismiss = { showLifespanDialog = false },
            onConfirm = { value ->                // 确认选择
                lifespan = value                   // 更新寿命
                errorMessage = null                // 清除错误
                showLifespanDialog = false         // 关闭弹窗
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()                        // 填充屏幕
            .padding(32.dp),                      // 四周32dp边距
        horizontalAlignment = Alignment.CenterHorizontally, // 子项水平居中
        verticalArrangement = Arrangement.Center             // 子项垂直居中
    ) {
        // 应用标题
        Text(
            text = "生命时钟",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,      // 主题色
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 英文副标题
        Text(
            text = "Life Clock",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant, // 次要文字色
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 选择生日按钮（点击弹出年月日滚动选择器）
        Button(
            onClick = { showBirthdayDialog = true },
            modifier = Modifier.fillMaxWidth(0.85f).height(52.dp), // 85%宽度，52dp高
            shape = RoundedCornerShape(12.dp),         // 12dp圆角
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,    // 次要容器色
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer     // 容器上文字色
            )
        ) {
            Text(
                text = if (birthday != null) "出生日期: ${birthday!!.format(dateFormatter)}" // 已选显示日期
                else "选择出生日期",                                                          // 未选显示提示
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 选择预期寿命按钮（点击弹出1-120滚动选择器）
        Button(
            onClick = { showLifespanDialog = true },
            modifier = Modifier.fillMaxWidth(0.85f).height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = "预期寿命: ${lifespan.toInt()} 岁", // 显示当前设定值
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 性别选择标签
        Text(text = "选择性别", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(10.dp))

        // 男女选择chip（蓝色男/粉色女）
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            GenderChip(                                      // 男性chip
                label = "♂ 男",
                selected = gender == UserPreferences.GENDER_MALE, // 选中时高亮
                color = Color(0xFF2196F3),                         // 蓝色
                onClick = { gender = UserPreferences.GENDER_MALE; errorMessage = null }
            )
            Spacer(modifier = Modifier.width(24.dp))
            GenderChip(                                      // 女性chip
                label = "♀ 女",
                selected = gender == UserPreferences.GENDER_FEMALE,
                color = Color(0xFFE91E63),                         // 粉色
                onClick = { gender = UserPreferences.GENDER_FEMALE; errorMessage = null }
            )
        }

        // 错误信息显示
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = errorMessage!!, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(36.dp))

        // 开始按钮：校验并保存设置，进入主页
        Button(
            onClick = {
                when {                                                          // 校验各字段
                    birthday == null -> errorMessage = "请选择出生日期"           // 未选生日
                    gender == null -> errorMessage = "请选择性别"                // 未选性别
                    lifespan <= 0 -> errorMessage = "请输入有效的预期寿命"       // 无效寿命
                    lifespan > 150 -> errorMessage = "预期寿命不能超过150岁"     // 寿命过大
                    else -> {                                                   // 校验通过
                        UserPreferences.saveUserData(                            // 保存所有数据
                            context, birthday.toString(), lifespan.toInt(), gender!!)
                        onSetupComplete()                                       // 通知父组件完成设置
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.85f).height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) // 主色按钮
        ) {
            Text(text = "开始", fontSize = 18.sp)
        }
    }
}

// 性别选择chip组件（选中时显示彩色边框和背景，未选中时灰色）
@Composable
private fun GenderChip(
    label: String,       // 显示文字
    selected: Boolean,   // 是否选中
    color: Color,        // 主题色
    onClick: () -> Unit  // 点击回调
) {
    val borderColor = if (selected) color else Color.Gray                    // 选中彩色边框，未选中灰色
    val bgColor = if (selected) color.copy(alpha = 0.15f) else Color.Transparent // 选中半透明背景

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))                                 // 24dp圆角裁剪
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(24.dp)) // 2dp边框
            .background(bgColor, RoundedCornerShape(24.dp))                  // 圆角背景
            .clickable(onClick = onClick)                                    // 点击事件
            .padding(horizontal = 28.dp, vertical = 12.dp),                 // 内边距
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,                                                    // "♂ 男"或"♀ 女"
            fontSize = 18.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, // 选中加粗
            color = if (selected) color else Color.Gray                       // 选中彩色
        )
    }
}
