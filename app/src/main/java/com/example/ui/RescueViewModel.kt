package com.example.ui

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.data.api.Content
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiRequest
import com.example.data.api.Part
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class EmergencyNumber(
    val title: String,
    val number: String,
    val iconName: String
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class RescueViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("rescue_hub_prefs", Context.MODE_PRIVATE)
    private val database = RescueDatabase.getDatabase(application)
    private val repository = RescueRepository(database)

    // Onboarding / Settings States
    val language = MutableStateFlow(sharedPrefs.getString("language", "en") ?: "en")
    val country = MutableStateFlow(sharedPrefs.getString("country", "🇧🇩 Bangladesh") ?: "🇧🇩 Bangladesh")
    val onboardingCompleted = MutableStateFlow(sharedPrefs.getBoolean("onboarding_completed", false))
    val isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", true))

    // SOS & Status States
    val isEmergencyMode = MutableStateFlow(sharedPrefs.getBoolean("emergency_mode", false))
    val isFlashlightOn = MutableStateFlow(false)
    val isSosBlinking = MutableStateFlow(false)
    val isSirenPlaying = MutableStateFlow(false)

    // Room Database Flow Observers
    val medicalProfile: StateFlow<MedicalProfile?> = repository.medicalProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val emergencyContacts: StateFlow<List<EmergencyContact>> = repository.emergencyContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val survivalItems: StateFlow<List<SurvivalItem>> = repository.survivalItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diaryEntries: StateFlow<List<DiaryEntry>> = repository.diaryEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedSafePlaces: StateFlow<List<SafePlaceBookmark>> = repository.bookmarkedSafePlaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _emergencyNumbers = MutableStateFlow<List<EmergencyNumber>>(emptyList())
    val emergencyNumbers: StateFlow<List<EmergencyNumber>> = _emergencyNumbers.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Welcome! Ask me any rescue or emergency question, even offline.", false)
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private var sosBlinkJob: Job? = null

    init {
        // Load default emergency numbers based on selected country
        updateEmergencyNumbers(country.value)
        
        // Listen to country changes
        viewModelScope.launch {
            country.collect { newCountry ->
                updateEmergencyNumbers(newCountry)
            }
        }
    }

    // Settings actions
    fun setLanguage(lang: String) {
        language.value = lang
        sharedPrefs.edit().putString("language", lang).apply()
    }

    fun setCountry(c: String) {
        country.value = c
        sharedPrefs.edit().putString("country", c).apply()
        updateEmergencyNumbers(c)
    }

    fun completeOnboarding() {
        onboardingCompleted.value = true
        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
        
        // Seed default survival items and places offline on completion
        seedDefaultOfflineData()
    }

    fun setDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        sharedPrefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    // SOS Mode Toggle
    fun toggleEmergencyMode() {
        val newState = !isEmergencyMode.value
        isEmergencyMode.value = newState
        sharedPrefs.edit().putBoolean("emergency_mode", newState).apply()
        
        if (newState) {
            // Automatically triggers siren or flashlights if required
            startSirenSound()
        } else {
            stopAllEmergencyAlerts()
        }
    }

    // Siren
    fun startSirenSound() {
        isSirenPlaying.value = true
        SirenPlayer.startSiren()
    }

    fun stopSirenSound() {
        isSirenPlaying.value = false
        SirenPlayer.stopSiren()
    }

    // Flashlight & Morse Code SOS Blink
    fun toggleFlashlight(context: Context) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.getOrNull(0)
            if (cameraId != null) {
                // If blinking, stop it first
                if (isSosBlinking.value) {
                    stopSosBlink(context)
                }
                val newState = !isFlashlightOn.value
                cameraManager.setTorchMode(cameraId, newState)
                isFlashlightOn.value = newState
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startSosBlink(context: Context) {
        isSosBlinking.value = true
        isFlashlightOn.value = false
        sosBlinkJob?.cancel()
        sosBlinkJob = viewModelScope.launch(Dispatchers.IO) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.getOrNull(0) ?: return@launch
            var state = false
            while (isActive) {
                try {
                    state = !state
                    cameraManager.setTorchMode(cameraId, state)
                    withContext(Dispatchers.Main) {
                        isFlashlightOn.value = state
                    }
                } catch (e: Exception) {
                    break
                }
                delay(400)
            }
        }
    }

    fun stopSosBlink(context: Context) {
        isSosBlinking.value = false
        sosBlinkJob?.cancel()
        sosBlinkJob = null
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.getOrNull(0)
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, false)
                isFlashlightOn.value = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAllEmergencyAlerts() {
        stopSirenSound()
        try {
            val context = getApplication<Application>().applicationContext
            stopSosBlink(context)
        } catch (e: Exception) {
            // silent catch
        }
    }

    // Database updates: Medical Profile
    fun saveMedicalProfile(profile: MedicalProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMedicalProfile(profile)
        }
    }

    // Database updates: Contacts
    fun addEmergencyContact(contact: EmergencyContact) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addEmergencyContact(contact)
        }
    }

    fun deleteEmergencyContact(contact: EmergencyContact) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEmergencyContact(contact)
        }
    }

    // Database updates: Survival checklist
    fun toggleSurvivalItem(item: SurvivalItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSurvivalItem(item.copy(isChecked = !item.isChecked))
        }
    }

    // Database updates: Diary
    fun addDiaryEntry(title: String, location: String, description: String, imagePath: String? = null, voicePath: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = DiaryEntry(
                title = title,
                location = location,
                description = description,
                imagePath = imagePath,
                voicePath = voicePath
            )
            repository.addDiaryEntry(entry)
        }
    }

    fun deleteDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDiaryEntry(entry)
        }
    }

    // Database updates: Bookmarks
    fun toggleSafePlaceBookmark(name: String, type: String, address: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = bookmarkedSafePlaces.value.firstOrNull { it.name == name }
            if (existing != null) {
                repository.removeSafePlaceBookmark(existing)
            } else {
                repository.bookmarkSafePlace(SafePlaceBookmark(name = name, type = type, address = address, phone = phone))
            }
        }
    }

    // AI Chat Handler
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        
        val userMsg = ChatMessage(text, true)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val langCode = language.value
            val apiKey = BuildConfig.GEMINI_API_KEY
            
            // Check if API key is standard placeholder
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                delay(800) // Simulated slight network delay
                val localAns = FaqDatabase.getAnswer(text, langCode)
                withContext(Dispatchers.Main) {
                    _chatMessages.value = _chatMessages.value + ChatMessage(localAns, false)
                    _isChatLoading.value = false
                }
                return@launch
            }

            try {
                val systemPrompt = "You are RescueHub AI, a highly specialized offline and online emergency disaster survival assistant. Provide extremely actionable, step-by-step instructions in the user's preferred language (Current Selected: $langCode). Be concise, serious, and accurate."
                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = text)))),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )
                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: FaqDatabase.getAnswer(text, langCode)
                
                withContext(Dispatchers.Main) {
                    _chatMessages.value = _chatMessages.value + ChatMessage(reply, false)
                    _isChatLoading.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Network error, timeout or rate limit -> Fallback to Offline FAQ
                val localAns = FaqDatabase.getAnswer(text, langCode)
                withContext(Dispatchers.Main) {
                    _chatMessages.value = _chatMessages.value + ChatMessage(localAns, false)
                    _isChatLoading.value = false
                }
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage("Chat history cleared. Ask me any emergency question.", false)
        )
    }

    // Country selection logic to build emergency numbers
    private fun updateEmergencyNumbers(selectedCountry: String) {
        _emergencyNumbers.value = when {
            selectedCountry.contains("Bangladesh") -> listOf(
                EmergencyNumber("National Emergency / জাতীয় সেবা", "999", "Emergency"),
                EmergencyNumber("Ambulance / অ্যাম্বুলেন্স সেবা", "999", "Ambulance"),
                EmergencyNumber("Fire Service / ফায়ার সার্ভিস", "999", "Fire"),
                EmergencyNumber("Police Service / পুলিশ কন্ট্রোল", "999", "Police"),
                EmergencyNumber("Coast Guard / কোস্ট গার্ড", "01769-440999", "CoastGuard"),
                EmergencyNumber("Disaster Management / দুর্যোগ বার্তা", "1090", "Disaster"),
                EmergencyNumber("Women Helpline / নারী ও শিশু সহায়তা", "10921", "WomenHelpline"),
                EmergencyNumber("Child Helpline / শিশু সহায়তা", "1098", "ChildHelpline"),
                EmergencyNumber("Electricity Desk (DESCO) / বিদ্যুৎ", "16120", "Electricity"),
                EmergencyNumber("Gas Emergencies (Titas) / গ্যাস", "16503", "Gas")
            )
            selectedCountry.contains("India") -> listOf(
                EmergencyNumber("National Emergency", "112", "Emergency"),
                EmergencyNumber("Police Control", "100", "Police"),
                EmergencyNumber("Ambulance Service", "102", "Ambulance"),
                EmergencyNumber("Fire Control Room", "101", "Fire"),
                EmergencyNumber("Disaster Management", "1078", "Disaster"),
                EmergencyNumber("Women Helpline", "1091", "WomenHelpline"),
                EmergencyNumber("Child Helpline", "1098", "ChildHelpline")
            )
            else -> listOf( // Default US/General
                EmergencyNumber("National Emergency", "911", "Emergency"),
                EmergencyNumber("Police", "911", "Police"),
                EmergencyNumber("Ambulance", "911", "Ambulance"),
                EmergencyNumber("Fire Department", "911", "Fire"),
                EmergencyNumber("FEMA Disaster Assistance", "1-800-621-3362", "Disaster"),
                EmergencyNumber("Poison Control Hotline", "1-800-222-1222", "Medical")
            )
        }
    }

    // Preseed room database
    private fun seedDefaultOfflineData() {
        viewModelScope.launch(Dispatchers.IO) {
            // Only seed if list is empty
            val currentList = database.survivalItemDao().getAllItems().first()
            if (currentList.isEmpty()) {
                val items = listOf(
                    SurvivalItem(name = "Clean Drinking Water (3 Litres per day)", category = "Essentials"),
                    SurvivalItem(name = "Dry Food & Energy Bars (3-day supply)", category = "Essentials"),
                    SurvivalItem(name = "First Aid Kit (Bandages, Antiseptic, Gauze)", category = "Medical"),
                    SurvivalItem(name = "Hand Torch or Flashlight", category = "Tools"),
                    SurvivalItem(name = "Battery-powered Portable Radio", category = "Tools"),
                    SurvivalItem(name = "Extra AAA/AA Batteries", category = "Tools"),
                    SurvivalItem(name = "Emergency Mylar Blanket", category = "Essentials"),
                    SurvivalItem(name = "Whistle to signal for rescue", category = "Tools"),
                    SurvivalItem(name = "Pocket Knife / Multi-tool", category = "Tools"),
                    SurvivalItem(name = "High-Capacity Power Bank & Cable", category = "Essentials"),
                    SurvivalItem(name = "NID, Passport & Medical papers in plastic bag", category = "Documents"),
                    SurvivalItem(name = "Emergency Cash", category = "Essentials")
                )
                repository.seedSurvivalItems(items)
                
                // Add initial Medical Profile
                repository.saveMedicalProfile(MedicalProfile(id = 1))
            }
        }
    }
}
