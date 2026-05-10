package com.example.lifeclock // 应用包名

import androidx.compose.foundation.clickable // 点击修饰符
import androidx.compose.foundation.layout.Column // 垂直布局
import androidx.compose.foundation.layout.Row // 水平布局
import androidx.compose.foundation.layout.Spacer // 间距占位
import androidx.compose.foundation.layout.fillMaxSize // 填充最大尺寸
import androidx.compose.foundation.layout.fillMaxWidth // 填充最大宽度
import androidx.compose.foundation.layout.padding // 内边距
import androidx.compose.foundation.layout.width // 固定宽度
import androidx.compose.material.icons.Icons // Material图标
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 返回箭头
import androidx.compose.material3.ExperimentalMaterial3Api // 实验API
import androidx.compose.material3.HorizontalDivider // 水平分割线
import androidx.compose.material3.Icon // 图标
import androidx.compose.material3.IconButton // 图标按钮
import androidx.compose.material3.MaterialTheme // 主题
import androidx.compose.material3.Scaffold // 脚手架
import androidx.compose.material3.Switch // 开关组件
import androidx.compose.material3.Text // 文本
import androidx.compose.material3.TopAppBar // 导航栏
import androidx.compose.material3.TopAppBarDefaults // 导航栏配置
import androidx.compose.runtime.Composable // Composable标记
import androidx.compose.runtime.getValue // 委托读取
import androidx.compose.runtime.mutableStateOf // 可观察状态
import androidx.compose.runtime.remember // 跨重组记忆
import androidx.compose.runtime.setValue // 委托写入
import androidx.compose.ui.Alignment // 对齐
import androidx.compose.ui.Modifier // 修饰符
import androidx.compose.ui.platform.LocalContext // 上下文
import androidx.compose.ui.unit.dp // dp单位
import androidx.compose.ui.unit.sp // sp单位
import java.time.LocalDate // 日期
import java.time.format.DateTimeFormatter // 日期格式化

// 基本信息页面：管理用户的预期寿命、生日、性别、地区
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInfoScreen(
    birthday: LocalDate,                     // 生日
    gender: String,                          // 性别
    personalLifespan: Float,                 // 个人预期寿命
    onBack: () -> Unit,                      // 返回
    onBirthdayChanged: (LocalDate) -> Unit,  // 生日变更
    onGenderChanged: (String) -> Unit,       // 性别变更
    onLifespanChanged: (Float) -> Unit       // 寿命变更
) {
    var showRegionDialog by remember { mutableStateOf(false) } // 地区选择弹窗状态
    var showLifespanDialog by remember { mutableStateOf(false) } // 寿命弹窗状态
    var showBirthdayDialog by remember { mutableStateOf(false) } // 生日弹窗状态
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日") // 日期格式

    // 预期寿命滚动选择器弹窗
    if (showLifespanDialog) {
        LifespanPickerDialog(
            currentLifespan = personalLifespan,           // 当前值
            onDismiss = { showLifespanDialog = false },    // 关闭
            onConfirm = { value -> onLifespanChanged(value); showLifespanDialog = false } // 确认
        )
    }

    // 地区选择弹窗
    if (showRegionDialog) {
        RegionPickerDialog(
            onDismiss = { showRegionDialog = false },             // 关闭弹窗
            onConfirm = { _, _, _ -> showRegionDialog = false }   // 确认后关闭
        )
    }

    // 生日滚动选择器弹窗
    if (showBirthdayDialog) {
        BirthdayPickerDialog(
            currentBirthday = birthday,                   // 当前值
            onDismiss = { showBirthdayDialog = false },
            onConfirm = { newDate -> onBirthdayChanged(newDate); showBirthdayDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("你的信息") },            // 标题
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(top = 8.dp)) {

            // 预期寿命行：点击弹出1-120滚动选择器
            Row(modifier = Modifier.fillMaxWidth().clickable { showLifespanDialog = true }
                .padding(horizontal = 24.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "你的预期寿命", fontSize = 16.sp, modifier = Modifier.weight(1f))    // 标签
                Text(text = "%.0f 岁".format(personalLifespan), fontSize = 13.sp,               // 当前值
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

            // 生日行：点击弹出年月日滚动选择器
            Row(modifier = Modifier.fillMaxWidth().clickable { showBirthdayDialog = true }
                .padding(horizontal = 24.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "你的生日", fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text(text = birthday.format(dateFormatter), fontSize = 13.sp,                    // 格式化显示
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

            // 性别行：Switch切换男/女
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(text = "你的性别", fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text(text = if (gender == UserPreferences.GENDER_MALE) "男" else "女",  // 当前性别
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = gender == UserPreferences.GENDER_FEMALE, // 女=开，男=关
                    onCheckedChange = { isFemale ->
                        onGenderChanged(if (isFemale) UserPreferences.GENDER_FEMALE else UserPreferences.GENDER_MALE)
                    }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

            // 地区行：点击进入三列滚动选择器
            val region = UserPreferences.getRegion(LocalContext.current)     // 读取当前地区
            Row(modifier = Modifier.fillMaxWidth().clickable { showRegionDialog = true }
                .padding(horizontal = 24.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "你的地区", fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text(text = "${region.first} ${region.second} ${region.third}", // 完整地区路径
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}
