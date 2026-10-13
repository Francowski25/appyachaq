package com.yachaq.minero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.yachaq.minero.data.Usuario
import com.yachaq.minero.ui.admin.DashboardAdminPantalla
import com.yachaq.minero.ui.auth.LoginPantalla
import com.yachaq.minero.ui.auth.RegistroPantalla
import com.yachaq.minero.ui.minero.DashboardMineroPantalla
import com.yachaq.minero.ui.theme.MineroAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MineroAppTheme {
                var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
                var mostrarRegistro by remember { mutableStateOf(false) }

                when {
                    // Si no hay usuario autenticado, muestra Login o Registro
                    usuarioActual == null -> {
                        if (mostrarRegistro) {
                            RegistroPantalla(
                                onRegistroExitoso = { usuarioCreado ->
                                    // Inicia sesión automáticamente con el usuario recién creado
                                    usuarioActual = usuarioCreado
                                    mostrarRegistro = false
                                },
                                onVolverALogin = {
                                    mostrarRegistro = false
                                }
                            )
                        } else {
                            LoginPantalla(
                                onLoginExitoso = { usuarioAutenticado ->
                                    usuarioActual = usuarioAutenticado
                                },
                                onIrARegistro = {
                                    mostrarRegistro = true
                                }
                            )
                        }
                    }
                    // Si el usuario es Administrador
                    usuarioActual?.rol == "admin" -> {
                        DashboardAdminPantalla(
                            usuario = usuarioActual!!,
                            onCerrarSesion = { usuarioActual = null }
                        )
                    }
                    // Si el usuario es Minero
                    usuarioActual?.rol == "minero" -> {
                        DashboardMineroPantalla(
                            usuario = usuarioActual!!,
                            onCerrarSesion = { usuarioActual = null }
                        )
                    }
                }
            }
        }
    }
}