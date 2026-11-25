package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yachaq.minero.data.remote.model.ResponsePlant
import com.yachaq.minero.data.remote.model.ResponseUserPlant
import com.yachaq.minero.ui.theme.AdvertenciaNaranja
import com.yachaq.minero.ui.theme.ExitoVerde
import java.util.Locale

// Modelo común para dibujar plantas del sistema y personales con la misma tarjeta
data class PlantaUi(
    val clave: String,
    val nombre: String,
    val ubicacion: String,
    val estado: String?,
    val capacidad: Double?,
    val alquiler: Double?,
    val maquila: Double?,
    val analisis: Double?,
    val esPersonal: Boolean,
    val personalizado: Boolean,
    val sistema: ResponsePlant? = null,
    val personal: ResponseUserPlant? = null
)

private fun dolar(v: Double?) = String.format(Locale.US, "$ %.2f", v ?: 0.0)

@Composable
private fun Etiqueta(texto: String, color: Color) {
    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.18f)) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun EtiquetaEstado(status: String?) {
    val estado = status ?: "Sin estado"
    val color = if (estado.equals("Operativa", ignoreCase = true)) ExitoVerde else AdvertenciaNaranja
    Etiqueta(estado, color)
}

@Composable
private fun IconoFabrica(resaltado: Boolean) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                if (resaltado) cs.primary else cs.surfaceContainerHigh,
                MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Factory,
            contentDescription = null,
            tint = if (resaltado) cs.onPrimary else cs.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun DatoPlanta(etiqueta: String, valor: String, destacado: Boolean = false, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    Surface(shape = MaterialTheme.shapes.medium, color = cs.surfaceContainerHigh, modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(etiqueta, style = tp.labelSmall, color = cs.onSurfaceVariant)
            Text(
                valor,
                style = tp.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (destacado) cs.primary else cs.onSurface
            )
        }
    }
}

@Composable
fun TarjetaSinFavoritas(onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    Card(
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, cs.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconoFabrica(resaltado = true)
            Text(
                "Aún no tienes plantas",
                style = tp.titleMedium,
                color = cs.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                "Añade una planta personal o elige de la lista las plantas del sistema con las que trabajas.",
                style = tp.bodySmall,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onClick,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ver lista de plantas", style = tp.labelLarge)
            }
        }
    }
}

@Composable
fun TarjetaPlantaMinero(
    planta: PlantaUi,
    onAjustar: () -> Unit,
    onQuitarFavorita: () -> Unit,
    onRestablecer: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    var menu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            1.dp,
            if (planta.esPersonal) cs.outlineVariant else cs.primary.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconoFabrica(resaltado = !planta.esPersonal)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        planta.nombre,
                        style = tp.titleMedium,
                        color = cs.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            planta.ubicacion,
                            style = tp.bodySmall,
                            color = cs.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = cs.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = menu,
                        onDismissRequest = { menu = false },
                        containerColor = cs.surfaceContainerHigh
                    ) {
                        if (planta.esPersonal) {
                            DropdownMenuItem(
                                text = { Text("Editar datos", style = tp.bodyMedium, color = cs.onSurface) },
                                leadingIcon = { Icon(Icons.Default.Edit, null, tint = cs.primary) },
                                onClick = { menu = false; onEditar() }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar", style = tp.bodyMedium, color = cs.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = cs.error) },
                                onClick = { menu = false; onEliminar() }
                            )
                        } else {
                            if (planta.personalizado) {
                                DropdownMenuItem(
                                    text = { Text("Restablecer términos", style = tp.bodyMedium, color = cs.onSurface) },
                                    leadingIcon = { Icon(Icons.Default.Restore, null, tint = cs.primary) },
                                    onClick = { menu = false; onRestablecer() }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Quitar de favoritas", style = tp.bodyMedium, color = cs.error) },
                                leadingIcon = { Icon(Icons.Default.Star, null, tint = cs.error) },
                                onClick = { menu = false; onQuitarFavorita() }
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                EtiquetaEstado(planta.estado)
                Etiqueta(
                    if (planta.esPersonal) "PERSONAL" else "FAVORITA",
                    if (planta.esPersonal) cs.onSurfaceVariant else cs.primary
                )
                if (planta.personalizado) Etiqueta("PERSONALIZADO", cs.primary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatoPlanta("CAPACIDAD", "${planta.capacidad ?: 0.0} TMAD", modifier = Modifier.weight(1f))
                DatoPlanta("ALQUILER / TM", dolar(planta.alquiler), destacado = true, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatoPlanta("MAQUILA", dolar(planta.maquila), modifier = Modifier.weight(1f))
                DatoPlanta("ANÁLISIS", dolar(planta.analisis), modifier = Modifier.weight(1f))
            }

            Button(
                onClick = onAjustar,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Icon(Icons.Default.Tune, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ajustar términos", style = tp.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoListaPlantas(
    plantas: List<ResponsePlant>,
    idsFavoritas: Set<String>,
    error: String?,
    onAgregar: (ResponsePlant) -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = cs.surfaceContainer) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text(
                "Plantas del sistema",
                style = tp.titleLarge,
                color = cs.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Agrega a favoritas las plantas con las que trabajas.",
                style = tp.bodySmall,
                color = cs.onSurfaceVariant
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                MensajeError(it)
            }
            Spacer(Modifier.height(12.dp))

            if (plantas.isEmpty()) {
                Text("No hay plantas registradas.", style = tp.bodyMedium, color = cs.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = plantas, key = { it.idPlant ?: it.hashCode() }) { planta ->
                        TarjetaPlantaLista(
                            planta = planta,
                            yaFavorita = planta.idPlant in idsFavoritas,
                            onAgregar = { onAgregar(planta) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaPlantaLista(
    planta: ResponsePlant,
    yaFavorita: Boolean,
    onAgregar: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    Surface(
        shape = MaterialTheme.shapes.large,
        color = cs.surfaceContainerHigh,
        border = BorderStroke(1.dp, if (yaFavorita) cs.primary.copy(alpha = 0.5f) else cs.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconoFabrica(resaltado = yaFavorita)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    planta.name ?: "Sin nombre",
                    style = tp.titleSmall,
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    planta.location ?: "Ubicación no especificada",
                    style = tp.bodySmall,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    EtiquetaEstado(planta.status)
                    Text(
                        "${dolar(planta.rentalCostPerTM)}/TM",
                        style = tp.labelSmall,
                        color = cs.primary
                    )
                }
            }
            if (yaFavorita) {
                Icon(Icons.Default.Check, contentDescription = "Agregada", tint = cs.primary)
            } else {
                FilledIconButton(onClick = onAgregar) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar a favoritas")
                }
            }
        }
    }
}