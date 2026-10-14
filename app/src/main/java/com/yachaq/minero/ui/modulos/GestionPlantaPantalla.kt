package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.PlantaProcesadora
import com.yachaq.minero.data.PlantasProc
import com.yachaq.minero.ui.theme.GrisCampos
import java.util.Locale

@Composable
fun GestionPlantaPantalla() {
    // Lista reactiva sincronizada con PlantasProc
    val plantasList = remember { mutableStateListOf(*PlantasProc.lista.toTypedArray()) }

    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var plantaAEliminar by remember { mutableStateOf<PlantaProcesadora?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Gestión de Plantas Mineras",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Módulo de administración para registrar y eliminar plantas procesadoras.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // LISTADO DE PLANTAS CON OPCIÓN DE ELIMINAR
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = plantasList,
                    key = { it.id }
                ) { planta ->
                    PlantaAdminCard(
                        planta = planta,
                        onEliminar = { plantaAEliminar = planta }
                    )
                }
            }
        }

        // BOTÓN FLOTANTE PARA REGISTRAR NUEVA PLANTA
        FloatingActionButton(
            onClick = { mostrarDialogoCrear = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar Planta")
        }
    }

    // DIÁLOGO PARA CREAR PLANTA
    if (mostrarDialogoCrear) {
        DialogoNuevaPlanta(
            onDismiss = { mostrarDialogoCrear = false },
            onGuardar = { nuevaPlanta ->
                PlantasProc.lista.add(nuevaPlanta)
                plantasList.add(nuevaPlanta)
                mostrarDialogoCrear = false
            }
        )
    }

    // DIÁLOGO DE CONFIRMACIÓN DE ELIMINACIÓN
    plantaAEliminar?.let { planta ->
        AlertDialog(
            onDismissRequest = { plantaAEliminar = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Eliminar Planta",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text("¿Está seguro de que desea eliminar la planta '${planta.nombre}'? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        PlantasProc.lista.remove(planta)
                        plantasList.remove(planta)
                        plantaAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { plantaAEliminar = null }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun PlantaAdminCard(
    planta: PlantaProcesadora,
    onEliminar: () -> Unit
) {
    val esOperativa = planta.estado.equals("Operativa", ignoreCase = true)
    val estadoColor = if (esOperativa) Color(0xFF4CAF50) else Color(0xFFFF9800)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Factory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = planta.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = estadoColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = planta.estado,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = estadoColor
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = planta.ubicacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Capacidad:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${planta.capacidadTMAD} TMAD", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Alquiler / TM:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(String.format(Locale.US, "$ %.2f", planta.costoAlquilerPorTM), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun DialogoNuevaPlanta(
    onDismiss: () -> Unit,
    onGuardar: (PlantaProcesadora) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }
    var costoAlquiler by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("Operativa") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Nueva Planta Procesadora",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; error = null },
                    label = { Text("Nombre de la Planta") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it; error = null },
                    label = { Text("Ubicación (Ej. Abancay, Apurímac)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = capacidad,
                    onValueChange = { capacidad = it; error = null },
                    label = { Text("Capacidad TMAD") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costoAlquiler,
                    onValueChange = { costoAlquiler = it; error = null },
                    label = { Text("Costo Alquiler por TM ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Estado Inicial:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Operativa", "Mantenimiento").forEach { opcion ->
                        val seleccionado = estado == opcion
                        Surface(
                            onClick = { estado = opcion },
                            shape = RoundedCornerShape(12.dp),
                            color = if (seleccionado) MaterialTheme.colorScheme.primary else GrisCampos,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = opcion,
                                    color = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val capDouble = capacidad.toDoubleOrNull()
                    val costoDouble = costoAlquiler.toDoubleOrNull()

                    if (nombre.isBlank() || ubicacion.isBlank() || capDouble == null || costoDouble == null) {
                        error = "Por favor ingrese todos los valores de forma válida."
                    } else {
                        val nuevoId = (PlantasProc.lista.maxOfOrNull { it.id } ?: 0) + 1
                        onGuardar(
                            PlantaProcesadora(
                                id = nuevoId,
                                nombre = nombre.trim(),
                                ubicacion = ubicacion.trim(),
                                capacidadTMAD = capDouble,
                                costoAlquilerPorTM = costoDouble,
                                estado = estado
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Planta", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}