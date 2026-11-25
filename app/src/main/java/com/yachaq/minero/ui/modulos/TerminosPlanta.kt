package com.yachaq.minero.ui.modulos

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestTerms
import com.yachaq.minero.data.remote.model.RequestUserTerms
import com.yachaq.minero.data.remote.model.ResponseTerms
import com.yachaq.minero.data.remote.model.ResponseUserTerms
import com.yachaq.minero.ui.auth.componentes.Aparecer
import com.yachaq.minero.ui.auth.componentes.AuthFondo
import com.yachaq.minero.ui.auth.componentes.BotonAuth
import kotlinx.coroutines.launch
import java.math.BigDecimal

private data class TerminosUi(
    val factorConcentrado: Double = 0.0,
    val humedad: Double = 0.0,
    val merma: Double = 0.0,
    val leyConcentradoCu: Double = 0.0,
    val leyConcentradoAg: Double = 0.0,
    val leyConcentradoAu: Double = 0.0,
    val pagableCu: Double = 0.0,
    val deduccionCu: Double = 0.0,
    val gastosRefCu: Double = 0.0,
    val proteccionCu: Double = 0.0,
    val pagableAg: Double = 0.0,
    val deduccionAg: Double = 0.0,
    val gastosRefAg: Double = 0.0,
    val pagableAu: Double = 0.0,
    val deduccionAu: Double = 0.0,
    val gastosRefAu: Double = 0.0,
    val maquila: Double = 0.0,
    val analisis: Double = 0.0,
    val trasladoInterno: Double = 0.0,
    val igvPorcentaje: Double = 0.0,
    val detraccionPorcentaje: Double = 0.0
) {
    fun toRequestTerms() = RequestTerms(
        factorConcentrado = factorConcentrado, humedad = humedad, merma = merma,
        leyConcentradoCu = leyConcentradoCu, leyConcentradoAg = leyConcentradoAg, leyConcentradoAu = leyConcentradoAu,
        pagableCu = pagableCu, deduccionCu = deduccionCu, gastosRefCu = gastosRefCu, proteccionCu = proteccionCu,
        pagableAg = pagableAg, deduccionAg = deduccionAg, gastosRefAg = gastosRefAg,
        pagableAu = pagableAu, deduccionAu = deduccionAu, gastosRefAu = gastosRefAu,
        maquila = maquila, analisis = analisis, trasladoInterno = trasladoInterno,
        igvPorcentaje = igvPorcentaje, detraccionPorcentaje = detraccionPorcentaje
    )

    fun toRequestUserTerms(idUser: String, idPlant: String) = RequestUserTerms(
        idUser = idUser, idPlant = idPlant,
        factorConcentrado = factorConcentrado, humedad = humedad, merma = merma,
        leyConcentradoCu = leyConcentradoCu, leyConcentradoAg = leyConcentradoAg, leyConcentradoAu = leyConcentradoAu,
        pagableCu = pagableCu, deduccionCu = deduccionCu, gastosRefCu = gastosRefCu, proteccionCu = proteccionCu,
        pagableAg = pagableAg, deduccionAg = deduccionAg, gastosRefAg = gastosRefAg,
        pagableAu = pagableAu, deduccionAu = deduccionAu, gastosRefAu = gastosRefAu,
        maquila = maquila, analisis = analisis, trasladoInterno = trasladoInterno,
        igvPorcentaje = igvPorcentaje, detraccionPorcentaje = detraccionPorcentaje
    )
}

private fun ResponseTerms.toUi() = TerminosUi(
    factorConcentrado = factorConcentrado ?: 0.0, humedad = humedad ?: 0.0, merma = merma ?: 0.0,
    leyConcentradoCu = leyConcentradoCu ?: 0.0, leyConcentradoAg = leyConcentradoAg ?: 0.0, leyConcentradoAu = leyConcentradoAu ?: 0.0,
    pagableCu = pagableCu ?: 0.0, deduccionCu = deduccionCu ?: 0.0, gastosRefCu = gastosRefCu ?: 0.0, proteccionCu = proteccionCu ?: 0.0,
    pagableAg = pagableAg ?: 0.0, deduccionAg = deduccionAg ?: 0.0, gastosRefAg = gastosRefAg ?: 0.0,
    pagableAu = pagableAu ?: 0.0, deduccionAu = deduccionAu ?: 0.0, gastosRefAu = gastosRefAu ?: 0.0,
    maquila = maquila ?: 0.0, analisis = analisis ?: 0.0, trasladoInterno = trasladoInterno ?: 0.0,
    igvPorcentaje = igvPorcentaje ?: 0.0, detraccionPorcentaje = detraccionPorcentaje ?: 0.0
)

