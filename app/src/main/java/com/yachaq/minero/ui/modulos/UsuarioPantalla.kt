package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestUserAdmin
import com.yachaq.minero.data.remote.model.ResponseUser
import com.yachaq.minero.data.remote.model.SesionUsuario
import com.yachaq.minero.logica.ResultadoValidacion
import com.yachaq.minero.logica.ValidadorUsuario
import com.yachaq.minero.ui.theme.AdvertenciaNaranja
import com.yachaq.minero.ui.theme.ExitoVerde
import kotlinx.coroutines.launch

private enum class FiltroEstado(val etiqueta: String) {
    TODOS("Todos"),
    ACTIVOS("Activos"),
    INHABILITADOS("Inhabilitados")
}

private fun inicialesUsuario(nombre: String?): String {
    val partes = (nombre?.trim()?.split(" ")?.filter { it.isNotBlank() }).orEmpty()
    return when {
        partes.isEmpty() -> "?"
        partes.size == 1 -> partes[0].take(1).uppercase()
        else -> (partes[0].take(1) + partes[1].take(1)).uppercase()
    }
}

@Composable
private fun coloresCampoUsuario(): TextFieldColors {
    val cs = MaterialTheme.colorScheme
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = cs.surfaceContainerHigh,
        unfocusedContainerColor = cs.surfaceContainerHigh,
        focusedBorderColor = cs.primary,
        unfocusedBorderColor = cs.outlineVariant,
        focusedLabelColor = cs.primary,
        unfocusedLabelColor = cs.onSurfaceVariant,
        focusedTextColor = cs.onSurface,
        unfocusedTextColor = cs.onSurface
    )
}

