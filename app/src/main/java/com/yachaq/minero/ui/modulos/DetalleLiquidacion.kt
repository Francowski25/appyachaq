package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yachaq.minero.ui.theme.*

/**
 * Detalle completo de una liquidación. Muestra exactamente los mismos
 * datos que se exportan al PDF (ambos salen de ConstructorLiquidacion).
 */
@Composable
fun DetalleLiquidacionDialog(
    registro: ResponseHistorial,
    onCerrar: () -> Unit,
    onExportar: () -> Unit
) {
    val vista = remember(registro) { ConstructorLiquidacion.construir(registro) }

    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = FondoNegroAbsoluto) {
            Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {

                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "DETALLE DE LIQUIDACIÓN",
                            color = AmarilloMineroBrillante,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "N° ${vista.numero}  ·  ${vista.fecha}",
                            color = TextoGrisSecundario,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onCerrar) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextoGrisSecundario)
                    }
                }

                // Contenido
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    item {
                        Surface(shape = RoundedCornerShape(16.dp), color = GrisCampos, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("TOTAL A PAGAR", fontSize = 10.sp, color = TextoGrisSecundario, fontWeight = FontWeight.Bold)
                                Text(vista.total, fontSize = 28.sp, color = AmarilloMineroBrillante, fontWeight = FontWeight.Black)
                                HorizontalDivider(color = ColorGrisBorde)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    vista.resumen.forEach { r ->
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(r.etiqueta, fontSize = 10.sp, color = TextoGrisSecundario)
                                            Text(r.valor, fontSize = 12.sp, color = TextoBlancoSuave, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    items(vista.bloques) { bloque ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ColorGrisBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    bloque.titulo.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AmarilloMineroBrillante,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = ColorGrisBorde)

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
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ColorGrisBorde),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoBlancoSuave),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cerrar", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onExportar,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmarilloMineroBrillante,
                            contentColor = FondoNegroAbsoluto
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Exportar PDF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaDetalle(f: FilaLiq) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = f.etiqueta,
            fontSize = 12.sp,
            color = if (f.resaltar) TextoBlancoSuave else TextoGrisSecundario,
            fontWeight = if (f.resaltar) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Text(
            text = f.valor,
            fontSize = 12.sp,
            color = if (f.resaltar) AmarilloMineroBrillante else TextoBlancoSuave,
            fontWeight = if (f.resaltar) FontWeight.Black else FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun TablaDetalle(b: BloqueTabla) {
    val pesos = b.columnas.indices.map { if (it == 0) 0.9f else 1.1f }

    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp)) {
        b.columnas.forEachIndexed { i, col ->
            Text(
                text = col,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextoGrisSecundario,
                textAlign = if (i == 0) TextAlign.Start else TextAlign.End,
                modifier = Modifier.weight(pesos[i])
            )
        }
    }
    HorizontalDivider(color = ColorGrisBorde)

    b.filas.forEach { fila ->
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            fila.forEachIndexed { i, celda ->
                Text(
                    text = celda,
                    fontSize = 11.sp,
                    fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Medium,
                    color = TextoBlancoSuave,
                    textAlign = if (i == 0) TextAlign.Start else TextAlign.End,
                    modifier = Modifier.weight(pesos[i])
                )
            }
        }
    }
}