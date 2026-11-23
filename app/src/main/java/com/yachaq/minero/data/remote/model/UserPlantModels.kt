package com.yachaq.minero.data.remote.model

data class RequestUserPlant(
    val name: String,
    val location: String,
    val capacityTMAD: Double,
    val rentalCostPerTM: Double,
    val status: String? = "Operativa"
)

data class ResponseUserPlant(
    val idUserPlant: String? = null,
    val name: String? = null,
    val location: String? = null,
    val capacityTMAD: Double? = null,
    val rentalCostPerTM: Double? = null,
    val status: String? = null,
    val maquila: Double? = null,
    val analisis: Double? = null,
    val listMessage: List<String>? = null,
    val type: String? = null
)