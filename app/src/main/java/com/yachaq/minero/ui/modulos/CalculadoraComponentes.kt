package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Warning
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
import com.yachaq.minero.data.Resultado
import com.yachaq.minero.ui.theme.*
import java.util.Locale

private fun dolares(v: Double) = "$ " + String.format(Locale.US, "%,.2f", v)
private fun toneladas(v: Double) = String.format(Locale.US, "%.2f TM", v)
private fun porcentaje(v: Double) = String.format(Locale.US, "%.2f %%", v)

@Composable
fun SeccionPlantaCard(
    listaPlantas: List<PlantaProcesadora>,
    esConfirmado: Boolean,
    plantaConfirmada: PlantaProcesadora?,
    onConfirmar: (PlantaProcesadora) -> Unit,
    onCambiar: () -> Unit
) {
    var plantaSeleccionadaTemp by remember { mutableStateOf(listaPlantas.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }
    var mostrarDialogoCambiar by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = AmarilloMineroBrillante),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Factory,
                    contentDescription = null,
                    tint = FondoNegroAbsoluto,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (esConfirmado) "PLANTA SELECCIONADA" else "SELECCIONAR PLANTA PROCESADORA",
                    color = FondoNegroAbsoluto,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            }

            if (!esConfirmado) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            onClick = { expanded = true },
                            shape = RoundedCornerShape(12.dp),
                            color = FondoNegroAbsoluto,
                            border = BorderStroke(1.dp, ColorGrisBorde),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = plantaSeleccionadaTemp?.nombre ?: "Seleccionar...",
                                        color = TextoBlancoSuave,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    plantaSeleccionadaTemp?.let { p ->
                                        Text(
                                            text = "${p.ubicacion} | Alquiler: $${p.costoAlquilerPorTM}/TM",
                                            color = TextoGrisSecundario,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = AmarilloMineroBrillante
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(FondoNegroSecundario).fillMaxWidth(0.85f)
                        ) {
                            listaPlantas.forEach { planta ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(Icons.Default.Factory, contentDescription = null, tint = AmarilloMineroBrillante, modifier = Modifier.size(20.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(planta.nombre, color = TextoBlancoSuave, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text("📍 ${planta.ubicacion}", color = TextoGrisSecundario, fontSize = 11.sp)
                                                    Text("💵 $${planta.costoAlquilerPorTM}/TM", color = AmarilloMineroBrillante, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    },
                                    onClick = {
                                        plantaSeleccionadaTemp = planta
                                        expanded = false
                                    }
                                )
                                HorizontalDivider(color = ColorGrisBorde.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (plantaSeleccionadaTemp != null) {
                                onConfirmar(plantaSeleccionadaTemp!!)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FondoNegroAbsoluto, contentColor = AmarilloMineroBrillante),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("Confirmar", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FondoNegroAbsoluto,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AmarilloMineroBrillante, modifier = Modifier.size(22.dp))
                            Column {
                                Text(plantaConfirmada?.nombre ?: "", color = TextoBlancoSuave, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("📍 ${plantaConfirmada?.ubicacion} | Alquiler: $${plantaConfirmada?.costoAlquilerPorTM}/TM", color = TextoGrisSecundario, fontSize = 11.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = { mostrarDialogoCambiar = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmarilloMineroBrillante),
                            border = BorderStroke(1.dp, AmarilloMineroBrillante),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Cambiar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoCambiar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCambiar = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = AmarilloMineroBrillante, modifier = Modifier.size(32.dp)) },
            title = { Text("¿Desea cambiar de Planta?", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AmarilloMineroBrillante) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Al cambiar de planta, la selección actual quedará sin efecto y se reiniciará el cálculo.", fontSize = 13.sp, color = TextoGrisSecundario)
                    plantaConfirmada?.let { planta ->
                        Surface(shape = RoundedCornerShape(14.dp), color = GrisCampos, border = BorderStroke(1.dp, ColorGrisBorde), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("PLANTA ACTUALMENTE ACTIVA", fontSize = 10.sp, fontWeight = FontWeight.Black, color = AmarilloMineroBrillante)
                                Text(planta.nombre, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextoBlancoSuave)
                                HorizontalDivider(color = ColorGrisBorde)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Ubicación:", fontSize = 12.sp, color = TextoGrisSecundario)
                                    Text(planta.ubicacion, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Costo Alquiler:", fontSize = 12.sp, color = TextoGrisSecundario)
                                    Text("$ ${String.format(Locale.US, "%.2f", planta.costoAlquilerPorTM)} / TM", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AmarilloMineroBrillante)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCambiar()
                        mostrarDialogoCambiar = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmarilloMineroBrillante, contentColor = FondoNegroAbsoluto),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Sí, Cambiar Planta", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCambiar = false }) { Text("Cancelar", color = TextoGrisSecundario, fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
fun SeccionMaterialSelector(
    materiales: List<String>,
    materialSeleccionado: String,
    onMaterialSeleccionado: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("MATERIAL O MINERAL", color = TextoGrisSecundario, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            materiales.forEach { material ->
                val seleccionado = materialSeleccionado == material
                Surface(
                    onClick = { onMaterialSeleccionado(material) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (seleccionado) AmarilloMineroBrillante else GrisCampos,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(material, color = if (seleccionado) FondoNegroAbsoluto else TextoBlancoSuave, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SeccionCamposEntrada(
    materialSeleccionado: String,
    tonelaje: String,
    onTonelajeChange: (String) -> Unit,
    ley: String,
    onLeyChange: (String) -> Unit,
    precio: String,
    onPrecioChange: (String) -> Unit,
    error: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Campo(tonelaje, "Toneladas métricas brutas (TM)", onTonelajeChange)
        Spacer(Modifier.height(12.dp))

        val etiquetaLey = when {
            materialSeleccionado.contains("Cobre") -> "Ley de Cobre (Cu %)"
            materialSeleccionado.contains("Oro") -> "Ley de Oro (Au oz/tm)"
            else -> "Ley de Plata (Ag oz/tm)"
        }
        Campo(ley, etiquetaLey, onLeyChange)
        Spacer(Modifier.height(12.dp))

        val etiquetaPrecio = when {
            materialSeleccionado.contains("Cobre") -> "Precio Cobre ($ / lb)"
            materialSeleccionado.contains("Oro") -> "Precio Oro ($ / oz)"
            else -> "Precio Plata ($ / oz)"
        }
        Campo(precio, etiquetaPrecio, onPrecioChange)

        error?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = ErrorRojo, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SeccionResultadosCard(resultado: Resultado, plantaConfirmada: PlantaProcesadora?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, ColorGrisBorde)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = GrisCampos, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("PLANTA DESTINO DE PROCESAMIENTO", color = AmarilloMineroBrillante, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(2.dp))
                    Text(plantaConfirmada?.nombre ?: "No especificada", color = TextoBlancoSuave, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Ubicación: ${plantaConfirmada?.ubicacion ?: ""} | Alquiler: $${plantaConfirmada?.costoAlquilerPorTM ?: 0.0}/TM", color = TextoGrisSecundario, fontSize = 11.sp)
                }
            }

            HorizontalDivider(color = ColorGrisBorde)

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LIQUIDACIÓN TOTAL A PAGAR", color = TextoGrisSecundario, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(dolares(resultado.totalAPagar), color = if (resultado.totalAPagar >= 0) AmarilloMineroBrillante else ErrorRojo, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(4.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TONELADAS DE CONCENTRADO (TMNS)", color = TextoGrisSecundario, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(toneladas(resultado.tmns), color = TextoBlancoSuave, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            }

            HorizontalDivider(color = ColorGrisBorde)

            SeccionTitulo("1. Valor del Concentrado")
            Fila("TM Húmedas de Concentrado", toneladas(resultado.tmHumedas))
            val porcentajeHumedad = if (resultado.tmHumedas > 0) ((resultado.tmHumedas - resultado.tmns) / resultado.tmHumedas) * 100 else 8.00
            Fila("% de Agua (Humedad)", porcentaje(porcentajeHumedad))
            Fila("TM Secas Netas (TMNS)", String.format(Locale.US, "%.2f TMNS", resultado.tmns))
            Fila("Pago por Cobre (Cu)", dolares(resultado.pagoCu))
            Fila("Pago por Plata (Ag)", dolares(resultado.pagoAg))
            Fila("Pago por Oro (Au)", dolares(resultado.pagoAu))

            HorizontalDivider(color = ColorGrisBorde)

            SeccionTitulo("2. Reducciones y Gastos")
            Fila("Maquila", "- " + dolares(resultado.maquila))
            Fila("Gastos de Refinación", "- " + dolares(resultado.reduccionRefinacion))
            Fila("Análisis", "- " + dolares(resultado.analisis))
            Fila("Traslado Interno", "- " + dolares(resultado.trasladoInterno))
            Fila("Pago por TM de Concentrado", dolares(resultado.pagoPorTm))

            HorizontalDivider(color = ColorGrisBorde)

            SeccionTitulo("3. Liquidación Final")
            Fila("Valor Neto", dolares(resultado.valorNeto))
            Fila("IGV (18 %)", dolares(resultado.igv))
            Fila("Valor Total", dolares(resultado.valorTotal))
            Fila("Detracción (10 %)", "- " + dolares(resultado.detraccion))
            Fila("Alquiler de Planta", "- " + dolares(resultado.alquilerTotal))
        }
    }
}

@Composable
private fun SeccionTitulo(titulo: String) {
    Text(titulo, color = AmarilloMineroBrillante, fontSize = 12.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun Campo(valor: String, etiqueta: String, onCambio: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = { onCambio(it.replace(',', '.')) },
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = GrisCampos,
            unfocusedContainerColor = GrisCampos,
            focusedBorderColor = AmarilloMineroBrillante,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = AmarilloMineroBrillante,
            unfocusedLabelColor = TextoGrisSecundario,
            focusedTextColor = TextoBlancoSuave,
            unfocusedTextColor = TextoBlancoSuave
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, color = TextoGrisSecundario, fontSize = 12.sp)
        Text(valor, color = TextoBlancoSuave, fontWeight = FontWeight.Medium, fontSize = 12.sp)
    }
}