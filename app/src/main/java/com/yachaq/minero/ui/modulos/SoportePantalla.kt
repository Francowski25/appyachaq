package com.yachaq.minero.ui.modulos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yachaq.minero.ui.theme.*

@Composable
fun SoportePantalla() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ENCABEZADO
        Text(
            text = "SOPORTE Y ASISTENCIA",
            color = AmarilloMineroBrillante,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Atención y orientación técnica para el operador minero",
            color = TextoGrisSecundario,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // TARJETA DE BANNER PRINCIPAL
        Card(
            colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, AmarilloMineroBrillante.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = AmarilloMineroBrillante.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HeadsetMic,
                            contentDescription = null,
                            tint = AmarilloMineroBrillante,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "¿Necesitas ayuda con tus liquidaciones?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoSuave
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nuestro equipo técnico está disponible para asistirte con consultas de cálculo, ley de mineral y trazabilidad.",
                        fontSize = 12.sp,
                        color = TextoGrisSecundario
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "CANALES DIRECTOS DE ATENCIÓN",
            color = TextoGrisSecundario,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // BOTÓN WHATSAPP
        TarjetaOpcionSoporte(
            titulo = "Asistencia por WhatsApp",
            subtitulo = "Respuesta rápida para enviar fotos de boletas o consultas de campo",
            icono = Icons.Default.Message,
            colorIcono = AmarilloMineroBrillante,
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/51999999999?text=Hola,%20necesito%20asistencia%20en%20Minero%20Yachaq")
                }
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // BOTÓN LLAMADA
        TarjetaOpcionSoporte(
            titulo = "Llamada Telefónica Directa",
            subtitulo = "Comunicación inmediata con la central de operaciones",
            icono = Icons.Default.Call,
            colorIcono = AmarilloMineroBrillante,
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:+51999999999")
                }
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // BOTÓN CORREO
        TarjetaOpcionSoporte(
            titulo = "Correo de Soporte Técnico",
            subtitulo = "soporte@yachaq.com para informes o reportes técnicos",
            icono = Icons.Default.Email,
            colorIcono = AmarilloMineroBrillante,
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:soporte@yachaq.com?subject=Soporte%20Minero%20Yachaq")
                }
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SECCIÓN HORARIOS
        Card(
            colors = CardDefaults.cardColors(containerColor = GrisCampos),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = TextoGrisSecundario
                )
                Column {
                    Text(
                        text = "Horarios de Atención Operativa",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoBlancoSuave
                    )
                    Text(
                        text = "Lunes a Sábado de 07:00 AM a 07:00 PM",
                        fontSize = 11.sp,
                        color = TextoGrisSecundario
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaOpcionSoporte(
    titulo: String,
    subtitulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    colorIcono: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorGrisBorde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GrisCampos,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorIcono,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoBlancoSuave
                )
                Text(
                    text = subtitulo,
                    fontSize = 11.sp,
                    color = TextoGrisSecundario,
                    lineHeight = 15.sp
                )
            }
        }
    }
}