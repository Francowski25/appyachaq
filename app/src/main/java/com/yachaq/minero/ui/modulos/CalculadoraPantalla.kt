package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.delay
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculadoraPantalla(onIrATerminos: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val usuarioActual = SesionUsuario.usuarioActual
    // Minero: cualquier usuario con sesión que no sea administrador
    val esMinero = usuarioActual != null && !SesionUsuario.esAdmin()

    var plantas by remember { mutableStateOf<List<ResponsePlant>>(emptyList()) }
    var plantasPersonales by remember { mutableStateOf<List<ResponseUserPlant>>(emptyList()) }
    var idsFavoritos by remember { mutableStateOf<Set<String>>(emptySet()) }
    var cargandoPlantas by remember { mutableStateOf(true) }
    var errorPlantas by remember { mutableStateOf<String?>(null) }
    var recargarPlantas by remember { mutableIntStateOf(0) }

    // Elegir una planta la confirma: ya no hay estado "esConfirmado"
    var plantaConfirmada by remember { mutableStateOf<PlantaCalculo?>(null) }

    var tonelaje by remember { mutableStateOf("") }
    var leyCu by remember { mutableStateOf("") }
    var leyAg by remember { mutableStateOf("") }
    var leyAu by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var resultado by remember { mutableStateOf<ResponseCalculo?>(null) }
    var calculando by remember { mutableStateOf(false) }
    var mostrarDialogoPremium by remember { mutableStateOf(false) }

    val resultadoRequester = remember { BringIntoViewRequester() }

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

    // Al aparecer el resultado, la pantalla baja hasta él
    LaunchedEffect(resultado) {
        if (resultado != null) {
            delay(80)
            resultadoRequester.bringIntoView()
        }
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
        focus.clearFocus()
        if (!SesionUsuario.puedeRealizarConsulta()) {
            mostrarDialogoPremium = true
            return
        }

        val t = tonelaje.replace(',', '.').toDoubleOrNull()
        val lCu = parseOpcional(leyCu)
        val lAg = parseOpcional(leyAg)
        val lAu = parseOpcional(leyAu)
        val planta = plantaConfirmada
        val idUser = usuarioActual?.idUser

        when {
            idUser.isNullOrBlank() -> error = "Sesión no válida. Vuelva a iniciar sesión."
            planta == null || planta.id.isBlank() -> error = "Primero elige una planta procesadora."
            t == null || lCu == null || lAg == null || lAu == null ->
                error = "Completa los campos con números válidos."
            t <= 0 -> error = "El tonelaje debe ser mayor que cero."
            lCu < 0 || lAg < 0 || lAu < 0 -> error = "Los valores no pueden ser negativos."
            lCu <= 0 && lAg <= 0 && lAu <= 0 -> error = "Ingresa la ley de al menos un metal."
            else -> scope.launch {
                calculando = true
                error = null
                resultado = null
                try {
                    val resp = RetrofitClient.calculationApi.calcular(
                        RequestCalculo(
                            idUser = idUser,
                            idPlant = if (planta.esPersonal) null else planta.id,
                            idUserPlant = if (planta.esPersonal) planta.id else null,
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

    // Cualquier cambio en los datos invalida el resultado anterior
    fun alEditar(actualizar: () -> Unit) {
        actualizar()
        resultado = null
        error = null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Calculadora",
                    color = AmarilloMineroBrillante,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text("Liquidación de concentrado", color = TextoGrisSecundario, fontSize = 13.sp)
            }
            if (usuarioActual?.role == "minero" && !SesionUsuario.esPremium()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GrisCampos,
                    border = BorderStroke(1.dp, ColorGrisBorde)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmarilloMineroBrillante,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${SesionUsuario.consultasRestantes()} gratis",
                            color = TextoGrisSecundario,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        PreciosMercadoCard()

        // Paso 1: planta
        TarjetaPaso(numero = 1, titulo = "PLANTA PROCESADORA") {
            when {
                cargandoPlantas -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = AmarilloMineroBrillante,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                    Text("Cargando plantas…", color = TextoGrisSecundario, fontSize = 13.sp)
                }
                errorPlantas != null && plantasCalculo.isEmpty() -> {
                    MensajeError(errorPlantas.orEmpty())
                    OutlinedButton(
                        onClick = { recargarPlantas++ },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmarilloMineroBrillante),
                        border = BorderStroke(1.dp, AmarilloMineroBrillante),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Reintentar", fontWeight = FontWeight.Bold) }
                }
                // El botón solo existe para el minero; el admin solo ve el texto
                plantasCalculo.isEmpty() -> if (esMinero) {
                    Text(
                        "Aún no tienes plantas. Añade una para poder calcular.",
                        color = TextoGrisSecundario,
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = onIrATerminos,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmarilloMineroBrillante,
                            contentColor = FondoNegroAbsoluto
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ir a Mi Planta y Términos", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("No hay plantas registradas.", color = TextoGrisSecundario, fontSize = 13.sp)
                }
                else -> SelectorPlanta(
                    plantas = plantasCalculo,
                    seleccionada = plantaConfirmada,
                    onSeleccionar = { planta ->
                        // Al cambiar de planta se reinicia el resultado
                        if (planta != plantaConfirmada) resultado = null
                        plantaConfirmada = planta
                        error = null
                    }
                )
            }
        }

        // Paso 2: datos del material
        TarjetaPaso(numero = 2, titulo = "DATOS DEL MATERIAL") {
            CampoNumero(
                valor = tonelaje,
                onCambio = { alEditar { tonelaje = it } },
                etiqueta = "Tonelaje",
                unidad = "TM",
                modifier = Modifier.fillMaxWidth()
            )
            CampoNumero(
                valor = leyCu,
                onCambio = { alEditar { leyCu = it } },
                etiqueta = "Ley de Cobre (Cu)",
                unidad = "%",
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoNumero(
                    valor = leyAg,
                    onCambio = { alEditar { leyAg = it } },
                    etiqueta = "Ley de Plata (Ag)",
                    unidad = "oz/tm",
                    modifier = Modifier.weight(1f)
                )
                CampoNumero(
                    valor = leyAu,
                    onCambio = { alEditar { leyAu = it } },
                    etiqueta = "Ley de Oro (Au)",
                    unidad = "oz/tm",
                    modifier = Modifier.weight(1f),
                    ultimo = true
                )
            }
            Text(
                "Las leyes son opcionales, pero debes ingresar al menos una.",
                color = TextoGrisSecundario,
                fontSize = 11.sp
            )
        }

        error?.let { MensajeError(it) }

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
                .height(54.dp)
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
                    letterSpacing = 1.sp
                )
            }
        }

        resultado?.let { r ->
            ResultadoLiquidacion(
                resultado = r,
                planta = plantaConfirmada,
                modifier = Modifier.bringIntoViewRequester(resultadoRequester)
            )
        }

        Text(
            "Los precios de los metales se administran centralmente y se actualizan periódicamente.",
            color = TextoGrisSecundario,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
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
                    "Límite gratuito alcanzado",
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