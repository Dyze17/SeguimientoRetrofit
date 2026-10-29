package co.edu.uniquindio.pokeapi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.pokeapi.R
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

/** La imagen oficial sobre un círculo de fondo, que también hace de marcador mientras Coil la descarga. */
@Composable
fun PokemonArtwork(
    imageUrl: String?,
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size * 0.88f)
                .clip(CircleShape)
                .background(backgroundColor),
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.pokemon_image, name),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size),
        )
    }
}

@Composable
fun TypeBadge(type: String, modifier: Modifier = Modifier) {
    val color = typeColor(type)
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color,
        // Texto oscuro sobre los tipos claros (eléctrico, hielo…) para que se lea.
        contentColor = if (color.luminance() > 0.5f) Color(0xFF1C1B1F) else Color.White,
    ) {
        Text(
            text = typeLabel(type),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}
