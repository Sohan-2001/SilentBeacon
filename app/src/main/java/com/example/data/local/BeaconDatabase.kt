package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.EmergencyTicket

@Database(entities = [EmergencyTicket::class], version = 1, exportSchema = false)
abstract class BeaconDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao

    companion object {
        @Volatile
        private var INSTANCE: BeaconDatabase? = null

        fun getInstance(context: Context): BeaconDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BeaconDatabase::class.java,
                    "silent_beacon.db"
                ).fallbackToDestructiveMigration()
                 .build()
                 .also { INSTANCE = it }
            }
        }
    }
}
