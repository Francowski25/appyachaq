package com.yachaq.minero.ui.modulos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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

@Composable
fun PreciosMercadoCard(
    modifier: Modifier = Modifier,
    viewModel: PreciosMercadoViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FondoNegroSecundario),
        border = BorderStroke(1.dp, ColorGrisBorde)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "PRECIOS DEL MERCADO",
                color = AmarilloMineroBrillante,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(12.dp))

            when (val s = state) {
                is PreciosUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AmarilloMineroBrillante,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                is PreciosUiState.Error -> {
                    Text(
                        text = s.mensaje,
                        color = ErrorRojo,
                        fontSize = 12.sp
                    )
                    TextButton(onClick = { viewModel.cargar() }) {
                        Text("REINTENTAR", color = AmarilloMineroBrillante)
                    }
                }

                is PreciosUiState.Success -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ItemPrecio("Cu", "COBRE", s.precios["CU"], "$/lb", ColorCobre, Modifier.weight(1f))
                        ItemPrecio("Ag", "PLATA", s.precios["AG"], "$/oz tr", ColorPlata, Modifier.weight(1f))
                        ItemPrecio("Au", "ORO", s.precios["AU"], "$/oz tr", ColorOro, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Actualizado: ${formatoFecha(s.actualizado)}",
                        color = TextoGrisSecundario,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemPrecio(
    simbolo: String,
    nombre: String,
    precio: Double?,
    unidad: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .clip(forma)
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.35f), forma)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = simbolo,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = nombre,
            color = TextoGrisSecundario,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
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
            fontSize = 10.sp
        )
    }
}