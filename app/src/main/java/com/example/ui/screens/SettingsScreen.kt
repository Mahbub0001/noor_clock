package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val activeTheme by viewModel.activeTheme.collectAsState()
    val isQuietHours by viewModel.isQuietHours.collectAsState()
    val snoozeSettingMinutes by viewModel.snoozeMinutesRule.collectAsState()
    val persistentReminders by viewModel.persistentReminders.collectAsState()

    val currentCity by viewModel.currentCity.collectAsState()
    val currentLat by viewModel.latitude.collectAsState()
    val currentLong by viewModel.longitude.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val weatherCondition by viewModel.weatherCondition.collectAsState()

    var snoozeDropdownExpanded by remember { mutableStateOf(false) }
    val snoozeIntervals = listOf(2, 5, 10, 15, 20)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Setup Settings", fontWeight = FontWeight.Bold) },
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
            // 1. SMART SNOOZE SETTINGS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "SMART ALARM RULES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Default Snooze Window Interval", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .clickable { snoozeDropdownExpanded = true }
                                .padding(14.dp)
                                .testTag("snooze_rule_dropdown_trigger")
                        ) {
                            Text("$snoozeSettingMinutes Minutes Standard Rule", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                            DropdownMenu(
                                expanded = snoozeDropdownExpanded,
                                onDismissRequest = { snoozeDropdownExpanded = false }
                            ) {
                                snoozeIntervals.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text("$option Minutes Standard Interval") },
                                        onClick = {
                                            viewModel.setSnoozeMinutesRule(option)
                                            snoozeDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("snooze_interval_option_$option")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. QUIET HOURS MODE SETTINGS
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
                                text = "Quiet Hours Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Disable all alarm sounds and notifications during family hours.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                        Switch(
                            checked = isQuietHours,
                            onCheckedChange = { viewModel.setQuietHours(it) },
                            modifier = Modifier.testTag("quiet_hours_switch")
                        )
                    }
                }
            }

            // 3. PERSISTENT CHECKLIST REMINDERS
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
                                text = "Persistent Habit Reminders",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Keep checklist reminders on status panel until manually deleted.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                        Switch(
                            checked = persistentReminders,
                            onCheckedChange = { viewModel.setPersistentReminders(it) },
                            modifier = Modifier.testTag("persistent_reminders_switch")
                        )
                    }
                }
            }

            // 4. WEATHER STATUS OVERVIEW
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LOCATION WEATHER STATUS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                            IconButton(
                                onClick = { viewModel.recalculatePrayerTimesAndWeather() },
                                modifier = Modifier.size(24.dp).testTag("refresh_weather_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh weather status",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Selected City/Region:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(currentCity, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mapped Latitude:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(String.format("%.4f", currentLat), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mapped Longitude:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(String.format("%.4f", currentLong), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Temperature reading:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(String.format("%.1f°C", temperature), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Weather Condition status:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(weatherCondition, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // 5. PREMIUM GEMINI STATUS PANEL
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "PREMIUM GEMINI SERVICES STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your daily spiritual companion utilizes the server-side Gemini 3.5 Flash model directly from user secrets to generate personalized, aesthetic lifestyle and prayer reflections.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "● INJECTED BY PLATFORM SECRETS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
