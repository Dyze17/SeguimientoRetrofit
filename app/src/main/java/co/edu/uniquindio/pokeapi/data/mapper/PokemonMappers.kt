package co.edu.uniquindio.pokeapi.data.mapper

import co.edu.uniquindio.pokeapi.data.remote.dto.NamedResourceDto
import co.edu.uniquindio.pokeapi.data.remote.dto.PokemonDto
import co.edu.uniquindio.pokeapi.data.remote.dto.PokemonSpeciesDto
import co.edu.uniquindio.pokeapi.domain.model.Generation
import co.edu.uniquindio.pokeapi.domain.model.PokemonAbility
import co.edu.uniquindio.pokeapi.domain.model.PokemonDetail
import co.edu.uniquindio.pokeapi.domain.model.PokemonSpeciesInfo
import co.edu.uniquindio.pokeapi.domain.model.PokemonStat
import co.edu.uniquindio.pokeapi.domain.model.PokemonSummary

// Los textos de la especie se toman en español y, si la API no los tiene, en inglés.
private val PREFERRED_LANGUAGES = listOf("es", "en")

/** El id es el último segmento de la URL: https://pokeapi.co/api/v2/pokemon/25/ → 25. */
fun idFromUrl(url: String): Int = url.trimEnd('/').substringAfterLast('/').toInt()

/** Los listados solo traen nombre y URL; la imagen oficial se arma con el id, sin pedir el detalle de cada uno. */
fun officialArtworkUrl(id: Int): String =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

fun NamedResourceDto.toPokemonSummary(): PokemonSummary {
    val id = idFromUrl(url)
    return PokemonSummary(id = id, name = name, imageUrl = officialArtworkUrl(id))
}

fun NamedResourceDto.toGeneration(): Generation = Generation(id = idFromUrl(url), name = name)

fun PokemonDto.toPokemonDetail(species: PokemonSpeciesDto?): PokemonDetail = PokemonDetail(
    id = id,
    name = name,
    imageUrl = sprites.other?.officialArtwork?.frontDefault ?: sprites.frontDefault,
    shinyImageUrl = sprites.other?.officialArtwork?.frontShiny,
    types = types.sortedBy { it.slot }.map { it.type.name },
    heightMeters = height / 10.0,
    weightKg = weight / 10.0,
    baseExperience = baseExperience,
    abilities = abilities.sortedBy { it.slot }.map { PokemonAbility(name = it.ability.name, isHidden = it.isHidden) },
    stats = stats.map { PokemonStat(name = it.stat.name, value = it.baseStat) },
    species = species?.toSpeciesInfo(),
)

fun PokemonSpeciesDto.toSpeciesInfo(): PokemonSpeciesInfo = PokemonSpeciesInfo(
    genus = genera.firstInPreferredLanguage({ it.language.name }) { it.genus },
    // Cada juego trae su propia descripción: se usa la más reciente del idioma preferido.
    description = flavorTextEntries.asReversed()
        .firstInPreferredLanguage({ it.language.name }) { cleanFlavorText(it.flavorText) },
    habitat = habitat?.name,
    generation = generation?.name,
    captureRate = captureRate,
    isLegendary = isLegendary,
    isMythical = isMythical,
)

/** Las descripciones vienen con los saltos de línea y de página del juego original (\n, \u000c). */
fun cleanFlavorText(text: String): String = text.replace(Regex("\\s+"), " ").trim()

private inline fun <T> List<T>.firstInPreferredLanguage(language: (T) -> String, value: (T) -> String): String? =
    PREFERRED_LANGUAGES.firstNotNullOfOrNull { lang -> firstOrNull { language(it) == lang }?.let(value) }
