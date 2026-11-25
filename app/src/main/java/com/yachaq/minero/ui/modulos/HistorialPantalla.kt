package com.yachaq.minero.ui.modulos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.data.remote.model.ResponseHistorial
import com.yachaq.minero.data.remote.model.SesionUsuario
import com.yachaq.minero.logica.ExportadorPdf
import com.yachaq.minero.logica.FormateadorFecha
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private fun dolares(v: Double) = "$ " + String.format(Locale.US, "%,.2f", v)

@Composable
fun HistorialPantalla() {
    if (SesionUsuario.esPremium()) HistorialContenido() else HistorialBloqueado()
}

@Composable
private fun HistorialBloqueado() {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = cs.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Historial exclusivo",
                style = tp.titleLarge,
                fontWeight = FontWeight.Black,
                color = cs.onSurface
            )
            Text(
                "El historial de liquidaciones está disponible para administradores y usuarios Premium.",
                style = tp.bodySmall,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { SesionUsuario.alternarPlanPremiumSimulado() },
                shape = MaterialTheme.shapes.medium
            ) {
                Text("ACTIVAR PREMIUM (SIMULACIÓN)", style = tp.labelLarge)
            }
        }
    }
}

@Composable
private fun HistorialContenido() {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes
    val idUser = SesionUsuario.usuarioActual?.idUser
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var exportandoId by remember { mutableStateOf<String?>(null) }
    var pendienteExportar by remember { mutableStateOf<ResponseHistorial?>(null) }
    var registroParaExportar by remember { mutableStateOf<ResponseHistorial?>(null) }

    val exportar: (ResponseHistorial) -> Unit = { reg ->
        if (exportandoId == null) {
            scope.launch {
                exportandoId = reg.idCalculation ?: ""
                try {
                    val nombre = withContext(Dispatchers.IO) {
                        ExportadorPdf.guardarEnDescargas(context, reg)
                    }
                    Toast.makeText(context, "PDF guardado en Descargas: $nombre", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "No se pudo exportar: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    exportandoId = null
                }
            }
        }
    }

    // Solo se usa en Android 7 a 9 (API 24-28)
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        val reg = pendienteExportar
        pendienteExportar = null
        if (concedido && reg != null) {
            exportar(reg)
        } else {
            Toast.makeText(context, "Se necesita permiso de almacenamiento para guardar el PDF", Toast.LENGTH_LONG).show()
        }
    }

    val iniciarExportacion: (ResponseHistorial) -> Unit = { reg ->
        val necesitaPermiso = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        if (necesitaPermiso) {
            pendienteExportar = reg
            permisoLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            exportar(reg)
        }
    }

    var registros by remember { mutableStateOf<List<ResponseHistorial>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var recargar by remember { mutableIntStateOf(0) }

    var mostrarDialogoEliminarGlobal by remember { mutableStateOf(false) }
    var eliminandoGlobal by remember { mutableStateOf(false) }
    var mensajeErrorGlobal by remember { mutableStateOf<String?>(null) }

    var idParaEliminar by remember { mutableStateOf<String?>(null) }
    var eliminandoIndividual by remember { mutableStateOf(false) }
    var mensajeErrorIndividual by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idUser, recargar) {
        if (idUser.isNullOrBlank()) {
            error = "Sesión no válida. Vuelva a iniciar sesión."
            cargando = false
            return@LaunchedEffect
        }
        cargando = true
        error = null
        try {
            val resp = RetrofitClient.calculationApi.obtenerHistorial(idUser)
            if (resp.isSuccessful) {
                registros = resp.body().orEmpty()
            } else {
                error = "Error al cargar el historial (${resp.code()})"
            }
        } catch (e: Exception) {
            error = "No se pudo conectar: ${e.message}"
        } finally {
            cargando = false
        }
    }

    var textoBusqueda by remember { mutableStateOf("") }
    var detalle by remember { mutableStateOf<ResponseHistorial?>(null) }

    val filtrados = remember(textoBusqueda, registros) {
        if (textoBusqueda.isBlank()) registros
        else registros.filter { (it.material ?: "").contains(textoBusqueda, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "HISTORIAL DE LIQUIDACIONES",
                style = tp.titleLarge,
                color = cs.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Registro histórico de cálculos y liquidaciones de concentrado",
                style = tp.bodySmall,
                color = cs.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    label = { Text("Buscar por mineral...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = cs.primary)
                    },
                    singleLine = true,
                    shape = formas.large,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cs.surfaceContainerHigh,
                        unfocusedContainerColor = cs.surfaceContainerHigh,
                        focusedBorderColor = cs.primary,
                        unfocusedBorderColor = cs.outlineVariant,
                        focusedLabelColor = cs.primary,
                        unfocusedLabelColor = cs.onSurfaceVariant,
                        focusedTextColor = cs.onSurface,
                        unfocusedTextColor = cs.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { mostrarDialogoEliminarGlobal = true },
                    enabled = registros.isNotEmpty() && !cargando
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Eliminar todo el historial",
                        tint = if (registros.isNotEmpty()) cs.error else cs.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (!cargando && error == null && registros.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (filtrados.size == 1) "1 liquidación" else "${filtrados.size} liquidaciones",
                    style = tp.labelMedium,
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            } else {
                Spacer(Modifier.height(16.dp))
            }

            when {
                cargando -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = cs.primary)
                }

                error != null -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error.orEmpty(), style = tp.bodyMedium, color = cs.error, textAlign = TextAlign.Center)
                        TextButton(onClick = { recargar++ }) {
                            Text("REINTENTAR", style = tp.labelLarge, color = cs.primary)
                        }
                    }
                }

                filtrados.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Sin Registros en el Historial", style = tp.titleMedium, color = cs.onSurface)
                        Text(
                            "No se encontraron liquidaciones realizadas o coincidentes.",
                            style = tp.bodySmall,
                            color = cs.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(items = filtrados, key = { it.idCalculation ?: it.hashCode() }) { registro ->
                        TarjetaHistorialCard(
                            registro = registro,
                            exportando = exportandoId != null && exportandoId == registro.idCalculation,
                            onClickDetalle = { detalle = registro },
                            onEliminar = { idParaEliminar = registro.idCalculation },
                            onExportar = { registroParaExportar = registro }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogoEliminarGlobal) {
        AlertDialog(
            onDismissRequest = { if (!eliminandoGlobal) mostrarDialogoEliminarGlobal = false },
            shape = formas.large,
            containerColor = cs.surfaceContainer,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = cs.error, modifier = Modifier.size(32.dp)) },
            title = {
                Text("¿Eliminar Historial Completo?", style = tp.titleLarge, fontWeight = FontWeight.Black, color = cs.primary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Se eliminarán TODOS tus cálculos guardados. Esta acción no se puede deshacer.",
                        style = tp.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                    mensajeErrorGlobal?.let {
                        Text(it, style = tp.bodySmall, color = cs.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            eliminandoGlobal = true
                            mensajeErrorGlobal = null
                            try {
                                val resp = RetrofitClient.calculationApi.eliminarTodoHistorial(idUser!!)
                                if (resp.isSuccessful && resp.body()?.type == "SUCCESS") {
                                    mostrarDialogoEliminarGlobal = false
                                    recargar++
                                } else {
                                    mensajeErrorGlobal = resp.body()?.listMessage?.firstOrNull() ?: "Error del servidor"
                                }
                            } catch (e: Exception) {
                                mensajeErrorGlobal = "Error de red: ${e.message}"
                            } finally {
                                eliminandoGlobal = false
                            }
                        }
                    },
                    enabled = !eliminandoGlobal,
                    colors = ButtonDefaults.buttonColors(containerColor = cs.error, contentColor = cs.onError),
                    shape = formas.medium
                ) {
                    if (eliminandoGlobal) CircularProgressIndicator(color = cs.onError, modifier = Modifier.size(18.dp))
                    else Text("Sí, eliminar todo", style = tp.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminarGlobal = false }, enabled = !eliminandoGlobal) {
                    Text("Cancelar", style = tp.labelLarge, color = cs.onSurfaceVariant)
                }
            }
        )
    }

    if (idParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { if (!eliminandoIndividual) idParaEliminar = null },
            shape = formas.large,
            containerColor = cs.surfaceContainer,
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = cs.error, modifier = Modifier.size(32.dp)) },
            title = {
                Text("¿Eliminar este cálculo?", style = tp.titleLarge, fontWeight = FontWeight.Black, color = cs.primary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Este cálculo se borrará permanentemente de tu historial.",
                        style = tp.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                    mensajeErrorIndividual?.let {
                        Text(it, style = tp.bodySmall, color = cs.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            eliminandoIndividual = true
                            mensajeErrorIndividual = null
                            try {
                                val resp = RetrofitClient.calculationApi.eliminarCalculo(idUser!!, idParaEliminar!!)
                                if (resp.isSuccessful && resp.body()?.type == "SUCCESS") {
                                    idParaEliminar = null
                                    recargar++
                                } else {
                                    mensajeErrorIndividual = resp.body()?.listMessage?.firstOrNull() ?: "Error del servidor"
                                }
                            } catch (e: Exception) {
                                mensajeErrorIndividual = "Error de red: ${e.message}"
                            } finally {
                                eliminandoIndividual = false
                            }
                        }
                    },
                    enabled = !eliminandoIndividual,
                    colors = ButtonDefaults.buttonColors(containerColor = cs.error, contentColor = cs.onError),
                    shape = formas.medium
                ) {
                    if (eliminandoIndividual) CircularProgressIndicator(color = cs.onError, modifier = Modifier.size(18.dp))
                    else Text("Sí, eliminar", style = tp.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { idParaEliminar = null }, enabled = !eliminandoIndividual) {
                    Text("Cancelar", style = tp.labelLarge, color = cs.onSurfaceVariant)
                }
            }
        )
    }

    registroParaExportar?.let { reg ->
        AlertDialog(
            onDismissRequest = { registroParaExportar = null },
            shape = formas.large,
            containerColor = cs.surfaceContainer,
            icon = {
                Icon(
                    Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("¿Exportar liquidación a PDF?", style = tp.titleLarge, fontWeight = FontWeight.Black, color = cs.primary)
            },
            text = {
                Text(
                    "Se generará un PDF con el detalle completo de este cálculo " +
                            "(${reg.material ?: "sin mineral"}) y se guardará en la carpeta " +
                            "Descargas de tu teléfono.",
                    style = tp.bodyMedium,
                    color = cs.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        registroParaExportar = null
                        iniciarExportacion(reg)
                    },
                    shape = formas.medium
                ) {
                    Text("Sí, exportar", style = tp.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { registroParaExportar = null }) {
                    Text("Cancelar", style = tp.labelLarge, color = cs.onSurfaceVariant)
                }
            }
        )
    }

    detalle?.let { reg ->
        DetalleLiquidacionDialog(
            registro = reg,
            onCerrar = { detalle = null },
            onExportar = { registroParaExportar = reg }
        )
    }
}

