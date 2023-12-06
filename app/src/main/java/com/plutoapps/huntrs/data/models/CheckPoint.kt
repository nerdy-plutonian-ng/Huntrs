package com.plutoapps.huntrs.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "Checkpoints")
data class CheckPoint(
    @PrimaryKey val id : String = UUID.randomUUID().toString(),
    val parentId : String,
    val title : String = "",
    val longitude: Double? = null,
    val latitude : Double? = null
)
