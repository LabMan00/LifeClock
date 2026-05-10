package com.example.lifeclock // 应用包名

import androidx.compose.foundation.Canvas // Canvas绘制
import androidx.compose.foundation.background // 背景色
import androidx.compose.foundation.layout.Arrangement // 排列方式
import androidx.compose.foundation.layout.Box // 层叠布局
import androidx.compose.foundation.layout.Column // 垂直布局
import androidx.compose.foundation.layout.Row // 水平布局
import androidx.compose.foundation.layout.Spacer // 间距
import androidx.compose.foundation.layout.fillMaxSize // 填充最大尺寸
import androidx.compose.foundation.layout.fillMaxWidth // 填充最大宽度
import androidx.compose.foundation.layout.height // 固定高度
import androidx.compose.foundation.layout.padding // 内边距
import androidx.compose.foundation.layout.size // 固定尺寸
import androidx.compose.foundation.layout.statusBarsPadding // 状态栏安全区
import androidx.compose.foundation.layout.width // 固定宽度
import androidx.compose.foundation.pager.HorizontalPager // 水平翻页器
import androidx.compose.foundation.pager.VerticalPager // 垂直翻页器
import androidx.compose.foundation.pager.rememberPagerState // 翻页状态
import androidx.compose.foundation.shape.CircleShape // 圆形
import androidx.compose.material3.DropdownMenu // 下拉菜单
import androidx.compose.material3.DropdownMenuItem // 下拉菜单项
import androidx.compose.material3.IconButton // 图标按钮
import androidx.compose.material3.Text // 文本
import androidx.compose.runtime.Composable // Composable标记
import androidx.compose.runtime.LaunchedEffect // 组合副作用
import androidx.compose.runtime.getValue // 值委托
import androidx.compose.runtime.mutableStateOf // 可观察状态
import androidx.compose.runtime.remember // 跨重组记忆
import androidx.compose.runtime.rememberCoroutineScope // 协程作用域
import androidx.compose.runtime.setValue // 值委托写入
import androidx.compose.ui.Alignment // 对齐
import androidx.compose.ui.Modifier // 修饰符
import androidx.compose.ui.draw.clip // 裁剪
import androidx.compose.ui.geometry.CornerRadius // 圆角半径
import androidx.compose.ui.geometry.Size // 尺寸
import androidx.compose.ui.graphics.Color // 颜色
import androidx.compose.ui.text.font.FontFamily // 字体族
import androidx.compose.ui.text.font.FontWeight // 字体粗细
import androidx.compose.ui.unit.dp // dp单位
import androidx.compose.ui.unit.sp // sp单位
import kotlinx.coroutines.delay // 协程延迟
import kotlinx.coroutines.launch // 协程启动
import java.text.NumberFormat // 数字格式化（千分位）
import java.time.Duration // 时间差
import java.time.LocalDate // 日期
import java.time.LocalDateTime // 日期时间
import java.util.Locale // 本地化

// 倒计时数据结构：各时间单位的剩余值 + 是否已超时
data class CountdownData(
    val years: Long,    // 剩余年数
    val days: Long,     // 剩余天数（不足一年的部分）
    val hours: Long,    // 剩余小时（不足一天的部分）
    val minutes: Long,  // 剩余分钟
    val seconds: Long,  // 剩余秒数
    val millis: Long,   // 剩余毫秒
    val isOver: Boolean // 是否已超过终点日期
)

// 根据生日和预测寿命（年）计算倒计时剩余时间
fun calculateRemaining(birthday: LocalDate, lifespanYears: Float): CountdownData {
    val totalLifespanDays = (lifespanYears * 365.25).toLong()          // 寿命总天数
    val deathDate = birthday.atStartOfDay().plusDays(totalLifespanDays) // 终点日期时间
    val now = LocalDateTime.now()                                       // 当前日期时间
    val remaining = Duration.between(now, deathDate)                    // 剩余时长

    if (remaining.isNegative || remaining.isZero) {                     // 已超过终点
        return CountdownData(0, 0, 0, 0, 0, 0, true)                   // 返回全零+超时标记
    }

    val totalMs = remaining.toMillis()        // 剩余总毫秒数
    val totalSeconds = totalMs / 1000         // 剩余总秒数
    val totalMinutes = totalSeconds / 60      // 剩余总分钟数
    val totalHours = totalMinutes / 60        // 剩余总小时数
    val totalDays = totalHours / 24           // 剩余总天数

    val years = totalDays / 365               // 剩余年数（一年按365天）
    val days = totalDays % 365                // 不足一年的剩余天数
    val hours = totalHours % 24               // 不足一天的剩余小时
    val minutes = totalMinutes % 60           // 不足一小时的剩余分钟
    val seconds = totalSeconds % 60           // 不足一分钟的剩余秒数
    val millis = totalMs % 1000               // 不足一秒的剩余毫秒数

    return CountdownData(years, days, hours, minutes, seconds, millis, false)
}

