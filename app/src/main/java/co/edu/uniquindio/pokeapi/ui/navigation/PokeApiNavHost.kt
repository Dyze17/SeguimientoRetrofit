package co.edu.uniquindio.pokeapi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.uniquindio.pokeapi.ui.detail.PokemonDetailScreen
import co.edu.uniquindio.pokeapi.ui.list.PokemonListScreen
import kotlinx.serialization.Serializable

@Serializable
data object PokemonListRoute

@Serializable
data class PokemonDetailRoute(val id: Int)

@Composable
fun PokeApiNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = PokemonListRoute) {
        composable<PokemonListRoute> {
            PokemonListScreen(
                onPokemonClick = { id -> navController.navigate(PokemonDetailRoute(id)) },
            )
        }
        composable<PokemonDetailRoute> {
            PokemonDetailScreen(onBack = { navController.navigateUp() })
        }
    }
}
