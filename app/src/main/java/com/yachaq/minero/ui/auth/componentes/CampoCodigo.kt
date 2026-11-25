package com.yachaq.minero.ui.auth.componentes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// Un solo campo invisible + 6 cuadros: soporta pegar, autocompletar y borrar con el teclado
@Composable
fun CampoCodigo(
    codigo: String,
    onCodigoChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    longitud: Int = 6,
    esError: Boolean = false,
    enabled: Boolean = true,
    sacudir: Int = 0,               // súbelo en 1 para animar un "sacudón" de error
    onCompleto: (String) -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    var enfocado by remember { mutableStateOf(false) }
    val desplazamiento = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(150)
        runCatching { focusRequester.requestFocus() }
    }

    LaunchedEffect(sacudir) {
        if (sacudir > 0) {
            repeat(3) {
                desplazamiento.animateTo(12f, tween(45))
                desplazamiento.animateTo(-12f, tween(45))
            }
            desplazamiento.animateTo(0f, tween(45))
        }
    }

    BasicTextField(
        value = codigo,
        onValueChange = { nuevo ->
            val limpio = nuevo.filter(Char::isDigit).take(longitud)
            if (limpio != codigo) {
                onCodigoChange(limpio)
                if (limpio.length == longitud) onCompleto(limpio)
            }
        },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            if (codigo.length == longitud) onCompleto(codigo)
        }),
        modifier = modifier
            .offset { IntOffset(desplazamiento.value.roundToInt(), 0) }
            .focusRequester(focusRequester)
            .onFocusChanged { enfocado = it.isFocused },
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(longitud) { i ->
                    CajaCodigo(
                        caracter = codigo.getOrNull(i)?.toString() ?: "",
                        activa = enfocado && enabled && i == codigo.length.coerceAtMost(longitud - 1),
                        esError = esError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}

@Composable
private fun CajaCodigo(
    caracter: String,
    activa: Boolean,
    esError: Boolean,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val forma = MaterialTheme.shapes.medium
    val colorBorde by animateColorAsState(
        targetValue = when {
            esError -> cs.error
            activa -> cs.primary
            caracter.isNotEmpty() -> cs.primary.copy(alpha = 0.6f)
            else -> cs.outlineVariant
        },
        label = "bordeCaja"
    )
    val escala by animateFloatAsState(if (activa) 1.06f else 1f, label = "escalaCaja")

    Box(
        modifier = modifier
            .height(58.dp)
            .scale(escala)
            .background(cs.surfaceContainerHigh, forma)
            .border(if (activa) 2.dp else 1.dp, colorBorde, forma),
        contentAlignment = Alignment.Center
    ) {
        if (caracter.isNotEmpty()) {
            Text(
                text = caracter,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = cs.onSurface
            )
        } else if (activa) {
            val transicion = rememberInfiniteTransition(label = "cursor")
            val alfa by transicion.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                label = "alfaCursor"
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .background(cs.primary.copy(alpha = alfa))
            )
        }
    }
}