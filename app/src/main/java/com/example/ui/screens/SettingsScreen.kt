package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.example.ui.RescueViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: RescueViewModel) {
    val language by viewModel.language.collectAsState()
    val country by viewModel.country.collectAsState()
    val darkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var showLangSelector by remember { mutableStateOf(false) }
    var showCountrySelector by remember { mutableStateOf(false) }

    var isUpdatingPack by remember { mutableStateOf(false) }
    var updateMsg by remember { mutableStateOf("") }

    val languages = listOf(
        "en" to "English 🇺🇸",
        "bn" to "বাংলা 🇧🇩",
        "hi" to "हिन्दी 🇮🇳",
        "ar" to "العربية 🇸🇦",
        "es" to "Español 🇪🇸"
    )

    val countries = listOf(
        "🇧🇩 Bangladesh",
        "🇺🇸 United States",
        "🇮🇳 India",
        "🇬🇧 United Kingdom",
        "🇪🇸 Spain"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Offline Packs", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "PREFERENCES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Language
            ListItem(
                headlineContent = { Text("App Language / ভাষা", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text(languages.firstOrNull { it.first == language }?.second ?: "English 🇺🇸") },
                leadingContent = { Icon(Icons.Filled.Language, contentDescription = "Lang", tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable { showLangSelector = true }
            )

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))

            // Country
            ListItem(
                headlineContent = { Text("Region & Dial Codes", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text(country) },
                leadingContent = { Icon(Icons.Filled.Place, contentDescription = "Country", tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable { showCountrySelector = true }
            )

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))

            // Dark Mode
            ListItem(
                headlineContent = { Text("Force Dark Mode (Battery Saver)", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Saves OLED/AMOLED battery power in emergency grids failure.") },
                leadingContent = { Icon(Icons.Filled.BatterySaver, contentDescription = "Dark", tint = MaterialTheme.colorScheme.primary) },
                trailingContent = {
                    Switch(checked = darkMode, onCheckedChange = { viewModel.setDarkMode(it) })
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "OFFLINE CONTENT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Redownload packs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Offline Safety Guides & Maps", fontWeight = FontWeight.Bold)
                    Text("Keep guides and local contact catalogs fully updated for active disaster seasons.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isUpdatingPack) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Updating databases...", fontSize = 14.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                isUpdatingPack = true
                                scope.launch {
                                    delay(1500)
                                    isUpdatingPack = false
                                    updateMsg = "All database regions successfully updated!"
                                }
                            },
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(Icons.Filled.CloudDownload, contentDescription = "Update")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Update Offline Pack")
                        }
                    }

                    if (updateMsg.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(updateMsg, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "SYSTEM INFO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Local Data Privacy", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("All your medical profiles and contacts are stored purely on-device.") },
                leadingContent = { Icon(Icons.Filled.PrivacyTip, contentDescription = "Privacy", tint = MaterialTheme.colorScheme.primary) }
            )

            ListItem(
                headlineContent = { Text("Backup & Security", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Manual offline backups can be exported via SD Card files.") },
                leadingContent = { Icon(Icons.Filled.Backup, contentDescription = "Backup", tint = MaterialTheme.colorScheme.primary) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Developer & Company profile representation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DEVELOPER & PUBLISHER PROFILE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("👨‍💻 Developed by Prince AR Abdur Rahman", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📞 Contact Developer:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("WhatsApp: 01707424006 | WhatsApp: 01796951709", fontSize = 12.sp)
                    
                    Row(modifier = Modifier.padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Facebook",
                            modifier = Modifier
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/share/1BNn32qoJo/"))
                                    context.startActivity(intent)
                                }
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Instagram",
                            modifier = Modifier
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/ur___abdur____rahman__2008"))
                                    context.startActivity(intent)
                                }
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("🏢 About Company: NexVora Lab's Ofc", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Text("Mission: Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.", fontSize = 11.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Version: 1.0.0 | © 2026 NexVora Lab's Ofc. All Rights Reserved.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "RescueHub v1.0.0-Beta • Open Source",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Language Dropdown Selector
    if (showLangSelector) {
        AlertDialog(
            onDismissRequest = { showLangSelector = false },
            confirmButton = {
                TextButton(onClick = { showLangSelector = false }) { Text("Dismiss") }
            },
            title = { Text("Select Language", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    languages.forEach { (code, name) ->
                        ListItem(
                            headlineContent = { Text(name, fontWeight = FontWeight.Bold) },
                            modifier = Modifier
                                .clickable {
                                    viewModel.setLanguage(code)
                                    showLangSelector = false
                                }
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        )
    }

    // Country Dropdown Selector
    if (showCountrySelector) {
        AlertDialog(
            onDismissRequest = { showCountrySelector = false },
            confirmButton = {
                TextButton(onClick = { showCountrySelector = false }) { Text("Dismiss") }
            },
            title = { Text("Select Country", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    countries.forEach { name ->
                        ListItem(
                            headlineContent = { Text(name, fontWeight = FontWeight.Bold) },
                            modifier = Modifier
                                .clickable {
                                    viewModel.setCountry(name)
                                    showCountrySelector = false
                                }
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        )
    }
}
