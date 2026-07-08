package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.RescueViewModel

@Composable
fun RescueMainScreen(viewModel: RescueViewModel) {
    val isOnboarded by viewModel.onboardingCompleted.collectAsState()

    if (!isOnboarded) {
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = {
                // Done in ViewModel via callback
            }
        )
    } else {
        var selectedTab by remember { mutableStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Filled.SmartToy, contentDescription = "AI Assistant") },
                        label = { Text("AI Assistant") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Filled.MedicalInformation, contentDescription = "Medical ID") },
                        label = { Text("Medical ID") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Filled.Book, contentDescription = "Diary") },
                        label = { Text("Diary") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = selectedTab, label = "ScreenTransition") { tab ->
                    when (tab) {
                        0 -> MainDashboard(viewModel = viewModel)
                        1 -> ChatAssistantScreen(viewModel = viewModel)
                        2 -> MedicalIdScreen(viewModel = viewModel)
                        3 -> EmergencyDiaryScreen(viewModel = viewModel)
                        4 -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
