package co.edu.uniquindio.pokeapi.data

import co.edu.uniquindio.pokeapi.data.remote.PokeApiService
import co.edu.uniquindio.pokeapi.data.remote.dto.GenerationDto
import co.edu.uniquindio.pokeapi.data.remote.dto.NamedResourceDto
import co.edu.uniquindio.pokeapi.data.remote.dto.NamedResourceListDto
import co.edu.uniquindio.pokeapi.data.remote.dto.PokemonDto
import co.edu.uniquindio.pokeapi.data.remote.dto.PokemonSpeciesDto
import co.edu.uniquindio.pokeapi.data.remote.dto.SpritesDto
import co.edu.uniquindio.pokeapi.data.remote.dto.TypeDto
import co.edu.uniquindio.pokeapi.data.remote.dto.TypePokemonDto
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

fun pokemonRef(id: Int, name: String) = NamedResourceDto(name, "https://pokeapi.co/api/v2/pokemon/$id/")

fun speciesRef(id: Int, name: String) = NamedResourceDto(name, "https://pokeapi.co/api/v2/pokemon-species/$id/")

fun typeOf(name: String, vararg pokemon: NamedResourceDto) =
    TypeDto(id = 1, name = name, pokemon = pokemon.map { TypePokemonDto(slot = 1, pokemon = it) })

fun generationOf(id: Int, vararg species: NamedResourceDto) =
    GenerationDto(id = id, name = "generation-$id", pokemonSpecies = species.toList())

fun pokemonDto(id: Int, name: String, speciesId: Int = id) = PokemonDto(
    id = id,
    name = name,
    height = 7,
    weight = 69,
    types = emptyList(),
    stats = emptyList(),
    abilities = emptyList(),
    sprites = SpritesDto(),
    species = speciesRef(speciesId, name),
)

/** Responde desde memoria lo que cada prueba carga, y cuenta las peticiones de tipo. */
class FakePokeApiService : PokeApiService {
    var failure: Exception? = null
    var allPokemon: List<NamedResourceDto> = emptyList()
    val types = mutableMapOf<String, TypeDto>()
    val generations = mutableMapOf<Int, GenerationDto>()
    val pokemon = mutableMapOf<Int, PokemonDto>()
    val species = mutableMapOf<Int, PokemonSpeciesDto>()
    var typeRequests = 0
        private set

    override suspend fun getPokemonList(offset: Int, limit: Int): NamedResourceListDto {
        failure?.let { throw it }
        val page = allPokemon.drop(offset).take(limit)
        return NamedResourceListDto(
            count = allPokemon.size,
            next = if (offset + page.size < allPokemon.size) "next" else null,
            results = page,
        )
    }

    override suspend fun getPokemon(id: Int): PokemonDto {
        failure?.let { throw it }
        return pokemon[id] ?: throw notFound()
    }

    override suspend fun getPokemonSpecies(id: Int): PokemonSpeciesDto {
        failure?.let { throw it }
        return species[id] ?: throw notFound()
    }

    override suspend fun getTypes(limit: Int): NamedResourceListDto {
        failure?.let { throw it }
        val results = types.keys.map { NamedResourceDto(it, "https://pokeapi.co/api/v2/type/$it/") }
        return NamedResourceListDto(count = results.size, results = results)
    }

    override suspend fun getType(name: String): TypeDto {
        failure?.let { throw it }
        typeRequests++
        return types[name] ?: throw notFound()
    }

    override suspend fun getGenerations(limit: Int): NamedResourceListDto {
        failure?.let { throw it }
        val results = generations.values.map { NamedResourceDto(it.name, "https://pokeapi.co/api/v2/generation/${it.id}/") }
        return NamedResourceListDto(count = results.size, results = results)
    }

    override suspend fun getGeneration(id: Int): GenerationDto {
        failure?.let { throw it }
        return generations[id] ?: throw notFound()
    }

    private fun notFound() = HttpException(Response.error<Any>(404, "".toResponseBody()))
}
