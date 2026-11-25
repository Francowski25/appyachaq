package com.yachaq.minero.ui.auth.componentes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.R
import kotlinx.coroutines.delay

// Fondo degradado con un resplandor dorado sutil
@Composable
fun AuthFondo(content: @Composable BoxScope.() -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cs.surface, cs.surfaceContainer)))
    ) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 120.dp, y = (-120).dp)
                .background(
                    Brush.radialGradient(listOf(cs.secondary.copy(alpha = 0.18f), Color.Transparent)),
                    CircleShape
                )
        )
        content()
    }
}

// Entrada animada (fade + deslizamiento); el retardo escalona los elementos
@Composable
fun Aparecer(retardoMs: Long = 0, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(retardoMs)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 6 }
    ) {
        content()
    }
}

@Composable
fun AuthLogo(tamano: Dp = 104.dp) {
    val cs = MaterialTheme.colorScheme
    val forma = MaterialTheme.shapes.extraLarge
    Card(
        modifier = Modifier
            .size(tamano)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(listOf(cs.primary, cs.secondary)),
                shape = forma
            ),
        shape = forma,
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "Logo Yachaq Minero",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(8.dp)
            )
        }
    }
}

@Composable
fun TarjetaAuth(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = cs.surfaceContainer,
        border = BorderStroke(1.dp, cs.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun CampoAuth(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    esClave: Boolean = false,
    teclado: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
    error: String? = null,
    enabled: Boolean = true,
    onPerderFoco: () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    var visible by remember { mutableStateOf(false) }
    var tuvoFoco by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(imageVector = icono, contentDescription = null, tint = cs.primary)
        },
        trailingIcon = if (esClave) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = cs.onSurfaceVariant
                    )
                }
            }
        } else null,
        visualTransformation = if (esClave && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (esClave) KeyboardType.Password else teclado,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = cs.surfaceContainerHigh,
            unfocusedContainerColor = cs.surfaceContainerHigh,
            errorContainerColor = cs.surfaceContainerHigh,
            disabledContainerColor = cs.surfaceContainerHigh,
            focusedBorderColor = cs.primary,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = cs.primary,
            unfocusedLabelColor = cs.onSurfaceVariant,
            focusedTextColor = cs.onSurface,
            unfocusedTextColor = cs.onSurface
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { estado ->
                if (estado.isFocused) tuvoFoco = true
                else if (tuvoFoco) onPerderFoco()
            }
    )
}

@Composable
fun BotonAuth(
    texto: String,
    cargando: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val cs = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        enabled = enabled && !cargando,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = cs.primary.copy(alpha = if (cargando) 0.7f else 0.35f),
            disabledContentColor = cs.onPrimary.copy(alpha = 0.8f)
        )
    ) {
        AnimatedContent(targetState = cargando, label = "contenidoBoton") { enCarga ->
            if (enCarga) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = cs.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = texto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

// Mensaje animado de error o de éxito
@Composable
fun MensajeAuth(mensaje: String?, esError: Boolean, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    var ultimo by remember { mutableStateOf(mensaje ?: "") }
    if (mensaje != null) ultimo = mensaje

    AnimatedVisibility(
        visible = mensaje != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val color = if (esError) cs.error else cs.primary
        Column {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (esError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = ultimo,
                        color = color,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}