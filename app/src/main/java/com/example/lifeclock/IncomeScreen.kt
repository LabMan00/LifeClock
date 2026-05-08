package com.example.lifeclock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class IncomeItem(
    val key: String,
    val label: String,
    val value: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(
    onBack: () -> Unit,
    onSalaryChanged: (Float) -> Unit
) {
    val context = LocalContext.current
    var salary by remember { mutableStateOf(UserPreferences.getSalary(context)) }
    var editingKey by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }

    val items = listOf(
        IncomeItem(UserPreferences.KEY_SALARY, "你的月薪", salary),
        IncomeItem(UserPreferences.KEY_PENSION, "养老保险", UserPreferences.getIncomeValue(context, UserPreferences.KEY_PENSION, salary)),
        IncomeItem(UserPreferences.KEY_MEDICAL, "医疗保险", UserPreferences.getIncomeValue(context, UserPreferences.KEY_MEDICAL, salary)),
        IncomeItem(UserPreferences.KEY_UNEMPLOYMENT, "失业保险", UserPreferences.getIncomeValue(context, UserPreferences.KEY_UNEMPLOYMENT, salary)),
        IncomeItem(UserPreferences.KEY_WORK_INJURY, "工伤保险", UserPreferences.getIncomeValue(context, UserPreferences.KEY_WORK_INJURY, salary)),
        IncomeItem(UserPreferences.KEY_MATERNITY, "生育保险", UserPreferences.getIncomeValue(context, UserPreferences.KEY_MATERNITY, salary)),
        IncomeItem(UserPreferences.KEY_HOUSING_FUND, "住房公积金", UserPreferences.getIncomeValue(context, UserPreferences.KEY_HOUSING_FUND, salary))
    )

    val editingItem = items.find { it.key == editingKey }

    if (editingItem != null) {
        val isSalary = editingKey == UserPreferences.KEY_SALARY
        AlertDialog(
            onDismissRequest = { editingKey = null },
            title = { Text(editingItem.label) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                editText = newValue
                            }
                        },
                        label = { Text("金额 (元)") },
                        singleLine = true
                    )
                    if (isSalary) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "修改薪资后，五险一金将按默认费率重新计算",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = editText.toFloatOrNull()
                    if (value != null && value >= 0) {
                        if (isSalary) {
                            salary = value
                            UserPreferences.updateSalary(context, value)
                            // Clear insurance values so they recalculate from new salary
                            listOf(
                                UserPreferences.KEY_PENSION,
                                UserPreferences.KEY_MEDICAL,
                                UserPreferences.KEY_UNEMPLOYMENT,
                                UserPreferences.KEY_WORK_INJURY,
                                UserPreferences.KEY_MATERNITY,
                                UserPreferences.KEY_HOUSING_FUND
                            ).forEach { key ->
                                UserPreferences.updateIncomeItem(context, key, -1f)
                            }
                            onSalaryChanged(value)
                        } else {
                            UserPreferences.updateIncomeItem(context, editingKey!!, value)
                        }
                        editingKey = null
                    }
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingKey = null }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("你的收入") },
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
                .verticalScroll(rememberScrollState())
        ) {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            editText = if (item.value > 0f) "%.2f".format(item.value) else ""
                            editingKey = item.key
                        }
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.label,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (item.value > 0f) "%.2f".format(item.value) else "未设置",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " 元",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
            }
        }
    }
}
