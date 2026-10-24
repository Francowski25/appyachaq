package com.yachaq.minero.data.remote.model

data class RequestCalculo(
    val idUser: String,
    val idPlant: String,
    val material: String,
    val tonelaje: Double,
    val ley: Double,
    val precio: Double
)

// Lo que el backend le devuelve (el desglose completo)
data class ResponseCalculo(
    val idCalculation: String? = null,
    val material: String? = null,

    val tmHumedas: Double? = null,
    val tmns: Double? = null,

    val pagoTotal: Double? = null, // El "PAGO TOTAL" del Excel
    val totalAPagar: Double? = null, // El "TOTAL A PAGAR" final

    // Desglose adicional para mostrar en la UI (opcional)
    val valorNeto: Double? = null,
    val igv: Double? = null,
    val detraccion: Double? = null,
    val alquilerTotal: Double? = null,

    val listMessage: List<String>? = null,
    val type: String? = null
)