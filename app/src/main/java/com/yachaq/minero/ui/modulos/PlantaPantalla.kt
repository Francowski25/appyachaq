package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yachaq.minero.R
import com.yachaq.minero.data.PlantaProcesadora
import com.yachaq.minero.data.PlantasProc
import java.util.Locale

@Composable
fun PlantaPantalla() {
    // Sincronizamos con la lista global para reflejar plantas añadidas o eliminadas
    val plantasList = remember { mutableStateOf(PlantasProc.lista) }

    // Estado para saber qué planta seleccionó el minero en el mapa
    var plantaSeleccionada by remember { mutableStateOf<PlantaProcesadora?>(null) }

    // CONTENEDOR RAÍZ (Permite superponer elementos correctamente)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // ========================================================
        // 1. CONTENIDO PRINCIPAL (TÍTULO Y MAPA)
        // ========================================================
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mapa de Plantas Procesadoras",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Seleccione una ubicación para ver los detalles técnicos en Apurímac.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Contenedor del mapa más pequeño y centrado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mapa_minero),
                    contentDescription = "Mapa minero de Apurímac",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Pines de ubicación limpios sobre el mapa
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    plantasList.value.forEachIndexed { index, planta ->
                        val modifierPin = when (index % 3) {
                            0 -> Modifier.align(Alignment.Center).offset(x = (-30).dp, y = (-40).dp)
                            1 -> Modifier.align(Alignment.Center).offset(x = 50.dp, y = 30.dp)
                            else -> Modifier.align(Alignment.TopEnd).offset(x = (-20).dp, y = 40.dp)
                        }

                        PinUbicacionIcono(
                            modifier = modifierPin,
                            onClick = { plantaSeleccionada = planta }
                        )
                    }
                }
            }
        }

        // ========================================================
        // 2. PANEL INFERIOR DE DETALLES (FLOTANDO EN LA PARTE INFERIOR)
        // ========================================================
        plantaSeleccionada?.let { planta ->
            val esOperativa = planta.estado.equals("Operativa", ignoreCase = true)
            val estadoColor = if (esOperativa) Color(0xFF4CAF50) else Color(0xFFFF9800)

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = planta.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { plantaSeleccionada = null }) {
                            Text(
                                text = "✕",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ubicación: ${planta.ubicacion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoBox(titulo = "CAPACIDAD", valor = "${planta.capacidadTMAD} TPD")
                        InfoBox(titulo = "ESTADO", valor = planta.estado, colorTexto = estadoColor)
                        InfoBox(titulo = "ALQUILER", valor = String.format(Locale.US, "$%.2f/TM", planta.costoAlquilerPorTM))
                    }
                }
            }
        }
    }
}

// ========================================================
// COMPONENTE: PIN DE UBICACIÓN LIMPIO
// ========================================================
@Composable
fun PinUbicacionIcono(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Pin de ubicación",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(38.dp)
        )
    }
}

// ========================================================
// COMPONENTE: CAJA DE INFORMACIÓN
// ========================================================
@Composable
fun InfoBox(titulo: String, valor: String, colorTexto: Color = MaterialTheme.colorScheme.onSurface) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.width(105.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            )
        }
    }
}