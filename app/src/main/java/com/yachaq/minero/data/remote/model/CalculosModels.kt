package com.yachaq.minero.data.remote.model

data class RequestCalculo(
    val idUser: String,
    val idPlant: String,
    val material: String,
    val tonelaje: Double,
    val ley: Double,
    val precio: Double
)

data class ResponseCalculo(
    val idCalculation: String? = null,
    val material: String? = null,
    val tonelaje: Double? = null,
    val ley: Double? = null,
    val precio: Double? = null,
    val tmHumedas: Double? = null,
    val tmns: Double? = null,
    val pagoTotal: Double? = null,
    val valorNeto: Double? = null,
    val igv: Double? = null,
    val detraccion: Double? = null,
    val alquilerTotal: Double? = null,
    val totalAPagar: Double? = null,
    val listMessage: List<String>? = null,
    val type: String? = null
)

data class ResponseHistorial(
    val idCalculation: String? = null,
    val material: String? = null,
    val tonelaje: Double? = null,
    val ley: Double? = null,
    val precio: Double? = null,
    val tmns: Double? = null,
    val totalAPagar: Double? = null,
    val calculatedAt: String? = null,
    val listMessage: List<String>? = null,
    val type: String? = null
)