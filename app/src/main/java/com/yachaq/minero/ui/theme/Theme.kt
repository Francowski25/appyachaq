package com.yachaq.minero.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ESQUEMA OSCURO INDUSTRIAL (Negro & Dorado)
private val DarkColorScheme = darkColorScheme(
    primary = AmarilloMineroBrillante,
    onPrimary = FondoNegroAbsoluto,
    primaryContainer = FondoNegroSecundario,
    onPrimaryContainer = AmarilloMineroBrillante,
    surface = FondoNegroAbsoluto,
    surfaceVariant = FondoNegroSecundario,
    onSurface = TextoBlancoSuave,
    onSurfaceVariant = TextoGrisSecundario,
    outline = TextoGrisSecundario,
    error = ErrorRojo
)

// ESQUEMA CLARO (Opción de respaldo)
private val LightColorScheme = lightColorScheme(
    primary = AmarilloMinero,
    onPrimary = FondoNegroAbsoluto,
    primaryContainer = GrisCampos,
    onPrimaryContainer = FondoNegroAbsoluto,
    surface = TextoBlancoSuave,
    surfaceVariant = ColorGrisBorde,
    onSurface = FondoNegroAbsoluto,
    onSurfaceVariant = TextoGrisSecundario,
    outline = TextoGrisSecundario,
    error = ErrorRojo
)

@Composable
fun MineroAppTheme(
    darkTheme: Boolean = true, // Forzado a modo oscuro para estética industrial
    dynamicColor: Boolean = false, // Desactivado para no perder los colores de marca
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}