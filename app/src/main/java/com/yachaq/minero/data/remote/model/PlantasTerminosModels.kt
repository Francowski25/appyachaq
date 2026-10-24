package com.yachaq.minero.data.remote.model

// ================== PLANTAS ==================

data class RequestPlant(
    val name: String,
    val location: String,
    val capacityTMAD: Double,
    val rentalCostPerTM: Double,
    val status: String? = "Operativa"
)

data class ResponsePlant(
    val idPlant: String? = null,
    val idTerm: String? = null,
    val name: String? = null,
    val location: String? = null,
    val capacityTMAD: Double? = null,
    val rentalCostPerTM: Double? = null,
    val status: String? = null,
    val listMessage: List<String>? = null,
    val type: String? = null
)

// ================== TÉRMINOS ==================

data class RequestTerms(
    val factorConcentrado: Double,
    val humedad: Double,
    val merma: Double,
    val leyConcentradoCu: Double,
    val leyConcentradoAg: Double,
    val leyConcentradoAu: Double,
    val pagableCu: Double,
    val deduccionCu: Double,
    val gastosRefCu: Double,
    val proteccionCu: Double,
    val pagableAg: Double,
    val deduccionAg: Double,
    val gastosRefAg: Double,
    val pagableAu: Double,
    val deduccionAu: Double,
    val gastosRefAu: Double,
    val maquila: Double,
    val analisis: Double,
    val trasladoInterno: Double,
    val igvPorcentaje: Double,
    val detraccionPorcentaje: Double
)

data class ResponseTerms(
    val idTerm: String? = null,
    val idPlant: String? = null,
    val factorConcentrado: Double? = null,
    val humedad: Double? = null,
    val merma: Double? = null,
    val leyConcentradoCu: Double? = null,
    val leyConcentradoAg: Double? = null,
    val leyConcentradoAu: Double? = null,
    val pagableCu: Double? = null,
    val deduccionCu: Double? = null,
    val gastosRefCu: Double? = null,
    val proteccionCu: Double? = null,
    val pagableAg: Double? = null,
    val deduccionAg: Double? = null,
    val gastosRefAg: Double? = null,
    val pagableAu: Double? = null,
    val deduccionAu: Double? = null,
    val gastosRefAu: Double? = null,
    val maquila: Double? = null,
    val analisis: Double? = null,
    val trasladoInterno: Double? = null,
    val igvPorcentaje: Double? = null,
    val detraccionPorcentaje: Double? = null,
    val listMessage: List<String>? = null,
    val type: String? = null
)