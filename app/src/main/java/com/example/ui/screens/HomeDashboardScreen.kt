package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.NoorViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    viewModel: NoorViewModel,
    onNavigateToAlarms: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToPrayer: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val city by viewModel.currentCity.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val condition by viewModel.weatherCondition.collectAsState()
    val isWeatherLoading by viewModel.isWeatherLoading.collectAsState()

    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val alarms by viewModel.alarms.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    val quote by viewModel.aiInspirationalQuote.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    // Location selection dialog visibility controller
    var showLocationDialog by remember { mutableStateOf(false) }

    // Real-time local digital clock
    var liveTimeString by remember { mutableStateOf("") }
    var liveDateString by remember { mutableStateOf("") }

    LaunchedEffect(key1 = true) {
        while (true) {
            val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val sdfDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            liveTimeString = sdfTime.format(Date())
            liveDateString = sdfDate.format(Date())
            delay(1000)
        }
    }

    // Calculate daily checklist accomplishments
    val totalTasksCount = tasks.size
    val completedTasksCount = tasks.count { it.isCompleted }
    val progressFraction = if (totalTasksCount > 0) completedTasksCount.toFloat() / totalTasksCount else 0.0f

    // Calculate next alarm indicator
    val enabledAlarms = alarms.filter { it.isEnabled }
    val nextAlarmStr = if (enabledAlarms.isEmpty()) {
         "No active alarms"
    } else {
         val sorted = enabledAlarms.sortedWith(compareBy({ it.hour }, { it.minute }))
         val first = sorted.first()
         String.format("Next: %02d:%02d", first.hour, first.minute)
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showLocationDialog = true }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                            .testTag("home_location_trigger"),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Assalamu Alaikum",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = city,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Change Coordinates",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Theme selector portal bubble
                    IconButton(
                        onClick = onNavigateToTheme,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            )
                            .testTag("theme_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme customization",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
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
            // 1. DIGITAL CLOCK & HIJRI HEADER
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = liveTimeString,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-1).sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = liveDateString,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Simulated Islamic Hijri Calender Date
                        Text(
                            text = "Hijri: 13 Dhul-Hijjah 1447 AH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 2. SMART AI REFRESH CHECK-IN (GEMINI PROMPTED INFUSE)
            item {
                Text(
                    text = "Aura Mindfulness",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🧠", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AI Spiritual Companion",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Tailored to your climate & tasks",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }

                            // Refresh button calling Gemini
                            Button(
                                onClick = { viewModel.triggerAiVibeCheck() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(34.dp).testTag("refresh_quote_btn"),
                                enabled = !isAiLoading
                            ) {
                                if (isAiLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Gemini AI",
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ask", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "“$quote”",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // 3. ISLAMIC DAILY SCHEDULE TIMELINE
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Salah & Prayer Windows",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Full Schedule →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToPrayer() }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                prayerTimes?.let { times ->
                    val timeline = listOf(
                        Pair("Fajr", times.fajr),
                        Pair("Dhuhr", times.dhuhr),
                        Pair("Asr", times.asr),
                        Pair("Maghrib", times.maghrib),
                        Pair("Isha", times.isha),
                        Pair("Suhoor End", times.suhoorEnd),
                        Pair("Tahajjud Start", times.tahajjudStart)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(timeline) { (name, time) ->
                            val isSpecial = name.contains("Suhoor") || name.contains("Tahajjud")
                            Box(
                                modifier = Modifier
                                    .width(130.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSpecial) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Icon(
                                        imageVector = when(name) {
                                            "Fajr" -> Icons.Default.WbTwilight
                                            "Dhuhr" -> Icons.Default.WbSunny
                                            "Asr" -> Icons.Outlined.WbSunny
                                            "Maghrib" -> Icons.Default.NightsStay
                                            "Isha" -> Icons.Outlined.NightsStay
                                            "Suhoor End" -> Icons.Default.HourglassBottom
                                            else -> Icons.Default.Spa
                                        },
                                        contentDescription = name,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = time,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // 4. ALARMS & WEATHER MERGED PORTALS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // WEATHER QUICK CARD
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onNavigateToSettings() } // settings handles weather customization as well
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "WEATHER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                )
                                Icon(
                                    imageVector = if (condition.contains("Rain")) Icons.Default.Cloud else Icons.Default.WbSunny,
                                    contentDescription = "Weather Icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            if (isWeatherLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = String.format("%.1f°C", temperature),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = condition,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // ALARM QUICK CARD
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onNavigateToAlarms() }
                            .padding(20.dp)
                            .testTag("alarms_portal_card")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ALARMS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = "Alarms",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = nextAlarmStr,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Active Alarms: ${alarms.count { it.isEnabled }}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 5. PRODUCTIVITY HABIT PORTAL CARD
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onNavigateToTasks() }
                        .padding(20.dp)
                        .testTag("tasks_portal_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PRODUCTIVITY CHECKLIST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$completedTasksCount of $totalTasksCount Completed",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Elegant linear progress indicator
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        }

                        // Right-aligned streak action circular badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Checklist Tasks",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // 6. BOTTOM NAVIGATION MENU MAPPER PORTALS (GRID STYLE LINKS)
            item {
                Text(
                    text = "Noor Core Services",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CoreGridButton(
                        text = "Alarm System",
                        icon = Icons.Default.AccessTime,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAlarms
                    )
                    CoreGridButton(
                        text = "Checklist Habits",
                        icon = Icons.Default.PlaylistAddCheck,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTasks
                    )
                    CoreGridButton(
                        text = "Prayer Times",
                        icon = Icons.Default.Explore,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPrayer
                    )
                    CoreGridButton(
                        text = "System Setup",
                        icon = Icons.Default.Settings,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSettings
                    )
                }
            }
        }

        // Worldwide Geocoding Search & Precision Coordinates Override Dialog
        if (showLocationDialog) {
            AlertDialog(
                onDismissRequest = {
                    showLocationDialog = false
                    viewModel.clearSearchResults()
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌐", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Change Location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                text = {
                    var searchQuery by remember { mutableStateOf("") }
                    var successFeedback by remember { mutableStateOf<String?>(null) }
                    val searchResults by viewModel.searchResults.collectAsState()
                    val isSearching by viewModel.isSearching.collectAsState()

                    // Collect current coordinate attributes from ViewModel
                    val currentLat by viewModel.latitude.collectAsState()
                    val currentLon by viewModel.longitude.collectAsState()
                    val currentCityName by viewModel.currentCity.collectAsState()

                    var manualLatStr by remember { mutableStateOf(String.format(Locale.US, "%.4f", currentLat)) }
                    var manualLonStr by remember { mutableStateOf(String.format(Locale.US, "%.4f", currentLon)) }
                    var manualCityStr by remember { mutableStateOf(currentCityName) }
                    var validationError by remember { mutableStateOf<String?>(null) }

                    // Sync values when coords change or dialog launches
                    LaunchedEffect(currentLat, currentLon, currentCityName) {
                        manualLatStr = String.format(Locale.US, "%.4f", currentLat)
                        manualLonStr = String.format(Locale.US, "%.4f", currentLon)
                        manualCityStr = currentCityName
                    }

                    // Perform search
                    LaunchedEffect(searchQuery) {
                        if (searchQuery.trim().length >= 2) {
                            viewModel.searchLocationsInWorld(searchQuery)
                        } else {
                            viewModel.clearSearchResults()
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header instructions
                        item {
                            Text(
                                text = "Search any country, city, district, village, or enter exact georeferenced coordinates to customize prayer times and live weather feeds offline-first.",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }

                        // Search Text Field Input
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search city, country, district, village...") },
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
                                    .testTag("dialog_location_search_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Display dynamic success feedback banner if user selected a place
                        successFeedback?.let { msg ->
                            item {
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
                        }

                        // Display search progress & matches list
                        if (isSearching && searchResults.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Searching global database...",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        } else if (searchResults.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Matches Found",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(searchResults) { place ->
                                val fullName = buildString {
                                    append(place.name)
                                    if (!place.country.isNullOrBlank()) {
                                        append(", ${place.country}")
                                    }
                                }
                                val coordinatesStr = String.format(Locale.US, "%.4f, %.4f", place.latitude, place.longitude)
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
                                            successFeedback = "Selected: $fullName"
                                            searchQuery = ""
                                            viewModel.clearSearchResults()
                                            showLocationDialog = false
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("📍", fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
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
                        } else if (searchQuery.trim().length >= 2 && !isSearching) {
                            item {
                                Text(
                                    text = "No matches found. Try coordinates override below.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }

                        // Presets Grid
                        item {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = "🎁 Global Landmarks & Islamic Capital Seeds",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        item {
                            val presets = listOf(
                                Triple("Makkah (Haram)", 21.4267, 39.8261),
                                Triple("Madinah (Al-Masjid)", 24.4672, 39.6108),
                                Triple("Al-Aqsa (Jerusalem)", 31.7761, 35.2358),
                                Triple("London Central", 51.5074, -0.1278),
                                Triple("Cairo (Egypt)", 30.0444, 31.2357),
                                Triple("Dhaka (Bangladesh)", 23.8103, 90.4125),
                                Triple("Jakarta", -6.2088, 106.8456),
                                Triple("Kuala Lumpur", 3.1390, 101.6869),
                                Triple("New York", 40.7128, -74.0060)
                            )

                            FlowRow(
                                maxItemsInEachRow = 3,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                presets.forEach { (name, lat, lon) ->
                                    val isSelected = city == name || 
                                            (name.startsWith("Makkah") && city.startsWith("Makkah"))
                                    
                                    SuggestionChip(
                                        onClick = {
                                            viewModel.updateLocation(name, lat, lon)
                                            successFeedback = "Position updated successfully to $name!"
                                            showLocationDialog = false
                                        },
                                        label = { Text(name, fontSize = 11.sp) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                            labelColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        // Precision Coordinate Overrides form (district, remote village precision override)
                        item {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = "📐 High-Precision Coordinate override",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = manualCityStr,
                                    onValueChange = { manualCityStr = it },
                                    label = { Text("Display Name") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = manualLatStr,
                                        onValueChange = { manualLatStr = it },
                                        label = { Text("Latitude") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = manualLonStr,
                                        onValueChange = { manualLonStr = it },
                                        label = { Text("Longitude") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                validationError?.let { err ->
                                    Text(err, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val lat = manualLatStr.toDoubleOrNull()
                                        val lon = manualLonStr.toDoubleOrNull()
                                        if (lat == null || lat < -90.0 || lat > 90.0) {
                                            validationError = "Invalid latitude value (-90 to +90)"
                                        } else if (lon == null || lon < -180.0 || lon > 180.0) {
                                            validationError = "Invalid longitude value (-180 to +180)"
                                        } else if (manualCityStr.trim().isEmpty()) {
                                            validationError = "Display name label cannot be empty"
                                        } else {
                                            validationError = null
                                            viewModel.updateLocation(manualCityStr.trim(), lat, lon)
                                            successFeedback = "Set custom position: $manualCityStr successfully!"
                                            showLocationDialog = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Save Option",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Apply Custom Coordinates", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLocationDialog = false
                            viewModel.clearSearchResults()
                        }
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    }
}

@Composable
fun CoreGridButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
