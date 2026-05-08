package com.example.lifeclock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    birthday: LocalDate,
    gender: String,
    personalLifespan: Float,
    onBack: () -> Unit,
    onBirthdayChanged: (LocalDate) -> Unit,
    onGenderChanged: (String) -> Unit,
    onLifespanChanged: (Float) -> Unit,
    onNavigateToIncome: () -> Unit,
    onNavigateToRegion: () -> Unit
) {
    var showLifespanDialog by remember { mutableStateOf(false) }
    var showBirthdayDialog by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    if (showLifespanDialog) {
        LifespanPickerDialog(
            currentLifespan = personalLifespan,
            onDismiss = { showLifespanDialog = false },
            onConfirm = { value ->
                onLifespanChanged(value)
                showLifespanDialog = false
            }
        )
    }

    if (showBirthdayDialog) {
        BirthdayPickerDialog(
            currentBirthday = birthday,
            onDismiss = { showBirthdayDialog = false },
            onConfirm = { newDate ->
                onBirthdayChanged(newDate)
                showBirthdayDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 8.dp)
        ) {
            // Lifespan row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLifespanDialog = true }
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "你的预期寿命",
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "%.0f 岁".format(personalLifespan),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

            // Birthday row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBirthdayDialog = true }
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "你的生日",
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = birthday.format(dateFormatter),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

            // Gender row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "你的性别",
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (gender == UserPreferences.GENDER_MALE) "男" else "女",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = gender == UserPreferences.GENDER_FEMALE,
                    onCheckedChange = { isFemale ->
                        onGenderChanged(
                            if (isFemale) UserPreferences.GENDER_FEMALE
                            else UserPreferences.GENDER_MALE
                        )
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

            // Income row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToIncome() }
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "你的收入",
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "薪资 / 五险一金",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

            // Region row
            val region = UserPreferences.getRegion(LocalContext.current)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRegion() }
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "你的地区",
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${region.first} ${region.second} ${region.third}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}

@Composable
private fun LifespanPickerDialog(
    currentLifespan: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var selected by remember { mutableStateOf(currentLifespan.toInt().coerceIn(1, 120)) }
    val listState = rememberLazyListState()
    val items = (1..120).toList()

    LaunchedEffect(Unit) {
        listState.scrollToItem(selected - 1)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择预期寿命") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    itemsIndexed(items) { index, value ->
                        val isSelected = index == selected - 1
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = value }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$value 岁",
                                fontSize = if (isSelected) 24.sp else 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.toFloat()) }) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun BirthdayPickerDialog(
    currentBirthday: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    var selectedYear by remember { mutableStateOf(currentBirthday.year) }
    var selectedMonth by remember { mutableStateOf(currentBirthday.monthValue) }
    var selectedDay by remember { mutableStateOf(currentBirthday.dayOfMonth) }

    val years = (1930..LocalDate.now().year).toList().reversed()
    val months = (1..12).toList()
    val daysInMonth = when (selectedMonth) {
        2 -> if (java.time.Year.isLeap(selectedYear.toLong())) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    val days = (1..daysInMonth).toList()

    val yearState = rememberLazyListState()
    val monthState = rememberLazyListState()
    val dayState = rememberLazyListState()

    LaunchedEffect(Unit) {
        yearState.scrollToItem(years.indexOf(selectedYear))
        monthState.scrollToItem(selectedMonth - 1)
        dayState.scrollToItem(selectedDay - 1)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择生日") },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                // Year column
                WheelColumn(
                    items = years.map { "${it}年" },
                    state = yearState,
                    selectedIndex = years.indexOf(selectedYear),
                    onSelect = { index -> selectedYear = years[index] },
                    modifier = Modifier.weight(1f)
                )
                // Month column
                WheelColumn(
                    items = months.map { "${it}月" },
                    state = monthState,
                    selectedIndex = selectedMonth - 1,
                    onSelect = { index -> selectedMonth = months[index] },
                    modifier = Modifier.weight(1f)
                )
                // Day column
                WheelColumn(
                    items = days.map { "${it}日" },
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
                    LocalDate.of(selectedYear, selectedMonth, selectedDay)
                } catch (e: Exception) {
                    null
                }
                if (newDate != null) onConfirm(newDate)
            }) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun WheelColumn(
    items: List<String>,
    state: androidx.compose.foundation.lazy.LazyListState,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = state,
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(items) { index, item ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(index) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    fontSize = if (isSelected) 18.sp else 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
