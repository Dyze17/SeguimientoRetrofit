package co.edu.uniquindio.pokeapi.domain.repository

import androidx.paging.PagingData
import co.edu.uniquindio.pokeapi.domain.model.Generation
import co.edu.uniquindio.pokeapi.domain.model.PokemonDetail
import co.edu.uniquindio.pokeapi.domain.model.PokemonFilter
import co.edu.uniquindio.pokeapi.domain.model.PokemonSummary
import kotlinx.coroutines.flow.Flow

/** El acceso a los datos de Pokémon. Las funciones suspend lanzan [co.edu.uniquindio.pokeapi.domain.model.AppException]. */
interface PokemonRepository {

    /** Las páginas de la lista para el scroll infinito, ya filtradas. */
    fun getPokemonPages(filter: PokemonFilter): Flow<PagingData<PokemonSummary>>

    suspend fun getPokemonDetail(id: Int): PokemonDetail

    /** Los nombres de los tipos que se pueden elegir en el filtro (fire, water…). */
    suspend fun getTypes(): List<String>

    suspend fun getGenerations(): List<Generation>
}
