package com.example.lifeclock // 应用包名

import androidx.compose.foundation.layout.Column // 垂直布局
import androidx.compose.foundation.layout.Row // 水平布局
import androidx.compose.foundation.layout.fillMaxSize // 填充最大尺寸
import androidx.compose.foundation.layout.fillMaxWidth // 填充最大宽度
import androidx.compose.foundation.layout.padding // 内边距
import androidx.compose.foundation.lazy.rememberLazyListState // 记住列表滚动状态
import androidx.compose.material.icons.Icons // Material图标
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 返回箭头
import androidx.compose.material3.ExperimentalMaterial3Api // 实验API
import androidx.compose.material3.HorizontalDivider // 分割线
import androidx.compose.material3.Icon // 图标
import androidx.compose.material3.IconButton // 图标按钮
import androidx.compose.material3.MaterialTheme // 主题
import androidx.compose.material3.Scaffold // 脚手架
import androidx.compose.material3.Text // 文本
import androidx.compose.material3.TopAppBar // 导航栏
import androidx.compose.material3.TopAppBarDefaults // 导航栏配置
import androidx.compose.runtime.Composable // Composable标记
import androidx.compose.runtime.LaunchedEffect // 副作用
import androidx.compose.runtime.getValue // 委托读取
import androidx.compose.runtime.mutableStateOf // 可观察状态
import androidx.compose.runtime.remember // 跨重组记忆
import androidx.compose.runtime.setValue // 委托写入
import androidx.compose.ui.Modifier // 修饰符
import androidx.compose.ui.platform.LocalContext // 上下文
import androidx.compose.ui.text.font.FontWeight // 字体粗细
import androidx.compose.ui.text.style.TextAlign // 文本对齐
import androidx.compose.ui.unit.dp // dp单位
import androidx.compose.ui.unit.sp // sp单位

// 地区选择页面：国家/省份/城市三级联动滚动选择器
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionPickerScreen(
    onBack: () -> Unit,                                // 返回回调
    onRegionSelected: (String, String, String) -> Unit  // 地区选择完成回调
) {
    val context = LocalContext.current                  // 获取上下文
    val currentRegion = UserPreferences.getRegion(context) // 读取当前已保存的地区
    val regions = RegionData.regions                    // 地区数据源

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

    val isComingSoon = regions[selectedCountry].name == "敬请期待" // 是否选中了敬请期待

    // 当前三级选项文本列表
    val countries = regions.map { it.name }                                                         // 国家列表
    val provinces = regions[selectedCountry].children.map { it.name }                               // 省份列表（联动）
    val cities = regions[selectedCountry].children[selectedProvince].children.map { it.name }        // 城市列表（联动）

    // 三列各自独立的滚动状态
    val countryState = rememberLazyListState()
    val provinceState = rememberLazyListState()
    val cityState = rememberLazyListState()

    // 打开时自动滚动到用户已保存的地区位置
    LaunchedEffect(Unit) {
        countryState.scrollToItem(selectedCountry)
        provinceState.scrollToItem(selectedProvince)
        cityState.scrollToItem(selectedCity)
    }

    // 保存地区并返回
    fun saveAndBack() {
        val country = regions[selectedCountry].name                                               // 国家名
        val province = if (!isComingSoon) regions[selectedCountry].children[selectedProvince].name // 省份名
                       else "敬请期待"
        val city = if (!isComingSoon) regions[selectedCountry].children[selectedProvince].children[selectedCity].name // 城市名
                   else "敬请期待"
        UserPreferences.updateRegion(context, country, province, city)                            // 持久化
        onRegionSelected(country, province, city)                                                 // 通知
        onBack()                                                                                  // 返回
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("你的地区") },
                navigationIcon = { IconButton(onClick = { saveAndBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 当前完整选择路径显示（如"中国 北京市 东城区"）
            Text(
                text = "${regions[selectedCountry].name} ${provinces[selectedProvince]} ${cities[selectedCity]}",
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                textAlign = TextAlign.Center
            )

            HorizontalDivider()

            // 三列滚动选择器：国家 | 省份 | 城市
            Row(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                WheelColumn(                                                       // 国家列
                    items = countries, state = countryState, selectedIndex = selectedCountry,
                    onSelect = { index -> selectedCountry = index; selectedProvince = 0; selectedCity = 0 }, // 切换国家时重置下级
                    modifier = Modifier.weight(1f))
                WheelColumn(                                                       // 省份列（敬请期待时灰显）
                    items = provinces, state = provinceState, selectedIndex = selectedProvince,
                    grayed = isComingSoon,                                         // 敬请期待→全部灰色
                    onSelect = { index -> selectedProvince = index; selectedCity = 0 },
                    modifier = Modifier.weight(1f))
                WheelColumn(                                                       // 城市列（敬请期待时灰显）
                    items = cities, state = cityState, selectedIndex = selectedCity,
                    grayed = isComingSoon,
                    onSelect = { index -> selectedCity = index },
                    modifier = Modifier.weight(1f))
            }
        }
    }
}
