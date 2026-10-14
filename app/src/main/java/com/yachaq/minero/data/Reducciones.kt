package com.yachaq.minero.data

class Reducciones(
    val contenidoPagableCu: Double,
    val contenidoPagableAg: Double,
    val contenidoPagableAu: Double,
    val tmns: Double,
    val t: Terminos
) {
    private companion object {
        const val LB_KG = 0.45359
    }

    val gastosRefCuPorTm: Double
        get() = 1000 * t.gastosRefCu / LB_KG

    val reduccionCu: Double get() = contenidoPagableCu * gastosRefCuPorTm
    val reduccionAg: Double get() = contenidoPagableAg * t.gastosRefAg
    val reduccionAu: Double get() = contenidoPagableAu * t.gastosRefAu

    val reduccionRefinacion: Double
        get() = reduccionCu + reduccionAg + reduccionAu

    val maquila: Double get() = t.maquila

    val analisisPorTmns: Double
        get() = if (tmns > 0) t.analisis / tmns else 0.0

    val trasladoPorTmns: Double
        get() = if (tmns > 0) t.trasladoInterno / tmns else 0.0
}