package com.yachaq.minero.data.remote.model
/**
// Lo que enviamos al backend para calcular y guardar
data class RequestCalculo(
    val idUser: String,
    val idPlant: String,
    val material: String,
    val tonelaje: Double,
    val ley: Double,
    val precio: Double
)

// Lo que el backend nos devuelve (el cálculo completo)
data class ResponseCalculo(
    val idCalculation: String? = null,
    val material: String? = null,
    val tonelaje: Double? = null,
    val ley: Double? = null,
    val precio: Double? = null,
    val tmHumedas: Double? = null,
    val tmns: Double? = null,
    val pagoTotal: Double? = null,
    val totalAPagar: Double? = null,
    val listMessage: List<String>? = null,
    val type: String? = null
)

// Lo que el backend devuelve al pedir el historial
data class ResponseHistorial(
    val idCalculation: String? = null,
    val material: String? = null,
    val tonelaje: Double? = null,
    val ley: Double? = null,
    val precio: Double? = null,
    val tmns: Double? = null,
    val totalAPagar: Double? = null,
    val calculatedAt: String? = null, // Fecha
    val listMessage: List<String>? = null,
    val type: String? = null
)
        **/