package com.yachaq.minero.data

class Reducciones(
    // Estáticos (configurables por admin)
    val maquila: Double = 175.0,
    val analisis: Double = 100.0,
    val trasladoInterno: Double = 55.0,

    // Contenido pagable (viene de Pagos)
    val contenidoPagableCu: Double,
    val contenidoPagableAg: Double,
    val contenidoPagableAu: Double,

    // Gastos de refinación (viene de Términos)
    val gastosRefCu: Double,
    val gastosRefAg: Double,
    val gastosRefAu: Double,

    // Viene de Concentrado
    val tmns: Double
) {
    private companion object {
        const val LB_KG = 0.45359
    }

    // ===== GASTOS DE REFINACIÓN =====

    /** Gastos de ref Cu en $/TMNS → 1000 × $/lb / 0.45359 */
    val gastosRefCuPorTm: Double
        get() = 1000 * gastosRefCu / LB_KG

    val reduccionCu: Double
        get() = contenidoPagableCu * gastosRefCuPorTm

    val reduccionAg: Double
        get() = contenidoPagableAg * gastosRefAg

    val reduccionAu: Double
        get() = contenidoPagableAu * gastosRefAu

    /** Reducción por refinación (suma de las 3) */
    val reduccionRefinacion: Double
        get() = reduccionCu + reduccionAg + reduccionAu

    // ===== SERVICIOS =====

    /** Análisis en USD/TMNS */
    val analisisPorTmns: Double
        get() = if (tmns > 0) analisis / tmns else 0.0

    /** Traslado interno en USD/TMNS */
    val trasladoPorTmns: Double
        get() = if (tmns > 0) trasladoInterno / tmns else 0.0

    /** Total servicios en USD/TMNS */
    val totalServiciosPorTmns: Double
        get() = analisisPorTmns + trasladoPorTmns
}