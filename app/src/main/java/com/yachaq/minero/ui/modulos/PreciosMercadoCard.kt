package com.yachaq.minero.ui.modulos

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yachaq.minero.R
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.ui.theme.ExitoVerde
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

// Los precios anteriores se conservan mientras se actualiza o si la actualización falla
data class PreciosUiState(
    val precios: Map<String, Double> = emptyMap(),
    val actualizado: String? = null,
    val cargando: Boolean = true,
    val error: String? = null
) {
    val hayDatos: Boolean get() = precios.isNotEmpty()
}

class PreciosMercadoViewModel : ViewModel() {

    private val _state = MutableStateFlow(PreciosUiState())
    val state: StateFlow<PreciosUiState> = _state.asStateFlow()

    private var trabajo: Job? = null

    init { cargar() }

    fun cargar() {
        if (trabajo?.isActive == true) return
        _state.update { it.copy(cargando = true, error = null) }

        trabajo = viewModelScope.launch {
            try {
                val response = RetrofitClient.marketPriceApi.listar()
                if (!response.isSuccessful) {
                    _state.update { it.copy(cargando = false, error = "Error del servidor (${response.code()})") }
                    return@launch
                }

                val lista = response.body().orEmpty()
                val precios = lista
                    .filter { it.metal != null && it.price != null }
                    .associate { it.metal!!.uppercase() to it.price!! }

                if (precios.isEmpty()) {
                    // El backend responde un solo elemento con el motivo cuando no hay precios
                    val mensaje = lista.firstOrNull()?.listMessage?.firstOrNull() ?: "No hay precios registrados."
                    _state.update { it.copy(cargando = false, error = mensaje) }
                } else {
                    _state.update {
                        it.copy(
                            precios = precios,
                            actualizado = lista.mapNotNull { item -> item.updatedAt }.maxOrNull(),
                            cargando = false,
                            error = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(cargando = false, error = "No se pudo conectar: ${e.message}") }
            }
        }
    }
}

private fun formatoPrecio(v: Double): String = String.format(Locale.US, "%,.2f", v)

// Convierte "2026-11-10T16:38:05.123" en "10/11/2026 · 16:38"
private fun formatoFecha(fecha: String?): String {
    if (fecha == null) return "—"
    if (fecha.length < 16 || fecha[10] != 'T') return fecha
    val partes = fecha.substring(0, 10).split("-")
    if (partes.size != 3) return fecha
    return "${partes[2]}/${partes[1]}/${partes[0]} · ${fecha.substring(11, 16)}"
}

// Reduce la cifra cuando es larga para que no se corte en pantallas angostas
private fun tamanoPrecio(texto: String): TextUnit = when {
    texto.length > 9 -> 13.sp
    texto.length > 7 -> 15.sp
    else -> 17.sp
}

private val ColorCobre = Color(0xFFE08A3C)
private val ColorPlata = Color(0xFFB0BEC5)
private val ColorOro = Color(0xFFFFD54F)

@Composable
fun PreciosMercadoCard(
    modifier: Modifier = Modifier,
    viewModel: PreciosMercadoViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val cs = MaterialTheme.colorScheme
    val forma = MaterialTheme.shapes.extraLarge

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(
                Brush.linearGradient(
                    listOf(
                        cs.primary.copy(alpha = 0.14f),
                        cs.surfaceContainer,
                        cs.surfaceContainer
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        cs.primary.copy(alpha = 0.7f),
                        cs.primary.copy(alpha = 0.08f)
                    )
                ),
                forma
            )
            .padding(18.dp)
    ) {
        EncabezadoPrecios(cargando = state.cargando, onActualizar = { viewModel.cargar() })

        Spacer(Modifier.height(16.dp))

        when {
            state.hayDatos -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ItemPrecio("COBRE", R.drawable.cobre, state.precios["CU"], "$/lb", ColorCobre, Modifier.weight(1f))
                    ItemPrecio("PLATA", R.drawable.plata, state.precios["AG"], "$/oz tr", ColorPlata, Modifier.weight(1f))
                    ItemPrecio("ORO", R.drawable.oro, state.precios["AU"], "$/oz tr", ColorOro, Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                PiePrecios(actualizado = state.actualizado, error = state.error)
            }

            state.cargando -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { PrecioSkeleton(Modifier.weight(1f)) }
                }
            }

            else -> ErrorPrecios(mensaje = state.error, onReintentar = { viewModel.cargar() })
        }
    }
}

