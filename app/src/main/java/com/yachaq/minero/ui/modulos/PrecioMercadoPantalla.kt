package com.yachaq.minero.ui.modulos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.R
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestMarketPrice
import com.yachaq.minero.data.remote.model.ResponseMarketPrice
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

private val ColorSubida = Color(0xFF4CAF50)

private class EstiloMetal(
    val simbolo: String,
    val nombre: String,
    val color: Color,
    val recurso: Int?
)

private fun estiloMetal(codigo: String?): EstiloMetal = when (codigo?.uppercase()) {
    "CU" -> EstiloMetal("Cu", "Cobre", Color(0xFFE08A3C), R.drawable.cobre)
    "AG" -> EstiloMetal("Ag", "Plata", Color(0xFFB0BEC5), R.drawable.plata)
    "AU" -> EstiloMetal("Au", "Oro", Color(0xFFFFD54F), R.drawable.oro)
    else -> EstiloMetal(codigo?.take(2).orEmpty(), codigo ?: "—", TextoGrisSecundario, null)
}

private fun metalOrden(metal: String?): Int = when (metal?.uppercase()) {
    "CU" -> 0
    "AG" -> 1
    "AU" -> 2
    else -> 99
}

private fun unidadMetal(codigo: String?): String = when (codigo?.uppercase()) {
    "CU" -> "$/lb"
    else -> "$/oz tr"
}

private fun formatoPrecio(v: Double): String = String.format(Locale.US, "%,.2f", v)

private fun formatoFecha(fecha: String?): String =
    fecha?.replace('T', ' ')?.take(16) ?: "—"

@Composable
private fun rememberShimmer(): Brush {
    val transicion = rememberInfiniteTransition(label = "shimmer")
    val desplazamiento by transicion.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    return Brush.linearGradient(
        colors = listOf(
            ColorGrisBorde.copy(alpha = 0.25f),
            ColorGrisBorde.copy(alpha = 0.6f),
            ColorGrisBorde.copy(alpha = 0.25f)
        ),
        start = Offset(desplazamiento, 0f),
        end = Offset(desplazamiento + 400f, 200f)
    )
}

