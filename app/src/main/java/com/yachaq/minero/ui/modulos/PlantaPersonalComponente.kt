package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.yachaq.minero.data.remote.model.RequestUserPlant
import com.yachaq.minero.data.remote.model.ResponseUserPlant
import com.yachaq.minero.ui.theme.*

@Composable
fun DialogoPlantaPersonal(
    planta: ResponseUserPlant?,
    guardando: Boolean,
    error: String?,
    onGuardar: (RequestUserPlant) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember(planta) { mutableStateOf(planta?.name.orEmpty()) }
    var ubicacion by remember(planta) { mutableStateOf(planta?.location.orEmpty()) }
    var capacidad by remember(planta) {
        mutableStateOf(planta?.capacityTMAD?.toString().orEmpty())
    }
    var costoAlquiler by remember(planta) {
        mutableStateOf(planta?.rentalCostPerTM?.toString().orEmpty())
    }
    var estado by remember(planta) { mutableStateOf(planta?.status ?: "Operativa") }
    var errorValidacion by remember(planta) { mutableStateOf<String?>(null) }

    val esEdicion = planta != null
    val mensajeError = errorValidacion ?: error

    fun guardar() {
        if (guardando) return

        val capacidadNumero = capacidad.trim().replace(',', '.').toDoubleOrNull()
        val costoNumero = costoAlquiler.trim().replace(',', '.').toDoubleOrNull()

        when {
            nombre.isBlank() || ubicacion.isBlank() ||
                    capacidadNumero == null || costoNumero == null ||
                    !capacidadNumero.isFinite() || !costoNumero.isFinite() -> {
                errorValidacion = "Completa todos los campos con valores válidos."
                return
            }

            capacidadNumero <= 0 || costoNumero < 0 -> {
                errorValidacion =
                    "La capacidad debe ser mayor que cero y el costo no puede ser negativo."
                return
            }
        }

        errorValidacion = null
        onGuardar(
            RequestUserPlant(
                name = nombre.trim(),
                location = ubicacion.trim(),
                capacityTMAD = capacidadNumero,
                rentalCostPerTM = costoNumero,
                status = estado
            )
        )
    }

    Dialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !guardando,
            dismissOnClickOutside = !guardando
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 520.dp)
                .heightIn(max = 780.dp),
            shape = RoundedCornerShape(28.dp),
            color = FondoNegroSecundario,
            border = BorderStroke(1.dp, ColorGrisBorde),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Encabezado
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = AmarilloMineroBrillante.copy(alpha = 0.13f)
                    ) {
                        Text(
                            text = "GESTIÓN DE PLANTAS",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = AmarilloMineroBrillante,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                    }

                    Text(
                        text = if (esEdicion) "Editar planta" else "Nueva planta personal",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoSuave
                    )

                    Text(
                        text = if (esEdicion)
                            "Actualiza la información y las condiciones de operación."
                        else
                            "Registra una planta para utilizarla en tus cálculos mineros.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoGrisSecundario,
                        lineHeight = 19.sp
                    )
                }

                HorizontalDivider(color = ColorGrisBorde.copy(alpha = 0.8f))

                // Datos generales
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TituloSeccionPlanta(
                        titulo = "Información general",
                        descripcion = "Identificación y ubicación de la planta"
                    )

                    CampoPlantaPersonal(
                        valor = nombre,
                        etiqueta = "Nombre de la planta",
                        enabled = !guardando,
                        onCambio = {
                            nombre = it
                            errorValidacion = null
                        }
                    )

                    CampoPlantaPersonal(
                        valor = ubicacion,
                        etiqueta = "Ubicación",
                        enabled = !guardando,
                        onCambio = {
                            ubicacion = it
                            errorValidacion = null
                        }
                    )
                }

                // Parámetros operativos
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TituloSeccionPlanta(
                        titulo = "Parámetros operativos",
                        descripcion = "Capacidad de procesamiento y costo de alquiler"
                    )

                    CampoPlantaPersonal(
                        valor = capacidad,
                        etiqueta = "Capacidad de procesamiento",
                        sufijo = "TMAD",
                        decimal = true,
                        enabled = !guardando,
                        onCambio = {
                            capacidad = it.replace(',', '.')
                            errorValidacion = null
                        }
                    )

                    CampoPlantaPersonal(
                        valor = costoAlquiler,
                        etiqueta = "Costo de alquiler",
                        sufijo = "por TM",
                        decimal = true,
                        enabled = !guardando,
                        onCambio = {
                            costoAlquiler = it.replace(',', '.')
                            errorValidacion = null
                        }
                    )
                }

                // Estado
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TituloSeccionPlanta(
                        titulo = "Estado de la planta",
                        descripcion = "Selecciona la condición operativa actual"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OpcionEstadoPlanta(
                            texto = "Operativa",
                            descripcion = "En funcionamiento",
                            seleccionada = estado.equals("Operativa", ignoreCase = true),
                            enabled = !guardando,
                            modifier = Modifier.weight(1f)
                        ) {
                            estado = "Operativa"
                        }

                        OpcionEstadoPlanta(
                            texto = "Mantenimiento",
                            descripcion = "Fuera de servicio",
                            seleccionada = estado.equals("Mantenimiento", ignoreCase = true),
                            enabled = !guardando,
                            modifier = Modifier.weight(1f)
                        ) {
                            estado = "Mantenimiento"
                        }
                    }
                }

                // Mensaje de validación o error de API
                if (mensajeError != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = ErrorRojo.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, ErrorRojo.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = mensajeError,
                            modifier = Modifier.padding(12.dp),
                            color = ErrorRojo,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                HorizontalDivider(color = ColorGrisBorde.copy(alpha = 0.8f))

                // Acciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !guardando,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            color = TextoGrisSecundario,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = ::guardar,
                        enabled = !guardando,
                        modifier = Modifier.weight(1.35f),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 13.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmarilloMineroBrillante,
                            contentColor = FondoNegroAbsoluto,
                            disabledContainerColor = AmarilloMineroBrillante.copy(alpha = 0.45f),
                            disabledContentColor = FondoNegroAbsoluto.copy(alpha = 0.65f)
                        )
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = FondoNegroAbsoluto,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(9.dp))
                            Text("Guardando", fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = if (esEdicion) "Guardar cambios" else "Registrar planta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TituloSeccionPlanta(
    titulo: String,
    descripcion: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = titulo,
            color = TextoBlancoSuave,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = descripcion,
            color = TextoGrisSecundario,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun OpcionEstadoPlanta(
    texto: String,
    descripcion: String,
    seleccionada: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (seleccionada)
            AmarilloMineroBrillante.copy(alpha = 0.12f)
        else
            GrisCampos,
        border = BorderStroke(
            width = if (seleccionada) 1.5.dp else 1.dp,
            color = if (seleccionada)
                AmarilloMineroBrillante
            else
                ColorGrisBorde
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                RadioButton(
                    selected = seleccionada,
                    onClick = null,
                    enabled = enabled,
                    modifier = Modifier.size(20.dp),
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AmarilloMineroBrillante,
                        unselectedColor = TextoGrisSecundario
                    )
                )
                Text(
                    text = texto,
                    color = if (seleccionada) AmarilloMineroBrillante else TextoBlancoSuave,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = descripcion,
                modifier = Modifier.padding(start = 27.dp),
                color = TextoGrisSecundario,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun CampoPlantaPersonal(
    valor: String,
    etiqueta: String,
    sufijo: String? = null,
    decimal: Boolean = false,
    enabled: Boolean = true,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        suffix = sufijo?.let { texto ->
            { Text(texto, color = TextoGrisSecundario, fontSize = 11.sp) }
        },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Text
        ),
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = GrisCampos,
            unfocusedContainerColor = GrisCampos,
            disabledContainerColor = GrisCampos.copy(alpha = 0.6f),
            focusedBorderColor = AmarilloMineroBrillante,
            unfocusedBorderColor = ColorGrisBorde,
            disabledBorderColor = ColorGrisBorde.copy(alpha = 0.5f),
            focusedLabelColor = AmarilloMineroBrillante,
            unfocusedLabelColor = TextoGrisSecundario,
            focusedTextColor = TextoBlancoSuave,
            unfocusedTextColor = TextoBlancoSuave,
            disabledTextColor = TextoGrisSecundario
        )
    )
}
