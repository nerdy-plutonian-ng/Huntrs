package com.plutoapps.huntrs.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.plutoapps.huntrs.data.models.CheckPoint
import com.plutoapps.huntrs.data.models.Hunt

@Database(entities = [Hunt::class, CheckPoint::class], version = 1, exportSchema = false)
abstract class HuntsDatabase : RoomDatabase() {

    abstract fun getHuntsDao(): HuntDao
    abstract fun getCheckpointDao(): CheckPointDao

    companion object {

        @Volatile
        private var Instance: HuntsDatabase? = null

        fun getDatabase(context: Context): HuntsDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, HuntsDatabase::class.java, "hunts_db").build()
                    .also { Instance = it }
            }
        }
    }


}