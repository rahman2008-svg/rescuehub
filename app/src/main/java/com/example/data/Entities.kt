package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medical_profile")
data class MedicalProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val bloodGroup: String = "",
    val allergy: String = "",
    val diabetes: Boolean = false,
    val heartDisease: Boolean = false,
    val currentMedicine: String = "",
    val age: Int = 0,
    val weight: Double = 0.0
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val relation: String,
    val name: String = "",
    val phone: String = ""
)

@Entity(tableName = "survival_items")
data class SurvivalItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isChecked: Boolean = false,
    val category: String = "General"
)

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val location: String = "",
    val description: String = "",
    val imagePath: String? = null,
    val voicePath: String? = null
)

@Entity(tableName = "safe_place_bookmarks")
data class SafePlaceBookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // Hospital, Police, Fire Station, Cyclone Shelter, Relief Center
    val address: String = "",
    val phone: String = ""
)
