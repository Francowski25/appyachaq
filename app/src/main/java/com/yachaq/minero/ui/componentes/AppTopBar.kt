package com.yachaq.minero.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.model.SesionUsuario
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    titulo: String,
    tabSeleccionada: Int,
    onSeleccionarTab: (Int) -> Unit,
    onCerrarSesion: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    // Fuente de verdad reactiva: si SesionUsuario cambia, esto se recompone
    val usuario = SesionUsuario.usuarioActual ?: return
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography

    val esAdmin = SesionUsuario.esAdmin()
    val esPremium = usuario.isPremium
    val esPremiumOAdmin = esPremium || esAdmin

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppSidebar(
                tabSeleccionada = tabSeleccionada,
                onSeleccionarTab = { index ->
                    onSeleccionarTab(index)
                    scope.launch { drawerState.close() }
                },
                onCerrarSesion = onCerrarSesion
            )
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = titulo,
                                style = tp.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = cs.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menú lateral",
                                    tint = cs.primary
                                )
                            }
                        },
                        actions = {
                            // FREE/PREMIUM solo para usuarios no admin
                            if (!esAdmin) {
                                Surface(
                                    onClick = {
                                        SesionUsuario.alternarPlanPremiumSimulado()
                                    },
                                    shape = MaterialTheme.shapes.large,
                                    color = if (esPremium) cs.primary else cs.surfaceContainerHigh,
                                    border = BorderStroke(
                                        1.dp,
                                        if (esPremium) cs.primary else cs.outlineVariant
                                    ),
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (esPremium) Icons.Default.Star else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (esPremium) cs.onPrimary else cs.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (esPremium) "PREMIUM" else "FREE",
                                            style = tp.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.8.sp,
                                            color = if (esPremium) cs.onPrimary else cs.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = if (esPremiumOAdmin) cs.secondary.copy(alpha = 0.25f)
                            else cs.surface
                        )
                    )
                    HorizontalDivider(
                        color = if (esPremiumOAdmin) cs.secondary.copy(alpha = 0.5f)
                        else cs.outlineVariant
                    )
                }
            },
            content = content
        )
    }
}