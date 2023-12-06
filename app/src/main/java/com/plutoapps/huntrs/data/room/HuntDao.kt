package com.plutoapps.huntrs.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.plutoapps.huntrs.data.models.CheckPoint
import com.plutoapps.huntrs.data.models.Hunt
import kotlinx.coroutines.flow.Flow

@Dao
interface HuntDao {
    @Query("Select * from Hunts")
    fun getHunts() : Flow<List<Hunt>>

    @Query("Select * from Hunts where isMine = :isMine")
    fun getHuntsByType(isMine: Boolean) : Flow<List<Hunt>>

    @Upsert
    suspend fun upsertHunt(hunt : Hunt)

    @Delete
    suspend fun deleteHunt(hunt: Hunt)

    @Query("Select * from Hunts where id = :id")
    suspend fun getHuntById(id : String) : Hunt

}