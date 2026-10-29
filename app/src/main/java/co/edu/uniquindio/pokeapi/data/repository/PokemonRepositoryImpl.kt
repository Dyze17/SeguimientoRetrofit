package co.edu.uniquindio.pokeapi.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.edu.uniquindio.pokeapi.data.mapper.idFromUrl
import co.edu.uniquindio.pokeapi.data.mapper.toGeneration
import co.edu.uniquindio.pokeapi.data.mapper.toPokemonDetail
import co.edu.uniquindio.pokeapi.data.mapper.toPokemonSummary
import co.edu.uniquindio.pokeapi.data.paging.FilteredPokemonPagingSource
import co.edu.uniquindio.pokeapi.data.paging.PokemonPagingSource
import co.edu.uniquindio.pokeapi.data.remote.PokeApiService
import co.edu.uniquindio.pokeapi.data.remote.safeApiCall
import co.edu.uniquindio.pokeapi.domain.model.Generation
import co.edu.uniquindio.pokeapi.domain.model.PokemonDetail
import co.edu.uniquindio.pokeapi.domain.model.PokemonFilter
import co.edu.uniquindio.pokeapi.domain.model.PokemonSummary
import co.edu.uniquindio.pokeapi.domain.repository.PokemonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val api: PokeApiService,
) : PokemonRepository {

    // Las respuestas de /type/{name}, /generation/{id} y el índice de nombres no cambian mientras la app está abierta:
    // se guardan en memoria para que pasar de página o combinar filtros no repita la petición.
    private val pokemonByType = ConcurrentHashMap<String, List<PokemonSummary>>()
    private val pokemonByGeneration = ConcurrentHashMap<Int, List<PokemonSummary>>()
    @Volatile
    private var pokemonIndex: List<PokemonSummary>? = null

    override fun getPokemonPages(filter: PokemonFilter): Flow<PagingData<PokemonSummary>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE * 2, enablePlaceholders = false),
            pagingSourceFactory = {
                if (filter.isEmpty) PokemonPagingSource(api) else FilteredPokemonPagingSource { findPokemon(filter) }
            },
        ).flow

    override suspend fun getPokemonDetail(id: Int): PokemonDetail = safeApiCall {
        val pokemon = api.getPokemon(id)
        // Las formas alternativas (megaevoluciones, formas regionales…) comparten la especie de la forma base.
        val species = try {
            api.getPokemonSpecies(idFromUrl(pokemon.species.url))
        } catch (e: HttpException) {
            if (e.code() == 404) null else throw e
        }
        pokemon.toPokemonDetail(species)
    }

    override suspend fun getTypes(): List<String> = safeApiCall {
        api.getTypes().results.map { it.name }.filterNot { it in TYPES_WITHOUT_POKEMON }
    }

    override suspend fun getGenerations(): List<Generation> = safeApiCall {
        api.getGenerations().results.map { it.toGeneration() }
    }

    /**
     * Combina los filtros: el tipo y la generación se piden en paralelo y se cruzan por número; el texto se busca en
     * el nombre (o es el número exacto). Sin tipo ni generación, el texto se busca en el índice de todos los Pokémon.
     */
    internal suspend fun findPokemon(filter: PokemonFilter): List<PokemonSummary> = coroutineScope {
        val ofType = filter.type?.let { async { pokemonOfType(it) } }
        val ofGeneration = filter.generationId?.let { async { pokemonOfGeneration(it) } }
        val typeResults = ofType?.await()
        val generationResults = ofGeneration?.await()

        val candidates = when {
            typeResults != null && generationResults != null -> {
                val generationIds = generationResults.mapTo(HashSet()) { it.id }
                typeResults.filter { it.id in generationIds }
            }
            typeResults != null -> typeResults
            generationResults != null -> generationResults
            else -> allPokemon()
        }

        val query = normalizeQuery(filter.query)
        candidates.filter { it.matches(query) }.sortedBy { it.id }
    }

    private suspend fun pokemonOfType(type: String): List<PokemonSummary> =
        pokemonByType[type] ?: api.getType(type).pokemon
            .map { it.pokemon.toPokemonSummary() }
            .also { pokemonByType[type] = it }

    // /generation/{id} responde especies, no Pokémon; el id de la especie es el de su forma base en /pokemon.
    private suspend fun pokemonOfGeneration(generationId: Int): List<PokemonSummary> =
        pokemonByGeneration[generationId] ?: api.getGeneration(generationId).pokemonSpecies
            .map { it.toPokemonSummary() }
            .also { pokemonByGeneration[generationId] = it }

    private suspend fun allPokemon(): List<PokemonSummary> =
        pokemonIndex ?: api.getPokemonList(offset = 0, limit = INDEX_LIMIT).results
            .map { it.toPokemonSummary() }
            .also { pokemonIndex = it }

    private companion object {
        const val PAGE_SIZE = 20

        // Más que el total de Pokémon de la API, para traer el índice completo de nombres en una sola petición.
        const val INDEX_LIMIT = 10_000

        // Tipos que la API lista pero que no tienen Pokémon en los juegos principales.
        val TYPES_WITHOUT_POKEMON = setOf("unknown", "shadow", "stellar")
    }
}

/** «Mr Mime», «#25» → «mr-mime», «25»: así escribe la API los nombres. */
internal fun normalizeQuery(query: String): String =
    query.trim().lowercase().removePrefix("#").replace(Regex("\\s+"), "-")

/** Un número busca ese Pokémon exacto; un texto, los que lo contienen en el nombre. */
internal fun PokemonSummary.matches(normalizedQuery: String): Boolean {
    if (normalizedQuery.isEmpty()) return true
    normalizedQuery.toIntOrNull()?.let { return id == it }
    return name.contains(normalizedQuery)
}