private fun ResponseUserTerms.toUi() = TerminosUi(
    factorConcentrado = factorConcentrado ?: 0.0, humedad = humedad ?: 0.0, merma = merma ?: 0.0,
    leyConcentradoCu = leyConcentradoCu ?: 0.0, leyConcentradoAg = leyConcentradoAg ?: 0.0, leyConcentradoAu = leyConcentradoAu ?: 0.0,
    pagableCu = pagableCu ?: 0.0, deduccionCu = deduccionCu ?: 0.0, gastosRefCu = gastosRefCu ?: 0.0, proteccionCu = proteccionCu ?: 0.0,
    pagableAg = pagableAg ?: 0.0, deduccionAg = deduccionAg ?: 0.0, gastosRefAg = gastosRefAg ?: 0.0,
    pagableAu = pagableAu ?: 0.0, deduccionAu = deduccionAu ?: 0.0, gastosRefAu = gastosRefAu ?: 0.0,
    maquila = maquila ?: 0.0, analisis = analisis ?: 0.0, trasladoInterno = trasladoInterno ?: 0.0,
    igvPorcentaje = igvPorcentaje ?: 0.0, detraccionPorcentaje = detraccionPorcentaje ?: 0.0
)

// Cada campo sabe leerse y escribirse en TerminosUi
private class CampoDef(
    val etiqueta: String,
    val unidad: String = "",
    val ayuda: String? = null,
    val maximo: Double? = null,
    val leer: (TerminosUi) -> Double,
    val escribir: (TerminosUi, Double) -> TerminosUi
)

private class BloqueDef(
    val corto: String,
    val titulo: String,
    val descripcion: String,
    val campos: List<CampoDef>
)

