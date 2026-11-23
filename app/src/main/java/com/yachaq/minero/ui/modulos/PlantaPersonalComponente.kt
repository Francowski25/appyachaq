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
    var estado by remember(planta) {
        mutableStateOf(planta?.status ?: "Operativa")
    }
    var errorValidacion by remember(planta) { mutableStateOf<String?>(null) }

    fun guardar() {
        val capacidadNumero = capacidad.replace(',', '.').toDoubleOrNull()
        val costoNumero = costoAlquiler.replace(',', '.').toDoubleOrNull()
        if (nombre.isBlank() || ubicacion.isBlank() ||
            capacidadNumero == null || costoNumero == null ||
            !capacidadNumero.isFinite() || !costoNumero.isFinite()
        ) {
            errorValidacion = "Complete todos los campos con valores válidos."
            return
        }
        if (capacidadNumero <= 0 || costoNumero < 0) {
            errorValidacion = "La capacidad debe ser mayor que cero y el costo no puede ser negativo."
            return
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
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = FondoNegroSecundario,
            border = BorderStroke(1.dp, ColorGrisBorde)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (planta == null) "AÑADIR PLANTA PERSONAL" else "EDITAR PLANTA PERSONAL",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = AmarilloMineroBrillante
                )
                CampoPlantaPersonal(nombre, "Nombre de la planta", enabled = !guardando) {
                    nombre = it
                    errorValidacion = null
                }
                CampoPlantaPersonal(ubicacion, "Ubicación", enabled = !guardando) {
                    ubicacion = it
                    errorValidacion = null
                }
                CampoPlantaPersonal(
                    capacidad,
                    "Capacidad TMAD",
                    decimal = true,
                    enabled = !guardando
                ) {
                    capacidad = it.replace(',', '.')
                    errorValidacion = null
                }
                CampoPlantaPersonal(
                    costoAlquiler,
                    "Costo de alquiler por TM",
                    decimal = true,
                    enabled = !guardando
                ) {
                    costoAlquiler = it.replace(',', '.')
                    errorValidacion = null
                }

                Text(
                    text = "Estado",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmarilloMineroBrillante
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Operativa", "Mantenimiento").forEach { opcion ->
                        val seleccionada = estado.equals(opcion, ignoreCase = true)
                        Surface(
                            onClick = { if (!guardando) estado = opcion },
                            enabled = !guardando,
                            shape = RoundedCornerShape(12.dp),
                            color = if (seleccionada) AmarilloMineroBrillante else GrisCampos,
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = opcion,
                                    color = if (seleccionada) FondoNegroAbsoluto else TextoBlancoSuave,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                (errorValidacion ?: error)?.let {
                    Text(it, color = ErrorRojo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, enabled = !guardando) {
                        Text("Cancelar", color = TextoGrisSecundario)
                    }
                    Button(
                        onClick = { guardar() },
                        enabled = !guardando,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmarilloMineroBrillante,
                            contentColor = FondoNegroAbsoluto
                        )
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = FondoNegroAbsoluto,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoPlantaPersonal(
    valor: String,
    etiqueta: String,
    decimal: Boolean = false,
    enabled: Boolean = true,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = if (decimal) KeyboardOptions(keyboardType = KeyboardType.Decimal)
        else KeyboardOptions.Default,
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
}