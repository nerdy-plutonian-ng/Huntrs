package com.plutoapps.huntrs.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "Hunts")
data class Hunt(
    @PrimaryKey val id : String = UUID.randomUUID().toString(),
    val title : String = "",
    val description : String = "",
    val isMine : Boolean = true
)

data class HuntWithCheckpoints(
    val id : String = UUID.randomUUID().toString(),
    val title : String = "",
    val description : String = "",
    val isMine : Boolean = true,
    val checkPoints : List<CheckPoint> = emptyList()
)
