package com.example.data

import kotlinx.coroutines.flow.Flow

class RescueRepository(private val db: RescueDatabase) {
    val medicalProfile: Flow<MedicalProfile?> = db.medicalProfileDao().getProfile()
    val emergencyContacts: Flow<List<EmergencyContact>> = db.emergencyContactDao().getAllContacts()
    val survivalItems: Flow<List<SurvivalItem>> = db.survivalItemDao().getAllItems()
    val diaryEntries: Flow<List<DiaryEntry>> = db.diaryEntryDao().getAllEntries()
    val bookmarkedSafePlaces: Flow<List<SafePlaceBookmark>> = db.safePlaceBookmarkDao().getAllBookmarks()

    suspend fun saveMedicalProfile(profile: MedicalProfile) {
        db.medicalProfileDao().insertOrUpdateProfile(profile)
    }

    suspend fun addEmergencyContact(contact: EmergencyContact) {
        db.emergencyContactDao().insertContact(contact)
    }

    suspend fun addEmergencyContacts(contacts: List<EmergencyContact>) {
        db.emergencyContactDao().insertContacts(contacts)
    }

    suspend fun deleteEmergencyContact(contact: EmergencyContact) {
        db.emergencyContactDao().deleteContact(contact)
    }

    suspend fun seedSurvivalItems(items: List<SurvivalItem>) {
        db.survivalItemDao().insertItems(items)
    }

    suspend fun updateSurvivalItem(item: SurvivalItem) {
        db.survivalItemDao().updateItem(item)
    }

    suspend fun addDiaryEntry(entry: DiaryEntry) {
        db.diaryEntryDao().insertEntry(entry)
    }

    suspend fun deleteDiaryEntry(entry: DiaryEntry) {
        db.diaryEntryDao().deleteEntry(entry)
    }

    suspend fun bookmarkSafePlace(place: SafePlaceBookmark) {
        db.safePlaceBookmarkDao().insertBookmark(place)
    }

    suspend fun removeSafePlaceBookmark(place: SafePlaceBookmark) {
        db.safePlaceBookmarkDao().deleteBookmark(place)
    }
}
