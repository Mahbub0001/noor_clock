package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.NoorViewModel
import com.example.util.PrayerTimeCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val currentCity by viewModel.currentCity.collectAsState()
    val rawLat by viewModel.latitude.collectAsState()
    val rawLon by viewModel.longitude.collectAsState()
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val adhanAlert by viewModel.adhanAlertHandler.collectAsState()
    val activeMethod by viewModel.prayerMethod.collectAsState()

    var showPresetMenu by remember { mutableStateOf(false) }

    // Manual coordinate inputs
    var manualLatStr by remember { mutableStateOf(String.format("%.4f", rawLat)) }
    var manualLonStr by remember { mutableStateOf(String.format("%.4f", rawLon)) }
    var manualCityStr by remember { mutableStateOf(currentCity) }

    // Synchronize input strings when global coordinates transition
    LaunchedEffect(rawLat, rawLon, currentCity) {
        manualLatStr = String.format("%.4f", rawLat)
        manualLonStr = String.format("%.4f", rawLon)
        manualCityStr = currentCity
    }

    val presetCities = listOf(
        Triple("Makkah (Haram)", 21.4267, 39.8261),
        Triple("London Central", 51.5074, -0.1278),
        Triple("Kuala Lumpur", 3.1390, 101.6869),
        Triple("Jakarta", -6.2088, 106.8456),
        Triple("Cairo (Egypt)", 30.0444, 31.2357),
        Triple("Karachi (Pakistan)", 24.8607, 67.0011),
        Triple("New York", 40.7128, -74.0060)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Islamic Prayer Schedule", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 0. GLOBAL LOCATION SEARCH CARD (Anywhere in the world)
            item {
                var searchQuery by remember { mutableStateOf("") }
                var successFeedback by remember { mutableStateOf<String?>(null) }
                val searchResults by viewModel.searchResults.collectAsState()
                val isSearching by viewModel.isSearching.collectAsState()

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🌐 SEARCH ANYWHERE IN THE WORLD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (isSearching) {
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                if (it.trim().length >= 2) {
                                    viewModel.searchLocationsInWorld(it)
                                } else {
                                    viewModel.clearSearchResults()
                                }
                            },
                            placeholder = { Text("e.g. Tokyo, Paris, Cairo, Makkah") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search icon",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        viewModel.clearSearchResults()
                                        successFeedback = null
                                    }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("global_search_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Display dynamic success feedback banner if user selected a place
                        successFeedback?.let { msg ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = msg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Search Results Area
                        if (isSearching && searchResults.isEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Searching worldwide database...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                }
                            }
                        } else if (searchResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                searchResults.take(6).forEach { place ->
                                    val fullName = buildString {
                                        append(place.name)
                                        if (!place.country.isNullOrBlank()) {
                                            append(", ${place.country}")
                                        }
                                    }
                                    val coordinatesStr = String.format("%.4f, %.4f", place.latitude, place.longitude)
                                    val subtitle = buildString {
                                        if (!place.admin1.isNullOrBlank()) {
                                            append("${place.admin1} — ")
                                        }
                                        append(coordinatesStr)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                            .clickable {
                                                viewModel.updateLocation(place.name, place.latitude, place.longitude)
                                                manualLatStr = String.format("%.4f", place.latitude)
                                                manualLonStr = String.format("%.4f", place.longitude)
                                                manualCityStr = place.name
                                                successFeedback = "Selected: $fullName"
                                                viewModel.clearSearchResults()
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("📍", fontSize = 16.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = fullName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = subtitle,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (searchQuery.trim().length >= 2 && !isSearching) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matches found. Try entering coordinates overrides below.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // 1. CHOOSE CITY CONFIGURATION DROP LIST
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "SELECT LOCATION PRESET",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .clickable { showPresetMenu = true }
                                .padding(14.dp)
                                .testTag("city_dropdown_trigger")
                        ) {
                            Text(currentCity, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            DropdownMenu(
                                expanded = showPresetMenu,
                                onDismissRequest = { showPresetMenu = false }
                            ) {
                                presetCities.forEach { (name, lat, lon) ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            viewModel.updateLocation(name, lat, lon)
                                            manualLatStr = String.format("%.4f", lat)
                                            manualLonStr = String.format("%.4f", lon)
                                            manualCityStr = name
                                            showPresetMenu = false
                                        },
                                        modifier = Modifier.testTag("preset_city_$name")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. DISPATCH OVERRIDE MANUAL VALUES
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "MANUAL COORDINATE OVERRIDES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = manualLatStr,
                                onValueChange = { manualLatStr = it },
                                label = { Text("Lat", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("lat_input")
                            )

                            OutlinedTextField(
                                value = manualLonStr,
                                onValueChange = { manualLonStr = it },
                                label = { Text("Long", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("lon_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = manualCityStr,
                            onValueChange = { manualCityStr = it },
                            label = { Text("Custom City Name", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("city_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val latDouble = manualLatStr.toDoubleOrNull() ?: rawLat
                                val lonDouble = manualLonStr.toDoubleOrNull() ?: rawLon
                                val cityName = manualCityStr.ifEmpty { "Custom Location" }
                                viewModel.updateLocation(cityName, latDouble, lonDouble)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("apply_coords_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("APPLY COORDINATES", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. ADHAN ALERTS SWITCH & CALC METHOD SELECTOR
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Adhan Alerts Notification",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Play peaceful alarm beep inside the app when Salah hits",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                        Switch(
                            checked = adhanAlert,
                            onCheckedChange = { viewModel.setAdhanAlerthandler(it) },
                            modifier = Modifier.testTag("adhan_toggle")
                        )
                    }
                }
            }

            // 4. CHRONIC PRAYER TIMES TIMELINE
            item {
                Text(
                    text = "Daily Schedule Timeline",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                prayerTimes?.let { times ->
                    val prayerItems = listOf(
                        Triple("Suhoor Start (Imyak)", times.suhoorStart, "Eat meal before fasting start"),
                        Triple("Fajr Dawn", times.fajr, "Morning twilight congregational Salah"),
                        Triple("Suhoor End / Shuruq", times.suhoorEnd, "Fasting begins, sun is rising"),
                        Triple("Dhuhr Midday", times.dhuhr, "Reflect and pray during midday rest"),
                        Triple("Asr Afternoon", times.asr, "Afternoon prayer of high spiritual valor"),
                        Triple("Maghrib Sunset", times.maghrib, "Time to break the fast & evening Salah"),
                        Triple("Isha Nightfall", times.isha, "Night prayer of peace and focus"),
                        Triple("Tahajjud Window", times.tahajjudStart, "Start of last third of night (ends: Fajr)")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        prayerItems.forEach { (name, time, desc) ->
                            val isSpecialWindow = name.contains("Suhoor") || name.contains("Tahajjud")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSpecialWindow) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                }
                                Text(
                                    text = time,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } ?: run {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Calculation parameters note
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Method Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calculation based on general Makkah parameters (Fajr: 18.5°, Isha: 1.5 hours offset). Calculated offline safely.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
