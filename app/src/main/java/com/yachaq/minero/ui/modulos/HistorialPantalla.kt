package com.yachaq.minero.ui.modulos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import java.util.Locale

private fun dolares(v: Double) = "$ " + String.format(Locale.US, "%,.2f", v)

@Composable
fun HistorialPantalla() {
    if (SesionUsuario.esPremium()) HistorialContenido() else HistorialBloqueado()
}

@Composable
private fun HistorialBloqueado() {
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
                tint = AmarilloMineroBrillante,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Historial exclusivo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = TextoBlancoSuave
            )
            Text(
                "El historial de liquidaciones está disponible para administradores y usuarios Premium.",
                fontSize = 12.sp,
                color = TextoGrisSecundario,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { SesionUsuario.alternarPlanPremiumSimulado() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmarilloMineroBrillante,
                    contentColor = FondoNegroAbsoluto
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ACTIVAR PREMIUM (SIMULACIÓN)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HistorialContenido() {
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

    // Se ejecuta después de que el usuario confirma en el diálogo
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
                color = AmarilloMineroBrillante,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Registro histórico de cálculos y liquidaciones de concentrado",
                color = TextoGrisSecundario,
                fontSize = 12.sp
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
                        Icon(Icons.Default.Search, contentDescription = null, tint = AmarilloMineroBrillante)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GrisCampos, unfocusedContainerColor = GrisCampos,
                        focusedBorderColor = AmarilloMineroBrillante, unfocusedBorderColor = ColorGrisBorde,
                        focusedLabelColor = AmarilloMineroBrillante, unfocusedLabelColor = TextoGrisSecundario,
                        focusedTextColor = TextoBlancoSuave, unfocusedTextColor = TextoBlancoSuave
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
                        tint = if (registros.isNotEmpty()) ErrorRojo else TextoGrisSecundario,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (!cargando && error == null && registros.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (filtrados.size == 1) "1 liquidación" else "${filtrados.size} liquidaciones",
                    color = TextoGrisSecundario,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
            } else {
                Spacer(Modifier.height(16.dp))
            }

            when {
                cargando -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AmarilloMineroBrillante)
                }

                error != null -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error.orEmpty(), color = ErrorRojo, fontSize = 13.sp, textAlign = TextAlign.Center)
                        TextButton(onClick = { recargar++ }) {
                            Text("REINTENTAR", color = AmarilloMineroBrillante)
                        }
                    }
                }

                filtrados.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = ErrorRojo, modifier = Modifier.size(48.dp))
                        Text("Sin Registros en el Historial", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave)
                        Text(
                            "No se encontraron liquidaciones realizadas o coincidentes.",
                            fontSize = 12.sp,
                            color = TextoGrisSecundario,
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
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRojo, modifier = Modifier.size(32.dp)) },
            title = { Text("¿Eliminar Historial Completo?", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AmarilloMineroBrillante) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se eliminarán TODOS tus cálculos guardados. Esta acción no se puede deshacer.", fontSize = 13.sp, color = TextoGrisSecundario)
                    mensajeErrorGlobal?.let { Text(it, color = ErrorRojo, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
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
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRojo, contentColor = TextoBlancoSuave),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (eliminandoGlobal) CircularProgressIndicator(color = TextoBlancoSuave, modifier = Modifier.size(18.dp))
                    else Text("Sí, eliminar todo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminarGlobal = false }, enabled = !eliminandoGlobal) {
                    Text("Cancelar", color = TextoGrisSecundario)
                }
            }
        )
    }

    if (idParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { if (!eliminandoIndividual) idParaEliminar = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRojo, modifier = Modifier.size(32.dp)) },
            title = { Text("¿Eliminar este cálculo?", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AmarilloMineroBrillante) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Este cálculo se borrará permanentemente de tu historial.", fontSize = 13.sp, color = TextoGrisSecundario)
                    mensajeErrorIndividual?.let { Text(it, color = ErrorRojo, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
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
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRojo, contentColor = TextoBlancoSuave),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (eliminandoIndividual) CircularProgressIndicator(color = TextoBlancoSuave, modifier = Modifier.size(18.dp))
                    else Text("Sí, eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { idParaEliminar = null }, enabled = !eliminandoIndividual) {
                    Text("Cancelar", color = TextoGrisSecundario)
                }
            }
        )
    }

    registroParaExportar?.let { reg ->
        AlertDialog(
            onDismissRequest = { registroParaExportar = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = FondoNegroSecundario,
            icon = {
                Icon(
                    Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = AmarilloMineroBrillante,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "¿Exportar liquidación a PDF?",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = AmarilloMineroBrillante
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Se generará un PDF con el detalle completo de este cálculo " +
                                "(${reg.material ?: "sin mineral"}) y se guardará en la carpeta " +
                                "Descargas de tu teléfono.",
                        fontSize = 13.sp,
                        color = TextoGrisSecundario
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        registroParaExportar = null
                        iniciarExportacion(reg)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloMineroBrillante,
                        contentColor = FondoNegroAbsoluto
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sí, exportar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { registroParaExportar = null }) {
                    Text("Cancelar", color = TextoGrisSecundario)
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
    val hayCu = (registro.leyCu ?: 0.0) > 0.0
    val hayAg = (registro.leyAg ?: 0.0) > 0.0
    val hayAu = (registro.leyAu ?: 0.0) > 0.0

    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ColorGrisBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Encabezado: mineral, fecha y planta
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoSuave
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextoGrisSecundario, modifier = Modifier.size(13.dp))
                        Text(
                            text = registro.calculatedAt?.let { FormateadorFecha.formatearParaMostrar(it) } ?: "-",
                            fontSize = 12.sp,
                            color = TextoGrisSecundario
                        )
                    }
                    if (!registro.plantName.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(Icons.Default.Factory, contentDescription = null, tint = TextoGrisSecundario, modifier = Modifier.size(13.dp))
                            Text(
                                text = registro.plantName.orEmpty(),
                                fontSize = 12.sp,
                                color = TextoGrisSecundario,
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

            HorizontalDivider(color = ColorGrisBorde.copy(alpha = 0.6f))

            // Total y cantidades
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "TOTAL A PAGAR",
                        fontSize = 10.sp,
                        color = TextoGrisSecundario,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = dolares(registro.totalAPagar ?: 0.0),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = AmarilloMineroBrillante
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "${String.format(Locale.US, "%,.2f", registro.tonelaje ?: 0.0)} TM brutas",
                        fontSize = 11.sp,
                        color = TextoGrisSecundario
                    )
                    Text(
                        "${String.format(Locale.US, "%,.2f", registro.tmns ?: 0.0)} TMNS secas",
                        fontSize = 11.sp,
                        color = TextoGrisSecundario
                    )
                }
            }

            // Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onClickDetalle,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloMineroBrillante,
                        contentColor = FondoNegroAbsoluto
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ver detalle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                FilledIconButton(
                    onClick = onExportar,
                    enabled = !exportando,
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = GrisCampos,
                        contentColor = AmarilloMineroBrillante,
                        disabledContainerColor = GrisCampos,
                        disabledContentColor = AmarilloMineroBrillante
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    if (exportando) {
                        CircularProgressIndicator(
                            color = AmarilloMineroBrillante,
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
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = ErrorRojo.copy(alpha = 0.12f),
                        contentColor = ErrorRojo
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
    Surface(shape = RoundedCornerShape(50), color = GrisCampos) {
        Text(
            text = texto,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AmarilloMineroBrillante,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}