private val BLOQUES = listOf(
    BloqueDef(
        "Concentrado", "Valores del concentrado", "Factor, humedad y merma del concentrado.",
        listOf(
            CampoDef("Factor de concentrado", leer = { it.factorConcentrado }, escribir = { t, v -> t.copy(factorConcentrado = v) }),
            CampoDef("Humedad (H2O)", "%", leer = { it.humedad }, escribir = { t, v -> t.copy(humedad = v) }),
            CampoDef("Merma de proceso", "%", leer = { it.merma }, escribir = { t, v -> t.copy(merma = v) })
        )
    ),
    BloqueDef(
        "Leyes", "Leyes del concentrado", "Ley de cobre, plata y oro del concentrado.",
        listOf(
            CampoDef("Ley de Cu", "%", leer = { it.leyConcentradoCu }, escribir = { t, v -> t.copy(leyConcentradoCu = v) }),
            CampoDef("Ley de Ag", "oz/tc", leer = { it.leyConcentradoAg }, escribir = { t, v -> t.copy(leyConcentradoAg = v) }),
            CampoDef("Ley de Au", "oz/tc", leer = { it.leyConcentradoAu }, escribir = { t, v -> t.copy(leyConcentradoAu = v) })
        )
    ),
    BloqueDef(
        "Cobre", "Términos: Cobre (Cu)", "Pagable, deducción, gastos de refinación y protección del cobre.",
        listOf(
            CampoDef("Pagable Cu", "%", leer = { it.pagableCu }, escribir = { t, v -> t.copy(pagableCu = v) }),
            CampoDef("Deducción Cu", leer = { it.deduccionCu }, escribir = { t, v -> t.copy(deduccionCu = v) }),
            CampoDef("Gastos de refinación Cu", "\$/lb", leer = { it.gastosRefCu }, escribir = { t, v -> t.copy(gastosRefCu = v) }),
            CampoDef("Protección Cu", leer = { it.proteccionCu }, escribir = { t, v -> t.copy(proteccionCu = v) })
        )
    ),
    BloqueDef(
        "Plata", "Términos: Plata (Ag)", "Pagable, deducción y gastos de refinación de la plata.",
        listOf(
            CampoDef("Pagable Ag", "%", leer = { it.pagableAg }, escribir = { t, v -> t.copy(pagableAg = v) }),
            CampoDef("Deducción Ag", "gr", leer = { it.deduccionAg }, escribir = { t, v -> t.copy(deduccionAg = v) }),
            CampoDef("Gastos de refinación Ag", "\$/oz", leer = { it.gastosRefAg }, escribir = { t, v -> t.copy(gastosRefAg = v) })
        )
    ),
    BloqueDef(
        "Oro", "Términos: Oro (Au)", "Pagable, deducción y gastos de refinación del oro.",
        listOf(
            CampoDef("Pagable Au", "%", leer = { it.pagableAu }, escribir = { t, v -> t.copy(pagableAu = v) }),
            CampoDef("Deducción Au", "gr", leer = { it.deduccionAu }, escribir = { t, v -> t.copy(deduccionAu = v) }),
            CampoDef("Gastos de refinación Au", "\$/oz", leer = { it.gastosRefAu }, escribir = { t, v -> t.copy(gastosRefAu = v) })
        )
    ),
    BloqueDef(
        "Maquila", "Maquila", "Costo de maquila por tonelada métrica neta seca.",
        listOf(
            CampoDef("Maquila", "\$/TMNS", leer = { it.maquila }, escribir = { t, v -> t.copy(maquila = v) })
        )
    ),
    BloqueDef(
        "Servicios", "Servicios operativos", "Análisis y traslado interno.",
        listOf(
            CampoDef("Análisis", "\$", leer = { it.analisis }, escribir = { t, v -> t.copy(analisis = v) }),
            CampoDef("Traslado interno", "\$", leer = { it.trasladoInterno }, escribir = { t, v -> t.copy(trasladoInterno = v) })
        )
    ),
    BloqueDef(
        "Impuestos", "Impuestos y detracciones", "IGV y detracción, expresados como fracción.",
        listOf(
            CampoDef(
                "IGV", ayuda = "Fracción: 0.18 equivale a 18 %", maximo = 1.0,
                leer = { it.igvPorcentaje }, escribir = { t, v -> t.copy(igvPorcentaje = v) }
            ),
            CampoDef(
                "Detracción", ayuda = "Fracción: 0.10 equivale a 10 %", maximo = 1.0,
                leer = { it.detraccionPorcentaje }, escribir = { t, v -> t.copy(detraccionPorcentaje = v) }
            )
        )
    )
)

// Muestra 85.0 como "85" y 0.18 como "0.18"
private fun num(v: Double): String = BigDecimal.valueOf(v).stripTrailingZeros().toPlainString()

// Mensaje de error o null si el valor es válido
private fun CampoDef.error(texto: String): String? {
    val v = texto.toDoubleOrNull() ?: return "Ingresa un número válido."
    if (v < 0) return "No puede ser negativo."
    val tope = maximo
    if (tope != null && v > tope) return "No puede superar ${num(tope)}."
    return null
}

