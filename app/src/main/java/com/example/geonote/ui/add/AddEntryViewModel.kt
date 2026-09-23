package com.example.geonote.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geonote.data.api.ApiService
import com.example.geonote.data.entity.TravelEntry
import com.example.geonote.data.repository.TravelRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AddEntryViewModel(
    private val repository: TravelRepository,
    private val weatherApi: ApiService,
    private val locationApi: ApiService
) : ViewModel() {

    private val _saveResult = MutableSharedFlow<Boolean>()
    val saveResult: SharedFlow<Boolean> = _saveResult

    fun saveEntry(
        title: String,
        description: String,
        date: Long,
        latitude: Double?,
        longitude: Double?,
        photoUri: String?
    ) {
        viewModelScope.launch {
            if (title.isBlank()) {
                _saveResult.emit(false)
                return@launch
            }

            var cityName: String? = null
            var weatherInfo: String? = null

            if (latitude != null && longitude != null) {
                try {
                    val weather = weatherApi.getCurrentWeather(latitude, longitude)
                    val location = locationApi.getReverseGeocode(latitude, longitude)

                    cityName = location.address.getReadableName()
                    val temp = weather.currentWeather.temperature
                    weatherInfo = formatWeather(weather.currentWeather.weatherCode, temp)
                } catch (e: Exception) {
                    cityName = "${String.format("%.2f", latitude)}, ${String.format("%.2f", longitude)}"
                    weatherInfo = "⚠️ Hors ligne"
                }
            }

            val entry = TravelEntry(
                title = title.trim(),
                description = description.trim(),
                date = date,
                latitude = latitude,
                longitude = longitude,
                photoUri = photoUri,
                cityName = cityName,
                weatherInfo = weatherInfo
            )

            repository.insert(entry)
            _saveResult.emit(true)
        }
    }

    private fun formatWeather(code: Int, temp: Double): String {
        val icon = when (code) {
            0 -> "☀️"      // Clear sky
            1, 2, 3 -> "🌤" // Partly cloudy
            45, 48 -> "🌫️"  // Fog
            51, 53, 55, 61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️" // Rain
            71, 73, 75, 77, 85, 86 -> "❄️" // Snow
            95, 96, 99 -> "⛈️" // Thunderstorm
            else -> "🌡️"
        }
        return "$icon ${temp.toInt()}°C"
    }
}