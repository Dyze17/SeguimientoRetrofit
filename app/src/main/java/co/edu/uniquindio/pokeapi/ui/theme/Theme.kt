package co.edu.uniquindio.pokeapi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Rojo Pokédex como color principal y azul como secundario.
private val LightColors = lightColorScheme(
    primary = Color(0xFFC62828),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF3B4CCA),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDFE0FF),
    onSecondaryContainer = Color(0xFF000E5E),
    background = Color(0xFFFFF8F7),
    surface = Color(0xFFFFF8F7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFBCC2FF),
    onSecondary = Color(0xFF07218F),
    secondaryContainer = Color(0xFF2536B1),
    onSecondaryContainer = Color(0xFFDFE0FF),
    background = Color(0xFF1A1111),
    surface = Color(0xFF1A1111),
)

/** Verde del indicador «En línea»; el de «Sin conexión» es el color de error del tema. */
val OnlineGreen = Color(0xFF2E7D32)
val OnlineGreenDark = Color(0xFF81C784)

@Composable
fun PokeApiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
