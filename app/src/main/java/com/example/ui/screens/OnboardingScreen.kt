@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmergencyContact
import com.example.data.MedicalProfile
import com.example.ui.RescueViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: RescueViewModel,
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(0) } // 0: Welcome, 1: Language, 2: Country, 3: Download, 4: Contacts, 5: Medical

    val selectedLang by viewModel.language.collectAsState()
    val selectedCountry by viewModel.country.collectAsState()
    val contacts by viewModel.emergencyContacts.collectAsState()

    // Transient onboarding states
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactRelation by remember { mutableStateOf("Mother") }

    // Medical inputs
    var medName by remember { mutableStateOf("") }
    var medBlood by remember { mutableStateOf("O+") }
    var medAllergy by remember { mutableStateOf("") }
    var medDiabetes by remember { mutableStateOf(false) }
    var medHeart by remember { mutableStateOf(false) }
    var medMedicine by remember { mutableStateOf("") }
    var medAge by remember { mutableStateOf("") }
    var medWeight by remember { mutableStateOf("") }

    val relations = listOf("Mother", "Father", "Brother", "Sister", "Friend", "Doctor", "Spouse", "Other")
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(24.dp)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Progress / Stepper
            if (step > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step $step of 5",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    LinearProgressIndicator(
                        progress = { step / 5f },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "OnboardingStep"
                ) { currentStep ->
                    when (currentStep) {
                        0 -> WelcomeView(onNext = { step = 1 })
                        1 -> LanguageStepView(
                            selectedLang = selectedLang,
                            onLangSelect = { viewModel.setLanguage(it) },
                            onNext = { step = 2 }
                        )
                        2 -> CountryStepView(
                            selectedCountry = selectedCountry,
                            onCountrySelect = { viewModel.setCountry(it) },
                            onNext = { step = 3 }
                        )
                        3 -> DownloadStepView(onNext = { step = 4 })
                        4 -> ContactsStepView(
                            contacts = contacts,
                            name = contactName,
                            phone = contactPhone,
                            relation = contactRelation,
                            relations = relations,
                            onNameChange = { contactName = it },
                            onPhoneChange = { contactPhone = it },
                            onRelationChange = { contactRelation = it },
                            onAddContact = {
                                if (contactName.isNotBlank() && contactPhone.isNotBlank()) {
                                    viewModel.addEmergencyContact(
                                        EmergencyContact(
                                            relation = contactRelation,
                                            name = contactName,
                                            phone = contactPhone
                                        )
                                    )
                                    contactName = ""
                                    contactPhone = ""
                                }
                            },
                            onDeleteContact = { viewModel.deleteEmergencyContact(it) },
                            onNext = { step = 5 }
                        )
                        5 -> MedicalStepView(
                            name = medName,
                            bloodGroup = medBlood,
                            allergy = medAllergy,
                            diabetes = medDiabetes,
                            heartDisease = medHeart,
                            medicine = medMedicine,
                            age = medAge,
                            weight = medWeight,
                            bloodGroups = bloodGroups,
                            onNameChange = { medName = it },
                            onBloodChange = { medBlood = it },
                            onAllergyChange = { medAllergy = it },
                            onDiabetesChange = { medDiabetes = it },
                            onHeartChange = { medHeart = it },
                            onMedicineChange = { medMedicine = it },
                            onAgeChange = { medAge = it },
                            onWeightChange = { medWeight = it },
                            onComplete = {
                                viewModel.saveMedicalProfile(
                                    MedicalProfile(
                                        name = medName,
                                        bloodGroup = medBlood,
                                        allergy = medAllergy,
                                        diabetes = medDiabetes,
                                        heartDisease = medHeart,
                                        currentMedicine = medMedicine,
                                        age = medAge.toIntOrNull() ?: 0,
                                        weight = medWeight.toDoubleOrNull() ?: 0.0
                                    )
                                )
                                viewModel.completeOnboarding()
                                onComplete()
                            }
                        )
                    }
                }
            }

            // Bottom Navigation Actions
            if (step > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { step-- },
                        enabled = step > 1
                    ) {
                        Text("Back", fontSize = 16.sp)
                    }

                    if (step in 1..4 && step != 3) {
                        Button(
                            onClick = { step++ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.ArrowForward, contentDescription = "Next")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeView(onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Logo / Shield animation mock
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = "Shield",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Heart",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .offset(y = (-4).dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Welcome to RescueHub",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your Offline Emergency & Disaster Companion.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Be Ready. Stay Safe. Save Lives.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.ArrowForward, contentDescription = "Get Started")
        }
    }
}

