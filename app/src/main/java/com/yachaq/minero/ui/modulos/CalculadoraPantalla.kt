package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestCalculo
import com.yachaq.minero.data.remote.model.ResponseCalculo
import com.yachaq.minero.data.remote.model.ResponsePlant
import com.yachaq.minero.data.remote.model.SesionUsuario
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CalculadoraPantalla() {
    val scope = rememberCoroutineScope()
    val usuarioActual = SesionUsuario.usuarioActual

    // ---------- Plantas desde el backend ----------
    var plantas by remember { mutableStateOf<List<ResponsePlant>>(emptyList()) }
    var cargandoPlantas by remember { mutableStateOf(true) }
    var errorPlantas by remember { mutableStateOf<String?>(null) }
    var recargarPlantas by remember { mutableIntStateOf(0) }

    var plantaConfirmada by remember { mutableStateOf<ResponsePlant?>(null) }
    var esConfirmado by remember { mutableStateOf(false) }

    LaunchedEffect(recargarPlantas) {
        cargandoPlantas = true
        errorPlantas = null
        try {
            val resp = RetrofitClient.plantApi.listarPlantas()
            if (resp.isSuccessful) {
                plantas = resp.body().orEmpty()
            } else {
                errorPlantas = "Error al cargar plantas (${resp.code()})"
            }
        } catch (e: Exception) {
            errorPlantas = "No se pudo conectar: ${e.message}"
        } finally {
            cargandoPlantas = false
        }
    }

    // ---------- Formulario ----------
    val materiales = listOf("Cobre (Cu)", "Oro (Au)", "Plata (Ag)")
    var materialSeleccionado by remember { mutableStateOf(materiales[0]) }

    var tonelaje by remember { mutableStateOf("") }
    var ley by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var resultado by remember { mutableStateOf<ResponseCalculo?>(null) }
    var calculando by remember { mutableStateOf(false) }

    fun calcular() {
        val t = tonelaje.toDoubleOrNull()
        val l = ley.toDoubleOrNull()
        val p = precio.toDoubleOrNull()
        val idPlant = plantaConfirmada?.idPlant
        val idUser = usuarioActual?.idUser // AJUSTAR: nombre real del campo en LoginResponse

        when {
            idUser.isNullOrBlank() -> error = "Sesión no válida. Vuelva a iniciar sesión."
            !esConfirmado ->
                error = "Por favor seleccione y confirme una planta procesadora primero."
            idPlant.isNullOrBlank() -> error = "La planta seleccionada no es válida."
            t == null || l == null || p == null ->
                error = "Por favor complete todos los campos con números válidos."
            t <= 0 || l <= 0 || p <= 0 -> error = "Los valores deben ser mayores que cero."
            else -> scope.launch {
                calculando = true
                error = null
                resultado = null
                try {
                    val resp = RetrofitClient.calculationApi.calcular(
                        RequestCalculo(
                            idUser = idUser,
                            idPlant = idPlant,
                            material = materialSeleccionado,
                            tonelaje = t,
                            ley = l,
                            precio = p
                        )
                    )
                    val body = resp.body()
                    if (resp.isSuccessful && body?.type == "SUCCESS") {
                        resultado = body
                    } else {
                        error = body?.listMessage?.firstOrNull()
                            ?: "Error del servidor (${resp.code()})"
                    }
                } catch (e: Exception) {
                    error = "No se pudo conectar: ${e.message}"
                } finally {
                    calculando = false
                }
            }
        }
    }

    // ---------- UI ----------
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

        when {
            cargandoPlantas -> CircularProgressIndicator(
                color = AmarilloMineroBrillante,
                strokeWidth = 2.dp,
                modifier = Modifier.size(28.dp)
            )
            errorPlantas != null -> {
                Text(errorPlantas.orEmpty(), color = ErrorRojo, fontSize = 12.sp)
                TextButton(onClick = { recargarPlantas++ }) {
                    Text("REINTENTAR", color = AmarilloMineroBrillante)
                }
            }
            plantas.isEmpty() -> Text(
                "No hay plantas registradas.",
                color = TextoGrisSecundario,
                fontSize = 13.sp
            )
            else -> SeccionPlantaCard(
                listaPlantas = plantas,
                esConfirmado = esConfirmado,
                plantaConfirmada = plantaConfirmada,
                onConfirmar = { planta ->
                    plantaConfirmada = planta
                    esConfirmado = true
                    error = null
                },
                onCambiar = {
                    esConfirmado = false
                    plantaConfirmada = null
                    resultado = null
                }
            )
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = ColorGrisBorde)
        Spacer(Modifier.height(20.dp))

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

        Button(
            onClick = { calcular() },
            enabled = !calculando,
            colors = ButtonDefaults.buttonColors(
                containerColor = AmarilloMineroBrillante,
                contentColor = FondoNegroAbsoluto
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (calculando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = FondoNegroAbsoluto,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "CALCULAR LIQUIDACIÓN",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 1.2.sp
                )
            }
        }

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