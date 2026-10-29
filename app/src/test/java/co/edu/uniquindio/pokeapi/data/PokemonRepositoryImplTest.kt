package co.edu.uniquindio.pokeapi.data

import co.edu.uniquindio.pokeapi.data.repository.PokemonRepositoryImpl
import co.edu.uniquindio.pokeapi.domain.model.AppError
import co.edu.uniquindio.pokeapi.domain.model.AppException
import co.edu.uniquindio.pokeapi.domain.model.PokemonFilter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.IOException

class PokemonRepositoryImplTest {

    private val api = FakePokeApiService()
    private val repository = PokemonRepositoryImpl(api)

    @Before
    fun setUp() {
        api.allPokemon = listOf(
            pokemonRef(1, "bulbasaur"),
            pokemonRef(2, "ivysaur"),
            pokemonRef(3, "venusaur"),
            pokemonRef(4, "charmander"),
            pokemonRef(6, "charizard"),
            pokemonRef(25, "pikachu"),
            pokemonRef(122, "mr-mime"),
        )
        api.types["fire"] = typeOf(
            "fire",
            pokemonRef(255, "torchic"),
            pokemonRef(4, "charmander"),
            pokemonRef(10034, "charizard-mega-x"),
            pokemonRef(6, "charizard"),
        )
        api.generations[1] = generationOf(1, speciesRef(1, "bulbasaur"), speciesRef(4, "charmander"), speciesRef(6, "charizard"))
    }

    @Test
    fun `type and generation are crossed by number`() = runTest {
        val result = repository.findPokemon(PokemonFilter(type = "fire", generationId = 1))

        assertEquals(listOf(4, 6), result.map { it.id })
    }

    @Test
    fun `type results are sorted by number`() = runTest {
        val result = repository.findPokemon(PokemonFilter(type = "fire"))

        assertEquals(listOf(4, 6, 255, 10034), result.map { it.id })
    }

    @Test
    fun `text searches part of the name and a number searches that exact pokemon`() = runTest {
        assertEquals(listOf(1, 2, 3), repository.findPokemon(PokemonFilter(query = "saur")).map { it.id })
        assertEquals(listOf(25), repository.findPokemon(PokemonFilter(query = "#25")).map { it.id })
        assertEquals(listOf(2), repository.findPokemon(PokemonFilter(query = "2")).map { it.id })
        assertEquals(listOf(122), repository.findPokemon(PokemonFilter(query = " Mr Mime ")).map { it.id })
    }

    @Test
    fun `text is searched inside the selected type`() = runTest {
        val result = repository.findPokemon(PokemonFilter(query = "char", type = "fire"))

        assertEquals(listOf("charmander", "charizard", "charizard-mega-x"), result.map { it.name })
    }

    @Test
    fun `each type is requested only once`() = runTest {
        repository.findPokemon(PokemonFilter(type = "fire"))
        repository.findPokemon(PokemonFilter(type = "fire", query = "char"))

        assertEquals(1, api.typeRequests)
    }

    @Test
    fun `detail without species keeps the pokemon data`() = runTest {
        api.pokemon[10034] = pokemonDto(10034, "charizard-mega-x", speciesId = 6)

        val detail = repository.getPokemonDetail(10034)

        assertEquals("charizard-mega-x", detail.name)
        assertEquals(0.7, detail.heightMeters, 0.001)
        assertEquals(6.9, detail.weightKg, 0.001)
        assertNull(detail.species)
    }

    @Test
    fun `missing pokemon is reported as not found`() = runTest {
        val error = runCatching { repository.getPokemonDetail(99999) }.exceptionOrNull()

        assertEquals(AppError.NotFound, (error as AppException).error)
    }

    @Test
    fun `network failures are reported as network errors`() = runTest {
        api.failure = IOException("sin internet")

        val error = runCatching { repository.getTypes() }.exceptionOrNull()

        assertEquals(AppError.Network, (error as AppException).error)
    }
}
