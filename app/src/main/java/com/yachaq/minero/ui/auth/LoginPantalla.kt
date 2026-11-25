package com.yachaq.minero.ui.auth

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.AuthLoginRequest
import com.yachaq.minero.data.remote.model.LoginResponse
import com.yachaq.minero.ui.auth.componentes.*
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun LoginPantalla(
    onLoginExitoso: (LoginResponse) -> Unit,
    onIrARegistro: () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography

    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorClave by remember { mutableStateOf<String?>(null) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var exitoMensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var mostrarRecuperar by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    fun ingresar() {
        errorMensaje = null
        exitoMensaje = null
        errorCorreo = if (!Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) "Ingresa un correo válido" else null
        errorClave = if (clave.isBlank()) "Ingresa tu contraseña" else null
        if (errorCorreo != null || errorClave != null) return

        focusManager.clearFocus()
        scope.launch {
            cargando = true
            try {
                val resp = RetrofitClient.authApi.login(
                    AuthLoginRequest(email = correo.trim(), password = clave)
                )
                if (resp.isSuccessful) {
                    val datos = resp.body()
                    if (datos == null || datos.type != "SUCCESS") {
                        errorMensaje = datos?.listMessage?.firstOrNull() ?: "Respuesta inválida del servidor"
                    } else {
                        errorMensaje = null
                        onLoginExitoso(datos)
                    }
                } else {
                    errorMensaje = when (resp.code()) {
                        401, 403 -> "Correo o contraseña incorrectos"
                        else -> "Error del servidor (${resp.code()})"
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                errorMensaje = "No se pudo conectar: ${e.message}"
            } catch (e: Exception) {
                errorMensaje = "Ocurrió un error inesperado: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    if (mostrarRecuperar) {
        RecuperarClaveDialog(
            correoInicial = correo,
            onCerrar = { mostrarRecuperar = false },
            onClaveRestablecida = { correoUsado ->
                mostrarRecuperar = false
                correo = correoUsado
                clave = ""
                errorMensaje = null
                exitoMensaje = "Contraseña actualizada. Ya puedes ingresar."
            }
        )
    }

    AuthFondo {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Aparecer(0) { AuthLogo() }

                Spacer(modifier = Modifier.height(20.dp))

                Aparecer(80) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "YACHAQ MINERO",
                            style = tp.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = cs.primary,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Gestión y cálculo minero",
                            style = tp.bodyMedium,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Aparecer(160) {
                    TarjetaAuth {
                        Text(
                            text = "Iniciar sesión",
                            style = tp.titleLarge,
                            color = cs.onSurface
                        )
                        Text(
                            text = "Ingresa con tu cuenta para continuar",
                            style = tp.bodySmall,
                            color = cs.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        CampoAuth(
                            valor = correo,
                            onValorChange = {
                                correo = it
                                errorCorreo = null
                                errorMensaje = null
                                exitoMensaje = null
                            },
                            etiqueta = "Correo electrónico",
                            icono = Icons.Default.Email,
                            teclado = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            error = errorCorreo,
                            enabled = !cargando
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CampoAuth(
                            valor = clave,
                            onValorChange = {
                                clave = it
                                errorClave = null
                                errorMensaje = null
                                exitoMensaje = null
                            },
                            etiqueta = "Contraseña",
                            icono = Icons.Default.Lock,
                            esClave = true,
                            imeAction = ImeAction.Done,
                            onDone = { ingresar() },
                            error = errorClave,
                            enabled = !cargando
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = { mostrarRecuperar = true },
                                enabled = !cargando,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "¿Olvidaste tu contraseña?",
                                    style = tp.labelMedium,
                                    color = cs.primary
                                )
                            }
                        }

                        MensajeAuth(mensaje = errorMensaje, esError = true)
                        MensajeAuth(mensaje = exitoMensaje, esError = false)

                        Spacer(modifier = Modifier.height(16.dp))

                        BotonAuth(texto = "INGRESAR", cargando = cargando, onClick = { ingresar() })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Aparecer(240) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "¿No tienes una cuenta? ",
                            style = tp.bodyMedium,
                            color = cs.onSurfaceVariant
                        )
                        TextButton(onClick = onIrARegistro, contentPadding = PaddingValues(0.dp)) {
                            Text(
                                text = "Regístrate aquí",
                                style = tp.labelLarge,
                                color = cs.primary
                            )
                        }
                    }
                }
            }
        }
    }
}