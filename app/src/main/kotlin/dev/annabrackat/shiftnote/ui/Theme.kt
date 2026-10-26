package dev.annabrackat.shiftnote.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Navy = Color(0xFF1B3A4B)
private val Paper = Color(0xFFF4EFE6)
private val Ink = Color(0xFF1A1814)
private val Surface = Color(0xFFFFFBF4)

private val Scheme = lightColorScheme(
    primary = Navy,
    onPrimary = Surface,
    background = Paper,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
)

@Composable
fun ShiftnoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
