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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
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

private fun dolares(v: Double) = "$ " + String.format(Locale.US, "%,.2f", v)
private fun toneladas(v: Double) = String.format(Locale.US, "%.2f TM", v)

private fun PlantaCalculo.alquilerTexto() = String.format(Locale.US, "%.2f", alquiler)

// Ubicación y costo de alquiler por TM de una planta
@Composable
private fun PlantaMeta(planta: PlantaCalculo?, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = TextoGrisSecundario,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = planta?.ubicacion ?: "",
            color = TextoGrisSecundario,
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier.weight(1f, fill = false)
        )
        Text(
            text = "  •  $ ${planta?.alquilerTexto() ?: "0.00"}/TM",
            color = AmarilloMineroBrillante,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

// Fila de una planta en la lista: ícono, nombre, ubicación y etiqueta (estrella o PERSONAL)
@Composable
private fun ItemPlantaContenido(planta: PlantaCalculo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(GrisCampos, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Factory,
                contentDescription = null,
                tint = AmarilloMineroBrillante,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = planta.nombre,
                color = TextoBlancoSuave,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
            PlantaMeta(planta)
        }
        if (planta.esPersonal) {
            Text(
                "PERSONAL",
                color = TextoGrisSecundario,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        } else if (planta.favorita) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorita",
                tint = AmarilloMineroBrillante,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ItemPlantaMenu(planta: PlantaCalculo, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { ItemPlantaContenido(planta) },
        onClick = onClick
    )
    HorizontalDivider(color = ColorGrisBorde.copy(alpha = 0.5f))
}

// Título de sección dentro de la lista desplegable
@Composable
private fun EncabezadoLista(texto: String, conEstrella: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (conEstrella) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AmarilloMineroBrillante,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = texto,
            color = AmarilloMineroBrillante,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

// Selector de planta: la lista ya viene filtrada según el rol
@Composable
fun SeccionPlantaCard(
    listaPlantas: List<PlantaCalculo>,
    esConfirmado: Boolean,
    plantaConfirmada: PlantaCalculo?,
    onConfirmar: (PlantaCalculo) -> Unit,
    onCambiar: () -> Unit,
    esMinero: Boolean = false
) {
    var plantaSeleccionadaTemp by remember(listaPlantas) {
        mutableStateOf(listaPlantas.firstOrNull())
    }
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
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = plantaSeleccionadaTemp?.nombre ?: "Seleccionar...",
                                            color = TextoBlancoSuave,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (plantaSeleccionadaTemp?.favorita == true) {
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = AmarilloMineroBrillante,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                    plantaSeleccionadaTemp?.let { p -> PlantaMeta(p) }
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
                            if (esMinero) EncabezadoLista("MIS PLANTAS")
                            listaPlantas.forEach { planta ->
                                ItemPlantaMenu(planta) {
                                    plantaSeleccionadaTemp = planta
                                    expanded = false
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { plantaSeleccionadaTemp?.let { onConfirmar(it) } },
                        enabled = plantaSeleccionadaTemp != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FondoNegroAbsoluto,
                            contentColor = AmarilloMineroBrillante
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
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
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AmarilloMineroBrillante,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = plantaConfirmada?.nombre ?: "",
                                        color = TextoBlancoSuave,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (plantaConfirmada?.favorita == true) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = AmarilloMineroBrillante,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                PlantaMeta(plantaConfirmada)
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
                                    Text("$ ${planta.alquilerTexto()} / TM", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AmarilloMineroBrillante)
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

// Resultado del cálculo: planta destino, total a pagar y detalle por bloques
@Composable
fun SeccionResultadosCard(resultado: ResponseCalculo, plantaConfirmada: PlantaCalculo?) {
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
    val valorNeto = resultado.valorNeto ?: 0.0
    val igv = resultado.igv ?: 0.0
    val valorTotal = resultado.valorTotal ?: 0.0
    val detraccion = resultado.detraccion ?: 0.0
    val alquilerTotal = resultado.alquilerTotal ?: 0.0

    val hayCu = (resultado.contenidoPagableCu ?: 0.0) > 0
    val hayAg = (resultado.contenidoPagableAg ?: 0.0) > 0
    val hayAu = (resultado.contenidoPagableAu ?: 0.0) > 0

    // La maquila no viene en la respuesta: se obtiene por diferencia
    val maquila = pagoCu + pagoAg + pagoAu - reduccionRefinacion - analisisPorTmns - trasladoPorTmns - pagoPorTm

    data class DetalleCalculo(val label: String, val valor: String, val esNegativo: Boolean = false)

    val concentrado = buildList<DetalleCalculo> {
        add(DetalleCalculo("TM Húmedas", toneladas(tmHumedas)))
        add(DetalleCalculo("TM Secas Netas (TMNS)", String.format(Locale.US, "%.2f TMNS", tmns)))
        if (hayCu) add(DetalleCalculo("Pago por Cobre (Cu)", dolares(pagoCu)))
        if (hayAg) add(DetalleCalculo("Pago por Plata (Ag)", dolares(pagoAg)))
        if (hayAu) add(DetalleCalculo("Pago por Oro (Au)", dolares(pagoAu)))
    }

    val listaDetalles = listOf(
        "1. Valor del Concentrado" to concentrado,
        "2. Reducciones" to listOf(
            DetalleCalculo("Maquila", dolares(maquila), true),
            DetalleCalculo("Gastos de Refinación", dolares(reduccionRefinacion), true),
            DetalleCalculo("Análisis", dolares(analisisPorTmns), true),
            DetalleCalculo("Traslado Interno", dolares(trasladoPorTmns), true),
            DetalleCalculo("Pago por TM de Concentrado", dolares(pagoPorTm))
        ),
        "3. Liquidación Final" to listOf(
            DetalleCalculo("Valor Neto", dolares(valorNeto)),
            DetalleCalculo("IGV (18 %)", dolares(igv)),
            DetalleCalculo("Valor Total", dolares(valorTotal)),
            DetalleCalculo("Detracción (10 %)", dolares(detraccion), true),
            DetalleCalculo("Alquiler de Planta", dolares(alquilerTotal), true)
        )
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, ColorGrisBorde)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = GrisCampos, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Factory,
                        contentDescription = null,
                        tint = AmarilloMineroBrillante,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PLANTA DESTINO DE PROCESAMIENTO", color = AmarilloMineroBrillante, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(2.dp))
                        Text(plantaConfirmada?.nombre ?: "No especificada", color = TextoBlancoSuave, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        PlantaMeta(plantaConfirmada)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FondoNegroAbsoluto,
                border = BorderStroke(1.dp, AmarilloMineroBrillante.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("LIQUIDACIÓN TOTAL A PAGAR", color = TextoGrisSecundario, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        dolares(totalAPagar),
                        color = if (totalAPagar >= 0) AmarilloMineroBrillante else ErrorRojo,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = ColorGrisBorde)
                    Spacer(Modifier.height(14.dp))
                    Text("TONELADAS DE CONCENTRADO (TMNS)", color = TextoGrisSecundario, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(toneladas(tmns), color = TextoBlancoSuave, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            listaDetalles.forEach { (titulo, detalles) ->
                SeccionTitulo(titulo)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GrisCampos.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        detalles.forEachIndexed { i, detalle ->
                            Fila(detalle.label, detalle.valor, detalle.esNegativo)
                            if (i < detalles.lastIndex) {
                                HorizontalDivider(color = ColorGrisBorde.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionTitulo(titulo: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .background(AmarilloMineroBrillante, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(titulo, color = AmarilloMineroBrillante, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// Campo numérico que acepta coma o punto decimal
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

// Fila de etiqueta y valor; los descuentos se muestran en rojo con signo negativo
@Composable
private fun Fila(etiqueta: String, valor: String, esNegativo: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(etiqueta, color = TextoGrisSecundario, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(
            text = if (esNegativo) "- $valor" else valor,
            color = if (esNegativo) ErrorRojo else TextoBlancoSuave,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}