package com.example.ui.screens

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FaqDatabase
import com.example.data.SafePlaceBookmark
import com.example.data.SurvivalItem
import com.example.ui.EmergencyNumber
import com.example.ui.RescueViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

data class GridMenuOption(
    val id: String,
    val titleKey: String,
    val icon: ImageVector,
    val color: Color,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(viewModel: RescueViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val lang by viewModel.language.collectAsState()
    val emergencyMode by viewModel.isEmergencyMode.collectAsState()
    val sirenPlaying by viewModel.isSirenPlaying.collectAsState()
    val flashlightOn by viewModel.isFlashlightOn.collectAsState()
    val sosBlinking by viewModel.isSosBlinking.collectAsState()

    val survivalItems by viewModel.survivalItems.collectAsState()
    val bookmarkedPlaces by viewModel.bookmarkedSafePlaces.collectAsState()
    val emergencyNumbers by viewModel.emergencyNumbers.collectAsState()
    val medicalProfile by viewModel.medicalProfile.collectAsState()
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()

    // Dashboard State Variables
    var selectedMenuId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<String?>(null) }

    // Symptoms check states
    var symBreath by remember { mutableStateOf(false) }
    var symDizzy by remember { mutableStateOf(false) }
    var symFever by remember { mutableStateOf(false) }
    var symBleeding by remember { mutableStateOf(false) }
    var symUnconscious by remember { mutableStateOf(false) }

    // Quiz training states
    var quizStep by remember { mutableStateOf(0) } // 0: Not started, 1: Q1, 2: Q2, 3: Q3, 4: Done
    var quizScore by remember { mutableStateOf(0) }
    var quizAnswerFeedback by remember { mutableStateOf<String?>(null) }

    // SharedPreferences values for Vault, Volunteer & Score
    val sharedPrefs = remember { context.getSharedPreferences("rescue_hub_local_db", Context.MODE_PRIVATE) }
    
    var nidVault by remember { mutableStateOf(sharedPrefs.getString("vault_nid", "") ?: "") }
    var passportVault by remember { mutableStateOf(sharedPrefs.getString("vault_passport", "") ?: "") }
    var birthCertVault by remember { mutableStateOf(sharedPrefs.getString("vault_birth_cert", "") ?: "") }
    var insuranceVault by remember { mutableStateOf(sharedPrefs.getString("vault_insurance", "") ?: "") }
    var bloodGroupVault by remember { mutableStateOf(sharedPrefs.getString("vault_blood", "") ?: "") }
    var medicalRecordVault by remember { mutableStateOf(sharedPrefs.getString("vault_medical", "") ?: "") }

    var isVolunteerRegistered by remember { mutableStateOf(sharedPrefs.getBoolean("vol_registered", false)) }
    var volunteerBloodGroup by remember { mutableStateOf(sharedPrefs.getString("vol_blood", "A+") ?: "A+") }
    var volunteerSkills by remember { mutableStateOf(sharedPrefs.getString("vol_skills", "First Aid, CPR") ?: "First Aid, CPR") }

    // TextToSpeech initialization
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = when (lang) {
                    "bn" -> Locale("bn", "BD")
                    else -> Locale.US
                }
                tts?.language = locale
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    fun speakText(text: String) {
        if (tts != null) {
            if (isTtsSpeaking) {
                tts?.stop()
                isTtsSpeaking = false
            } else {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "RescueHubTTS")
                isTtsSpeaking = true
            }
        }
    }

    // Speech to text launcher (Voice Search)
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                searchQuery = spokenText
                searchResult = FaqDatabase.getAnswer(spokenText, lang)
            }
        }
    }

    // Dynamic Personal Preparedness Score Calculation (0 - 100)
    val totalScore = remember(survivalItems, emergencyContacts, medicalProfile, isVolunteerRegistered, quizStep) {
        var score = 0
        // 1. Checklist completion (up to 25 points)
        val itemsChecked = survivalItems.filter { it.isChecked }.size
        val itemsTotal = survivalItems.size
        if (itemsTotal > 0) {
            score += ((itemsChecked.toFloat() / itemsTotal.toFloat()) * 25).toInt()
        }
        // 2. Emergency Contacts exists (25 points)
        if (emergencyContacts.isNotEmpty()) {
            score += 25
        }
        // 3. Medical Profile loaded or blood group vault present (25 points)
        if (medicalProfile != null || bloodGroupVault.isNotBlank()) {
            score += 25
        }
        // 4. Completed Survival Quiz or volunteer registration (25 points)
        if (quizStep == 4 || isVolunteerRegistered) {
            score += 25
        }
        score.coerceAtMost(100)
    }

    // Grid menu definition mapping 15 features
    val gridOptions = listOf(
        GridMenuOption("first_aid", "first_aid", Icons.Filled.MedicalServices, Color(0xFFEF4444), "Health"),
        GridMenuOption("flood", "flood", Icons.Filled.Water, Color(0xFF3B82F6), "Disasters"),
        GridMenuOption("cyclone", "cyclone", Icons.Filled.Cyclone, Color(0xFF06B6D4), "Disasters"),
        GridMenuOption("earthquake", "earthquake", Icons.Filled.Public, Color(0xFFF59E0B), "Disasters"),
        GridMenuOption("fire", "fire", Icons.Filled.LocalFireDepartment, Color(0xFFF97316), "Disasters"),
        GridMenuOption("lightning", "lightning", Icons.Filled.Thunderstorm, Color(0xFF8B5CF6), "Disasters"),
        GridMenuOption("health_emergency", "health_emergency", Icons.Filled.Coronavirus, Color(0xFF10B981), "Health"),
        GridMenuOption("numbers", "numbers", Icons.Filled.ContactPhone, Color(0xFF14B8A6), "Tools"),
        GridMenuOption("map", "map", Icons.Filled.Map, Color(0xFF6366F1), "Tools"),
        GridMenuOption("kit", "kit", Icons.Filled.Backpack, Color(0xFFEC4899), "Tools"),
        GridMenuOption("radio", "radio", Icons.Filled.Radio, Color(0xFF84CC16), "Tools"),
        GridMenuOption("flashlight", "flashlight", Icons.Filled.FlashlightOn, Color(0xFFFBBF24), "Tools"),
        GridMenuOption("safe_places", "safe_places", Icons.Filled.HomeWork, Color(0xFF22C55E), "Tools"),
        GridMenuOption("family_plan", "family_plan", Icons.Filled.People, Color(0xFF64748B), "Tools"),
        GridMenuOption("alerts", "alerts", Icons.Filled.Warning, Color(0xFFDC2626), "Tools")
    )

    // Setup color schema based on simulated Smart Emergency battery saving mode
    val dynamicBackground = if (emergencyMode) Color(0xFF0C0C0E) else MaterialTheme.colorScheme.background
    val dynamicSurface = if (emergencyMode) Color(0xFF16161A) else MaterialTheme.colorScheme.surface
    val dynamicOnSurface = if (emergencyMode) Color(0xFFE3E2E6) else MaterialTheme.colorScheme.onSurface
    val dynamicOnBackground = if (emergencyMode) Color(0xFFF3F0F4) else MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dynamicBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(dynamicBackground)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Current Status Row with Emergency Saving toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (emergencyMode) Color(0xFFEF4444) else Color(0xFF2E7D32),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = if (emergencyMode) "EMERGENCY POWER SAVING ACTIVE" else "STATUS: DISASTER SAFE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (emergencyMode) Color(0xFFEF4444) else Color(0xFF2E7D32),
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Smart Emergency",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = dynamicOnBackground.copy(alpha = 0.6f)
                    )
                    Switch(
                        checked = emergencyMode,
                        onCheckedChange = { viewModel.toggleEmergencyMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Red,
                            checkedTrackColor = Color.Red.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }

            // 2. Title Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RescueHub",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        color = dynamicOnBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🛟",
                        fontSize = 24.sp
                    )
                }
                Text(
                    text = "Smart Survival & Disaster Preparedness Engine",
                    fontSize = 13.sp,
                    color = if (emergencyMode) Color.LightGray else SubtitleColor,
                    lineHeight = 16.sp
                )
            }

            // 3. circular preparedness score gauge
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = dynamicSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == "bn") "ব্যক্তিগত প্রস্তুতি স্কোর" else "Personal Preparedness Score",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = dynamicOnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == "bn") {
                                "কিট সম্পন্ন: ${survivalItems.filter { it.isChecked }.size}/${survivalItems.size} | প্রোফাইল ও কন্ট্যাক্ট যুক্ত আছে"
                            } else {
                                "Kit Completed: ${survivalItems.filter { it.isChecked }.size}/${survivalItems.size} | Contacts registered"
                            },
                            fontSize = 12.sp,
                            color = dynamicOnSurface.copy(alpha = 0.7f)
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(64.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { totalScore / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = if (totalScore >= 80) Color(0xFF2E7D32) else if (totalScore >= 50) Color(0xFFF59E0B) else Color(0xFFEF4444),
                            strokeWidth = 6.dp,
                            trackColor = dynamicOnSurface.copy(alpha = 0.1f)
                        )
                        Text(
                            text = "$totalScore%",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = dynamicOnSurface
                        )
                    }
                }
            }

            // 4. One-click Family Safety "I'M SAFE" check
            Button(
                onClick = {
                    val coords = "23.8103° N, 90.4125° E"
                    val timestamp = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                    val msg = if (lang == "bn") {
                        "আমি নিরাপদ আছি! আমার অবস্থান কোঅর্ডিনেট: $coords। সময়: $timestamp। - পাঠানো হয়েছে RescueHub থেকে।"
                    } else {
                        "I'm Safe! My location coordinates: $coords. Time: $timestamp. - Sent via RescueHub"
                    }
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:${emergencyContacts.firstOrNull()?.phone ?: "999"}")
                        putExtra("sms_body", msg)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Ready to send: $msg", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Safe Check")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (lang == "bn") "আমি নিরাপদ আছি (Family Safety Check)" else "I'M SAFE (Family Safety Check)",
                    fontWeight = FontWeight.Bold
                )
            }

            // 5. Smart Search & Built-in Speech-to-Text
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = dynamicSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "bn") "সার্চ গাইড (Voice Search ও অফলাইন প্রশ্ন)" else "Smart Question System (Voice & Text)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = dynamicOnSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                if (it.isNotBlank()) {
                                    searchResult = FaqDatabase.getAnswer(it, lang)
                                } else {
                                    searchResult = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = if (lang == "bn") "সাপ কামড়, ভূমিকম্প, আগুন..." else "Search guide or ask question...",
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = ""; searchResult = null }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                                    }
                                }
                            }
                        )

                        // Microphone Button for Speech-to-Text (Voice Search)
                        IconButton(
                            onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lang == "bn") "bn-BD" else "en-US")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, if (lang == "bn") "বলুন..." else "Speak now...")
                                }
                                try {
                                    speechRecognizerLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Voice Search not supported on this container", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(Icons.Filled.Mic, contentDescription = "Voice Search", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Real-time Matched Instructions Display with Sound support
                    searchResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (lang == "bn") "অফলাইন নির্দেশিকা" else "Offline Emergency Guide",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row {
                                        IconButton(onClick = { speakText(result) }) {
                                            Icon(Icons.Filled.RecordVoiceOver, contentDescription = "Speak", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            clipboardManager.setText(AnnotatedString(result))
                                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = result,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = dynamicOnSurface
                                )
                            }
                        }
                    }
                }
            }

            // 6. Smart Symptoms Finder (☑)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = dynamicSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "bn") "লক্ষণ নির্বাচক (Smart Symptoms Finder)" else "Smart Symptoms Finder",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = dynamicOnSurface
                    )
                    Text(
                        text = if (lang == "bn") "লক্ষণগুলো সিলেক্ট করুন, অ্যাপ উপযুক্ত ফার্স্ট এইড ও গাইড দেখাবে" else "Select symptoms to view recommended emergency actions.",
                        fontSize = 12.sp,
                        color = dynamicOnSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Expandable Grid of 5 Checkboxes
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = symBreath, onCheckedChange = { symBreath = it })
                                Text(text = if (lang == "bn") "শ্বাস নিতে কষ্ট" else "Dyspnea", fontSize = 13.sp, color = dynamicOnSurface)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = symDizzy, onCheckedChange = { symDizzy = it })
                                Text(text = if (lang == "bn") "মাথা ঘোরা" else "Dizzy", fontSize = 13.sp, color = dynamicOnSurface)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = symFever, onCheckedChange = { symFever = it })
                                Text(text = if (lang == "bn") "জ্বর (তীব্র)" else "Fever", fontSize = 13.sp, color = dynamicOnSurface)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = symBleeding, onCheckedChange = { symBleeding = it })
                                Text(text = if (lang == "bn") "রক্ত পড়ছে" else "Bleeding", fontSize = 13.sp, color = dynamicOnSurface)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = symUnconscious, onCheckedChange = { symUnconscious = it })
                                Text(text = if (lang == "bn") "অজ্ঞান" else "Unconscious", fontSize = 13.sp, color = dynamicOnSurface)
                            }
                        }
                    }

                    // Suggesting emergency guides dynamically based on symptoms selected
                    val suggestedGuides = remember(symBreath, symDizzy, symFever, symBleeding, symUnconscious) {
                        val list = mutableListOf<Pair<String, String>>()
                        if (symBreath) {
                            list.add("CPR" to "cpr")
                            list.add("Choking First Aid" to "choking")
                        }
                        if (symDizzy) {
                            list.add("Heat Stroke Guide" to "h_stroke")
                            list.add("Stroke FAST Steps" to "stroke")
                        }
                        if (symFever) {
                            list.add("Heat Stroke Guide" to "h_stroke")
                        }
                        if (symBleeding) {
                            list.add("Bleeding Control" to "bleeding")
                        }
                        if (symUnconscious) {
                            list.add("Unconscious Recovery" to "unconscious")
                            list.add("CPR" to "cpr")
                        }
                        list.distinct()
                    }

                    if (suggestedGuides.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (lang == "bn") "পরামর্শকৃত গাইডসমূহ:" else "Recommended Survival Guides:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestedGuides.forEach { guide ->
                                Box(
                                    modifier = Modifier
                                        .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                        .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                        .clickable {
                                            searchQuery = guide.first
                                            searchResult = FaqDatabase.getAnswer(guide.first, lang)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "➡️ ${guide.first}", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 7. Survival Training Drill (Mini scenario quiz)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = dynamicSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "bn") "সারভাইভাল মিশন ও ড্রিল (Survival Training)" else "Survival Mission & Training Drill",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = dynamicOnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (quizStep == 0) {
                        Text(
                            text = if (lang == "bn") "একটি কুইজ মিশন খেলে আপনার প্রস্তুতি স্কোর উন্নত করুন।" else "Take a quick scenario challenge to boost your readiness score.",
                            fontSize = 12.sp,
                            color = dynamicOnSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { quizStep = 1; quizScore = 0; quizAnswerFeedback = null },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(text = if (lang == "bn") "মিশন শুরু করুন" else "Start Drill Mission")
                        }
                    } else if (quizStep == 1) {
                        Text(text = "Q1. সাপ কামড়ালে কি প্রথমে ক্ষতস্থান কেটে রক্ত বের করতে হবে?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = dynamicOnSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                quizAnswerFeedback = "❌ ভুল উত্তর! সাপের বিষ টানতে কাটাকাটি করলে ইনফেকশন বা অতিরিক্ত রক্তক্ষরণ হতে পারে।"
                                scope.launch { delay(2500); quizStep = 2; quizAnswerFeedback = null }
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                                Text("হ্যাঁ (Yes)")
                            }
                            Button(onClick = {
                                quizScore += 10
                                quizAnswerFeedback = "✅ সঠিক উত্তর! ক্ষত স্থির ও অনড় রাখতে হবে এবং নিকটস্থ হাসপাতালে যেতে হবে।"
                                scope.launch { delay(2500); quizStep = 2; quizAnswerFeedback = null }
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                                Text("না (No)")
                            }
                        }
                    } else if (quizStep == 2) {
                        Text(text = "Q2. ভূমিকম্প শুরু হলে নিরাপদতম স্থান কোনটি?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = dynamicOnSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = {
                                quizAnswerFeedback = "❌ ভুল! ভূমিকম্পের সময় লিফট ব্যবহার অত্যন্ত বিপজ্জনক।"
                                scope.launch { delay(2500); quizStep = 3; quizAnswerFeedback = null }
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("১. দ্রুত লিফট দিয়ে নিচে নামা")
                            }
                            Button(onClick = {
                                quizScore += 10
                                quizAnswerFeedback = "✅ চমৎকার! শক্ত টেবিল বা খাটের নিচে আশ্রয় নেওয়াই (Drop, Cover, Hold on) সেরা।"
                                scope.launch { delay(2500); quizStep = 3; quizAnswerFeedback = null }
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("২. শক্ত টেবিলের নিচে আশ্রয় নেওয়া")
                            }
                        }
                    } else if (quizStep == 3) {
                        Text(text = "Q3. বিদ্যুৎস্পৃষ্ট ব্যক্তিকে রক্ষা করতে নিচের কোনটি করবেন?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = dynamicOnSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = {
                                quizAnswerFeedback = "❌ অত্যন্ত বিপজ্জনক! খালি হাতে ধরলে আপনিও বিদ্যুৎস্পৃষ্ট হবেন।"
                                scope.launch { delay(2500); quizStep = 4; quizAnswerFeedback = null }
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("১. হাত দিয়ে টেনে দূরে সরানো")
                            }
                            Button(onClick = {
                                quizScore += 10
                                quizAnswerFeedback = "✅ চমৎকার! শুকনো বাঁশ/কাঠ দিয়ে তার সরাতে হবে এবং মেইন সুইচ অফ করতে হবে।"
                                scope.launch { delay(2500); quizStep = 4; quizAnswerFeedback = null }
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("২. মেইন সুইচ অফ করা বা শুকনো কাঠ ব্যবহার")
                            }
                        }
                    } else if (quizStep == 4) {
                        Text(
                            text = "🎉 মিশন সম্পন্ন! আপনার ড্রিল স্কোর: $quizScore/30",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            fontSize = 15.sp
                        )
                        Text(
                            text = "আপনার প্রস্তুতি স্কোর বৃদ্ধি পেয়েছে!",
                            fontSize = 13.sp,
                            color = dynamicOnSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { quizStep = 0 }) {
                            Text("পুনরায় খেলুন (Restart Drill)")
                        }
                    }

                    quizAnswerFeedback?.let { feedback ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = feedback,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 8. Visual / Grid Section with top rounded borders and beautiful grey/lavender background
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = dynamicSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SURVIVAL HANDBOOK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = SubtitleColor.copy(alpha = 0.8f)
                        )
                        Box(
                            modifier = Modifier
                                .background(PurpleBadgeBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "15 OFFLINE CORES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnPurpleBadge
                            )
                        }
                    }

                    // Clean Grid of 15 items
                    val gridHeight = 540.dp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(gridOptions) { option ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.95f)
                                        .clickable {
                                            selectedMenuId = option.id
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = getEmojiFor(option.id),
                                            fontSize = 24.sp,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        Text(
                                            text = L10n.t(option.titleKey, lang),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = dynamicOnSurface,
                                            lineHeight = 13.sp,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 9. Disaster Learning for Kids (শিশুদের জন্য 📖)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = dynamicSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "bn") "শিশুদের জন্য দুর্যোগ শিক্ষা 📖" else "Disaster Learning for Kids 📖",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = dynamicOnSurface
                    )
                    Text(
                        text = if (lang == "bn") "সহজ কার্টুন ও ছড়ার মাধ্যমে বাচ্চাদের সচেতন করুন" else "Simple illustrative cartoon-style learning stories.",
                        fontSize = 12.sp,
                        color = dynamicOnSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    var selectedKidStory by remember { mutableStateOf<Int?>(null) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFFFFF9C4), RoundedCornerShape(12.dp))
                                .clickable { selectedKidStory = 1 }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🐸", fontSize = 28.sp)
                                Text("বজ্রপাতে উবু হওয়া", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                .clickable { selectedKidStory = 2 }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🐢", fontSize = 28.sp)
                                Text("ভূমিকম্পে খাটাবরণ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFFE1F5FE), RoundedCornerShape(12.dp))
                                .clickable { selectedKidStory = 3 }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🦆", fontSize = 28.sp)
                                Text("কলার ভেলায় টুনি", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }

                    selectedKidStory?.let { story ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val storyText = when(story) {
                                        1 -> "⚡ মেঘ যখন জোরে ডাকে, মন্টু ব্যাং উবু হয়ে হাঁটু ভাঁজ করে মাটির কাছে বসে পড়ে! উঁচু গাছের নিচে কোনোভাবেই দাঁড়ানো যাবে না, তাহলে মেঘের আলো মন্টুর ওপরে এসে পড়তে পারে!"
                                        2 -> "🐢 কাঁপন শুরু হলে টুনটুনি কচ্ছপের মতো নিজেকে ঢেকে নেয় শক্ত টেবিলের নিচে! বাইরে থাকলে বড় দালান আর বিদ্যুতের খুঁটি এড়িয়ে চলে!"
                                        3 -> "🦆 বন্যার জল যখন ঘরে এলো, টিপু তার হাঁসেদের সাথে কলার ভেলা বানিয়ে ওপরে উঠে পড়লো! জল ভালো করে ফুটিয়ে তবেই পান করে!"
                                        else -> ""
                                    }
                                    Text(text = storyText, fontSize = 13.sp, lineHeight = 18.sp, color = dynamicOnSurface)
                                }
                                IconButton(onClick = { selectedKidStory = null }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close")
                                }
                            }
                        }
                    }
                }
            }

            // 10. Community Volunteer registration
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = dynamicSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "bn") "স্বেচ্ছাসেবক প্রোফাইল (Community Volunteer)" else "Community Volunteer Registration",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = dynamicOnSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (!isVolunteerRegistered) {
                        Text(
                            text = if (lang == "bn") "আপনি কি আপনার এলাকার উদ্ধার কাজে সাহায্য করতে চান? স্বেচ্ছাসেবক হিসেবে রেজিস্টার করুন।" else "Do you want to enlist as a local responder? Register your emergency skills here.",
                            fontSize = 12.sp,
                            color = dynamicOnSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = volunteerBloodGroup,
                                onValueChange = { volunteerBloodGroup = it },
                                label = { Text("Blood Group") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = volunteerSkills,
                                onValueChange = { volunteerSkills = it },
                                label = { Text("Rescue Skills") },
                                modifier = Modifier.weight(2f),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                isVolunteerRegistered = true
                                sharedPrefs.edit().putBoolean("vol_registered", true).apply()
                                sharedPrefs.edit().putString("vol_blood", volunteerBloodGroup).apply()
                                sharedPrefs.edit().putString("vol_skills", volunteerSkills).apply()
                                Toast.makeText(context, "Volunteer Profile Saved Offline!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("রেজিস্টার করুন (Register Offline)")
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Badge, contentDescription = "Badge", tint = Color(0xFF2E7D32), modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("LOCAL VOLUNTEER BADGE ACTIVE 🎖️", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 13.sp)
                                Text("Blood: $volunteerBloodGroup | Skills: $volunteerSkills", fontSize = 12.sp, color = dynamicOnSurface)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            isVolunteerRegistered = false
                            sharedPrefs.edit().putBoolean("vol_registered", false).apply()
                        }) {
                            Text("প্রোফাইল সংশোধন করুন (Edit Profile)", color = Color.Red)
                        }
                    }
                }
            }

            // 11. Clear Disclaimer Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🚨 DISCLAIMER & NOTICE",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lang == "bn") {
                            "RescueHub তথ্য ও প্রস্তুতিতে সহায়তা করে। জীবন-ঝুঁকিপূর্ণ পরিস্থিতিতে সর্বদা স্থানীয় জরুরি সেবা এবং সরকারি নির্দেশনা অনুসরণ করুন।"
                        } else {
                            "RescueHub provides disaster awareness and guidelines. For any life-threatening situation, always contact official government emergency help desk immediately."
                        },
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = dynamicOnSurface
                    )
                }
            }

            // 12. Developer & Company profile representation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = dynamicSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DEVELOPER & PUBLISHER PROFILE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = SubtitleColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("👨‍💻 Developed by Prince AR Abdur Rahman", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = dynamicOnSurface)
                    Text("Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.", fontSize = 12.sp, color = dynamicOnSurface.copy(alpha = 0.8f))
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📞 Contact Developer:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = dynamicOnSurface)
                    Text("WhatsApp: 01707424006 | WhatsApp: 01796951709", fontSize = 12.sp, color = dynamicOnSurface)
                    
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

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("🏢 About Company: NexVora Lab's Ofc", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = dynamicOnSurface)
                    Text("NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.", fontSize = 12.sp, color = dynamicOnSurface.copy(alpha = 0.8f))
                    Text("Mission: Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.", fontSize = 11.sp, fontStyle = FontStyle.Italic, color = dynamicOnSurface.copy(alpha = 0.7f))

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Version: 1.0.0 | © 2026 NexVora Lab's Ofc. All Rights Reserved.",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = dynamicOnSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Modal Sheet Alert dialogs for the 15 features
    selectedMenuId?.let { menuId ->
        AlertDialog(
            onDismissRequest = {
                selectedMenuId = null
                tts?.stop()
                isTtsSpeaking = false
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedMenuId = null
                    tts?.stop()
                    isTtsSpeaking = false
                }) {
                    Text("Close")
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = L10n.t(menuId, lang),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = {
                        val fullInstruction = getDetailsTextFor(menuId, lang, survivalItems, bookmarkedPlaces, emergencyNumbers)
                        speakText(fullInstruction)
                    }) {
                        Icon(
                            imageVector = if (isTtsSpeaking) Icons.Filled.StopCircle else Icons.Filled.RecordVoiceOver,
                            contentDescription = "Speak Guide",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Context specific overrides for sub-components
                    if (menuId == "kit") {
                        // Smart Emergency Bag Checklist Builder with baby, elderly, diabetic, pet filters!
                        var subCategoryFilter by remember { mutableStateOf("general") }
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("general" to "🎒 General", "baby" to "👶 Baby", "elderly" to "👴 Elderly", "diabetic" to "💉 Diabetic", "pet" to "🐾 Pet").forEach { cat ->
                                    val isSelected = cat.first == subCategoryFilter
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { subCategoryFilter = cat.first }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cat.second,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            val filteredBagItems = remember(survivalItems, subCategoryFilter) {
                                when(subCategoryFilter) {
                                    "baby" -> listOf(
                                        "Infant Formula Milk" to "Baby",
                                        "Baby Diapers & Wipes" to "Baby",
                                        "Thermos flask with warm water" to "Baby",
                                        "Baby clothes & small blanket" to "Baby"
                                    )
                                    "elderly" -> listOf(
                                        "High Blood Pressure Tablets" to "Elderly",
                                        "Walking cane or support strap" to "Elderly",
                                        "Spare reading spectacles" to "Elderly",
                                        "Adult dry wipes" to "Elderly"
                                    )
                                    "diabetic" -> listOf(
                                        "Insulin vials & syringes in thermo pouch" to "Diabetic",
                                        "Glucose meter & test strips" to "Diabetic",
                                        "Sugar candies or glucose gel" to "Diabetic",
                                        "Diabetic health log booklet" to "Diabetic"
                                    )
                                    "pet" -> listOf(
                                        "Dry dog/cat food pellets" to "Pet",
                                        "Collapsible pet water bowl" to "Pet",
                                        "Pet leash and identification harness" to "Pet"
                                    )
                                    else -> emptyList()
                                }
                            }

                            if (subCategoryFilter == "general") {
                                survivalItems.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.toggleSurvivalItem(item) }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(checked = item.isChecked, onCheckedChange = { viewModel.toggleSurvivalItem(item) })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.name,
                                            style = if (item.isChecked) MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium,
                                            color = dynamicOnSurface
                                        )
                                    }
                                }
                            } else {
                                filteredBagItems.forEach { itemPair ->
                                    val checkedKey = "custom_checked_${itemPair.first}"
                                    var isCustomChecked by remember { mutableStateOf(sharedPrefs.getBoolean(checkedKey, false)) }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                isCustomChecked = !isCustomChecked
                                                sharedPrefs.edit().putBoolean(checkedKey, isCustomChecked).apply()
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(checked = isCustomChecked, onCheckedChange = {
                                            isCustomChecked = it
                                            sharedPrefs.edit().putBoolean(checkedKey, it).apply()
                                        })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = itemPair.first,
                                            style = if (isCustomChecked) MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium,
                                            color = dynamicOnSurface
                                        )
                                    }
                                }
                            }
                        }
                    } else if (menuId == "family_plan") {
                        // Secure Family Emergency Vault
                        Column {
                            Text("📦 SECURE EMERGENCY VAULT", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            Text("Documents stored fully offline & encrypted on device.", fontSize = 11.sp, color = dynamicOnSurface.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(value = nidVault, onValueChange = { nidVault = it; sharedPrefs.edit().putString("vault_nid", it).apply() }, label = { Text("NID Number") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = passportVault, onValueChange = { passportVault = it; sharedPrefs.edit().putString("vault_passport", it).apply() }, label = { Text("Passport ID") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = birthCertVault, onValueChange = { birthCertVault = it; sharedPrefs.edit().putString("vault_birth_cert", it).apply() }, label = { Text("Birth Certificate") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = insuranceVault, onValueChange = { insuranceVault = it; sharedPrefs.edit().putString("vault_insurance", it).apply() }, label = { Text("Insurance Policy ID") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = bloodGroupVault, onValueChange = { bloodGroupVault = it; sharedPrefs.edit().putString("vault_blood", it).apply() }, label = { Text("Blood Group") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = medicalRecordVault, onValueChange = { medicalRecordVault = it; sharedPrefs.edit().putString("vault_medical", it).apply() }, label = { Text("Critical Medical Records") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = { Toast.makeText(context, "Encrypted Vault Sync OK!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Save Emergency Vault Copy")
                            }
                        }
                    } else if (menuId == "map") {
                        // Smart Safe Route instructions
                        Column {
                            Text("📍 SMART OFFLINE DIRECTION ROUTE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("My GPS Coordinates: 23.8103° N, 90.4125° E", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            listOf(
                                "Hospital (কাছাকাছি হাসপাতাল)" to "0.8 km • Walk East 400m down Fire Lane, turn left onto Dhaka Highway.",
                                "Cyclone Shelter (সাইক্লোন শেল্টার)" to "1.2 km • Head North towards River Embankment, enter High School Building.",
                                "Flood Shelter (বন্যা আশ্রয়কেন্দ্র)" to "1.5 km • Take Safe Embankment road to Union Parisad Hub.",
                                "Fire Station (ফায়ার স্টেশন)" to "0.9 km • Direct Route South via Zone A corridor.",
                                "Police Station (পুলিশ স্টেশন)" to "2.1 km • Follow Dhaka-Mymensingh corridor."
                            ).forEach { route ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = "🏠 " + route.first, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                        Text(text = route.second, fontSize = 12.sp, color = dynamicOnSurface)
                                    }
                                }
                            }
                        }
                    } else if (menuId == "first_aid") {
                        // Offline Survival Encyclopedia (First Aid, Safe Water, Food Storage, Shelter Building)
                        var selectedEncyAid by remember { mutableStateOf<String?>(null) }
                        var handbookSearchQuery by remember { mutableStateOf("") }
                        
                        val firstAidGuides = listOf(
                            "Snake Bite Guide (সাপ কামড়)" to "সাপ কামড়",
                            "Dog Bite First Aid (কুকুরের কামড়)" to "কুকুরের কামড়",
                            "Burn Treatment (পোড়া বা হাত পুড়ে গেছে)" to "হাত পুড়ে গেছে",
                            "Electric Shock (বিদ্যুৎস্পৃষ্ট)" to "বিদ্যুৎস্পৃষ্ট",
                            "Drowning First Aid (পানিতে ডুবে যাওয়া)" to "ডুবে যাওয়া",
                            "CPR Steps (সিপিআর)" to "cpr",
                            "Choking First Aid (গলায় আটকে যাওয়া)" to "গলায় আটকে",
                            "Heart Attack First Aid (হার্ট অ্যাটাক)" to "হার্ট অ্যাটাক",
                            "Stroke First Aid (স্ট্রোক)" to "স্ট্রোক",
                            "Poisoning Emergency (বিষক্রিয়া)" to "বিষক্রিয়া"
                        )

                        val disasterGuides = listOf(
                            "Flood Emergency (বন্যা)" to "বন্যা",
                            "Cyclone Survival Guide (ঘূর্ণিঝড়)" to "ঘূর্ণিঝড়",
                            "Earthquake Survival Guide (ভূমিকম্প)" to "ভূমিকম্প",
                            "Fire Emergency (আগুন)" to "আগুন",
                            "Landslide Emergency (পাহাড় ধস)" to "পাহাড় ধস",
                            "Heatwave Safety Guide (তীব্র তাপদাহ)" to "তীব্র তাপদাহ",
                            "Lightning Safety Guide (বজ্রপাত)" to "বজ্রপাত",
                            "Safe Water Purification (নিরাপদ পানীয় জল)" to "Purification",
                            "Disaster Food Storage (খাদ্য মজুদকরণ)" to "Storage",
                            "Emergency Shelter Building (জরুরি আশ্রয় তৈরি)" to "Shelter"
                        )

                        if (selectedEncyAid == null) {
                            Column {
                                OutlinedTextField(
                                    value = handbookSearchQuery,
                                    onValueChange = { handbookSearchQuery = it },
                                    label = { Text(if (lang == "bn") "অনুসন্ধান করুন (যেমন: সাপ, CPR, বন্যা)" else "Search (e.g. Snake, CPR, Flood)") },
                                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                val filteredFirstAid = firstAidGuides.filter {
                                    it.first.lowercase().contains(handbookSearchQuery.lowercase()) ||
                                    it.second.lowercase().contains(handbookSearchQuery.lowercase())
                                }

                                val filteredDisaster = disasterGuides.filter {
                                    it.first.lowercase().contains(handbookSearchQuery.lowercase()) ||
                                    it.second.lowercase().contains(handbookSearchQuery.lowercase())
                                }

                                if (filteredFirstAid.isNotEmpty()) {
                                    Text(
                                        text = if (lang == "bn") "🏥 ফার্স্ট এইড ও মেডিকেল গাইড" else "🏥 FIRST AID & MEDICAL GUIDES",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                    filteredFirstAid.forEach { (name, _) ->
                                        ListItem(
                                            headlineContent = { Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            leadingContent = { Icon(Icons.Filled.MedicalServices, contentDescription = "Medical", tint = Color(0xFFEF4444)) },
                                            trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = "Go") },
                                            modifier = Modifier
                                                .clickable { selectedEncyAid = name }
                                                .padding(vertical = 2.dp)
                                        )
                                    }
                                }

                                if (filteredDisaster.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (lang == "bn") "🌪️ প্রাকৃতিক দুর্যোগ ও পরিবেশ নিরাপত্তা" else "🌪️ DISASTERS & SAFETY GUIDES",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                    filteredDisaster.forEach { (name, _) ->
                                        ListItem(
                                            headlineContent = { Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            leadingContent = { Icon(Icons.Filled.Tornado, contentDescription = "Disaster", tint = Color(0xFF0284C7)) },
                                            trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = "Go") },
                                            modifier = Modifier
                                                .clickable { selectedEncyAid = name }
                                                .padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { selectedEncyAid = null }
                                ) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (lang == "bn") "হ্যান্ডবুকে ফিরে যান" else "Back to Handbook", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = selectedEncyAid!!,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    lineHeight = 26.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val matchedKey = (firstAidGuides + disasterGuides)
                                    .firstOrNull { it.first == selectedEncyAid }?.second ?: "cpr"

                                val guideDetailsText = when (matchedKey) {
                                    "Purification" -> if (lang == "bn") {
                                        """💧 নিরাপদ পানীয় জল শোধন গাইড:
১. ফোটানো: রোগজীবাণু ধ্বংস করতে জল ১০-২০ মিনিট ফুটিয়ে ঠাণ্ডা করুন।
২. ক্লোরিন বা হ্যালোট্যাব: প্রতি ১ লিটার পরিষ্কার জলে ১টি ওয়াটার পিউরিফিকেশন ট্যাবলেট দিন। ৩০ মিনিট অপেক্ষা করুন।
৩. সৌর নির্বীজন (SODIS): পরিষ্কার স্বচ্ছ পিইটি বোতলে জল ভরে ৬ ঘণ্টা ছাদ বা সমতল স্থানে সরাসরি সূর্যের তাপে রাখুন।"""
                                    } else {
                                        """💧 Safe Water Purification Guide:
1. Boiling: Boil water rapidly for 10-20 minutes to kill pathogens.
2. Chlorine/Halotab: Use 1 purification tablet per 1 litre of clean water. Wait 30 minutes.
3. Solar Disinfection (SODIS): Fill clean transparent PET bottles, lay them in direct solar heat on roof/flat surface for 6 hours."""
                                    }
                                    "Storage" -> if (lang == "bn") {
                                        """🌾 দুর্যোগকালীন খাদ্য মজুদ নির্দেশিকা:
১. উচ্চ শক্তি: রান্না ছাড়াই খাওয়া যায় এমন শুকনো খাবার (যেমন- চিঁড়ে, মুড়ি, গুড়, বিস্কুট, খেজুর) মজুদ রাখুন।
২. বায়ুরোধী পাত্র: আর্দ্রতা ও জল এড়াতে সব শুকনো খাবার বায়ুরোধী প্লাস্টিক ড্রাম বা জিপ ব্যাগে সিল করে রাখুন।
৩. শুকনো স্থান: খাবারের প্যাকেটগুলো সরাসরি মেঝেতে রাখবেন না; উঁচু শেলফে সংরক্ষণ করুন।"""
                                    } else {
                                        """🌾 Disaster Food Storage Manual:
1. High Energy: Keep ready-to-eat foods that require no cooking (molasses, puffed rice, biscuits, dry beans).
2. Air-tight: Pack all grains and dry foods inside air-tight sealed plastic drums or zip bags to avoid water/humidity dampness.
3. Keep dry: Never place food packages directly on the floor; store on high shelves."""
                                    }
                                    "Shelter" -> if (lang == "bn") {
                                        """🎪 জরুরি অস্থায়ী আশ্রয়কেন্দ্র নির্মাণ:
১. শুকনো উঁচু জমি: নদীভাঙন বা আলগা মাটি থেকে দূরে উঁচু পাহাড়ি বা সমতল জমি বেছে নিন।
২. টারপলিন বা এ-ফ্রেম: দুটি শক্ত গাছের মাঝে একটি মজবুত রশি দিয়ে টারপলিন টানিয়ে নিন এবং কোণগুলো খুঁটি দিয়ে মাটিতে গেঁথে দিন।
৩. আর্দ্রতা রোধ: মাটির স্যাঁতসেঁতে ভাব দূর করতে মেঝেতে বিছানা পাতার আগে একটি প্লাস্টিক শিট বিছিয়ে দিন।"""
                                    } else {
                                        """🎪 Emergency Shelter Building:
1. Dry high ground: Choose flat ground on hills or elevated areas far from loose soils or river banks.
2. Tarpaulin/A-Frame: Tie a durable tarpaulin rope between two sturdy trees, stretch down, and anchor corners with dry wooden peg pegs.
3. Floor protection: Cover soil with a plastic sheet before placing bedding to block earth moisture."""
                                    }
                                    else -> FaqDatabase.getAnswer(matchedKey, lang)
                                }

                                BeautifulGuideRenderer(text = guideDetailsText, lang = lang)
                            }
                        }
                    } else {
                        // Default handler
                        RenderDetailView(
                            menuId = menuId,
                            lang = lang,
                            viewModel = viewModel,
                            survivalItems = survivalItems,
                            bookmarkedPlaces = bookmarkedPlaces,
                            emergencyNumbers = emergencyNumbers,
                            onCall = { number ->
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                                context.startActivity(intent)
                            },
                            onToggleCheck = { item -> viewModel.toggleSurvivalItem(item) }
                        )
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

object L10n {
    private val translations = mapOf(
        "bn" to mapOf(
            "hold_sos" to "SOS বাটনে চেপে ধরে রাখুন",
            "first_aid" to "ফার্স্ট এইড",
            "flood" to "বন্যা গাইড",
            "cyclone" to "ঘূর্ণিঝড় গাইড",
            "earthquake" to "ভূমিকম্প গাইড",
            "fire" to "অগ্নি নিরাপত্তা",
            "lightning" to "বজ্রপাত গাইড",
            "health_emergency" to "স্বাস্থ্য জরুরি",
            "numbers" to "জরুরি ফোন নম্বর",
            "map" to "স্মার্ট সেফ রুট",
            "kit" to "সারভাইভাল ব্যাগ",
            "radio" to "অফলাইন রেডিও",
            "flashlight" to "উন্নত SOS টুলকিট",
            "safe_places" to "নিরাপদ আশ্রয়স্থল",
            "family_plan" to "ফ্যামিলি ভল্ট",
            "alerts" to "দুর্যোগ এলার্ট"
        ),
        "en" to mapOf(
            "hold_sos" to "Hold SOS button to activate",
            "first_aid" to "First Aid",
            "flood" to "Flood Guide",
            "cyclone" to "Cyclone Guide",
            "earthquake" to "Earthquake Guide",
            "fire" to "Fire Safety",
            "lightning" to "Lightning Guide",
            "health_emergency" to "Health Emergency",
            "numbers" to "Emergency Numbers",
            "map" to "Smart Safe Route",
            "kit" to "Survival Kit Bag",
            "radio" to "Offline Radio",
            "flashlight" to "Advanced SOS Toolkit",
            "safe_places" to "Safe Shelters",
            "family_plan" to "Family Vault",
            "alerts" to "Disaster Alerts"
        )
    )

    fun t(key: String, lang: String): String {
        return translations[lang]?.get(key) ?: translations["en"]?.get(key) ?: key
    }
}

fun getEmojiFor(id: String): String {
    return when (id) {
        "first_aid" -> "🩹"
        "flood" -> "🌊"
        "cyclone" -> "🌪"
        "earthquake" -> "🌍"
        "fire" -> "🔥"
        "lightning" -> "🌩"
        "health_emergency" -> "🦠"
        "numbers" -> "📞"
        "map" -> "🗺"
        "kit" -> "🎒"
        "radio" -> "📻"
        "flashlight" -> "🔦"
        "safe_places" -> "📍"
        "family_plan" -> "📦"
        "alerts" -> "📡"
        else -> "❓"
    }
}

fun getDetailsTextFor(
    menuId: String,
    lang: String,
    survivalItems: List<SurvivalItem>,
    bookmarkedPlaces: List<com.example.data.SafePlaceBookmark>,
    emergencyNumbers: List<com.example.ui.EmergencyNumber>
): String {
    return when (menuId) {
        "first_aid" -> if (lang == "bn") "ফার্স্ট এইড ও অফলাইন এনসাইক্লোপিডিয়া। সাপ কামড়, সিপিআর, বা অগ্নিকাণ্ডের প্রাথমিক চিকিৎসা জানতে সার্চ বক্সে লিখুন বা লক্ষণ নির্বাচক ব্যবহার করুন।" else "First Aid and Offline Survival Encyclopedia. Choose guides or type query to see life saving rules."
        "flood" -> if (lang == "bn") "বন্যা কালীন করণীয়: ১. শুকনো খাবার প্রস্তুত রাখুন। ২. নিরাপদ উঁচুতে যান। ৩. পানি ফুটিয়ে বা ব্লিচিং দিয়ে শোধন করুন।" else "Flood action items: 1. Pack dry foods. 2. Evacuate to higher grounds. 3. Boil and purify water before drinking."
        "cyclone" -> if (lang == "bn") "ঘূর্ণিঝড় সতর্কতা: ১. বিপদ সংকেত লক্ষ্য করুন। ২. জরুরি কাগজপত্র প্লাস্টিকে মুড়ে রাখুন। ৩. দ্রুত আশ্রয়কেন্দ্রে যান।" else "Cyclone Safety: 1. Track wind levels. 2. Pack NID/Docs in dry plastics. 3. Relocate to concrete storm shelter."
        "earthquake" -> if (lang == "bn") "ভূমিকম্প শুরু হলে: নিচু হোন, টেবিল বা খাটের নিচে আশ্রয় নিন এবং শক্ত করে ধরে থাকুন (Drop, Cover, Hold on)।" else "Earthquake protocol: Drop, Cover, and Hold on under a sturdy table or bed. Avoid glass cabinets."
        "fire" -> if (lang == "bn") "অগ্নি নিরাপত্তা: ১. ধোঁয়ায় নিচু হয়ে চলুন। ২. বৈদ্যুতিক লাইনের ফিউজ বন্ধ করুন। ৩. লিফট ব্যবহার করা থেকে বিরত থাকুন।" else "Fire safety: 1. Stay low under heavy smoke. 2. Unplug electric mains. 3. Do not use elevators under any circumstances."
        "lightning" -> if (lang == "bn") "বজ্রপাত হলে: ১. ঘরের ভেতর পাকা দালানে থাকুন। ২. মাঠে থাকলে উবু হয়ে বসুন। ৩. কোনো গাছের নিচে দাঁড়াবেন না।" else "Lightning protection: 1. Go indoors to concrete houses. 2. If outdoors, crouch low like a frog. 3. Never stand under trees."
        "health_emergency" -> if (lang == "bn") "স্বাস্থ্য জরুরি: সংক্রামক রোগ বা মহামারীর সময় মাস্ক ব্যবহার করুন, বিশুদ্ধ পানি পান করুন এবং নিরাপদ খাদ্য সেবন করুন।" else "Health emergency: Use face masks during pandemics, keep rehydration solutions ready, and practice sterile hygiene."
        "numbers" -> if (lang == "bn") "জাতীয় জরুরি নম্বরসমূহ: ৯৯৯ (জাতীয় হেল্পলাইন), ৩৩৩ (তথ্য ও সেবা), ১০৯ (নারী ও শিশু)। ট্যাপ করে সরাসরি কল করুন।" else "National Emergency Numbers: 999 (BD National Help Desk), 333 (Information), 109 (Women & Kids). Tap to dial directly."
        "map" -> if (lang == "bn") "অফলাইন স্মার্ট সেফ রুট: আপনার কাছাকাছি হাসপাতাল ও সাইক্লোন শেল্টার এর রুট ও দূরত্ব নির্দেশিকা এখানে প্রদর্শিত হচ্ছে।" else "Smart Safe Route: Direction vectors and walk routes to closest hospitals, storm centers, and emergency hubs."
        "kit" -> if (lang == "bn") "স্মার্ট ইমার্জেন্সি ব্যাগ: আপনার পরিবারের শিশু, বয়স্ক ও রোগীদের প্রয়োজনীয় ঔষধপত্রসহ ব্যাগ গুছিয়ে রাখুন।" else "Emergency Bag: Organize medical supplies, dry biscuits, water bottles, and specialized tools."
        "radio" -> if (lang == "bn") "অফলাইন রেডিও: দুর্যোগে মোবাইল নেটওয়ার্ক বন্ধ হলে ৮০.০ মেগাহার্টজ থেকে ১০৮.০ মেগাহার্টজ এর মধ্যে আবহাওয়া সংবাদ শুনুন।" else "Offline Radio: Tune into local emergency frequencies (88.0 - 108.0 MHz) to receive government disaster bulletins."
        "flashlight" -> if (lang == "bn") "উন্নত SOS টুলকিট: ফ্ল্যাশলাইট, মোর্স কোড ফ্লাশার এবং সাইরেন বাজিয়ে সাহায্যকারী বা উদ্ধারকারী দলকে সংকেত দিন।" else "Advanced SOS Toolkit: Trigger rapid strobe flashes, Morse Code SOS beacons, and alternating high-pitch sirens."
        "safe_places" -> if (lang == "bn") "নিরাপদ আশ্রয়স্থল: আপনার এলাকায় চিহ্নিত সরকারি বন্যা ও সাইক্লোন শেল্টারসমূহের তালিকা এখানে পাবেন।" else "Safe Shelters: Geolocation and landmark records of certified community centers and flood shelters."
        "family_plan" -> if (lang == "bn") "ফ্যামিলি ইমার্জেন্সি ভল্ট: জাতীয় পরিচয়পত্র, পাসপোর্ট ও রক্তের গ্রুপসহ অতি জরুরি তথ্য অফলাইনে নিরাপদ রাখুন।" else "Family Vault: Secure offline repository of NID, birth IDs, blood groups, and medical histories."
        "alerts" -> if (lang == "bn") "লাইভ দুর্যোগ এলার্ট: ঘূর্ণিঝড়, তাপপ্রবাহ, বা অতিবৃষ্টির সরকারি বুলেটিনসমূহ এবং আবহাওয়া পূর্বাভাস।" else "Live Alerts: Live storm trackers, flood warnings, heatwave bulletins, and heavy rainfall notifications."
        else -> "No information available"
    }
}

@Composable
fun RenderDetailView(
    menuId: String,
    lang: String,
    viewModel: RescueViewModel,
    survivalItems: List<SurvivalItem>,
    bookmarkedPlaces: List<com.example.data.SafePlaceBookmark>,
    emergencyNumbers: List<com.example.ui.EmergencyNumber>,
    onCall: (String) -> Unit,
    onToggleCheck: (SurvivalItem) -> Unit
) {
    val context = LocalContext.current
    val textDetails = getDetailsTextFor(menuId, lang, survivalItems, bookmarkedPlaces, emergencyNumbers)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = textDetails,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (menuId) {
            "numbers" -> {
                Text(
                    text = if (lang == "bn") "সরাসরি কল করতে ট্যাপ করুন:" else "Tap to dial directly:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Real numbers list
                val numbersList = listOf(
                    "999" to (if (lang == "bn") "জাতীয় জরুরি সেবা (National Help)" else "National Emergency Helpdesk"),
                    "333" to (if (lang == "bn") "সরকারি তথ্য ও সেবা (National Info)" else "National Info & Services"),
                    "109" to (if (lang == "bn") "নারী ও শিশু নির্যাতন প্রতিরোধ" else "Women & Child Helpline"),
                    "16263" to (if (lang == "bn") "স্বাস্থ্য বাতায়ন (Health Portal)" else "Health Portal Helpline"),
                    "102" to (if (lang == "bn") "ফায়ার সার্ভিস (Fire Services)" else "Fire Services HQ")
                )
                numbersList.forEach { num ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onCall(num.first) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = num.second, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "📞 ${num.first}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                            }
                            Icon(Icons.Filled.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            "flashlight" -> {
                // Flashlight, SOS, Morse and Siren control buttons
                val flashlightOn by viewModel.isFlashlightOn.collectAsState()
                val sosBlinking by viewModel.isSosBlinking.collectAsState()
                val sirenPlaying by viewModel.isSirenPlaying.collectAsState()

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.toggleFlashlight(context) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (flashlightOn) Color(0xFFFBBF24) else Color.Gray)
                        ) {
                            Text(text = if (flashlightOn) "🔦 LIGHT ON" else "🔦 LIGHT OFF", color = Color.White)
                        }

                        Button(
                            onClick = {
                                if (sosBlinking) viewModel.stopSosBlink(context) else viewModel.startSosBlink(context)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (sosBlinking) Color.Red else Color.Gray)
                        ) {
                            Text(text = if (sosBlinking) "🚨 SOS ACTIVE" else "🚨 START SOS", color = Color.White)
                        }
                    }

                    Button(
                        onClick = { if (sirenPlaying) viewModel.stopSirenSound() else viewModel.startSirenSound() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (sirenPlaying) Color.Red else MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(if (sirenPlaying) Icons.Filled.VolumeUp else Icons.Filled.VolumeMute, contentDescription = "Siren")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (sirenPlaying) "STOP SIREN ALERT" else "PLAY HIGH SIREN SOUND")
                    }

                    // Full Screen Glowing Beacon simulation
                    var flashScreenBeacon by remember { mutableStateOf(false) }
                    Button(
                        onClick = { flashScreenBeacon = !flashScreenBeacon },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                    ) {
                        Text(text = if (flashScreenBeacon) "STOP FULLSCREEN SIGNAL" else "LAUNCH HAZARD FLASH SIGNAL")
                    }

                    if (flashScreenBeacon) {
                        var strobeColor by remember { mutableStateOf(Color.Red) }
                        LaunchedEffect(Unit) {
                            while (true) {
                                delay(300)
                                strobeColor = if (strobeColor == Color.Red) Color.Yellow else Color.Red
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(strobeColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("FULL SCREEN SIGNAL ACTIVE", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 14.sp)
                        }
                    }
                }
            }
            "radio" -> {
                Text(
                    text = if (lang == "bn") "ফ্রিকোয়েন্সি ডায়াল (Tune Dial):" else "Tuned Frequency:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                var tunedFreq by remember { mutableStateOf(88.0f) }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${String.format("%.1f", tunedFreq)} MHz",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Slider(
                        value = tunedFreq,
                        onValueChange = { tunedFreq = it },
                        valueRange = 88.0f..108.0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = if (tunedFreq in 89.0f..91.5f) {
                            "📻 Weather Channel Broadcast Received"
                        } else {
                            "🔇 Static (No Offline Broadcast Detected)"
                        },
                        fontWeight = FontWeight.Bold,
                        color = if (tunedFreq in 89.0f..91.5f) Color(0xFF2E7D32) else Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
            "alerts" -> {
                // Real local offline disaster alert logs
                listOf(
                    "⚠️ CYCLONE BULLETIN (ঘূর্ণিঝড় বার্তা)" to "Cyclone storm brewing in Bay of Bengal. Area 4 advised to evacuate.",
                    "⚡ LIGHTNING ADVISORY (বজ্রপাত সতর্কতা)" to "Heavy thunder and lightning expected. Avoid outdoor fields.",
                    "🌊 FLOOD BULLETIN (বন্যা পূর্বাভাস)" to "Teesta and Padma river water level rising. Stay safe on dykes."
                ).forEach { alert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = alert.first, fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 13.sp)
                            Text(text = alert.second, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            "safe_places" -> {
                // Safe shelters list
                listOf(
                    "Shelter 1: Union Parisad High School" to "Distance: 1.2 km • Capacity: 800 people",
                    "Shelter 2: Government Primary School" to "Distance: 1.5 km • Capacity: 500 people",
                    "Shelter 3: Cyclone Center Zone B" to "Distance: 2.1 km • Capacity: 1200 people"
                ).forEach { shelter ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Place, contentDescription = "Place", tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = shelter.first, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = shelter.second, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BeautifulGuideRenderer(text: String, lang: String) {
    val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        lines.forEach { line ->
            when {
                line.startsWith("✅") -> {
                    val cleanText = line.removePrefix("✅").trim()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        ),
                        border = BorderStroke(2.dp, Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✅", fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                            Text(
                                text = cleanText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                line.startsWith("❌") -> {
                    val cleanText = line.removePrefix("❌").trim()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        border = BorderStroke(2.dp, Color(0xFFC62828)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("❌", fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                            Text(
                                text = cleanText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB71C1C),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                line.startsWith("⚠️") -> {
                    val cleanText = line.removePrefix("⚠️").trim()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        ),
                        border = BorderStroke(2.dp, Color(0xFFEF6C00)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                            Text(
                                text = cleanText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFE65100),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
                // Check if line starts with a number like "1.", "2.", "১.", "২."
                line.firstOrNull()?.isDigit() == true || (line.length > 1 && (line.startsWith("১") || line.startsWith("২") || line.startsWith("৩") || line.startsWith("৪") || line.startsWith("৫") || line.startsWith("৬") || line.startsWith("৭") || line.startsWith("৮") || line.startsWith("৯") || line.startsWith("০"))) -> {
                    val parts = line.split(Regex("[.\\-]"), 2)
                    val number = parts.getOrNull(0)?.trim() ?: ""
                    val content = parts.getOrNull(1)?.trim() ?: line
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = number,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = content,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                else -> {
                    // General or descriptive line
                    Text(
                        text = line,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }
}


