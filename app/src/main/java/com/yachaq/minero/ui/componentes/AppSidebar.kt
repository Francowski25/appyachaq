package com.yachaq.minero.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yachaq.minero.data.remote.model.LoginResponse
import com.yachaq.minero.ui.theme.AmarilloMinero
import com.yachaq.minero.ui.theme.AmarilloMineroBrillante

data class DrawerItem(
    val index: Int,
    val label: String,
    val icon: ImageVector,
    val isAdminOnly: Boolean = false
)

@Composable
fun AppSidebar(
    usuario: LoginResponse,
    tabSeleccionada: Int,
    onSeleccionarTab: (Int) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val esRolAdmin = usuario.role?.trim()?.equals("admin", ignoreCase = true) == true
    val esPremium = usuario.isPremium || esRolAdmin

    val menuItems = listOf(
        DrawerItem(0, "Calculadora Minera", Icons.Default.Calculate),
        DrawerItem(1, "Plantas Mineras", Icons.Default.Factory),
        DrawerItem(2, "Historial de Operaciones", Icons.Default.History),
        DrawerItem(3, "Gestión de Usuarios", Icons.Default.SupervisorAccount, isAdminOnly = true),
        DrawerItem(4, "Gestión de Plantas", Icons.Default.Factory, isAdminOnly = true)
    )

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.width(300.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (esPremium) AmarilloMinero.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surface
                )
                .padding(24.dp)
        ) {
            Column {
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .border(2.dp, AmarilloMineroBrillante, CircleShape),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AmarilloMineroBrillante,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = usuario.name ?: "Usuario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = usuario.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

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
                                color = if (esPremium) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(12.dp))

        menuItems.forEach { item ->
            if (!item.isAdminOnly || esRolAdmin) {
                NavigationDrawerItem(
                    label = { Text(item.label) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    selected = tabSeleccionada == item.index,
                    onClick = { onSeleccionarTab(item.index) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        NavigationDrawerItem(
            label = { Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick = onCerrarSesion,
            modifier = Modifier.padding(12.dp)
        )
    }
}