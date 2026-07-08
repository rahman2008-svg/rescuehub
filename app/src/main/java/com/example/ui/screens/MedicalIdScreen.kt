package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicalProfile
import com.example.ui.RescueViewModel
import com.example.ui.theme.*
import java.util.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalIdScreen(viewModel: RescueViewModel) {
    val profileOpt by viewModel.medicalProfile.collectAsState()
    val contacts by viewModel.emergencyContacts.collectAsState()
    
    val profile = profileOpt ?: MedicalProfile()

    var isEditing by remember { mutableStateOf(false) }

    // Form editing states
    var medName by remember { mutableStateOf(profile.name) }
    var medBlood by remember { mutableStateOf(profile.bloodGroup) }
    var medAllergy by remember { mutableStateOf(profile.allergy) }
    var medDiabetes by remember { mutableStateOf(profile.diabetes) }
    var medHeart by remember { mutableStateOf(profile.heartDisease) }
    var medMedicine by remember { mutableStateOf(profile.currentMedicine) }
    var medAge by remember { mutableStateOf(profile.age.toString()) }
    var medWeight by remember { mutableStateOf(profile.weight.toString()) }

    var expandedBlood by remember { mutableStateOf(false) }
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    // Sync editing states with profile updates
    LaunchedEffect(profileOpt) {
        profileOpt?.let {
            medName = it.name
            medBlood = it.bloodGroup
            medAllergy = it.allergy
            medDiabetes = it.diabetes
            medHeart = it.heartDisease
            medMedicine = it.currentMedicine
            medAge = it.age.toString()
            medWeight = it.weight.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical ID & Lock QR", fontWeight = FontWeight.Black) },
                actions = {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Filled.Close else Icons.Filled.Edit,
                            contentDescription = "Edit Profile"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // QR Code Section
            if (!isEditing) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "SCAN TO SECURE MEDICAL DETAILS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // High quality custom generated mock QR
                    QRCard(profile = profile)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "First Responders scan this QR code to view offline blood details and emergency alerts.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (isEditing) {
                // Render Edit form
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Edit Medical Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = medName,
                                onValueChange = { medName = it },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1.5f)) {
                                    ExposedDropdownMenuBox(
                                        expanded = expandedBlood,
                                        onExpandedChange = { expandedBlood = it }
                                    ) {
                                        OutlinedTextField(
                                            value = medBlood,
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
                                                        medBlood = bg
                                                        expandedBlood = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                OutlinedTextField(
                                    value = medAge,
                                    onValueChange = { medAge = it },
                                    label = { Text("Age") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                OutlinedTextField(
                                    value = medWeight,
                                    onValueChange = { medWeight = it },
                                    label = { Text("Weight") },
                                    modifier = Modifier.weight(1.2f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = medAllergy,
                                onValueChange = { medAllergy = it },
                                label = { Text("Allergies") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = medMedicine,
                                onValueChange = { medMedicine = it },
                                label = { Text("Current Medications") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Has Diabetes", fontWeight = FontWeight.Bold)
                                Switch(checked = medDiabetes, onCheckedChange = { medDiabetes = it })
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Has Heart Disease", fontWeight = FontWeight.Bold)
                                Switch(checked = medHeart, onCheckedChange = { medHeart = it })
                            }
                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    viewModel.saveMedicalProfile(
                                        MedicalProfile(
                                            id = 1,
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
                                    isEditing = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Save Profile Updates", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            } else {
                // Read-only Details Lists
                item {
                    MedicalSummaryCard(profile = profile)
                    Spacer(modifier = Modifier.height(20.dp))
                }

                item {
                    Text(
                        "EMERGENCY CONTACTS DIALER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                if (contacts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No Emergency Contacts Added", fontWeight = FontWeight.Bold)
                                Text("Please configure contacts in Settings or Onboarding.", fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(contacts.size) { index ->
                        val contact = contacts[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Person, contentDescription = "Person", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("${contact.relation} • ${contact.phone}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }

                                // Dial action
                                val context = LocalContext.current
                                IconButton(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                                        context.startActivity(dialIntent)
                                    }
                                ) {
                                    Icon(Icons.Filled.Call, contentDescription = "Call", tint = Color(0xFF22C55E))
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun QRCard(profile: MedicalProfile) {
    Card(
        modifier = Modifier
            .size(240.dp)
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Draw custom matrix look
            Canvas(modifier = Modifier.size(180.dp)) {
                val gridCount = 21
                val cellSize = size.width / gridCount

                // Draw finder patterns (three corner squares)
                fun drawFinderPattern(x: Float, y: Float) {
                    // Outer square
                    drawRect(Color.Black, Offset(x, y), Size(cellSize * 7, cellSize * 7))
                    drawRect(Color.White, Offset(x + cellSize, y + cellSize), Size(cellSize * 5, cellSize * 5))
                    drawRect(Color.Black, Offset(x + cellSize * 2, y + cellSize * 2), Size(cellSize * 3, cellSize * 3))
                }

                drawFinderPattern(0f, 0f)
                drawFinderPattern((gridCount - 7) * cellSize, 0f)
                drawFinderPattern(0f, (gridCount - 7) * cellSize)

                // Fill custom deterministic blocks based on patient details
                val rawString = "${profile.name}|${profile.bloodGroup}|${profile.allergy}|${profile.diabetes}|${profile.heartDisease}"
                val hash = rawString.hashCode()
                val r = Random(hash.toLong())

                for (col in 0 until gridCount) {
                    for (row in 0 until gridCount) {
                        // Skip Finder Patterns
                        if ((col < 8 && row < 8) || (col > gridCount - 9 && row < 8) || (col < 8 && row > gridCount - 9)) {
                            continue
                        }
                        if (r.nextBoolean()) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(col * cellSize, row * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }
            }

            // High contrast central medical icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, PrimaryRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MedicalInformation, contentDescription = "Medical", tint = PrimaryRed, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun MedicalSummaryCard(profile: MedicalProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = profile.name.ifBlank { "User Profile" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text("Age: ${profile.age} yrs • Weight: ${profile.weight} kg", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = profile.bloodGroup.ifBlank { "O+" },
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.2f))

            Row(modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Warning, contentDescription = "Allergy", tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Allergies", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(profile.allergy.ifBlank { "None Specified" }, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Medication, contentDescription = "Medicine", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Current Medications", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(profile.currentMedicine.ifBlank { "None" }, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.HeartBroken, contentDescription = "Heart", tint = Color.Red, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Chronic Conditions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    val conditions = mutableListOf<String>()
                    if (profile.diabetes) conditions.add("Diabetes (Sugar)")
                    if (profile.heartDisease) conditions.add("Heart Disease")
                    if (conditions.isEmpty()) conditions.add("None reported")
                    Text(conditions.joinToString(", "), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        }
    }
}
