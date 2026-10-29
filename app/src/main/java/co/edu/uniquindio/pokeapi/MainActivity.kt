package co.edu.uniquindio.pokeapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.edu.uniquindio.pokeapi.ui.navigation.PokeApiNavHost
import co.edu.uniquindio.pokeapi.ui.theme.PokeApiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeApiTheme {
                PokeApiNavHost()
            }
        }
    }
}
