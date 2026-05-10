package com.example.lifeclock // 应用包名

import androidx.compose.foundation.clickable // 点击修饰符
import androidx.compose.foundation.layout.Box // 层叠布局容器
import androidx.compose.foundation.layout.Column // 垂直布局
import androidx.compose.foundation.layout.Row // 水平排列布局
import androidx.compose.foundation.layout.fillMaxHeight // 填充最大高度
import androidx.compose.foundation.layout.fillMaxSize // 填充最大尺寸
import androidx.compose.foundation.layout.fillMaxWidth // 填充最大宽度
import androidx.compose.foundation.layout.height // 固定高度
import androidx.compose.foundation.layout.padding // 内边距
import androidx.compose.foundation.lazy.LazyColumn // 懒加载列表
import androidx.compose.foundation.lazy.itemsIndexed // 带索引的列表项
import androidx.compose.foundation.lazy.rememberLazyListState // 记住列表滚动状态
import androidx.compose.material3.AlertDialog // Material3弹窗
import androidx.compose.material3.MaterialTheme // Material3主题
import androidx.compose.material3.Text // 文本组件
import androidx.compose.material3.TextButton // 文本按钮
import androidx.compose.runtime.Composable // Composable函数标记
import androidx.compose.runtime.LaunchedEffect // 组合后执行的副作用
import androidx.compose.runtime.getValue // 委托属性读取
import androidx.compose.runtime.mutableStateOf // 可观察状态
import androidx.compose.runtime.remember // 记住变量（跨重组）
import androidx.compose.runtime.setValue // 委托属性写入
import androidx.compose.ui.Alignment // 对齐方式
import androidx.compose.ui.Modifier // UI修饰符
import androidx.compose.ui.graphics.Color // 颜色
import androidx.compose.ui.platform.LocalContext // 上下文
import androidx.compose.ui.text.font.FontWeight // 字体粗细
import androidx.compose.ui.text.style.TextAlign // 文本对齐
import androidx.compose.ui.unit.dp // dp密度单位
import androidx.compose.ui.unit.sp // sp字体单位
import java.time.LocalDate // Java日期类

// 预期寿命滚动选择器弹窗（1-120岁）
@Composable
fun LifespanPickerDialog(
    currentLifespan: Float,     // 当前寿命值
    onDismiss: () -> Unit,      // 取消回调
    onConfirm: (Float) -> Unit  // 确认回调（返回选中的寿命值）
) {
    var selected by remember { mutableStateOf(currentLifespan.toInt().coerceIn(1, 120)) } // 当前选中值（1-120范围）
    val listState = rememberLazyListState() // 列表滚动状态
    val items = (1..120).toList()           // 1到120的整数列表

    LaunchedEffect(Unit) {
        listState.scrollToItem(selected - 1) // 打开时滚动到当前选中值
    }

    AlertDialog(
        onDismissRequest = onDismiss,         // 点击外部取消
        title = { Text("选择预期寿命") },       // 弹窗标题
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()           // 填充宽度
                    .height(250.dp),          // 固定高度250dp
                contentAlignment = Alignment.Center // 内容居中
            ) {
                LazyColumn(
                    state = listState,        // 绑定滚动状态
                    modifier = Modifier.fillMaxSize(), // 填充父容器
                    horizontalAlignment = Alignment.CenterHorizontally // 水平居中
                ) {
                    itemsIndexed(items) { index, value -> // 遍历1-120
                        val isSelected = index == selected - 1 // 判断是否为选中项
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()           // 填充宽度
                                .clickable { selected = value } // 点击选中
                                .padding(vertical = 6.dp), // 垂直内边距
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$value 岁",       // 显示"X 岁"
                                fontSize = if (isSelected) 24.sp else 16.sp, // 选中项大号
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, // 选中项加粗
                                color = if (isSelected) MaterialTheme.colorScheme.primary // 选中项主题色
                                else MaterialTheme.colorScheme.onSurface // 非选中项默认色
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.toFloat()) }) { Text("确认") } // 确认按钮
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") } // 取消按钮
        }
    )
}

