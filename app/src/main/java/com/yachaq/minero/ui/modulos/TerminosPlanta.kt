package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestTerms
import com.yachaq.minero.data.remote.model.ResponseTerms
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.launch

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
    fun toRequest() = RequestTerms(
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

@Composable
fun TerminosPlanta(
    idPlant: String,
    nombrePlanta: String,
    onDismiss: () -> Unit
) {
    var terminos by remember { mutableStateOf<TerminosUi?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    var recargar by remember { mutableIntStateOf(0) }

    LaunchedEffect(idPlant, recargar) {
        cargando = true
        errorCarga = null
        try {
            val resp = RetrofitClient.termsApi.obtenerTerminos(idPlant)
            val body = resp.body()
            if (resp.isSuccessful && body != null && (body.type == null || body.type == "SUCCESS")) {
                terminos = body.toUi()
            } else {
                errorCarga = body?.listMessage?.firstOrNull() ?: "Error al cargar términos (${resp.code()})"
            }
        } catch (e: Exception) {
            errorCarga = "No se pudo conectar: ${e.message}"
        } finally {
            cargando = false
        }
    }

    // Envía TODOS los términos con los cambios de una sección. Devuelve mensaje de error o null si salió bien.
    suspend fun guardar(nuevo: TerminosUi): String? {
        return try {
            val resp = RetrofitClient.termsApi.actualizarTerminos(idPlant, nuevo.toRequest())
            val body = resp.body()
            if (resp.isSuccessful && body != null && (body.type == null || body.type == "SUCCESS")) {
                terminos = body.toUi()
                null
            } else {
                body?.listMessage?.firstOrNull() ?: "Error del servidor (${resp.code()})"
            }
        } catch (e: Exception) {
            "No se pudo conectar: ${e.message}"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = FondoNegroAbsoluto,
            border = BorderStroke(1.dp, ColorGrisBorde)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val t = terminos
                when {
                    cargando -> CircularProgressIndicator(
                        color = AmarilloMineroBrillante,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    errorCarga != null -> Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(errorCarga.orEmpty(), color = ErrorRojo, fontSize = 13.sp)
                        TextButton(onClick = { recargar++ }) {
                            Text("REINTENTAR", color = AmarilloMineroBrillante)
                        }
                    }

                    t != null -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "CONFIGURACIÓN DE TÉRMINOS",
                            color = AmarilloMineroBrillante,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nombrePlanta,
                            color = TextoBlancoSuave,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Parámetros técnicos de comercialización y liquidación minera.",
                            color = TextoGrisSecundario,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        TarjetaConfiguracionSeccion(
                            titulo = "1. Valores del Concentrado",
                            campos = listOf(
                                "Factor de concentrado" to t.factorConcentrado.toString(),
                                "% H2O (Humedad)" to t.humedad.toString(),
                                "Merma de proceso (%)" to t.merma.toString()
                            ),
                            onGuardar = { v ->
                                val fc = v["Factor de concentrado"]?.toDoubleOrNull()
                                val hu = v["% H2O (Humedad)"]?.toDoubleOrNull()
                                val me = v["Merma de proceso (%)"]?.toDoubleOrNull()
                                if (fc == null || hu == null || me == null) "Por favor ingrese valores numéricos válidos."
                                else guardar(t.copy(factorConcentrado = fc, humedad = hu, merma = me))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TarjetaConfiguracionSeccion(
                            titulo = "2. Leyes del Concentrado",
                            campos = listOf(
                                "Ley Cu concentrado (%)" to t.leyConcentradoCu.toString(),
                                "Ley Ag concentrado (oz/tc)" to t.leyConcentradoAg.toString(),
                                "Ley Au concentrado (oz/tc)" to t.leyConcentradoAu.toString()
                            ),
                            onGuardar = { v ->
                                val lc = v["Ley Cu concentrado (%)"]?.toDoubleOrNull()
                                val la = v["Ley Ag concentrado (oz/tc)"]?.toDoubleOrNull()
                                val lo = v["Ley Au concentrado (oz/tc)"]?.toDoubleOrNull()
                                if (lc == null || la == null || lo == null) "Por favor ingrese valores numéricos válidos."
                                else guardar(t.copy(leyConcentradoCu = lc, leyConcentradoAg = la, leyConcentradoAu = lo))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TarjetaConfiguracionSeccion(
                            titulo = "3. Términos: Cobre (Cu)",
                            campos = listOf(
                                "% Pagable Cu" to t.pagableCu.toString(),
                                "Deducción Cu" to t.deduccionCu.toString(),
                                "Gastos ref Cu (\$/lb)" to t.gastosRefCu.toString(),
                                "Protección Cu" to t.proteccionCu.toString()
                            ),
                            onGuardar = { v ->
                                val p = v["% Pagable Cu"]?.toDoubleOrNull()
                                val d = v["Deducción Cu"]?.toDoubleOrNull()
                                val g = v["Gastos ref Cu (\$/lb)"]?.toDoubleOrNull()
                                val pr = v["Protección Cu"]?.toDoubleOrNull()
                                if (p == null || d == null || g == null || pr == null) "Por favor ingrese valores numéricos válidos."
                                else guardar(t.copy(pagableCu = p, deduccionCu = d, gastosRefCu = g, proteccionCu = pr))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TarjetaConfiguracionSeccion(
                            titulo = "4. Términos: Plata (Ag)",
                            campos = listOf(
                                "% Pagable Ag" to t.pagableAg.toString(),
                                "Deducción Ag (gr)" to t.deduccionAg.toString(),
                                "Gastos ref Ag (\$/oz)" to t.gastosRefAg.toString()
                            ),
                            onGuardar = { v ->
                                val p = v["% Pagable Ag"]?.toDoubleOrNull()
                                val d = v["Deducción Ag (gr)"]?.toDoubleOrNull()
                                val g = v["Gastos ref Ag (\$/oz)"]?.toDoubleOrNull()
                                if (p == null || d == null || g == null) "Por favor ingrese valores numéricos válidos."
                                else guardar(t.copy(pagableAg = p, deduccionAg = d, gastosRefAg = g))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TarjetaConfiguracionSeccion(
                            titulo = "5. Términos: Oro (Au)",
                            campos = listOf(
                                "% Pagable Au" to t.pagableAu.toString(),
                                "Deducción Au (gr)" to t.deduccionAu.toString(),
                                "Gastos ref Au (\$/oz)" to t.gastosRefAu.toString()
                            ),
                            onGuardar = { v ->
                                val p = v["% Pagable Au"]?.toDoubleOrNull()
                                val d = v["Deducción Au (gr)"]?.toDoubleOrNull()
                                val g = v["Gastos ref Au (\$/oz)"]?.toDoubleOrNull()
                                if (p == null || d == null || g == null) "Por favor ingrese valores numéricos válidos."
                                else guardar(t.copy(pagableAu = p, deduccionAu = d, gastosRefAu = g))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TarjetaConfiguracionSeccion(
                            titulo = "6. Maquila",
                            campos = listOf("Maquila (\$/TMNS)" to t.maquila.toString()),
                            onGuardar = { v ->
                                val m = v["Maquila (\$/TMNS)"]?.toDoubleOrNull()
                                if (m == null) "Por favor ingrese valores numéricos válidos."
                                else guardar(t.copy(maquila = m))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TarjetaConfiguracionSeccion(
                            titulo = "7. Servicios Operativos",
                            campos = listOf(
                                "Análisis (\$)" to t.analisis.toString(),
                                "Traslado interno (\$)" to t.trasladoInterno.toString()
                            ),
                            onGuardar = { v ->
                                val a = v["Análisis (\$)"]?.toDoubleOrNull()
                                val tr = v["Traslado interno (\$)"]?.toDoubleOrNull()
                                if (a == null || tr == null) "Por favor ingrese valores numéricos válidos."
                                else guardar(t.copy(analisis = a, trasladoInterno = tr))
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TarjetaConfiguracionSeccion(
                            titulo = "8. Impuestos y Detracciones",
                            campos = listOf(
                                "IGV Porcentaje (0.18 = 18%)" to t.igvPorcentaje.toString(),
                                "Detracción Porcentaje (0.10 = 10%)" to t.detraccionPorcentaje.toString()
                            ),
                            onGuardar = { v ->
                                val igv = v["IGV Porcentaje (0.18 = 18%)"]?.toDoubleOrNull()
                                val det = v["Detracción Porcentaje (0.10 = 10%)"]?.toDoubleOrNull()
                                if (igv == null || det == null) "Por favor ingrese valores numéricos válidos."
                                else guardar(t.copy(igvPorcentaje = igv, detraccionPorcentaje = det))
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextoGrisSecundario)
                }
            }
        }
    }
}

// ========================================================
// TARJETA DE CONFIGURACIÓN CON MODO EDITAR/GUARDAR
// onGuardar devuelve un mensaje de error, o null si se guardó bien
// ========================================================
@Composable
private fun TarjetaConfiguracionSeccion(
    titulo: String,
    campos: List<Pair<String, String>>,
    onGuardar: suspend (Map<String, String>) -> String?
) {
    val scope = rememberCoroutineScope()
    var enModoEdicion by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    val estadoCampos = remember(campos) {
        mutableStateMapOf<String, String>().apply {
            campos.forEach { put(it.first, it.second) }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (enModoEdicion) AmarilloMineroBrillante else ColorGrisBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    color = AmarilloMineroBrillante,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                if (!enModoEdicion) {
                    IconButton(onClick = { enModoEdicion = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar sección", tint = AmarilloMineroBrillante)
                    }
                }
            }

            HorizontalDivider(color = ColorGrisBorde.copy(alpha = 0.5f))

            campos.forEach { (etiqueta, _) ->
                val valorActual = estadoCampos[etiqueta] ?: ""

                if (enModoEdicion) {
                    OutlinedTextField(
                        value = valorActual,
                        onValueChange = {
                            estadoCampos[etiqueta] = it.replace(',', '.')
                            errorMensaje = null
                        },
                        label = { Text(etiqueta) },
                        enabled = !guardando,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GrisCampos,
                            unfocusedContainerColor = GrisCampos,
                            focusedBorderColor = AmarilloMineroBrillante,
                            unfocusedBorderColor = ColorGrisBorde,
                            focusedLabelColor = AmarilloMineroBrillante,
                            unfocusedLabelColor = TextoGrisSecundario,
                            focusedTextColor = TextoBlancoSuave,
                            unfocusedTextColor = TextoBlancoSuave
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = etiqueta, color = TextoGrisSecundario, fontSize = 13.sp)
                        Text(
                            text = valorActual,
                            color = TextoBlancoSuave,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            errorMensaje?.let {
                Text(it, color = ErrorRojo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            if (enModoEdicion) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            campos.forEach { estadoCampos[it.first] = it.second }
                            errorMensaje = null
                            enModoEdicion = false
                        },
                        enabled = !guardando,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoGrisSecundario),
                        border = BorderStroke(1.dp, ColorGrisBorde),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { mostrarConfirmacion = true },
                        enabled = !guardando,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmarilloMineroBrillante,
                            contentColor = FondoNegroAbsoluto
                        ),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = FondoNegroAbsoluto,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Guardar", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = AmarilloMineroBrillante, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Confirmar Modificación", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AmarilloMineroBrillante)
            },
            text = {
                Text(
                    text = "¿Desea aplicar y guardar las modificaciones en la sección '$titulo'?",
                    fontSize = 13.sp,
                    color = TextoGrisSecundario
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacion = false
                        scope.launch {
                            guardando = true
                            val fallo = onGuardar(estadoCampos.toMap())
                            guardando = false
                            if (fallo == null) {
                                errorMensaje = null
                                enModoEdicion = false
                            } else {
                                errorMensaje = fallo
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloMineroBrillante,
                        contentColor = FondoNegroAbsoluto
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sí, Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar", color = TextoGrisSecundario, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}