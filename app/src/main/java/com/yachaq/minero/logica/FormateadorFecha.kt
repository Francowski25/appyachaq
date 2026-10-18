package com.yachaq.minero.logica

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object FormateadorFecha {

    fun obtenerFechaActualTexto(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().toString()
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.format(Date())
        }
    }

    fun formatearParaMostrar(fechaStr: String): String {
        return try {
            val partes = fechaStr.split("-")
            if (partes.size == 3) {
                "${partes[2]}/${partes[1]}/${partes[0]}"
            } else fechaStr
        } catch (e: Exception) {
            fechaStr
        }
    }

    fun validarYObtenerFecha(fechaStr: String): LocalDate? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // Para minSdk 24 sin desugaring, validación mediante SimpleDateFormat
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                sdf.isLenient = false
                sdf.parse(fechaStr.trim())
                LocalDate.parse(fechaStr.trim())
            } catch (e: Exception) {
                null
            }
        }
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            LocalDate.parse(fechaStr.trim(), formatter)
        } catch (e: Exception) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun esPosteriorAHoy(fecha: LocalDate): Boolean {
        return fecha.isAfter(LocalDate.now())
    }
}