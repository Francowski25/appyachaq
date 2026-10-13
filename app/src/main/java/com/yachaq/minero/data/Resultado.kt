package com.yachaq.minero.data

data class Resultado(
    // Concentrado
    val tmHumedas: Double,
    val tmns: Double,

    // Pagos
    val pagoCu: Double,
    val pagoAg: Double,
    val pagoAu: Double,
    val totalPago: Double,

    // Reducciones
    val maquila: Double,
    val reduccionRefinacion: Double,
    val analisis: Double,
    val trasladoInterno: Double,
    val pagoPorTm: Double,

    // Liquidación
    val valorNeto: Double,
    val igv: Double,
    val valorTotal: Double,
    val detraccion: Double,
    val alquilerTotal: Double,
    val totalAPagar: Double
)