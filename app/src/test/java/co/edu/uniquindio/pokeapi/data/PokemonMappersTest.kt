package co.edu.uniquindio.pokeapi.data

import co.edu.uniquindio.pokeapi.data.mapper.cleanFlavorText
import co.edu.uniquindio.pokeapi.data.mapper.idFromUrl
import co.edu.uniquindio.pokeapi.data.mapper.toPokemonSummary
import co.edu.uniquindio.pokeapi.data.mapper.toSpeciesInfo
import co.edu.uniquindio.pokeapi.data.remote.dto.FlavorTextDto
import co.edu.uniquindio.pokeapi.data.remote.dto.GenusDto
import co.edu.uniquindio.pokeapi.data.remote.dto.NamedResourceDto
import co.edu.uniquindio.pokeapi.data.remote.dto.PokemonSpeciesDto
import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonMappersTest {

    private fun language(code: String) = NamedResourceDto(code, "https://pokeapi.co/api/v2/language/$code/")

    @Test
    fun `the id is the last segment of the url`() {
        assertEquals(25, idFromUrl("https://pokeapi.co/api/v2/pokemon-species/25/"))
        assertEquals(10034, idFromUrl("https://pokeapi.co/api/v2/pokemon/10034"))
    }

    @Test
    fun `a list reference gets the official artwork of its id`() {
        val summary = pokemonRef(25, "pikachu").toPokemonSummary()

        assertEquals(25, summary.id)
        assertEquals(
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png",
            summary.imageUrl,
        )
    }

    @Test
    fun `flavor text loses the line and page breaks of the games`() {
        assertEquals("Cuando varios de estos Pokémon se juntan", cleanFlavorText("Cuando varios\nde estos\u000cPokémon  se juntan "))
    }

    @Test
    fun `species texts prefer the latest spanish entry`() {
        val species = PokemonSpeciesDto(
            id = 25,
            name = "pikachu",
            genera = listOf(GenusDto("Mouse Pokémon", language("en")), GenusDto("Pokémon Ratón", language("es"))),
            flavorTextEntries = listOf(
                FlavorTextDto("Old english", language("en")),
                FlavorTextDto("Viejo", language("es")),
                FlavorTextDto("Nuevo", language("es")),
                FlavorTextDto("New english", language("en")),
            ),
        )

        val info = species.toSpeciesInfo()

        assertEquals("Pokémon Ratón", info.genus)
        assertEquals("Nuevo", info.description)
    }

    @Test
    fun `species texts fall back to english`() {
        val species = PokemonSpeciesDto(
            id = 1,
            name = "bulbasaur",
            genera = listOf(GenusDto("Seed Pokémon", language("en"))),
            flavorTextEntries = listOf(FlavorTextDto("A strange seed", language("en"))),
        )

        val info = species.toSpeciesInfo()

        assertEquals("Seed Pokémon", info.genus)
        assertEquals("A strange seed", info.description)
    }
}
