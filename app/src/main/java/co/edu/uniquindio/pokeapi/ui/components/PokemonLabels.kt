package co.edu.uniquindio.pokeapi.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.pokeapi.R
import java.util.Locale

// La API escribe los nombres en inglés y en minúscula (fire, special-attack, generation-iv…). Aquí se pasan a lo que
// muestra la pantalla. Lo que no está en las tablas se muestra con el nombre de la API, en formato de título.

/** «mr-mime» → «Mr Mime». */
fun formatApiName(name: String): String =
    name.split('-').joinToString(" ") { part -> part.replaceFirstChar { it.titlecase(Locale.getDefault()) } }

/** 25 → «#025». */
fun formatPokemonNumber(id: Int): String = "#" + id.toString().padStart(3, '0')

private data class TypeStyle(@StringRes val label: Int, val color: Color)

private val TYPE_STYLES = mapOf(
    "normal" to TypeStyle(R.string.type_normal, Color(0xFFA8A77A)),
    "fire" to TypeStyle(R.string.type_fire, Color(0xFFEE8130)),
    "water" to TypeStyle(R.string.type_water, Color(0xFF6390F0)),
    "electric" to TypeStyle(R.string.type_electric, Color(0xFFF7D02C)),
    "grass" to TypeStyle(R.string.type_grass, Color(0xFF7AC74C)),
    "ice" to TypeStyle(R.string.type_ice, Color(0xFF96D9D6)),
    "fighting" to TypeStyle(R.string.type_fighting, Color(0xFFC22E28)),
    "poison" to TypeStyle(R.string.type_poison, Color(0xFFA33EA1)),
    "ground" to TypeStyle(R.string.type_ground, Color(0xFFE2BF65)),
    "flying" to TypeStyle(R.string.type_flying, Color(0xFFA98FF3)),
    "psychic" to TypeStyle(R.string.type_psychic, Color(0xFFF95587)),
    "bug" to TypeStyle(R.string.type_bug, Color(0xFFA6B91A)),
    "rock" to TypeStyle(R.string.type_rock, Color(0xFFB6A136)),
    "ghost" to TypeStyle(R.string.type_ghost, Color(0xFF735797)),
    "dragon" to TypeStyle(R.string.type_dragon, Color(0xFF6F35FC)),
    "dark" to TypeStyle(R.string.type_dark, Color(0xFF705746)),
    "steel" to TypeStyle(R.string.type_steel, Color(0xFFB7B7CE)),
    "fairy" to TypeStyle(R.string.type_fairy, Color(0xFFD685AD)),
)

fun typeColor(type: String): Color = TYPE_STYLES[type]?.color ?: Color(0xFF68A090)

@Composable
fun typeLabel(type: String): String = TYPE_STYLES[type]?.let { stringResource(it.label) } ?: formatApiName(type)

private val STAT_LABELS = mapOf(
    "hp" to R.string.stat_hp,
    "attack" to R.string.stat_attack,
    "defense" to R.string.stat_defense,
    "special-attack" to R.string.stat_special_attack,
    "special-defense" to R.string.stat_special_defense,
    "speed" to R.string.stat_speed,
)

@Composable
fun statLabel(stat: String): String = STAT_LABELS[stat]?.let { stringResource(it) } ?: formatApiName(stat)

private val HABITAT_LABELS = mapOf(
    "cave" to R.string.habitat_cave,
    "forest" to R.string.habitat_forest,
    "grassland" to R.string.habitat_grassland,
    "mountain" to R.string.habitat_mountain,
    "rare" to R.string.habitat_rare,
    "rough-terrain" to R.string.habitat_rough_terrain,
    "sea" to R.string.habitat_sea,
    "urban" to R.string.habitat_urban,
    "waters-edge" to R.string.habitat_waters_edge,
)

@Composable
fun habitatLabel(habitat: String): String = HABITAT_LABELS[habitat]?.let { stringResource(it) } ?: formatApiName(habitat)

private val REGIONS = mapOf(
    "i" to "Kanto", "ii" to "Johto", "iii" to "Hoenn", "iv" to "Sinnoh", "v" to "Teselia",
    "vi" to "Kalos", "vii" to "Alola", "viii" to "Galar", "ix" to "Paldea",
)

private fun generationNumeral(generation: String): String = generation.removePrefix("generation-")

/** «generation-iv» → «Gen. IV», para el chip del filtro. */
@Composable
fun generationShortLabel(generation: String): String =
    stringResource(R.string.generation_short, generationNumeral(generation).uppercase())

/** «generation-iv» → «IV · Sinnoh», para el detalle, donde la etiqueta ya dice «Generación». */
fun generationValueLabel(generation: String): String {
    val numeral = generationNumeral(generation)
    return listOfNotNull(numeral.uppercase(), REGIONS[numeral]).joinToString(" · ")
}

/** «generation-iv» → «Generación IV · Sinnoh». */
@Composable
fun generationLongLabel(generation: String): String {
    val numeral = generationNumeral(generation)
    val region = REGIONS[numeral]
    return if (region != null) {
        stringResource(R.string.generation_with_region, numeral.uppercase(), region)
    } else {
        stringResource(R.string.generation_long, numeral.uppercase())
    }
}
