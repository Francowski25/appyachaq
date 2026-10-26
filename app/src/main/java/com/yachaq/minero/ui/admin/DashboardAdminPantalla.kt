package com.yachaq.minero.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.yachaq.minero.data.remote.model.LoginResponse
import com.yachaq.minero.ui.componentes.AppTopBar
import com.yachaq.minero.ui.modulos.CalculadoraPantalla
import com.yachaq.minero.ui.modulos.PlantaPantalla
import com.yachaq.minero.ui.modulos.HistorialPantalla
import com.yachaq.minero.ui.modulos.UsuarioPantalla
import com.yachaq.minero.ui.modulos.GestionPlantaPantalla

@Composable
fun DashboardAdminPantalla(
    usuario: LoginResponse,
    onCerrarSesion: () -> Unit
) {
    var tabSeleccionada by remember { mutableIntStateOf(0) }

    val tituloActual = when (tabSeleccionada) {
        0 -> "Calculadora Minera"
        1 -> "Plantas Mineras"
        2 -> "Historial de Operaciones"
        3 -> "Gestión de Usuarios"
        4 -> "Gestión de Plantas"
        else -> "Minero Yachaq"
    }

    AppTopBar(
        titulo = tituloActual,
        usuario = usuario,
        tabSeleccionada = tabSeleccionada,
        onSeleccionarTab = { tabSeleccionada = it },
        onCerrarSesion = onCerrarSesion
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (tabSeleccionada) {
                0 -> CalculadoraPantalla()
                1 -> PlantaPantalla()
                2 -> HistorialPantalla()
                3 -> UsuarioPantalla()
                4 -> GestionPlantaPantalla()
                else -> CalculadoraPantalla()
            }
        }
    }
}