package co.edu.uniquindio.pokeapi.domain.repository

import kotlinx.coroutines.flow.Flow

/** Avisa si el teléfono tiene conexión a internet. Emite el estado actual al suscribirse y luego cada cambio. */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
