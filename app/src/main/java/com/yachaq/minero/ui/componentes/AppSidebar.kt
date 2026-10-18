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
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yachaq.minero.data.Usuario
import com.yachaq.minero.ui.theme.AmarilloMinero
import com.yachaq.minero.ui.theme.AmarilloMineroBrillante

@Composable
fun AppSidebar(
    usuario: Usuario,
    tabSeleccionada: Int,
    onSeleccionarTab: (Int) -> Unit,
    onCerrarSesion: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.width(300.dp)
    ) {
        // ENCABEZADO PERFIL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
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
                    text = usuario.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmarilloMinero.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, AmarilloMinero)
                ) {
                    Text(
                        text = usuario.rol.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AmarilloMineroBrillante
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(12.dp))

        // ===== OPCIONES COMUNES =====
        // Tab 0: Calculadora Minera
        NavigationDrawerItem(
            label = { Text("Calculadora Minera") },
            icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
            selected = tabSeleccionada == 0,
            onClick = { onSeleccionarTab(0) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Tab 1: Plantas Mineras
        NavigationDrawerItem(
            label = { Text("Plantas Mineras") },
            icon = { Icon(Icons.Default.Factory, contentDescription = null) },
            selected = tabSeleccionada == 1,
            onClick = { onSeleccionarTab(1) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Tab 2: Seguimiento de Carga (NUEVO MÓDULO)
        NavigationDrawerItem(
            label = { Text("Seguimiento de Carga") },
            icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
            selected = tabSeleccionada == 2,
            onClick = { onSeleccionarTab(2) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // ===== OPCIONES EXCLUSIVAS PARA ADMINISTRADORES =====
        if (usuario.rol == "admin") {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Tab 3: Gestión de Usuarios
            NavigationDrawerItem(
                label = { Text("Gestión de Usuarios") },
                icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null) },
                selected = tabSeleccionada == 3,
                onClick = { onSeleccionarTab(3) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Tab 4: Gestión de Plantas
            NavigationDrawerItem(
                label = { Text("Gestión de Plantas") },
                icon = { Icon(Icons.Default.Factory, contentDescription = null) },
                selected = tabSeleccionada == 4,
                onClick = { onSeleccionarTab(4) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Tab 5: Gestión de Calculadora (Términos)
            NavigationDrawerItem(
                label = { Text("Gestión de Calculadora") },
                icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
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