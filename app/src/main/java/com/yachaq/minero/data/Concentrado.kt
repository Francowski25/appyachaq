package com.yachaq.minero.data


class Concentrado(
    val tonelaje: Double,
    val ley: Double,
    val factorConcentrado: Double = 3.3,
    val humedad: Double = 10.1,
    val merma: Double = 1.0
) {
    /** TM Húmedas de concentrado (interno) */
    val tmHumedas: Double
        get() = tonelaje * ley * factorConcentrado / 100

    /** TONELADAS DE CONCENTRADO (TMNS) — salida principal */
    val toneladasConcentrado: Double
        get() = tmHumedas - (merma / 100) - (tmHumedas * humedad / 100)

    fun esValido(): Boolean = tonelaje > 0 && ley > 0 && toneladasConcentrado > 0
}