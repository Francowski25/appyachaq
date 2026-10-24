package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.data.*
import com.yachaq.minero.ui.theme.*
import com.yachaq.minero.logica.FormateadorFecha
import java.util.Locale

private fun dolares(v: Double) = "$ " + String.format(Locale.US, "%,.2f", v)

@Composable
fun SeguimientoPantalla() {
    val usuarioActual = SesionUsuario.usuarioActual
    val esAdmin = usuarioActual?.rol == "admin"
    val esPremium = usuarioActual?.tienePlanPremium == true

    // REGLA PLAN PREMIUM: Si no es Admin ni Premium, se bloquea el módulo por completo
    if (!esAdmin && !esPremium) {
        TarjetaBloqueoSeguimientoPremium()
        return
    }

    var listaSeguimientos by remember {
        mutableStateOf(SeguimientosRepository.obtenerPorUsuario(usuarioActual?.correo ?: ""))
    }
    var mostrarFormularioCrear by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ENCABEZADO
        Text(
            text = "SEGUIMIENTO DE CARGA",
            color = AmarilloMineroBrillante,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Trazabilidad y permanencia de mineral en planta",
            color = TextoGrisSecundario,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (listaSeguimientos.isEmpty() && !mostrarFormularioCrear) {
            CardEstadoVacio(onCrearNuevo = { mostrarFormularioCrear = true })
        } else if (mostrarFormularioCrear) {
            FormularioNuevoSeguimiento(
                onGuardar = { nuevoSeguimiento ->
                    SeguimientosRepository.agregarSeguimiento(nuevoSeguimiento)
                    listaSeguimientos = SeguimientosRepository.obtenerPorUsuario(usuarioActual?.correo ?: "")
                    mostrarFormularioCrear = false
                },
                onCancelar = { mostrarFormularioCrear = false }
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CARGAS REGISTRADAS (${listaSeguimientos.size})",
                    color = TextoGrisSecundario,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { mostrarFormularioCrear = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloMineroBrillante,
                        contentColor = FondoNegroAbsoluto
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Nueva Carga", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            listaSeguimientos.forEach { carga ->
                TarjetaSeguimientoCarga(carga)
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

// ========================================================
// TARJETA DE ACCESO RESTRINGIDO POR PLAN PREMIUM
// ========================================================
@Composable
fun TarjetaBloqueoSeguimientoPremium() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, AmarilloMineroBrillante.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = AmarilloMineroBrillante.copy(alpha = 0.15f),
                    modifier = Modifier.size(70.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AmarilloMineroBrillante,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    text = "Módulo Exclusivo Premium",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AmarilloMineroBrillante,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "El seguimiento en tiempo real, trazabilidad de lotes de mineral y control de días de alquiler en planta requieren una suscripción activa al Plan Premium.",
                    fontSize = 13.sp,
                    color = TextoGrisSecundario,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { SesionUsuario.alternarPlanPremiumSimulado() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloMineroBrillante,
                        contentColor = FondoNegroAbsoluto
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ACTIVAR PLAN PREMIUM", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CardEstadoVacio(onCrearNuevo: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ColorGrisBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                tint = AmarilloMineroBrillante,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Sin Cargas en Seguimiento",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextoBlancoSuave
            )

            Text(
                text = "No tienes ningún lote de mineral registrado en planta actualmente. Registra la fecha de ingreso para monitorear el tiempo de alquiler.",
                fontSize = 13.sp,
                color = TextoGrisSecundario,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onCrearNuevo,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmarilloMineroBrillante,
                    contentColor = FondoNegroAbsoluto
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("REGISTRAR NUEVO SEGUIMIENTO", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TarjetaSeguimientoCarga(carga: SeguimientoCarga) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ColorGrisBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Factory, contentDescription = null, tint = AmarilloMineroBrillante)
                    Text(
                        text = carga.planta.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoSuave
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GrisCampos
                ) {
                    Text(
                        text = carga.estado.etiqueta,
                        color = AmarilloMineroBrillante,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = ColorGrisBorde)

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = GrisCampos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AmarilloMineroBrillante,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text("TIEMPO EN PLANTA", fontSize = 10.sp, color = TextoGrisSecundario, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${carga.diasEnPlanta} Días",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = AmarilloMineroBrillante
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("INGRESO", fontSize = 10.sp, color = TextoGrisSecundario, fontWeight = FontWeight.Bold)
                        Text(
                            text = FormateadorFecha.formatearParaMostrar(carga.fechaIngreso.toString()),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoBlancoSuave
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Mineral:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(carga.mineral, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Carga Bruta:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text("${carga.toneladasBrutas} TM", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextoBlancoSuave)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Costo Alquiler Est.:", fontSize = 11.sp, color = TextoGrisSecundario)
                    Text(dolares(carga.costoAlquilerAcumulado), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmarilloMineroBrillante)
                }
            }
        }
    }
}