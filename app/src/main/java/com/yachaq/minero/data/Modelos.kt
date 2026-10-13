package com.yachaq.minero.data

data class Usuario(
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: String // "admin" o "minero"
)

data class PlantaProcesadora(
    val id: Int,
    val nombre: String,
    val ubicacion: String,
    val capacidadTMAD: Double,
    val costoAlquilerPorTM: Double,
    val estado: String
)

object PlantasProc {
    val lista = mutableListOf(
        PlantaProcesadora(1, "Planta Metalúrgica Chalhuanca", "Aymaraes, Apurímac, Perú", 350.0, 55.0, "Operativa"),
        PlantaProcesadora(2, "Planta Procesadora Antabamba", "Antabamba, Apurímac, Perú", 400.0, 58.0, "Operativa"),
        PlantaProcesadora(3, "Planta Concentradora Grau", "Grau, Apurímac, Perú", 250.0, 52.0, "Mantenimiento")
    )
}

object Usuarios {
    var lista = listOf(
        Usuario("Administrador", "admin@yachaq.com", "admin123", "admin"),
        Usuario("Minero Demo", "minero@yachaq.com", "minero123", "minero")
    )

    fun validar(correo: String, clave: String): Usuario? =
        lista.find { it.correo == correo.trim() && it.clave == clave }
}