package com.yachaq.minero.data

class Liquidacion(
    val pagoPorTm: Double,
    val cantidadConcentrado: Double,
    val alquilerPlanta: Double,
    val tonelaje: Double,
    val adelanto: Double = 0.0,
    val t: Terminos
) {
    val valorNeto: Double get() = pagoPorTm * cantidadConcentrado
    val igv: Double get() = valorNeto * t.igvPorcentaje
    val valorTotal: Double get() = valorNeto + igv
    val detraccion: Double get() = valorTotal * t.detraccionPorcentaje
    val alquilerTotal: Double get() = alquilerPlanta * tonelaje
    val totalAPagar: Double get() = valorTotal - detraccion - alquilerTotal - adelanto
}