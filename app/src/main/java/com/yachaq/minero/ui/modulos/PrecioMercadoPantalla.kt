package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yachaq.minero.R
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestMarketPrice
import com.yachaq.minero.data.remote.model.ResponseMarketPrice
import com.yachaq.minero.ui.theme.ExitoVerde
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

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
    else -> EstiloMetal(codigo?.take(2).orEmpty(), codigo ?: "—", Color.Gray, null)
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
fun PrecioMercadoPantalla(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes

    var precios by remember { mutableStateOf<List<ResponseMarketPrice>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var mensajeEsError by remember { mutableStateOf(false) }

    fun mostrar(texto: String, esError: Boolean = false) {
        mensaje = texto
        mensajeEsError = esError
    }

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
                        mostrar(resp.body()?.message ?: "Precio actualizado.")
                        cargarPrecios()
                    }
                    resp.code() == 403 -> mostrar("No tienes permiso (solo admin).", true)
                    else -> mostrar("Error ${resp.code()}", true)
                }
            } catch (e: Exception) {
                mostrar(e.message ?: "Error de red", true)
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = formas.medium,
                color = cs.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = cs.primary
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Precios del Mercado",
                    style = tp.titleLarge,
                    color = cs.primary
                )
                Text(
                    text = "Base del cálculo de liquidación",
                    style = tp.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                if (cargando || guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = { recargar() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar precios",
                            tint = cs.primary
                        )
                    }
                }
            }
        }

        val ultimaActualizacion = precios.mapNotNull { it.updatedAt }.maxOrNull()
        if (ultimaActualizacion != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Última actualización: ${formatoFecha(ultimaActualizacion)}",
                style = tp.bodySmall,
                color = cs.onSurfaceVariant
            )
        }

        mensaje?.let {
            val colorMensaje = if (mensajeEsError) cs.error else cs.primary
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = formas.small,
                color = colorMensaje.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, colorMensaje.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (mensajeEsError) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorMensaje,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(it, style = tp.bodySmall, color = cs.onSurface, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            cargando && precios.isEmpty() -> Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            error != null && precios.isEmpty() -> Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No se pudieron cargar los precios",
                    style = tp.titleSmall,
                    color = cs.onSurface
                )
                Text(error.orEmpty(), style = tp.bodySmall, color = cs.error)
                TextButton(onClick = { recargar() }) {
                    Text("REINTENTAR", style = tp.labelLarge)
                }
            }

            precios.isEmpty() -> Text(
                text = "No hay precios registrados.",
                style = tp.bodyMedium,
                color = cs.onSurfaceVariant
            )

            else -> precios.forEach { precio ->
                TarjetaPrecio(
                    precio = precio,
                    guardando = guardando,
                    onActualizar = { metal, nuevo -> actualizar(metal, nuevo) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = cs.onSurfaceVariant,
                modifier = Modifier.padding(top = 1.dp).size(14.dp)
            )
            Text(
                text = "Los precios se sincronizan automáticamente una vez al día. Si falla, puedes ajustarlos manualmente desde aquí.",
                style = tp.bodySmall,
                color = cs.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TarjetaPrecio(
    precio: ResponseMarketPrice,
    guardando: Boolean,
    onActualizar: (metal: String, nuevoPrecio: Double) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes
    val metal = precio.metal.orEmpty()
    val estilo = estiloMetal(metal)
    val unidad = unidadMetal(metal)
    var mostrarDialogo by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
        shape = formas.large,
        border = BorderStroke(1.dp, cs.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = formas.medium,
                    color = estilo.color.copy(alpha = 0.15f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (estilo.recurso != null) {
                            Image(
                                painter = painterResource(id = estilo.recurso),
                                contentDescription = estilo.nombre,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize().padding(8.dp)
                            )
                        } else {
                            Text(
                                text = estilo.simbolo,
                                style = tp.titleLarge,
                                color = estilo.color,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = estilo.nombre,
                            style = tp.titleMedium,
                            color = cs.onSurface
                        )
                        Surface(
                            shape = formas.small,
                            color = estilo.color.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = estilo.simbolo,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = tp.labelSmall,
                                color = estilo.color
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = precio.price?.let { "$ ${formatoPrecio(it)}" } ?: "—",
                            style = tp.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = cs.primary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = unidad,
                            style = tp.bodySmall,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = "Actualizado: ${formatoFecha(precio.updatedAt)}",
                        style = tp.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = cs.outlineVariant)
            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { mostrarDialogo = true },
                enabled = !guardando,
                shape = formas.small,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Actualizar precio", style = tp.labelMedium)
            }
        }
    }

    if (mostrarDialogo) {
        DialogoActualizarPrecio(
            estilo = estilo,
            unidad = unidad,
            actual = precio.price,
            onDismiss = { mostrarDialogo = false },
            onConfirmar = { nuevo ->
                mostrarDialogo = false
                onActualizar(metal, nuevo)
            }
        )
    }
}

@Composable
private fun DialogoActualizarPrecio(
    estilo: EstiloMetal,
    unidad: String,
    actual: Double?,
    onDismiss: () -> Unit,
    onConfirmar: (Double) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes

    var texto by remember {
        mutableStateOf(actual?.let { String.format(Locale.US, "%.2f", it) } ?: "")
    }

    val nuevo = texto.replace(',', '.').toDoubleOrNull()
    val valido = nuevo != null && nuevo > 0
    val puedeGuardar = valido && nuevo != actual
    val variacion = if (nuevo != null && nuevo > 0 && actual != null && actual > 0) {
        (nuevo - actual) / actual * 100
    } else null

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = formas.large,
        containerColor = cs.surfaceContainer,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = cs.primary
                )
                Text(
                    text = "Actualizar ${estilo.nombre.lowercase()}",
                    style = tp.titleLarge,
                    color = cs.onSurface
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Precio actual: ${actual?.let { "$ ${formatoPrecio(it)}" } ?: "—"} $unidad",
                    style = tp.bodySmall,
                    color = cs.onSurfaceVariant
                )

                OutlinedTextField(
                    value = texto,
                    onValueChange = { entrada ->
                        texto = entrada.filter { it.isDigit() || it == '.' || it == ',' }
                    },
                    label = { Text("Nuevo precio") },
                    prefix = { Text("$") },
                    suffix = { Text(unidad, style = tp.bodySmall) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = formas.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cs.surfaceContainerHigh,
                        unfocusedContainerColor = cs.surfaceContainerHigh,
                        focusedBorderColor = cs.primary,
                        unfocusedBorderColor = cs.outlineVariant,
                        focusedLabelColor = cs.primary,
                        unfocusedLabelColor = cs.onSurfaceVariant,
                        focusedTextColor = cs.onSurface,
                        unfocusedTextColor = cs.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (variacion != null) {
                    Text(
                        text = "Variación: ${String.format(Locale.US, "%+.2f%%", variacion)}",
                        style = tp.labelMedium,
                        color = if (variacion >= 0) ExitoVerde else cs.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nuevo != null && nuevo > 0) onConfirmar(nuevo) },
                enabled = puedeGuardar,
                shape = formas.medium
            ) {
                Text("Guardar precio", style = tp.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", style = tp.labelLarge, color = cs.onSurfaceVariant)
            }
        }
    )
}