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
import kotlinx.serialization.Serializable

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = modifier.fillMaxSize(),
    ) {
        composable<Home> {
            TimerScreen(
                onOpenMap = {
                    navController.navigate(Map)
                },
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
    }
}

@Serializable
data object Home

@Serializable
data object Map
