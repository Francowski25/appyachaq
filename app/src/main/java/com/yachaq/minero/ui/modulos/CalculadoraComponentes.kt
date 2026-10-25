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
import com.yachaq.minero.data.remote.model.ResponseCalculo
import com.yachaq.minero.data.remote.model.ResponsePlant
import com.yachaq.minero.ui.theme.*
import java.util.Locale

private fun dolares(v: Double) = "$ " + String.format(Locale.US, "%,.2f", v)
private fun toneladas(v: Double) = String.format(Locale.US, "%.2f TM", v)

private fun ResponsePlant.alquiler() = String.format(Locale.US, "%.2f", rentalCostPerTM ?: 0.0)

@Composable
fun SeccionPlantaCard(
    listaPlantas: List<ResponsePlant>,
    esConfirmado: Boolean,
    plantaConfirmada: ResponsePlant?,
    onConfirmar: (ResponsePlant) -> Unit,
    onCambiar: () -> Unit
) {
    var plantaSeleccionadaTemp by remember(listaPlantas) { mutableStateOf(listaPlantas.firstOrNull()) }
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
                                        text = plantaSeleccionadaTemp?.name ?: "Seleccionar...",
                                        color = TextoBlancoSuave,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    plantaSeleccionadaTemp?.let { p ->
                                        Text(
                                            text = "${p.location ?: ""} | Alquiler: $${p.alquiler()}/TM",
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
                                                Text(planta.name ?: "", color = TextoBlancoSuave, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text("📍 ${planta.location ?: ""}", color = TextoGrisSecundario, fontSize = 11.sp)
                                                    Text("💵 $${planta.alquiler()}/TM", color = AmarilloMineroBrillante, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
                        onClick = { plantaSeleccionadaTemp?.let { onConfirmar(it) } },
                        enabled = plantaSeleccionadaTemp != null,
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
                                Text(plantaConfirmada?.name ?: "", color = TextoBlancoSuave, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("📍 ${plantaConfirmada?.location ?: ""} | Alquiler: $${plantaConfirmada?.alquiler() ?: "0.00"}/TM", color = TextoGrisSecundario, fontSize = 11.sp)
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
                                Text(planta.name ?: "", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextoBlancoSuave)
                                HorizontalDivider(color = ColorGrisBorde)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Ubicación:", fontSize = 12.sp, color = TextoGrisSecundario)
                                    Text(planta.location ?: "", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Costo Alquiler:", fontSize = 12.sp, color = TextoGrisSecundario)
                                    Text("$ ${planta.alquiler()} / TM", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AmarilloMineroBrillante)
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
fun SeccionResultadosCard(resultado: ResponseCalculo, plantaConfirmada: ResponsePlant?) {
    val totalAPagar = resultado.totalAPagar ?: 0.0
    val tmns = resultado.tmns ?: 0.0
    val tmHumedas = resultado.tmHumedas ?: 0.0
    val pagoTotal = resultado.pagoTotal ?: 0.0
    val valorNeto = resultado.valorNeto ?: 0.0
    val igv = resultado.igv ?: 0.0
    val detraccion = resultado.detraccion ?: 0.0
    val alquilerTotal = resultado.alquilerTotal ?: 0.0

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
                    Text(plantaConfirmada?.name ?: "No especificada", color = TextoBlancoSuave, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Ubicación: ${plantaConfirmada?.location ?: ""} | Alquiler: $${plantaConfirmada?.alquiler() ?: "0.00"}/TM", color = TextoGrisSecundario, fontSize = 11.sp)
                }
            }

            HorizontalDivider(color = ColorGrisBorde)

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LIQUIDACIÓN TOTAL A PAGAR", color = TextoGrisSecundario, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    dolares(totalAPagar),
                    color = if (totalAPagar >= 0) AmarilloMineroBrillante else ErrorRojo,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(4.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TONELADAS DE CONCENTRADO (TMNS)", color = TextoGrisSecundario, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(toneladas(tmns), color = TextoBlancoSuave, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            }

            HorizontalDivider(color = ColorGrisBorde)

            SeccionTitulo("1. Valor del Concentrado")
            Fila("TM Húmedas de Concentrado", toneladas(tmHumedas))
            Fila("TM Secas Netas (TMNS)", String.format(Locale.US, "%.2f TMNS", tmns))
            Fila("Pago Bruto", dolares(pagoTotal))

            HorizontalDivider(color = ColorGrisBorde)

            SeccionTitulo("2. Liquidación Final")
            Fila("Valor Neto", dolares(valorNeto))
            Fila("IGV (18 %)", dolares(igv))
            Fila("Valor Total", dolares(valorNeto + igv))
            Fila("Detracción (10 %)", "- " + dolares(detraccion))
            Fila("Alquiler de Planta", "- " + dolares(alquilerTotal))
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