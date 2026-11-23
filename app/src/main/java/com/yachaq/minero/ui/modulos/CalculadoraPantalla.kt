package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestCalculo
import com.yachaq.minero.data.remote.model.ResponseCalculo
import com.yachaq.minero.data.remote.model.ResponsePlant
import com.yachaq.minero.data.remote.model.ResponseUserPlant
import com.yachaq.minero.data.remote.model.SesionUsuario
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

// Un campo opcional vacío cuenta como 0; si tiene texto, debe ser un número válido
private fun parseOpcional(texto: String): Double? =
    if (texto.isBlank()) 0.0 else texto.replace(',', '.').toDoubleOrNull()

// Ids de las plantas que el minero marcó como favoritas
private suspend fun obtenerIdsFavoritos(idUser: String): Set<String> {
    val resp = RetrofitClient.userTermsApi.listar(idUser)
    if (!resp.isSuccessful) return emptySet()
    return resp.body().orEmpty()
        .filter { it.type == "SUCCESS" && it.favorita == true }
        .mapNotNull { it.idPlant }
        .toSet()
}

@Composable
private fun CampoNumero(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    ultimo: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = if (ultimo) ImeAction.Done else ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = GrisCampos,
            unfocusedContainerColor = GrisCampos,
            focusedBorderColor = AmarilloMineroBrillante,
            unfocusedBorderColor = ColorGrisBorde,
            focusedLabelColor = AmarilloMineroBrillante,
            unfocusedLabelColor = TextoGrisSecundario,
            focusedTextColor = TextoBlancoSuave,
            unfocusedTextColor = TextoBlancoSuave,
            cursorColor = AmarilloMineroBrillante
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    )
}

