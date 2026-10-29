package co.edu.uniquindio.pokeapi.data.remote

import co.edu.uniquindio.pokeapi.data.remote.dto.GenerationDto
import co.edu.uniquindio.pokeapi.data.remote.dto.NamedResourceListDto
import co.edu.uniquindio.pokeapi.data.remote.dto.PokemonDto
import co.edu.uniquindio.pokeapi.data.remote.dto.PokemonSpeciesDto
import co.edu.uniquindio.pokeapi.data.remote.dto.TypeDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Los endpoints de PokeAPI (https://pokeapi.co/docs/v2) que consume la app. Todos son GET y responden JSON. */
interface PokeApiService {

    /** La lista paginada de todos los Pokémon: el scroll infinito sin filtros, y el índice para buscar por nombre. */
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): NamedResourceListDto

    /** Tipos, estadísticas, habilidades, medidas e imágenes de un Pokémon (pantalla de detalle). */
    @GET("pokemon/{id}")
    suspend fun getPokemon(@Path("id") id: Int): PokemonDto

    /** Descripción, categoría, hábitat y generación de la especie (pantalla de detalle). */
    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): PokemonSpeciesDto

    /** Los tipos que se ofrecen en el filtro. */
    @GET("type")
    suspend fun getTypes(@Query("limit") limit: Int = 100): NamedResourceListDto

    /** Los Pokémon de un tipo (filtro por tipo). */
    @GET("type/{name}")
    suspend fun getType(@Path("name") name: String): TypeDto

    /** Las generaciones que se ofrecen en el filtro. */
    @GET("generation")
    suspend fun getGenerations(@Query("limit") limit: Int = 100): NamedResourceListDto

    /** Las especies que aparecieron en una generación (filtro por generación). */
    @GET("generation/{id}")
    suspend fun getGeneration(@Path("id") id: Int): GenerationDto
}
