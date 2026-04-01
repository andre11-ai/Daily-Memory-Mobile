package com.AEAS.dailymemory

import androidx.compose.runtime.Composable
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

            composable("index") {
                IndexScreen(
                    onStartClick = { nav.navigate("login") }
                )
            }

            composable("login") {
                LoginSignupScreen(
                    onLoginSuccess = {
                        nav.navigate("home") { popUpTo(0) }
                    }
                )
            }

            composable("home") {
                MenuScreen(
                    onNavigateToMemoryTypes = { /* Lo haremos en el Hito 2 */ },
                    onNavigateToStory = { /* Lo haremos más adelante */ },
                    onNavigateToRelax = { /* Lo haremos más adelante */ },
                    onLogout = {
                        nav.navigate("index") { popUpTo(0) }
                    }
                )
            }
        }
    }
}