@Composable
private fun EtiquetaSeccion(texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .background(AmarilloMineroBrillante, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = texto,
            color = TextoGrisSecundario,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SeccionCamposLiquidacion(
    tonelaje: String, onTonelaje: (String) -> Unit,
    leyCu: String, onLeyCu: (String) -> Unit,
    leyAg: String, onLeyAg: (String) -> Unit,
    leyAu: String, onLeyAu: (String) -> Unit,
    error: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CampoNumero(tonelaje, onTonelaje, "Tonelaje (TM)", Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        CampoNumero(leyCu, onLeyCu, "Ley Cu % (opcional)", Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        CampoNumero(leyAg, onLeyAg, "Ley Ag oz/tm (opcional)", Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        CampoNumero(leyAu, onLeyAu, "Ley Au oz/tm (opcional)", Modifier.fillMaxWidth(), ultimo = true)

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = ErrorRojo, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CalculadoraPantalla() {
    val scope = rememberCoroutineScope()
    val usuarioActual = SesionUsuario.usuarioActual
    // Minero: cualquier usuario con sesión que no sea administrador
    val esMinero = usuarioActual != null && !SesionUsuario.esAdmin()

    var plantas by remember { mutableStateOf<List<ResponsePlant>>(emptyList()) }
    var plantasPersonales by remember { mutableStateOf<List<ResponseUserPlant>>(emptyList()) }
    var idsFavoritos by remember { mutableStateOf<Set<String>>(emptySet()) }
    var cargandoPlantas by remember { mutableStateOf(true) }
    var errorPlantas by remember { mutableStateOf<String?>(null) }
    var recargarPlantas by remember { mutableIntStateOf(0) }

    var plantaConfirmada by remember { mutableStateOf<PlantaCalculo?>(null) }
    var esConfirmado by remember { mutableStateOf(false) }

    var tonelaje by remember { mutableStateOf("") }
    var leyCu by remember { mutableStateOf("") }
    var leyAg by remember { mutableStateOf("") }
    var leyAu by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var resultado by remember { mutableStateOf<ResponseCalculo?>(null) }
    var calculando by remember { mutableStateOf(false) }

    var mostrarDialogoPremium by remember { mutableStateOf(false) }

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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorPlantas = "No se pudo conectar: ${e.message}"
        }

        // Favoritas y plantas personales: si fallan no se bloquea la calculadora
        val idUser = usuarioActual?.idUser
        if (esMinero && !idUser.isNullOrBlank()) {
            idsFavoritos = try {
                obtenerIdsFavoritos(idUser)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptySet()
            }
            plantasPersonales = try {
                val resp = RetrofitClient.userPlantApi.listarPlantas()
                // El filtro descarta el elemento de error que el backend devuelve dentro de la lista
                if (resp.isSuccessful) resp.body().orEmpty().filter { it.idUserPlant != null } else emptyList()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            }
        }
        cargandoPlantas = false
    }

    // Minero: sus favoritas del sistema + sus plantas personales. Admin: todas las del sistema.
    val plantasCalculo = remember(plantas, idsFavoritos, plantasPersonales, esMinero) {
        if (esMinero) {
            val favoritas = plantas.mapNotNull { p ->
                val id = p.idPlant
                if (id != null && id in idsFavoritos) {
                    PlantaCalculo(
                        id = id,
                        nombre = p.name ?: "Sin nombre",
                        ubicacion = p.location ?: "",
                        alquiler = p.rentalCostPerTM ?: 0.0,
                        esPersonal = false,
                        favorita = true
                    )
                } else null
            }
            val personales = plantasPersonales.mapNotNull { p ->
                val id = p.idUserPlant ?: return@mapNotNull null
                PlantaCalculo(
                    id = id,
                    nombre = p.name ?: "Sin nombre",
                    ubicacion = p.location ?: "",
                    alquiler = p.rentalCostPerTM ?: 0.0,
                    esPersonal = true,
                    favorita = false
                )
            }
            favoritas + personales
        } else {
            plantas.mapNotNull { p ->
                val id = p.idPlant ?: return@mapNotNull null
                PlantaCalculo(
                    id = id,
                    nombre = p.name ?: "Sin nombre",
                    ubicacion = p.location ?: "",
                    alquiler = p.rentalCostPerTM ?: 0.0,
                    esPersonal = false,
                    favorita = false
                )
            }
        }
    }

    fun calcular() {
        if (!SesionUsuario.puedeRealizarConsulta()) {
            mostrarDialogoPremium = true
            return
        }

        val t = tonelaje.replace(',', '.').toDoubleOrNull()
        val lCu = parseOpcional(leyCu)
        val lAg = parseOpcional(leyAg)
        val lAu = parseOpcional(leyAu)
        val planta = plantaConfirmada
        val idPlant = planta?.id
        val idUser = usuarioActual?.idUser

        when {
            idUser.isNullOrBlank() -> error = "Sesión no válida. Vuelva a iniciar sesión."
            !esConfirmado -> error = "Por favor seleccione y confirme una planta procesadora primero."
            idPlant.isNullOrBlank() -> error = "La planta seleccionada no es válida."
            t == null || lCu == null || lAg == null || lAu == null ->
                error = "Por favor complete los campos con números válidos."
            t <= 0 -> error = "El tonelaje debe ser mayor que cero."
            lCu < 0 || lAg < 0 || lAu < 0 -> error = "Los valores no pueden ser negativos."
            lCu <= 0 && lAg <= 0 && lAu <= 0 -> error = "Ingrese la ley de al menos un metal."
            else -> scope.launch {
                calculando = true
                error = null
                resultado = null
                try {
                    val resp = RetrofitClient.calculationApi.calcular(
                        RequestCalculo(
                            idUser = idUser,
                            idPlant = if (planta.esPersonal) null else idPlant,
                            idUserPlant = if (planta.esPersonal) idPlant else null,
                            material = "Cobre + Plata + Oro",
                            tonelaje = t,
                            leyCu = lCu,
                            leyAg = lAg,
                            leyAu = lAu
                        )
                    )
                    val body = resp.body()
                    if (resp.isSuccessful && body?.type == "SUCCESS") {
                        resultado = body
                        SesionUsuario.registrarConsulta()
                    } else {
                        error = body?.listMessage?.firstOrNull() ?: "Error del servidor (${resp.code()})"
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    error = "No se pudo conectar: ${e.message}"
                } finally {
                    calculando = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = AmarilloMineroBrillante,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = "CALCULADORA DE LIQUIDACIÓN",
                color = AmarilloMineroBrillante,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
        }

        if (usuarioActual?.role == "minero" && !SesionUsuario.esPremium()) {
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = GrisCampos,
                border = BorderStroke(1.dp, ColorGrisBorde)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AmarilloMineroBrillante,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Consultas gratuitas restantes: ${SesionUsuario.consultasRestantes()}",
                        color = TextoGrisSecundario,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        PreciosMercadoCard()

        Spacer(Modifier.height(24.dp))

        EtiquetaSeccion("PLANTA PROCESADORA")
        Spacer(Modifier.height(10.dp))

        when {
            cargandoPlantas -> CircularProgressIndicator(
                color = AmarilloMineroBrillante,
                strokeWidth = 2.dp,
                modifier = Modifier.size(28.dp)
            )
            errorPlantas != null && plantasCalculo.isEmpty() -> {
                Text(errorPlantas.orEmpty(), color = ErrorRojo, fontSize = 12.sp)
                TextButton(onClick = { recargarPlantas++ }) {
                    Text("REINTENTAR", color = AmarilloMineroBrillante)
                }
            }
            plantasCalculo.isEmpty() -> Text(
                if (esMinero) "Aún no tienes plantas. Añádelas en Mis términos por planta."
                else "No hay plantas registradas.",
                color = TextoGrisSecundario,
                fontSize = 13.sp
            )
            else -> SeccionPlantaCard(
                listaPlantas = plantasCalculo,
                esConfirmado = esConfirmado,
                plantaConfirmada = plantaConfirmada,
                onConfirmar = { planta -> plantaConfirmada = planta; esConfirmado = true; error = null },
                onCambiar = { esConfirmado = false; plantaConfirmada = null; resultado = null },
                esMinero = esMinero
            )
        }

        Spacer(Modifier.height(24.dp))
        EtiquetaSeccion("DATOS DEL MATERIAL")
        Spacer(Modifier.height(10.dp))

        SeccionCamposLiquidacion(
            tonelaje = tonelaje, onTonelaje = { tonelaje = it; resultado = null; error = null },
            leyCu = leyCu, onLeyCu = { leyCu = it; resultado = null; error = null },
            leyAg = leyAg, onLeyAg = { leyAg = it; resultado = null; error = null },
            leyAu = leyAu, onLeyAu = { leyAu = it; resultado = null; error = null },
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
            modifier = Modifier.fillMaxWidth().height(54.dp)
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
            "Los precios de los metales se administran centralmente y se actualizan periódicamente.",
            color = TextoGrisSecundario,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }

    if (mostrarDialogoPremium) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPremium = false },
            icon = {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = AmarilloMineroBrillante,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    "Límite Gratuito Alcanzado",
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = TextoBlancoSuave
                )
            },
            text = {
                Text(
                    "Has alcanzado el límite de 10 consultas gratuitas. Actualiza a Plan Premium para continuar realizando cálculos ilimitados.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = TextoGrisSecundario
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        SesionUsuario.alternarPlanPremiumSimulado()
                        mostrarDialogoPremium = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloMineroBrillante,
                        contentColor = FondoNegroAbsoluto
                    )
                ) { Text("ACTIVAR PREMIUM (SIMULACIÓN)", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPremium = false }) {
                    Text("CERRAR", color = TextoGrisSecundario)
                }
            },
            containerColor = FondoNegroSecundario,
            shape = RoundedCornerShape(20.dp)
        )
    }
}