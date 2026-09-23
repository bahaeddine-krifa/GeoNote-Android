package com.example.geonote.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geonote.data.entity.TravelEntry
import com.example.geonote.data.repository.NetworkResult
import com.example.geonote.data.repository.TravelRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(
        val entry: TravelEntry,
        val weatherText: String,
        val locationText: String
    ) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class DetailViewModel(private val repository: TravelRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    fun loadEntry(entryId: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val entry = repository.getEntryById(entryId)

            if (entry == null) {
                _uiState.value = DetailUiState.Error("Entrée introuvable")
                return@launch
            }

            var weather = "Météo non disponible"
            var location = "Position non définie"

            // Appels API parallèles si coordonnées présentes
            if (entry.latitude != null && entry.longitude != null) {
                when (val result = repository.fetchWeatherAndLocation(entry.latitude, entry.longitude)) {
                    is NetworkResult.Success -> {
                        val (temp, city) = result.data
                        weather = "☀️ $temp°C"
                        location = " $city"
                    }
                    is NetworkResult.Error -> {
                        weather = "⚠️ Hors ligne"
                        location = " ${String.format("%.2f, %.2f", entry.latitude, entry.longitude)}"
                    }
                }
            }

            _uiState.value = DetailUiState.Success(entry, weather, location)
        }
    }
}