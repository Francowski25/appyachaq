package com.yachaq.minero.ui.modulos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.ui.auth.componentes.TarjetaAuth
import com.yachaq.minero.ui.theme.*

@Composable
fun TarjetaPaso(
    numero: Int,
    titulo: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    TarjetaAuth(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            Brush.linearGradient(listOf(AmarilloMineroBrillante, AmarilloMinero)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = numero.toString(),
                        color = FondoNegroAbsoluto,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = titulo,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            content()
        }
    }
}

// Campo decimal con el estilo de CampoAuth: fondo gris, borde solo al enfocar,
// ícono opcional a la izquierda y unidad al final. Solo dígitos y un punto (acepta coma).
// "icono" es opcional: si no lo pasas, los llamados actuales funcionan igual.
@Composable
fun CampoNumero(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    unidad: String,
    modifier: Modifier = Modifier,
    ultimo: Boolean = false,
    error: String? = null,
    ayuda: String? = null,
    icono: ImageVector? = null
) {
    val mensaje = error ?: ayuda
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevo ->
            val limpio = nuevo.replace(',', '.')
            if (limpio.all { it.isDigit() || it == '.' } && limpio.count { it == '.' } <= 1) {
                onCambio(limpio)
            }
        },
        label = { Text(etiqueta) },
        leadingIcon = if (icono != null) {
            { Icon(imageVector = icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        } else null,
        suffix = {
            Text(unidad, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        },
        isError = error != null,
        supportingText = if (mensaje != null) {
            { Text(mensaje) }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = if (ultimo) ImeAction.Done else ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = GrisCampos,
            unfocusedContainerColor = GrisCampos,
            errorContainerColor = GrisCampos,
            disabledContainerColor = GrisCampos,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    )
}

// Mensaje de error con el mismo look de MensajeAuth, con entrada animada
@Composable
fun MensajeError(texto: String, modifier: Modifier = Modifier) {
    val estado = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = estado,
        enter = fadeIn() + expandVertically(),
        modifier = modifier.fillMaxWidth()
    ) {
        val color = MaterialTheme.colorScheme.error
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = texto,
                    color = color,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}