@Composable
fun LanguageStepView(
    selectedLang: String,
    onLangSelect: (String) -> Unit,
    onNext: () -> Unit
) {
    val languages = listOf(
        "en" to "English 🇺🇸",
        "bn" to "বাংলা 🇧🇩",
        "hi" to "हिन्दी 🇮🇳",
        "ar" to "العربية 🇸🇦",
        "es" to "Español 🇪🇸"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Filled.Language,
            contentDescription = "Language",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Select Language",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Choose your preferred language for guides and warnings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        languages.forEach { (code, name) ->
            val isSelected = selectedLang == code
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onLangSelect(code) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected) borderStroke(2.dp, MaterialTheme.colorScheme.primary) else borderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (isSelected) {
                        Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun CountryStepView(
    selectedCountry: String,
    onCountrySelect: (String) -> Unit,
    onNext: () -> Unit
) {
    val countries = listOf(
        "🇧🇩 Bangladesh",
        "🇺🇸 United States",
        "🇮🇳 India",
        "🇬🇧 United Kingdom",
        "🇪🇸 Spain"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Filled.Place,
            contentDescription = "Country",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Select Your Country",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "RescueHub will customize emergency contacts, guides, and safe places for your region.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        countries.forEach { name ->
            val isSelected = selectedCountry == name
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onCountrySelect(name) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected) borderStroke(2.dp, MaterialTheme.colorScheme.primary) else borderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    if (isSelected) {
                        Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadStepView(onNext: () -> Unit) {
    var progress by remember { mutableStateOf(0.0f) }
    var currentItem by remember { mutableStateOf("Initializing guides...") }

    val downloadItems = listOf(
        "First Aid Guide & CPR Videos" to 0.25f,
        "Regional Disaster Guidelines" to 0.5f,
        "Local Emergency Toll-Free Numbers" to 0.7f,
        "Survival Manual & Kit Checklist" to 0.85f,
        "OpenStreetMap Local Area Tiles" to 1.0f
    )

    LaunchedEffect(Unit) {
        downloadItems.forEach { (name, targetProg) ->
            currentItem = "Downloading $name..."
            while (progress < targetProg) {
                progress += 0.05f
                delay(120)
            }
            progress = targetProg
            delay(200)
        }
        currentItem = "100% Offline Active. All files saved!"
        delay(800)
        onNext()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Filled.CloudDownload,
            contentDescription = "Download",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Downloading Offline Survival Pack",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "We are caching core disaster maps and lifesaving resources directly onto your device's memory so you can access them anytime without network.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentItem,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ContactsStepView(
    contacts: List<EmergencyContact>,
    name: String,
    phone: String,
    relation: String,
    relations: List<String>,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRelationChange: (String) -> Unit,
    onAddContact: () -> Unit,
    onDeleteContact: (EmergencyContact) -> Unit,
    onNext: () -> Unit
) {
    var expandedRelation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.ContactPhone,
            contentDescription = "Contacts",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Emergency Contacts",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Add up to 5 family members, friends, or doctors to alert during crises.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Add form
        if (contacts.size < 5) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Emergency Contact (${contacts.size}/5)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = { Text("Contact Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1.2f)) {
                            ExposedDropdownMenuBox(
                                expanded = expandedRelation,
                                onExpandedChange = { expandedRelation = it }
                            ) {
                                OutlinedTextField(
                                    value = relation,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Relation") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRelation) },
                                    modifier = Modifier.menuAnchor(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedRelation,
                                    onDismissRequest = { expandedRelation = false }
                                ) {
                                    relations.forEach { rel ->
                                        DropdownMenuItem(
                                            text = { Text(rel) },
                                            onClick = {
                                                onRelationChange(rel)
                                                expandedRelation = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = onPhoneChange,
                            label = { Text("Phone Number") },
                            modifier = Modifier.weight(1.8f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onAddContact,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank() && phone.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Contact")
                    }
                }
            }
        } else {
            Text(
                "You have added maximum 5 emergency contacts.",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Contacts list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(contacts) { contact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = "Person",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${contact.relation} • ${contact.phone}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                        IconButton(onClick = { onDeleteContact(contact) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalStepView(
    name: String,
    bloodGroup: String,
    allergy: String,
    diabetes: Boolean,
    heartDisease: Boolean,
    medicine: String,
    age: String,
    weight: String,
    bloodGroups: List<String>,
    onNameChange: (String) -> Unit,
    onBloodChange: (String) -> Unit,
    onAllergyChange: (String) -> Unit,
    onDiabetesChange: (Boolean) -> Unit,
    onHeartChange: (Boolean) -> Unit,
    onMedicineChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    var expandedBlood by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Icon(
                Icons.Filled.MedicalInformation,
                contentDescription = "Medical",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Offline Medical Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Stored securely offline on your device, accessible to rescuers via lock screen QR.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1.5f)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedBlood,
                        onExpandedChange = { expandedBlood = it }
                    ) {
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Blood Group") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBlood) },
                            modifier = Modifier.menuAnchor(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = expandedBlood,
                            onDismissRequest = { expandedBlood = false }
                        ) {
                            bloodGroups.forEach { bg ->
                                DropdownMenuItem(
                                    text = { Text(bg) },
                                    onClick = {
                                        onBloodChange(bg)
                                        expandedBlood = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = age,
                    onValueChange = onAgeChange,
                    label = { Text("Age") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = onWeightChange,
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.weight(1.2f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = allergy,
                onValueChange = onAllergyChange,
                label = { Text("Allergies (e.g. Penicillin, Nuts)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = medicine,
                onValueChange = onMedicineChange,
                label = { Text("Current Medications") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.WaterDrop, contentDescription = "Diabetes", tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Has Diabetes", fontWeight = FontWeight.Bold)
                }
                Switch(checked = diabetes, onCheckedChange = onDiabetesChange)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, contentDescription = "Heart", tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Has Heart Disease", fontWeight = FontWeight.Bold)
                }
                Switch(checked = heartDisease, onCheckedChange = onHeartChange)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Complete & Setup Home", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Filled.Check, contentDescription = "Check")
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// Simple border stroke creator
fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