// Planta del sistema: términos globales (admin) o del minero (favoritas)
@Composable
fun TerminosPlanta(
    idPlant: String,
    nombrePlanta: String,
    idUser: String? = null,
    esAdmin: Boolean = true,
    onDismiss: () -> Unit
) {
    TerminosFormulario(
        clave = idPlant,
        titulo = if (esAdmin) "TÉRMINOS GLOBALES DE LA PLANTA" else "MIS TÉRMINOS PERSONALES",
        nombrePlanta = nombrePlanta,
        cargar = {
            try {
                if (esAdmin) {
                    val resp = RetrofitClient.termsApi.obtenerTerminos(idPlant)
                    val body = resp.body()
                    if (resp.isSuccessful && body != null && (body.type == null || body.type == "SUCCESS")) {
                        body.toUi() to null
                    } else {
                        null to (body?.listMessage?.firstOrNull() ?: "Error al cargar términos (${resp.code()})")
                    }
                } else if (idUser == null) {
                    null to "Error: ID de usuario no proporcionado."
                } else {
                    val resp = RetrofitClient.userTermsApi.listar(idUser)
                    if (resp.isSuccessful) {
                        val termino = resp.body().orEmpty().find { it.idPlant == idPlant }
                        if (termino != null) termino.toUi() to null
                        else null to "No se encontraron términos para esta planta."
                    } else {
                        null to "Error al cargar términos del usuario (${resp.code()})"
                    }
                }
            } catch (e: Exception) {
                null to "No se pudo conectar: ${e.message}"
            }
        },
        guardar = { nuevo ->
            try {
                if (esAdmin) {
                    val resp = RetrofitClient.termsApi.actualizarTerminos(idPlant, nuevo.toRequestTerms())
                    val body = resp.body()
                    if (resp.isSuccessful && body != null && (body.type == null || body.type == "SUCCESS")) null
                    else body?.listMessage?.firstOrNull() ?: "Error del servidor (${resp.code()})"
                } else if (idUser == null) {
                    "Error: ID de usuario no proporcionado."
                } else {
                    val resp = RetrofitClient.userTermsApi.guardar(nuevo.toRequestUserTerms(idUser, idPlant))
                    val body = resp.body()
                    if (resp.isSuccessful && (body?.type == null || body.type == "SUCCESS")) null
                    else body?.listMessage?.firstOrNull() ?: "Error al guardar (${resp.code()})"
                }
            } catch (e: Exception) {
                "No se pudo conectar: ${e.message}"
            }
        },
        onDismiss = onDismiss
    )
}

// Términos propios de una planta personal del minero
@Composable
fun TerminosPlantaPersonal(
    idUserPlant: String,
    nombrePlanta: String,
    onDismiss: () -> Unit
) {
    TerminosFormulario(
        clave = idUserPlant,
        titulo = "TÉRMINOS DE MI PLANTA",
        nombrePlanta = nombrePlanta,
        cargar = {
            try {
                val resp = RetrofitClient.userPlantApi.obtenerTerminos(idUserPlant)
                val body = resp.body()
                if (resp.isSuccessful && body != null && (body.type == null || body.type == "SUCCESS")) {
                    body.toUi() to null
                } else {
                    null to (body?.listMessage?.firstOrNull() ?: "Error al cargar términos (${resp.code()})")
                }
            } catch (e: Exception) {
                null to "No se pudo conectar: ${e.message}"
            }
        },
        guardar = { nuevo ->
            try {
                val resp = RetrofitClient.userPlantApi.actualizarTerminos(idUserPlant, nuevo.toRequestTerms())
                val body = resp.body()
                if (resp.isSuccessful && body != null && (body.type == null || body.type == "SUCCESS")) null
                else body?.listMessage?.firstOrNull() ?: "Error al guardar (${resp.code()})"
            } catch (e: Exception) {
                "No se pudo conectar: ${e.message}"
            }
        },
        onDismiss = onDismiss
    )
}

