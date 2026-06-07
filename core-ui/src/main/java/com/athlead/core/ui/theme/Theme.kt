package com.athlead.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Azure,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E3FF),
    onPrimaryContainer = Navy,

    secondary = Electrico,
    onSecondary = Color.White,
    secondaryContainer = InfoContainer,
    onSecondaryContainer = Navy,

    tertiary = Verificado,
    onTertiary = Color.White,
    tertiaryContainer = SuccessContainer,
    onTertiaryContainer = Color(0xFF003822),

    error = Error,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFF5F0016),

    background = Papel,
    onBackground = Gris80,

    surface = Bruma,
    onSurface = Gris80,
    surfaceVariant = Gris10,
    onSurfaceVariant = Gris60,

    outline = Gris20,
    outlineVariant = Gris10
)

@Composable
fun AthleedTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AthleedTypography,
        content = content
    )
}
