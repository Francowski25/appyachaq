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
import com.yachaq.minero.data.PlantaProcesadora
import com.yachaq.minero.data.PlantasProc
import com.yachaq.minero.ui.theme.*

@Composable
fun DialogoNuevaPlanta(
    onDismiss: () -> Unit,
    onGuardar: (PlantaProcesadora) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }
    var costoAlquiler by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("Operativa") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; error = null },
                    label = { Text("Nombre de la Planta") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GrisCampos, unfocusedContainerColor = GrisCampos,
                        focusedBorderColor = AmarilloMineroBrillante, unfocusedBorderColor = ColorGrisBorde,
                        focusedLabelColor = AmarilloMineroBrillante, unfocusedLabelColor = TextoGrisSecundario,
                        focusedTextColor = TextoBlancoSuave, unfocusedTextColor = TextoBlancoSuave
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it; error = null },
                    label = { Text("Ubicación (Ej. Abancay, Apurímac)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GrisCampos, unfocusedContainerColor = GrisCampos,
                        focusedBorderColor = AmarilloMineroBrillante, unfocusedBorderColor = ColorGrisBorde,
                        focusedLabelColor = AmarilloMineroBrillante, unfocusedLabelColor = TextoGrisSecundario,
                        focusedTextColor = TextoBlancoSuave, unfocusedTextColor = TextoBlancoSuave
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = capacidad,
                    onValueChange = { capacidad = it.replace(',', '.'); error = null },
                    label = { Text("Capacidad TMAD") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GrisCampos, unfocusedContainerColor = GrisCampos,
                        focusedBorderColor = AmarilloMineroBrillante, unfocusedBorderColor = ColorGrisBorde,
                        focusedLabelColor = AmarilloMineroBrillante, unfocusedLabelColor = TextoGrisSecundario,
                        focusedTextColor = TextoBlancoSuave, unfocusedTextColor = TextoBlancoSuave
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costoAlquiler,
                    onValueChange = { costoAlquiler = it.replace(',', '.'); error = null },
                    label = { Text("Costo Alquiler por TM ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GrisCampos, unfocusedContainerColor = GrisCampos,
                        focusedBorderColor = AmarilloMineroBrillante, unfocusedBorderColor = ColorGrisBorde,
                        focusedLabelColor = AmarilloMineroBrillante, unfocusedLabelColor = TextoGrisSecundario,
                        focusedTextColor = TextoBlancoSuave, unfocusedTextColor = TextoBlancoSuave
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

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
                            onClick = { estado = opcion },
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
                onClick = {
                    val capDouble = capacidad.toDoubleOrNull()
                    val costoDouble = costoAlquiler.toDoubleOrNull()

                    if (nombre.isBlank() || ubicacion.isBlank() || capDouble == null || costoDouble == null) {
                        error = "Por favor complete todos los campos con valores válidos."
                    } else {
                        val nuevoId = (PlantasProc.lista.maxOfOrNull { it.id } ?: 0) + 1
                        onGuardar(
                            PlantaProcesadora(
                                id = nuevoId,
                                nombre = nombre.trim(),
                                ubicacion = ubicacion.trim(),
                                capacidadTMAD = capDouble,
                                costoAlquilerPorTM = costoDouble,
                                estado = estado
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmarilloMineroBrillante,
                    contentColor = FondoNegroAbsoluto
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Planta", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextoGrisSecundario, fontWeight = FontWeight.Bold)
            }
        }
    )
}