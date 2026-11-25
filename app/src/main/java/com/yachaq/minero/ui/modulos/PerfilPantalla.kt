package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestUserAdmin
import com.yachaq.minero.data.remote.model.SesionUsuario
import com.yachaq.minero.logica.ResultadoValidacion
import com.yachaq.minero.logica.ValidadorUsuario
import com.yachaq.minero.ui.auth.componentes.Aparecer
import com.yachaq.minero.ui.auth.componentes.AuthFondo
import com.yachaq.minero.ui.auth.componentes.BotonAuth
import com.yachaq.minero.ui.auth.componentes.CampoAuth
import com.yachaq.minero.ui.auth.componentes.FortalezaClave
import com.yachaq.minero.ui.auth.componentes.MensajeAuth
import com.yachaq.minero.ui.auth.componentes.TarjetaAuth
import com.yachaq.minero.ui.auth.componentes.requisitosClave
import kotlinx.coroutines.launch

private fun inicialesPerfil(nombre: String?): String {
    val partes = (nombre?.trim()?.split(" ")?.filter { it.isNotBlank() }).orEmpty()
    return when {
        partes.isEmpty() -> "?"
        partes.size == 1 -> partes[0].take(1).uppercase()
        else -> (partes[0].take(1) + partes[1].take(1)).uppercase()
    }
}