@Composable
fun PrecioMercadoPantalla(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()

    var precios by remember { mutableStateOf<List<ResponseMarketPrice>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    suspend fun cargarPrecios() {
        error = null
        try {
            val resp = RetrofitClient.marketPriceApi.listar()
            if (resp.isSuccessful) {
                precios = resp.body().orEmpty()
                    .filter { it.metal != null }
                    .sortedBy { metalOrden(it.metal) }
            } else {
                error = "Error ${resp.code()}"
            }
        } catch (e: Exception) {
            error = e.message ?: "Error al cargar precios"
        }
    }

    fun recargar() {
        scope.launch {
            cargando = true
            cargarPrecios()
            cargando = false
        }
    }

    fun actualizar(metal: String, price: Double) {
        scope.launch {
            guardando = true
            mensaje = null
            try {
                val resp = RetrofitClient.marketPriceApi
                    .actualizar(RequestMarketPrice(metal = metal, price = price))
                when {
                    resp.isSuccessful -> {
                        mensaje = resp.body()?.message ?: "Precio actualizado."
                        cargarPrecios()
                    }
                    resp.code() == 403 -> mensaje = "No tienes permiso (solo admin)."
                    else -> mensaje = "Error ${resp.code()}"
                }
            } catch (e: Exception) {
                mensaje = e.message ?: "Error de red"
            } finally {
                guardando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargando = true
        cargarPrecios()
        cargando = false
    }

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            delay(3500)
            mensaje = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        EncabezadoPrecios(
            ultimaActualizacion = precios.mapNotNull { it.updatedAt }.maxOrNull(),
            ocupado = cargando || guardando,
            onRecargar = { recargar() }
        )

        Spacer(Modifier.height(20.dp))

        mensaje?.let {
            BannerMensaje(it)
            Spacer(Modifier.height(16.dp))
        }

        when {
            cargando && precios.isEmpty() -> {
                repeat(3) {
                    TarjetaCarga()
                    Spacer(Modifier.height(14.dp))
                }
            }

            error != null && precios.isEmpty() -> TarjetaError(
                mensaje = error.orEmpty(),
                onReintentar = { recargar() }
            )

            precios.isEmpty() -> Text(
                text = "No hay precios registrados.",
                color = TextoGrisSecundario,
                fontSize = 13.sp
            )

            else -> precios.forEachIndexed { indice, precio ->
                TarjetaPrecioMercado(
                    indice = indice,
                    precio = precio,
                    guardando = guardando,
                    onConfirmar = { metal, nuevoPrecio -> actualizar(metal, nuevoPrecio) }
                )
                Spacer(Modifier.height(14.dp))
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TextoGrisSecundario,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(14.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Los precios se sincronizan automáticamente una vez al día. Si la sincronización falla, puedes ajustarlos manualmente desde aquí.",
                color = TextoGrisSecundario,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EncabezadoPrecios(
    ultimaActualizacion: String?,
    ocupado: Boolean,
    onRecargar: () -> Unit
) {
    val forma = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(
                Brush.verticalGradient(
                    listOf(
                        AmarilloMineroBrillante.copy(alpha = 0.18f),
                        FondoNegroSecundario
                    )
                )
            )
            .border(1.dp, AmarilloMineroBrillante.copy(alpha = 0.28f), forma)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MERCADO DE METALES",
                    color = AmarilloMineroBrillante,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.6.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Precios de referencia",
                    color = TextoBlancoSuave,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AmarilloMineroBrillante.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                if (ocupado) {
                    CircularProgressIndicator(
                        color = AmarilloMineroBrillante,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    IconButton(onClick = onRecargar) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar precios",
                            tint = AmarilloMineroBrillante
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Base del cálculo de liquidación de cobre, plata y oro.",
            color = TextoGrisSecundario,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(ColorSubida)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Última actualización: ${formatoFecha(ultimaActualizacion)}",
                color = TextoBlancoSuave,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BannerMensaje(texto: String) {
    val forma = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(AmarilloMineroBrillante.copy(alpha = 0.14f))
            .border(1.dp, AmarilloMineroBrillante.copy(alpha = 0.4f), forma)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = AmarilloMineroBrillante,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = texto,
            color = TextoBlancoSuave,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TarjetaCarga() {
    val brush = rememberShimmer()
    val forma = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(FondoNegroSecundario)
            .border(1.dp, ColorGrisBorde, forma)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(brush)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
private fun TarjetaError(mensaje: String, onReintentar: () -> Unit) {
    val forma = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(ErrorRojo.copy(alpha = 0.08f))
            .border(1.dp, ErrorRojo.copy(alpha = 0.4f), forma)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No se pudieron cargar los precios",
            color = TextoBlancoSuave,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(text = mensaje, color = ErrorRojo, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onReintentar,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AmarilloMineroBrillante,
                contentColor = FondoNegroAbsoluto
            )
        ) {
            Text("REINTENTAR", fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ImagenMetal(estilo: EstiloMetal) {
    val forma = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(forma)
            .background(
                Brush.radialGradient(
                    listOf(
                        estilo.color.copy(alpha = 0.38f),
                        estilo.color.copy(alpha = 0.06f)
                    )
                )
            )
            .border(1.dp, estilo.color.copy(alpha = 0.45f), forma),
        contentAlignment = Alignment.Center
    ) {
        if (estilo.recurso != null) {
            Image(
                painter = painterResource(id = estilo.recurso),
                contentDescription = estilo.nombre,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            )
        } else {
            Text(
                text = estilo.simbolo,
                color = estilo.color,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun PrecioAnimado(valor: Double?, unidad: String, color: Color) {
    var objetivo by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(valor) { objetivo = valor?.toFloat() ?: 0f }

    val mostrado by animateFloatAsState(
        targetValue = objetivo,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "precioAnimado"
    )

    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = if (valor != null) "$${formatoPrecio(mostrado.toDouble())}" else "—",
            color = TextoBlancoSuave,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = unidad,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 5.dp)
        )
    }
}

@Composable
private fun TarjetaPrecioMercado(
    indice: Int,
    precio: ResponseMarketPrice,
    guardando: Boolean,
    onConfirmar: (metal: String, nuevoPrecio: Double) -> Unit
) {
    val metal = precio.metal.orEmpty()
    val estilo = estiloMetal(metal)
    val unidad = unidadMetal(metal)
    val actual = precio.price
    val forma = RoundedCornerShape(24.dp)

    var visible by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf(false) }
    var textoPrecio by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(indice * 90L)
        visible = true
    }

    val nuevo = textoPrecio.replace(',', '.').toDoubleOrNull()
    val nuevoValido = nuevo != null && nuevo > 0
    val variacion = if (nuevoValido && actual != null && actual > 0) {
        (nuevo!! - actual) / actual * 100
    } else null

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 5 }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(forma)
                .background(
                    Brush.linearGradient(
                        listOf(
                            estilo.color.copy(alpha = 0.18f),
                            FondoNegroSecundario,
                            FondoNegroSecundario
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            estilo.color.copy(alpha = 0.75f),
                            estilo.color.copy(alpha = 0.10f)
                        )
                    ),
                    forma
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ImagenMetal(estilo)
                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = estilo.nombre.uppercase(),
                            color = estilo.color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.4.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(estilo.color.copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = estilo.simbolo,
                                color = estilo.color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    PrecioAnimado(valor = actual, unidad = unidad, color = estilo.color)

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = "Actualizado: ${formatoFecha(precio.updatedAt)}",
                        color = TextoGrisSecundario,
                        fontSize = 10.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = !editando,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            textoPrecio = actual?.let { String.format(Locale.US, "%.2f", it) } ?: ""
                            editando = true
                        },
                        enabled = !guardando,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, estilo.color.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = estilo.color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "ACTUALIZAR PRECIO",
                            color = estilo.color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = editando,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = textoPrecio,
                        onValueChange = { entrada ->
                            textoPrecio = entrada.filter { it.isDigit() || it == '.' || it == ',' }
                        },
                        label = { Text("Nuevo precio") },
                        prefix = { Text("$", color = TextoGrisSecundario) },
                        suffix = { Text(unidad, color = TextoGrisSecundario, fontSize = 12.sp) },
                        singleLine = true,
                        enabled = !guardando,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextoBlancoSuave,
                            unfocusedTextColor = TextoBlancoSuave,
                            focusedBorderColor = estilo.color,
                            unfocusedBorderColor = ColorGrisBorde,
                            focusedLabelColor = estilo.color,
                            unfocusedLabelColor = TextoGrisSecundario,
                            cursorColor = estilo.color
                        )
                    )

                    if (variacion != null) {
                        Spacer(Modifier.height(8.dp))
                        val sube = variacion >= 0
                        Text(
                            text = "Variación: ${String.format(Locale.US, "%+.2f%%", variacion)}",
                            color = if (sube) ColorSubida else ErrorRojo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editando = false },
                            enabled = !guardando,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ColorGrisBorde),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("Cancelar", color = TextoGrisSecundario, fontSize = 13.sp)
                        }
                        Button(
                            onClick = { mostrarDialogo = true },
                            enabled = !guardando && nuevoValido,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = estilo.color,
                                contentColor = FondoNegroAbsoluto
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("Guardar", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            containerColor = FondoNegroSecundario,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Confirmar actualización",
                    color = TextoBlancoSuave,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Se actualizará el precio de ${estilo.nombre.lowercase()}:",
                        color = TextoGrisSecundario,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${actual?.let { "$${formatoPrecio(it)}" } ?: "—"}  →  ${if (nuevoValido) "$${formatoPrecio(nuevo!!)}" else "—"}",
                        color = estilo.color,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = unidad,
                        color = TextoGrisSecundario,
                        fontSize = 11.sp
                    )
                    if (variacion != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = String.format(Locale.US, "%+.2f%%", variacion),
                            color = if (variacion >= 0) ColorSubida else ErrorRojo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevoValido) {
                            mostrarDialogo = false
                            editando = false
                            onConfirmar(metal, nuevo!!)
                        }
                    },
                    enabled = nuevoValido,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = estilo.color,
                        contentColor = FondoNegroAbsoluto
                    )
                ) {
                    Text("Confirmar", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar", color = TextoGrisSecundario)
                }
            }
        )
    }
}