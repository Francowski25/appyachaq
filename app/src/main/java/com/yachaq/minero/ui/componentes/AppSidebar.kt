package com.yachaq.minero.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yachaq.minero.data.remote.model.SesionUsuario
import com.yachaq.minero.ui.modulos.PerfilPantalla
import com.yachaq.minero.ui.theme.AmarilloMinero
import com.yachaq.minero.ui.theme.AmarilloMineroBrillante

data class DrawerItem(
    val index: Int,
    val label: String,
    val icon: ImageVector,
    val isAdminOnly: Boolean = false,
    val isMineroOnly: Boolean = false
)

private fun iniciales(nombre: String?): String {
    val partes = (nombre?.trim()?.split(" ")?.filter { it.isNotBlank() }).orEmpty()
    return when {
        partes.isEmpty() -> ""
        partes.size == 1 -> partes[0].take(1).uppercase()
        else -> (partes[0].take(1) + partes[1].take(1)).uppercase()
    }
}

@Composable
private fun EncabezadoSeccionMenu(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 28.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun PerfilDialogo(onCerrar: () -> Unit) {
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                IconButton(
                    onClick = onCerrar,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    PerfilPantalla()
                }
            }
        }
    }
}

@Composable
fun AppSidebar(
    tabSeleccionada: Int,
    onSeleccionarTab: (Int) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val usuario = SesionUsuario.usuarioActual ?: return

    val esRolAdmin = usuario.role?.trim()?.equals("admin", ignoreCase = true) == true
    val esPremium = usuario.isPremium || esRolAdmin

    var mostrarPerfil by remember { mutableStateOf(false) }

    val menuItems = listOf(
        DrawerItem(0, "Calculadora Minera", Icons.Default.Calculate),
        DrawerItem(1, "Plantas Mineras", Icons.Default.Factory),
        DrawerItem(6, "Mi Planta y Términos", Icons.Default.Tune, isMineroOnly = true),
        DrawerItem(2, "Historial de Operaciones", Icons.Default.History),
        DrawerItem(3, "Gestión de Usuarios", Icons.Default.SupervisorAccount, isAdminOnly = true),
        DrawerItem(4, "Gestión de Plantas", Icons.Default.Factory, isAdminOnly = true),
        DrawerItem(5, "Precios del Mercado", Icons.Default.AttachMoney, isAdminOnly = true)
    )

    val visibles = menuItems.filter { (!it.isAdminOnly || esRolAdmin) && (!it.isMineroOnly || !esRolAdmin) }
    val secciones = listOf(
        "GENERAL" to visibles.filter { !it.isAdminOnly },
        "ADMINISTRACIÓN" to visibles.filter { it.isAdminOnly }
    ).filter { it.second.isNotEmpty() }

    val coloresItem = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = AmarilloMineroBrillante.copy(alpha = 0.16f),
        unselectedContainerColor = Color.Transparent,
        selectedIconColor = AmarilloMineroBrillante,
        selectedTextColor = AmarilloMineroBrillante,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurface
    )

    if (mostrarPerfil) {
        PerfilDialogo(onCerrar = { mostrarPerfil = false })
    }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.width(300.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (esPremium) {
                            listOf(AmarilloMinero.copy(alpha = 0.30f), MaterialTheme.colorScheme.surface)
                        } else {
                            listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)
                        }
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .border(2.dp, AmarilloMineroBrillante, CircleShape),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val letras = iniciales(usuario.name)
                        if (letras.isNotEmpty()) {
                            Text(
                                text = letras,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = AmarilloMineroBrillante
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = AmarilloMineroBrillante,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = usuario.name ?: "Usuario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = usuario.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmarilloMinero.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AmarilloMinero)
                    ) {
                        Text(
                            text = (usuario.role ?: "MINERO").uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmarilloMineroBrillante
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (esPremium) AmarilloMineroBrillante else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, AmarilloMineroBrillante)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (esPremium) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = when {
                                    esRolAdmin -> "ADMIN"
                                    usuario.isPremium -> "PREMIUM"
                                    else -> "FREE"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (esPremium) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 4.dp)
        ) {
            secciones.forEach { (titulo, items) ->
                EncabezadoSeccionMenu(titulo)
                items.forEach { item ->
                    val seleccionado = tabSeleccionada == item.index
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = item.label,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        selected = seleccionado,
                        onClick = { onSeleccionarTab(item.index) },
                        shape = RoundedCornerShape(14.dp),
                        colors = coloresItem,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        NavigationDrawerItem(
            label = { Text("Mi Perfil", fontWeight = FontWeight.Medium) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            selected = mostrarPerfil,
            onClick = { mostrarPerfil = true },
            shape = RoundedCornerShape(14.dp),
            colors = coloresItem,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 2.dp)
        )

        NavigationDrawerItem(
            label = { Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick = onCerrarSesion,
            shape = RoundedCornerShape(14.dp),
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 12.dp)
        )
    }
}