// 主页倒计时屏幕：支持上下滑动切换模式、左右滑动查看储蓄/收入/养老金
@Composable
fun CountdownScreen(
    birthday: LocalDate,                              // 用户生日
    personalLifespan: Float,                          // 当前活跃的寿命值
    lifespanSource: String,                           // 寿命来源（personal/overall/male/female/retirement）
    retirementAge: Float,                             // 计算出的退休年龄
    userPersonalLifespan: Float,                      // 用户个人设定的寿命（不受外部覆盖）
    monthlySalary: Float,                             // 月薪
    monthlyPension: Float,                            // 月养老金
    netMonthlyIncome: Float,                          // 月净收入（扣除五险一金和固定支出后）
    monthlyDeposit: Float,                            // 每月存款
    onNavigateToGenderLifespan: () -> Unit,            // 导航到人均预期寿命页
    onNavigateToSettings: () -> Unit,                  // 导航到设置页
    onSelectRetirement: () -> Unit,                    // 切换到退休模式回调
    onSelectPersonal: () -> Unit                       // 切换到个人模式回调
) {
    // 预期寿命倒计时（随personalLifespan变化自动更新）
    var lifeCountdown by remember(birthday, personalLifespan) {
        mutableStateOf(calculateRemaining(birthday, personalLifespan))
    }
    // 退休倒计时（随retirementAge变化自动更新）
    var retirementCountdown by remember(birthday, retirementAge) {
        mutableStateOf(calculateRemaining(birthday, retirementAge))
    }
    var menuExpanded by remember { mutableStateOf(false) }   // 下拉菜单展开状态
    val coroutineScope = rememberCoroutineScope()            // 协程作用域（用于菜单点击后滚动翻页器）

    // 每27ms更新预期寿命倒计时（毫秒级精度）
    LaunchedEffect(birthday, personalLifespan) {
        while (true) {
            lifeCountdown = calculateRemaining(birthday, personalLifespan)
            delay(27) // 约37fps刷新率，兼顾流畅度和性能
        }
    }
    // 每27ms更新退休倒计时
    LaunchedEffect(birthday, retirementAge) {
        while (true) {
            retirementCountdown = calculateRemaining(birthday, retirementAge)
            delay(27)
        }
    }

    // 判断是否有多页面（有薪资/养老金数据时才显示左右翻页）
    val hasLifePages = monthlyPension > 0f          // 有养老金→预期寿命模式有4页
    val hasRetirePages = monthlySalary > 0f         // 有薪资→退休模式有3页
    val hLifePages = if (hasLifePages) 4 else 1     // 预期寿命模式：终生总收入|倒计时|终生总存款|预计养老金
    val hRetirePages = if (hasRetirePages) 3 else 1 // 退休模式：退休总存款|倒计时|退休总收入

    // 垂直翻页器初始页（根据当前模式决定）
    val initialVerticalPage = if (lifespanSource == UserPreferences.SOURCE_RETIREMENT) 1 else 0
    val verticalPagerState = rememberPagerState(initialPage = initialVerticalPage, pageCount = { 2 }) // 2页：预期寿命/退休
    val hLifeState = rememberPagerState(pageCount = { hLifePages })     // 预期寿命模式的水平翻页状态
    val hRetireState = rememberPagerState(pageCount = { hRetirePages }) // 退休模式的水平翻页状态

    // 有多页面时水平翻页器默认居中（第1页=倒计时页）
    LaunchedEffect(hLifePages) { if (hLifePages > 1) hLifeState.scrollToPage(2) } // 4页时居中=页2（倒计时）
    LaunchedEffect(hRetirePages) { if (hRetirePages > 1) hRetireState.scrollToPage(1) }

    // 垂直翻页切换时同步更新模式状态
    LaunchedEffect(verticalPagerState.currentPage) {
        val targetSource = if (verticalPagerState.currentPage == 0)
            UserPreferences.SOURCE_PERSONAL else UserPreferences.SOURCE_RETIREMENT
        if (targetSource != lifespanSource) {                        // 仅在模式真的改变时触发
            if (targetSource == UserPreferences.SOURCE_PERSONAL) onSelectPersonal()
            else onSelectRetirement()
        }
    }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) } // 千分位格式化

    // 当前是否在倒计时中心页（仅中心页允许上下滑动，避免与左右翻页冲突）
    val isOnCountdownPage = if (verticalPagerState.currentPage == 0) {
        hLifePages == 1 || hLifeState.currentPage == 2               // 预期寿命模式：单页或页2（倒计时中心）
    } else {
        hRetirePages == 1 || hRetireState.currentPage == 1           // 退休模式：单页或页1（倒计时中心）
    }

    Box(
        modifier = Modifier
            .fillMaxSize()                           // 充满屏幕
            .background(Color(0xFF0D0D0D))           // 深黑色背景
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VerticalPager(
                state = verticalPagerState,          // 垂直翻页状态
                userScrollEnabled = isOnCountdownPage, // 仅在倒计时页允许上下滑动
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)                      // 占满剩余空间
            ) { vPage ->
                if (vPage == 0) {
                    // ====== 预期寿命模式 ======
                    val hCount = hLifePages
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (hCount > 1) {
                            // 水平页面指示器（与左上角按钮居中对齐）
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()       // 避开状态栏
                                    .height(48.dp),            // 与IconButton同高
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(hCount) { index ->
                                    val isSelected = hLifeState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .clip(CircleShape) // 圆形指示点
                                            .background(
                                                if (isSelected) Color(0xFF4CAF50) // 选中绿色
                                                else Color(0xFF404040)             // 未选中灰色
                                            )
                                            .width(if (isSelected) 16.dp else 8.dp) // 选中拉长
                                            .height(8.dp)
                                    )
                                }
                            }
                        }
                        if (hCount == 1) {
                            // 只有1页：直接显示倒计时（无薪资/养老金数据时）
                            CountdownPage(
                                birthday = birthday,
                                personalLifespan = personalLifespan,
                                lifespanSource = lifespanSource,
                                personalCountdown = lifeCountdown
                            )
                        } else {
                            // 4页水平翻页：预计养老金(0) | 终生总存款(1) | 倒计时(2) | 终生总收入(3)
                            HorizontalPager(
                                state = hLifeState,
                                modifier = Modifier.fillMaxSize()
                            ) { hPage ->
                                when (hPage) {
                                    0 -> PensionPage(                   // 页0：预计养老金（右划2次）
                                        countdown = lifeCountdown,
                                        monthlyPension = monthlyPension,
                                        numberFormat = numberFormat
                                    )
                                    1 -> SavingsPage(                   // 页1：终生总存款（右划1次）
                                        birthday = birthday,
                                        countdown = lifeCountdown,
                                        netMonthlyIncome = netMonthlyIncome,
                                        monthlyDeposit = monthlyDeposit,
                                        monthlyPension = monthlyPension,
                                        retirementAge = retirementAge,
                                        isRetirement = false,
                                        numberFormat = numberFormat
                                    )
                                    2 -> CountdownPage(                 // 页2：倒计时（默认中心页）
                                        birthday = birthday,
                                        personalLifespan = personalLifespan,
                                        lifespanSource = lifespanSource,
                                        personalCountdown = lifeCountdown
                                    )
                                    3 -> TotalLifeIncomePage(           // 页3：终生总收入（左划1次）
                                        birthday = birthday,
                                        countdown = lifeCountdown,
                                        monthlySalary = monthlySalary,
                                        monthlyDeposit = monthlyDeposit,
                                        monthlyPension = monthlyPension,
                                        retirementAge = retirementAge,
                                        numberFormat = numberFormat
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // ====== 退休模式 ======
                    val hCount = hRetirePages
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (hCount > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .height(48.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(hCount) { index ->
                                    val isSelected = hRetireState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color(0xFF4CAF50)
                                                else Color(0xFF404040)
                                            )
                                            .width(if (isSelected) 16.dp else 8.dp)
                                            .height(8.dp)
                                    )
                                }
                            }
                        }
                        if (hCount == 1) {
                            CountdownPage(
                                birthday = birthday,
                                personalLifespan = retirementAge,
                                lifespanSource = UserPreferences.SOURCE_RETIREMENT,
                                personalCountdown = retirementCountdown
                            )
                        } else {
                            // 3页水平翻页：总存款(0) | 倒计时(1) | 退休收入(2)
                            HorizontalPager(
                                state = hRetireState,
                                modifier = Modifier.fillMaxSize()
                            ) { hPage ->
                                when (hPage) {
                                    0 -> SavingsPage(                    // 第0页：退休前总存款
                                        birthday = birthday,
                                        countdown = retirementCountdown,
                                        netMonthlyIncome = netMonthlyIncome,
                                        monthlyDeposit = monthlyDeposit,
                                        monthlyPension = monthlyPension,
                                        retirementAge = retirementAge,
                                        isRetirement = true,
                                        numberFormat = numberFormat
                                    )
                                    1 -> CountdownPage(                 // 第1页：退休倒计时（默认）
                                        birthday = birthday,
                                        personalLifespan = retirementAge,
                                        lifespanSource = UserPreferences.SOURCE_RETIREMENT,
                                        personalCountdown = retirementCountdown
                                    )
                                    2 -> EarningsPage(                  // 第2页：退休总收入
                                        countdown = retirementCountdown,
                                        monthlySalary = monthlySalary,
                                        numberFormat = numberFormat
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 上下滑动提示文字（仅倒计时中心页可见）
            if (isOnCountdownPage) {
            val vPage = verticalPagerState.currentPage
            val vLabel = if (vPage == 0) "↓ 向下滑动切换到退休倒计时" else "↑ 向上滑动切换到预期寿命倒计时"
            Text(
                text = vLabel,
                fontSize = 10.sp,
                color = Color(0xFF505050),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            }
        }

        // 上下翻页指示器（右侧中间竖排圆点，仅倒计时中心页可见）
        if (isOnCountdownPage) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)          // 右侧居中
                .padding(end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(2) { index ->                     // 2个圆点对应2个垂直页
                val isSelected = verticalPagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(vertical = 3.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFF4CAF50) // 当前页绿色
                            else Color(0xFF404040)             // 非当前页灰色
                        )
                        .size(if (isSelected) 8.dp else 6.dp)
                )
            }
        }
        }

        // 左上角菜单按钮
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)           // 左上角定位
                .statusBarsPadding()                 // 避开状态栏
                .padding(start = 8.dp, top = 4.dp)
        ) {
            IconButton(onClick = { menuExpanded = true }) {
                Text(
                    text = "☰",                      // 汉堡菜单字符
                    fontSize = 26.sp,
                    color = Color(0xFFB0B0B0)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,             // 展开状态
                onDismissRequest = { menuExpanded = false } // 点击外部关闭
            ) {
                // 菜单项：你的预期寿命（右侧显示设定值）
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("你的预期寿命")
                            Text(
                                text = "%.0f 岁".format(userPersonalLifespan),
                                fontSize = 13.sp,
                                color = Color(0xFF808080)
                            )
                        }
                    },
                    onClick = {
                        menuExpanded = false
                        onSelectPersonal()                       // 切换到个人模式
                        coroutineScope.launch {
                            verticalPagerState.scrollToPage(0)   // 滚动到预期寿命页
                        }
                    }
                )
                // 菜单项：你的退休年龄（右侧显示计算值）
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("你的退休年龄")
                            Text(
                                text = UserPreferences.formatRetirementAge(retirementAge),
                                fontSize = 13.sp,
                                color = Color(0xFF808080)
                            )
                        }
                    },
                    onClick = {
                        menuExpanded = false
                        onSelectRetirement()                     // 切换到退休模式
                        coroutineScope.launch {
                            verticalPagerState.scrollToPage(1)   // 滚动到退休页
                        }
                    }
                )
                // 菜单项：中国人均预期寿命（含数据来源）
                DropdownMenuItem(
                    text = {
                        Column {
                            Text("中国人均预期寿命")
                            Text(
                                text = "数据来源：国家卫健委（2026年）",
                                fontSize = 11.sp,
                                color = Color(0xFF808080)
                            )
                        }
                    },
                    onClick = {
                        menuExpanded = false
                        onNavigateToGenderLifespan()             // 导航到人均预期寿命详情页
                    }
                )
                // 菜单项：设置
                DropdownMenuItem(
                    text = { Text("设置") },
                    onClick = {
                        menuExpanded = false
                        onNavigateToSettings()                   // 导航到设置页
                    }
                )
            }
        }
    }
}