@Composable
fun PerfilPantalla() {
    val usuario = SesionUsuario.usuarioActual ?: return
    val scope = rememberCoroutineScope()
    val foco = LocalFocusManager.current
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes

    val nombreOriginal = remember(usuario.name) { usuario.name.orEmpty() }
    val correoOriginal = remember(usuario.email) { usuario.email.orEmpty() }

    var nombre by remember(usuario.name) { mutableStateOf(nombreOriginal) }
    var correo by remember(usuario.email) { mutableStateOf(correoOriginal) }

    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    val cambiaCorreo = correo.trim() != correoOriginal
    val cambiaPassword = password.isNotEmpty() || confirmarPassword.isNotEmpty()
    val hayCambios = nombre.trim() != nombreOriginal || cambiaCorreo || cambiaPassword

    val requisitosCumplidos = requisitosClave(password).all { it.cumple }
    val noCoinciden = confirmarPassword.isNotEmpty() && password.isNotEmpty() && confirmarPassword != password

    val esAdmin = SesionUsuario.esAdmin()
    val esPremium = SesionUsuario.esPremium()
    val rol = (usuario.role ?: "minero").uppercase()
    val etiquetaPlan = when {
        esAdmin -> "ADMIN"
        esPremium -> "PREMIUM"
        else -> "FREE"
    }

    fun validar(): Boolean {
        val resNombre = ValidadorUsuario.validarNombre(nombre)
        if (resNombre is ResultadoValidacion.Error) {
            error = resNombre.mensaje
            return false
        }
        val resCorreo = ValidadorUsuario.validarCorreo(correo)
        if (resCorreo is ResultadoValidacion.Error) {
            error = resCorreo.mensaje
            return false
        }

        if (password.isNotEmpty() || confirmarPassword.isNotEmpty()) {
            if (!requisitosCumplidos) {
                error = "La contraseña no cumple todos los requisitos."
                return false
            }
            if (password != confirmarPassword) {
                error = "Las contraseñas no coinciden."
                return false
            }
        }
        return true
    }

    fun guardar() {
        val id = usuario.idUser ?: return
        val n = nombre.trim()
        val c = correo.trim()
        val p = password.ifEmpty { null }

        scope.launch {
            guardando = true
            error = null
            exito = null
            try {
                val resp = RetrofitClient.userApi.editarUsuario(
                    id,
                    RequestUserAdmin(
                        name = n,
                        email = c,
                        password = p,
                        role = usuario.role ?: "minero",
                        status = true
                    )
                )
                val body = resp.body()
                if (resp.isSuccessful && body?.type == "SUCCESS") {
                    val nuevoNombre = body.name ?: n
                    val nuevoCorreo = body.email ?: c
                    SesionUsuario.actualizarPerfil(nuevoNombre, nuevoCorreo)
                    nombre = nuevoNombre
                    correo = nuevoCorreo
                    password = ""
                    confirmarPassword = ""
                    exito = "Perfil actualizado correctamente."
                } else {
                    error = body?.listMessage?.firstOrNull()
                        ?: "No se pudo actualizar (${resp.code()})"
                }
            } catch (e: Exception) {
                error = "No se pudo conectar: ${e.message}"
            } finally {
                guardando = false
            }
        }
    }

    fun alPulsarGuardar() {
        exito = null
        if (!validar()) return
        if (cambiaCorreo || cambiaPassword) mostrarConfirmacion = true else guardar()
    }

    // Mismo fondo degradado con resplandor dorado de las pantallas de auth
    AuthFondo {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Aparecer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "Mi perfil",
                        style = tp.headlineMedium,
                        color = cs.onBackground
                    )
                    Text(
                        text = "Administra tu información y la seguridad de tu cuenta.",
                        style = tp.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            Aparecer(retardoMs = 80) {
                TarjetaAuth(modifier = Modifier.widthIn(max = 640.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(cs.surfaceContainerHigh, CircleShape)
                                .border(
                                    2.dp,
                                    Brush.linearGradient(listOf(cs.primary, cs.secondary)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = inicialesPerfil(usuario.name),
                                style = tp.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = cs.primary
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = usuario.name ?: "Usuario",
                                style = tp.titleLarge,
                                color = cs.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = usuario.email ?: "",
                                style = tp.bodySmall,
                                color = cs.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = cs.secondary.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = rol,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        style = tp.labelSmall,
                                        color = cs.primary
                                    )
                                }

                                val planActivo = esPremium || esAdmin
                                Row(
                                    modifier = Modifier
                                        .background(
                                            if (planActivo) {
                                                Brush.linearGradient(listOf(cs.primary, cs.secondary))
                                            } else {
                                                Brush.linearGradient(
                                                    listOf(cs.surfaceContainerHigh, cs.surfaceContainerHigh)
                                                )
                                            },
                                            CircleShape
                                        )
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (planActivo) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = cs.onPrimary
                                        )
                                    }
                                    Text(
                                        text = etiquetaPlan,
                                        style = tp.labelSmall,
                                        color = if (planActivo) cs.onPrimary else cs.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Aparecer(retardoMs = 160) {
                TarjetaAuth(modifier = Modifier.widthIn(max = 640.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Información personal",
                                style = tp.titleMedium,
                                color = cs.onSurface
                            )
                            Text(
                                text = "Actualiza los datos asociados a tu cuenta.",
                                style = tp.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }

                        CampoAuth(
                            valor = nombre,
                            onValorChange = {
                                if (it.length <= 100) nombre = it
                                error = null
                                exito = null
                            },
                            etiqueta = "Nombre completo",
                            icono = Icons.Default.Person,
                            enabled = !guardando
                        )

                        CampoAuth(
                            valor = correo,
                            onValorChange = {
                                if (it.length <= 150) correo = it
                                error = null
                                exito = null
                            },
                            etiqueta = "Correo electrónico",
                            icono = Icons.Default.Email,
                            teclado = KeyboardType.Email,
                            enabled = !guardando
                        )
                    }
                }
            }

            Aparecer(retardoMs = 240) {
                TarjetaAuth(modifier = Modifier.widthIn(max = 640.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = formas.small,
                                color = cs.primary.copy(alpha = 0.10f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = cs.primary,
                                    modifier = Modifier.padding(10.dp).size(20.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "Seguridad",
                                    style = tp.titleMedium,
                                    color = cs.onSurface
                                )
                                Text(
                                    text = "Deja estos campos vacíos si no deseas cambiar tu contraseña.",
                                    style = tp.bodySmall,
                                    color = cs.onSurfaceVariant
                                )
                            }
                        }

                        CampoAuth(
                            valor = password,
                            onValorChange = {
                                if (it.length <= 100) password = it
                                error = null
                                exito = null
                            },
                            etiqueta = "Nueva contraseña",
                            icono = Icons.Default.Lock,
                            esClave = true,
                            enabled = !guardando
                        )

                        if (password.isNotEmpty()) {
                            FortalezaClave(
                                clave = password,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        CampoAuth(
                            valor = confirmarPassword,
                            onValorChange = {
                                if (it.length <= 100) confirmarPassword = it
                                error = null
                                exito = null
                            },
                            etiqueta = "Confirmar contraseña",
                            icono = Icons.Default.Lock,
                            esClave = true,
                            imeAction = ImeAction.Done,
                            onDone = { foco.clearFocus() },
                            error = if (noCoinciden) "Las contraseñas no coinciden." else null,
                            enabled = !guardando
                        )

                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = cs.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Utiliza una contraseña segura y evita compartirla con otras personas.",
                                style = tp.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            ) {
                MensajeAuth(mensaje = error, esError = true)
                MensajeAuth(mensaje = exito, esError = false)
                if (error != null || exito != null) Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            nombre = nombreOriginal
                            correo = correoOriginal
                            password = ""
                            confirmarPassword = ""
                            error = null
                            exito = null
                        },
                        enabled = hayCambios && !guardando,
                        shape = formas.medium,
                        border = BorderStroke(1.dp, cs.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.onSurface),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Text("DESCARTAR", style = tp.labelLarge, letterSpacing = 1.sp)
                    }

                    BotonAuth(
                        texto = "GUARDAR",
                        cargando = guardando,
                        onClick = { alPulsarGuardar() },
                        enabled = hayCambios,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (mostrarConfirmacion) {
        val mensaje = buildString {
            if (cambiaCorreo) {
                append("Su nuevo correo será '${correo.trim()}'. Deberá usarlo para iniciar sesión y para recuperar su contraseña.\n\n")
            }
            if (cambiaPassword) {
                append("Su contraseña será actualizada. Deberá usarla en su próximo inicio de sesión.")
            }
        }
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            shape = formas.large,
            containerColor = cs.surfaceContainer,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = when {
                        cambiaCorreo && cambiaPassword -> "Cambiar correo y contraseña"
                        cambiaCorreo -> "Cambiar correo"
                        else -> "Cambiar contraseña"
                    },
                    style = tp.titleLarge,
                    color = cs.onSurface
                )
            },
            text = { Text(text = mensaje.trim(), style = tp.bodyMedium, color = cs.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacion = false
                        guardar()
                    },
                    shape = formas.medium
                ) { Text("Sí, cambiar", style = tp.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar", style = tp.labelLarge, color = cs.onSurfaceVariant)
                }
            }
        )
    }
}