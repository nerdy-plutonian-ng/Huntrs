package com.plutoapps.huntrs.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plutoapps.huntrs.data.models.Hunt
import com.plutoapps.huntrs.data.models.HuntWithCheckpoints
import com.plutoapps.huntrs.data.repos.HuntsRepo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HuntsViewModel(private val huntsRepo: HuntsRepo) : ViewModel() , HuntsRepo {

    private var _uiState = MutableStateFlow(UiState(emptyList()))
    val uiState : StateFlow<UiState> = _uiState

    override fun getHunts(): Flow<List<Hunt>> = huntsRepo.getHunts()


    override fun getHuntsByType(isMine: Boolean): Flow<List<Hunt>> = huntsRepo.getHuntsByType(isMine)

    override suspend fun upsertHunt(huntWithCheckpoints: HuntWithCheckpoints) {
        viewModelScope.launch {
            huntsRepo.upsertHunt(huntWithCheckpoints)
        }
    }

    override suspend fun deleteHunt(hunt: Hunt) {
        viewModelScope.launch {
            huntsRepo.deleteHunt(hunt)
        }
    }

    override suspend fun getHuntById(id: String): HuntWithCheckpoints {
        val deferred = CompletableDeferred<HuntWithCheckpoints>()
        viewModelScope.launch {
            val hunt  = huntsRepo.getHuntById(id)
            deferred.complete(hunt)
        }
        return deferred.await()
    }


}

data class UiState (
    val hunts: List<Hunt>
)