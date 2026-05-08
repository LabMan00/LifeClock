package com.example.lifeclock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionPickerScreen(
    onBack: () -> Unit,
    onRegionSelected: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val currentRegion = UserPreferences.getRegion(context)
    val regions = RegionData.regions

    var selectedCountry by remember {
        mutableStateOf(regions.indexOfFirst { it.name == currentRegion.first }.coerceAtLeast(0))
    }
    var selectedProvince by remember {
        val children = regions[selectedCountry].children
        mutableStateOf(children.indexOfFirst { it.name == currentRegion.second }.coerceAtLeast(0))
    }
    var selectedCity by remember {
        val children = regions[selectedCountry].children[selectedProvince].children
        mutableStateOf(children.indexOfFirst { it.name == currentRegion.third }.coerceAtLeast(0))
    }

    val isComingSoon = regions[selectedCountry].name == "敬请期待"
    val countries = regions.map { it.name }
    val provinces = regions[selectedCountry].children.map { it.name }
    val cities = regions[selectedCountry].children[selectedProvince].children.map { it.name }

    val countryState = rememberLazyListState()
    val provinceState = rememberLazyListState()
    val cityState = rememberLazyListState()

    LaunchedEffect(Unit) {
        countryState.scrollToItem(selectedCountry)
        provinceState.scrollToItem(selectedProvince)
        cityState.scrollToItem(selectedCity)
    }

    fun saveAndBack() {
        val country = regions[selectedCountry].name
        val province = if (!isComingSoon) regions[selectedCountry].children[selectedProvince].name else "敬请期待"
        val city = if (!isComingSoon) regions[selectedCountry].children[selectedProvince].children[selectedCity].name else "敬请期待"
        UserPreferences.updateRegion(context, country, province, city)
        onRegionSelected(country, province, city)
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("你的地区") },
                navigationIcon = {
                    IconButton(onClick = { saveAndBack() }) {
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
        ) {
            // Selected region display
            Text(
                text = "${regions[selectedCountry].name} ${provinces[selectedProvince]} ${cities[selectedCity]}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                textAlign = TextAlign.Center
            )

            HorizontalDivider()

            // Three-column wheel picker
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
            ) {
                // Country column
                WheelColumn(
                    items = countries,
                    state = countryState,
                    selectedIndex = selectedCountry,
                    isGrayed = { false },
                    onSelect = { index ->
                        selectedCountry = index
                        selectedProvince = 0
                        selectedCity = 0
                    },
                    modifier = Modifier.weight(1f)
                )

                // Province column
                WheelColumn(
                    items = provinces,
                    state = provinceState,
                    selectedIndex = selectedProvince,
                    isGrayed = { isComingSoon },
                    onSelect = { index ->
                        selectedProvince = index
                        selectedCity = 0
                    },
                    modifier = Modifier.weight(1f)
                )

                // City column
                WheelColumn(
                    items = cities,
                    state = cityState,
                    selectedIndex = selectedCity,
                    isGrayed = { isComingSoon },
                    onSelect = { index -> selectedCity = index },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WheelColumn(
    items: List<String>,
    state: androidx.compose.foundation.lazy.LazyListState,
    selectedIndex: Int,
    isGrayed: () -> Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val grayed = isGrayed()
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
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    fontSize = if (isSelected) 17.sp else 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (grayed) Color(0xFFB0B0B0)
                    else if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
