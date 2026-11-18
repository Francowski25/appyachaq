package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.ResponsePlant
import com.yachaq.minero.ui.theme.GrisCampos
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

private val ColorEstadoOperativa = Color(0xFF4CAF50)
private val ColorEstadoMantenimiento = Color(0xFFFF9800)

private enum class FiltroPlanta(val etiqueta: String) {
    TODAS("Todas"),
    OPERATIVAS("Operativas"),
    MANTENIMIENTO("Mantenimiento")
}

private fun ResponsePlant.estaOperativa() = status.equals("Operativa", ignoreCase = true)

@Composable
fun GestionPlantaPantalla() {
    val scope = rememberCoroutineScope()

    var plantas by remember { mutableStateOf<List<ResponsePlant>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorLista by remember { mutableStateOf<String?>(null) }
    var recargar by remember { mutableIntStateOf(0) }

    LaunchedEffect(recargar) {
        cargando = true
        errorLista = null
        try {
            val resp = RetrofitClient.plantApi.listarPlantas()
            if (resp.isSuccessful) {
                plantas = resp.body().orEmpty()
            } else {
                errorLista = "Error al cargar plantas (${resp.code()})"
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorLista = "No se pudo conectar: ${e.message}"
        } finally {
            cargando = false
        }
    }

    var textoBusqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf(FiltroPlanta.TODAS) }
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var plantaSeleccionadaTerminos by remember { mutableStateOf<ResponsePlant?>(null) }
    var plantaAEliminar by remember { mutableStateOf<ResponsePlant?>(null) }
    var eliminando by remember { mutableStateOf(false) }
    var errorEliminar by remember { mutableStateOf<String?>(null) }

    val totalOperativas = plantas.count { it.estaOperativa() }
    val totalMantenimiento = plantas.size - totalOperativas

    val plantasVisibles = remember(textoBusqueda, plantas, filtro) {
        plantas
            .filter { p ->
                when (filtro) {
                    FiltroPlanta.TODAS -> true
                    FiltroPlanta.OPERATIVAS -> p.estaOperativa()
                    FiltroPlanta.MANTENIMIENTO -> !p.estaOperativa()
                }
            }
            .filter {
                textoBusqueda.isBlank() ||
                        (it.name ?: "").contains(textoBusqueda, ignoreCase = true) ||
                        (it.location ?: "").contains(textoBusqueda, ignoreCase = true)
            }
    }

    fun eliminar(planta: ResponsePlant) {
        val id = planta.idPlant ?: return
        scope.launch {
            eliminando = true
            errorEliminar = null
            try {
                val resp = RetrofitClient.plantApi.eliminarPlanta(id)
                val body = resp.body()
                if (resp.isSuccessful && body?.type == "SUCCESS") {
                    plantas = plantas.filterNot { it.idPlant == id }
                    plantaAEliminar = null
                } else {
                    errorEliminar = body?.listMessage?.firstOrNull()
                        ?: "No se pudo eliminar (${resp.code()})"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorEliminar = "No se pudo conectar: ${e.message}"
            } finally {
                eliminando = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Factory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column {
                    Text(
                        text = "Gestión de Plantas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Plantas procesadoras y sus términos por defecto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar por nombre o ubicación...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GrisCampos,
                    unfocusedContainerColor = GrisCampos,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroPlanta.entries.forEach { f ->
                    val cantidad = when (f) {
                        FiltroPlanta.TODAS -> plantas.size
                        FiltroPlanta.OPERATIVAS -> totalOperativas
                        FiltroPlanta.MANTENIMIENTO -> totalMantenimiento
                    }
                    FilterChip(
                        selected = filtro == f,
                        onClick = { filtro = f },
                        label = {
                            Text(
                                text = "${f.etiqueta} ($cantidad)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                cargando -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                errorLista != null -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorLista.orEmpty(), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        TextButton(onClick = { recargar++ }) { Text("REINTENTAR") }
                    }
                }

                plantasVisibles.isEmpty() -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (plantas.isEmpty()) "No hay plantas registradas" else "No se encontraron plantas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (textoBusqueda.isNotBlank()) {
                            Text(
                                text = "No hay resultados para \"$textoBusqueda\". Intente con otro término.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        } else if (plantas.isNotEmpty()) {
                            Text(
                                text = "No hay plantas con el estado seleccionado.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(
                        items = plantasVisibles,
                        key = { it.idPlant ?: it.hashCode() }
                    ) { planta ->
                        PlantaAdminCard(
                            planta = planta,
                            onGestionarTerminos = { plantaSeleccionadaTerminos = planta },
                            onEliminar = {
                                errorEliminar = null
                                plantaAEliminar = planta
                            }
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { mostrarDialogoCrear = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Nueva planta", fontWeight = FontWeight.Bold) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }

    plantaSeleccionadaTerminos?.let { planta ->
        val id = planta.idPlant
        if (id != null) {
            TerminosPlanta(
                idPlant = id,
                nombrePlanta = planta.name ?: "",
                onDismiss = { plantaSeleccionadaTerminos = null }
            )
        } else {
            plantaSeleccionadaTerminos = null
        }
    }

    if (mostrarDialogoCrear) {
        DialogoNuevaPlanta(
            onDismiss = { mostrarDialogoCrear = false },
            onCreada = {
                mostrarDialogoCrear = false
                recargar++
            }
        )
    }

    plantaAEliminar?.let { planta ->
        AlertDialog(
            onDismissRequest = { if (!eliminando) plantaAEliminar = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(text = "Eliminar Planta", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "¿Está seguro de que desea eliminar la planta '${planta.name ?: ""}'? Esta acción afectará los registros asociados.",
                        fontSize = 14.sp
                    )
                    errorEliminar?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { eliminar(planta) },
                    enabled = !eliminando,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (eliminando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Eliminar", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { plantaAEliminar = null }, enabled = !eliminando) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun PlantaAdminCard(
    planta: ResponsePlant,
    onGestionarTerminos: () -> Unit,
    onEliminar: () -> Unit
) {
    val estado = planta.status ?: "Sin estado"
    val estadoColor = if (planta.estaOperativa()) ColorEstadoOperativa else ColorEstadoMantenimiento

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Factory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = planta.name ?: "Sin nombre",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = planta.location ?: "Ubicación no especificada",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(shape = RoundedCornerShape(8.dp), color = estadoColor.copy(alpha = 0.2f)) {
                    Text(
                        text = estado.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = estadoColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Capacidad",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${planta.capacityTMAD ?: 0.0} TMAD",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Alquiler / TM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "$ %.2f", planta.rentalCostPerTM ?: 0.0),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onGestionarTerminos,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gestionar términos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar planta",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}