// 倒计时中心页面：显示标题、寿命值、倒计时数字、进度条、目标日期
@Composable
private fun CountdownPage(
    birthday: LocalDate,                  // 生日
    personalLifespan: Float,              // 寿命值
    lifespanSource: String,               // 寿命来源
    personalCountdown: CountdownData      // 倒计时数据
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally, // 水平居中
        verticalArrangement = Arrangement.Center             // 垂直居中
    ) {
        // 标题（"你的生命倒计时"或"你的退休倒计时"）
        Text(
            text = UserPreferences.lifespanTitle(lifespanSource),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFB0B0B0),
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 寿命说明（"中国人均预期寿命 79.08岁"或"退休年龄 61.42岁"等）
        Text(
            text = "${UserPreferences.lifespanLabel(lifespanSource)} ${
                "%.2f".format(personalLifespan)
            }岁",
            fontSize = 12.sp,
            color = Color(0xFF808080)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 倒计时数字或超时提示
        if (personalCountdown.isOver) {
            Text(
                text = UserPreferences.lifespanOverText(lifespanSource), // "已超出预期寿命"或"已超过退休年龄"
                fontSize = 20.sp,
                color = Color(0xFFE53935), // 红色
                fontWeight = FontWeight.Bold
            )
        } else {
            CountdownDisplay(personalCountdown) // 年:天 时:分:秒.毫秒 显示
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 生命进度条（已走过XX%）
        LifeProgressBar(
            birthday = birthday,
            lifespanYears = personalLifespan,
            color = Color(0xFF4CAF50), // 绿色
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 目标日期（"预计结束: 2090年05月10日"或"退休日期: 2050年03月15日"）
        val targetDate = birthday.atStartOfDay()
            .plusDays((personalLifespan * 365.25).toLong()) // 生日+寿命天数=目标日期
            .toLocalDate()
        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日")
        Text(
            text = "${UserPreferences.targetDateLabel(lifespanSource)}: ${targetDate.format(dateFormatter)}",
            fontSize = 11.sp,
            color = Color(0xFF707070)
        )
    }
}

// 终生总收入页面：退休前总收入 + 养老金总收入
@Composable
private fun TotalLifeIncomePage(
    birthday: LocalDate,           // 生日
    countdown: CountdownData,      // 倒计时数据
    monthlySalary: Float,          // 月薪
    monthlyDeposit: Float,         // 已有存款
    monthlyPension: Float,         // 月养老金
    retirementAge: Float,          // 退休年龄
    numberFormat: NumberFormat     // 数字格式化器
) {
    val totalMonths = countdown.years * 12 + countdown.days / 30            // 剩余总月数
    // 计算退休日期和工作/退休月数
    val retireDate = birthday.atStartOfDay()
        .plusDays((retirementAge * 365.25).toLong())                        // 退休日期
    val now = java.time.LocalDateTime.now()                                 // 当前时间
    val remainingToRetire = maxOf(0L, java.time.Duration.between(now, retireDate).toDays() / 30) // 距退休月数
    val monthsWorking = minOf(remainingToRetire, totalMonths)               // 剩余工作月数
    val monthsRetired = maxOf(0L, totalMonths - monthsWorking)              // 退休后月数
    // 终生总收入 = 月薪×工作月数 + 已有存款 + 月养老金×退休月数
    val totalIncome = monthlySalary.toLong() * monthsWorking + monthlyDeposit.toLong() + monthlyPension.toLong() * monthsRetired

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(text = "终生总收入", fontSize = 14.sp, fontWeight = FontWeight.Normal,
            color = Color(0xFFB0B0B0), letterSpacing = 4.sp)                // 标题
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "月薪 ${numberFormat.format(monthlySalary.toLong())} 元 + 已有存款 ${numberFormat.format(monthlyDeposit.toLong())} 元 + 养老金 ${numberFormat.format(monthlyPension.toLong())} 元",
            fontSize = 12.sp, color = Color(0xFF808080))
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "¥ ${numberFormat.format(totalIncome)}",                 // 总金额（金色）
            fontSize = 30.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace, color = Color(0xFFFFD700))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "工作 ${numberFormat.format(monthsWorking)} 个月 + 退休 ${numberFormat.format(monthsRetired)} 个月",
            fontSize = 12.sp, color = Color(0xFF707070))                     // 月份明细
    }
}