// 生日年月日三列滚动选择器弹窗
@Composable
fun BirthdayPickerDialog(
    currentBirthday: LocalDate,   // 当前生日
    onDismiss: () -> Unit,        // 取消回调
    onConfirm: (LocalDate) -> Unit // 确认回调（返回选中日期）
) {
    var selectedYear by remember { mutableStateOf(currentBirthday.year) }       // 选中的年
    var selectedMonth by remember { mutableStateOf(currentBirthday.monthValue) } // 选中的月
    var selectedDay by remember { mutableStateOf(currentBirthday.dayOfMonth) }   // 选中的日

    val years = (1930..LocalDate.now().year).toList().reversed() // 年份列表（1930-今年，倒序）
    val months = (1..12).toList()                                 // 月份1-12

    // 根据年月计算当月天数（处理闰年）
    val daysInMonth = when (selectedMonth) {
        2 -> if (java.time.Year.isLeap(selectedYear.toLong())) 29 else 28 // 2月闰年29天
        4, 6, 9, 11 -> 30    // 小月30天
        else -> 31            // 大月31天
    }
    val days = (1..daysInMonth).toList() // 当月天数列表

    val yearState = rememberLazyListState()  // 年列滚动状态
    val monthState = rememberLazyListState() // 月列滚动状态
    val dayState = rememberLazyListState()   // 日列滚动状态

    LaunchedEffect(Unit) {
        yearState.scrollToItem(years.indexOf(selectedYear))  // 滚动到当前年
        monthState.scrollToItem(selectedMonth - 1)            // 滚动到当前月
        dayState.scrollToItem(selectedDay - 1)                // 滚动到当前日
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择生日") },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp), // 弹窗内容高度
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                WheelColumn(
                    items = years.map { "${it}年" },              // 年份选项
                    state = yearState,                             // 滚动状态
                    selectedIndex = years.indexOf(selectedYear),   // 当前选中索引
                    onSelect = { index -> selectedYear = years[index] }, // 选中回调
                    modifier = Modifier.weight(1f)                // 等分宽度
                )
                WheelColumn(
                    items = months.map { "${it}月" },              // 月份选项
                    state = monthState,
                    selectedIndex = selectedMonth - 1,
                    onSelect = { index -> selectedMonth = months[index] },
                    modifier = Modifier.weight(1f)
                )
                WheelColumn(
                    items = days.map { "${it}日" },                // 日期选项
                    state = dayState,
                    selectedIndex = selectedDay - 1,
                    onSelect = { index -> selectedDay = days[index] },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newDate = try {
                    LocalDate.of(selectedYear, selectedMonth, selectedDay) // 组装日期
                } catch (e: Exception) {
                    null // 无效日期返回null（如2月30日切换月份时）
                }
                if (newDate != null) onConfirm(newDate) // 有效日期才确认
            }) { Text("确认") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// 通用滚轮列组件（用于地区选择器、生日选择器等场景）
@Composable
fun WheelColumn(
    items: List<String>,                                                  // 选项文本列表
    state: androidx.compose.foundation.lazy.LazyListState,                // 列表滚动状态
    selectedIndex: Int,                                                   // 选中项索引
    onSelect: (Int) -> Unit,                                              // 选中回调
    modifier: Modifier = Modifier,                                        // 修饰符
    grayed: Boolean = false                                               // 是否灰显（用于"敬请期待"等不可选状态）
) {
    LazyColumn(
        state = state,                                     // 绑定滚动状态
        modifier = modifier.fillMaxHeight(),               // 填充高度
        horizontalAlignment = Alignment.CenterHorizontally  // 水平居中
    ) {
        itemsIndexed(items) { index, item ->               // 遍历选项
            val isSelected = index == selectedIndex         // 是否选中
            Box(
                modifier = Modifier
                    .fillMaxWidth()                        // 填充宽度
                    .clickable { onSelect(index) }         // 点击选择
                    .padding(vertical = 6.dp),             // 垂直内边距
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,                           // 选项文本
                    fontSize = if (isSelected) 18.sp else 14.sp,     // 选中项18sp，其他14sp
                    fontWeight = if (isSelected) FontWeight.Bold     // 选中项加粗
                                 else FontWeight.Normal,
                    color = if (grayed) Color(0xFFB0B0B0)            // 灰显状态
                    else if (isSelected) MaterialTheme.colorScheme.primary // 选中主题色
                    else MaterialTheme.colorScheme.onSurface,        // 默认颜色
                    textAlign = TextAlign.Center                     // 文本居中
                )
            }
        }
    }
}

// 地区选择器弹窗：国家/省份/城市三级联动滚动选择器
@Composable
fun RegionPickerDialog(
    onDismiss: () -> Unit,                                // 取消回调
    onConfirm: (String, String, String) -> Unit            // 确认回调（国家, 省份, 城市）
) {
    val context = LocalContext.current                      // 获取上下文
    val currentRegion = UserPreferences.getRegion(context)  // 读取当前已保存的地区
    val regions = RegionData.regions                        // 地区数据源

    // 初始化选中项为用户已保存的地区
    var selectedCountry by remember {
        mutableStateOf(regions.indexOfFirst { it.name == currentRegion.first }.coerceAtLeast(0)) // 国家索引
    }
    var selectedProvince by remember {
        val children = regions[selectedCountry].children
        mutableStateOf(children.indexOfFirst { it.name == currentRegion.second }.coerceAtLeast(0)) // 省份索引
    }
    var selectedCity by remember {
        val children = regions[selectedCountry].children[selectedProvince].children
        mutableStateOf(children.indexOfFirst { it.name == currentRegion.third }.coerceAtLeast(0))  // 城市索引
    }

    val isComingSoon = regions[selectedCountry].name == "敬请期待" // 是否选中"敬请期待"

    // 三级选项文本列表
    val countries = regions.map { it.name }                                                         // 国家列表
    val provinces = regions[selectedCountry].children.map { it.name }                               // 省份列表（联动）
    val cities = regions[selectedCountry].children[selectedProvince].children.map { it.name }        // 城市列表（联动）

    // 三列滚动状态
    val countryState = rememberLazyListState()  // 国家列
    val provinceState = rememberLazyListState() // 省份列
    val cityState = rememberLazyListState()     // 城市列

    // 打开时定位到已保存地区
    LaunchedEffect(Unit) {
        countryState.scrollToItem(selectedCountry)
        provinceState.scrollToItem(selectedProvince)
        cityState.scrollToItem(selectedCity)
    }

    AlertDialog(
        onDismissRequest = onDismiss,           // 点击外部取消
        title = { Text("选择地区") },            // 弹窗标题
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 当前选择路径
                Text(
                    text = "${regions[selectedCountry].name} ${provinces[selectedProvince]} ${cities[selectedCity]}",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
                // 三列滚动选择器
                Row(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    WheelColumn(                                                       // 国家列
                        items = countries, state = countryState, selectedIndex = selectedCountry,
                        onSelect = { index -> selectedCountry = index; selectedProvince = 0; selectedCity = 0 },
                        modifier = Modifier.weight(1f))
                    WheelColumn(                                                       // 省份列
                        items = provinces, state = provinceState, selectedIndex = selectedProvince,
                        grayed = isComingSoon,
                        onSelect = { index -> selectedProvince = index; selectedCity = 0 },
                        modifier = Modifier.weight(1f))
                    WheelColumn(                                                       // 城市列
                        items = cities, state = cityState, selectedIndex = selectedCity,
                        grayed = isComingSoon,
                        onSelect = { index -> selectedCity = index },
                        modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val country = regions[selectedCountry].name                                           // 最终国家
                val province = if (!isComingSoon) regions[selectedCountry].children[selectedProvince].name else "敬请期待"
                val city = if (!isComingSoon) regions[selectedCountry].children[selectedProvince].children[selectedCity].name else "敬请期待"
                UserPreferences.updateRegion(context, country, province, city)                        // 持久化
                onConfirm(country, province, city)                                                     // 回调
            }) { Text("确认") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
