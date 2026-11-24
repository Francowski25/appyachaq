package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.yachaq.minero.ui.theme.*
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
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = TextoGrisSecundario,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = planta.ubicacion.ifBlank { "Sin ubicación" },
            color = TextoGrisSecundario,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Text(
            text = "  •  $ ${planta.alquilerTexto()}/TM",
            color = AmarilloMineroBrillante,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun IconoPlanta(activo: Boolean) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (activo) AmarilloMineroBrillante else FondoNegroAbsoluto,
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Factory,
            contentDescription = null,
            tint = if (activo) FondoNegroAbsoluto else AmarilloMineroBrillante,
            modifier = Modifier.size(22.dp)
        )
    }
}

// Etiqueta pequeña junto al nombre: estrella (favorita) o PERSONAL
@Composable
private fun EtiquetaPlanta(planta: PlantaCalculo) {
    when {
        planta.esPersonal -> Text(
            "PERSONAL",
            color = TextoGrisSecundario,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        planta.favorita -> Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Favorita",
            tint = AmarilloMineroBrillante,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ItemPlanta(planta: PlantaCalculo, seleccionada: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = GrisCampos,
        border = BorderStroke(
            width = if (seleccionada) 1.5.dp else 1.dp,
            color = if (seleccionada) AmarilloMineroBrillante else ColorGrisBorde
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
                        color = TextoBlancoSuave,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
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
                    tint = AmarilloMineroBrillante,
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
        color = AmarilloMineroBrillante,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

// Campo tocable que muestra la planta elegida; al tocarlo abre una hoja con las opciones.
// Elegir una planta la confirma al instante (sin botón extra).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorPlanta(
    plantas: List<PlantaCalculo>,
    seleccionada: PlantaCalculo?,
    onSeleccionar: (PlantaCalculo) -> Unit,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }

    Surface(
        onClick = { abierto = true },
        shape = RoundedCornerShape(14.dp),
        color = GrisCampos,
        border = BorderStroke(
            width = 1.dp,
            color = if (seleccionada != null) AmarilloMineroBrillante.copy(alpha = 0.7f) else ColorGrisBorde
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
                    Text("Elige una planta procesadora", color = TextoBlancoSuave, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Define el alquiler por TM", color = TextoGrisSecundario, fontSize = 11.sp)
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = seleccionada.nombre,
                            color = TextoBlancoSuave,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
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
                color = AmarilloMineroBrillante,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
    }

    if (abierto) {
        // Agrupa en favoritas, personales y del sistema; omite los grupos vacíos
        val grupos = listOf(
            "FAVORITAS" to plantas.filter { !it.esPersonal && it.favorita },
            "MIS PLANTAS" to plantas.filter { it.esPersonal },
            "PLANTAS DEL SISTEMA" to plantas.filter { !it.esPersonal && !it.favorita }
        ).filter { it.second.isNotEmpty() }

        ModalBottomSheet(
            onDismissRequest = { abierto = false },
            containerColor = FondoNegroSecundario
        ) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Selecciona la planta",
                        color = TextoBlancoSuave,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Al cambiar de planta se reinicia el resultado.",
                        color = TextoGrisSecundario,
                        fontSize = 12.sp
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