package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.HistorialRepository
import com.yachaq.minero.data.RegistroHistorial
import com.yachaq.minero.data.SesionUsuario
import com.yachaq.minero.logica.FormateadorFecha
import com.yachaq.minero.ui.theme.*
import java.util.Locale

private fun dolares(v: Double) = "$ " + String.format(Locale.US, "%,.2f", v)

@Composable
fun HistorialPantalla() {
    val usuarioActual = SesionUsuario.usuarioActual
    val esAdmin = usuarioActual?.rol == "admin"

    // REGLA PLAN PREMIUM: Si no es Admin ni Premium, se bloquea el módulo
    if (!SesionUsuario.puedeAccederHistorial()) {
        TarjetaBloqueoHistorialPremium()
        return
    }

    val listaCompleta = remember {
        HistorialRepository.obtenerPorUsuario(usuarioActual?.correo ?: "", esAdmin)
    }

    var textoBusqueda by remember { mutableStateOf("") }
    var registroDetalleSeleccionado by remember { mutableStateOf<RegistroHistorial?>(null) }

    val listaFiltrada = remember(textoBusqueda, listaCompleta) {
        if (textoBusqueda.isBlank()) {
            listaCompleta
        } else {
            listaCompleta.filter {
                it.plantaNombre.contains(textoBusqueda, ignoreCase = true) ||
                        it.mineral.contains(textoBusqueda, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ENCABEZADO
            Text(
                text = "HISTORIAL DE LIQUIDACIONES",
                color = AmarilloMineroBrillante,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Registro histórico de cálculos y liquidaciones de concentrado",
                color = TextoGrisSecundario,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // BUSCADOR
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar por planta o mineral...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = AmarilloMineroBrillante
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GrisCampos, unfocusedContainerColor = GrisCampos,
                    focusedBorderColor = AmarilloMineroBrillante, unfocusedBorderColor = ColorGrisBorde,
                    focusedLabelColor = AmarilloMineroBrillante, unfocusedLabelColor = TextoGrisSecundario,
                    focusedTextColor = TextoBlancoSuave, unfocusedTextColor = TextoBlancoSuave
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // LISTA
            if (listaFiltrada.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = ErrorRojo,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Sin Registros en el Historial",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoBlancoSuave
                        )
                        Text(
                            text = "No se encontraron liquidaciones realizadas o coincidentes.",
                            fontSize = 12.sp,
                            color = TextoGrisSecundario,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(
                        items = listaFiltrada,
                        key = { it.id }
                    ) { registro ->
                        TarjetaHistorialCard(
                            registro = registro,
                            onClickDetalle = { registroDetalleSeleccionado = registro }
                        )
                    }
                }
            }
        }
    }

    // DIÁLOGO DE DETALLE
    registroDetalleSeleccionado?.let { reg ->
        AlertDialog(
            onDismissRequest = { registroDetalleSeleccionado = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            title = {
                Text(
                    text = "DETALLE DE LIQUIDACIÓN #${reg.id}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = AmarilloMineroBrillante
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Planta: ${reg.plantaNombre} (${reg.plantaUbicacion})", fontSize = 12.sp, color = TextoBlancoSuave, fontWeight = FontWeight.Bold)
                    Text("Mineral: ${reg.mineral}", fontSize = 12.sp, color = TextoGrisSecundario)
                    Text("Tonelaje Bruto: ${reg.toneladasBrutas} TM", fontSize = 12.sp, color = TextoGrisSecundario)
                    Text("Ley Registrada: ${reg.ley}", fontSize = 12.sp, color = TextoGrisSecundario)
                    Text("Precio Base: $ ${reg.precioUnitario}", fontSize = 12.sp, color = TextoGrisSecundario)
                    HorizontalDivider(color = ColorGrisBorde)
                    Text("Total Liquidado: $ ${String.format(Locale.US, "%,.2f", reg.totalAPagar)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = AmarilloMineroBrillante)
                }
            },
            confirmButton = {
                Button(
                    onClick = { registroDetalleSeleccionado = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AmarilloMineroBrillante, contentColor = FondoNegroAbsoluto),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ========================================================
// TARJETA DE ACCESO RESTRINGIDO POR PLAN PREMIUM
// ========================================================
@Composable
fun TarjetaBloqueoHistorialPremium() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, AmarilloMineroBrillante.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = AmarilloMineroBrillante.copy(alpha = 0.15f),
                    modifier = Modifier.size(70.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AmarilloMineroBrillante,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    text = "Módulo Exclusivo Premium",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AmarilloMineroBrillante,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "El acceso al historial completo de liquidaciones y auditoría de cálculos requiere una suscripción activa al Plan Premium.",
                    fontSize = 13.sp,
                    color = TextoGrisSecundario,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { SesionUsuario.alternarPlanPremiumSimulado() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloMineroBrillante,
                        contentColor = FondoNegroAbsoluto
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ACTIVAR PLAN PREMIUM", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    }
}

// ========================================================
// COMPONENTE TARJETA DE ÍTEM DE HISTORIAL
// ========================================================
@Composable
fun TarjetaHistorialCard(
    registro: RegistroHistorial,
    onClickDetalle: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ColorGrisBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Factory,
                        contentDescription = null,
                        tint = AmarilloMineroBrillante,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = registro.plantaNombre,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoSuave
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextoGrisSecundario,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = FormateadorFecha.formatearParaMostrar(registro.fechaRegistro),
                        fontSize = 12.sp,
                        color = TextoGrisSecundario,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(color = ColorGrisBorde)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Mineral & Carga:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text("${registro.mineral} (${registro.toneladasBrutas} TM)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TMNS Secas:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text("${registro.tmns} TMNS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GrisCampos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL LIQUIDACIÓN NETO", fontSize = 10.sp, color = TextoGrisSecundario, fontWeight = FontWeight.Bold)
                        Text(
                            text = dolares(registro.totalAPagar),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = AmarilloMineroBrillante
                        )
                    }

                    OutlinedButton(
                        onClick = onClickDetalle,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AmarilloMineroBrillante),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmarilloMineroBrillante),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Detalle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}