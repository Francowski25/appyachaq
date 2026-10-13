package com.yachaq.minero.logica

import android.util.Patterns

/**
 * Representa el resultado de la validación.
 */
sealed class ResultadoValidacion {
    object Exito : ResultadoValidacion()
    data class Error(val mensaje: String) : ResultadoValidacion()
}

/**
 * Clase encargada de validar las credenciales de un usuario.
 */
object ValidadorUsuario {

    /**
     * Valida si el nombre completo es válido.
     */
    fun validarNombre(nombre: String): ResultadoValidacion {
        if (nombre.trim().isBlank()) {
            return ResultadoValidacion.Error("El nombre no puede estar vacío.")
        }
        if (nombre.trim().length < 3) {
            return ResultadoValidacion.Error("El nombre debe tener al menos 3 caracteres.")
        }
        return ResultadoValidacion.Exito
    }

    /**
     * Valida el formato del correo electrónico.
     */
    fun validarCorreo(correo: String): ResultadoValidacion {
        val correoLimpio = correo.trim()
        if (correoLimpio.isEmpty()) {
            return ResultadoValidacion.Error("El correo electrónico es requerido.")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            return ResultadoValidacion.Error("Ingrese un correo electrónico válido (ejemplo@correo.com).")
        }
        return ResultadoValidacion.Exito
    }

    /**
     * Valida la fortaleza de la contraseña:
     * - Mínimo 6 caracteres
     * - Debe contener al menos una letra y un número
     */
    fun validarContrasena(clave: String): ResultadoValidacion {
        if (clave.isEmpty()) {
            return ResultadoValidacion.Error("La contraseña no puede estar vacía.")
        }
        if (clave.length < 6) {
            return ResultadoValidacion.Error("La contraseña debe tener al menos 6 caracteres.")
        }
        val tieneLetra = clave.any { it.isLetter() }
        val tieneNumero = clave.any { it.isDigit() }

        if (!tieneLetra || !tieneNumero) {
            return ResultadoValidacion.Error("La contraseña debe incluir al menos una letra y un número.")
        }
        return ResultadoValidacion.Exito
    }

    /**
     * Valida de manera integral todos los campos antes de guardar.
     */
    fun validarFormularioCompleto(
        nombre: String,
        correo: String,
        clave: String
    ): ResultadoValidacion {
        val resNombre = validarNombre(nombre)
        if (resNombre is ResultadoValidacion.Error) return resNombre

        val resCorreo = validarCorreo(correo)
        if (resCorreo is ResultadoValidacion.Error) return resCorreo

        val resClave = validarContrasena(clave)
        if (resClave is ResultadoValidacion.Error) return resClave

        return ResultadoValidacion.Exito
    }
}