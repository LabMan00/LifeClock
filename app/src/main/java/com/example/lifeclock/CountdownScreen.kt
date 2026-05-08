package com.example.lifeclock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

data class CountdownData(
    val years: Long,
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val millis: Long,
    val isOver: Boolean
)

fun calculateRemaining(birthday: LocalDate, lifespanYears: Float): CountdownData {
    val totalLifespanDays = (lifespanYears * 365.25).toLong()
    val deathDate = birthday.atStartOfDay().plusDays(totalLifespanDays)
    val now = LocalDateTime.now()
    val remaining = Duration.between(now, deathDate)

    if (remaining.isNegative || remaining.isZero) {
        return CountdownData(0, 0, 0, 0, 0, 0, true)
    }

    val totalMs = remaining.toMillis()
    val totalSeconds = totalMs / 1000
    val totalMinutes = totalSeconds / 60
    val totalHours = totalMinutes / 60
    val totalDays = totalHours / 24

    val years = totalDays / 365
    val days = totalDays % 365
    val hours = totalHours % 24
    val minutes = totalMinutes % 60
    val seconds = totalSeconds % 60
    val millis = totalMs % 1000

    return CountdownData(years, days, hours, minutes, seconds, millis, false)
}

@Composable
fun CountdownScreen(
    birthday: LocalDate,
    personalLifespan: Float,
    lifespanSource: String,
    retirementAge: Float,
    userPersonalLifespan: Float,
    monthlySalary: Float,
    monthlyPension: Float,
    netMonthlyIncome: Float,
    onNavigateToGenderLifespan: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSelectRetirement: () -> Unit,
    onSelectPersonal: () -> Unit
) {
    var lifeCountdown by remember(birthday, personalLifespan) {
        mutableStateOf(calculateRemaining(birthday, personalLifespan))
    }
    var retirementCountdown by remember(birthday, retirementAge) {
        mutableStateOf(calculateRemaining(birthday, retirementAge))
    }
    var menuExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(birthday, personalLifespan) {
        while (true) {
            lifeCountdown = calculateRemaining(birthday, personalLifespan)
            delay(27)
        }
    }
    LaunchedEffect(birthday, retirementAge) {
        while (true) {
            retirementCountdown = calculateRemaining(birthday, retirementAge)
            delay(27)
        }
    }

    val hasLifePages = monthlyPension > 0f
    val hasRetirePages = monthlySalary > 0f
    val hLifePages = if (hasLifePages) 3 else 1
    val hRetirePages = if (hasRetirePages) 3 else 1

    val initialVerticalPage = if (lifespanSource == UserPreferences.SOURCE_RETIREMENT) 1 else 0
    val verticalPagerState = rememberPagerState(initialPage = initialVerticalPage, pageCount = { 2 })
    val hLifeState = rememberPagerState(pageCount = { hLifePages })
    val hRetireState = rememberPagerState(pageCount = { hRetirePages })

    LaunchedEffect(hLifePages) { if (hLifePages > 1) hLifeState.scrollToPage(1) }
    LaunchedEffect(hRetirePages) { if (hRetirePages > 1) hRetireState.scrollToPage(1) }

    LaunchedEffect(verticalPagerState.currentPage) {
        val targetSource = if (verticalPagerState.currentPage == 0)
            UserPreferences.SOURCE_PERSONAL else UserPreferences.SOURCE_RETIREMENT
        if (targetSource != lifespanSource) {
            if (targetSource == UserPreferences.SOURCE_PERSONAL) onSelectPersonal()
            else onSelectRetirement()
        }
    }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VerticalPager(
                state = verticalPagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { vPage ->
                if (vPage == 0) {
                    // Life expectancy mode
                    val hCount = hLifePages
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
                                    val isSelected = hLifeState.currentPage == index
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
                        HorizontalPager(
                            state = hLifeState,
                            modifier = Modifier.fillMaxSize()
                        ) { hPage ->
                            when (hPage) {
                                0 -> SavingsPage(
                                    birthday = birthday,
                                    countdown = lifeCountdown,
                                    netMonthlyIncome = netMonthlyIncome,
                                    monthlyPension = monthlyPension,
                                    retirementAge = retirementAge,
                                    isRetirement = false,
                                    numberFormat = numberFormat
                                )
                                1 -> CountdownPage(
                                    birthday = birthday,
                                    personalLifespan = personalLifespan,
                                    lifespanSource = lifespanSource,
                                    personalCountdown = lifeCountdown
                                )
                                2 -> PensionPage(
                                    countdown = lifeCountdown,
                                    monthlyPension = monthlyPension,
                                    numberFormat = numberFormat
                                )
                            }
                        }
                    }
                } else {
                    // Retirement mode
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
                        HorizontalPager(
                            state = hRetireState,
                            modifier = Modifier.fillMaxSize()
                        ) { hPage ->
                            when (hPage) {
                                0 -> SavingsPage(
                                    birthday = birthday,
                                    countdown = retirementCountdown,
                                    netMonthlyIncome = netMonthlyIncome,
                                    monthlyPension = monthlyPension,
                                    retirementAge = retirementAge,
                                    isRetirement = true,
                                    numberFormat = numberFormat
                                )
                                1 -> CountdownPage(
                                    birthday = birthday,
                                    personalLifespan = retirementAge,
                                    lifespanSource = UserPreferences.SOURCE_RETIREMENT,
                                    personalCountdown = retirementCountdown
                                )
                                2 -> EarningsPage(
                                    countdown = retirementCountdown,
                                    monthlySalary = monthlySalary,
                                    numberFormat = numberFormat
                                )
                            }
                        }
                    }
                }
            }

            // Swipe hints
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

        // Vertical page indicator (center-right)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(2) { index ->
                val isSelected = verticalPagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(vertical = 3.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFF4CAF50)
                            else Color(0xFF404040)
                        )
                        .size(if (isSelected) 8.dp else 6.dp)
                )
            }
        }

        // Menu button — top layer
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 8.dp, top = 4.dp)
        ) {
            IconButton(onClick = { menuExpanded = true }) {
                Text(
                    text = "☰",
                    fontSize = 26.sp,
                    color = Color(0xFFB0B0B0)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
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
                        onSelectPersonal()
                        coroutineScope.launch {
                            verticalPagerState.scrollToPage(0)
                        }
                    }
                )
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
                        onSelectRetirement()
                        coroutineScope.launch {
                            verticalPagerState.scrollToPage(1)
                        }
                    }
                )
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
                        onNavigateToGenderLifespan()
                    }
                )
                DropdownMenuItem(
                    text = { Text("设置") },
                    onClick = {
                        menuExpanded = false
                        onNavigateToSettings()
                    }
                )
            }
        }
    }
}

