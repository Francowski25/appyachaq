package com.yachaq.minero.ui.auth.componentes

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yachaq.minero.ui.theme.AdvertenciaNaranja
import com.yachaq.minero.ui.theme.ExitoVerde

data class RequisitoClave(val texto: String, val cumple: Boolean)

fun requisitosClave(clave: String) = listOf(
    RequisitoClave("Mínimo 8 caracteres", clave.length >= 8),
    RequisitoClave("Mayúsculas y minúsculas", clave.any { it.isUpperCase() } && clave.any { it.isLowerCase() }),
    RequisitoClave("Al menos un número", clave.any { it.isDigit() }),
    RequisitoClave("Un símbolo (por ejemplo @ # $ !)", clave.any { !it.isLetterOrDigit() })
)

// Es una guía visual, no bloquea nada por sí mismo
@Composable
fun FortalezaClave(clave: String, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val requisitos = requisitosClave(clave)
    val puntos = requisitos.count { it.cumple }

    val (etiqueta, color) = when {
        clave.isEmpty() -> "Escribe una contraseña" to cs.onSurfaceVariant
        puntos <= 1 -> "Débil" to cs.error
        puntos == 2 -> "Aceptable" to AdvertenciaNaranja
        puntos == 3 -> "Buena" to cs.primary
        else -> "Fuerte" to ExitoVerde
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { i ->
                val colorBarra by animateColorAsState(
                    targetValue = if (clave.isNotEmpty() && i < puntos.coerceAtLeast(1)) color
                    else cs.outlineVariant,
                    label = "barra$i"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colorBarra)
                )
            }
        }

        Text(
            text = etiqueta,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        requisitos.forEach { requisito ->
            val colorRequisito by animateColorAsState(
                targetValue = if (requisito.cumple) ExitoVerde
                else cs.onSurfaceVariant.copy(alpha = 0.7f),
                label = "requisito"
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (requisito.cumple) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = colorRequisito,
                    modifier = Modifier.size(16.dp)
                )
                Text(text = requisito.texto, color = colorRequisito, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}