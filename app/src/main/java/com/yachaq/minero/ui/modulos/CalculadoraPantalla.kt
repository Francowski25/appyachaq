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
import com.yachaq.minero.data.Entrada
import com.yachaq.minero.data.Resultado
import com.yachaq.minero.logica.Calculadora
import com.yachaq.minero.ui.theme.*
import java.util.Locale

private fun dolares(v: Double) = "$ " + String.format(Locale.US, "%,.2f", v)
private fun toneladas(v: Double) = String.format(Locale.US, "%.2f TM", v)

@Composable
fun CalculadoraPantalla() {
    val materiales = listOf("Cobre (Cu)", "Oro (Au)", "Plata (Ag)")
    var materialSeleccionado by remember { mutableStateOf(materiales[0]) }

    var tonelaje by remember { mutableStateOf("") }
    var ley by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var resultado by remember { mutableStateOf<Resultado?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Calculadora Minera",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(16.dp))

        // ===== SELECTOR DE MATERIAL =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            materiales.forEach { material ->
                val seleccionado = materialSeleccionado == material
                val contenedorColor = if (seleccionado) MaterialTheme.colorScheme.primary else GrisCampos
                val textColor = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                Surface(
                    selected = seleccionado,
                    onClick = {
                        if (materialSeleccionado != material) {
                            materialSeleccionado = material
                            tonelaje = ""
                            ley = ""
                            precio = ""
                            resultado = null
                            error = null
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = contenedorColor,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = material,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ===== CAMPOS =====
        Campo(tonelaje, "Toneladas métricas brutas (TM)") {
            tonelaje = it; resultado = null; error = null
        }
        Spacer(Modifier.height(12.dp))

        val etiquetaLey = when {
            materialSeleccionado.contains("Cobre") -> "Ley de Cobre (Cu %)"
            materialSeleccionado.contains("Oro") -> "Ley de Oro (Au oz/tm)"
            else -> "Ley de Plata (Ag oz/tm)"
        }
        Campo(ley, etiquetaLey) {
            ley = it; resultado = null; error = null
        }

        Spacer(Modifier.height(12.dp))

        val etiquetaPrecio = when {
            materialSeleccionado.contains("Cobre") -> "Precio Cobre ($ / lb)"
            materialSeleccionado.contains("Oro") -> "Precio Oro ($ / oz)"
            else -> "Precio Plata ($ / oz)"
        }
        Campo(precio, etiquetaPrecio) {
            precio = it; resultado = null; error = null
        }

        error?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))

        // ===== BOTÓN =====
        Button(
            onClick = {
                val t = tonelaje.toDoubleOrNull()
                val l = ley.toDoubleOrNull()
                val p = precio.toDoubleOrNull()

                when {
                    t == null || l == null || p == null -> {
                        error = "Por favor complete todos los campos con números válidos"
                        resultado = null
                    }
                    t <= 0 || l <= 0 || p <= 0 -> {
                        error = "Los valores deben ser mayores que cero"
                        resultado = null
                    }
                    else -> {
                        val entrada = Entrada(
                            material = materialSeleccionado,
                            tonelaje = t,
                            ley = l,
                            precio = p
                        )
                        val r = Calculadora.calcular(entrada)
                        if (r == null) {
                            error = "El cálculo no generó concentrado válido."
                            resultado = null
                        } else {
                            error = null
                            resultado = r
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(
                "CALCULAR LIQUIDACIÓN",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 1.2.sp
            )
        }

        // ===== RESULTADOS =====
        resultado?.let { r ->
            Spacer(Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // ===== PAGO TOTAL =====
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PAGO TOTAL",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = dolares(r.totalAPagar),
                            color = if (r.totalAPagar >= 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // ===== TONELADAS DE CONCENTRADO =====
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TONELADAS DE CONCENTRADO",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = toneladas(r.tmns),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // ===== 1. VALOR DEL CONCENTRADO =====
                    SeccionTitulo("1. Valor del Concentrado")
                    Fila("TM Húmedas de Concentrado", toneladas(r.tmHumedas))
                    Fila("TM Secas Netas (TMNS)", String.format(Locale.US, "%.2f TMNS", r.tmns))
                    Fila("Pago por Cobre (Cu)", dolares(r.pagoCu))
                    Fila("Pago por Plata (Ag)", dolares(r.pagoAg))
                    Fila("Pago por Oro (Au)", dolares(r.pagoAu))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // ===== 2. REDUCCIONES =====
                    SeccionTitulo("2. Reducciones")
                    Fila("Maquila", "- " + dolares(r.maquila))
                    Fila("Gastos de Refinación", "- " + dolares(r.reduccionRefinacion))
                    Fila("Análisis", "- " + dolares(r.analisis))
                    Fila("Traslado Interno", "- " + dolares(r.trasladoInterno))
                    Fila("Pago por TM de Concentrado", dolares(r.pagoPorTm))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // ===== 3. LIQUIDACIÓN =====
                    SeccionTitulo("3. Liquidación")
                    Fila("Valor Neto", dolares(r.valorNeto))
                    Fila("IGV (18 %)", dolares(r.igv))
                    Fila("Valor Total", dolares(r.valorTotal))
                    Fila("Detracción (10 %)", "- " + dolares(r.detraccion))
                    Fila("Alquiler de Planta", "- " + dolares(r.alquilerTotal))
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Los parámetros adicionales de liquidación son administrados centralmente.",
            color = MaterialTheme.colorScheme.outline,
            fontSize = 11.sp
        )
    }
}

// ===== AUXILIARES =====

@Composable
private fun SeccionTitulo(titulo: String) {
    Text(titulo, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun Campo(valor: String, etiqueta: String, onCambio: (String) -> Unit) {
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
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(valor, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}