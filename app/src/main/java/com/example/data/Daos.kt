package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalProfileDao {
    @Query("SELECT * FROM medical_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<MedicalProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: MedicalProfile)
}

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY id ASC")
    fun getAllContacts(): Flow<List<EmergencyContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<EmergencyContact>)

    @Delete
    suspend fun deleteContact(contact: EmergencyContact)
}

@Dao
interface SurvivalItemDao {
    @Query("SELECT * FROM survival_items ORDER BY id ASC")
    fun getAllItems(): Flow<List<SurvivalItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<SurvivalItem>)

    @Update
    suspend fun updateItem(item: SurvivalItem)
}

@Dao
interface DiaryEntryDao {
    @Query("SELECT * FROM diary_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntry)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)
}

@Dao
interface SafePlaceBookmarkDao {
    @Query("SELECT * FROM safe_place_bookmarks ORDER BY id DESC")
    fun getAllBookmarks(): Flow<List<SafePlaceBookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: SafePlaceBookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: SafePlaceBookmark)
}
