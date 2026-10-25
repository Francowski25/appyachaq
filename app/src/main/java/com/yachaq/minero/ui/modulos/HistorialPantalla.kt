package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.ResponseHistorial
import com.yachaq.minero.data.remote.model.SesionUsuario
import com.yachaq.minero.logica.FormateadorFecha
import com.yachaq.minero.ui.theme.*
import java.util.Locale

private fun dolares(v: Double) = "$ " + String.format(Locale.US, "%,.2f", v)

@Composable
fun HistorialPantalla() {
    val idUser = SesionUsuario.usuarioActual?.idUser // AJUSTAR: campo real en LoginResponse

    var registros by remember { mutableStateOf<List<ResponseHistorial>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var recargar by remember { mutableIntStateOf(0) }

    LaunchedEffect(idUser, recargar) {
        if (idUser.isNullOrBlank()) {
            error = "Sesión no válida. Vuelva a iniciar sesión."
            cargando = false
            return@LaunchedEffect
        }
        cargando = true
        error = null
        try {
            val resp = RetrofitClient.calculationApi.obtenerHistorial(idUser)
            if (resp.isSuccessful) {
                registros = resp.body().orEmpty()
            } else {
                error = "Error al cargar el historial (${resp.code()})"
            }
        } catch (e: Exception) {
            error = "No se pudo conectar: ${e.message}"
        } finally {
            cargando = false
        }
    }

    var textoBusqueda by remember { mutableStateOf("") }
    var detalle by remember { mutableStateOf<ResponseHistorial?>(null) }

    val filtrados = remember(textoBusqueda, registros) {
        if (textoBusqueda.isBlank()) registros
        else registros.filter { (it.material ?: "").contains(textoBusqueda, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                fontSize = 12.sp
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar por mineral...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AmarilloMineroBrillante)
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

            Spacer(Modifier.height(16.dp))

            when {
                cargando -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AmarilloMineroBrillante)
                }

                error != null -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error.orEmpty(), color = ErrorRojo, fontSize = 13.sp, textAlign = TextAlign.Center)
                        TextButton(onClick = { recargar++ }) {
                            Text("REINTENTAR", color = AmarilloMineroBrillante)
                        }
                    }
                }

                filtrados.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = ErrorRojo, modifier = Modifier.size(48.dp))
                        Text("Sin Registros en el Historial", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave)
                        Text(
                            "No se encontraron liquidaciones realizadas o coincidentes.",
                            fontSize = 12.sp,
                            color = TextoGrisSecundario,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(items = filtrados, key = { it.idCalculation ?: it.hashCode() }) { registro ->
                        TarjetaHistorialCard(registro = registro, onClickDetalle = { detalle = registro })
                    }
                }
            }
        }
    }

    detalle?.let { reg ->
        AlertDialog(
            onDismissRequest = { detalle = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            title = {
                Text(
                    "DETALLE DE LIQUIDACIÓN",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = AmarilloMineroBrillante
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mineral: ${reg.material ?: "-"}", fontSize = 12.sp, color = TextoBlancoSuave, fontWeight = FontWeight.Bold)
                    Text("Tonelaje Bruto: ${reg.tonelaje ?: 0.0} TM", fontSize = 12.sp, color = TextoGrisSecundario)
                    Text("Ley Registrada: ${reg.ley ?: 0.0}", fontSize = 12.sp, color = TextoGrisSecundario)
                    Text("Precio Base: $ ${reg.precio ?: 0.0}", fontSize = 12.sp, color = TextoGrisSecundario)
                    Text(
                        "TMNS: ${String.format(Locale.US, "%.2f", reg.tmns ?: 0.0)}",
                        fontSize = 12.sp,
                        color = TextoGrisSecundario
                    )
                    HorizontalDivider(color = ColorGrisBorde)
                    Text(
                        "Total Liquidado: ${dolares(reg.totalAPagar ?: 0.0)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = AmarilloMineroBrillante
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { detalle = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AmarilloMineroBrillante, contentColor = FondoNegroAbsoluto),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cerrar", fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
fun TarjetaHistorialCard(
    registro: ResponseHistorial,
    onClickDetalle: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ColorGrisBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = registro.material ?: "Sin mineral",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoBlancoSuave
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextoGrisSecundario,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = registro.calculatedAt?.let { FormateadorFecha.formatearParaMostrar(it) } ?: "-",
                        fontSize = 12.sp,
                        color = TextoGrisSecundario,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(color = ColorGrisBorde)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Carga bruta:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        "${registro.tonelaje ?: 0.0} TM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoSuave
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TMNS Secas:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        "${String.format(Locale.US, "%.2f", registro.tmns ?: 0.0)} TMNS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoSuave
                    )
                }
            }

            Surface(shape = RoundedCornerShape(12.dp), color = GrisCampos, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL LIQUIDACIÓN NETO", fontSize = 10.sp, color = TextoGrisSecundario, fontWeight = FontWeight.Bold)
                        Text(
                            text = dolares(registro.totalAPagar ?: 0.0),
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