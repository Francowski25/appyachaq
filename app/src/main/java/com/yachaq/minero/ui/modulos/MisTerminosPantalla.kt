package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.RequestUserTerms
import com.yachaq.minero.data.remote.model.ResponsePlant
import com.yachaq.minero.data.remote.model.ResponseUserTerms
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MisTerminosPantalla(idUser: String) {
    val scope = rememberCoroutineScope()

    var plantas by remember { mutableStateOf<List<ResponsePlant>>(emptyList()) }
    var terminos by remember { mutableStateOf<List<ResponseUserTerms>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorLista by remember { mutableStateOf<String?>(null) }
    var errorAccion by remember { mutableStateOf<String?>(null) }
    var recargar by remember { mutableIntStateOf(0) }

    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var plantaSeleccionadaTerminos by remember { mutableStateOf<ResponsePlant?>(null) }
    var plantaPorQuitar by remember { mutableStateOf<ResponsePlant?>(null) }
    var plantaPorRestablecer by remember { mutableStateOf<ResponsePlant?>(null) }

    // 1. Carga las plantas (datos generales) y los términos del usuario (favorita / personalizado)
    LaunchedEffect(recargar) {
        if (plantas.isEmpty()) cargando = true
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
        } catch (e: Exception) {
            errorLista = "No se pudo conectar: ${e.message}"
        } finally {
            cargando = false
        }
    }

    val terminoPorPlanta = remember(terminos) { terminos.associateBy { it.idPlant } }
    val favoritas = remember(plantas, terminos) {
        plantas.filter { terminoPorPlanta[it.idPlant]?.favorita == true }
    }
    val idsFavoritas = remember(favoritas) { favoritas.mapNotNull { it.idPlant }.toSet() }

    val favoritasVisibles = favoritas.filter {
        textoBusqueda.isBlank() ||
                (it.name ?: "").contains(textoBusqueda, ignoreCase = true) ||
                (it.location ?: "").contains(textoBusqueda, ignoreCase = true)
    }

    // 2. Marcar o quitar favorita (solo envía idUser, idPlant y favorita; no toca los términos)
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

    // 3. Restablecer los términos de una planta a los valores por defecto (la favorita se conserva)
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

        when {
            cargando -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AmarilloMineroBrillante)
                }
            }

            errorLista != null -> item {
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

            // Sin favoritas: card con botón para abrir la lista de plantas
            favoritas.isEmpty() -> item {
                TarjetaSinFavoritas(onClick = { mostrarDialogo = true })
            }

            // Con favoritas: buscador, lista y botón para agregar más
            else -> {
                item {
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        label = { Text("Buscar en mis favoritas...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AmarilloMineroBrillante) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GrisCampos, unfocusedContainerColor = GrisCampos,
                            focusedBorderColor = AmarilloMineroBrillante, unfocusedBorderColor = ColorGrisBorde,
                            focusedLabelColor = AmarilloMineroBrillante, unfocusedLabelColor = TextoGrisSecundario,
                            focusedTextColor = TextoBlancoSuave, unfocusedTextColor = TextoBlancoSuave
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (favoritasVisibles.isEmpty()) {
                    item {
                        Text("No se encontraron favoritas con ese nombre.", color = TextoGrisSecundario, fontSize = 13.sp)
                    }
                } else {
                    items(items = favoritasVisibles, key = { it.idPlant ?: it.hashCode() }) { planta ->
                        TarjetaFavorita(
                            planta = planta,
                            termino = terminoPorPlanta[planta.idPlant],
                            onQuitar = { plantaPorQuitar = planta },
                            onAjustar = { plantaSeleccionadaTerminos = planta },
                            onRestablecer = { plantaPorRestablecer = planta }
                        )
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { mostrarDialogo = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AmarilloMineroBrillante),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmarilloMineroBrillante),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar más plantas", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Diálogo con la lista de todas las plantas (solo detalles + agregar)
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

    // Modal para editar términos (el que ya tienes); al cerrar se recarga para actualizar las etiquetas
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
}

// ========================================================
// CARD CUANDO NO TIENE FAVORITAS
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
                text = "Aún no tienes plantas favoritas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextoBlancoSuave,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Elige de la lista las plantas con las que trabajas para ajustar tus términos.",
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
// TARJETA DE FAVORITA (con sus términos, Ajustar y Restablecer)
// ========================================================
@Composable
fun TarjetaFavorita(
    planta: ResponsePlant,
    termino: ResponseUserTerms?,
    onQuitar: () -> Unit,
    onAjustar: () -> Unit,
    onRestablecer: () -> Unit
) {
    val esPersonalizado = termino?.personalizado == true

    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, AmarilloMineroBrillante.copy(alpha = 0.5f)),
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
                    Text(planta.name ?: "Sin nombre", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave)
                }
                IconButton(onClick = onQuitar) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Quitar de favoritas",
                        tint = AmarilloMineroBrillante
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                EtiquetaEstado(planta.status)
                if (esPersonalizado) {
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
                Text(planta.location ?: "Ubicación no especificada", fontSize = 12.sp, color = TextoGrisSecundario)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ColorGrisBorde)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Capacidad:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        text = "${planta.capacityTMAD ?: 0.0} TMAD",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Alquiler / TM:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        text = String.format(Locale.US, "$ %.2f", planta.rentalCostPerTM ?: 0.0),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmarilloMineroBrillante
                    )
                }
            }

            // Términos que realmente se usan para este minero (los suyos o los por defecto)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Maquila:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        text = String.format(Locale.US, "$ %.2f", termino?.maquila ?: 0.0),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave
                    )
                }
                Column {
                    Text("Análisis:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(
                        text = String.format(Locale.US, "$ %.2f", termino?.analisis ?: 0.0),
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

            // Solo si tiene términos personalizados hay algo que restablecer
            if (esPersonalizado) {
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
// DIÁLOGO: LISTA DE PLANTAS (solo detalles + agregar)
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
private fun EtiquetaEstado(status: String?) {
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