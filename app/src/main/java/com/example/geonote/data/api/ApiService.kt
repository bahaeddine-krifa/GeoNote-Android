package com.example.geonote.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    // API 1 : Open-Météo (Météo actuelle)
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): WeatherResponse

    // API 2 : Nominatim (Géocodage inverse)
    @GET("reverse")
    suspend fun getReverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json",
        @Query("zoom") zoom: Int = 10
    ): LocationResponse
}