@Composable
private fun EncabezadoPrecios(cargando: Boolean, onActualizar: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "PRECIOS DEL MERCADO",
                style = tp.labelMedium,
                color = cs.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp
            )
            Text(
                text = "Base de tu liquidación",
                style = tp.bodySmall,
                color = cs.onSurfaceVariant
            )
        }
        BotonActualizar(cargando = cargando, onClick = onActualizar)
    }
}

// El ícono gira mientras se actualiza y el botón queda deshabilitado
@Composable
private fun BotonActualizar(cargando: Boolean, onClick: () -> Unit) {
    val grados = if (cargando) {
        val transicion = rememberInfiniteTransition(label = "giroActualizar")
        val valor by transicion.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
            label = "grados"
        )
        valor
    } else {
        0f
    }

    IconButton(onClick = onClick, enabled = !cargando) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Actualizar precios",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = if (cargando) 0.6f else 1f),
            modifier = Modifier.rotate(grados)
        )
    }
}

// Verde si está al día, rojo si la última consulta falló
@Composable
private fun PiePrecios(actualizado: String?, error: String?) {
    val cs = MaterialTheme.colorScheme
    val fallo = error != null
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(if (fallo) cs.error else ExitoVerde, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (fallo) {
                "No se pudo actualizar. Última actualización: ${formatoFecha(actualizado)}"
            } else {
                "Actualizado: ${formatoFecha(actualizado)}"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (fallo) cs.error else cs.onSurfaceVariant
        )
    }
}

@Composable
private fun ItemPrecio(
    nombre: String,
    recurso: Int,
    precio: Double?,
    unidad: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes
    val texto = precio?.let { formatoPrecio(it) }

    Column(
        modifier = modifier
            .clip(formas.large)
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.4f), formas.large)
            .padding(vertical = 12.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(formas.medium)
                .background(
                    Brush.radialGradient(
                        listOf(
                            color.copy(alpha = 0.38f),
                            color.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = recurso),
                contentDescription = nombre,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(7.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = nombre,
            style = tp.labelSmall,
            color = cs.onSurfaceVariant,
            fontWeight = FontWeight.Black
        )

        Spacer(Modifier.height(2.dp))

        if (texto != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$",
                    style = tp.labelMedium,
                    color = cs.onSurfaceVariant
                )
                Text(
                    text = texto,
                    style = tp.titleMedium,
                    color = cs.onSurface,
                    fontSize = tamanoPrecio(texto),
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        } else {
            Text(
                text = "—",
                style = tp.titleMedium,
                color = cs.onSurfaceVariant,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Text(
            text = unidad,
            style = tp.labelSmall,
            color = color
        )
    }
}

@Composable
private fun PrecioSkeleton(modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val formas = MaterialTheme.shapes
    val transicion = rememberInfiniteTransition(label = "cargaPrecio")
    val alfa by transicion.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alfaCarga"
    )
    val bloque = cs.onSurfaceVariant.copy(alpha = alfa)

    Column(
        modifier = modifier
            .clip(formas.large)
            .background(cs.onSurfaceVariant.copy(alpha = alfa / 2))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(54.dp).background(bloque, formas.medium))
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.width(40.dp).height(8.dp).background(bloque, formas.extraSmall))
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.width(56.dp).height(14.dp).background(bloque, formas.extraSmall))
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.width(32.dp).height(8.dp).background(bloque, formas.extraSmall))
    }
}

// Solo cuando nunca se pudo cargar ningún precio
@Composable
private fun ErrorPrecios(mensaje: String?, onReintentar: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            tint = cs.error,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "No se pudieron cargar los precios",
            style = tp.titleSmall,
            color = cs.onSurface,
            textAlign = TextAlign.Center
        )
        mensaje?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = it,
                style = tp.bodySmall,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onReintentar,
            shape = MaterialTheme.shapes.medium
        ) {
            Text("REINTENTAR", style = tp.labelLarge, fontWeight = FontWeight.Black)
        }
    }
}