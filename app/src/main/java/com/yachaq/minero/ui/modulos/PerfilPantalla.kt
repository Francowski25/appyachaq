package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestUserAdmin
import com.yachaq.minero.data.remote.model.SesionUsuario
import com.yachaq.minero.logica.ResultadoValidacion
import com.yachaq.minero.logica.ValidadorUsuario
import com.yachaq.minero.ui.auth.componentes.FortalezaClave
import com.yachaq.minero.ui.auth.componentes.requisitosClave
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.launch

private val ColorExito = Color(0xFF4CAF50)

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

    val nombreOriginal = remember(usuario.name) { usuario.name.orEmpty() }
    val correoOriginal = remember(usuario.email) { usuario.email.orEmpty() }

    var nombre by remember(usuario.name) { mutableStateOf(nombreOriginal) }
    var correo by remember(usuario.email) { mutableStateOf(correoOriginal) }

    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }
    var mostrarConfirmar by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    val cambiaCorreo = correo.trim() != correoOriginal
    val cambiaPassword = password.isNotEmpty() || confirmarPassword.isNotEmpty()
    val hayCambios = nombre.trim() != nombreOriginal || cambiaCorreo || cambiaPassword

    val requisitosCumplidos = requisitosClave(password).all { it.cumple }

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
                    mostrarPassword = false
                    mostrarConfirmar = false
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

    val colorCampos = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = GrisCampos,
        unfocusedContainerColor = GrisCampos,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Consulte y actualice sus datos personales",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .size(84.dp)
                        .border(2.dp, AmarilloMineroBrillante, CircleShape),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = inicialesPerfil(usuario.name),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = AmarilloMineroBrillante
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = usuario.name ?: "Usuario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = usuario.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmarilloMinero.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AmarilloMinero)
                    ) {
                        Text(
                            text = rol,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmarilloMineroBrillante
                        )
                    }
                    val planActivo = esPremium || esAdmin
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (planActivo) AmarilloMineroBrillante else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, AmarilloMineroBrillante)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            if (planActivo) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = FondoNegroAbsoluto,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = etiquetaPlan,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (planActivo) FondoNegroAbsoluto
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "DATOS PERSONALES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        if (it.length <= 100) nombre = it
                        error = null
                        exito = null
                    },
                    label = { Text("Nombre completo") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    enabled = !guardando,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(14.dp),
                    colors = colorCampos,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        if (it.length <= 150) correo = it
                        error = null
                        exito = null
                    },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    enabled = !guardando,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = colorCampos,
                    modifier = Modifier.fillMaxWidth()
                )

                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "CAMBIAR CONTRASEÑA (OPCIONAL)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        if (it.length <= 100) password = it
                        error = null
                        exito = null
                    },
                    label = { Text("Nueva contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                            Icon(
                                imageVector = if (mostrarPassword) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = if (mostrarPassword) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    visualTransformation = if (mostrarPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    enabled = !guardando,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = colorCampos,
                    modifier = Modifier.fillMaxWidth()
                )

                if (password.isNotEmpty()) {
                    FortalezaClave(
                        clave = password,
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = confirmarPassword,
                    onValueChange = {
                        if (it.length <= 100) confirmarPassword = it
                        error = null
                        exito = null
                    },
                    label = { Text("Confirmar nueva contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { mostrarConfirmar = !mostrarConfirmar }) {
                            Icon(
                                imageVector = if (mostrarConfirmar) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = if (mostrarConfirmar) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    visualTransformation = if (mostrarConfirmar) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    enabled = !guardando,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = colorCampos,
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmarPassword.isNotEmpty() &&
                            password.isNotEmpty() &&
                            confirmarPassword != password
                )

                if (requisitosCumplidos &&
                    confirmarPassword.isNotEmpty() &&
                    confirmarPassword != password
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Las contraseñas no coinciden.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp).padding(top = 1.dp)
                    )
                    Text(
                        text = "Deje ambos campos de contraseña vacíos si no desea cambiarla. " +
                                "Su rol y el estado de su cuenta solo pueden ser modificados por un administrador.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                error?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                exito?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ColorExito,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(it, color = ColorExito, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            nombre = nombreOriginal
                            correo = correoOriginal
                            password = ""
                            confirmarPassword = ""
                            mostrarPassword = false
                            mostrarConfirmar = false
                            error = null
                            exito = null
                        },
                        enabled = hayCambios && !guardando,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Descartar", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { alPulsarGuardar() },
                        enabled = hayCambios && !guardando,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Guardar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
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
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
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
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(text = mensaje.trim(), fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacion = false
                        guardar()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Sí, cambiar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") }
            }
        )
    }
}