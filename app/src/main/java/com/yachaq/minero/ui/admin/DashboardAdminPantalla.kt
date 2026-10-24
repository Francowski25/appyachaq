package com.yachaq.minero.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.yachaq.minero.data.remote.model.LoginResponse
import com.yachaq.minero.ui.componentes.AppTopBar
import com.yachaq.minero.ui.modulos.CalculadoraPantalla
import com.yachaq.minero.ui.modulos.PlantaPantalla
import com.yachaq.minero.ui.modulos.SeguimientoPantalla
import com.yachaq.minero.ui.modulos.HistorialPantalla
import com.yachaq.minero.ui.modulos.UsuarioPantalla
import com.yachaq.minero.ui.modulos.GestionPlantaPantalla

@Composable
fun DashboardAdminPantalla(
    usuario: LoginResponse,
    onCerrarSesion: () -> Unit
) {
    var tabSeleccionada by remember { mutableIntStateOf(0) }

    AppTopBar(
        titulo = "Minero Yachaq",
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
                2 -> SeguimientoPantalla()
                3 -> HistorialPantalla()
                4 -> UsuarioPantalla(usuarioActual = usuario)
                5 -> GestionPlantaPantalla()
            }
        }
    }
}