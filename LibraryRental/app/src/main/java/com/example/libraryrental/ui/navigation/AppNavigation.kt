package com.example.libraryrental.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.libraryrental.ui.screens.CatalogScreen
import com.example.libraryrental.ui.screens.DashboardScreen
import com.example.libraryrental.viewmodel.CatalogViewModel
import com.example.libraryrental.viewmodel.DashboardViewModel
import com.example.libraryrental.viewmodel.ViewModelFactory

@Composable
fun AppNavigation(factory: ViewModelFactory) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "catalog") {

        composable("catalog") {
            val viewModel: CatalogViewModel = viewModel(factory = factory)
            CatalogScreen(
                viewModel = viewModel,
                onGoToReservations = { navController.navigate("dashboard") }
            )
        }

        composable("dashboard") {
            val viewModel: DashboardViewModel = viewModel(factory = factory)
            DashboardScreen(
                viewModel = viewModel,
                onGoBack = { navController.popBackStack() }
            )
        }
    }
}
