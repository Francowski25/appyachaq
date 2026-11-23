package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestUserPlant
import com.yachaq.minero.data.remote.model.RequestUserTerms
import com.yachaq.minero.data.remote.model.ResponsePlant
import com.yachaq.minero.data.remote.model.ResponseUserPlant
import com.yachaq.minero.data.remote.model.ResponseUserTerms
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.Locale

// Modelo común para dibujar plantas del sistema y personales con la misma tarjeta
data class PlantaUi(
    val clave: String,
    val nombre: String,
    val ubicacion: String,
    val estado: String?,
    val capacidad: Double?,
    val alquiler: Double?,
    val maquila: Double?,
    val analisis: Double?,
    val esPersonal: Boolean,
    val personalizado: Boolean,
    val sistema: ResponsePlant? = null,
    val personal: ResponseUserPlant? = null
)

@Composable
fun MisTerminosPantalla(idUser: String) {
    val scope = rememberCoroutineScope()

    var plantas by remember { mutableStateOf<List<ResponsePlant>>(emptyList()) }
    var plantasPersonales by remember { mutableStateOf<List<ResponseUserPlant>>(emptyList()) }
    var terminos by remember { mutableStateOf<List<ResponseUserTerms>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorLista by remember { mutableStateOf<String?>(null) }
    var errorPlantasPersonales by remember { mutableStateOf<String?>(null) }
    var errorAccion by remember { mutableStateOf<String?>(null) }
    var recargar by remember { mutableIntStateOf(0) }

    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var plantaSeleccionadaTerminos by remember { mutableStateOf<ResponsePlant?>(null) }
    var plantaPersonalTerminos by remember { mutableStateOf<ResponseUserPlant?>(null) }
    var plantaPorQuitar by remember { mutableStateOf<ResponsePlant?>(null) }
    var plantaPorRestablecer by remember { mutableStateOf<ResponsePlant?>(null) }
    var mostrarFormularioPersonal by remember { mutableStateOf(false) }
    var plantaPersonalEditando by remember { mutableStateOf<ResponseUserPlant?>(null) }
    var plantaPersonalPorEliminar by remember { mutableStateOf<ResponseUserPlant?>(null) }
    var guardandoPlantaPersonal by remember { mutableStateOf(false) }
    var eliminandoPlantaPersonal by remember { mutableStateOf(false) }

    // Mantiene la carga de favoritas independiente de la disponibilidad de plantas personales.
    LaunchedEffect(recargar) {
        if (plantas.isEmpty() && plantasPersonales.isEmpty()) cargando = true
        errorLista = null
        try {
            val respPlantas = RetrofitClient.plantApi.listarPlantas()
            val respTerminos = RetrofitClient.userTermsApi.listar(idUser)
            if (respPlantas.isSuccessful && respTerminos.isSuccessful) {
                plantas = respPlantas.body().orEmpty()
                terminos = respTerminos.body().orEmpty().filter { it.idPlant != null }
            } else {
                val codigo = if (!respPlantas.isSuccessful) respPlantas.code() else respTerminos.code()
                errorLista = "Error al cargar las plantas ($codigo)"
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorLista = "No se pudo conectar: ${e.message}"
        }

        try {
            val respPersonales = RetrofitClient.userPlantApi.listarPlantas()
            if (respPersonales.isSuccessful) {
                plantasPersonales = respPersonales.body().orEmpty()
                errorPlantasPersonales = null
            } else {
                errorPlantasPersonales = "Error al cargar tus plantas personales (${respPersonales.code()})"
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorPlantasPersonales = "No se pudieron cargar tus plantas personales: ${e.message}"
        } finally {
            cargando = false
        }
    }

    val terminoPorPlanta = remember(terminos) { terminos.associateBy { it.idPlant } }
    val favoritas = remember(plantas, terminos) {
        plantas.filter { terminoPorPlanta[it.idPlant]?.favorita == true }
    }
    val idsFavoritas = remember(favoritas) { favoritas.mapNotNull { it.idPlant }.toSet() }

    // Lista única: favoritas del sistema + plantas personales
    val todas = remember(favoritas, terminoPorPlanta, plantasPersonales) {
        val delSistema = favoritas.map { p ->
            val t = terminoPorPlanta[p.idPlant]
            PlantaUi(
                clave = "S-${p.idPlant ?: p.hashCode()}",
                nombre = p.name ?: "Sin nombre",
                ubicacion = p.location ?: "Ubicación no especificada",
                estado = p.status,
                capacidad = p.capacityTMAD,
                alquiler = p.rentalCostPerTM,
                maquila = t?.maquila,
                analisis = t?.analisis,
                esPersonal = false,
                personalizado = t?.personalizado == true,
                sistema = p
            )
        }
        val personales = plantasPersonales.map { p ->
            PlantaUi(
                clave = "P-${p.idUserPlant ?: p.hashCode()}",
                nombre = p.name ?: "Sin nombre",
                ubicacion = p.location ?: "Ubicación no especificada",
                estado = p.status,
                capacidad = p.capacityTMAD,
                alquiler = p.rentalCostPerTM,
                maquila = p.maquila,
                analisis = p.analisis,
                esPersonal = true,
                personalizado = false,
                personal = p
            )
        }
        delSistema + personales
    }

    val visibles = todas.filter {
        textoBusqueda.isBlank() ||
                it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                it.ubicacion.contains(textoBusqueda, ignoreCase = true)
    }

    // Marcar o quitar favorita (solo envía idUser, idPlant y favorita; no toca los términos)
    fun cambiarFavorita(planta: ResponsePlant, marcar: Boolean) {
        val idPlant = planta.idPlant ?: return
        scope.launch {
            errorAccion = null
            try {
                val resp = RetrofitClient.userTermsApi.guardar(
                    RequestUserTerms(idUser = idUser, idPlant = idPlant, favorita = marcar)
                )
                if (resp.isSuccessful) {
                    recargar++
                } else {
                    errorAccion = "No se pudo actualizar (${resp.code()})"
                }
            } catch (e: Exception) {
                errorAccion = "No se pudo conectar: ${e.message}"
            }
        }
    }

    // Restablecer los términos de una planta a los valores por defecto (la favorita se conserva)
    fun restablecerTerminos(planta: ResponsePlant) {
        val idPlant = planta.idPlant ?: return
        scope.launch {
            errorAccion = null
            try {
                val resp = RetrofitClient.userTermsApi.restablecer(
                    RequestUserTerms(idUser = idUser, idPlant = idPlant)
                )
                if (resp.isSuccessful) {
                    recargar++
                } else {
                    errorAccion = "No se pudo restablecer (${resp.code()})"
                }
            } catch (e: Exception) {
                errorAccion = "No se pudo conectar: ${e.message}"
            }
        }
    }

    fun guardarPlantaPersonal(request: RequestUserPlant) {
        val plantaExistente = plantaPersonalEditando
        val id = plantaExistente?.idUserPlant
        if (plantaExistente != null && id == null) {
            errorAccion = "No se puede editar la planta: no tiene un identificador disponible."
            return
        }
        scope.launch {
            guardandoPlantaPersonal = true
            errorAccion = null
            try {
                val resp = if (id == null) {
                    RetrofitClient.userPlantApi.crearPlanta(request)
                } else {
                    RetrofitClient.userPlantApi.actualizarPlanta(id, request)
                }
                val body = resp.body()
                if (resp.isSuccessful && (body?.type == null || body.type == "SUCCESS")) {
                    mostrarFormularioPersonal = false
                    plantaPersonalEditando = null
                    recargar++
                } else {
                    errorAccion = body?.listMessage?.firstOrNull()
                        ?: "No se pudo guardar la planta (${resp.code()})"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorAccion = "No se pudo conectar: ${e.message}"
            } finally {
                guardandoPlantaPersonal = false
            }
        }
    }

    fun eliminarPlantaPersonal(planta: ResponseUserPlant) {
        val id = planta.idUserPlant
        if (id == null) {
            errorAccion = "No se puede eliminar la planta: no tiene un identificador disponible."
            return
        }
        scope.launch {
            eliminandoPlantaPersonal = true
            errorAccion = null
            try {
                val resp = RetrofitClient.userPlantApi.eliminarPlanta(id)
                val body = resp.body()
                if (resp.isSuccessful && (body?.type == null || body.type == "SUCCESS")) {
                    plantasPersonales = plantasPersonales.filterNot { it.idUserPlant == id }
                    plantaPersonalPorEliminar = null
                    recargar++
                } else {
                    errorAccion = body?.listMessage?.firstOrNull()
                        ?: "No se pudo eliminar la planta (${resp.code()})"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorAccion = "No se pudo conectar: ${e.message}"
            } finally {
                eliminandoPlantaPersonal = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "MIS TÉRMINOS POR PLANTA",
                    color = AmarilloMineroBrillante,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Marca tus plantas favoritas y configura tus condiciones comerciales.",
                    color = TextoGrisSecundario,
                    fontSize = 12.sp
                )
                errorAccion?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = ErrorRojo, fontSize = 12.sp)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        plantaPersonalEditando = null
                        errorAccion = null
                        mostrarFormularioPersonal = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloMineroBrillante,
                        contentColor = FondoNegroAbsoluto
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Añadir planta", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { mostrarDialogo = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AmarilloMineroBrillante),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmarilloMineroBrillante),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Factory, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Añadir planta del sistema", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        when {
            cargando -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AmarilloMineroBrillante)
                }
            }

            errorLista != null && todas.isEmpty() -> item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(errorLista.orEmpty(), color = ErrorRojo, fontSize = 13.sp)
                    TextButton(onClick = { recargar++ }) {
                        Text("REINTENTAR", color = AmarilloMineroBrillante)
                    }
                }
            }

            else -> {
                // Errores parciales: no ocultan la lista que sí cargó
                errorLista?.let { msg ->
                    item { MensajeErrorReintentar(msg) { recargar++ } }
                }
                errorPlantasPersonales?.let { msg ->
                    item { MensajeErrorReintentar(msg) { recargar++ } }
                }

                if (todas.isEmpty()) {
                    item { TarjetaSinFavoritas(onClick = { mostrarDialogo = true }) }
                } else {
                    item {
                        OutlinedTextField(
                            value = textoBusqueda,
                            onValueChange = { textoBusqueda = it },
                            label = { Text("Buscar en mis plantas...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = AmarilloMineroBrillante
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
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
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (visibles.isEmpty()) {
                        item {
                            Text(
                                "No se encontraron plantas con ese nombre o ubicación.",
                                color = TextoGrisSecundario,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        items(items = visibles, key = { it.clave }) { planta ->
                            TarjetaPlantaMinero(
                                planta = planta,
                                onAjustar = {
                                    if (planta.esPersonal) plantaPersonalTerminos = planta.personal
                                    else plantaSeleccionadaTerminos = planta.sistema
                                },
                                onQuitarFavorita = { plantaPorQuitar = planta.sistema },
                                onRestablecer = { plantaPorRestablecer = planta.sistema },
                                onEditar = {
                                    plantaPersonalEditando = planta.personal
                                    errorAccion = null
                                    mostrarFormularioPersonal = true
                                },
                                onEliminar = {
                                    errorAccion = null
                                    plantaPersonalPorEliminar = planta.personal
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo con la lista de todas las plantas del sistema (solo detalles + agregar)
    if (mostrarDialogo) {
        DialogoListaPlantas(
            plantas = plantas,
            idsFavoritas = idsFavoritas,
            error = errorAccion,
            onAgregar = { planta -> cambiarFavorita(planta, true) },
            onDismiss = {
                mostrarDialogo = false
                errorAccion = null
            }
        )
    }

    if (mostrarFormularioPersonal) {
        DialogoPlantaPersonal(
            planta = plantaPersonalEditando,
            guardando = guardandoPlantaPersonal,
            error = errorAccion,
            onGuardar = ::guardarPlantaPersonal,
            onDismiss = {
                if (!guardandoPlantaPersonal) {
                    mostrarFormularioPersonal = false
                    plantaPersonalEditando = null
                    errorAccion = null
                }
            }
        )
    }

    plantaPersonalPorEliminar?.let { planta ->
        AlertDialog(
            onDismissRequest = { if (!eliminandoPlantaPersonal) plantaPersonalPorEliminar = null },
            containerColor = FondoNegroSecundario,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Eliminar planta personal", color = ErrorRojo, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "¿Eliminar la planta \"${planta.name ?: "Sin nombre"}\"? Esta acción eliminará tu registro personal y sus términos.",
                        color = TextoBlancoSuave,
                        fontSize = 14.sp
                    )
                    errorAccion?.let {
                        Text(it, color = ErrorRojo, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { eliminarPlantaPersonal(planta) },
                    enabled = !eliminandoPlantaPersonal
                ) {
                    if (eliminandoPlantaPersonal) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = ErrorRojo,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("ELIMINAR", color = ErrorRojo, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!eliminandoPlantaPersonal) {
                            plantaPersonalPorEliminar = null
                            errorAccion = null
                        }
                    },
                    enabled = !eliminandoPlantaPersonal
                ) {
                    Text("CANCELAR", color = TextoGrisSecundario)
                }
            }
        )
    }

    // Confirmación para quitar una favorita
    plantaPorQuitar?.let { planta ->
        val personalizada = terminoPorPlanta[planta.idPlant]?.personalizado == true
        AlertDialog(
            onDismissRequest = { plantaPorQuitar = null },
            containerColor = FondoNegroSecundario,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Quitar de favoritas", color = AmarilloMineroBrillante, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "¿Quitar \"${planta.name ?: "esta planta"}\" de tus favoritas?" +
                            if (personalizada) "\n\nTus términos personalizados se conservan y se seguirán usando al calcular con esta planta." else "",
                    color = TextoBlancoSuave,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    cambiarFavorita(planta, false)
                    plantaPorQuitar = null
                }) {
                    Text("QUITAR", color = ErrorRojo, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { plantaPorQuitar = null }) {
                    Text("CANCELAR", color = TextoGrisSecundario)
                }
            }
        )
    }

    // Confirmación para restablecer los términos de una favorita
    plantaPorRestablecer?.let { planta ->
        AlertDialog(
            onDismissRequest = { plantaPorRestablecer = null },
            containerColor = FondoNegroSecundario,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Restablecer términos", color = AmarilloMineroBrillante, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "¿Restablecer los términos de \"${planta.name ?: "esta planta"}\" a los valores por defecto?" +
                            "\n\nSe perderán tus ajustes personalizados. La planta seguirá en tus favoritas.",
                    color = TextoBlancoSuave,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    restablecerTerminos(planta)
                    plantaPorRestablecer = null
                }) {
                    Text("RESTABLECER", color = ErrorRojo, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { plantaPorRestablecer = null }) {
                    Text("CANCELAR", color = TextoGrisSecundario)
                }
            }
        )
    }

    // Modal de términos: planta del sistema (el que ya tienes); al cerrar se recarga
    plantaSeleccionadaTerminos?.let { planta ->
        val idPlant = planta.idPlant
        if (idPlant != null) {
            TerminosPlanta(
                idPlant = idPlant,
                nombrePlanta = planta.name ?: "",
                idUser = idUser,
                esAdmin = false,
                onDismiss = {
                    plantaSeleccionadaTerminos = null
                    recargar++
                }
            )
        } else {
            plantaSeleccionadaTerminos = null
        }
    }

    // Modal de términos: planta personal (PENDIENTE: depende de refactorizar TerminosPlanta)
    plantaPersonalTerminos?.let { planta ->
        val id = planta.idUserPlant
        if (id != null) {
            TerminosPlantaPersonal(
                idUserPlant = id,
                nombrePlanta = planta.name ?: "",
                onDismiss = {
                    plantaPersonalTerminos = null
                    recargar++
                }
            )
        } else {
            plantaPersonalTerminos = null
        }
    }
}

@Composable
private fun MensajeErrorReintentar(mensaje: String, onReintentar: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(mensaje, color = ErrorRojo, fontSize = 13.sp)
        TextButton(onClick = onReintentar) {
            Text("REINTENTAR", color = AmarilloMineroBrillante)
        }
    }
}

// ========================================================
// CARD CUANDO NO TIENE PLANTAS
// ========================================================
@Composable
fun TarjetaSinFavoritas(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ColorGrisBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AmarilloMineroBrillante.copy(alpha = 0.5f),
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "Aún no tienes plantas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextoBlancoSuave,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Añade una planta personal o elige de la lista las plantas del sistema con las que trabajas.",
                fontSize = 12.sp,
                color = TextoGrisSecundario,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmarilloMineroBrillante,
                    contentColor = FondoNegroAbsoluto
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver lista de plantas", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ========================================================
// TARJETA ÚNICA (sistema y personal): mismo formato, acciones distintas
// ========================================================
@Composable
fun TarjetaPlantaMinero(
    planta: PlantaUi,
    onAjustar: () -> Unit,
    onQuitarFavorita: () -> Unit,
    onRestablecer: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            if (planta.esPersonal) ColorGrisBorde else AmarilloMineroBrillante.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Factory,
                        contentDescription = null,
                        tint = if (planta.esPersonal) TextoGrisSecundario else AmarilloMineroBrillante,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        planta.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoSuave,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!planta.esPersonal) {
                    IconButton(onClick = onQuitarFavorita) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Quitar de favoritas",
                            tint = AmarilloMineroBrillante
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                EtiquetaEstado(planta.estado)
                if (planta.esPersonal) {
                    Surface(shape = RoundedCornerShape(8.dp), color = TextoGrisSecundario.copy(alpha = 0.2f)) {
                        Text(
                            "PERSONAL",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoBlancoSuave
                        )
                    }
                }
                if (planta.personalizado) {
                    Surface(shape = RoundedCornerShape(8.dp), color = AmarilloMinero.copy(alpha = 0.2f)) {
                        Text(
                            "PERSONALIZADO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmarilloMineroBrillante
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextoGrisSecundario, modifier = Modifier.size(16.dp))
                Text(
                    planta.ubicacion,
                    fontSize = 12.sp,
                    color = TextoGrisSecundario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ColorGrisBorde)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Capacidad:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        text = "${planta.capacidad ?: 0.0} TMAD",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Alquiler / TM:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        text = String.format(Locale.US, "$ %.2f", planta.alquiler ?: 0.0),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmarilloMineroBrillante
                    )
                }
            }

            // Términos que realmente se usan (los del minero o los por defecto)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Maquila:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        text = String.format(Locale.US, "$ %.2f", planta.maquila ?: 0.0),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave
                    )
                }
                Column {
                    Text("Análisis:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        text = String.format(Locale.US, "$ %.2f", planta.analisis ?: 0.0),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave
                    )
                }
                OutlinedButton(
                    onClick = onAjustar,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AmarilloMineroBrillante),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmarilloMineroBrillante),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajustar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Acciones propias de cada origen
            if (planta.esPersonal) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditar,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ColorGrisBorde),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmarilloMineroBrillante),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Editar", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onEliminar,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ErrorRojo.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRojo),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Eliminar", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (planta.personalizado) {
                // Solo si tiene términos personalizados hay algo que restablecer
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = onRestablecer,
                        colors = ButtonDefaults.textButtonColors(contentColor = TextoGrisSecundario),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restablecer términos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ========================================================
// DIÁLOGO: LISTA DE PLANTAS DEL SISTEMA (solo detalles + agregar)
// ========================================================
@Composable
fun DialogoListaPlantas(
    plantas: List<ResponsePlant>,
    idsFavoritas: Set<String>,
    error: String?,
    onAgregar: (ResponsePlant) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = FondoNegroAbsoluto,
            border = BorderStroke(1.dp, ColorGrisBorde)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Plantas disponibles",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AmarilloMineroBrillante
                )
                Text(
                    "Agrega a favoritas las plantas con las que trabajas.",
                    fontSize = 12.sp,
                    color = TextoGrisSecundario
                )
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = ErrorRojo, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (plantas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No hay plantas registradas.", color = TextoGrisSecundario, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(items = plantas, key = { it.idPlant ?: it.hashCode() }) { planta ->
                            TarjetaPlantaLista(
                                planta = planta,
                                yaFavorita = planta.idPlant in idsFavoritas,
                                onAgregar = { onAgregar(planta) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGrisBorde)
                ) {
                    Text("Cerrar", color = TextoBlancoSuave)
                }
            }
        }
    }
}

@Composable
fun TarjetaPlantaLista(
    planta: ResponsePlant,
    yaFavorita: Boolean,
    onAgregar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (yaFavorita) AmarilloMineroBrillante.copy(alpha = 0.5f) else ColorGrisBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Factory, contentDescription = null, tint = AmarilloMineroBrillante, modifier = Modifier.size(24.dp))
                    Text(planta.name ?: "Sin nombre", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave)
                }
                EtiquetaEstado(planta.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextoGrisSecundario, modifier = Modifier.size(16.dp))
                Text(planta.location ?: "Ubicación no especificada", fontSize = 12.sp, color = TextoGrisSecundario)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ColorGrisBorde)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Capacidad:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        text = "${planta.capacityTMAD ?: 0.0} TMAD",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave
                    )
                }
                Column {
                    Text("Alquiler / TM:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        text = String.format(Locale.US, "$ %.2f", planta.rentalCostPerTM ?: 0.0),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmarilloMineroBrillante
                    )
                }

                if (yaFavorita) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AmarilloMineroBrillante, modifier = Modifier.size(16.dp))
                        Text("Agregada", fontSize = 12.sp, color = AmarilloMineroBrillante, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onAgregar,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmarilloMineroBrillante,
                            contentColor = FondoNegroAbsoluto
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EtiquetaEstado(status: String?) {
    val estado = status ?: "Sin estado"
    val esOperativa = estado.equals("Operativa", ignoreCase = true)
    val color = if (esOperativa) Color(0xFF4CAF50) else Color(0xFFFF9800)
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.2f)) {
        Text(
            estado,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}