@Composable
fun TarjetaHistorialCard(
    registro: ResponseHistorial,
    exportando: Boolean = false,
    onClickDetalle: () -> Unit = {},
    onEliminar: () -> Unit = {},
    onExportar: () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    val tp = MaterialTheme.typography
    val formas = MaterialTheme.shapes
    val hayCu = (registro.leyCu ?: 0.0) > 0.0
    val hayAg = (registro.leyAg ?: 0.0) > 0.0
    val hayAu = (registro.leyAu ?: 0.0) > 0.0

    Card(
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
        shape = formas.large,
        border = BorderStroke(1.dp, cs.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = registro.material ?: "Sin mineral",
                        style = tp.titleMedium,
                        color = cs.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = cs.onSurfaceVariant, modifier = Modifier.size(13.dp))
                        Text(
                            text = registro.calculatedAt?.let { FormateadorFecha.formatearParaMostrar(it) } ?: "-",
                            style = tp.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                    if (!registro.plantName.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(Icons.Default.Factory, contentDescription = null, tint = cs.onSurfaceVariant, modifier = Modifier.size(13.dp))
                            Text(
                                text = registro.plantName.orEmpty(),
                                style = tp.bodySmall,
                                color = cs.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (hayCu) EtiquetaMetal("Cu")
                    if (hayAg) EtiquetaMetal("Ag")
                    if (hayAu) EtiquetaMetal("Au")
                }
            }

            HorizontalDivider(color = cs.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("TOTAL A PAGAR", style = tp.labelSmall, color = cs.onSurfaceVariant)
                    Text(
                        text = dolares(registro.totalAPagar ?: 0.0),
                        style = tp.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = cs.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "${String.format(Locale.US, "%,.2f", registro.tonelaje ?: 0.0)} TM brutas",
                        style = tp.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                    Text(
                        "${String.format(Locale.US, "%,.2f", registro.tmns ?: 0.0)} TMNS secas",
                        style = tp.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onClickDetalle,
                    shape = formas.medium,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ver detalle", style = tp.labelMedium)
                }

                FilledIconButton(
                    onClick = onExportar,
                    enabled = !exportando,
                    shape = formas.medium,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = cs.surfaceContainerHigh,
                        contentColor = cs.primary,
                        disabledContainerColor = cs.surfaceContainerHigh,
                        disabledContentColor = cs.primary
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    if (exportando) {
                        CircularProgressIndicator(
                            color = cs.primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "Exportar a PDF",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                FilledIconButton(
                    onClick = onEliminar,
                    shape = formas.medium,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = cs.error.copy(alpha = 0.12f),
                        contentColor = cs.error
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar este cálculo",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EtiquetaMetal(texto: String) {
    val cs = MaterialTheme.colorScheme
    Surface(shape = CircleShape, color = cs.surfaceContainerHigh) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = cs.primary,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}