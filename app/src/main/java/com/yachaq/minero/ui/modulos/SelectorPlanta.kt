package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale

// Planta lista para elegir en la calculadora (del sistema o personal del minero)
data class PlantaCalculo(
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val alquiler: Double,
    val esPersonal: Boolean,
    val favorita: Boolean
)

fun PlantaCalculo.alquilerTexto(): String = String.format(Locale.US, "%.2f", alquiler)

// Ubicación y costo de alquiler por TM
@Composable
fun PlantaMeta(planta: PlantaCalculo, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = cs.onSurfaceVariant,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = planta.ubicacion.ifBlank { "Sin ubicación" },
            style = tp.bodySmall,
            color = cs.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Text(
            text = "  •  $ ${planta.alquilerTexto()}/TM",
            style = tp.labelSmall,
            color = cs.primary,
            maxLines = 1
        )
    }
}

@Composable
private fun IconoPlanta(activo: Boolean) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (activo) cs.primary else cs.background,
                MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Factory,
            contentDescription = null,
            tint = if (activo) cs.onPrimary else cs.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}

// Estrella si es favorita, PERSONAL si es propia
@Composable
private fun EtiquetaPlanta(planta: PlantaCalculo) {
    val cs = MaterialTheme.colorScheme
    when {
        planta.esPersonal -> Text(
            "PERSONAL",
            style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant
        )
        planta.favorita -> Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Favorita",
            tint = cs.primary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ItemPlanta(planta: PlantaCalculo, seleccionada: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = cs.surfaceContainerHigh,
        border = BorderStroke(
            width = if (seleccionada) 1.5.dp else 1.dp,
            color = if (seleccionada) cs.primary else cs.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconoPlanta(activo = seleccionada)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = planta.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        color = cs.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    EtiquetaPlanta(planta)
                }
                PlantaMeta(planta)
            }
            if (seleccionada) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seleccionada",
                    tint = cs.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun EncabezadoGrupo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

// Elegir una planta la confirma al instante, sin botón extra
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorPlanta(
    plantas: List<PlantaCalculo>,
    seleccionada: PlantaCalculo?,
    onSeleccionar: (PlantaCalculo) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    var abierto by remember { mutableStateOf(false) }

    Surface(
        onClick = { abierto = true },
        shape = MaterialTheme.shapes.medium,
        color = cs.surfaceContainerHigh,
        border = BorderStroke(
            width = 1.dp,
            color = if (seleccionada != null) cs.primary.copy(alpha = 0.7f) else cs.outlineVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconoPlanta(activo = seleccionada != null)
            Column(modifier = Modifier.weight(1f)) {
                if (seleccionada == null) {
                    Text("Elige una planta procesadora", style = tp.titleSmall, color = cs.onSurface)
                    Text("Define el alquiler por TM", style = tp.bodySmall, color = cs.onSurfaceVariant)
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = seleccionada.nombre,
                            style = tp.titleSmall,
                            color = cs.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        EtiquetaPlanta(seleccionada)
                    }
                    PlantaMeta(seleccionada)
                }
            }
            Text(
                text = if (seleccionada == null) "ELEGIR" else "CAMBIAR",
                style = tp.labelMedium,
                color = cs.primary,
                fontWeight = FontWeight.Black
            )
        }
    }

    if (abierto) {
        // Omite los grupos vacíos
        val grupos = listOf(
            "FAVORITAS" to plantas.filter { !it.esPersonal && it.favorita },
            "MIS PLANTAS" to plantas.filter { it.esPersonal },
            "PLANTAS DEL SISTEMA" to plantas.filter { !it.esPersonal && !it.favorita }
        ).filter { it.second.isNotEmpty() }

        ModalBottomSheet(
            onDismissRequest = { abierto = false },
            containerColor = cs.surfaceContainer
        ) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Selecciona la planta",
                        style = tp.titleLarge,
                        color = cs.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Al cambiar de planta se reinicia el resultado.",
                        style = tp.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
                grupos.forEach { (titulo, lista) ->
                    item(key = "titulo-$titulo") { EncabezadoGrupo(titulo) }
                    items(lista, key = { "${it.esPersonal}-${it.id}" }) { planta ->
                        ItemPlanta(
                            planta = planta,
                            seleccionada = seleccionada?.id == planta.id &&
                                    seleccionada.esPersonal == planta.esPersonal,
                            onClick = {
                                onSeleccionar(planta)
                                abierto = false
                            }
                        )
                    }
                }
            }
        }
    }
}