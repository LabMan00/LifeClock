package com.example.lifeclock // 应用包名

import android.widget.Toast // Android Toast提示
import androidx.compose.foundation.clickable // 点击修饰符
import androidx.compose.foundation.layout.Box // 层叠布局
import androidx.compose.foundation.layout.Column // 垂直布局
import androidx.compose.foundation.layout.Row // 水平布局
import androidx.compose.foundation.layout.Spacer // 间距
import androidx.compose.foundation.layout.fillMaxSize // 填充最大尺寸
import androidx.compose.foundation.layout.fillMaxWidth // 填充最大宽度
import androidx.compose.foundation.layout.height // 固定高度
import androidx.compose.foundation.layout.padding // 内边距
import androidx.compose.foundation.lazy.LazyColumn // 懒加载列表
import androidx.compose.foundation.lazy.itemsIndexed // 带索引列表项
import androidx.compose.foundation.lazy.rememberLazyListState // 列表滚动状态
import androidx.compose.foundation.rememberScrollState // 滚动状态
import androidx.compose.foundation.verticalScroll // 垂直滚动
import androidx.compose.material.icons.Icons // Material图标
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 返回箭头
import androidx.compose.material3.AlertDialog // 弹窗
import androidx.compose.material3.ExperimentalMaterial3Api // 实验API
import androidx.compose.material3.HorizontalDivider // 分割线
import androidx.compose.material3.Icon // 图标
import androidx.compose.material3.IconButton // 图标按钮
import androidx.compose.material3.MaterialTheme // 主题
import androidx.compose.material3.OutlinedTextField // 文本输入框
import androidx.compose.material3.Scaffold // 脚手架
import androidx.compose.material3.Text // 文本
import androidx.compose.material3.TextButton // 文本按钮
import androidx.compose.material3.TopAppBar // 导航栏
import androidx.compose.material3.TopAppBarDefaults // 导航栏配置
import androidx.compose.runtime.Composable // Composable标记
import androidx.compose.runtime.LaunchedEffect // 副作用
import androidx.compose.runtime.getValue // 委托读取
import androidx.compose.runtime.mutableStateOf // 可观察状态
import androidx.compose.runtime.remember // 跨重组记忆
import androidx.compose.runtime.setValue // 委托写入
import androidx.compose.ui.Alignment // 对齐
import androidx.compose.ui.Modifier // 修饰符
import androidx.compose.ui.platform.LocalContext // 上下文
import androidx.compose.ui.text.font.FontWeight // 字体粗细
import androidx.compose.ui.unit.dp // dp单位
import androidx.compose.ui.unit.sp // sp单位

// 收入项目数据结构：存储键、显示标签、当前金额
data class IncomeItem(val key: String, val label: String, val value: Float)

