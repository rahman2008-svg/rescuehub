package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MedicalProfile::class,
        EmergencyContact::class,
        SurvivalItem::class,
        DiaryEntry::class,
        SafePlaceBookmark::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RescueDatabase : RoomDatabase() {
    abstract fun medicalProfileDao(): MedicalProfileDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun survivalItemDao(): SurvivalItemDao
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun safePlaceBookmarkDao(): SafePlaceBookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: RescueDatabase? = null

        fun getDatabase(context: Context): RescueDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RescueDatabase::class.java,
                    "rescue_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
