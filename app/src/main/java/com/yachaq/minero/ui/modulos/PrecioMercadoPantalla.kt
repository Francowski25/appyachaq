package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestMarketPrice
import com.yachaq.minero.data.remote.model.ResponseMarketPrice
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// ---------- Helpers ----------

private fun metalOrden(metal: String?): Int = when (metal?.uppercase()) {
    "CU" -> 0
    "AG" -> 1
    "AU" -> 2
    else -> 99
}

private fun nombreMetal(codigo: String?): String = when (codigo?.uppercase()) {
    "CU" -> "Cobre"
    "AG" -> "Plata"
    "AU" -> "Oro"
    else -> codigo ?: "—"
}

private fun unidadMetal(codigo: String?): String = when (codigo?.uppercase()) {
    "CU" -> "$/lb"
    else -> "$/oz tr"
}

private fun formatoPrecio(v: Double): String = String.format(Locale.US, "%,.2f", v)

private fun formatoFecha(fecha: String?): String =
    fecha?.replace('T', ' ')?.take(16) ?: "—"

private class EstiloMetal(val simbolo: String, val color: Color)

private fun estiloMetal(codigo: String?): EstiloMetal = when (codigo?.uppercase()) {
    "CU" -> EstiloMetal("Cu", Color(0xFFE08A3C))
    "AG" -> EstiloMetal("Ag", Color(0xFFB0BEC5))
    "AU" -> EstiloMetal("Au", Color(0xFFFFD54F))
    else -> EstiloMetal(codigo?.take(2) ?: "?", TextoGrisSecundario)
}

@Composable
private fun InsigniaMetal(codigo: String?, tamano: Int = 44) {
    val estilo = estiloMetal(codigo)
    Box(
        modifier = Modifier
            .size(tamano.dp)
            .clip(CircleShape)
            .background(estilo.color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = estilo.simbolo,
            color = estilo.color,
            fontWeight = FontWeight.Black,
            fontSize = (tamano * 0.36f).sp
        )
    }
}

// ---------- Pantalla de administración ----------

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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "PRECIOS DEL MERCADO",
            color = AmarilloMineroBrillante,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Precios de referencia usados en el cálculo de liquidación.",
            color = TextoGrisSecundario,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(16.dp))

        when {
            cargando -> Box(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AmarilloMineroBrillante,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }

            error != null -> {
                Text(error.orEmpty(), color = ErrorRojo, fontSize = 12.sp)
                TextButton(onClick = {
                    scope.launch {
                        cargando = true
                        cargarPrecios()
                        cargando = false
                    }
                }) { Text("REINTENTAR", color = AmarilloMineroBrillante) }
            }

            precios.isEmpty() -> Text(
                "No hay precios registrados.",
                color = TextoGrisSecundario,
                fontSize = 13.sp
            )

            else -> precios.forEach { precio ->
                TarjetaPrecioMercado(
                    precio = precio,
                    guardando = guardando,
                    onConfirmar = { metal, nuevoPrecio -> actualizar(metal, nuevoPrecio) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        mensaje?.let { msg ->
            Spacer(Modifier.height(4.dp))
            Text(msg, color = AmarilloMineroBrillante, fontSize = 12.sp)
            LaunchedEffect(msg) {
                delay(3000)
                mensaje = null
            }
        }
    }
}

// ---------- Tarjeta por metal (admin) ----------

@Composable
private fun TarjetaPrecioMercado(
    precio: ResponseMarketPrice,
    guardando: Boolean,
    onConfirmar: (metal: String, nuevoPrecio: Double) -> Unit
) {
    var editando by remember { mutableStateOf(false) }
    var textoPrecio by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val metal = precio.metal ?: return
    val estilo = estiloMetal(metal)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        border = BorderStroke(1.dp, estilo.color.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InsigniaMetal(metal)
                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombreMetal(metal).uppercase(),
                        color = estilo.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = precio.price?.let { "$${formatoPrecio(it)}" } ?: "—",
                            color = TextoBlancoSuave,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = unidadMetal(metal),
                            color = TextoGrisSecundario,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    Text(
                        text = "Actualizado: ${formatoFecha(precio.updatedAt)}",
                        color = TextoGrisSecundario,
                        fontSize = 10.sp
                    )
                }

                if (!editando) {
                    OutlinedButton(
                        onClick = {
                            textoPrecio = precio.price?.let { formatoPrecio(it).replace(",", "") } ?: ""
                            editando = true
                        },
                        enabled = !guardando,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, estilo.color.copy(alpha = 0.6f))
                    ) {
                        Text("Actualizar", fontSize = 12.sp, color = estilo.color)
                    }
                }
            }

            if (editando) {
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = textoPrecio,
                    onValueChange = { textoPrecio = it },
                    label = { Text("Nuevo precio (${unidadMetal(metal)})") },
                    singleLine = true,
                    enabled = !guardando,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
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

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { editando = false },
                        enabled = !guardando
                    ) {
                        Text("Cancelar", color = TextoGrisSecundario)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { mostrarDialogo = true },
                        enabled = !guardando && textoPrecio.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmarilloMineroBrillante,
                            contentColor = FondoNegroSecundario
                        )
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        val nuevo = textoPrecio.replace(",", ".").toDoubleOrNull()
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Confirmar actualización") },
            text = {
                Text(
                    if (nuevo != null && nuevo > 0)
                        "¿Actualizar ${nombreMetal(metal)} a $${formatoPrecio(nuevo)} ${unidadMetal(metal)}?"
                    else
                        "El precio ingresado no es válido."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nuevo != null && nuevo > 0) {
                            mostrarDialogo = false
                            editando = false
                            onConfirmar(metal, nuevo)
                        }
                    },
                    enabled = nuevo != null && nuevo > 0
                ) {
                    Text("Confirmar", color = AmarilloMineroBrillante, fontWeight = FontWeight.Bold)
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

// ---------- Resumen compacto de solo lectura (para la calculadora) ----------

@Composable
fun ResumenPreciosMercado(modifier: Modifier = Modifier) {
    var precios by remember { mutableStateOf<List<ResponseMarketPrice>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val resp = RetrofitClient.marketPriceApi.listar()
            if (resp.isSuccessful) {
                precios = resp.body().orEmpty()
                    .filter { it.metal != null && it.price != null }
                    .sortedBy { metalOrden(it.metal) }
            }
        } catch (e: Exception) {
            // Sin conexión: simplemente no se muestra el resumen
        }
        cargando = false
    }

    when {
        cargando -> Box(
            modifier = modifier.fillMaxWidth().height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = AmarilloMineroBrillante,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        }

        precios.isNotEmpty() -> Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
            border = BorderStroke(1.dp, ColorGrisBorde)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "PRECIOS DE REFERENCIA",
                    color = AmarilloMineroBrillante,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    precios.forEach { p ->
                        ItemPrecioCompacto(p, Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Actualizado: ${formatoFecha(precios.mapNotNull { it.updatedAt }.maxOrNull())}",
                    color = TextoGrisSecundario,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun ItemPrecioCompacto(precio: ResponseMarketPrice, modifier: Modifier = Modifier) {
    val estilo = estiloMetal(precio.metal)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InsigniaMetal(precio.metal, tamano = 32)
        Spacer(Modifier.height(6.dp))
        Text(
            text = precio.price?.let { "$${formatoPrecio(it)}" } ?: "—",
            color = TextoBlancoSuave,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = unidadMetal(precio.metal),
            color = estilo.color,
            fontSize = 10.sp
        )
    }
}