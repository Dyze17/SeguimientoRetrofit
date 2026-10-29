package co.edu.uniquindio.pokeapi.domain.model

/** Lo que muestra cada tarjeta de la lista: número, nombre e imagen. */
data class PokemonSummary(
    val id: Int,
    val name: String,
    val imageUrl: String,
)

/** El detalle sale de dos endpoints: /pokemon/{id} (datos de combate) y /pokemon-species/{id} (descripción). */
data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val shinyImageUrl: String?,
    val types: List<String>,
    val heightMeters: Double,
    val weightKg: Double,
    val baseExperience: Int?,
    val abilities: List<PokemonAbility>,
    val stats: List<PokemonStat>,
    val species: PokemonSpeciesInfo?,
)

data class PokemonAbility(
    val name: String,
    val isHidden: Boolean,
)

data class PokemonStat(
    val name: String,
    val value: Int,
)

/** Datos de la especie. Es nulo cuando la API no tiene especie para esa forma (respondió 404). */
data class PokemonSpeciesInfo(
    val genus: String?,
    val description: String?,
    val habitat: String?,
    val generation: String?,
    val captureRate: Int?,
    val isLegendary: Boolean,
    val isMythical: Boolean,
)

data class Generation(
    val id: Int,
    val name: String,
)

/** Los campos de búsqueda de la lista. Si todos están vacíos se recorre el listado completo, página por página. */
data class PokemonFilter(
    val query: String = "",
    val type: String? = null,
    val generationId: Int? = null,
) {
    val isEmpty: Boolean get() = query.isBlank() && type == null && generationId == null
}
