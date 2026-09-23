package com.example.geonote.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geonote.data.entity.TravelEntry
import com.example.geonote.data.repository.TravelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// États de l'UI
sealed class UiState {
    object Loading : UiState()
    object Empty : UiState()
    data class Success(val entries: List<TravelEntry>) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel(private val repository: TravelRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.allEntries
                .catch { e -> _uiState.value = UiState.Error(e.message ?: "Erreur inconnue") }
                .collect { entries ->
                    _uiState.value = if (entries.isEmpty()) UiState.Empty else UiState.Success(entries)
                }
        }
    }

    fun refresh() = loadEntries()

    fun deleteEntry(entry: TravelEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    companion object {
        fun Factory(repo: TravelRepository) = MainViewModelFactory(repo)
    }
}