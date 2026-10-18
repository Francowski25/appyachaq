package com.yachaq.minero.data

data class Usuario(
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: String, // "admin" o "minero"
    var plantaSeleccionadaId: Int? = null // Relación Usuario -> Planta
)

data class UsuarioEstado(
    val usuario: Usuario,
    var esActivo: Boolean = true
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
        PlantaProcesadora(1, "Utunsa", "Cotabambas, Apurímac, Perú", 15000.0, 0.0, "Por verificar"),
        PlantaProcesadora(2, "Chabuca Granda", "Grau, Apurímac, Perú", 350.0, 0.0, "Por verificar"),
        PlantaProcesadora(3, "Explorador", "Aymaraes, Apurímac, Perú", 350.0, 0.0, "Por verificar"),
        PlantaProcesadora(4, "Señor de Piscoya", "Aymaraes, Apurímac, Perú", 20.0, 0.0, "Por verificar"),
        PlantaProcesadora(5, "Los Chankas", "Andahuaylas, Apurímac, Perú", 350.0, 0.0, "Por verificar"),
        PlantaProcesadora(6, "Lambraspampa", "Andahuaylas, Apurímac, Perú", 150.0, 0.0, "Por verificar"),
        PlantaProcesadora(7, "Máximo Jesús", "Grau, Apurímac, Perú", 350.0, 0.0, "Por verificar"),
        PlantaProcesadora(8, "María Mercedes I", "Andahuaylas, Apurímac, Perú", 300.0, 0.0, "Por verificar")
    )

    fun buscarPorId(id: Int?): PlantaProcesadora? = lista.find { it.id == id }
}

object Usuarios {
    var lista = listOf(
        Usuario("Administrador General", "admin@yachaq.com", "admin123", "admin"),
        Usuario("Minero Demo", "minero@yachaq.com", "minero123", "minero", plantaSeleccionadaId = 1), // Ejemplo con Utunsa por defecto
        Usuario("Carlos Quispe", "carlos.quispe@yachaq.com", "carlos123", "minero"),
        Usuario("María Huamaní", "maria.humani@yachaq.com", "maria123", "minero"),
        Usuario("Supervisor Operativo", "supervisor@yachaq.com", "sup123", "admin")
    )

    fun validar(correo: String, clave: String): Usuario? =
        lista.find { it.correo == correo.trim() && it.clave == clave }
}

// ===== GESTOR DE SESIÓN ACTIVA =====
object SesionUsuario {
    var usuarioActual: Usuario? = Usuarios.lista[1] // Inicia con Minero Demo por defecto
        private set

    fun iniciarSesion(usuario: Usuario) {
        usuarioActual = usuario
    }

    fun cerrarSesion() {
        usuarioActual = null
    }

    fun obtenerPlantaActiva(): PlantaProcesadora? {
        val plantaId = usuarioActual?.plantaSeleccionadaId
        return PlantasProc.buscarPorId(plantaId)
    }

    fun guardarPlantaActiva(planta: PlantaProcesadora?) {
        usuarioActual?.plantaSeleccionadaId = planta?.id
    }
}