// Contenedor común: no sabe de dónde vienen los datos.
// cargar devuelve (términos, error); guardar devuelve el mensaje de error o null si salió bien.
@Composable
private fun TerminosFormulario(
    clave: String,
    titulo: String,
    nombrePlanta: String,
    cargar: suspend () -> Pair<TerminosUi?, String?>,
    guardar: suspend (TerminosUi) -> String?,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var terminos by remember { mutableStateOf<TerminosUi?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    var recargar by remember { mutableIntStateOf(0) }

    LaunchedEffect(clave, recargar) {
        cargando = true
        errorCarga = null
        val (resultado, fallo) = cargar()
        if (resultado != null) terminos = resultado else errorCarga = fallo ?: "No se pudieron cargar los términos."
        cargando = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = cs.surface,
            border = BorderStroke(1.dp, cs.outlineVariant)
        ) {
            AuthFondo {
                val t = terminos
                when {
                    cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = cs.primary)
                    }

                    errorCarga != null -> Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        MensajeError(errorCarga.orEmpty())
                        Spacer(Modifier.height(16.dp))
                        BotonAuth(texto = "REINTENTAR", cargando = false, onClick = { recargar++ })
                        TextButton(onClick = onDismiss) {
                            Text("CERRAR", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                        }
                    }

                    t != null -> AsistenteTerminos(
                        titulo = titulo,
                        nombrePlanta = nombrePlanta,
                        inicial = t,
                        guardar = guardar,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

// Un bloque por paso; cada bloque se verifica y se guarda antes de avanzar
@Composable
private fun AsistenteTerminos(
    titulo: String,
    nombrePlanta: String,
    inicial: TerminosUi,
    guardar: suspend (TerminosUi) -> String?,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes

    // Lo último guardado en el servidor
    var actual by remember { mutableStateOf(inicial) }
    // BLOQUES.size es el resumen final
    var paso by remember { mutableIntStateOf(0) }
    var guardando by remember { mutableStateOf(false) }
    var errorBloque by remember { mutableStateOf<String?>(null) }
    val guardados = remember { mutableStateListOf<Int>() }
    val textos = remember {
        mutableStateMapOf<String, String>().apply {
            BLOQUES.forEach { b -> b.campos.forEach { put(it.etiqueta, num(it.leer(inicial))) } }
        }
    }

    val total = BLOQUES.size
    val enResumen = paso == total

    fun cambiado(c: CampoDef): Boolean {
        val v = textos[c.etiqueta]?.toDoubleOrNull() ?: return false
        return v != c.leer(actual)
    }

    fun irA(i: Int) {
        if (!guardando) {
            errorBloque = null
            paso = i
        }
    }

    fun continuar() {
        val indice = paso
        val bloque = BLOQUES[indice]
        if (bloque.campos.any { it.error(textos[it.etiqueta].orEmpty()) != null }) return
        if (bloque.campos.none { cambiado(it) }) {
            errorBloque = null
            paso = indice + 1
            return
        }
        scope.launch {
            guardando = true
            errorBloque = null
            var nuevo = actual
            bloque.campos.forEach { c -> nuevo = c.escribir(nuevo, textos.getValue(c.etiqueta).toDouble()) }
            val fallo = guardar(nuevo)
            guardando = false
            if (fallo == null) {
                actual = nuevo
                if (indice !in guardados) guardados.add(indice)
                bloque.campos.forEach { textos[it.etiqueta] = num(it.leer(nuevo)) }
                paso = indice + 1
            } else {
                errorBloque = fallo
            }
        }
    }

    val bloqueActual = BLOQUES.getOrNull(paso)
    val bloqueValido = bloqueActual?.campos?.all { it.error(textos[it.etiqueta].orEmpty()) == null } ?: true
    val bloqueConCambios = bloqueActual?.campos?.any { cambiado(it) } ?: false

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = tp.labelMedium,
                    color = cs.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    nombrePlanta,
                    style = tp.titleLarge,
                    color = cs.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            IconButton(onClick = onDismiss, enabled = !guardando) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = cs.onSurfaceVariant)
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LinearProgressIndicator(
                progress = { paso / total.toFloat() },
                color = cs.primary,
                trackColor = cs.outlineVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BLOQUES.forEachIndexed { i, b ->
                    ChipBloque(
                        numero = i + 1,
                        texto = b.corto,
                        actual = i == paso,
                        hecho = i in guardados,
                        onClick = { irA(i) }
                    )
                }
                ChipBloque(
                    numero = total + 1,
                    texto = "Resumen",
                    actual = enResumen,
                    hecho = false,
                    onClick = { irA(total) }
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Crossfade(targetState = paso, label = "bloque") { p ->
                if (p < total) {
                    val b = BLOQUES[p]
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "BLOQUE ${p + 1} DE $total",
                            style = tp.labelMedium,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        TarjetaPaso(numero = p + 1, titulo = b.titulo.uppercase()) {
                            Text(b.descripcion, style = tp.bodySmall, color = cs.onSurfaceVariant)
                            b.campos.forEachIndexed { i, c ->
                                val texto = textos[c.etiqueta].orEmpty()
                                val err = c.error(texto)
                                CampoNumero(
                                    valor = texto,
                                    onCambio = {
                                        textos[c.etiqueta] = it
                                        errorBloque = null
                                    },
                                    etiqueta = c.etiqueta,
                                    unidad = c.unidad,
                                    modifier = Modifier.fillMaxWidth(),
                                    ultimo = i == b.campos.lastIndex,
                                    error = err,
                                    ayuda = when {
                                        cambiado(c) -> "Modificado · antes: ${num(c.leer(actual))}"
                                        else -> c.ayuda
                                    }
                                )
                            }
                        }
                    }
                } else {
                    ResumenFinal(actual = actual, guardados = guardados, onIrA = { irA(it) })
                }
            }

            errorBloque?.let { MensajeError(it) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { if (paso == 0) onDismiss() else irA(paso - 1) },
                enabled = !guardando,
                shape = formas.medium,
                border = BorderStroke(1.dp, cs.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.onSurface),
                modifier = Modifier.weight(1f).height(54.dp)
            ) {
                Text(
                    if (paso == 0) "CERRAR" else "ATRÁS",
                    style = tp.labelLarge,
                    letterSpacing = 1.sp
                )
            }

            BotonAuth(
                texto = when {
                    enResumen -> "FINALIZAR"
                    bloqueConCambios -> "GUARDAR"
                    else -> "CONTINUAR"
                },
                cargando = guardando,
                onClick = { if (enResumen) onDismiss() else continuar() },
                enabled = bloqueValido,
                modifier = Modifier.weight(1.6f)
            )
        }
    }
}

@Composable
private fun Insignia(numero: Int, tamano: Dp = 24.dp) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(tamano)
            .background(Brush.linearGradient(listOf(cs.primary, cs.secondary)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            numero.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = cs.onPrimary,
            fontWeight = FontWeight.Black
        )
    }
}

// Check si ya se guardó; degradado dorado si es el actual
@Composable
private fun ChipBloque(numero: Int, texto: String, actual: Boolean, hecho: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val forma = MaterialTheme.shapes.large
    val fondo = if (actual) Brush.linearGradient(listOf(cs.primary, cs.secondary)) else SolidColor(cs.surfaceContainerHigh)
    Row(
        modifier = Modifier
            .clip(forma)
            .background(fondo)
            .border(1.dp, if (actual || hecho) cs.primary else Color.Transparent, forma)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(
                    if (actual) cs.onPrimary else if (hecho) cs.primary else cs.outlineVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hecho && !actual) {
                Icon(Icons.Default.Check, null, tint = cs.onPrimary, modifier = Modifier.size(12.dp))
            } else {
                Text(
                    numero.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = if (actual) cs.primary else cs.onSurface
                )
            }
        }
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            color = if (actual) cs.onPrimary else cs.onSurface
        )
    }
}

