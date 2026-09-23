package com.example.geonote.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.geonote.data.repository.TravelRepository

class MainViewModelFactory(
    private val repository: TravelRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Extension pratique pour l'injection dans MainActivity
fun MainViewModel.Companion.Factory(repo: TravelRepository) = MainViewModelFactory(repo)