// 退休总收入页面：月薪×剩余月数
@Composable
private fun EarningsPage(
    countdown: CountdownData,     // 倒计时数据
    monthlySalary: Float,         // 月薪
    numberFormat: NumberFormat    // 数字格式化器
) {
    val totalMonths = countdown.years * 12 + countdown.days / 30 // 剩余总月数（约数）
    val totalEarnings = totalMonths * monthlySalary.toLong()      // 总收入 = 月薪 × 月数

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "退休前总收入", fontSize = 14.sp, fontWeight = FontWeight.Normal,
            color = Color(0xFFB0B0B0), letterSpacing = 4.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "月薪 ${numberFormat.format(monthlySalary.toLong())} 元", // 显示月薪
            fontSize = 12.sp, color = Color(0xFF808080))
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "¥ ${numberFormat.format(totalEarnings)}",  // 总金额（金色大字）
            fontSize = 30.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace, color = Color(0xFFFFD700))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "约 ${numberFormat.format(totalMonths)} 个月", // 剩余月数
            fontSize = 13.sp, color = Color(0xFF707070))
    }
}

// 预计养老金总额页面：月养老金×剩余月数
@Composable
private fun PensionPage(
    countdown: CountdownData,     // 倒计时数据
    monthlyPension: Float,        // 月养老金
    numberFormat: NumberFormat    // 数字格式化器
) {
    val totalMonths = countdown.years * 12 + countdown.days / 30 // 剩余总月数
    val totalPension = totalMonths * monthlyPension.toLong()      // 总养老金 = 月养老金 × 月数

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "预计养老金总额", fontSize = 14.sp, fontWeight = FontWeight.Normal,
            color = Color(0xFFB0B0B0), letterSpacing = 4.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "月养老金 ${numberFormat.format(monthlyPension.toLong())} 元",
            fontSize = 12.sp, color = Color(0xFF808080))
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "¥ ${numberFormat.format(totalPension)}",
            fontSize = 30.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace, color = Color(0xFFFFD700))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "约 ${numberFormat.format(totalMonths)} 个月",
            fontSize = 13.sp, color = Color(0xFF707070))
    }
}

