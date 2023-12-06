package com.plutoapps.huntrs.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.plutoapps.huntrs.data.repos.HuntsRepo



class HuntsViewModelFactory(private val repository: HuntsRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HuntsViewModel::class.java)) {
            return HuntsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}