// 收入设置页面：月薪、存款、固定支出 + 五险一金子页面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(onBack: () -> Unit, onSalaryChanged: (Float) -> Unit) {
    val context = LocalContext.current                              // 上下文
    var salary by remember { mutableStateOf(UserPreferences.getSalary(context)) }     // 月薪
    var deposit by remember { mutableStateOf(UserPreferences.getDeposit(context)) }   // 每月存款
    var fixedExpense by remember { mutableStateOf(UserPreferences.getFixedExpense(context)) } // 月固定支出
    var base by remember { mutableStateOf(UserPreferences.getContributionBase(context, salary)) } // 缴费基数
    var editingKey by remember { mutableStateOf<String?>(null) }     // 当前编辑的收入项key
    var editText by remember { mutableStateOf("") }                  // 编辑框文本
    var showInsuranceDialog by remember { mutableStateOf(false) }    // 五险一金弹窗状态

    // 住房公积金：当前费率（-1=使用默认12%）和计算金额
    val housingRate = UserPreferences.getHousingFundRate(context)       // 读取存储的费率
    val currentHousingRate = if (housingRate >= 0f) housingRate else UserPreferences.RATE_HOUSING_FUND
    val housingAmount = base * currentHousingRate                       // 公积金金额 = 缴费基数 × 费率

    // 主页收入项目列表
    val mainItems = listOf(
        IncomeItem(UserPreferences.KEY_SALARY, "你的月薪", salary),       // 月薪
        IncomeItem("deposit", "你的存款", deposit),                       // 每月存款
        IncomeItem("fixed_expense", "月固定支出", fixedExpense)            // 月固定支出
    )

    // 五险一金项目列表
    val insuranceItems = listOf(
        IncomeItem("contribution_base", "缴费基数", base),                // 缴费基数
        IncomeItem(UserPreferences.KEY_PENSION, "养老保险",
            UserPreferences.getIncomeValue(context, UserPreferences.KEY_PENSION, base)),
        IncomeItem(UserPreferences.KEY_MEDICAL, "医疗保险",
            UserPreferences.getIncomeValue(context, UserPreferences.KEY_MEDICAL, base)),
        IncomeItem(UserPreferences.KEY_UNEMPLOYMENT, "失业保险",
            UserPreferences.getIncomeValue(context, UserPreferences.KEY_UNEMPLOYMENT, base)),
        IncomeItem(UserPreferences.KEY_WORK_INJURY, "工伤保险",
            UserPreferences.getIncomeValue(context, UserPreferences.KEY_WORK_INJURY, base)),
        IncomeItem(UserPreferences.KEY_MATERNITY, "生育保险",
            UserPreferences.getIncomeValue(context, UserPreferences.KEY_MATERNITY, base)),
        IncomeItem(UserPreferences.KEY_HOUSING_FUND, "住房公积金", housingAmount)
    )

    // 通用金额编辑弹窗
    if (editingKey != null) {
        val label = (mainItems + insuranceItems).find { it.key == editingKey }?.label ?: ""
        val isSpecial = editingKey == UserPreferences.KEY_SALARY || editingKey == "contribution_base"
        AlertDialog(
            onDismissRequest = { editingKey = null },
            title = { Text(label) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) editText = newValue
                        },
                        label = { Text("金额 (元)") },
                        singleLine = true
                    )
                    if (isSpecial) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("修改后五险一金将按默认费率重新计算", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = editText.toFloatOrNull()
                    if (value != null && value < 0) {
                        Toast.makeText(context, "小于0的数值无法设置", Toast.LENGTH_SHORT).show()
                    } else if (value != null && value >= 0) {
                        when (editingKey) {
                            UserPreferences.KEY_SALARY -> {
                                salary = value; UserPreferences.updateSalary(context, value); onSalaryChanged(value) }
                            "deposit" -> { deposit = value; UserPreferences.updateDeposit(context, value) }
                            "fixed_expense" -> { fixedExpense = value; UserPreferences.updateFixedExpense(context, value) }
                            "contribution_base" -> {
                                base = value; UserPreferences.updateContributionBase(context, value)
                                listOf(UserPreferences.KEY_PENSION, UserPreferences.KEY_MEDICAL,
                                    UserPreferences.KEY_UNEMPLOYMENT, UserPreferences.KEY_WORK_INJURY,
                                    UserPreferences.KEY_MATERNITY, UserPreferences.KEY_HOUSING_FUND
                                ).forEach { key -> UserPreferences.updateIncomeItem(context, key, -1f) }
                                UserPreferences.updateHousingFundRate(context, -1f)
                            }
                            else -> UserPreferences.updateIncomeItem(context, editingKey!!, value)
                        }
                        editingKey = null
                    }
                }) { Text("确认") }
            },
            dismissButton = { TextButton(onClick = { editingKey = null }) { Text("取消") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("你的收入") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(top = 8.dp).verticalScroll(rememberScrollState())) {

            // === 主页列表：月薪、存款、固定支出、五险一金入口 ===
            mainItems.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().clickable {
                    editText = if (item.value > 0f) "%.2f".format(item.value) else ""; editingKey = item.key
                }.padding(horizontal = 24.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.label, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Text(text = "%.2f".format(item.value.coerceAtLeast(0f)), fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = " 元", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
            }

            // 五险一金入口行
            Row(modifier = Modifier.fillMaxWidth().clickable { showInsuranceDialog = true }
                .padding(horizontal = 24.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "五险一金", fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text(text = "缴费基数 / 养老 / 医疗 / 失业 / 工伤 / 生育 / 公积金", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
        }
    }

    // 五险一金弹窗
    if (showInsuranceDialog) {
        var showRateDialog by remember { mutableStateOf(false) }
        if (showRateDialog) {
            HousingRatePickerDialog(
                currentRate = currentHousingRate,
                onDismiss = { showRateDialog = false },
                onConfirm = { rate ->
                    UserPreferences.updateHousingFundRate(context, rate)
                    UserPreferences.updateIncomeItem(context, UserPreferences.KEY_HOUSING_FUND, -1f)
                    showRateDialog = false
                }
            )
        }
        AlertDialog(
            onDismissRequest = { showInsuranceDialog = false },
            title = { Text("五险一金") },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(350.dp).verticalScroll(rememberScrollState())) {
                    insuranceItems.forEach { item ->
                        val isHousing = item.key == UserPreferences.KEY_HOUSING_FUND
                        Row(modifier = Modifier.fillMaxWidth().clickable {
                            if (isHousing) showRateDialog = true
                            else { editText = if (item.value > 0f) "%.2f".format(item.value) else ""; editingKey = item.key }
                        }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.label, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            if (isHousing) Text(text = "${(currentHousingRate * 100).toInt()}%  ", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "%.2f".format(item.value.coerceAtLeast(0f)), fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = " 元", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showInsuranceDialog = false }) { Text("关闭") } },
            dismissButton = {}
        )
    }
}

// 住房公积金费率滚动选择器（0%-12%）
@Composable
private fun HousingRatePickerDialog(currentRate: Float, onDismiss: () -> Unit, onConfirm: (Float) -> Unit) {
    var selected by remember { mutableStateOf((currentRate * 100).toInt().coerceIn(0, 12)) }
    val listState = rememberLazyListState(); val rates = (0..12).toList()
    LaunchedEffect(Unit) { listState.scrollToItem(selected) }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("住房公积金费率") },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    itemsIndexed(rates) { index, rate ->
                        val isSelected = index == selected
                        Box(modifier = Modifier.fillMaxWidth().clickable { selected = rate }.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center) {
                            Text(text = "$rate%", fontSize = if (isSelected) 24.sp else 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selected / 100f) }) { Text("确认") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
