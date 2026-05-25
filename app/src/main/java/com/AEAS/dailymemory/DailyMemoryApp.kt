package com.AEAS.dailymemory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.AEAS.dailymemory.ui.theme.DailyMemoryTheme

@Composable
fun DailyMemoryApp() {
    val nav = rememberNavController()
    val currentUser = FirebaseProviders.auth.currentUser
    val startDestination = if (currentUser == null) "index" else "home"

    DailyMemoryTheme {
        NavHost(navController = nav, startDestination = startDestination) {

            composable("index") { IndexScreen(onStartClick = { nav.navigate("login") }) }

            composable("login") {
                LoginSignupScreen(onLoginSuccess = { nav.navigate("home") { popUpTo(0) } })
            }

            composable("home") {
                MenuScreen(
                    onNavigateToMemoryTypes = { nav.navigate("tipos_memoria") },
                    onNavigateToStory = { nav.navigate("story") },                    onNavigateToRelax = { },
                    onNavigateToChat = { nav.navigate("chat") },
                    onLogout = { nav.navigate("index") { popUpTo(0) } }
                )
            }

            composable("story") {
                StoryScreen(
                    onNavigateBack = { nav.navigateUp() },
                    onLevelClick = { levelSeleccionado ->
                        // Aquí navegas a la pantalla del juego específico o nivel
                        nav.navigate("game_screen/Simon_$levelSeleccionado")
                    }
                )
            }

            composable("chat") { ChatScreen(onNavigateBack = { nav.navigateUp() }) }



            composable("tipos_memoria") {
                TiposMemoriaScreen(
                    onNavigateBack = { nav.navigateUp() },
                    onNavigateToIconic = { nav.navigate("memoria_iconica") },
                    onNavigateToMuscular = { nav.navigate("memoria_muscular") },
                    onNavigateToEchoic = { nav.navigate("memoria_ecoica") }
                )
            }

            composable("memoria_iconica") {
                IconicaScreen(
                    onNavigateBack = { nav.navigateUp() },
                    onNavigateToGame = { gameAndDifficulty ->
                        nav.navigate("game_screen/$gameAndDifficulty")
                    }
                )
            }

            composable("memoria_muscular") {
                MuscularScreen(
                    onNavigateBack = { nav.navigateUp() },
                    onNavigateToGame = { gameAndDifficulty ->
                        nav.navigate("game_screen/$gameAndDifficulty")
                    }
                )
            }

            composable("memoria_ecoica") {
                EcoicaScreen(
                    onNavigateBack = { nav.navigateUp() },
                    onNavigateToGame = { gameAndDifficulty ->
                        nav.navigate("game_screen/$gameAndDifficulty")
                    }
                )
            }

            composable("game_screen/{gameData}") { backStackEntry ->
                val gameData = backStackEntry.arguments?.getString("gameData") ?: ""
                val parts = gameData.split("_")
                val gameName = parts.getOrNull(0) ?: ""
                val difficulty = parts.getOrNull(1) ?: "Fácil"

                when (gameName) {
                    "Color" -> {
                        ColorGameScreen(difficulty = difficulty, onNavigateBack = { nav.navigateUp() })
                    }
                    "Secuencia" -> {
                        SequenceGameScreen(difficulty = difficulty, onNavigateBack = { nav.navigateUp() })
                    }
                    "Memorama" -> {
                        MemoramaScreen(difficulty = difficulty, onNavigateBack = { nav.navigateUp() })
                    }
                    "Simon" -> {
                        SimonGameScreen(difficulty = difficulty, onNavigateBack = { nav.navigateUp() })
                    }
                    "Palabra" -> {
                        WordRepeatScreen(difficulty = difficulty, onNavigateBack = { nav.navigateUp() })
                    }
                    "Sonido" -> {
                        AudioPairsScreen(difficulty = difficulty, onNavigateBack = { nav.navigateUp() })
                    }
                    "Velocimetro" -> { // <--- AÑADIMOS EL VELOCÍMETRO AQUÍ (Sin acento porque así lo manda tu menú)
                        TypingSpeedScreen(difficulty = difficulty, onNavigateBack = { nav.navigateUp() })
                    }
                    "Lluvia" -> { // <--- AÑADIMOS EL NUEVO JUEGO AQUÍ
                        FallingTilesScreen(difficulty = difficulty, onNavigateBack = { nav.navigateUp() })
                    }
                    "Scary" -> { // <--- EL JUEGO FINAL AÑADIDO AQUÍ
                        DefendBaseScreen(difficulty = difficulty, onNavigateBack = { nav.navigateUp() })
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Juego $gameName en desarrollo...")
                        }
                    }
                }
            }
        }
    }
}