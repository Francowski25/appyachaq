package com.yachaq.minero.data.remote.model

data class AuthLoginRequest(
    val email: String,
    val password: String
)

data class AuthRegisterRequest(
    val email: String,
    val name: String,
    val password: String
)

data class LoginResponse(
    val idUser: String?,
    val email: String?,
    val name: String?,
    val role: String?,
    val isPremium: Boolean = false,
    val listMessage: List<String>? = null,
    val type: String? = null
)

object SesionUsuario {
    var usuarioActual: LoginResponse? = null
        private set

    fun iniciarSesion(usuario: LoginResponse) {
        usuarioActual = usuario
    }

    fun cerrarSesion() {
        usuarioActual = null
    }

    fun esAdmin(): Boolean {
        return usuarioActual?.role?.trim()?.equals("admin", ignoreCase = true) == true
    }

    fun esPremium(): Boolean {
        val u = usuarioActual ?: return false
        return u.isPremium || esAdmin()
    }

    fun alternarPlanPremiumSimulado() {
        usuarioActual = usuarioActual?.copy(isPremium = !(usuarioActual?.isPremium ?: false))
    }
}