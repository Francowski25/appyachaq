package com.yachaq.minero.ui.modulos

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yachaq.minero.ui.theme.*
import java.util.Locale

// Un paso = un término editable (maquila, análisis, etc.). El llamador define la lista.
data class PasoTermino(
    val clave: String,
    val titulo: String,
    val descripcion: String,
    val unidad: String,
    val valorActual: Double,
    val minimo: Double = 0.0,
    val maximo: Double? = null
)

private fun formato(v: Double) = String.format(Locale.US, "%.2f", v)

// Devuelve el mensaje de error o null si el valor es válido
private fun PasoTermino.validar(texto: String): String? {
    val v = texto.toDoubleOrNull() ?: return "Ingresa un número válido."
    if (v < minimo) return "Debe ser mayor o igual a ${formato(minimo)}."
    val tope = maximo
    if (tope != null && v > tope) return "No puede superar ${formato(tope)}."
    return null
}

// Pasarela: valida cada término en su paso y termina en un resumen con los cambios
@Composable
fun PasarelaTerminos(
    titulo: String,
    pasos: List<PasoTermino>,
    guardando: Boolean,
    error: String?,
    onConfirmar: (Map<String, Double>) -> Unit,
    onDismiss: () -> Unit
) {
    var paso by remember { mutableIntStateOf(0) }
    var volverAlResumen by remember { mutableStateOf(false) }
    var errorCampo by remember { mutableStateOf<String?>(null) }
    val textos = remember(pasos) {
        mutableStateMapOf<String, String>().apply { pasos.forEach { put(it.clave, formato(it.valorActual)) } }
    }

    val enResumen = paso == pasos.size
    val cambios = pasos.count { (textos[it.clave]?.toDoubleOrNull() ?: it.valorActual) != it.valorActual }

    fun siguiente() {
        val actual = pasos[paso]
        val e = actual.validar(textos[actual.clave].orEmpty())
        if (e != null) {
            errorCampo = e
            return
        }
        errorCampo = null
        paso = if (volverAlResumen) pasos.size else paso + 1
        volverAlResumen = false
    }

    Dialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f),
            shape = RoundedCornerShape(24.dp),
            color = FondoNegroAbsoluto,
            border = BorderStroke(1.dp, ColorGrisBorde)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column {
                    Text(
                        "AJUSTAR TÉRMINOS",
                        color = AmarilloMineroBrillante,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(titulo, color = TextoBlancoSuave, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }

                IndicadorPasos(total = pasos.size + 1, actual = paso)

                Crossfade(targetState = paso, label = "paso") { p ->
                    if (p < pasos.size) {
                        val actual = pasos[p]
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Paso ${p + 1} de ${pasos.size}",
                                color = TextoGrisSecundario,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(actual.titulo, color = TextoBlancoSuave, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(actual.descripcion, color = TextoGrisSecundario, fontSize = 12.sp)
                            CampoNumero(
                                valor = textos[actual.clave].orEmpty(),
                                onCambio = {
                                    textos[actual.clave] = it
                                    errorCampo = null
                                },
                                etiqueta = actual.titulo,
                                unidad = actual.unidad,
                                modifier = Modifier.fillMaxWidth(),
                                ultimo = true
                            )
                            errorCampo?.let { MensajeError(it) }
                            Text(
                                "Valor actual: ${formato(actual.valorActual)} ${actual.unidad}",
                                color = TextoGrisSecundario,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        ResumenCambios(
                            pasos = pasos,
                            textos = textos,
                            onEditar = { i ->
                                volverAlResumen = true
                                paso = i
                            }
                        )
                    }
                }

                error?.let { MensajeError(it) }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            errorCampo = null
                            if (paso == 0) onDismiss() else { volverAlResumen = false; paso-- }
                        },
                        enabled = !guardando,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ColorGrisBorde),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoBlancoSuave),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text(if (paso == 0) "Cancelar" else "Atrás", fontWeight = FontWeight.Bold) }

                    Button(
                        onClick = {
                            if (enResumen) {
                                onConfirmar(pasos.associate { it.clave to (textos[it.clave]?.toDoubleOrNull() ?: it.valorActual) })
                            } else siguiente()
                        },
                        enabled = !guardando && (!enResumen || cambios > 0),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmarilloMineroBrillante,
                            contentColor = FondoNegroAbsoluto
                        ),
                        modifier = Modifier.weight(1.4f).height(48.dp)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = FondoNegroAbsoluto,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                when {
                                    !enResumen -> "Siguiente"
                                    cambios == 0 -> "Sin cambios"
                                    else -> "Guardar $cambios cambio${if (cambios == 1) "" else "s"}"
                                },
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// Círculos conectados: completado (check), actual (resaltado) y pendiente
@Composable
private fun IndicadorPasos(total: Int, actual: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        for (i in 0 until total) {
            val hecho = i < actual
            val enCurso = i == actual
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .then(
                        if (hecho) Modifier.background(AmarilloMineroBrillante, CircleShape)
                        else Modifier.border(
                            1.5.dp,
                            if (enCurso) AmarilloMineroBrillante else ColorGrisBorde,
                            CircleShape
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (hecho) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = FondoNegroAbsoluto, modifier = Modifier.size(15.dp))
                } else {
                    Text(
                        (i + 1).toString(),
                        color = if (enCurso) AmarilloMineroBrillante else TextoGrisSecundario,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (i < total - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (hecho) AmarilloMineroBrillante else ColorGrisBorde)
                )
            }
        }
    }
}

// Último paso: cada término con su valor anterior y nuevo; tocar una fila vuelve a ese paso
@Composable
private fun ResumenCambios(
    pasos: List<PasoTermino>,
    textos: Map<String, String>,
    onEditar: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Revisa tus cambios", color = TextoBlancoSuave, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Toca un término para corregirlo.", color = TextoGrisSecundario, fontSize = 12.sp)
        pasos.forEachIndexed { i, p ->
            val nuevo = textos[p.clave]?.toDoubleOrNull() ?: p.valorActual
            val cambio = nuevo != p.valorActual
            Surface(
                onClick = { onEditar(i) },
                shape = RoundedCornerShape(12.dp),
                color = FondoNegroSecundario,
                border = BorderStroke(1.dp, if (cambio) AmarilloMineroBrillante.copy(alpha = 0.6f) else ColorGrisBorde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(p.titulo, color = TextoGrisSecundario, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    if (cambio) {
                        Text(
                            "${formato(p.valorActual)}  →  ",
                            color = TextoGrisSecundario,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        "${formato(nuevo)} ${p.unidad}",
                        color = if (cambio) AmarilloMineroBrillante else TextoBlancoSuave,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}