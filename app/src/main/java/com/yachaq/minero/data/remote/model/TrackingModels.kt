package com.yachaq.minero.data.remote.model

data class RequestTracking(
    val idUser: String,
    val idPlant: String,
    val mineral: String,
    val grossTonnage: Double,
    val entryDate: String,
    val status: String? = "EN PLANTA"
)

data class ResponseTracking(
    val idTracking: String? = null,
    val idUser: String? = null,
    val idPlant: String? = null,
    val plantName: String? = null,
    val mineral: String? = null,
    val grossTonnage: Double? = null,
    val entryDate: String? = null,
    val status: String? = null,
    val daysInPlant: Int? = null,
    val listMessage: List<String>? = null,
    val type: String? = null
)