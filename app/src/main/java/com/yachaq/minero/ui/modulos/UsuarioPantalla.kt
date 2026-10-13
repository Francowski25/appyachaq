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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.Usuario
import com.yachaq.minero.data.Usuarios
import com.yachaq.minero.logica.ResultadoValidacion // <--- Importado desde logica
import com.yachaq.minero.logica.ValidadorUsuario     // <--- Importado desde logica
import com.yachaq.minero.ui.theme.GrisCampos

data class UsuarioEstado(
    val usuario: Usuario,
    var esActivo: Boolean = true
)

@Composable
fun UsuarioPantalla(usuarioActual: Usuario? = null) {
    val listaFiltrada = remember(usuarioActual) {
        Usuarios.lista
            .filter { it.correo != usuarioActual?.correo }
            .map { UsuarioEstado(it, esActivo = true) }
    }

    val usuariosEstado = remember { mutableStateListOf(*listaFiltrada.toTypedArray()) }
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var usuarioAEliminar by remember { mutableStateOf<UsuarioEstado?>(null) }

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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = usuariosEstado,
                    key = { it.usuario.correo }
                ) { item ->
                    UsuarioCardMejorada(
                        item = item,
                        onToggleEstado = {
                            val index = usuariosEstado.indexOf(item)
                            if (index != -1) {
                                usuariosEstado[index] = item.copy(esActivo = !item.esActivo)
                            }
                        },
                        onEliminar = {
                            usuarioAEliminar = item
                        }
                    )
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
            onGuardar = { nuevoUsuario ->
                Usuarios.lista = Usuarios.lista + nuevoUsuario
                usuariosEstado.add(UsuarioEstado(nuevoUsuario, esActivo = true))
                mostrarDialogoAgregar = false
            }
        )
    }

    usuarioAEliminar?.let { item ->
        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(text = "Eliminar Usuario", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "¿Está seguro de que desea eliminar a '${item.usuario.nombre}'? Esta acción no se puede deshacer.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        Usuarios.lista = Usuarios.lista.filter { it.correo != item.usuario.correo }
                        usuariosEstado.remove(item)
                        usuarioAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEliminar = null }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun UsuarioCardMejorada(
    item: UsuarioEstado,
    onToggleEstado: () -> Unit,
    onEliminar: () -> Unit
) {
    val u = item.usuario
    val esAdmin = u.rol.equals("admin", ignoreCase = true)
    val rolColor = if (esAdmin) MaterialTheme.colorScheme.primary else Color(0xFFFFB300)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (item.esActivo) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                        color = if (item.esActivo) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (item.esActivo) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                    Column {
                        Text(
                            text = u.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (item.esActivo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = u.correo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = rolColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = u.rol.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = rolColor
                        )
                    }

                    IconButton(onClick = onEliminar) {
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
                        imageVector = if (item.esActivo) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = null,
                        tint = if (item.esActivo) Color(0xFF4CAF50) else Color(0xFFE53935),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (item.esActivo) "Cuenta Activa" else "Cuenta Inhabilitada",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (item.esActivo) Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                }

                OutlinedButton(
                    onClick = onToggleEstado,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (item.esActivo) Color(0xFFE53935) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (item.esActivo) "Inhabilitar" else "Habilitar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogoNuevoUsuarioMejorado(
    onDismiss: () -> Unit,
    onGuardar: (Usuario) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("minero") }
    var claveVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it; error = null },
                    label = { Text("Correo Electrónico") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clave,
                    onValueChange = { clave = it; error = null },
                    label = { Text("Contraseña de Acceso") },
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
                            onClick = { rol = claveRol },
                            shape = RoundedCornerShape(12.dp),
                            color = if (seleccionado) MaterialTheme.colorScheme.primary else GrisCampos,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = etiqueta,
                                    color = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
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
                onClick = {
                    // AQUÍ USAMOS EL VALIDADOR DE LA CARPETA LOGICA
                    when (val resultado = ValidadorUsuario.validarFormularioCompleto(nombre, correo, clave)) {
                        is ResultadoValidacion.Exito -> {
                            onGuardar(Usuario(nombre.trim(), correo.trim(), clave, rol))
                        }
                        is ResultadoValidacion.Error -> {
                            error = resultado.mensaje
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Usuario", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}