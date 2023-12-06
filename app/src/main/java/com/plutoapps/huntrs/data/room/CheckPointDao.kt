package com.plutoapps.huntrs.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.plutoapps.huntrs.data.models.CheckPoint
import com.plutoapps.huntrs.data.models.Hunt
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckPointDao {

    @Query("Select * from Checkpoints where parentId = :parentId")
    suspend fun getCheckpointsByParentId(parentId: String) : List<CheckPoint>

    @Upsert
    suspend fun upsertCheckpoint(checkpoint : CheckPoint)

    @Delete
    suspend fun deleteCheckpoint(checkpoint : CheckPoint)

    @Query("Delete from Checkpoints where parentId = :parentId")
    suspend fun deleteCheckpointsByParentId(parentId: String)


}