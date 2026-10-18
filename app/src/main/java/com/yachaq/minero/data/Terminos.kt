package com.yachaq.minero.data

data class Terminos(
    val factorConcentrado: Double = 3.3,
    val humedad: Double = 10.1,
    val merma: Double = 1.0,

    val leyConcentradoCu: Double = 25.23,
    val leyConcentradoAg: Double = 0.0,
    val leyConcentradoAu: Double = 0.0,

    val pagableCu: Double = 100.0,
    val deduccionCu: Double = 1.35,
    val gastosRefCu: Double = 0.19,
    val proteccionCu: Double = 0.1,

    val pagableAg: Double = 90.0,
    val deduccionAg: Double = 2.0,
    val gastosRefAg: Double = 1.2,

    val pagableAu: Double = 90.0,
    val deduccionAu: Double = 60.0,
    val gastosRefAu: Double = 12.0,

    val maquila: Double = 175.0,
    val analisis: Double = 100.0,
    val trasladoInterno: Double = 55.0,

    val igvPorcentaje: Double = 0.18,
    val detraccionPorcentaje: Double = 0.10
)