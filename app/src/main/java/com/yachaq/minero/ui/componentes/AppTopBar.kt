package com.yachaq.minero.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.model.LoginResponse
import com.yachaq.minero.data.remote.model.SesionUsuario
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    titulo: String,
    usuario: LoginResponse,
    tabSeleccionada: Int,
    onSeleccionarTab: (Int) -> Unit,
    onCerrarSesion: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val esAdmin = SesionUsuario.esAdmin()

    // Estado local sincronizado con el usuario y SesionUsuario
    var esPremiumLocal by remember(usuario.isPremium) { mutableStateOf(usuario.isPremium) }

    // Actualizar estado local si cambia la sesión globalmente
    LaunchedEffect(usuario.isPremium) {
        esPremiumLocal = usuario.isPremium
    }

    // Identidad visual Premium (Admin o Usuario Premium)
    val esPremiumOAdmin = esPremiumLocal || esAdmin

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppSidebar(
                usuario = usuario,
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
                TopAppBar(
                    title = {
                        Text(
                            text = titulo,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú lateral",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // BOTÓN DE CAMBIO DE PLAN (solo para usuarios mineros regulares)
                        if (!esAdmin) {
                            Surface(
                                onClick = {
                                    SesionUsuario.alternarPlanPremiumSimulado()
                                    esPremiumLocal = SesionUsuario.usuarioActual?.isPremium ?: !esPremiumLocal
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (esPremiumLocal) AmarilloMineroBrillante.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (esPremiumLocal) AmarilloMineroBrillante else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (esPremiumLocal) Icons.Default.Star else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (esPremiumLocal) AmarilloMineroBrillante else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (esPremiumLocal) "PREMIUM" else "FREE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (esPremiumLocal) AmarilloMineroBrillante else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        // Amarillo si es Premium o Admin, de lo contrario el surface estándar
                        containerColor = if (esPremiumOAdmin) AmarilloMinero.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surface
                    )
                )
            },
            content = content
        )
    }
}