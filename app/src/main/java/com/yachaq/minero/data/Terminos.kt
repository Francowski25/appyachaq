package com.yachaq.minero.data

class Terminos(
    // Concentrado
    val leyConcentradoCu: Double = 25.23,   // Ley de Cu en el concentrado

    // Cobre
    val pagableCu: Double = 100.0,          // % PAGABLES CU
    val deduccionCu: Double = 1.35,         // DEDUCCION
    val gastosRefCu: Double = 0.19,         // GASTOS DE REF CU $/LB
    val proteccionCu: Double = 0.1,         // PROTECCION

    // Plata
    val pagableAg: Double = 90.0,           // % PAGABLE DE PLATA
    val deduccionAg: Double = 2.0,          // DEDUCCION Ag GR
    val gastosRefAg: Double = 1.2,          // GASTOS DE REF AG $/OZ

    // Oro
    val pagableAu: Double = 90.0,           // %PAGABLE DE AU
    val deduccionAu: Double = 60.0,         // DEDUCCION AU GR
    val gastosRefAu: Double = 12.0,         // GASTOS DE REF AU $/OZ

    // Maquila
    val maquila: Double = 175.0,            // MAQUILA $/TMNS

    // Servicios
    val analisis: Double = 100.0,           // ANÁLISIS ($)
    val trasladoInterno: Double = 55.0,     // TRASLADO INTERNO ($)

    // Impuestos
    val igvPorcentaje: Double = 0.18,       // IGV 18%
    val detraccionPorcentaje: Double = 0.10 // DETRACCIÓN 10%
)