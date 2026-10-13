package com.yachaq.minero.ui.minero

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.yachaq.minero.data.Usuario
import com.yachaq.minero.ui.componentes.AppTopBar
import com.yachaq.minero.ui.modulos.CalculadoraPantalla
import com.yachaq.minero.ui.modulos.PlantaPantalla

@Composable
fun DashboardMineroPantalla(
    usuario: Usuario,
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
            }
        }
    }
}