@Composable
private fun CountdownPage(
    birthday: LocalDate,
    personalLifespan: Float,
    lifespanSource: String,
    personalCountdown: CountdownData
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = UserPreferences.lifespanTitle(lifespanSource),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFB0B0B0),
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${UserPreferences.lifespanLabel(lifespanSource)} ${
                "%.2f".format(personalLifespan)
            }岁",
            fontSize = 12.sp,
            color = Color(0xFF808080)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (personalCountdown.isOver) {
            Text(
                text = UserPreferences.lifespanOverText(lifespanSource),
                fontSize = 20.sp,
                color = Color(0xFFE53935),
                fontWeight = FontWeight.Bold
            )
        } else {
            CountdownDisplay(personalCountdown)
        }

        Spacer(modifier = Modifier.height(24.dp))

        LifeProgressBar(
            birthday = birthday,
            lifespanYears = personalLifespan,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val targetDate = birthday.atStartOfDay()
            .plusDays((personalLifespan * 365.25).toLong())
            .toLocalDate()
        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日")
        Text(
            text = "${UserPreferences.targetDateLabel(lifespanSource)}: ${targetDate.format(dateFormatter)}",
            fontSize = 11.sp,
            color = Color(0xFF707070)
        )
    }
}

@Composable
private fun EarningsPage(
    countdown: CountdownData,
    monthlySalary: Float,
    numberFormat: NumberFormat
) {
    val totalMonths = countdown.years * 12 + countdown.days / 30
    val totalEarnings = totalMonths * monthlySalary.toLong()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "退休前总收入",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFB0B0B0),
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "月薪 ${numberFormat.format(monthlySalary.toLong())} 元",
            fontSize = 12.sp,
            color = Color(0xFF808080)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "¥ ${numberFormat.format(totalEarnings)}",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "约 ${numberFormat.format(totalMonths)} 个月",
            fontSize = 13.sp,
            color = Color(0xFF707070)
        )
    }
}

