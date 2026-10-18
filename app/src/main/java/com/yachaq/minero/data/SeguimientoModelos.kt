package com.yachaq.minero.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class EstadoCarga(val etiqueta: String) {
    EN_PLANTA("En Planta / Espera"),
    EN_PROCESAMIENTO("En Procesamiento"),
    COMPLETADO("Completado / Liquidado")
}

data class SeguimientoCarga(
    val id: Int,
    val usuarioCorreo: String,
    val planta: PlantaProcesadora,
    val mineral: String,                // Ej: "Cobre (Cu)"
    val toneladasBrutas: Double,       // Ej: 200.0 TM
    val fechaIngreso: LocalDate,        // Fecha que inicia el alquiler / entrega
    var estado: EstadoCarga = EstadoCarga.EN_PLANTA,
    var observaciones: String = ""
) {
    // Calcula los días exactos transcurridos desde el ingreso
    val diasEnPlanta: Long
        get() = ChronoUnit.DAYS.between(fechaIngreso, LocalDate.now()).coerceAtLeast(0)

    // Estimación del costo acumulado por alquiler de planta hasta la fecha
    val costoAlquilerAcumulado: Double
        get() = toneladasBrutas * planta.costoAlquilerPorTM
}

// Repositorio en memoria para los seguimientos de carga
object SeguimientosRepository {
    val lista = mutableListOf<SeguimientoCarga>()

    fun obtenerPorUsuario(correo: String): List<SeguimientoCarga> {
        return lista.filter { it.usuarioCorreo == correo }
    }

    fun agregarSeguimiento(seguimiento: SeguimientoCarga) {
        lista.add(0, seguimiento) // Agrega al inicio
    }
}