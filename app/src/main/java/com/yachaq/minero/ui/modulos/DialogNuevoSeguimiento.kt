package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.*
import com.yachaq.minero.ui.theme.*
import com.yachaq.minero.logica.FormateadorFecha
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioNuevoSeguimiento(
    onGuardar: (SeguimientoCarga) -> Unit,
    onCancelar: () -> Unit
) {
    val usuarioActual = SesionUsuario.usuarioActual
    val listaPlantas = PlantasProc.lista

    // Selección de Planta
    var plantaSeleccionadaTemp by remember { mutableStateOf(listaPlantas.firstOrNull()) }
    var plantaConfirmada by remember { mutableStateOf<PlantaProcesadora?>(null) }
    var esPlantaConfirmada by remember { mutableStateOf(false) }
    var expandedPlanta by remember { mutableStateOf(false) }
    var mostrarDialogoCambiarPlanta by remember { mutableStateOf(false) }

    // Mineral y Toneladas
    var mineral by remember { mutableStateOf("Cobre (Cu)") }
    var toneladasInput by remember { mutableStateOf("200") }

    // Fecha
    var fechaTextoInput by remember { mutableStateOf(FormateadorFecha.obtenerFechaActualTexto()) }
    var mostrarDatePickerDialog by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AmarilloMineroBrillante),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "NUEVO REGISTRO DE CARGA",
                color = AmarilloMineroBrillante,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )

            HorizontalDivider(color = ColorGrisBorde)

            // SELECCIÓN DE PLANTA (ESTILO INDUSTRIAL)
            Card(
                colors = CardDefaults.cardColors(containerColor = AmarilloMineroBrillante),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Factory,
                            contentDescription = null,
                            tint = FondoNegroAbsoluto,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (esPlantaConfirmada) "PLANTA CONFIRMADA" else "SELECCIONAR PLANTA",
                            color = FondoNegroAbsoluto,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (!esPlantaConfirmada) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                Surface(
                                    onClick = { expandedPlanta = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = FondoNegroAbsoluto,
                                    border = BorderStroke(1.dp, ColorGrisBorde),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = plantaSeleccionadaTemp?.nombre ?: "Seleccionar...",
                                                color = TextoBlancoSuave,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            plantaSeleccionadaTemp?.let { p ->
                                                Text(
                                                    text = "📍 ${p.ubicacion} | $${p.costoAlquilerPorTM}/TM",
                                                    color = TextoGrisSecundario,
                                                    fontSize = 10.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AmarilloMineroBrillante)
                                    }
                                }

                                DropdownMenu(
                                    expanded = expandedPlanta,
                                    onDismissRequest = { expandedPlanta = false },
                                    modifier = Modifier.background(FondoNegroSecundario).fillMaxWidth(0.85f)
                                ) {
                                    listaPlantas.forEach { planta ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(planta.nombre, color = TextoBlancoSuave, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("📍 ${planta.ubicacion} | Alquiler: $${planta.costoAlquilerPorTM}/TM", color = TextoGrisSecundario, fontSize = 11.sp)
                                                }
                                            },
                                            onClick = {
                                                plantaSeleccionadaTemp = planta
                                                expandedPlanta = false
                                            }
                                        )
                                        HorizontalDivider(color = ColorGrisBorde.copy(alpha = 0.5f))
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (plantaSeleccionadaTemp != null) {
                                        plantaConfirmada = plantaSeleccionadaTemp
                                        esPlantaConfirmada = true
                                        errorMsg = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FondoNegroAbsoluto, contentColor = AmarilloMineroBrillante),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Confirmar", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FondoNegroAbsoluto,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AmarilloMineroBrillante, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text(plantaConfirmada?.nombre ?: "", color = TextoBlancoSuave, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("📍 ${plantaConfirmada?.ubicacion} | $${plantaConfirmada?.costoAlquilerPorTM}/TM", color = TextoGrisSecundario, fontSize = 11.sp)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { mostrarDialogoCambiarPlanta = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmarilloMineroBrillante),
                                    border = BorderStroke(1.dp, AmarilloMineroBrillante),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Cambiar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Mineral
            OutlinedTextField(
                value = mineral,
                onValueChange = { mineral = it },
                label = { Text("Tipo de Mineral") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GrisCampos, unfocusedContainerColor = GrisCampos,
                    focusedBorderColor = AmarilloMineroBrillante, unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedLabelColor = AmarilloMineroBrillante, unfocusedLabelColor = TextoGrisSecundario,
                    focusedTextColor = TextoBlancoSuave, unfocusedTextColor = TextoBlancoSuave
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Toneladas
            OutlinedTextField(
                value = toneladasInput,
                onValueChange = { toneladasInput = it.replace(',', '.') },
                label = { Text("Toneladas Métricas Brutas (TM)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GrisCampos, unfocusedContainerColor = GrisCampos,
                    focusedBorderColor = AmarilloMineroBrillante, unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedLabelColor = AmarilloMineroBrillante, unfocusedLabelColor = TextoGrisSecundario,
                    focusedTextColor = TextoBlancoSuave, unfocusedTextColor = TextoBlancoSuave
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // FECHA CON CALENDARIO NATIVO
            OutlinedTextField(
                value = fechaTextoInput,
                onValueChange = { input ->
                    fechaTextoInput = input
                    errorMsg = null
                },
                label = { Text("Fecha de Ingreso (AAAA-MM-DD)") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { mostrarDatePickerDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Abrir Calendario",
                            tint = AmarilloMineroBrillante
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GrisCampos, unfocusedContainerColor = GrisCampos,
                    focusedBorderColor = AmarilloMineroBrillante, unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedLabelColor = AmarilloMineroBrillante, unfocusedLabelColor = TextoGrisSecundario,
                    focusedTextColor = TextoBlancoSuave, unfocusedTextColor = TextoBlancoSuave
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            errorMsg?.let {
                Text(it, color = ErrorRojo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            // Botones Acciones
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onCancelar,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoGrisSecundario),
                    border = BorderStroke(1.dp, ColorGrisBorde),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val tm = toneladasInput.toDoubleOrNull()
                        val fechaValidada = FormateadorFecha.validarYObtenerFecha(fechaTextoInput)

                        when {
                            !esPlantaConfirmada || plantaConfirmada == null -> {
                                errorMsg = "Por favor seleccione y confirme una planta de destino."
                            }
                            tm == null || tm <= 0 -> {
                                errorMsg = "Ingrese un tonelaje válido mayor a cero."
                            }
                            fechaValidada == null -> {
                                errorMsg = "Fecha inválida. Use el formato AAAA-MM-DD o el selector."
                            }
                            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                                    FormateadorFecha.esPosteriorAHoy(fechaValidada) -> {
                                errorMsg = "La fecha no puede ser posterior al día de hoy."
                            }
                            else -> {
                                val nuevo = SeguimientoCarga(
                                    id = (System.currentTimeMillis() % 10000).toInt(),
                                    usuarioCorreo = usuarioActual?.correo ?: "demo@yachaq.com",
                                    planta = plantaConfirmada!!,
                                    mineral = mineral,
                                    toneladasBrutas = tm,
                                    fechaIngreso = fechaValidada
                                )
                                onGuardar(nuevo)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloMineroBrillante,
                        contentColor = FondoNegroAbsoluto
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Guardar Carga", fontWeight = FontWeight.Black)
                }
            }
        }
    }

    // DIÁLOGO DATEPICKER MATERIAL 3
    if (mostrarDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            fechaTextoInput = date.toString()
                        }
                        mostrarDatePickerDialog = false
                    }
                ) {
                    Text("Aceptar", color = AmarilloMineroBrillante, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerDialog = false }) {
                    Text("Cancelar", color = TextoGrisSecundario)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // DIÁLOGO CONFIRMACIÓN DE CAMBIO DE PLANTA
    if (mostrarDialogoCambiarPlanta) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCambiarPlanta = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = AmarilloMineroBrillante, modifier = Modifier.size(32.dp)) },
            title = { Text("¿Desea cambiar de Planta?", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AmarilloMineroBrillante) },
            text = { Text("Al cambiar de planta se reiniciará la selección previa de este registro.", fontSize = 13.sp, color = TextoGrisSecundario) },
            confirmButton = {
                Button(
                    onClick = {
                        esPlantaConfirmada = false
                        plantaConfirmada = null
                        mostrarDialogoCambiarPlanta = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmarilloMineroBrillante, contentColor = FondoNegroAbsoluto),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Sí, Cambiar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCambiarPlanta = false }) { Text("Cancelar", color = TextoGrisSecundario) }
            }
        )
    }
}