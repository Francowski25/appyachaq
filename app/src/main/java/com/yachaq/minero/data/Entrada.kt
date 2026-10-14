package com.yachaq.minero.data

data class Entrada(
    val material: String,
    val tonelaje: Double,
    val ley: Double,
    val precio: Double,
    val alquilerPlanta: Double = 50.0,
    val adelanto: Double = 0.0
)