// Tocar un bloque lo abre para editarlo
@Composable
private fun ResumenFinal(actual: TerminosUi, guardados: List<Int>, onIrA: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text("RESUMEN", style = tp.labelMedium, color = cs.onSurfaceVariant)
            Text(
                if (guardados.isEmpty()) "No hiciste cambios en esta sesión."
                else "${guardados.size} bloque${if (guardados.size == 1) "" else "s"} actualizado${if (guardados.size == 1) "" else "s"}.",
                style = tp.titleMedium,
                color = cs.onSurface
            )
        }
        BLOQUES.forEachIndexed { i, b ->
            Aparecer(retardoMs = i * 70L) {
                Surface(
                    onClick = { onIrA(i) },
                    shape = MaterialTheme.shapes.large,
                    color = cs.surfaceContainer,
                    border = BorderStroke(
                        1.dp,
                        if (i in guardados) cs.primary.copy(alpha = 0.6f) else cs.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Insignia(i + 1)
                            Text(
                                b.titulo,
                                style = tp.titleSmall,
                                color = cs.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (i in guardados) {
                                Text("ACTUALIZADO", style = tp.labelSmall, color = cs.primary)
                            }
                        }
                        b.campos.forEach { c ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(c.etiqueta, style = tp.bodySmall, color = cs.onSurfaceVariant)
                                Text(
                                    "${num(c.leer(actual))} ${c.unidad}".trim(),
                                    style = tp.bodySmall,
                                    color = cs.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}