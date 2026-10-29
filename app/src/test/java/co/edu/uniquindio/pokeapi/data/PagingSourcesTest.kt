package co.edu.uniquindio.pokeapi.data

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import co.edu.uniquindio.pokeapi.data.paging.FilteredPokemonPagingSource
import co.edu.uniquindio.pokeapi.data.paging.PokemonPagingSource
import co.edu.uniquindio.pokeapi.domain.model.AppError
import co.edu.uniquindio.pokeapi.domain.model.AppException
import co.edu.uniquindio.pokeapi.domain.model.PokemonSummary
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class PagingSourcesTest {

    private val config = PagingConfig(pageSize = 20, initialLoadSize = 20, enablePlaceholders = false)

    @Test
    fun `api pages continue at the next offset until the api has no next page`() = runTest {
        val api = FakePokeApiService().apply { allPokemon = (1..45).map { pokemonRef(it, "pokemon-$it") } }
        val pager = TestPager(config, PokemonPagingSource(api))

        val first = pager.refresh() as LoadResult.Page
        val second = pager.append() as LoadResult.Page
        val last = pager.append() as LoadResult.Page

        assertEquals((1..20).toList(), first.data.map { it.id })
        assertEquals(20, first.nextKey)
        assertEquals((21..40).toList(), second.data.map { it.id })
        assertEquals((41..45).toList(), last.data.map { it.id })
        assertNull(last.nextKey)
    }

    @Test
    fun `api page failures become app errors`() = runTest {
        val api = FakePokeApiService().apply { failure = IOException("sin internet") }
        val pager = TestPager(config, PokemonPagingSource(api))

        val result = pager.refresh() as LoadResult.Error

        assertEquals(AppError.Network, (result.throwable as AppException).error)
    }

    @Test
    fun `filtered results are delivered one page at a time`() = runTest {
        val results = (1..45).map { PokemonSummary(id = it, name = "pokemon-$it", imageUrl = "") }
        val pager = TestPager(config, FilteredPokemonPagingSource { results })

        val first = pager.refresh() as LoadResult.Page
        pager.append()
        val last = pager.append() as LoadResult.Page

        assertEquals(20, first.data.size)
        assertEquals((41..45).toList(), last.data.map { it.id })
        assertNull(last.nextKey)
    }

    @Test
    fun `empty filtered results end the list`() = runTest {
        val pager = TestPager(config, FilteredPokemonPagingSource { emptyList() })

        val page = pager.refresh() as LoadResult.Page

        assertEquals(0, page.data.size)
        assertNull(page.nextKey)
    }
}
