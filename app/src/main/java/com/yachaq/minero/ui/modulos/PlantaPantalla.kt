package com.yachaq.minero.ui.modulos

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yachaq.minero.R
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.ResponsePlant
import com.yachaq.minero.ui.theme.AdvertenciaNaranja
import com.yachaq.minero.ui.theme.ExitoVerde
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

private fun ResponsePlant.esOperativa() = status.equals("Operativa", ignoreCase = true)

// Los enteros se muestran sin decimales y el resto con un decimal
private fun formatoNumero(valor: Double): String =
    if (valor % 1.0 == 0.0) String.format(Locale.US, "%,.0f", valor)
    else String.format(Locale.US, "%,.1f", valor)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantaPantalla() {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography

    var plantas by remember { mutableStateOf<List<ResponsePlant>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var refrescando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var mostrarVisor by remember { mutableStateOf(false) }

    suspend fun cargarPlantas() {
        error = null
        try {
            val resp = RetrofitClient.plantApi.listarPlantas()
            if (resp.isSuccessful) {
                plantas = resp.body().orEmpty()
            } else {
                error = "Error al cargar plantas (${resp.code()})"
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = "No se pudo conectar: ${e.message}"
        }
    }

    fun recargar() {
        scope.launch {
            cargando = true
            cargarPlantas()
            cargando = false
        }
    }

    LaunchedEffect(Unit) {
        cargando = true
        cargarPlantas()
        cargando = false
    }

    PullToRefreshBox(
        isRefreshing = refrescando,
        onRefresh = {
            scope.launch {
                refrescando = true
                cargarPlantas()
                refrescando = false
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "encabezado") {
                Column {
                    Text(
                        text = "Plantas Procesadoras",
                        style = tp.titleLarge,
                        color = cs.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vista general y listado de plantas procesadoras en Apurímac.",
                        style = tp.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            item(key = "vistaGeneral") {
                VistaGeneralPlanta(onClick = { mostrarVisor = true })
            }

            when {
                cargando && plantas.isEmpty() -> items(3) { PlantaSkeleton() }

                error != null && plantas.isEmpty() -> item(key = "error") {
                    EstadoMensaje(
                        icono = Icons.Default.CloudOff,
                        colorIcono = cs.error,
                        titulo = "No se pudieron cargar las plantas",
                        detalle = error.orEmpty(),
                        textoAccion = "Reintentar",
                        onAccion = { recargar() }
                    )
                }

                plantas.isEmpty() -> item(key = "vacio") {
                    EstadoMensaje(
                        icono = Icons.Default.Factory,
                        titulo = "Sin plantas registradas",
                        detalle = "Cuando se registren plantas procesadoras aparecerán aquí."
                    )
                }

                else -> {
                    // Si falló una actualización pero ya había datos, se conservan y se avisa
                    error?.let { mensaje ->
                        item(key = "aviso") { BannerError(mensaje = mensaje, onReintentar = { recargar() }) }
                    }

                    items(items = plantas, key = { it.idPlant ?: it.hashCode() }) { planta ->
                        PlantaItemCard(planta = planta, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }

    if (mostrarVisor) {
        VisorImagen(onCerrar = { mostrarVisor = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VistaGeneralPlanta(onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes

    Card(
        onClick = onClick,
        shape = formas.large,
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
        border = BorderStroke(1.dp, cs.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.planta_procesadora),
                contentDescription = "Vista general de la planta",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(formas.medium)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vista general de la planta",
                    style = tp.titleSmall,
                    color = cs.onSurface
                )
                Text(
                    text = "Toca para ver la imagen en pantalla completa",
                    style = tp.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = null,
                tint = cs.primary
            )
        }
    }
}

// Zoom de pellizco, arrastre y doble toque para ampliar o restablecer
@Composable
private fun VisorImagen(onCerrar: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    var escala by remember { mutableFloatStateOf(1f) }
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }

    val estadoTransformacion = rememberTransformableState { zoom, pan, _ ->
        escala = (escala * zoom).coerceIn(1f, 5f)
        desplazamiento = if (escala == 1f) Offset.Zero else desplazamiento + pan
    }

    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = cs.background) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.planta_procesadora),
                    contentDescription = "Planta procesadora",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = escala,
                            scaleY = escala,
                            translationX = desplazamiento.x,
                            translationY = desplazamiento.y
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(onDoubleTap = {
                                if (escala > 1f) {
                                    escala = 1f
                                    desplazamiento = Offset.Zero
                                } else {
                                    escala = 2.5f
                                }
                            })
                        }
                        .transformable(state = estadoTransformacion)
                )

                FilledTonalIconButton(
                    onClick = onCerrar,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }

                Text(
                    text = "Pellizca para ampliar o toca dos veces",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun PlantaItemCard(planta: ResponsePlant, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes
    val estado = planta.status ?: "Sin estado"
    val estadoColor = if (planta.esOperativa()) ExitoVerde else AdvertenciaNaranja

    Card(
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
        shape = formas.large,
        border = BorderStroke(1.dp, cs.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(cs.primary.copy(alpha = 0.12f), formas.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Factory,
                        contentDescription = null,
                        tint = cs.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = planta.name ?: "Sin nombre",
                        style = tp.titleMedium,
                        color = cs.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = planta.location ?: "Ubicación no especificada",
                            style = tp.bodySmall,
                            color = cs.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                EstadoBadge(texto = estado, color = estadoColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DatoPlanta(
                    etiqueta = "Capacidad",
                    valor = "${formatoNumero(planta.capacityTMAD ?: 0.0)} TMAD",
                    modifier = Modifier.weight(1f)
                )
                DatoPlanta(
                    etiqueta = "Alquiler por TM",
                    valor = String.format(Locale.US, "$ %.2f", planta.rentalCostPerTM ?: 0.0),
                    colorValor = cs.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EstadoBadge(texto: String, color: Color) {
    Surface(shape = CircleShape, color = color.copy(alpha = 0.2f)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun DatoPlanta(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    colorValor: Color = MaterialTheme.colorScheme.onSurface
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = cs.surfaceContainerHigh,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(text = etiqueta, style = tp.labelSmall, color = cs.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = valor,
                style = tp.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = colorValor
            )
        }
    }
}

@Composable
private fun PlantaSkeleton() {
    val cs = MaterialTheme.colorScheme
    val formas = MaterialTheme.shapes
    val transicion = rememberInfiniteTransition(label = "carga")
    val alfa by transicion.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alfaCarga"
    )
    val bloque = cs.onSurfaceVariant.copy(alpha = alfa)

    Card(
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
        shape = formas.large,
        border = BorderStroke(1.dp, cs.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).background(bloque, formas.medium))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(14.dp)
                            .background(bloque, formas.extraSmall)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(10.dp)
                            .background(bloque, formas.extraSmall)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).height(52.dp).background(bloque, formas.medium))
                Box(modifier = Modifier.weight(1f).height(52.dp).background(bloque, formas.medium))
            }
        }
    }
}

@Composable
private fun EstadoMensaje(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    modifier: Modifier = Modifier,
    colorIcono: Color = MaterialTheme.colorScheme.primary,
    textoAccion: String? = null,
    onAccion: () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(colorIcono.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = titulo,
            style = tp.titleMedium,
            color = cs.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = detalle,
            style = tp.bodyMedium,
            color = cs.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (textoAccion != null) {
            Spacer(modifier = Modifier.height(20.dp))
            FilledTonalButton(onClick = onAccion) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(textoAccion, style = tp.labelLarge)
            }
        }
    }
}

@Composable
private fun BannerError(mensaje: String, onReintentar: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = cs.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = cs.onErrorContainer,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onReintentar) {
                Text("Reintentar", style = MaterialTheme.typography.labelLarge, color = cs.onErrorContainer)
            }
        }
    }
}