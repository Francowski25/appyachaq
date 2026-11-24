package com.yachaq.minero.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = AmarilloMineroBrillante,
    onPrimary = FondoNegroAbsoluto,
    primaryContainer = FondoNegroSecundario,
    onPrimaryContainer = AmarilloMineroBrillante,
    secondary = AmarilloMinero,
    onSecondary = FondoNegroAbsoluto,
    secondaryContainer = GrisCampos,
    onSecondaryContainer = TextoBlancoSuave,
    tertiary = ExitoVerde,
    onTertiary = FondoNegroAbsoluto,
    background = FondoNegroAbsoluto,
    onBackground = TextoBlancoSuave,
    surface = FondoNegroAbsoluto,
    onSurface = TextoBlancoSuave,
    surfaceVariant = FondoNegroSecundario,
    onSurfaceVariant = TextoGrisSecundario,
    surfaceTint = AmarilloMineroBrillante,
    surfaceContainerLowest = FondoNegroAbsoluto,
    surfaceContainerLow = Color(0xFF11141A),
    surfaceContainer = FondoNegroSecundario,
    surfaceContainerHigh = GrisCampos,
    surfaceContainerHighest = SuperficieElevada,
    outline = TextoGrisSecundario,
    outlineVariant = ColorGrisBorde,
    error = ErrorRojo,
    onError = FondoNegroAbsoluto,
    errorContainer = ErrorContenedor,
    onErrorContainer = Color(0xFFFFB4B4),
    scrim = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = ClaroPrimario,
    onPrimary = Color.White,
    primaryContainer = ClaroPrimarioContenedor,
    onPrimaryContainer = ClaroTexto,
    secondary = AmarilloMinero,
    onSecondary = ClaroTexto,
    background = ClaroFondo,
    onBackground = ClaroTexto,
    surface = ClaroFondo,
    onSurface = ClaroTexto,
    surfaceVariant = ClaroSuperficieVariante,
    onSurfaceVariant = ClaroTextoSecundario,
    outline = ClaroTextoSecundario,
    outlineVariant = ClaroBorde,
    error = ClaroError,
    onError = Color.White
)

private val FormasMinero = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MineroAppTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
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
        shapes = FormasMinero,
        content = content
    )
}