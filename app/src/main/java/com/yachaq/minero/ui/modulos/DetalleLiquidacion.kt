package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yachaq.minero.data.remote.model.ResponseHistorial
import com.yachaq.minero.logica.BloqueFilas
import com.yachaq.minero.logica.BloqueTabla
import com.yachaq.minero.logica.ConstructorLiquidacion
import com.yachaq.minero.logica.FilaLiq


@Composable
fun DetalleLiquidacionDialog(
    registro: ResponseHistorial,
    onCerrar: () -> Unit,
    onExportar: () -> Unit
) {
    val vista = remember(registro) { ConstructorLiquidacion.construir(registro) }
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes

    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = cs.background) {
            Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {

                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "DETALLE DE LIQUIDACIÓN",
                            style = tp.titleMedium,
                            color = cs.primary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "N° ${vista.numero}  ·  ${vista.fecha}",
                            style = tp.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onCerrar) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = cs.onSurfaceVariant)
                    }
                }

                // Contenido
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    item {
                        Surface(
                            shape = formas.large,
                            color = cs.surfaceContainerHigh,
                            border = BorderStroke(1.dp, cs.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "TOTAL A PAGAR",
                                    style = tp.labelSmall,
                                    color = cs.onSurfaceVariant
                                )
                                Text(
                                    vista.total,
                                    style = tp.headlineLarge,
                                    color = cs.primary,
                                    fontWeight = FontWeight.Black
                                )
                                HorizontalDivider(color = cs.outlineVariant)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    vista.resumen.forEach { r ->
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(r.etiqueta, style = tp.labelSmall, color = cs.onSurfaceVariant)
                                            Text(
                                                r.valor,
                                                style = tp.bodySmall,
                                                color = cs.onSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    items(vista.bloques) { bloque ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
                            shape = formas.large,
                            border = BorderStroke(1.dp, cs.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    bloque.titulo.uppercase(),
                                    style = tp.labelMedium,
                                    color = cs.primary,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = cs.outlineVariant)

                                when (bloque) {
                                    is BloqueFilas -> bloque.filas.forEach { FilaDetalle(it) }
                                    is BloqueTabla -> TablaDetalle(bloque)
                                }
                            }
                        }
                    }
                }

                // Acciones
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCerrar,
                        shape = formas.medium,
                        border = BorderStroke(1.dp, cs.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.onSurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cerrar", style = tp.labelLarge)
                    }
                    Button(
                        onClick = onExportar,
                        shape = formas.medium,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Exportar PDF", style = tp.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaDetalle(f: FilaLiq) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = f.etiqueta,
            style = tp.bodySmall,
            color = if (f.resaltar) cs.onSurface else cs.onSurfaceVariant,
            fontWeight = if (f.resaltar) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Text(
            text = f.valor,
            style = tp.bodySmall,
            color = if (f.resaltar) cs.primary else cs.onSurface,
            fontWeight = if (f.resaltar) FontWeight.Black else FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun TablaDetalle(b: BloqueTabla) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val pesos = b.columnas.indices.map { if (it == 0) 0.9f else 1.1f }

    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp)) {
        b.columnas.forEachIndexed { i, col ->
            Text(
                text = col,
                style = tp.labelSmall,
                color = cs.onSurfaceVariant,
                textAlign = if (i == 0) TextAlign.Start else TextAlign.End,
                modifier = Modifier.weight(pesos[i])
            )
        }
    }
    HorizontalDivider(color = cs.outlineVariant)

    b.filas.forEach { fila ->
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            fila.forEachIndexed { i, celda ->
                Text(
                    text = celda,
                    style = tp.bodySmall,
                    fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Medium,
                    color = cs.onSurface,
                    textAlign = if (i == 0) TextAlign.Start else TextAlign.End,
                    modifier = Modifier.weight(pesos[i])
                )
            }
        }
    }
}