package com.yachaq.minero.data.remote.model

import com.google.gson.annotations.SerializedName

data class RequestUserTerms(
    val idUser: String,
    val idPlant: String,

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

    val favorita: Boolean? = null
)

data class ResponseUserTerms(
    val idPlant: String? = null,
    val name: String? = null,
    val location: String? = null,
    val personalizado: Boolean? = null,

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
    val updatedAt: String? = null,

    val type: String? = null,
    val listMessage: List<String>? = null,

    val favorita: Boolean? = null
)

data class ResponseGenericTerms(
    val type: String? = null,
    val listMessage: List<String>? = null
)