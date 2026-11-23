package com.yachaq.minero.data.remote.model

data class ResponseCalculo(
    val idCalculation: String? = null,
    val material: String? = null,
    val tonelaje: Double? = null,
    val ley: Double? = null,
    val precio: Double? = null,

    val tmHumedas: Double? = null,
    val tmns: Double? = null,

    val contenidoPagableCu: Double? = null,
    val precioAjustadoCu: Double? = null,
    val pagoCu: Double? = null,

    val contenidoPagableAg: Double? = null,
    val precioAjustadoAg: Double? = null,
    val pagoAg: Double? = null,

    val contenidoPagableAu: Double? = null,
    val precioAjustadoAu: Double? = null,
    val pagoAu: Double? = null,

    val reduccionRefinacion: Double? = null,
    val analisisPorTmns: Double? = null,
    val trasladoPorTmns: Double? = null,
    val pagoPorTm: Double? = null,

    val valorNeto: Double? = null,
    val igv: Double? = null,
    val valorTotal: Double? = null,
    val detraccion: Double? = null,
    val alquilerTotal: Double? = null,
    val totalAPagar: Double? = null,
    val pagoTotal: Double? = null,

    val listMessage: List<String>? = null,
    val type: String? = null
)

data class ResponseHistorial(
    val idCalculation: String? = null,

    // Resumen (compatibilidad)
    val material: String? = null,
    val tonelaje: Double? = null,
    val ley: Double? = null,
    val precio: Double? = null,
    val tmns: Double? = null,
    val totalAPagar: Double? = null,
    val calculatedAt: String? = null,

    // Planta
    val idPlant: String? = null,
    val idUserPlant: String? = null,
    val plantType: String? = null,          // OFICIAL | PERSONAL
    val plantName: String? = null,
    val plantLocation: String? = null,
    val plantRentalCostPerTM: Double? = null,
    val termsSource: String? = null,        // GENERAL | PERSONALIZADO

    // Términos usados
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

    // Entradas
    val leyCu: Double? = null,
    val leyAg: Double? = null,
    val leyAu: Double? = null,
    val adelanto: Double? = null,

    // Precios usados
    val precioCu: Double? = null,
    val precioAg: Double? = null,
    val precioAu: Double? = null,
    val preciosUpdatedAt: String? = null,

    // Resultados: concentrado
    val tmHumedas: Double? = null,

    // Resultados: por metal
    val contenidoPagableCu: Double? = null,
    val precioAjustadoCu: Double? = null,
    val pagoCu: Double? = null,
    val contenidoPagableAg: Double? = null,
    val precioAjustadoAg: Double? = null,
    val pagoAg: Double? = null,
    val contenidoPagableAu: Double? = null,
    val precioAjustadoAu: Double? = null,
    val pagoAu: Double? = null,

    // Resultados: deducciones y totales
    val pagoTotal: Double? = null,
    val reduccionRefinacion: Double? = null,
    val analisisPorTmns: Double? = null,
    val trasladoPorTmns: Double? = null,
    val pagoPorTm: Double? = null,
    val valorNeto: Double? = null,
    val igv: Double? = null,
    val valorTotal: Double? = null,
    val detraccion: Double? = null,
    val alquilerPlanta: Double? = null,
    val alquilerTotal: Double? = null,

    val isSynced: Boolean? = null,

    val listMessage: List<String>? = null,
    val type: String? = null
)