// 总存款页面：已有存款 + 净收入×工作月数（+养老金×退休月数）
@Composable
private fun SavingsPage(
    birthday: LocalDate,           // 生日
    countdown: CountdownData,      // 倒计时数据
    netMonthlyIncome: Float,       // 月净收入
    monthlyDeposit: Float,         // 已有存款
    monthlyPension: Float,         // 月养老金
    retirementAge: Float,          // 退休年龄
    isRetirement: Boolean,         // 当前是否为退休模式
    numberFormat: NumberFormat     // 数字格式化器
) {
    val totalMonths = countdown.years * 12 + countdown.days / 30 // 剩余总月数

    val (totalSavings, workMonths, retireMonths) = if (isRetirement) {
        // 退休前总存款 = 月净收入×工作月数 + 已有存款
        Triple(netMonthlyIncome.toLong() * totalMonths + monthlyDeposit.toLong(), totalMonths, 0L)
    } else {
        // 预期寿命模式：拆分工作期和退休期
        val retireDate = birthday.atStartOfDay()
            .plusDays((retirementAge * 365.25).toLong())  // 退休日期
        val now = LocalDateTime.now()
        val remainingToRetire = maxOf(0L, Duration.between(now, retireDate).toDays() / 30) // 距退休月数
        val monthsWorking = minOf(remainingToRetire, totalMonths)  // 工作月数
        val monthsRetired = maxOf(0L, totalMonths - monthsWorking) // 退休月数
        Triple(
            netMonthlyIncome.toLong() * monthsWorking + monthlyDeposit.toLong() + monthlyPension.toLong() * monthsRetired, // 终生总存款=净收入×工作月数+已有存款+养老金×退休月数
            monthsWorking,   // 工作月数
            monthsRetired    // 退休月数
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (isRetirement) "退休前总存款" else "终生总存款",
            fontSize = 14.sp, fontWeight = FontWeight.Normal,
            color = Color(0xFFB0B0B0), letterSpacing = 4.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isRetirement) "月净收入 ${numberFormat.format(netMonthlyIncome.toLong())} 元 + 已有存款 ${numberFormat.format(monthlyDeposit.toLong())} 元"
                   else "月净收入 ${numberFormat.format(netMonthlyIncome.toLong())} 元 + 已有存款 ${numberFormat.format(monthlyDeposit.toLong())} 元 + 月养老金 ${numberFormat.format(monthlyPension.toLong())} 元",
            fontSize = 12.sp, color = Color(0xFF808080))
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "¥ ${numberFormat.format(totalSavings)}",
            fontSize = 30.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace, color = Color(0xFFFFD700))
        Spacer(modifier = Modifier.height(12.dp))
        if (isRetirement) {
            Text(text = "约 ${numberFormat.format(totalMonths)} 个月",
                fontSize = 13.sp, color = Color(0xFF707070))
        } else {
            Text(text = "工作 ${numberFormat.format(workMonths)} 个月 + 退休 ${numberFormat.format(retireMonths)} 个月",
                fontSize = 12.sp, color = Color(0xFF707070))
        }
    }
}

