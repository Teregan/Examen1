package com.example.examen1

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.examen1.pages.*
import com.example.examen1.viewmodels.*

@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    activeProfileViewModel: ActiveProfileViewModel,
    foodEntryViewModel: FoodEntryViewModel,
    symptomEntryViewModel: SymptomEntryViewModel,
    stoolEntryViewModel: StoolEntryViewModel,
    historyViewModel: HistoryViewModel
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home") {
            HomePage(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                activeProfileViewModel = activeProfileViewModel,
                profileViewModel = profileViewModel,
                foodEntryViewModel = foodEntryViewModel,
                symptomEntryViewModel = symptomEntryViewModel,
                stoolEntryViewModel = stoolEntryViewModel
            )
        }
        composable("profiles") {
            ProfilesPage(modifier, navController, profileViewModel)
        }

        // Rutas de Alimentación
        composable("food_entry") {
            FoodEntryPage(modifier, navController, foodEntryViewModel)
        }
        composable(
            route = "food_entry_edit/{entryId}",
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
            FoodEntryPage(
                modifier = modifier,
                navController = navController,
                viewModel = foodEntryViewModel,
                entryId = entryId
            )
        }

        // Rutas de Síntomas
        composable(
            route = "symptom_entry/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            SymptomEntryPage(
                modifier = modifier,
                navController = navController,
                viewModel = symptomEntryViewModel,
                profileId = profileId
            )
        }
        composable(
            route = "symptom_entry_edit/{entryId}/{profileId}",
            arguments = listOf(
                navArgument("entryId") { type = NavType.StringType },
                navArgument("profileId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            SymptomEntryPage(
                modifier = modifier,
                navController = navController,
                viewModel = symptomEntryViewModel,
                entryId = entryId,
                profileId = profileId
            )
        }

        // Rutas de Deposiciones
        composable(
            route = "stool_entry/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            StoolEntryPage(
                modifier = modifier,
                navController = navController,
                viewModel = stoolEntryViewModel,
                profileId = profileId
            )
        }
        composable(
            route = "stool_entry_edit/{entryId}/{profileId}",
            arguments = listOf(
                navArgument("entryId") { type = NavType.StringType },
                navArgument("profileId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            StoolEntryPage(
                modifier = modifier,
                navController = navController,
                viewModel = stoolEntryViewModel,
                entryId = entryId,
                profileId = profileId
            )
        }
        composable("history") {
            HistoryPage(
                modifier = modifier,
                navController = navController,
                historyViewModel = historyViewModel,
                activeProfileViewModel = activeProfileViewModel
            )
        }
        composable("food_correlation") {
            val foodCorrelationViewModel: FoodCorrelationViewModel = viewModel(
                factory = FoodCorrelationViewModelFactory(
                    foodEntryViewModel,
                    symptomEntryViewModel,
                    stoolEntryViewModel
                )
            )
            FoodCorrelationPage(
                navController = navController,
                viewModel = foodCorrelationViewModel,
                foodEntryViewModel = foodEntryViewModel
            )
        }
    }
}