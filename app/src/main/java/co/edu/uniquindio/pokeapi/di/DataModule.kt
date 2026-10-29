package co.edu.uniquindio.pokeapi.di

import co.edu.uniquindio.pokeapi.data.network.ConnectivityNetworkMonitor
import co.edu.uniquindio.pokeapi.data.repository.PokemonRepositoryImpl
import co.edu.uniquindio.pokeapi.domain.repository.NetworkMonitor
import co.edu.uniquindio.pokeapi.domain.repository.PokemonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Une las interfaces del dominio con sus implementaciones de la capa de datos. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindPokemonRepository(impl: PokemonRepositoryImpl): PokemonRepository

    @Binds
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor
}
