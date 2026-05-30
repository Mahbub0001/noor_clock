package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Alarm
import com.example.ui.viewmodel.NoorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditAlarmScreen(
    alarmId: Int, // -1 if creating new
    viewModel: NoorViewModel,
    onBack: () -> Unit
) {
    val alarms by viewModel.alarms.collectAsState()

    // Find if we are editing an existing alarm
    val existingAlarm = remember(alarmId, alarms) {
        if (alarmId != -1) alarms.find { it.id == alarmId } else null
    }

    // State Variables
    var hour by remember { mutableStateOf(existingAlarm?.hour ?: 7) }
    var minute by remember { mutableStateOf(existingAlarm?.minute ?: 0) }
    var label by remember { mutableStateOf(existingAlarm?.label ?: "Wake Up for Reflection") }
    var ringtone by remember { mutableStateOf(existingAlarm?.ringtone ?: "Serene Dawn") }
    var vibration by remember { mutableStateOf(existingAlarm?.vibrationPattern ?: "Classic Pulsing") }
    var mediaPreset by remember { mutableStateOf(existingAlarm?.mediaPreset ?: "Mountain Sunrise") }
    var snoozeRule by remember { mutableStateOf(existingAlarm?.snoozeMinutes ?: 5) }
    var forceDismiss by remember { mutableStateOf(existingAlarm?.forceDismissMode ?: false) }

    // Dropdown toggle states
    var ringtoneExpanded by remember { mutableStateOf(false) }
    var vibrationExpanded by remember { mutableStateOf(false) }
    var mediaExpanded by remember { mutableStateOf(false) }
    var snoozeExpanded by remember { mutableStateOf(false) }

    val ringtoneOptions = listOf("Serene Dawn", "Birds Melody", "Gentle Harp", "Melodious Echo")
    val vibrationOptions = listOf("None", "Classic Pulsing", "Rapid Heartbeat", "Smooth Wave")
    val mediaOptions = listOf("Mountain Sunrise", "Makkah Mosque", "Peaceful Cosmos", "Ocean Ripples")
    val snoozeOptions = listOf(2, 5, 10, 15)

    val customRingtones by viewModel.customRingtones.collectAsState()
    val selectedRingtoneName = remember(ringtone, customRingtones) {
        if (ringtone.startsWith("/") || ringtone.startsWith("content://")) {
            customRingtones.find { it.path == ringtone }?.name ?: ringtone.substringAfterLast('/').substringBeforeLast('.')
        } else {
            ringtone
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val path = viewModel.addCustomRingtone(it)
            if (path != null) {
                ringtone = path
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (alarmId == -1) "Set New Alarm" else "Edit Alarm", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
            // 1. TIME SELECTOR (HOUR & MINUTE CONTROLS)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SELECT AWAKENING TIME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hour selector
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(
                                    onClick = { if (hour < 23) hour++ else hour = 0 },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp).testTag("hour_up")
                                ) { Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = String.format("%02d", hour),
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { if (hour > 0) hour-- else hour = 23 },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp).testTag("hour_down")
                                ) { Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                            }

                            Text(
                                text = ":",
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Minute selector
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(
                                    onClick = { if (minute < 59) minute++ else minute = 0 },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp).testTag("min_up")
                                ) { Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = String.format("%02d", minute),
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { if (minute > 0) minute-- else minute = 59 },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp).testTag("min_down")
                                ) { Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                            }
                        }

                        // AM / PM Helper String
                        Spacer(modifier = Modifier.height(16.dp))
                        val displayH = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                        val amPm = if (hour < 12) "AM" else "PM"
                        Text(
                            text = "Rings at: $displayH:${String.format("%02d", minute)} $amPm",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 2. ALARM LABEL TEXTFIELD
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "ALARM LABEL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            placeholder = { Text("What triggers this alarm?") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("alarm_label_input")
                        )
                    }
                }
            }

            // 3. MEDIA PRESENTS & RINGTONE CONTROLS (EXPANDABLE DROPDOWNS)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "RINGTONE & MULTIMEDIA SLIDESHOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )

                        // Ringtone Dropdown
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Ringtone Preset Sound", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .clickable { ringtoneExpanded = true }
                                    .padding(14.dp)
                                    .testTag("ringtone_picker")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🎵", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(selectedRingtoneName, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                                    }
                                    Text("▼", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                DropdownMenu(
                                    expanded = ringtoneExpanded,
                                    onDismissRequest = { ringtoneExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Standard Presets", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary) },
                                        onClick = {},
                                        enabled = false
                                    )
                                    ringtoneOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                ringtone = option
                                                ringtoneExpanded = false
                                            },
                                            leadingIcon = {
                                                if (ringtone == option) {
                                                    Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        )
                                    }
                                    if (customRingtones.isNotEmpty()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        DropdownMenuItem(
                                            text = { Text("Custom Audio Files", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary) },
                                            onClick = {},
                                            enabled = false
                                        )
                                        customRingtones.forEach { customTone ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(customTone.name, modifier = Modifier.weight(1f), fontSize = 13.sp)
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.deleteCustomRingtone(customTone.path)
                                                                if (ringtone == customTone.path) {
                                                                    ringtone = "Serene Dawn"
                                                                }
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Text("❌", fontSize = 10.sp)
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    ringtone = customTone.path
                                                    ringtoneExpanded = false
                                                },
                                                leadingIcon = {
                                                    if (ringtone == customTone.path) {
                                                        Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = { audioPickerLauncher.launch("audio/*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("➕ Import Custom Audio File (.mp3, .wav, .ogg)")
                            }
                        }

                        // Custom Post-Alarm slideshow/media selector (Preset wall choices)
                        Column {
                            Text("Post-Alarm Media Screen Preset", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Aesthetic content shown fullscreen on dismiss", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .clickable { mediaExpanded = true }
                                    .padding(14.dp)
                                    .testTag("media_picker_card")
                            ) {
                                Text(mediaPreset, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                                DropdownMenu(
                                    expanded = mediaExpanded,
                                    onDismissRequest = { mediaExpanded = false }
                                ) {
                                    mediaOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                mediaPreset = option
                                                mediaExpanded = false
                                            },
                                            modifier = Modifier.testTag("media_option_$option")
                                        )
                                    }
                                }
                            }
                        }

                        // Vibration Options Dropdown
                        Column {
                            Text("Vibration Pattern", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .clickable { vibrationExpanded = true }
                                    .padding(14.dp)
                            ) {
                                Text(vibration, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                                DropdownMenu(
                                    expanded = vibrationExpanded,
                                    onDismissRequest = { vibrationExpanded = false }
                                ) {
                                    vibrationOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                vibration = option
                                                vibrationExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Snooze Minutes list Dropdown
                        Column {
                            Text("Snooze Custom Rules", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .clickable { snoozeExpanded = true }
                                    .padding(14.dp)
                            ) {
                                Text("$snoozeRule Minutes Snooze Rule", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                                DropdownMenu(
                                    expanded = snoozeExpanded,
                                    onDismissRequest = { snoozeExpanded = false }
                                ) {
                                    snoozeOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text("$option Minutes Rule") },
                                            onClick = {
                                                snoozeRule = option
                                                snoozeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. SMART FORCE DISMISS ACTIVATE SWITCH
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
                                text = "Force Dismiss Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Require math puzzle solver to mute alarm. Prevents going back to bed!",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                        Switch(
                            checked = forceDismiss,
                            onCheckedChange = { forceDismiss = it },
                            modifier = Modifier.testTag("force_dismiss_toggle")
                        )
                    }
                }
            }

            // 5. SAVE SUBMIT BUTTONS
            item {
                Button(
                    onClick = {
                        val alarm = Alarm(
                            id = if (existingAlarm != null) existingAlarm.id else 0,
                            hour = hour,
                            minute = minute,
                            label = label,
                            daysOfWeek = existingAlarm?.daysOfWeek ?: "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
                            isEnabled = true,
                            ringtone = ringtone,
                            vibrationPattern = vibration,
                            mediaPreset = mediaPreset,
                            snoozeMinutes = snoozeRule,
                            forceDismissMode = forceDismiss
                        )
                        viewModel.saveAlarm(alarm) {
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_alarm_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "SAVE DISPATCH RULE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