@Composable
fun UsuarioPantalla() {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes
    val idUsuarioActual = SesionUsuario.usuarioActual?.idUser

    var usuarios by remember { mutableStateOf<List<ResponseUser>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorLista by remember { mutableStateOf<String?>(null) }
    var recargar by remember { mutableIntStateOf(0) }

    LaunchedEffect(recargar) {
        cargando = true
        errorLista = null
        try {
            val resp = RetrofitClient.userApi.listarUsuarios()
            if (resp.isSuccessful) {
                usuarios = resp.body().orEmpty()
            } else {
                errorLista = "Error al cargar usuarios (${resp.code()})"
            }
        } catch (e: Exception) {
            errorLista = "No se pudo conectar: ${e.message}"
        } finally {
            cargando = false
        }
    }

    var textoBusqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf(FiltroEstado.TODOS) }
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var usuarioCambioEstado by remember { mutableStateOf<ResponseUser?>(null) }
    var usuarioAEliminar by remember { mutableStateOf<ResponseUser?>(null) }
    var eliminando by remember { mutableStateOf(false) }
    var errorEliminar by remember { mutableStateOf<String?>(null) }
    var procesandoId by remember { mutableStateOf<String?>(null) }
    var errorAccion by remember { mutableStateOf<String?>(null) }

    val baseUsuarios = remember(usuarios, idUsuarioActual) {
        usuarios.filter { it.idUser != idUsuarioActual }
    }
    val totalActivos = baseUsuarios.count { it.status ?: true }
    val totalInhabilitados = baseUsuarios.size - totalActivos

    val usuariosVisibles = remember(textoBusqueda, baseUsuarios, filtro) {
        baseUsuarios
            .filter { u ->
                val activo = u.status ?: true
                when (filtro) {
                    FiltroEstado.TODOS -> true
                    FiltroEstado.ACTIVOS -> activo
                    FiltroEstado.INHABILITADOS -> !activo
                }
            }
            .filter {
                textoBusqueda.isBlank() ||
                        (it.name ?: "").contains(textoBusqueda, ignoreCase = true) ||
                        (it.email ?: "").contains(textoBusqueda, ignoreCase = true)
            }
    }

    fun alternarEstado(u: ResponseUser) {
        val id = u.idUser ?: return
        scope.launch {
            procesandoId = id
            errorAccion = null
            try {
                val resp = RetrofitClient.userApi.editarUsuario(
                    id,
                    RequestUserAdmin(
                        name = u.name.orEmpty(),
                        email = u.email.orEmpty(),
                        password = null,
                        role = u.role ?: "minero",
                        status = !(u.status ?: true)
                    )
                )
                val body = resp.body()
                if (resp.isSuccessful && body?.type == "SUCCESS") {
                    usuarios = usuarios.map { if (it.idUser == id) body else it }
                } else {
                    errorAccion = body?.listMessage?.firstOrNull()
                        ?: "No se pudo actualizar (${resp.code()})"
                }
            } catch (e: Exception) {
                errorAccion = "No se pudo conectar: ${e.message}"
            } finally {
                procesandoId = null
            }
        }
    }

    fun eliminar(u: ResponseUser) {
        val id = u.idUser ?: return
        scope.launch {
            eliminando = true
            errorEliminar = null
            try {
                val resp = RetrofitClient.userApi.eliminarUsuario(id)
                val body = resp.body()
                if (resp.isSuccessful && body?.type == "SUCCESS") {
                    usuarios = usuarios.filterNot { it.idUser == id }
                    usuarioAEliminar = null
                } else {
                    errorEliminar = body?.listMessage?.firstOrNull()
                        ?: "No se pudo eliminar (${resp.code()})"
                }
            } catch (e: Exception) {
                errorEliminar = "No se pudo conectar: ${e.message}"
            } finally {
                eliminando = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = formas.medium,
                    color = cs.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SupervisorAccount,
                            contentDescription = null,
                            tint = cs.primary
                        )
                    }
                }
                Column {
                    Text(
                        text = "Gestión de Usuarios",
                        style = tp.titleLarge,
                        color = cs.primary
                    )
                    Text(
                        text = "Accesos y estados de cuenta del sistema",
                        style = tp.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar por nombre o correo...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = cs.primary
                    )
                },
                singleLine = true,
                shape = formas.large,
                modifier = Modifier.fillMaxWidth(),
                colors = coloresCampoUsuario()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FiltroEstado.values().forEach { f ->
                    val cantidad = when (f) {
                        FiltroEstado.TODOS -> baseUsuarios.size
                        FiltroEstado.ACTIVOS -> totalActivos
                        FiltroEstado.INHABILITADOS -> totalInhabilitados
                    }
                    FilterChip(
                        selected = filtro == f,
                        onClick = { filtro = f },
                        label = { Text(text = "${f.etiqueta} ($cantidad)", style = tp.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cs.primaryContainer,
                            selectedLabelColor = cs.onPrimaryContainer
                        )
                    )
                }
            }

            errorAccion?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, style = tp.bodySmall, color = cs.error, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                cargando -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                errorLista != null -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorLista.orEmpty(), style = tp.bodyMedium, color = cs.error)
                        TextButton(onClick = { recargar++ }) {
                            Text("REINTENTAR", style = tp.labelLarge)
                        }
                    }
                }

                usuariosVisibles.isEmpty() -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No se encontraron usuarios",
                            style = tp.titleMedium,
                            color = cs.onSurface
                        )
                        if (textoBusqueda.isNotBlank()) {
                            Text(
                                text = "No hay resultados para \"$textoBusqueda\". Intente con otro término.",
                                style = tp.bodySmall,
                                color = cs.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(
                        items = usuariosVisibles,
                        key = { it.idUser ?: it.hashCode() }
                    ) { usuario ->
                        UsuarioCardMejorada(
                            usuario = usuario,
                            procesando = procesandoId != null && procesandoId == usuario.idUser,
                            onToggleEstado = {
                                errorAccion = null
                                usuarioCambioEstado = usuario
                            },
                            onEliminar = {
                                errorEliminar = null
                                usuarioAEliminar = usuario
                            }
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { mostrarDialogoAgregar = true },
            icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
            text = { Text("Nuevo usuario", style = tp.labelLarge) },
            containerColor = cs.primary,
            contentColor = cs.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }

    if (mostrarDialogoAgregar) {
        DialogoNuevoUsuarioMejorado(
            onDismiss = { mostrarDialogoAgregar = false },
            onCreado = {
                mostrarDialogoAgregar = false
                recargar++
            }
        )
    }

    usuarioCambioEstado?.let { u ->
        val activo = u.status ?: true
        val color = if (activo) cs.error else ExitoVerde
        AlertDialog(
            onDismissRequest = { usuarioCambioEstado = null },
            shape = formas.large,
            containerColor = cs.surfaceContainer,
            icon = {
                Icon(
                    imageVector = if (activo) Icons.Default.Block else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (activo) "Inhabilitar cuenta" else "Habilitar cuenta",
                    style = tp.titleLarge
                )
            },
            text = {
                Text(
                    text = if (activo) {
                        "¿Desea inhabilitar a '${u.name ?: ""}'? No podrá iniciar sesión hasta que vuelva a habilitar su cuenta."
                    } else {
                        "¿Desea habilitar a '${u.name ?: ""}'? Podrá volver a iniciar sesión en el sistema."
                    },
                    style = tp.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        usuarioCambioEstado = null
                        alternarEstado(u)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color,
                        contentColor = if (activo) cs.onError else cs.onTertiary
                    ),
                    shape = formas.medium
                ) {
                    Text(if (activo) "Sí, inhabilitar" else "Sí, habilitar", style = tp.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioCambioEstado = null }) {
                    Text("Cancelar", style = tp.labelLarge, color = cs.onSurfaceVariant)
                }
            }
        )
    }

    usuarioAEliminar?.let { u ->
        AlertDialog(
            onDismissRequest = { if (!eliminando) usuarioAEliminar = null },
            shape = formas.large,
            containerColor = cs.surfaceContainer,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = cs.error
                )
            },
            title = { Text(text = "Eliminar Usuario", style = tp.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "¿Está seguro de que desea eliminar a '${u.name ?: ""}'? Esta acción no se puede deshacer.",
                        style = tp.bodyMedium
                    )
                    errorEliminar?.let {
                        Text(it, style = tp.bodySmall, color = cs.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { eliminar(u) },
                    enabled = !eliminando,
                    colors = ButtonDefaults.buttonColors(containerColor = cs.error, contentColor = cs.onError),
                    shape = formas.medium
                ) {
                    if (eliminando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = cs.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Eliminar", style = tp.labelLarge)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEliminar = null }, enabled = !eliminando) {
                    Text("Cancelar", style = tp.labelLarge, color = cs.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun UsuarioCardMejorada(
    usuario: ResponseUser,
    procesando: Boolean,
    onToggleEstado: () -> Unit,
    onEliminar: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes
    val activo = usuario.status ?: true
    val rol = usuario.role ?: "minero"
    val esAdmin = rol.equals("admin", ignoreCase = true)
    val rolColor = if (esAdmin) cs.primary else AdvertenciaNaranja
    val estadoColor = if (activo) ExitoVerde else cs.error
    val colorAvatar = if (activo) cs.primary else cs.onSurfaceVariant

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (activo) cs.surfaceContainer else cs.surfaceContainer.copy(alpha = 0.5f)
        ),
        shape = formas.large,
        border = BorderStroke(1.dp, cs.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = formas.medium,
                    color = colorAvatar.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = inicialesUsuario(usuario.name),
                            style = tp.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = colorAvatar
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = usuario.name ?: "Sin nombre",
                        style = tp.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (activo) cs.onSurface else cs.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = usuario.email ?: "",
                        style = tp.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = cs.onSurfaceVariant
                    )
                }

                Surface(shape = formas.small, color = rolColor.copy(alpha = 0.2f)) {
                    Text(
                        text = rol.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = tp.labelSmall,
                        color = rolColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).background(estadoColor, CircleShape))
                Text(
                    text = if (activo) "Cuenta activa" else "Cuenta inhabilitada",
                    style = tp.labelMedium,
                    color = estadoColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = cs.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val colorToggle = if (activo) cs.error else cs.primary
                OutlinedButton(
                    onClick = onToggleEstado,
                    enabled = !procesando,
                    shape = formas.small,
                    border = BorderStroke(1.dp, colorToggle.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colorToggle),
                    modifier = Modifier.weight(1f)
                ) {
                    if (procesando) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = if (activo) Icons.Default.Block else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (activo) "Inhabilitar" else "Habilitar", style = tp.labelMedium)
                    }
                }

                IconButton(onClick = onEliminar, enabled = !procesando) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar usuario",
                        tint = cs.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogoNuevoUsuarioMejorado(
    onDismiss: () -> Unit,
    onCreado: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("minero") }
    var claveVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }

    fun guardar() {
        when (val validacion = ValidadorUsuario.validarFormularioCompleto(nombre, correo, clave)) {
            is ResultadoValidacion.Error -> error = validacion.mensaje
            is ResultadoValidacion.Exito -> scope.launch {
                guardando = true
                error = null
                try {
                    val resp = RetrofitClient.userApi.crearUsuario(
                        RequestUserAdmin(
                            name = nombre.trim(),
                            email = correo.trim(),
                            password = clave,
                            role = rol,
                            status = true
                        )
                    )
                    val body = resp.body()
                    if (resp.isSuccessful && body?.type == "SUCCESS") {
                        onCreado()
                    } else {
                        error = body?.listMessage?.firstOrNull()
                            ?: "Error del servidor (${resp.code()})"
                    }
                } catch (e: Exception) {
                    error = "No se pudo conectar: ${e.message}"
                } finally {
                    guardando = false
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        shape = formas.large,
        containerColor = cs.surfaceContainer,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = cs.primary
                )
                Text("Nuevo Usuario", style = tp.titleLarge, color = cs.onSurface)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Ingrese las credenciales del nuevo operador o administrador.",
                    style = tp.bodySmall,
                    color = cs.onSurfaceVariant
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; error = null },
                    label = { Text("Nombre Completo") },
                    enabled = !guardando,
                    singleLine = true,
                    shape = formas.medium,
                    colors = coloresCampoUsuario(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it; error = null },
                    label = { Text("Correo Electrónico") },
                    enabled = !guardando,
                    singleLine = true,
                    shape = formas.medium,
                    colors = coloresCampoUsuario(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clave,
                    onValueChange = { clave = it; error = null },
                    label = { Text("Contraseña de Acceso") },
                    enabled = !guardando,
                    singleLine = true,
                    shape = formas.medium,
                    colors = coloresCampoUsuario(),
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { claveVisible = !claveVisible }) {
                            Icon(
                                imageVector = if (claveVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    }
                )

                Text(text = "Rol de Acceso:", style = tp.labelMedium, color = cs.primary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val roles = listOf("minero" to "Minero", "admin" to "Administrador")
                    roles.forEach { (claveRol, etiqueta) ->
                        val seleccionado = rol == claveRol
                        Surface(
                            selected = seleccionado,
                            onClick = { if (!guardando) rol = claveRol },
                            shape = formas.medium,
                            color = if (seleccionado) cs.primary else cs.surfaceContainerHigh,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = etiqueta,
                                    style = tp.labelMedium,
                                    color = if (seleccionado) cs.onPrimary else cs.onSurface
                                )
                            }
                        }
                    }
                }

                error?.let {
                    Text(it, style = tp.bodySmall, color = cs.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { guardar() },
                enabled = !guardando,
                shape = formas.medium
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = cs.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar Usuario", style = tp.labelLarge)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !guardando) {
                Text("Cancelar", style = tp.labelLarge, color = cs.onSurfaceVariant)
            }
        }
    )
}