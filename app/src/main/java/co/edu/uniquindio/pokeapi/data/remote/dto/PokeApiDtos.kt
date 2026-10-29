package co.edu.uniquindio.pokeapi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Solo los campos que usa la app: el Json de NetworkModule ignora el resto (ignoreUnknownKeys).

/** La referencia que PokeAPI usa en todas partes: un nombre y la URL del recurso, que termina en su id. */
@Serializable
data class NamedResourceDto(
    val name: String,
    val url: String,
)

/** GET /pokemon, /type y /generation: un listado paginado de referencias. */
@Serializable
data class NamedResourceListDto(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<NamedResourceDto>,
)

/** GET /pokemon/{id} */
@Serializable
data class PokemonDto(
    val id: Int,
    val name: String,
    /** En decímetros. */
    val height: Int,
    /** En hectogramos. */
    val weight: Int,
    @SerialName("base_experience") val baseExperience: Int? = null,
    val types: List<PokemonTypeSlotDto>,
    val stats: List<PokemonStatDto>,
    val abilities: List<PokemonAbilityDto>,
    val sprites: SpritesDto,
    val species: NamedResourceDto,
)

@Serializable
data class PokemonTypeSlotDto(
    val slot: Int,
    val type: NamedResourceDto,
)

@Serializable
data class PokemonStatDto(
    @SerialName("base_stat") val baseStat: Int,
    val stat: NamedResourceDto,
)

@Serializable
data class PokemonAbilityDto(
    val ability: NamedResourceDto,
    @SerialName("is_hidden") val isHidden: Boolean,
    val slot: Int,
)

@Serializable
data class SpritesDto(
    @SerialName("front_default") val frontDefault: String? = null,
    val other: OtherSpritesDto? = null,
)

@Serializable
data class OtherSpritesDto(
    @SerialName("official-artwork") val officialArtwork: ArtworkDto? = null,
)

@Serializable
data class ArtworkDto(
    @SerialName("front_default") val frontDefault: String? = null,
    @SerialName("front_shiny") val frontShiny: String? = null,
)

/** GET /pokemon-species/{id} */
@Serializable
data class PokemonSpeciesDto(
    val id: Int,
    val name: String,
    @SerialName("capture_rate") val captureRate: Int? = null,
    @SerialName("is_legendary") val isLegendary: Boolean = false,
    @SerialName("is_mythical") val isMythical: Boolean = false,
    val habitat: NamedResourceDto? = null,
    val generation: NamedResourceDto? = null,
    val genera: List<GenusDto> = emptyList(),
    @SerialName("flavor_text_entries") val flavorTextEntries: List<FlavorTextDto> = emptyList(),
)

@Serializable
data class GenusDto(
    val genus: String,
    val language: NamedResourceDto,
)

@Serializable
data class FlavorTextDto(
    @SerialName("flavor_text") val flavorText: String,
    val language: NamedResourceDto,
)

/** GET /type/{name} */
@Serializable
data class TypeDto(
    val id: Int,
    val name: String,
    val pokemon: List<TypePokemonDto>,
)

@Serializable
data class TypePokemonDto(
    val slot: Int,
    val pokemon: NamedResourceDto,
)

/** GET /generation/{id} */
@Serializable
data class GenerationDto(
    val id: Int,
    val name: String,
    @SerialName("pokemon_species") val pokemonSpecies: List<NamedResourceDto>,
)
