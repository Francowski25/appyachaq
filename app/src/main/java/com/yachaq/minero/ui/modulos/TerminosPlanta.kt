package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
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
import com.yachaq.minero.ui.theme.*

@Composable
fun TerminosPlanta(
    terminosActuales: Terminos = Terminos(),
    onGuardarTerminos: (Terminos) -> Unit = {},
    onDismiss: () -> Unit // ✅ Agregado para controlar el cierre desde la pantalla padre
) {
    // Estado local de términos (actualizable reactivamente)
    var terminos by remember { mutableStateOf(terminosActuales) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = FondoNegroAbsoluto,
            border = BorderStroke(1.dp, ColorGrisBorde)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Contenido scrolleable con todas las secciones de términos
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // ENCABEZADO
                    Text(
                        text = "CONFIGURACIÓN DE TÉRMINOS",
                        color = AmarilloMineroBrillante,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Parámetros técnicos de comercialización y liquidación minera.",
                        color = TextoGrisSecundario,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ===== 1. VALORES DEL CONCENTRADO =====
                    TarjetaConfiguracionSeccion(
                        titulo = "1. Valores del Concentrado",
                        campos = listOf(
                            "Factor de concentrado" to terminos.factorConcentrado.toString(),
                            "% H2O (Humedad)" to terminos.humedad.toString(),
                            "Merma de proceso (%)" to terminos.merma.toString()
                        ),
                        onGuardar = { nuevosValores ->
                            val fc = nuevosValores["Factor de concentrado"]?.toDoubleOrNull()
                            val hu = nuevosValores["% H2O (Humedad)"]?.toDoubleOrNull()
                            val me = nuevosValores["Merma de proceso (%)"]?.toDoubleOrNull()
                            if (fc != null && hu != null && me != null) {
                                terminos = terminos.copy(factorConcentrado = fc, humedad = hu, merma = me)
                                onGuardarTerminos(terminos)
                                true
                            } else false
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ===== 2. LEYES DEL CONCENTRADO =====
                    TarjetaConfiguracionSeccion(
                        titulo = "2. Leyes del Concentrado",
                        campos = listOf(
                            "Ley Cu concentrado (%)" to terminos.leyConcentradoCu.toString(),
                            "Ley Ag concentrado (oz/tc)" to terminos.leyConcentradoAg.toString(),
                            "Ley Au concentrado (oz/tc)" to terminos.leyConcentradoAu.toString()
                        ),
                        onGuardar = { nuevosValores ->
                            val lc = nuevosValores["Ley Cu concentrado (%)"]?.toDoubleOrNull()
                            val la = nuevosValores["Ley Ag concentrado (oz/tc)"]?.toDoubleOrNull()
                            val lo = nuevosValores["Ley Au concentrado (oz/tc)"]?.toDoubleOrNull()
                            if (lc != null && la != null && lo != null) {
                                terminos = terminos.copy(leyConcentradoCu = lc, leyConcentradoAg = la, leyConcentradoAu = lo)
                                onGuardarTerminos(terminos)
                                true
                            } else false
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ===== 3. TÉRMINOS: COBRE =====
                    TarjetaConfiguracionSeccion(
                        titulo = "3. Términos: Cobre (Cu)",
                        campos = listOf(
                            "% Pagable Cu" to terminos.pagableCu.toString(),
                            "Deducción Cu" to terminos.deduccionCu.toString(),
                            "Gastos ref Cu ($/lb)" to terminos.gastosRefCu.toString(),
                            "Protección Cu" to terminos.proteccionCu.toString()
                        ),
                        onGuardar = { nuevosValores ->
                            val pCu = nuevosValores["% Pagable Cu"]?.toDoubleOrNull()
                            val dCu = nuevosValores["Deducción Cu"]?.toDoubleOrNull()
                            val gCu = nuevosValores["Gastos ref Cu ($/lb)"]?.toDoubleOrNull()
                            val proCu = nuevosValores["Protección Cu"]?.toDoubleOrNull()
                            if (pCu != null && dCu != null && gCu != null && proCu != null) {
                                terminos = terminos.copy(pagableCu = pCu, deduccionCu = dCu, gastosRefCu = gCu, proteccionCu = proCu)
                                onGuardarTerminos(terminos)
                                true
                            } else false
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ===== 4. TÉRMINOS: PLATA =====
                    TarjetaConfiguracionSeccion(
                        titulo = "4. Términos: Plata (Ag)",
                        campos = listOf(
                            "% Pagable Ag" to terminos.pagableAg.toString(),
                            "Deducción Ag (gr)" to terminos.deduccionAg.toString(),
                            "Gastos ref Ag ($/oz)" to terminos.gastosRefAg.toString()
                        ),
                        onGuardar = { nuevosValores ->
                            val pAg = nuevosValores["% Pagable Ag"]?.toDoubleOrNull()
                            val dAg = nuevosValores["Deducción Ag (gr)"]?.toDoubleOrNull()
                            val gAg = nuevosValores["Gastos ref Ag ($/oz)"]?.toDoubleOrNull()
                            if (pAg != null && dAg != null && gAg != null) {
                                terminos = terminos.copy(pagableAg = pAg, deduccionAg = dAg, gastosRefAg = gAg)
                                onGuardarTerminos(terminos)
                                true
                            } else false
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ===== 5. TÉRMINOS: ORO =====
                    TarjetaConfiguracionSeccion(
                        titulo = "5. Términos: Oro (Au)",
                        campos = listOf(
                            "% Pagable Au" to terminos.pagableAu.toString(),
                            "Deducción Au (gr)" to terminos.deduccionAu.toString(),
                            "Gastos ref Au ($/oz)" to terminos.gastosRefAu.toString()
                        ),
                        onGuardar = { nuevosValores ->
                            val pAu = nuevosValores["% Pagable Au"]?.toDoubleOrNull()
                            val dAu = nuevosValores["Deducción Au (gr)"]?.toDoubleOrNull()
                            val gAu = nuevosValores["Gastos ref Au ($/oz)"]?.toDoubleOrNull()
                            if (pAu != null && dAu != null && gAu != null) {
                                terminos = terminos.copy(pagableAu = pAu, deduccionAu = dAu, gastosRefAu = gAu)
                                onGuardarTerminos(terminos)
                                true
                            } else false
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ===== 6. MAQUILA =====
                    TarjetaConfiguracionSeccion(
                        titulo = "6. Maquila",
                        campos = listOf(
                            "Maquila ($/TMNS)" to terminos.maquila.toString()
                        ),
                        onGuardar = { nuevosValores ->
                            val maq = nuevosValores["Maquila ($/TMNS)"]?.toDoubleOrNull()
                            if (maq != null) {
                                terminos = terminos.copy(maquila = maq)
                                onGuardarTerminos(terminos)
                                true
                            } else false
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ===== 7. SERVICIOS =====
                    TarjetaConfiguracionSeccion(
                        titulo = "7. Servicios Operativos",
                        campos = listOf(
                            "Análisis ($)" to terminos.analisis.toString(),
                            "Traslado interno ($)" to terminos.trasladoInterno.toString()
                        ),
                        onGuardar = { nuevosValores ->
                            val ana = nuevosValores["Análisis ($)"]?.toDoubleOrNull()
                            val tra = nuevosValores["Traslado interno ($)"]?.toDoubleOrNull()
                            if (ana != null && tra != null) {
                                terminos = terminos.copy(analisis = ana, trasladoInterno = tra)
                                onGuardarTerminos(terminos)
                                true
                            } else false
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ===== 8. IMPUESTOS =====
                    TarjetaConfiguracionSeccion(
                        titulo = "8. Impuestos y Detracciones",
                        campos = listOf(
                            "IGV Porcentaje (0.18 = 18%)" to terminos.igvPorcentaje.toString(),
                            "Detracción Porcentaje (0.10 = 10%)" to terminos.detraccionPorcentaje.toString()
                        ),
                        onGuardar = { nuevosValores ->
                            val igv = nuevosValores["IGV Porcentaje (0.18 = 18%)"]?.toDoubleOrNull()
                            val det = nuevosValores["Detracción Porcentaje (0.10 = 10%)"]?.toDoubleOrNull()
                            if (igv != null && det != null) {
                                terminos = terminos.copy(igvPorcentaje = igv, detraccionPorcentaje = det)
                                onGuardarTerminos(terminos)
                                true
                            } else false
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Botón de Cierre (X) en la esquina superior derecha flotante
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = TextoGrisSecundario
                    )
                }
            }
        }
    }
}

// ========================================================
// COMPONENTE TARJETA DE CONFIGURACIÓN CON MODO EDITAR/GUARDAR
// ========================================================
@Composable
private fun TarjetaConfiguracionSeccion(
    titulo: String,
    campos: List<Pair<String, String>>,
    onGuardar: (Map<String, String>) -> Boolean
) {
    var enModoEdicion by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    val estadoCampos = remember(campos) {
        mutableStateMapOf<String, String>().apply {
            campos.forEach { put(it.first, it.second) }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (enModoEdicion) AmarilloMineroBrillante else ColorGrisBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    color = AmarilloMineroBrillante,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                if (!enModoEdicion) {
                    IconButton(
                        onClick = { enModoEdicion = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar sección",
                            tint = AmarilloMineroBrillante
                        )
                    }
                }
            }

            HorizontalDivider(color = ColorGrisBorde.copy(alpha = 0.5f))

            campos.forEach { (etiqueta, _) ->
                val valorActual = estadoCampos[etiqueta] ?: ""

                if (enModoEdicion) {
                    OutlinedTextField(
                        value = valorActual,
                        onValueChange = {
                            estadoCampos[etiqueta] = it.replace(',', '.')
                            errorMensaje = null
                        },
                        label = { Text(etiqueta) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = etiqueta,
                            color = TextoGrisSecundario,
                            fontSize = 13.sp
                        )
                        Text(
                            text = valorActual,
                            color = TextoBlancoSuave,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            errorMensaje?.let {
                Text(it, color = ErrorRojo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            if (enModoEdicion) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            campos.forEach { estadoCampos[it.first] = it.second }
                            errorMensaje = null
                            enModoEdicion = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoGrisSecundario),
                        border = BorderStroke(1.dp, ColorGrisBorde),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            mostrarConfirmacion = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmarilloMineroBrillante,
                            contentColor = FondoNegroAbsoluto
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Guardar", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AmarilloMineroBrillante,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Confirmar Modificación",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = AmarilloMineroBrillante
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "¿Desea aplicar y guardar las modificaciones en la sección '$titulo'?",
                        fontSize = 13.sp,
                        color = TextoGrisSecundario
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val exito = onGuardar(estadoCampos)
                        if (exito) {
                            enModoEdicion = false
                            errorMensaje = null
                        } else {
                            errorMensaje = "Por favor ingrese valores numéricos válidos."
                        }
                        mostrarConfirmacion = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloMineroBrillante,
                        contentColor = FondoNegroAbsoluto
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sí, Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar", color = TextoGrisSecundario, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}