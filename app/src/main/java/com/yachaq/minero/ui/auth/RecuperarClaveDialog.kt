package com.yachaq.minero.ui.auth

import android.util.Patterns
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.ForgotPasswordRequest
import com.yachaq.minero.data.remote.model.ResetPasswordRequest
import com.yachaq.minero.ui.auth.componentes.*
import com.yachaq.minero.ui.theme.ExitoVerde
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

private enum class PasoRecuperacion { CORREO, CODIGO, CLAVE, LISTO }

@Composable
fun RecuperarClaveDialog(
    correoInicial: String,
    onCerrar: () -> Unit,
    onClaveRestablecida: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography

    var paso by remember { mutableStateOf(PasoRecuperacion.CORREO) }
    var correo by remember { mutableStateOf(correoInicial) }
    var codigo by remember { mutableStateOf("") }
    var nuevaClave by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var mensajeEsError by remember { mutableStateOf(true) }
    var cargando by remember { mutableStateOf(false) }
    var sacudida by remember { mutableIntStateOf(0) }
    var reinicioContador by remember { mutableIntStateOf(0) }
    var segundosReenvio by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Cuenta regresiva para poder reenviar el código
    LaunchedEffect(reinicioContador) {
        if (reinicioContador > 0) {
            segundosReenvio = 60
            while (segundosReenvio > 0) {
                delay(1000)
                segundosReenvio--
            }
        }
    }

    // Al terminar, se muestra el éxito un momento y se vuelve al login
    LaunchedEffect(paso) {
        if (paso == PasoRecuperacion.LISTO) {
            delay(1500)
            onClaveRestablecida(correo.trim())
        }
    }

    fun mostrar(texto: String?, esError: Boolean = true) {
        mensaje = texto
        mensajeEsError = esError
    }

    fun enviarCodigo(esReenvio: Boolean = false) {
        val c = correo.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(c).matches()) {
            mostrar("Ingresa un correo válido")
            return
        }
        focusManager.clearFocus()
        scope.launch {
            cargando = true
            try {
                val resp = RetrofitClient.authApi.forgotPassword(ForgotPasswordRequest(email = c))
                val body = resp.body()
                when {
                    body?.exitoso == true -> {
                        codigo = ""
                        paso = PasoRecuperacion.CODIGO
                        reinicioContador++
                        mostrar(if (esReenvio) "Enviamos un nuevo código a $c" else null, esError = false)
                    }
                    body != null -> mostrar(body.mensaje)
                    else -> mostrar("Datos inválidos (código ${resp.code()})")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mostrar("No se pudo conectar: ${e.message}")
            } finally {
                cargando = false
            }
        }
    }

    // El backend valida el código junto con la nueva contraseña (un solo endpoint):
    // este paso confirma que el código está completo y habilita el siguiente.
    fun verificarCodigo(valor: String) {
        if (valor.length != 6) {
            mostrar("Ingresa los 6 dígitos del código")
            sacudida++
            return
        }
        mostrar(null)
        paso = PasoRecuperacion.CLAVE
    }

    fun cambiarClave() {
        if (nuevaClave.length < 8) {
            mostrar("La contraseña debe tener al menos 8 caracteres")
            return
        }
        if (nuevaClave != confirmarClave) {
            mostrar("Las contraseñas no coinciden")
            return
        }
        focusManager.clearFocus()
        scope.launch {
            cargando = true
            try {
                val resp = RetrofitClient.authApi.resetPassword(
                    ResetPasswordRequest(email = correo.trim(), code = codigo, newPassword = nuevaClave)
                )
                val body = resp.body()
                when {
                    body?.exitoso == true -> {
                        mostrar(null)
                        paso = PasoRecuperacion.LISTO
                    }
                    body != null -> mostrar(body.mensaje)
                    else -> mostrar("Datos inválidos (código ${resp.code()})")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mostrar("No se pudo conectar: ${e.message}")
            } finally {
                cargando = false
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!cargando && paso != PasoRecuperacion.LISTO) onCerrar() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 420.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = cs.surfaceContainer,
            border = BorderStroke(1.dp, cs.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    AnimatedVisibility(visible = paso == PasoRecuperacion.CODIGO || paso == PasoRecuperacion.CLAVE) {
                        IconButton(
                            onClick = {
                                mostrar(null)
                                paso = if (paso == PasoRecuperacion.CLAVE) PasoRecuperacion.CODIGO else PasoRecuperacion.CORREO
                            },
                            enabled = !cargando
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = cs.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (paso != PasoRecuperacion.LISTO) {
                        IconButton(onClick = onCerrar, enabled = !cargando) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = cs.onSurfaceVariant
                            )
                        }
                    }
                }

                IndicadorPasos(actual = minOf(paso.ordinal, 2))

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedContent(
                    targetState = paso,
                    transitionSpec = {
                        val avanza = targetState.ordinal > initialState.ordinal
                        val entrada = slideInHorizontally(tween(300)) { if (avanza) it / 3 else -it / 3 } + fadeIn(tween(300))
                        val salida = slideOutHorizontally(tween(300)) { if (avanza) -it / 3 else it / 3 } + fadeOut(tween(200))
                        entrada togetherWith salida
                    },
                    label = "pasosRecuperacion"
                ) { pasoActual ->
                    when (pasoActual) {
                        PasoRecuperacion.CORREO -> Column {
                            IconoPaso(Icons.Default.Email)
                            Text(
                                text = "Recuperar contraseña",
                                style = tp.titleLarge,
                                color = cs.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Escribe tu correo y te enviaremos un código de verificación de 6 dígitos.",
                                style = tp.bodyMedium,
                                color = cs.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            CampoAuth(
                                valor = correo,
                                onValorChange = {
                                    correo = it
                                    mostrar(null)
                                },
                                etiqueta = "Correo electrónico",
                                icono = Icons.Default.Email,
                                teclado = KeyboardType.Email,
                                imeAction = ImeAction.Done,
                                onDone = { enviarCodigo() },
                                enabled = !cargando
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            BotonAuth(texto = "ENVIAR CÓDIGO", cargando = cargando, onClick = { enviarCodigo() })
                        }

                        PasoRecuperacion.CODIGO -> Column {
                            IconoPaso(Icons.Default.MarkEmailRead)
                            Text(
                                text = "Verifica tu correo",
                                style = tp.titleLarge,
                                color = cs.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Ingresa el código de 6 dígitos que enviamos a ")
                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            color = cs.onSurface
                                        )
                                    ) { append(correo.trim()) }
                                },
                                style = tp.bodyMedium,
                                color = cs.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            CampoCodigo(
                                codigo = codigo,
                                onCodigoChange = {
                                    codigo = it
                                    mostrar(null)
                                },
                                esError = mensaje != null && mensajeEsError,
                                enabled = !cargando,
                                sacudir = sacudida,
                                onCompleto = { completo ->
                                    scope.launch {
                                        delay(250) // deja ver el último dígito antes de avanzar
                                        verificarCodigo(completo)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                if (segundosReenvio > 0) {
                                    Text(
                                        text = String.format(
                                            Locale.US,
                                            "Reenviar código en %d:%02d",
                                            segundosReenvio / 60,
                                            segundosReenvio % 60
                                        ),
                                        style = tp.bodySmall,
                                        color = cs.onSurfaceVariant
                                    )
                                } else {
                                    TextButton(onClick = { enviarCodigo(esReenvio = true) }, enabled = !cargando) {
                                        Text(text = "Reenviar código", style = tp.labelMedium)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            BotonAuth(
                                texto = "VERIFICAR CÓDIGO",
                                cargando = false,
                                onClick = { verificarCodigo(codigo) },
                                enabled = codigo.length == 6 && !cargando
                            )
                        }

                        PasoRecuperacion.CLAVE -> Column {
                            IconoPaso(Icons.Default.LockReset)
                            Text(
                                text = "Crea tu nueva contraseña",
                                style = tp.titleLarge,
                                color = cs.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Elige una contraseña segura. La usarás para ingresar a tu cuenta.",
                                style = tp.bodyMedium,
                                color = cs.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            CampoAuth(
                                valor = nuevaClave,
                                onValorChange = {
                                    nuevaClave = it
                                    mostrar(null)
                                },
                                etiqueta = "Nueva contraseña",
                                icono = Icons.Default.Lock,
                                esClave = true,
                                imeAction = ImeAction.Next,
                                enabled = !cargando
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FortalezaClave(clave = nuevaClave)
                            Spacer(modifier = Modifier.height(12.dp))
                            CampoAuth(
                                valor = confirmarClave,
                                onValorChange = {
                                    confirmarClave = it
                                    mostrar(null)
                                },
                                etiqueta = "Confirmar contraseña",
                                icono = Icons.Default.Lock,
                                esClave = true,
                                imeAction = ImeAction.Done,
                                onDone = { cambiarClave() },
                                error = if (confirmarClave.isNotEmpty() && confirmarClave != nuevaClave)
                                    "Las contraseñas no coinciden" else null,
                                enabled = !cargando
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            BotonAuth(
                                texto = "CAMBIAR CONTRASEÑA",
                                cargando = cargando,
                                onClick = { cambiarClave() },
                                enabled = nuevaClave.length >= 8 && nuevaClave == confirmarClave
                            )
                            // Si el servidor rechazó el código, se puede volver a escribirlo
                            if (mensaje != null && mensajeEsError) {
                                TextButton(
                                    onClick = {
                                        codigo = ""
                                        mostrar(null)
                                        paso = PasoRecuperacion.CODIGO
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Cambiar el código", style = tp.labelLarge)
                                }
                            }
                        }

                        PasoRecuperacion.LISTO -> {
                            var aparece by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { aparece = true }
                            val escala by animateFloatAsState(
                                targetValue = if (aparece) 1f else 0f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "escalaExito"
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ExitoVerde,
                                    modifier = Modifier.size(84.dp).scale(escala)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "¡Contraseña actualizada!",
                                    style = tp.titleLarge,
                                    color = cs.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Te llevamos al inicio de sesión.",
                                    style = tp.bodyMedium,
                                    color = cs.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                MensajeAuth(mensaje = mensaje, esError = mensajeEsError)
            }
        }
    }
}

@Composable
private fun IndicadorPasos(actual: Int, total: Int = 3) {
    val cs = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            val color by animateColorAsState(
                targetValue = if (i <= actual) cs.primary else cs.outlineVariant,
                label = "paso$i"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun IconoPaso(icono: ImageVector) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(cs.primary.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = cs.primary,
            modifier = Modifier.size(28.dp)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}