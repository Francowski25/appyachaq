package com.yachaq.minero.data

class Concentrado(
    val tonelaje: Double,
    val ley: Double,
    val t: Terminos
) {
    val tmHumedas: Double
        get() = tonelaje * ley * t.factorConcentrado / 100

    val toneladasConcentrado: Double
        get() = tmHumedas - (t.merma / 100) - (tmHumedas * t.humedad / 100)

    fun esValido(): Boolean = tonelaje > 0 && ley > 0 && toneladasConcentrado > 0
}