package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestPlant
import com.yachaq.minero.data.remote.model.ResponsePlant
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DialogoNuevaPlanta(
    onDismiss: () -> Unit,
    onCreada: (ResponsePlant) -> Unit
) {
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }
    var costoAlquiler by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("Operativa") }
    var error by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }

    fun guardar() {
        val cap = capacidad.toDoubleOrNull()
        val costo = costoAlquiler.toDoubleOrNull()

        if (nombre.isBlank() || ubicacion.isBlank() || cap == null || costo == null) {
            error = "Por favor complete todos los campos con valores válidos."
            return
        }
        if (cap <= 0 || costo < 0) {
            error = "La capacidad debe ser mayor que cero y el costo no puede ser negativo."
            return
        }

        scope.launch {
            guardando = true
            error = null
            try {
                val resp = RetrofitClient.plantApi.crearPlanta(
                    RequestPlant(
                        name = nombre.trim(),
                        location = ubicacion.trim(),
                        capacityTMAD = cap,
                        rentalCostPerTM = costo,
                        status = estado
                    )
                )
                val body = resp.body()
                if (resp.isSuccessful && body?.type == "SUCCESS") {
                    onCreada(body)
                } else {
                    error = body?.listMessage?.firstOrNull()
                        ?: "Error del servidor (${resp.code()})"
                }
            } catch (e: Exception) {
                error = "No se pudo conectar: ${e.message}"
            } finally {
                guardando = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        shape = RoundedCornerShape(20.dp),
        containerColor = FondoNegroSecundario,
        title = {
            Text(
                text = "REGISTRAR NUEVA PLANTA",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = AmarilloMineroBrillante,
                letterSpacing = 0.8.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoPlanta(nombre, "Nombre de la Planta", enabled = !guardando) {
                    nombre = it; error = null
                }
                CampoPlanta(ubicacion, "Ubicación (Ej. Abancay, Apurímac)", enabled = !guardando) {
                    ubicacion = it; error = null
                }
                CampoPlanta(capacidad, "Capacidad TMAD", decimal = true, enabled = !guardando) {
                    capacidad = it.replace(',', '.'); error = null
                }
                CampoPlanta(costoAlquiler, "Costo Alquiler por TM ($)", decimal = true, enabled = !guardando) {
                    costoAlquiler = it.replace(',', '.'); error = null
                }

                Text(
                    text = "Estado Inicial:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmarilloMineroBrillante
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Operativa", "Mantenimiento").forEach { opcion ->
                        val seleccionado = estado == opcion
                        Surface(
                            onClick = { if (!guardando) estado = opcion },
                            shape = RoundedCornerShape(12.dp),
                            color = if (seleccionado) AmarilloMineroBrillante else GrisCampos,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = opcion,
                                    color = if (seleccionado) FondoNegroAbsoluto else TextoBlancoSuave,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                error?.let {
                    Text(it, color = ErrorRojo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { guardar() },
                enabled = !guardando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmarilloMineroBrillante,
                    contentColor = FondoNegroAbsoluto
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = FondoNegroAbsoluto,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar Planta", fontWeight = FontWeight.Black)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !guardando) {
                Text("Cancelar", color = TextoGrisSecundario, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun CampoPlanta(
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
            focusedContainerColor = GrisCampos, unfocusedContainerColor = GrisCampos,
            focusedBorderColor = AmarilloMineroBrillante, unfocusedBorderColor = ColorGrisBorde,
            focusedLabelColor = AmarilloMineroBrillante, unfocusedLabelColor = TextoGrisSecundario,
            focusedTextColor = TextoBlancoSuave, unfocusedTextColor = TextoBlancoSuave
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}