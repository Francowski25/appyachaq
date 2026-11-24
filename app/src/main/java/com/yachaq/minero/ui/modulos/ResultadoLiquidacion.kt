package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.model.ResponseCalculo
import com.yachaq.minero.ui.theme.*
import java.util.Locale

private fun dolares(v: Double) = "$ " + String.format(Locale.US, "%,.2f", v)
private fun toneladas(v: Double) = String.format(Locale.US, "%,.2f", v)

private data class DetalleCalculo(
    val label: String,
    val valor: String,
    val esDescuento: Boolean = false
)

private data class BloqueDetalle(val titulo: String, val filas: List<DetalleCalculo>)

// Resultado: total destacado, datos clave y desglose en 3 bloques
@Composable
fun ResultadoLiquidacion(
    resultado: ResponseCalculo,
    planta: PlantaCalculo?,
    modifier: Modifier = Modifier
) {
    val totalAPagar = resultado.totalAPagar ?: 0.0
    val tmns = resultado.tmns ?: 0.0
    val tmHumedas = resultado.tmHumedas ?: 0.0
    val pagoCu = resultado.pagoCu ?: 0.0
    val pagoAg = resultado.pagoAg ?: 0.0
    val pagoAu = resultado.pagoAu ?: 0.0
    val reduccionRefinacion = resultado.reduccionRefinacion ?: 0.0
    val analisisPorTmns = resultado.analisisPorTmns ?: 0.0
    val trasladoPorTmns = resultado.trasladoPorTmns ?: 0.0
    val pagoPorTm = resultado.pagoPorTm ?: 0.0

    // La maquila no viene en la respuesta: se obtiene por diferencia
    val maquila = pagoCu + pagoAg + pagoAu - reduccionRefinacion - analisisPorTmns - trasladoPorTmns - pagoPorTm

    val bloques = listOf(
        BloqueDetalle(
            "VALOR DEL CONCENTRADO",
            buildList {
                if ((resultado.contenidoPagableCu ?: 0.0) > 0) add(DetalleCalculo("Pago por Cobre (Cu)", dolares(pagoCu)))
                if ((resultado.contenidoPagableAg ?: 0.0) > 0) add(DetalleCalculo("Pago por Plata (Ag)", dolares(pagoAg)))
                if ((resultado.contenidoPagableAu ?: 0.0) > 0) add(DetalleCalculo("Pago por Oro (Au)", dolares(pagoAu)))
            }
        ),
        BloqueDetalle(
            "REDUCCIONES",
            listOf(
                DetalleCalculo("Maquila", dolares(maquila), true),
                DetalleCalculo("Gastos de refinación", dolares(reduccionRefinacion), true),
                DetalleCalculo("Análisis", dolares(analisisPorTmns), true),
                DetalleCalculo("Traslado interno", dolares(trasladoPorTmns), true),
                DetalleCalculo("Pago por TM de concentrado", dolares(pagoPorTm))
            )
        ),
        BloqueDetalle(
            "LIQUIDACIÓN FINAL",
            listOf(
                DetalleCalculo("Valor neto", dolares(resultado.valorNeto ?: 0.0)),
                DetalleCalculo("IGV (18 %)", dolares(resultado.igv ?: 0.0)),
                DetalleCalculo("Valor total", dolares(resultado.valorTotal ?: 0.0)),
                DetalleCalculo("Detracción (10 %)", dolares(resultado.detraccion ?: 0.0), true),
                DetalleCalculo("Alquiler de planta", dolares(resultado.alquilerTotal ?: 0.0), true)
            )
        )
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TotalDestacado(totalAPagar, planta)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DatoClave("TM HÚMEDAS", toneladas(tmHumedas), "TM", Modifier.weight(1f))
            DatoClave("TM SECAS NETAS", toneladas(tmns), "TMNS", Modifier.weight(1f))
        }

        bloques.forEach { BloqueDesglose(it) }
    }
}

@Composable
private fun TotalDestacado(total: Double, planta: PlantaCalculo?) {
    val positivo = total >= 0
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = FondoNegroSecundario,
        border = BorderStroke(1.5.dp, (if (positivo) AmarilloMineroBrillante else ErrorRojo).copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "LIQUIDACIÓN TOTAL A PAGAR",
                color = TextoGrisSecundario,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Text(
                dolares(total),
                color = if (positivo) AmarilloMineroBrillante else ErrorRojo,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black
            )
            if (planta != null) {
                Spacer(Modifier.height(6.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = GrisCampos) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Factory,
                            contentDescription = null,
                            tint = AmarilloMineroBrillante,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(planta.nombre, color = TextoBlancoSuave, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DatoClave(etiqueta: String, valor: String, unidad: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = FondoNegroSecundario,
        border = BorderStroke(1.dp, ColorGrisBorde),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(etiqueta, color = TextoGrisSecundario, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(valor, color = TextoBlancoSuave, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text(unidad, color = TextoGrisSecundario, fontSize = 11.sp, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
    }
}

@Composable
private fun BloqueDesglose(bloque: BloqueDetalle) {
    if (bloque.filas.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(14.dp)
                    .background(AmarilloMineroBrillante, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                bloque.titulo,
                color = AmarilloMineroBrillante,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = FondoNegroSecundario,
            border = BorderStroke(1.dp, ColorGrisBorde),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                bloque.filas.forEachIndexed { i, fila ->
                    FilaDetalle(fila)
                    if (i < bloque.filas.lastIndex) {
                        HorizontalDivider(color = ColorGrisBorde.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

// Los descuentos se muestran en rojo con signo negativo
@Composable
private fun FilaDetalle(fila: DetalleCalculo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(fila.label, color = TextoGrisSecundario, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(
            text = if (fila.esDescuento) "- ${fila.valor}" else fila.valor,
            color = if (fila.esDescuento) ErrorRojo else TextoBlancoSuave,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}