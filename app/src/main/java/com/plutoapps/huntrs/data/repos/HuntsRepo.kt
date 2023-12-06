package com.plutoapps.huntrs.data.repos

import com.plutoapps.huntrs.data.models.Hunt
import com.plutoapps.huntrs.data.models.HuntWithCheckpoints
import com.plutoapps.huntrs.data.room.CheckPointDao
import com.plutoapps.huntrs.data.room.HuntDao
import kotlinx.coroutines.flow.Flow

interface HuntsRepo {
    fun getHunts() : Flow<List<Hunt>>

    fun getHuntsByType(isMine: Boolean) : Flow<List<Hunt>>

    suspend fun upsertHunt(huntWithCheckpoints: HuntWithCheckpoints)

    suspend fun deleteHunt(hunt: Hunt)

    suspend fun getHuntById(id : String) : HuntWithCheckpoints

}

class HuntsRoomRepo(private val huntDao: HuntDao, private val checkPointDao: CheckPointDao) : HuntsRepo {
    override fun getHunts(): Flow<List<Hunt>> = huntDao.getHunts()

    override fun getHuntsByType(isMine: Boolean): Flow<List<Hunt>> = huntDao.getHuntsByType(isMine)

    override suspend fun upsertHunt(huntWithCheckpoints: HuntWithCheckpoints) {
        val newHunt = Hunt(id = huntWithCheckpoints.id,
            title = huntWithCheckpoints.title,
            description = huntWithCheckpoints.description,
            isMine = huntWithCheckpoints.isMine)
        deleteHunt(Hunt(id = huntWithCheckpoints.id, title = huntWithCheckpoints.title,
            description = huntWithCheckpoints.description, isMine = huntWithCheckpoints.isMine))
        huntDao.upsertHunt(hunt = newHunt)
        for(checkpoint in huntWithCheckpoints.checkPoints)
            checkPointDao.upsertCheckpoint(checkpoint)
    }

    override suspend fun deleteHunt(hunt: Hunt) {
        huntDao.deleteHunt(hunt = Hunt(id = hunt.id,
            title = hunt.title,
            description = hunt.description,
            isMine = hunt.isMine))
        checkPointDao.deleteCheckpointsByParentId(hunt.id)
    }

    override suspend fun getHuntById(id: String): HuntWithCheckpoints {
        val hunt = huntDao.getHuntById(id)
        val checkpoints = checkPointDao.getCheckpointsByParentId(id)
        return HuntWithCheckpoints(id = id, title = hunt.title, description = hunt.description, isMine = hunt.isMine, checkPoints = checkpoints)
    }

}

