package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yachaq.minero.data.PlantaProcesadora
import com.yachaq.minero.data.PlantasProc
import com.yachaq.minero.ui.theme.*
import java.util.Locale

@Composable
fun GestionPlantaPantalla() {
    val plantasList = remember { mutableStateListOf(*PlantasProc.lista.toTypedArray()) }

    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    // Estado para controlar qué planta tiene abierto su diálogo de términos actualmente
    var plantaSeleccionadaTerminos by remember { mutableStateOf<PlantaProcesadora?>(null) }

    var plantaAEliminar by remember { mutableStateOf<PlantaProcesadora?>(null) }

    val plantasVisibles = remember(textoBusqueda, plantasList.toList()) {
        if (textoBusqueda.isBlank()) {
            plantasList
        } else {
            plantasList.filter {
                it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                        it.ubicacion.contains(textoBusqueda, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ENCABEZADO ESTILO YACHAQ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GESTIÓN DE PLANTAS MINERAS",
                        color = AmarilloMineroBrillante,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Administración general para agregar y gestionar plantas procesadoras",
                        color = TextoGrisSecundario,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BUSCADOR INDUSTRIAL
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar por nombre o ubicación...") },
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

            // LISTA DE PLANTAS O MENSAJE VACÍO
            if (plantasVisibles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                            tint = ErrorRojo,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No se encontraron plantas",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoBlancoSuave
                        )
                        Text(
                            text = "No hay resultados para \"$textoBusqueda\". Intente con otro término.",
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
                        items = plantasVisibles,
                        key = { it.id }
                    ) { planta ->
                        PlantaAdminCard(
                            planta = planta,
                            onGestionarTerminos = { plantaSeleccionadaTerminos = planta },
                            onEliminar = { plantaAEliminar = planta }
                        )
                    }
                }
            }
        }

        // BOTÓN FLOTANTE PARA REGISTRAR NUEVA PLANTA
        FloatingActionButton(
            onClick = { mostrarDialogoCrear = true },
            containerColor = AmarilloMineroBrillante,
            contentColor = FondoNegroAbsoluto,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar Planta")
        }
    }

    // DIÁLOGO MODAL FULL-SCREEN PARA GESTIONAR TÉRMINOS DE LA PLANTA SELECCIONADA
    plantaSeleccionadaTerminos?.let { plantaActual ->
        Dialog(
            onDismissRequest = { plantaSeleccionadaTerminos = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = FondoNegroAbsoluto,
                border = BorderStroke(1.dp, ColorGrisBorde)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // DIÁLOGO MODAL FULL-SCREEN PARA GESTIONAR TÉRMINOS DE LA PLANTA SELECCIONADA
                    plantaSeleccionadaTerminos?.let { plantaActual ->
                        TerminosPlanta(
                            terminosActuales = plantaActual.terminos,
                            onGuardarTerminos = { nuevosTerminos ->
                                plantaActual.terminos = nuevosTerminos
                            },
                            onDismiss = { plantaSeleccionadaTerminos = null }
                        )
                    }

                    // Botón de Cierre en la esquina superior derecha
                    IconButton(
                        onClick = { plantaSeleccionadaTerminos = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextoGrisSecundario
                        )
                    }
                }
            }
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

    // DIÁLOGO DE CONFIRMACIÓN DE ELIMINACIÓN DE PLANTA
    plantaAEliminar?.let { planta ->
        AlertDialog(
            onDismissRequest = { plantaAEliminar = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = ErrorRojo,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Eliminar Planta Procesadora",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = ErrorRojo
                )
            },
            text = {
                Text(
                    text = "¿Está seguro de que desea eliminar la planta '${planta.nombre}'? Esta acción afectará los registros asociados.",
                    fontSize = 13.sp,
                    color = TextoGrisSecundario
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        PlantasProc.lista.remove(planta)
                        plantasList.remove(planta)
                        plantaAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRojo, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Eliminar Planta", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { plantaAEliminar = null }) {
                    Text("Cancelar", color = TextoGrisSecundario, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ========================================================
// COMPONENTE TARJETA PARA CADA PLANTA EN LA LISTA
// ========================================================
@Composable
fun PlantaAdminCard(
    planta: PlantaProcesadora,
    onGestionarTerminos: () -> Unit,
    onEliminar: () -> Unit
) {
    val esOperativa = planta.estado.equals("Operativa", ignoreCase = true)
    val estadoColor = if (esOperativa) Color(0xFF4CAF50) else Color(0xFFFF9800)

    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ColorGrisBorde),
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
                        tint = AmarilloMineroBrillante,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = planta.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoSuave
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = estadoColor
                        )
                    }

                    IconButton(onClick = onEliminar) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar planta",
                            tint = ErrorRojo
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
                    tint = TextoGrisSecundario,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = planta.ubicacion,
                    fontSize = 12.sp,
                    color = TextoGrisSecundario
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ColorGrisBorde)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Capacidad:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        text = "${planta.capacidadTMAD} TMAD",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoSuave
                    )
                }
                Column {
                    Text("Alquiler / TM:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        text = String.format(Locale.US, "$ %.2f", planta.costoAlquilerPorTM),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmarilloMineroBrillante
                    )
                }

                // BOTÓN DE TÉRMINOS INDIVIDUAL POR PLANTA
                OutlinedButton(
                    onClick = onGestionarTerminos,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AmarilloMineroBrillante),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AmarilloMineroBrillante
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Términos",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}