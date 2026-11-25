package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.ResponsePlant
import com.yachaq.minero.ui.theme.AdvertenciaNaranja
import com.yachaq.minero.ui.theme.ExitoVerde
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

private val ColorEstadoOperativa = ExitoVerde
private val ColorEstadoMantenimiento = AdvertenciaNaranja

private enum class FiltroPlanta(val etiqueta: String) {
    TODAS("Todas"),
    OPERATIVAS("Operativas"),
    MANTENIMIENTO("Mantenimiento")
}

private fun ResponsePlant.estaOperativa() = status.equals("Operativa", ignoreCase = true)

@Composable
fun GestionPlantaPantalla() {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes

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
                    shape = formas.medium,
                    color = cs.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Factory,
                            contentDescription = null,
                            tint = cs.primary
                        )
                    }
                }
                Column {
                    Text(
                        text = "Gestión de Plantas",
                        style = tp.titleLarge,
                        color = cs.primary
                    )
                    Text(
                        text = "Plantas procesadoras y sus términos por defecto",
                        style = tp.bodySmall,
                        color = cs.onSurfaceVariant
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
                        tint = cs.primary
                    )
                },
                singleLine = true,
                shape = formas.large,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cs.surfaceContainerHigh,
                    unfocusedContainerColor = cs.surfaceContainerHigh,
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.outlineVariant,
                    focusedLabelColor = cs.primary,
                    unfocusedLabelColor = cs.onSurfaceVariant,
                    focusedTextColor = cs.onSurface,
                    unfocusedTextColor = cs.onSurface
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
                                style = tp.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cs.primaryContainer,
                            selectedLabelColor = cs.onPrimaryContainer
                        )
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
                        Text(errorLista.orEmpty(), style = tp.bodyMedium, color = cs.error)
                        TextButton(onClick = { recargar++ }) {
                            Text("REINTENTAR", style = tp.labelLarge)
                        }
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
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (plantas.isEmpty()) "No hay plantas registradas" else "No se encontraron plantas",
                            style = tp.titleMedium,
                            color = cs.onSurface
                        )
                        if (textoBusqueda.isNotBlank()) {
                            Text(
                                text = "No hay resultados para \"$textoBusqueda\". Intente con otro término.",
                                style = tp.bodySmall,
                                color = cs.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        } else if (plantas.isNotEmpty()) {
                            Text(
                                text = "No hay plantas con el estado seleccionado.",
                                style = tp.bodySmall,
                                color = cs.onSurfaceVariant,
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
            text = { Text("Nueva planta", style = tp.labelLarge) },
            containerColor = cs.primary,
            contentColor = cs.onPrimary,
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
                    tint = cs.error
                )
            },
            title = { Text(text = "Eliminar Planta", style = tp.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "¿Está seguro de que desea eliminar la planta '${planta.name ?: ""}'? Esta acción afectará los registros asociados.",
                        style = tp.bodyMedium
                    )
                    errorEliminar?.let {
                        Text(it, style = tp.bodySmall, color = cs.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { eliminar(planta) },
                    enabled = !eliminando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.error,
                        contentColor = cs.onError
                    ),
                    shape = formas.medium
                ) {
                    if (eliminando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = cs.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Eliminar", style = tp.labelLarge)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { plantaAEliminar = null }, enabled = !eliminando) {
                    Text("Cancelar", style = tp.labelLarge, color = cs.onSurfaceVariant)
                }
            },
            containerColor = cs.surfaceContainer,
            titleContentColor = cs.onSurface,
            textContentColor = cs.onSurfaceVariant,
            shape = formas.large
        )
    }
}

@Composable
fun PlantaAdminCard(
    planta: ResponsePlant,
    onGestionarTerminos: () -> Unit,
    onEliminar: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes
    val estado = planta.status ?: "Sin estado"
    val estadoColor = if (planta.estaOperativa()) ColorEstadoOperativa else ColorEstadoMantenimiento

    Card(
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
        shape = formas.large,
        border = BorderStroke(1.dp, cs.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = formas.medium,
                    color = cs.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Factory,
                            contentDescription = null,
                            tint = cs.primary
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = planta.name ?: "Sin nombre",
                        style = tp.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = cs.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = planta.location ?: "Ubicación no especificada",
                            style = tp.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                Surface(shape = formas.small, color = estadoColor.copy(alpha = 0.2f)) {
                    Text(
                        text = estado.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = tp.labelSmall,
                        color = estadoColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Capacidad",
                        style = tp.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                    Text(
                        text = "${planta.capacityTMAD ?: 0.0} TMAD",
                        style = tp.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Alquiler / TM",
                        style = tp.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "$ %.2f", planta.rentalCostPerTM ?: 0.0),
                        style = tp.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = cs.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = cs.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onGestionarTerminos,
                    shape = formas.small,
                    border = BorderStroke(1.dp, cs.primary.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Gestionar términos", style = tp.labelMedium)
                }

                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar planta",
                        tint = cs.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}