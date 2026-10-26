package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
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
import kotlinx.coroutines.launch
import java.util.Locale

private fun dolares(v: Double) = "$ " + String.format(Locale.US, "%,.2f", v)

@Composable
fun HistorialPantalla() {
    val idUser = SesionUsuario.usuarioActual?.idUser
    val scope = rememberCoroutineScope()

    var registros by remember { mutableStateOf<List<ResponseHistorial>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var recargar by remember { mutableIntStateOf(0) }

    // Estados para la eliminación GLOBAL
    var mostrarDialogoEliminarGlobal by remember { mutableStateOf(false) }
    var eliminandoGlobal by remember { mutableStateOf(false) }
    var mensajeErrorGlobal by remember { mutableStateOf<String?>(null) }

    // Estados para la eliminación INDIVIDUAL
    var idParaEliminar by remember { mutableStateOf<String?>(null) }
    var eliminandoIndividual by remember { mutableStateOf(false) }
    var mensajeErrorIndividual by remember { mutableStateOf<String?>(null) }

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

            // BARRA DE BÚSQUEDA Y BOTÓN DE ELIMINAR GLOBAL
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { mostrarDialogoEliminarGlobal = true },
                    enabled = registros.isNotEmpty() && !cargando
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Eliminar todo el historial",
                        tint = if (registros.isNotEmpty()) ErrorRojo else TextoGrisSecundario,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

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
                        TarjetaHistorialCard(
                            registro = registro,
                            onClickDetalle = { detalle = registro },
                            onEliminar = { idParaEliminar = registro.idCalculation } // <-- NUEVO
                        )
                    }
                }
            }
        }
    }

    // DIÁLOGO PARA ELIMINAR GLOBAL
    if (mostrarDialogoEliminarGlobal) {
        AlertDialog(
            onDismissRequest = { if (!eliminandoGlobal) mostrarDialogoEliminarGlobal = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRojo, modifier = Modifier.size(32.dp)) },
            title = { Text("¿Eliminar Historial Completo?", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AmarilloMineroBrillante) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se eliminarán TODOS tus cálculos guardados. Esta acción no se puede deshacer.", fontSize = 13.sp, color = TextoGrisSecundario)
                    mensajeErrorGlobal?.let { Text(it, color = ErrorRojo, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            eliminandoGlobal = true
                            mensajeErrorGlobal = null
                            try {
                                val resp = RetrofitClient.calculationApi.eliminarTodoHistorial(idUser!!)
                                if (resp.isSuccessful && resp.body()?.type == "SUCCESS") {
                                    mostrarDialogoEliminarGlobal = false
                                    recargar++
                                } else {
                                    mensajeErrorGlobal = resp.body()?.listMessage?.firstOrNull() ?: "Error del servidor"
                                }
                            } catch (e: Exception) {
                                mensajeErrorGlobal = "Error de red: ${e.message}"
                            } finally {
                                eliminandoGlobal = false
                            }
                        }
                    },
                    enabled = !eliminandoGlobal,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRojo, contentColor = TextoBlancoSuave),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (eliminandoGlobal) CircularProgressIndicator(color = TextoBlancoSuave, modifier = Modifier.size(18.dp))
                    else Text("Sí, eliminar todo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminarGlobal = false }, enabled = !eliminandoGlobal) {
                    Text("Cancelar", color = TextoGrisSecundario)
                }
            }
        )
    }

    // DIÁLOGO PARA ELIMINAR INDIVIDUAL
    if (idParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { if (!eliminandoIndividual) idParaEliminar = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRojo, modifier = Modifier.size(32.dp)) },
            title = { Text("¿Eliminar este cálculo?", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AmarilloMineroBrillante) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Este cálculo se borrará permanentemente de tu historial.", fontSize = 13.sp, color = TextoGrisSecundario)
                    mensajeErrorIndividual?.let { Text(it, color = ErrorRojo, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            eliminandoIndividual = true
                            mensajeErrorIndividual = null
                            try {
                                val resp = RetrofitClient.calculationApi.eliminarCalculo(idUser!!, idParaEliminar!!)
                                if (resp.isSuccessful && resp.body()?.type == "SUCCESS") {
                                    idParaEliminar = null
                                    recargar++
                                } else {
                                    mensajeErrorIndividual = resp.body()?.listMessage?.firstOrNull() ?: "Error del servidor"
                                }
                            } catch (e: Exception) {
                                mensajeErrorIndividual = "Error de red: ${e.message}"
                            } finally {
                                eliminandoIndividual = false
                            }
                        }
                    },
                    enabled = !eliminandoIndividual,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRojo, contentColor = TextoBlancoSuave),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (eliminandoIndividual) CircularProgressIndicator(color = TextoBlancoSuave, modifier = Modifier.size(18.dp))
                    else Text("Sí, eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { idParaEliminar = null }, enabled = !eliminandoIndividual) {
                    Text("Cancelar", color = TextoGrisSecundario)
                }
            }
        )
    }

    // DIÁLOGO DE DETALLE (Se mantiene igual)
    detalle?.let { reg ->
        AlertDialog(
            onDismissRequest = { detalle = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            title = { Text("DETALLE DE LIQUIDACIÓN", fontWeight = FontWeight.Black, fontSize = 16.sp, color = AmarilloMineroBrillante) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mineral: ${reg.material ?: "-"}", fontSize = 12.sp, color = TextoBlancoSuave, fontWeight = FontWeight.Bold)
                    Text("Tonelaje Bruto: ${reg.tonelaje ?: 0.0} TM", fontSize = 12.sp, color = TextoGrisSecundario)
                    Text("Ley Registrada: ${reg.ley ?: 0.0}", fontSize = 12.sp, color = TextoGrisSecundario)
                    Text("Precio Base: $ ${reg.precio ?: 0.0}", fontSize = 12.sp, color = TextoGrisSecundario)
                    Text("TMNS: ${String.format(Locale.US, "%.2f", reg.tmns ?: 0.0)}", fontSize = 12.sp, color = TextoGrisSecundario)
                    HorizontalDivider(color = ColorGrisBorde)
                    Text("Total Liquidado: ${dolares(reg.totalAPagar ?: 0.0)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = AmarilloMineroBrillante)
                }
            },
            confirmButton = {
                Button(onClick = { detalle = null }, colors = ButtonDefaults.buttonColors(containerColor = AmarilloMineroBrillante, contentColor = FondoNegroAbsoluto), shape = RoundedCornerShape(12.dp)) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// TARJETA ACTUALIZADA CON EL BOTÓN DE ELIMINAR
@Composable
fun TarjetaHistorialCard(
    registro: ResponseHistorial,
    onClickDetalle: () -> Unit = {},
    onEliminar: () -> Unit = {} // <-- NUEVO CALLBACK
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
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextoGrisSecundario, modifier = Modifier.size(14.dp))
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
                    Text("${registro.tonelaje ?: 0.0} TM", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TMNS Secas:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text("${String.format(Locale.US, "%.2f", registro.tmns ?: 0.0)} TMNS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave)
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
                        Text(text = dolares(registro.totalAPagar ?: 0.0), fontSize = 18.sp, fontWeight = FontWeight.Black, color = AmarilloMineroBrillante)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // BOTÓN DE ELIMINAR INDIVIDUAL (Papelera)
                        IconButton(
                            onClick = onEliminar,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar este cálculo",
                                tint = ErrorRojo
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
}