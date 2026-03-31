package com.tolstykh.eatABurrita

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tolstykh.eatABurrita.ui.main.TimerScreen
import com.tolstykh.eatABurrita.ui.map.MapScreen
import com.tolstykh.eatABurrita.ui.recipes.RecipesScreen
import com.tolstykh.eatABurrita.ui.settings.SettingsScreen
import com.tolstykh.eatABurrita.ui.stats.StatsScreen
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = Home,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize(),
    ) {
        composable<Home> {
            TimerScreen(
                onOpenMap = { navController.navigate(Map) },
                onOpenSettings = { navController.navigate(Settings) },
                onOpenStats = { navController.navigate(Stats) },
                onOpenRecipes = { navController.navigate(Recipes) },
            )
        }
        composable<Map> {
            MapScreen(
                onBackPressed = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Home)
                    }
                }
            )
        }
        composable<Settings> {
            SettingsScreen(onBackPressed = { navController.popBackStack() })
        }
        composable<Stats> {
            StatsScreen(onBackPressed = { navController.popBackStack() })
        }
        composable<Recipes> {
            RecipesScreen(onBackPressed = { navController.popBackStack() })
        }
    }
}

@Serializable
@SerialName("Home")
data object Home

@Serializable
@SerialName("Map")
data object Map

@Serializable
@SerialName("Settings")
data object Settings

@Serializable
@SerialName("Stats")
data object Stats

@Serializable
@SerialName("Recipes")
data object Recipes
