package co.edu.uniquindio.pokeapi.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.pokeapi.R
import co.edu.uniquindio.pokeapi.domain.model.AppError
import co.edu.uniquindio.pokeapi.domain.model.AppException

@StringRes
fun AppError.messageRes(): Int = when (this) {
    AppError.Network -> R.string.error_network
    AppError.NotFound -> R.string.error_not_found
    AppError.Server -> R.string.error_server
    AppError.Unknown -> R.string.error_unknown
}

/** Paging entrega el error como Throwable: el repositorio ya lo convirtió en [AppException]. */
fun Throwable.toAppError(): AppError = (this as? AppException)?.error ?: AppError.Unknown

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Pantalla completa de error con el motivo y un botón para volver a intentar. */
@Composable
fun ErrorState(error: AppError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    MessageState(
        icon = if (error == AppError.Network) R.drawable.ic_cloud_off else R.drawable.ic_error,
        title = stringResource(if (error == AppError.Network) R.string.error_title_network else R.string.error_title),
        message = stringResource(error.messageRes()),
        actionLabel = stringResource(R.string.retry),
        onAction = onRetry,
        modifier = modifier,
    )
}

@Composable
fun MessageState(
    @DrawableRes icon: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}
