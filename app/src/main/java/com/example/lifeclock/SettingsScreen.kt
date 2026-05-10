package com.example.lifeclock // 应用包名

import androidx.compose.foundation.clickable // 点击修饰符
import androidx.compose.foundation.layout.Column // 垂直布局
import androidx.compose.foundation.layout.Row // 水平布局
import androidx.compose.foundation.layout.fillMaxSize // 填充最大尺寸
import androidx.compose.foundation.layout.fillMaxWidth // 填充最大宽度
import androidx.compose.foundation.layout.padding // 内边距
import androidx.compose.material.icons.Icons // Material图标
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 返回箭头图标
import androidx.compose.material3.ExperimentalMaterial3Api // Material3实验API
import androidx.compose.material3.HorizontalDivider // 水平分割线
import androidx.compose.material3.Icon // 图标组件
import androidx.compose.material3.IconButton // 图标按钮
import androidx.compose.material3.MaterialTheme // Material3主题
import androidx.compose.material3.Scaffold // 脚手架布局
import androidx.compose.material3.Text // 文本组件
import androidx.compose.material3.TopAppBar // 顶部导航栏
import androidx.compose.material3.TopAppBarDefaults // 导航栏默认配置
import androidx.compose.runtime.Composable // Composable标记
import androidx.compose.ui.Alignment // 对齐方式
import androidx.compose.ui.Modifier // UI修饰符
import androidx.compose.ui.unit.dp // dp单位
import androidx.compose.ui.unit.sp // sp单位

// 设置页面：显示"你的信息"和"你的收入"两个入口
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,                   // 返回回调
    onNavigateToBasicInfo: () -> Unit,    // 进入基本信息页
    onNavigateToIncome: () -> Unit        // 进入收入设置页
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") }, // 页面标题
                navigationIcon = {         // 返回按钮
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface // 导航栏背景色
                )
            )
        }
    ) { padding -> // Scaffold内边距
        Column(
            modifier = Modifier
                .fillMaxSize()            // 填充屏幕
                .padding(padding)         // 避开导航栏
                .padding(top = 8.dp)      // 顶部间距
        ) {
            // 你的信息行：点击进入预期寿命/生日/性别/地区设置
            Row(
                modifier = Modifier
                    .fillMaxWidth()       // 填充宽度
                    .clickable { onNavigateToBasicInfo() } // 点击跳转
                    .padding(horizontal = 24.dp, vertical = 18.dp), // 内边距
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "你的信息", fontSize = 16.sp, modifier = Modifier.weight(1f)) // 标签
                Text(                       // 右侧说明文字
                    text = "预期寿命 / 生日 / 性别 / 地区",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // 次要文字色
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp)) // 分割线

            // 你的收入行：点击进入薪资和五险一金设置
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToIncome() }
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "你的收入", fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text(
                    text = "薪资 / 五险一金",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}
