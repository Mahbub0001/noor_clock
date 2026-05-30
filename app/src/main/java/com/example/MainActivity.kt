package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.NoorClockTheme
import com.example.ui.viewmodel.NoorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Instantiate our central ViewModel running the background alarm dispatcher
            val noorViewModel: NoorViewModel = viewModel()
            val activeThemeName by noorViewModel.activeTheme.collectAsState()
            val triggeredAlarm by noorViewModel.triggeredAlarm.collectAsState()

            NoorClockTheme(themeName = activeThemeName) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()

                    // Normal app navigation backstack
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onNavigateToHome = {
                                    navController.navigate("dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("dashboard") {
                            HomeDashboardScreen(
                                viewModel = noorViewModel,
                                onNavigateToAlarms = { navController.navigate("alarm_list") },
                                onNavigateToTasks = { navController.navigate("tasks") },
                                onNavigateToPrayer = { navController.navigate("prayer_times") },
                                onNavigateToTheme = { navController.navigate("themes") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }

                        composable("alarm_list") {
                            AlarmListScreen(
                                viewModel = noorViewModel,
                                onNavigateToCreateAlarm = { alarmId ->
                                    navController.navigate("create_alarm/$alarmId")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "create_alarm/{alarmId}",
                            arguments = listOf(navArgument("alarmId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val alarmId = backStackEntry.arguments?.getInt("alarmId") ?: -1
                            CreateEditAlarmScreen(
                                alarmId = alarmId,
                                viewModel = noorViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("tasks") {
                            ChecklistScreen(
                                viewModel = noorViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("prayer_times") {
                            PrayerTimesScreen(
                                viewModel = noorViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("themes") {
                            ThemeScreen(
                                viewModel = noorViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = noorViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // CRITICAL DISPATCH OVERLAY INTERCEPT
                    // If an alarm triggers, it immediately overlays fullscreen on top of all views!
                    if (triggeredAlarm != null) {
                        AlarmOverlayScreen(viewModel = noorViewModel)
                    }
                }
            }
        }
    }
}
