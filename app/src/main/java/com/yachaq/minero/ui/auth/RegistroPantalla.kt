package com.yachaq.minero.ui.auth

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.AuthRegisterRequest
import com.yachaq.minero.data.remote.model.LoginResponse
import com.yachaq.minero.logica.ResultadoValidacion
import com.yachaq.minero.logica.ValidadorUsuario
import com.yachaq.minero.ui.auth.componentes.*
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroPantalla(
    onRegistroExitoso: (LoginResponse) -> Unit,
    onVolverALogin: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    // Los errores de cada campo aparecen al salir del campo o al intentar registrarse
    var tocoNombre by remember { mutableStateOf(false) }
    var tocoCorreo by remember { mutableStateOf(false) }
    var intento by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val correoValido = Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()
    val errorNombre = if ((tocoNombre || intento) && nombre.isBlank()) "Ingresa tu nombre completo" else null
    val errorCorreo = if ((tocoCorreo || intento) && !correoValido) "Ingresa un correo válido" else null
    val errorClave = if (intento && clave.isBlank()) "Ingresa una contraseña" else null
    val errorConfirmar = when {
        confirmar.isNotEmpty() && confirmar != clave -> "Las contraseñas no coinciden"
        intento && confirmar.isEmpty() -> "Confirma tu contraseña"
        else -> null
    }

    fun ejecutarRegistro() {
        intento = true
        errorMensaje = null

        if (nombre.isBlank() || !correoValido || clave.isBlank() || confirmar != clave) return

        focusManager.clearFocus()

        when (val resultado = ValidadorUsuario.validarFormularioCompleto(nombre, correo, clave)) {
            is ResultadoValidacion.Exito -> {
                val correoLimpio = correo.trim()
                val nombreLimpio = nombre.trim()

                scope.launch {
                    cargando = true
                    try {
                        val resp = RetrofitClient.authApi.register(
                            AuthRegisterRequest(
                                email = correoLimpio,
                                name = nombreLimpio,
                                password = clave
                            )
                        )
                        if (resp.isSuccessful) {
                            val datos = resp.body()
                            if (datos?.type == "SUCCESS") {
                                errorMensaje = null
                                onRegistroExitoso(datos)
                            } else {
                                errorMensaje = datos?.listMessage?.firstOrNull() ?: "Respuesta inválida del servidor"
                            }
                        } else {
                            errorMensaje = resp.mensajeDeError() ?: when (resp.code()) {
                                409 -> "El correo ya se encuentra registrado en el sistema."
                                400 -> "Datos inválidos. Revise el formulario."
                                else -> "Error del servidor (${resp.code()})"
                            }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: IOException) {
                        errorMensaje = "No se pudo conectar al servidor"
                    } catch (e: Exception) {
                        errorMensaje = "Ocurrió un error inesperado: ${e.message}"
                    } finally {
                        cargando = false
                    }
                }
            }
            is ResultadoValidacion.Error -> {
                errorMensaje = resultado.mensaje
            }
        }
    }

    AuthFondo {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Crear cuenta",
                            style = tp.titleLarge,
                            color = cs.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onVolverALogin, enabled = !cargando) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = cs.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Aparecer(0) { AuthLogo(tamano = 84.dp) }

                    Spacer(modifier = Modifier.height(16.dp))

                    Aparecer(80) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Únete a Yachaq Minero",
                                style = tp.titleLarge,
                                color = cs.primary
                            )
                            Text(
                                text = "Crea tu cuenta para calcular y gestionar tus operaciones",
                                style = tp.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Aparecer(160) {
                        TarjetaAuth {
                            CampoAuth(
                                valor = nombre,
                                onValorChange = {
                                    nombre = it
                                    errorMensaje = null
                                },
                                etiqueta = "Nombre completo",
                                icono = Icons.Default.Person,
                                imeAction = ImeAction.Next,
                                error = errorNombre,
                                enabled = !cargando,
                                onPerderFoco = { tocoNombre = true }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            CampoAuth(
                                valor = correo,
                                onValorChange = {
                                    correo = it
                                    errorMensaje = null
                                },
                                etiqueta = "Correo electrónico",
                                icono = Icons.Default.Email,
                                teclado = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                error = errorCorreo,
                                enabled = !cargando,
                                onPerderFoco = { tocoCorreo = true }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            CampoAuth(
                                valor = clave,
                                onValorChange = {
                                    clave = it
                                    errorMensaje = null
                                },
                                etiqueta = "Contraseña",
                                icono = Icons.Default.Lock,
                                esClave = true,
                                imeAction = ImeAction.Next,
                                error = errorClave,
                                enabled = !cargando
                            )

                            AnimatedVisibility(visible = clave.isNotEmpty()) {
                                FortalezaClave(clave = clave, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            CampoAuth(
                                valor = confirmar,
                                onValorChange = {
                                    confirmar = it
                                    errorMensaje = null
                                },
                                etiqueta = "Confirmar contraseña",
                                icono = Icons.Default.Lock,
                                esClave = true,
                                imeAction = ImeAction.Done,
                                onDone = { ejecutarRegistro() },
                                error = errorConfirmar,
                                enabled = !cargando
                            )

                            MensajeAuth(mensaje = errorMensaje, esError = true)

                            Spacer(modifier = Modifier.height(20.dp))

                            BotonAuth(texto = "CREAR CUENTA", cargando = cargando, onClick = { ejecutarRegistro() })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Aparecer(240) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "¿Ya tienes una cuenta? ",
                                style = tp.bodyMedium,
                                color = cs.onSurfaceVariant
                            )
                            TextButton(onClick = onVolverALogin, enabled = !cargando, contentPadding = PaddingValues(0.dp)) {
                                Text(
                                    text = "Inicia sesión",
                                    style = tp.labelLarge,
                                    color = cs.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun <T> Response<T>.mensajeDeError(): String? = try {
    errorBody()?.string()
        ?.let { Gson().fromJson(it, LoginResponse::class.java) }
        ?.listMessage?.firstOrNull()
} catch (e: Exception) {
    null
}