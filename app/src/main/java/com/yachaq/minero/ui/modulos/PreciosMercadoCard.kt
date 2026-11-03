package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yachaq.minero.R
import com.yachaq.minero.data.remote.RetrofitClient
import com.yachaq.minero.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface PreciosUiState {
    data object Loading : PreciosUiState
    data class Success(
        val precios: Map<String, Double>,
        val actualizado: String?
    ) : PreciosUiState
    data class Error(val mensaje: String) : PreciosUiState
}

class PreciosMercadoViewModel : ViewModel() {

    private val _state = MutableStateFlow<PreciosUiState>(PreciosUiState.Loading)
    val state: StateFlow<PreciosUiState> = _state

    init { cargar() }

    fun cargar() {
        _state.value = PreciosUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.marketPriceApi.listar()
                if (response.isSuccessful) {
                    val lista = response.body().orEmpty()
                    val mapa = lista
                        .filter { it.metal != null && it.price != null }
                        .associate { it.metal!!.uppercase() to it.price!! }
                    val fecha = lista.mapNotNull { it.updatedAt }.maxOrNull()
                    _state.value = PreciosUiState.Success(mapa, fecha)
                } else {
                    _state.value = PreciosUiState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = PreciosUiState.Error(e.message ?: "Error al cargar precios")
            }
        }
    }
}

private fun formatoPrecio(v: Double): String = String.format(Locale.US, "%,.2f", v)

private fun formatoFecha(fecha: String?): String =
    fecha?.replace('T', ' ')?.take(16) ?: "—"

private val ColorCobre = Color(0xFFE08A3C)
private val ColorPlata = Color(0xFFB0BEC5)
private val ColorOro = Color(0xFFFFD54F)
private val ColorVivo = Color(0xFF4CAF50)

@Composable
fun PreciosMercadoCard(
    modifier: Modifier = Modifier,
    viewModel: PreciosMercadoViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val forma = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(
                Brush.linearGradient(
                    listOf(
                        AmarilloMineroBrillante.copy(alpha = 0.14f),
                        FondoNegroSecundario,
                        FondoNegroSecundario
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        AmarilloMineroBrillante.copy(alpha = 0.7f),
                        AmarilloMineroBrillante.copy(alpha = 0.08f)
                    )
                ),
                forma
            )
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PRECIOS DEL MERCADO",
                    color = AmarilloMineroBrillante,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = "Base de tu liquidación",
                    color = TextoGrisSecundario,
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(ColorVivo)
            )
        }

        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            is PreciosUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AmarilloMineroBrillante,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            is PreciosUiState.Error -> {
                Text(
                    text = s.mensaje,
                    color = ErrorRojo,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.cargar() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmarilloMineroBrillante,
                        contentColor = FondoNegroAbsoluto
                    )
                ) {
                    Text("REINTENTAR", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }

            is PreciosUiState.Success -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ItemPrecio("COBRE", R.drawable.cobre, s.precios["CU"], "$/lb", ColorCobre, Modifier.weight(1f))
                    ItemPrecio("PLATA", R.drawable.plata, s.precios["AG"], "$/oz tr", ColorPlata, Modifier.weight(1f))
                    ItemPrecio("ORO", R.drawable.oro, s.precios["AU"], "$/oz tr", ColorOro, Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Actualizado: ${formatoFecha(s.actualizado)}",
                    color = TextoGrisSecundario,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun ItemPrecio(
    nombre: String,
    recurso: Int,
    precio: Double?,
    unidad: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .clip(forma)
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.4f), forma)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        listOf(
                            color.copy(alpha = 0.38f),
                            color.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = recurso),
                contentDescription = nombre,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(7.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = nombre,
            color = TextoGrisSecundario,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Text(
            text = precio?.let { "$${formatoPrecio(it)}" } ?: "—",
            color = TextoBlancoSuave,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = unidad,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}