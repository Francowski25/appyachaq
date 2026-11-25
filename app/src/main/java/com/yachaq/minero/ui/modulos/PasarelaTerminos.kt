package com.yachaq.minero.ui.modulos

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

// Valida cada término en su paso y termina en un resumen con los cambios
@Composable
fun PasarelaTerminos(
    titulo: String,
    pasos: List<PasoTermino>,
    guardando: Boolean,
    error: String?,
    onConfirmar: (Map<String, Double>) -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes

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
            shape = formas.extraLarge,
            color = cs.surface,
            border = BorderStroke(1.dp, cs.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column {
                    Text(
                        "AJUSTAR TÉRMINOS",
                        style = tp.labelMedium,
                        color = cs.primary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        titulo,
                        style = tp.titleLarge,
                        color = cs.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                IndicadorPasos(total = pasos.size + 1, actual = paso)

                Crossfade(targetState = paso, label = "paso") { p ->
                    if (p < pasos.size) {
                        val actual = pasos[p]
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Paso ${p + 1} de ${pasos.size}",
                                style = tp.labelMedium,
                                color = cs.onSurfaceVariant
                            )
                            Text(actual.titulo, style = tp.titleLarge, color = cs.onSurface)
                            Text(actual.descripcion, style = tp.bodySmall, color = cs.onSurfaceVariant)
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
                                style = tp.bodySmall,
                                color = cs.onSurfaceVariant
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
                        shape = formas.medium,
                        border = BorderStroke(1.dp, cs.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.onSurface),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text(if (paso == 0) "Cancelar" else "Atrás", style = tp.labelLarge) }

                    Button(
                        onClick = {
                            if (enResumen) {
                                onConfirmar(pasos.associate { it.clave to (textos[it.clave]?.toDoubleOrNull() ?: it.valorActual) })
                            } else siguiente()
                        },
                        enabled = !guardando && (!enResumen || cambios > 0),
                        shape = formas.medium,
                        modifier = Modifier.weight(1.4f).height(48.dp)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = cs.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                when {
                                    !enResumen -> "Siguiente"
                                    cambios == 0 -> "Sin cambios"
                                    else -> "Guardar $cambios cambio${if (cambios == 1) "" else "s"}"
                                },
                                style = tp.labelLarge,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IndicadorPasos(total: Int, actual: Int) {
    val cs = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        for (i in 0 until total) {
            val hecho = i < actual
            val enCurso = i == actual
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .then(
                        if (hecho) Modifier.background(cs.primary, CircleShape)
                        else Modifier.border(
                            1.5.dp,
                            if (enCurso) cs.primary else cs.outlineVariant,
                            CircleShape
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (hecho) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = cs.onPrimary, modifier = Modifier.size(15.dp))
                } else {
                    Text(
                        (i + 1).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (enCurso) cs.primary else cs.onSurfaceVariant
                    )
                }
            }
            if (i < total - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (hecho) cs.primary else cs.outlineVariant)
                )
            }
        }
    }
}

// Tocar una fila vuelve a ese paso
@Composable
private fun ResumenCambios(
    pasos: List<PasoTermino>,
    textos: Map<String, String>,
    onEditar: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Revisa tus cambios", style = tp.titleLarge, color = cs.onSurface)
        Text("Toca un término para corregirlo.", style = tp.bodySmall, color = cs.onSurfaceVariant)
        pasos.forEachIndexed { i, p ->
            val nuevo = textos[p.clave]?.toDoubleOrNull() ?: p.valorActual
            val cambio = nuevo != p.valorActual
            Surface(
                onClick = { onEditar(i) },
                shape = MaterialTheme.shapes.medium,
                color = cs.surfaceContainer,
                border = BorderStroke(1.dp, if (cambio) cs.primary.copy(alpha = 0.6f) else cs.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(p.titulo, style = tp.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                    if (cambio) {
                        Text(
                            "${formato(p.valorActual)}  →  ",
                            style = tp.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                    Text(
                        "${formato(nuevo)} ${p.unidad}",
                        style = tp.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (cambio) cs.primary else cs.onSurface
                    )
                }
            }
        }
    }
}