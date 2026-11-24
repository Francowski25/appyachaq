package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestUserPlant
import com.yachaq.minero.data.remote.model.RequestUserTerms
import com.yachaq.minero.data.remote.model.ResponsePlant
import com.yachaq.minero.data.remote.model.ResponseUserPlant
import com.yachaq.minero.data.remote.model.ResponseUserTerms
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private enum class FiltroPlantas(val etiqueta: String) {
    TODAS("Todas"), SISTEMA("Favoritas"), PERSONALES("Personales")
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var filtro by remember { mutableStateOf(FiltroPlantas.TODAS) }
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
        val pasaFiltro = when (filtro) {
            FiltroPlantas.TODAS -> true
            FiltroPlantas.SISTEMA -> !it.esPersonal
            FiltroPlantas.PERSONALES -> it.esPersonal
        }
        pasaFiltro && (textoBusqueda.isBlank() ||
                it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                it.ubicacion.contains(textoBusqueda, ignoreCase = true))
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
                if (resp.isSuccessful) recargar++ else errorAccion = "No se pudo actualizar (${resp.code()})"
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
                if (resp.isSuccessful) recargar++ else errorAccion = "No se pudo restablecer (${resp.code()})"
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
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("Mis plantas", color = AmarilloMineroBrillante, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(
                    "Tus favoritas, tus plantas personales y las condiciones comerciales de cada una.",
                    color = TextoGrisSecundario,
                    fontSize = 13.sp
                )
            }
        }

        if (!cargando && todas.isNotEmpty()) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ResumenTile("TOTAL", todas.size, Modifier.weight(1f))
                    ResumenTile("FAVORITAS", todas.count { !it.esPersonal }, Modifier.weight(1f))
                    ResumenTile("PERSONALES", todas.count { it.esPersonal }, Modifier.weight(1f))
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Nueva planta", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = { mostrarDialogo = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AmarilloMineroBrillante),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmarilloMineroBrillante),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(Icons.Default.Factory, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Del sistema", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        errorAccion?.let { item { MensajeError(it) } }

        when {
            cargando -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AmarilloMineroBrillante)
                }
            }

            errorLista != null && todas.isEmpty() -> item {
                ErrorConReintento(errorLista.orEmpty()) { recargar++ }
            }

            else -> {
                // Errores parciales: no ocultan la lista que sí cargó
                errorLista?.let { msg -> item { ErrorConReintento(msg) { recargar++ } } }
                errorPlantasPersonales?.let { msg -> item { ErrorConReintento(msg) { recargar++ } } }

                if (todas.isEmpty()) {
                    item { TarjetaSinFavoritas(onClick = { mostrarDialogo = true }) }
                } else {
                    item {
                        OutlinedTextField(
                            value = textoBusqueda,
                            onValueChange = { textoBusqueda = it },
                            label = { Text("Buscar por nombre o ubicación") },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = AmarilloMineroBrillante) },
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
                                unfocusedTextColor = TextoBlancoSuave,
                                cursorColor = AmarilloMineroBrillante
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FiltroPlantas.values().forEach { f ->
                                FilterChip(
                                    selected = filtro == f,
                                    onClick = { filtro = f },
                                    label = { Text(f.etiqueta, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = GrisCampos,
                                        labelColor = TextoGrisSecundario,
                                        selectedContainerColor = AmarilloMineroBrillante,
                                        selectedLabelColor = FondoNegroAbsoluto
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = filtro == f,
                                        borderColor = ColorGrisBorde,
                                        selectedBorderColor = AmarilloMineroBrillante
                                    )
                                )
                            }
                        }
                    }

                    if (visibles.isEmpty()) {
                        item {
                            Text(
                                "No se encontraron plantas con ese filtro o búsqueda.",
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

    // Hoja con todas las plantas del sistema (agregar a favoritas)
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
        DialogoConfirmacion(
            titulo = "Eliminar planta personal",
            mensaje = "¿Eliminar la planta \"${planta.name ?: "Sin nombre"}\"? Esta acción eliminará tu registro personal y sus términos.",
            textoConfirmar = "ELIMINAR",
            cargando = eliminandoPlantaPersonal,
            error = errorAccion,
            onConfirmar = { eliminarPlantaPersonal(planta) },
            onDismiss = {
                if (!eliminandoPlantaPersonal) {
                    plantaPersonalPorEliminar = null
                    errorAccion = null
                }
            }
        )
    }

    plantaPorQuitar?.let { planta ->
        val personalizada = terminoPorPlanta[planta.idPlant]?.personalizado == true
        DialogoConfirmacion(
            titulo = "Quitar de favoritas",
            mensaje = "¿Quitar \"${planta.name ?: "esta planta"}\" de tus favoritas?" +
                    if (personalizada) "\n\nTus términos personalizados se conservan y se seguirán usando al calcular con esta planta." else "",
            textoConfirmar = "QUITAR",
            onConfirmar = {
                cambiarFavorita(planta, false)
                plantaPorQuitar = null
            },
            onDismiss = { plantaPorQuitar = null }
        )
    }

    plantaPorRestablecer?.let { planta ->
        DialogoConfirmacion(
            titulo = "Restablecer términos",
            mensaje = "¿Restablecer los términos de \"${planta.name ?: "esta planta"}\" a los valores por defecto?" +
                    "\n\nSe perderán tus ajustes personalizados. La planta seguirá en tus favoritas.",
            textoConfirmar = "RESTABLECER",
            onConfirmar = {
                restablecerTerminos(planta)
                plantaPorRestablecer = null
            },
            onDismiss = { plantaPorRestablecer = null }
        )
    }

    // Modal de términos: planta del sistema; al cerrar se recarga
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

    // Modal de términos: planta personal; al cerrar se recarga
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
private fun ResumenTile(etiqueta: String, valor: Int, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = FondoNegroSecundario,
        border = BorderStroke(1.dp, ColorGrisBorde),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(valor.toString(), color = AmarilloMineroBrillante, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(etiqueta, color = TextoGrisSecundario, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        }
    }
}

@Composable
private fun ErrorConReintento(mensaje: String, onReintentar: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MensajeError(mensaje)
        TextButton(onClick = onReintentar) {
            Text("REINTENTAR", color = AmarilloMineroBrillante, fontWeight = FontWeight.Bold)
        }
    }
}

// Un solo diálogo de confirmación para eliminar, quitar y restablecer
@Composable
private fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit,
    cargando: Boolean = false,
    error: String? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FondoNegroSecundario,
        shape = RoundedCornerShape(20.dp),
        title = { Text(titulo, color = AmarilloMineroBrillante, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(mensaje, color = TextoBlancoSuave, fontSize = 14.sp)
                error?.let { MensajeError(it) }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmar, enabled = !cargando) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ErrorRojo, strokeWidth = 2.dp)
                } else {
                    Text(textoConfirmar, color = ErrorRojo, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !cargando) {
                Text("CANCELAR", color = TextoGrisSecundario)
            }
        }
    )
}