package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.Terminos
import com.yachaq.minero.ui.theme.GrisCampos

@Composable
fun ConfiguracionPantalla(
    terminosActuales: Terminos = Terminos(),
    onGuardar: (Terminos) -> Unit = {},
    onCancelar: () -> Unit = {}
) {
    // ===== Estados de cada campo =====
    var factorConcentrado by remember { mutableStateOf(terminosActuales.factorConcentrado.toString()) }
    var humedad by remember { mutableStateOf(terminosActuales.humedad.toString()) }
    var merma by remember { mutableStateOf(terminosActuales.merma.toString()) }

    var leyConcentradoCu by remember { mutableStateOf(terminosActuales.leyConcentradoCu.toString()) }
    var leyConcentradoAg by remember { mutableStateOf(terminosActuales.leyConcentradoAg.toString()) }
    var leyConcentradoAu by remember { mutableStateOf(terminosActuales.leyConcentradoAu.toString()) }

    var pagableCu by remember { mutableStateOf(terminosActuales.pagableCu.toString()) }
    var deduccionCu by remember { mutableStateOf(terminosActuales.deduccionCu.toString()) }
    var gastosRefCu by remember { mutableStateOf(terminosActuales.gastosRefCu.toString()) }
    var proteccionCu by remember { mutableStateOf(terminosActuales.proteccionCu.toString()) }

    var pagableAg by remember { mutableStateOf(terminosActuales.pagableAg.toString()) }
    var deduccionAg by remember { mutableStateOf(terminosActuales.deduccionAg.toString()) }
    var gastosRefAg by remember { mutableStateOf(terminosActuales.gastosRefAg.toString()) }

    var pagableAu by remember { mutableStateOf(terminosActuales.pagableAu.toString()) }
    var deduccionAu by remember { mutableStateOf(terminosActuales.deduccionAu.toString()) }
    var gastosRefAu by remember { mutableStateOf(terminosActuales.gastosRefAu.toString()) }

    var maquila by remember { mutableStateOf(terminosActuales.maquila.toString()) }

    var analisis by remember { mutableStateOf(terminosActuales.analisis.toString()) }
    var trasladoInterno by remember { mutableStateOf(terminosActuales.trasladoInterno.toString()) }

    var igvPorcentaje by remember { mutableStateOf(terminosActuales.igvPorcentaje.toString()) }
    var detraccionPorcentaje by remember { mutableStateOf(terminosActuales.detraccionPorcentaje.toString()) }

    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // ENCABEZADO
        Text(
            text = "Configuración de Términos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Solo el administrador puede modificar los parámetros de liquidación.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ===== 1. VALORES DEL CONCENTRADO =====
        SeccionTitulo("1. Valores del Concentrado")
        Campo(factorConcentrado, "Factor de concentrado") { factorConcentrado = it; error = null }
        Campo(humedad, "% H2O") { humedad = it; error = null }
        Campo(merma, "Merma") { merma = it; error = null }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 2. LEYES DEL CONCENTRADO =====
        SeccionTitulo("2. Leyes del Concentrado")
        Campo(leyConcentradoCu, "Ley Cu concentrado (%)") { leyConcentradoCu = it; error = null }
        Campo(leyConcentradoAg, "Ley Ag concentrado (oz/tc)") { leyConcentradoAg = it; error = null }
        Campo(leyConcentradoAu, "Ley Au concentrado (oz/tc)") { leyConcentradoAu = it; error = null }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 3. TÉRMINOS: COBRE =====
        SeccionTitulo("3. Términos: Cobre")
        Campo(pagableCu, "% Pagable Cu") { pagableCu = it; error = null }
        Campo(deduccionCu, "Deducción Cu") { deduccionCu = it; error = null }
        Campo(gastosRefCu, "Gastos ref Cu ($/lb)") { gastosRefCu = it; error = null }
        Campo(proteccionCu, "Protección Cu") { proteccionCu = it; error = null }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 4. TÉRMINOS: PLATA =====
        SeccionTitulo("4. Términos: Plata")
        Campo(pagableAg, "% Pagable Ag") { pagableAg = it; error = null }
        Campo(deduccionAg, "Deducción Ag (gr)") { deduccionAg = it; error = null }
        Campo(gastosRefAg, "Gastos ref Ag ($/oz)") { gastosRefAg = it; error = null }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 5. TÉRMINOS: ORO =====
        SeccionTitulo("5. Términos: Oro")
        Campo(pagableAu, "% Pagable Au") { pagableAu = it; error = null }
        Campo(deduccionAu, "Deducción Au (gr)") { deduccionAu = it; error = null }
        Campo(gastosRefAu, "Gastos ref Au ($/oz)") { gastosRefAu = it; error = null }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 6. MAQUILA =====
        SeccionTitulo("6. Maquila")
        Campo(maquila, "Maquila ($/TMNS)") { maquila = it; error = null }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 7. SERVICIOS =====
        SeccionTitulo("7. Servicios")
        Campo(analisis, "Análisis ($)") { analisis = it; error = null }
        Campo(trasladoInterno, "Traslado interno ($)") { trasladoInterno = it; error = null }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 8. IMPUESTOS =====
        SeccionTitulo("8. Impuestos")
        Campo(igvPorcentaje, "IGV (0.18 = 18%)") { igvPorcentaje = it; error = null }
        Campo(detraccionPorcentaje, "Detracción (0.10 = 10%)") { detraccionPorcentaje = it; error = null }

        error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== BOTONES DE ACCIÓN =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancelar,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("CANCELAR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Button(
                onClick = {
                    val fc = factorConcentrado.toDoubleOrNull()
                    val hu = humedad.toDoubleOrNull()
                    val me = merma.toDoubleOrNull()
                    val lc = leyConcentradoCu.toDoubleOrNull()
                    val la = leyConcentradoAg.toDoubleOrNull()
                    val lo = leyConcentradoAu.toDoubleOrNull()
                    val pCu = pagableCu.toDoubleOrNull()
                    val dCu = deduccionCu.toDoubleOrNull()
                    val gCu = gastosRefCu.toDoubleOrNull()
                    val proCu = proteccionCu.toDoubleOrNull()
                    val pAg = pagableAg.toDoubleOrNull()
                    val dAg = deduccionAg.toDoubleOrNull()
                    val gAg = gastosRefAg.toDoubleOrNull()
                    val pAu = pagableAu.toDoubleOrNull()
                    val dAu = deduccionAu.toDoubleOrNull()
                    val gAu = gastosRefAu.toDoubleOrNull()
                    val maq = maquila.toDoubleOrNull()
                    val ana = analisis.toDoubleOrNull()
                    val tra = trasladoInterno.toDoubleOrNull()
                    val igv = igvPorcentaje.toDoubleOrNull()
                    val det = detraccionPorcentaje.toDoubleOrNull()

                    if (listOf(
                            fc, hu, me, lc, la, lo, pCu, dCu, gCu, proCu,
                            pAg, dAg, gAg, pAu, dAu, gAu, maq, ana, tra, igv, det
                        ).any { it == null }
                    ) {
                        error = "Por favor complete todos los campos con números válidos."
                    } else {
                        val nuevos = Terminos(
                            factorConcentrado = fc!!,
                            humedad = hu!!,
                            merma = me!!,
                            leyConcentradoCu = lc!!,
                            leyConcentradoAg = la!!,
                            leyConcentradoAu = lo!!,
                            pagableCu = pCu!!,
                            deduccionCu = dCu!!,
                            gastosRefCu = gCu!!,
                            proteccionCu = proCu!!,
                            pagableAg = pAg!!,
                            deduccionAg = dAg!!,
                            gastosRefAg = gAg!!,
                            pagableAu = pAu!!,
                            deduccionAu = dAu!!,
                            gastosRefAu = gAu!!, // Mantenido tal cual tu objeto original
                            maquila = maq!!,
                            analisis = ana!!,
                            trasladoInterno = tra!!,
                            igvPorcentaje = igv!!,
                            detraccionPorcentaje = det!!
                        )
                        error = null
                        onGuardar(nuevos)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("GUARDAR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ===== COMPONENTES AUXILIARES ESTILIZADOS =====

@Composable
private fun SeccionTitulo(titulo: String) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun Campo(
    valor: String,
    etiqueta: String,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { onCambio(it.replace(',', '.')) },
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = GrisCampos,
            unfocusedContainerColor = GrisCampos,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}