package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.Entrada
import com.yachaq.minero.data.PlantasProc
import com.yachaq.minero.data.Resultado
import com.yachaq.minero.data.SesionUsuario
import com.yachaq.minero.logica.Calculadora
import com.yachaq.minero.ui.theme.*
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun CalculadoraPantalla() {
    val listaPlantas = PlantasProc.lista

    // Recupera automáticamente la planta asociada al usuario activo
    var plantaConfirmada by remember { mutableStateOf(SesionUsuario.obtenerPlantaActiva()) }
    var esConfirmado by remember { mutableStateOf(plantaConfirmada != null) }

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
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CALCULADORA DE LIQUIDACIÓN",
            color = AmarilloMineroBrillante,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Estimación técnica y económica de concentrado minero",
            color = TextoGrisSecundario,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(Modifier.height(20.dp))

        // 1. Panel de Planta (Guarda en la sesión del usuario)
        SeccionPlantaCard(
            listaPlantas = listaPlantas,
            esConfirmado = esConfirmado,
            plantaConfirmada = plantaConfirmada,
            onConfirmar = { planta ->
                plantaConfirmada = planta
                esConfirmado = true
                SesionUsuario.guardarPlantaActiva(planta) // Asocia la planta al usuario
                error = null
            },
            onCambiar = {
                esConfirmado = false
                plantaConfirmada = null
                SesionUsuario.guardarPlantaActiva(null) // Desvincula la planta
                resultado = null
            }
        )

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = ColorGrisBorde)
        Spacer(Modifier.height(20.dp))

        // 2. Selector de Material
        SeccionMaterialSelector(
            materiales = materiales,
            materialSeleccionado = materialSeleccionado,
            onMaterialSeleccionado = { material ->
                if (materialSeleccionado != material) {
                    materialSeleccionado = material
                    tonelaje = ""
                    ley = ""
                    precio = ""
                    resultado = null
                    error = null
                }
            }
        )

        Spacer(Modifier.height(20.dp))

        // 3. Campos de Entrada
        SeccionCamposEntrada(
            materialSeleccionado = materialSeleccionado,
            tonelaje = tonelaje,
            onTonelajeChange = { tonelaje = it; resultado = null; error = null },
            ley = ley,
            onLeyChange = { ley = it; resultado = null; error = null },
            precio = precio,
            onPrecioChange = { precio = it; resultado = null; error = null },
            error = error
        )

        Spacer(Modifier.height(24.dp))

        // Botón Calcular
        Button(
            onClick = {
                val t = tonelaje.toDoubleOrNull()
                val l = ley.toDoubleOrNull()
                val p = precio.toDoubleOrNull()

                when {
                    !esConfirmado || plantaConfirmada == null -> {
                        error = "Por favor seleccione y confirme una planta procesadora primero."
                        resultado = null
                    }
                    t == null || l == null || p == null -> {
                        error = "Por favor complete todos los campos con números válidos."
                        resultado = null
                    }
                    t <= 0 || l <= 0 || p <= 0 -> {
                        error = "Los valores deben ser mayores que cero."
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
                containerColor = AmarilloMineroBrillante,
                contentColor = FondoNegroAbsoluto
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                "CALCULAR LIQUIDACIÓN",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 1.2.sp
            )
        }

        // 4. Resultados de la Liquidación
        resultado?.let { r ->
            Spacer(Modifier.height(24.dp))
            SeccionResultadosCard(resultado = r, plantaConfirmada = plantaConfirmada)
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Los parámetros adicionales de liquidación son administrados centralmente.",
            color = TextoGrisSecundario,
            fontSize = 11.sp
        )
    }
}