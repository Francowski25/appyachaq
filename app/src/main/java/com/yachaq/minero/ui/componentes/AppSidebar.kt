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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yachaq.minero.data.remote.model.LoginResponse // <-- Importa tu modelo remoto
import com.yachaq.minero.ui.theme.AmarilloMinero
import com.yachaq.minero.ui.theme.AmarilloMineroBrillante

@Composable
fun AppSidebar(
    usuario: LoginResponse, // <-- Cambiado de Usuario a LoginResponse
    tabSeleccionada: Int,
    onSeleccionarTab: (Int) -> Unit,
    onCerrarSesion: () -> Unit
) {
    // Verificamos si es admin o premium usando los campos de LoginResponse
    val esRolAdmin = usuario.role?.trim()?.equals("admin", ignoreCase = true) == true
    val esPremium = usuario.isPremium || esRolAdmin

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.width(300.dp)
    ) {
        // ENCABEZADO PERFIL CON FONDO AMARILLO SI ES PREMIUM / ADMIN
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
                    // Etiqueta de ROL
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

                    // Etiqueta de PLAN
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

        // ===== OPCIONES COMUNES (DISPONIBLES PARA MINEROS Y ADMINS) =====
        NavigationDrawerItem(
            label = { Text("Calculadora Minera") },
            icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
            selected = tabSeleccionada == 0,
            onClick = { onSeleccionarTab(0) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            label = { Text("Plantas Mineras") },
            icon = { Icon(Icons.Default.Factory, contentDescription = null) },
            selected = tabSeleccionada == 1,
            onClick = { onSeleccionarTab(1) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            label = { Text("Historial de Operaciones") },
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            selected = tabSeleccionada == 3,
            onClick = { onSeleccionarTab(3) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // ===== OPCIONES EXCLUSIVAS PARA ADMINISTRADORES =====
        if (esRolAdmin) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            NavigationDrawerItem(
                label = { Text("Gestión de Usuarios") },
                icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null) },
                selected = tabSeleccionada == 4,
                onClick = { onSeleccionarTab(4) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            NavigationDrawerItem(
                label = { Text("Gestión de Plantas") },
                icon = { Icon(Icons.Default.Factory, contentDescription = null) },
                selected = tabSeleccionada == 5,
                onClick = { onSeleccionarTab(5) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // CERRAR SESIÓN
        NavigationDrawerItem(
            label = { Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick = onCerrarSesion,
            modifier = Modifier.padding(12.dp)
        )
    }
}