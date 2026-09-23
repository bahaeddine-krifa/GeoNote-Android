package com.example.geonote.data.api

import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("address") val address: Address
)

data class Address(
    @SerializedName("city") val city: String?,
    @SerializedName("town") val town: String?,
    @SerializedName("village") val village: String?,
    @SerializedName("country") val country: String?
) {
    fun getReadableName(): String = city ?: town ?: village ?: country ?: "Position inconnue"
}