// 倒计时数字显示器：年 天 + 时:分:秒.毫秒
@Composable
private fun CountdownDisplay(data: CountdownData) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
            TimeUnit(value = data.years, unit = "年") // "X 年"
            Spacer(modifier = Modifier.width(12.dp))
            TimeUnit(value = data.days, unit = "天")  // "Y 天"
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
            TimeDigit(value = data.hours, pad = true)   // "HH"（补零）
            Colon()                                      // ":"
            TimeDigit(value = data.minutes, pad = true)  // "MM"
            Colon()
            TimeDigit(value = data.seconds, pad = true)  // "SS"
            Text(text = ".", fontSize = 28.sp, fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Monospace, color = Color.White,
                modifier = Modifier.padding(bottom = 2.dp))
            MillisDigit(value = data.millis)             // "mmm"（3位毫秒）
        }
    }
}

// 时间单位显示组件（数字+单位）
@Composable
private fun TimeUnit(value: Long, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), fontSize = 36.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace, color = Color.White) // 大号等宽白色数字
        Text(text = unit, fontSize = 12.sp, color = Color(0xFF808080)) // 灰色单位
    }
}

// 时间数字显示组件（可选补零）
@Composable
private fun TimeDigit(value: Long, pad: Boolean) {
    val text = if (pad) value.toString().padStart(2, '0') else value.toString() // 补零到2位
    Text(text = text, fontSize = 36.sp, fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace, color = Color.White)
}

