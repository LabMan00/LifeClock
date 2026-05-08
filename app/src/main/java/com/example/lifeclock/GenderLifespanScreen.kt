package com.example.lifeclock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderLifespanScreen(
    gender: String,
    onBack: () -> Unit,
    onSelectLifespan: (Double, String) -> Unit
) {
    val isFemale = gender == UserPreferences.GENDER_FEMALE
    val isMale = gender == UserPreferences.GENDER_MALE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("中国人均预期寿命") },
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
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "中国人均预期寿命",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "点击卡片可将该预期寿命设为你的倒计时",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "数据来源：国家卫健委（2026年）",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Overall card
            LifespanCard(
                label = "人均",
                lifespan = UserPreferences.CHINA_OVERALL_LIFESPAN,
                color = Color(0xFF9C27B0),
                highlighted = true,
                onClick = { onSelectLifespan(UserPreferences.CHINA_OVERALL_LIFESPAN, UserPreferences.SOURCE_OVERALL) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Male card
            LifespanCard(
                label = "男性",
                lifespan = UserPreferences.CHINA_MALE_LIFESPAN,
                color = Color(0xFF2196F3),
                highlighted = isMale,
                onClick = { onSelectLifespan(UserPreferences.CHINA_MALE_LIFESPAN, UserPreferences.SOURCE_MALE) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Female card
            LifespanCard(
                label = "女性",
                lifespan = UserPreferences.CHINA_FEMALE_LIFESPAN,
                color = Color(0xFFE91E63),
                highlighted = isFemale,
                onClick = { onSelectLifespan(UserPreferences.CHINA_FEMALE_LIFESPAN, UserPreferences.SOURCE_FEMALE) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LifespanCard(
    label: String,
    lifespan: Double,
    color: Color,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (highlighted) color else Color.Transparent
    val bgColor = if (highlighted) color.copy(alpha = 0.1f) else Color(0xFF1A1A1A)
    val borderMod = if (highlighted) Modifier.border(BorderStroke(2.dp, borderColor), RoundedCornerShape(16.dp)) else Modifier

    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor, RoundedCornerShape(16.dp))
            .then(borderMod)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal,
                color = if (highlighted) color else Color(0xFFCCCCCC),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "%.2f".format(lifespan),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlighted) color else Color.White
            )
            Text(
                text = "岁",
                fontSize = 16.sp,
                color = if (highlighted) color else Color(0xFF808080),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "约 ${UserPreferences.formatLifespan(lifespan)}",
            fontSize = 13.sp,
            color = if (highlighted) color.copy(alpha = 0.8f) else Color(0xFF808080)
        )
    }
}
