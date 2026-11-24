package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.model.ResponsePlant
import com.yachaq.minero.data.remote.model.ResponseUserPlant
import com.yachaq.minero.ui.theme.*
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
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.18f)) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun EtiquetaEstado(status: String?) {
    val estado = status ?: "Sin estado"
    val color = if (estado.equals("Operativa", ignoreCase = true)) Color(0xFF4CAF50) else Color(0xFFFF9800)
    Etiqueta(estado, color)
}

@Composable
private fun IconoFabrica(resaltado: Boolean) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                if (resaltado) AmarilloMineroBrillante else GrisCampos,
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Factory,
            contentDescription = null,
            tint = if (resaltado) FondoNegroAbsoluto else AmarilloMineroBrillante,
            modifier = Modifier.size(22.dp)
        )
    }
}

// Casilla pequeña con etiqueta y valor
@Composable
private fun DatoPlanta(etiqueta: String, valor: String, destacado: Boolean = false, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(12.dp), color = GrisCampos, modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(etiqueta, fontSize = 10.sp, color = TextoGrisSecundario, fontWeight = FontWeight.Bold)
            Text(
                valor,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (destacado) AmarilloMineroBrillante else TextoBlancoSuave
            )
        }
    }
}

@Composable
fun TarjetaSinFavoritas(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ColorGrisBorde),
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextoBlancoSuave,
                textAlign = TextAlign.Center
            )
            Text(
                "Añade una planta personal o elige de la lista las plantas del sistema con las que trabajas.",
                fontSize = 12.sp,
                color = TextoGrisSecundario,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmarilloMineroBrillante,
                    contentColor = FondoNegroAbsoluto
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ver lista de plantas", fontWeight = FontWeight.Bold)
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
    var menu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (planta.esPersonal) ColorGrisBorde else AmarilloMineroBrillante.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconoFabrica(resaltado = !planta.esPersonal)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        planta.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoSuave,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = TextoGrisSecundario, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            planta.ubicacion,
                            fontSize = 12.sp,
                            color = TextoGrisSecundario,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = TextoGrisSecundario)
                    }
                    DropdownMenu(
                        expanded = menu,
                        onDismissRequest = { menu = false },
                        containerColor = FondoNegroSecundario
                    ) {
                        if (planta.esPersonal) {
                            DropdownMenuItem(
                                text = { Text("Editar datos", color = TextoBlancoSuave) },
                                leadingIcon = { Icon(Icons.Default.Edit, null, tint = AmarilloMineroBrillante) },
                                onClick = { menu = false; onEditar() }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar", color = ErrorRojo) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = ErrorRojo) },
                                onClick = { menu = false; onEliminar() }
                            )
                        } else {
                            if (planta.personalizado) {
                                DropdownMenuItem(
                                    text = { Text("Restablecer términos", color = TextoBlancoSuave) },
                                    leadingIcon = { Icon(Icons.Default.Restore, null, tint = AmarilloMineroBrillante) },
                                    onClick = { menu = false; onRestablecer() }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Quitar de favoritas", color = ErrorRojo) },
                                leadingIcon = { Icon(Icons.Default.Star, null, tint = ErrorRojo) },
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
                    if (planta.esPersonal) TextoGrisSecundario else AmarilloMineroBrillante
                )
                if (planta.personalizado) Etiqueta("PERSONALIZADO", AmarilloMineroBrillante)
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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmarilloMineroBrillante,
                    contentColor = FondoNegroAbsoluto
                ),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Icon(Icons.Default.Tune, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ajustar términos", fontWeight = FontWeight.Bold)
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
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = FondoNegroSecundario) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text("Plantas del sistema", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextoBlancoSuave)
            Text(
                "Agrega a favoritas las plantas con las que trabajas.",
                fontSize = 12.sp,
                color = TextoGrisSecundario
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                MensajeError(it)
            }
            Spacer(Modifier.height(12.dp))

            if (plantas.isEmpty()) {
                Text("No hay plantas registradas.", color = TextoGrisSecundario, fontSize = 13.sp)
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
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GrisCampos,
        border = BorderStroke(1.dp, if (yaFavorita) AmarilloMineroBrillante.copy(alpha = 0.5f) else ColorGrisBorde),
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoBlancoSuave,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    planta.location ?: "Ubicación no especificada",
                    fontSize = 11.sp,
                    color = TextoGrisSecundario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    EtiquetaEstado(planta.status)
                    Text(
                        "${dolar(planta.rentalCostPerTM)}/TM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmarilloMineroBrillante
                    )
                }
            }
            if (yaFavorita) {
                Icon(Icons.Default.Check, contentDescription = "Agregada", tint = AmarilloMineroBrillante)
            } else {
                FilledIconButton(
                    onClick = onAgregar,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = AmarilloMineroBrillante,
                        contentColor = FondoNegroAbsoluto
                    )
                ) { Icon(Icons.Default.Add, contentDescription = "Agregar a favoritas") }
            }
        }
    }
}