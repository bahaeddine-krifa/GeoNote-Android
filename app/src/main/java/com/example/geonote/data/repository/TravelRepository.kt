package com.example.geonote.data.repository

import com.example.geonote.data.api.ApiService
import com.example.geonote.data.dao.TravelEntryDao
import com.example.geonote.data.entity.TravelEntry
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

// Classe utilitaire pour les états réseau
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val exception: Exception) : NetworkResult<T>()
}

class TravelRepository(
    private val dao: TravelEntryDao,
    private val weatherApi: ApiService,
    private val locationApi: ApiService
) {
    val allEntries: Flow<List<TravelEntry>> = dao.getAllEntries()

    suspend fun insert(entry: TravelEntry) = dao.insert(entry)
    suspend fun delete(entry: TravelEntry) = dao.delete(entry)
    suspend fun getEntryById(id: Int) = dao.getEntryById(id)

    // Récupération météo + ville depuis les APIs
    suspend fun fetchWeatherAndLocation(lat: Double, lon: Double): NetworkResult<Pair<Double, String>> {
        return try {
            // Appel parallèle des 2 APIs
            val weather = weatherApi.getCurrentWeather(lat, lon)
            val location = locationApi.getReverseGeocode(lat, lon)
            NetworkResult.Success(Pair(weather.currentWeather.temperature, location.address.getReadableName()))
        } catch (e: IOException) {
            NetworkResult.Error(e)
        } catch (e: HttpException) {
            NetworkResult.Error(e)
        }
    }
}