@Composable
private fun PensionPage(
    countdown: CountdownData,
    monthlyPension: Float,
    numberFormat: NumberFormat
) {
    val totalMonths = countdown.years * 12 + countdown.days / 30
    val totalPension = totalMonths * monthlyPension.toLong()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "预计养老金总额",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFB0B0B0),
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "月养老金 ${numberFormat.format(monthlyPension.toLong())} 元",
            fontSize = 12.sp,
            color = Color(0xFF808080)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "¥ ${numberFormat.format(totalPension)}",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "约 ${numberFormat.format(totalMonths)} 个月",
            fontSize = 13.sp,
            color = Color(0xFF707070)
        )
    }
}

@Composable
private fun SavingsPage(
    birthday: LocalDate,
    countdown: CountdownData,
    netMonthlyIncome: Float,
    monthlyPension: Float,
    retirementAge: Float,
    isRetirement: Boolean,
    numberFormat: NumberFormat
) {
    val totalMonths = countdown.years * 12 + countdown.days / 30

    val (totalSavings, workMonths, retireMonths) = if (isRetirement) {
        Triple(
            netMonthlyIncome.toLong() * totalMonths,
            totalMonths,
            0L
        )
    } else {
        val retireDate = birthday.atStartOfDay()
            .plusDays((retirementAge * 365.25).toLong())
        val now = LocalDateTime.now()
        val remainingToRetire = maxOf(0L, java.time.Duration.between(now, retireDate).toDays() / 30)
        val monthsWorking = minOf(remainingToRetire, totalMonths)
        val monthsRetired = maxOf(0L, totalMonths - monthsWorking)
        Triple(
            netMonthlyIncome.toLong() * monthsWorking + monthlyPension.toLong() * monthsRetired,
            monthsWorking,
            monthsRetired
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isRetirement) "退休总存款" else "终身总存款",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFB0B0B0),
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "月净收入 ${numberFormat.format(netMonthlyIncome.toLong())} 元",
            fontSize = 12.sp,
            color = Color(0xFF808080)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "¥ ${numberFormat.format(totalSavings)}",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isRetirement) {
            Text(
                text = "约 ${numberFormat.format(totalMonths)} 个月",
                fontSize = 13.sp,
                color = Color(0xFF707070)
            )
        } else {
            Text(
                text = "工作 ${numberFormat.format(workMonths)} 个月 + 退休 ${numberFormat.format(retireMonths)} 个月",
                fontSize = 12.sp,
                color = Color(0xFF707070)
            )
        }
    }
}

@Composable
private fun CountdownDisplay(data: CountdownData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            TimeUnit(value = data.years, unit = "年")
            Spacer(modifier = Modifier.width(12.dp))
            TimeUnit(value = data.days, unit = "天")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            TimeDigit(value = data.hours, pad = true)
            Colon()
            TimeDigit(value = data.minutes, pad = true)
            Colon()
            TimeDigit(value = data.seconds, pad = true)
            Text(
                text = ".",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Monospace,
                color = Color.White,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            MillisDigit(value = data.millis)
        }
    }
}

@Composable
private fun TimeUnit(value: Long, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color.White
        )
        Text(
            text = unit,
            fontSize = 12.sp,
            color = Color(0xFF808080)
        )
    }
}

@Composable
private fun TimeDigit(value: Long, pad: Boolean) {
    val text = if (pad) value.toString().padStart(2, '0') else value.toString()
    Text(
        text = text,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = Color.White
    )
}

@Composable
private fun Colon() {
    Text(
        text = ":",
        fontSize = 28.sp,
        fontWeight = FontWeight.Light,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF808080),
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun MillisDigit(value: Long) {
    Text(
        text = value.toString().padStart(3, '0'),
        fontSize = 20.sp,
        fontWeight = FontWeight.Light,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFAAAAAA),
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun LifeProgressBar(
    birthday: LocalDate,
    lifespanYears: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val now = LocalDate.now()
    val totalLifespanDays = (lifespanYears * 365.25).toLong()
    val totalDays = totalLifespanDays
    val elapsedDays = Duration.between(
        birthday.atStartOfDay(),
        now.atStartOfDay()
    ).toDays()
    val progress = if (totalDays > 0) {
        (elapsedDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val percentText = "%.1f%%".format(progress * 100)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "已走过 $percentText",
            fontSize = 10.sp,
            color = Color(0xFF606060)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        ) {
            drawRoundRect(
                color = Color(0xFF333333),
                size = size,
                cornerRadius = CornerRadius(2f, 2f)
            )
            if (progress > 0f) {
                drawRoundRect(
                    color = color,
                    size = Size(size.width * progress, size.height),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
        }
    }
}
