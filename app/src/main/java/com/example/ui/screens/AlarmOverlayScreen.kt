package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AestheticMediaCanvas
import com.example.ui.viewmodel.NoorViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AlarmOverlayScreen(viewModel: NoorViewModel) {
    val activeAlarm by viewModel.triggeredAlarm.collectAsState()
    val mathQuestion by viewModel.dismissMathQuestion.collectAsState()

    val alarm = activeAlarm ?: return

    var userAnswer by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fullscreen dynamic background slides based on user alarm media rule selection!
        AestheticMediaCanvas(
            presetName = alarm.mediaPreset,
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
        )

        // Screen content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section (Header)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = alarm.mediaPreset.uppercase(),
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ALARM RINGING",
                    fontSize = 28.sp,
                    letterSpacing = 4.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Custom Alarm Notification Label
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = alarm.label.ifEmpty { "Wake Up Beautiful Soul" },
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Middle Section (Clock and Puzzle)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Large Clock Display
                val formattedHour = String.format("%02d", alarm.hour)
                val formattedMinute = String.format("%02d", alarm.minute)
                Text(
                    text = "$formattedHour:$formattedMinute",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    letterSpacing = (-2).sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Puzzle panel (if Force Dismiss Mode is activated)
                if (alarm.forceDismissMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🧠 FORCE DISMISS CHALLENGE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Solve this equation to wake up your mind:",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = mathQuestion,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = userAnswer,
                                onValueChange = { userAnswer = it },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color(0xFFFFD54F),
                                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                placeholder = { Text("Answer", color = Color.White.copy(alpha = 0.5f)) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (inputError) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Incorrect, new puzzle generated!",
                                    color = Color(0xFFFFEF8A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Section (Action buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Snooze Button
                Button(
                    onClick = { viewModel.snoozeAlarm() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.25f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "SNOOZE (${alarm.snoozeMinutes}m)",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Dismiss Button
                Button(
                    onClick = {
                        if (alarm.forceDismissMode) {
                            val correct = viewModel.verifyMathAnswer(userAnswer)
                            if (!correct) {
                                userAnswer = ""
                                inputError = true
                            }
                        } else {
                            viewModel.dismissAlarmOnly()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "DISMISS",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