// 冒号分隔符
@Composable
private fun Colon() {
    Text(text = ":", fontSize = 28.sp, fontWeight = FontWeight.Light,
        fontFamily = FontFamily.Monospace, color = Color(0xFF808080),
        modifier = Modifier.padding(bottom = 2.dp))
}

// 毫秒显示组件（3位补零）
@Composable
private fun MillisDigit(value: Long) {
    Text(text = value.toString().padStart(3, '0'), fontSize = 20.sp,
        fontWeight = FontWeight.Light, fontFamily = FontFamily.Monospace,
        color = Color(0xFFAAAAAA), modifier = Modifier.padding(bottom = 2.dp))
}

// 生命进度条组件：显示已走过百分比 + 圆角进度条Canvas
@Composable
private fun LifeProgressBar(
    birthday: LocalDate,       // 生日
    lifespanYears: Float,      // 寿命年数
    color: Color,              // 进度条前景色
    modifier: Modifier = Modifier
) {
    val now = LocalDate.now()
    val totalLifespanDays = (lifespanYears * 365.25).toLong()            // 寿命总天数
    val elapsedDays = Duration.between(                                 // 已过去天数
        birthday.atStartOfDay(), now.atStartOfDay()
    ).toDays()
    val progress = if (totalLifespanDays > 0) {
        (elapsedDays.toFloat() / totalLifespanDays.toFloat()).coerceIn(0f, 1f)  // 进度比例（0-1）
    } else 0f
    val percentText = "%.1f%%".format(progress * 100)                    // 百分比文本

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = "已走过 $percentText", fontSize = 10.sp, color = Color(0xFF606060))
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {       // 4dp高进度条
            drawRoundRect(                                               // 背景（深灰）
                color = Color(0xFF333333), size = size,
                cornerRadius = CornerRadius(2f, 2f))
            if (progress > 0f) {
                drawRoundRect(                                           // 前景（绿色）
                    color = color,
                    size = Size(size.width * progress, size.height),
                    cornerRadius = CornerRadius(2f, 2f))
            }
        }
    }
}
