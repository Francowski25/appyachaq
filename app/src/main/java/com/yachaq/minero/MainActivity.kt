package com.yachaq.minero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.yachaq.minero.data.remote.model.LoginResponse
import com.yachaq.minero.data.remote.model.SesionUsuario
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
                var usuarioActual by remember {
                    mutableStateOf<LoginResponse?>(SesionUsuario.usuarioActual)
                }
                var mostrarRegistro by remember { mutableStateOf(false) }

                val usuario = usuarioActual

                val cerrarSesion = {
                    SesionUsuario.cerrarSesion()
                    usuarioActual = null
                }

                when {
                    // Login / Registro mientras no haya sesión iniciada
                    usuario == null -> {
                        if (mostrarRegistro) {
                            RegistroPantalla(
                                onRegistroExitoso = { datos ->
                                    SesionUsuario.iniciarSesion(datos)
                                    usuarioActual = datos
                                    mostrarRegistro = false
                                },
                                onVolverALogin = { mostrarRegistro = false }
                            )
                        } else {
                            LoginPantalla(
                                onLoginExitoso = { datos ->
                                    SesionUsuario.iniciarSesion(datos)
                                    usuarioActual = datos
                                },
                                onIrARegistro = { mostrarRegistro = true }
                            )
                        }
                    }

                    // Dashboard Administrador
                    SesionUsuario.esAdmin() -> {
                        DashboardAdminPantalla(
                            usuario = usuario,
                            onCerrarSesion = cerrarSesion
                        )
                    }

                    // Dashboard Minero (cualquier rol que no sea admin)
                    else -> {
                        DashboardMineroPantalla(
                            usuario = usuario,
                            onCerrarSesion = cerrarSesion
                        )
                    }
                }
            }
        }
    }
}