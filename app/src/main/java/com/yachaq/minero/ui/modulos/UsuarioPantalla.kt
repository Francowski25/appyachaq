package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestUserAdmin
import com.yachaq.minero.data.remote.model.ResponseUser
import com.yachaq.minero.data.remote.model.SesionUsuario
import com.yachaq.minero.logica.ResultadoValidacion
import com.yachaq.minero.logica.ValidadorUsuario
import com.yachaq.minero.ui.theme.GrisCampos
import kotlinx.coroutines.launch

@Composable
fun UsuarioPantalla() {
    val scope = rememberCoroutineScope()
    val idUsuarioActual = SesionUsuario.usuarioActual?.idUser // AJUSTAR: campo real en LoginResponse

    // ---------- Datos desde el backend ----------
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

    // ---------- Estado de UI ----------
    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var usuarioAEliminar by remember { mutableStateOf<ResponseUser?>(null) }
    var eliminando by remember { mutableStateOf(false) }
    var errorEliminar by remember { mutableStateOf<String?>(null) }
    var procesandoId by remember { mutableStateOf<String?>(null) }
    var errorAccion by remember { mutableStateOf<String?>(null) }

    val usuariosVisibles = remember(textoBusqueda, usuarios, idUsuarioActual) {
        usuarios
            .filter { it.idUser != idUsuarioActual }
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
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Gestión de Usuarios",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Administración de accesos y estados de cuenta en el sistema.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar por nombre o correo...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            errorAccion?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        Text(errorLista.orEmpty(), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        TextButton(onClick = { recargar++ }) { Text("REINTENTAR") }
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
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No se encontraron usuarios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (textoBusqueda.isNotBlank()) {
                            Text(
                                text = "No hay resultados para \"$textoBusqueda\". Intente con otro término.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(
                        items = usuariosVisibles,
                        key = { it.idUser ?: it.hashCode() }
                    ) { usuario ->
                        UsuarioCardMejorada(
                            usuario = usuario,
                            procesando = procesandoId != null && procesandoId == usuario.idUser,
                            onToggleEstado = { alternarEstado(usuario) },
                            onEliminar = {
                                errorEliminar = null
                                usuarioAEliminar = usuario
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { mostrarDialogoAgregar = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar Usuario")
        }
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

    usuarioAEliminar?.let { u ->
        AlertDialog(
            onDismissRequest = { if (!eliminando) usuarioAEliminar = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(text = "Eliminar Usuario", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "¿Está seguro de que desea eliminar a '${u.name ?: ""}'? Esta acción no se puede deshacer.",
                        fontSize = 14.sp
                    )
                    errorEliminar?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { eliminar(u) },
                    enabled = !eliminando,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (eliminando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Eliminar", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEliminar = null }, enabled = !eliminando) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(20.dp)
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
    val activo = usuario.status ?: true
    val rol = usuario.role ?: "minero"
    val esAdmin = rol.equals("admin", ignoreCase = true)
    val rolColor = if (esAdmin) MaterialTheme.colorScheme.primary else Color(0xFFFFB300)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (activo) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (activo) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (activo) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                    Column {
                        Text(
                            text = usuario.name ?: "Sin nombre",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (activo) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = usuario.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = rolColor.copy(alpha = 0.2f)) {
                        Text(
                            text = rol.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = rolColor
                        )
                    }

                    IconButton(onClick = onEliminar, enabled = !procesando) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar usuario",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (activo) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = null,
                        tint = if (activo) Color(0xFF4CAF50) else Color(0xFFE53935),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (activo) "Cuenta Activa" else "Cuenta Inhabilitada",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (activo) Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                }

                OutlinedButton(
                    onClick = onToggleEstado,
                    enabled = !procesando,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (activo) Color(0xFFE53935) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (procesando) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (activo) "Inhabilitar" else "Habilitar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Nuevo Usuario", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Ingrese las credenciales del nuevo operador o administrador.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; error = null },
                    label = { Text("Nombre Completo") },
                    enabled = !guardando,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it; error = null },
                    label = { Text("Correo Electrónico") },
                    enabled = !guardando,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clave,
                    onValueChange = { clave = it; error = null },
                    label = { Text("Contraseña de Acceso") },
                    enabled = !guardando,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
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

                Text(
                    text = "Rol de Acceso:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

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
                            shape = RoundedCornerShape(12.dp),
                            color = if (seleccionado) MaterialTheme.colorScheme.primary else GrisCampos,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = etiqueta,
                                    color = if (seleccionado) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { guardar() },
                enabled = !guardando,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar Usuario", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !guardando) { Text("Cancelar") }
        }
    )
}