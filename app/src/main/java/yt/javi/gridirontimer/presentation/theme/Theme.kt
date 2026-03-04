package yt.javi.gridirontimer.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val GridironColors = Colors(
    primary = Color(0xFF66C3FF),
    primaryVariant = Color(0xFF3EA1E0),
    secondary = Color(0xFFFFB74D),
    secondaryVariant = Color(0xFFE0942E),
    error = Color(0xFFFF6B6B),
    onPrimary = Color(0xFF0E1A24),
    onSecondary = Color(0xFF23180A),
    onError = Color(0xFF2A0C0C),
    background = Color(0xFF060A12),
    onBackground = Color(0xFFEAF2FF),
    surface = Color(0xFF0D1522),
    onSurface = Color(0xFFDCE8FF)
)

@Composable
fun GridironTimerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = GridironColors,
        content = content
    )
}
