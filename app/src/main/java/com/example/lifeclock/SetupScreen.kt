package com.example.lifeclock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    var birthday by remember { mutableStateOf<LocalDate?>(null) }
    var lifespanText by remember { mutableStateOf("80") }
    var gender by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val datePickerState = rememberDatePickerState()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        birthday = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "生命时钟",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Life Clock",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (birthday != null) {
                    "出生日期: ${birthday!!.format(dateFormatter)}"
                } else {
                    "选择出生日期"
                },
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Gender selection
        Text(
            text = "选择性别",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GenderChip(
                label = "♂ 男",
                selected = gender == UserPreferences.GENDER_MALE,
                color = Color(0xFF2196F3),
                onClick = {
                    gender = UserPreferences.GENDER_MALE
                    lifespanText = UserPreferences.CHINA_MALE_LIFESPAN.toInt().toString()
                    errorMessage = null
                }
            )
            Spacer(modifier = Modifier.width(24.dp))
            GenderChip(
                label = "♀ 女",
                selected = gender == UserPreferences.GENDER_FEMALE,
                color = Color(0xFFE91E63),
                onClick = {
                    gender = UserPreferences.GENDER_FEMALE
                    lifespanText = UserPreferences.CHINA_FEMALE_LIFESPAN.toInt().toString()
                    errorMessage = null
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = lifespanText,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    lifespanText = newValue
                    errorMessage = null
                }
            },
            label = { Text("预期寿命 (岁)") },
            modifier = Modifier.fillMaxWidth(0.8f),
            singleLine = true,
            supportingText = if (errorMessage != null) {
                { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
            } else null,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                val lifespan = lifespanText.toIntOrNull()
                when {
                    birthday == null -> errorMessage = "请选择出生日期"
                    gender == null -> errorMessage = "请选择性别"
                    lifespan == null || lifespan <= 0 -> errorMessage = "请输入有效的预期寿命"
                    lifespan > 150 -> errorMessage = "预期寿命不能超过150岁"
                    else -> {
                        UserPreferences.saveUserData(
                            context,
                            birthday.toString(),
                            lifespan,
                            gender!!
                        )
                        onSetupComplete()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "开始", fontSize = 18.sp)
        }
    }
}

@Composable
private fun GenderChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val borderColor = if (selected) color else Color.Gray
    val bgColor = if (selected) color.copy(alpha = 0.15f) else Color.Transparent

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(24.dp))
            .background(bgColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) color else Color.Gray
        )
    }
}
