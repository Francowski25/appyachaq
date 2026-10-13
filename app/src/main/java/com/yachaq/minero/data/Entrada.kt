package com.yachaq.minero.data

data class Entrada(
    val material: String,       // "Cobre (Cu)", "Oro (Au)", "Plata (Ag)"
    val tonelaje: Double,
    val ley: Double,
    val precio: Double,
    val alquilerPlanta: Double = 50.0, // $/TM
    val adelanto: Double = 0.0
)