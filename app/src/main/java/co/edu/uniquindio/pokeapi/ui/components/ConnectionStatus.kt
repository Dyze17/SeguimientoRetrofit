package co.edu.uniquindio.pokeapi.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.pokeapi.R
import co.edu.uniquindio.pokeapi.ui.theme.OnlineGreen
import co.edu.uniquindio.pokeapi.ui.theme.OnlineGreenDark

/** El indicador de la barra superior: siempre visible, con el estado actual («En línea» o «Sin conexión»). */
@Composable
fun ConnectionStatusChip(isOnline: Boolean, modifier: Modifier = Modifier) {
    val onlineColor = if (isSystemInDarkTheme()) OnlineGreenDark else OnlineGreen
    val color by animateColorAsState(
        targetValue = if (isOnline) onlineColor else MaterialTheme.colorScheme.error,
        label = "connectionColor",
    )
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color.copy(alpha = 0.12f),
        contentColor = color,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(if (isOnline) R.drawable.ic_cloud_done else R.drawable.ic_cloud_off),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(if (isOnline) R.string.status_online else R.string.status_offline),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

/** La franja que explica qué pasa sin conexión. Aparece y desaparece con el estado de la red. */
@Composable
fun OfflineBanner(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cloud_off),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(text = stringResource(R.string.offline_banner), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
