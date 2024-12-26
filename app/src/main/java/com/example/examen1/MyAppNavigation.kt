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
    historyViewModel: HistoryViewModel,
    controlTypeViewModel: ControlTypeViewModel
) {
    val navController = rememberNavController()
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(
            foodEntryViewModel,
            controlTypeViewModel
        )
    )
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginPage(modifier, navController, authViewModel, profileViewModel)
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
                stoolEntryViewModel = stoolEntryViewModel,
                controlTypeViewModel = controlTypeViewModel,
                notificationViewModel = notificationViewModel
            )
        }
        composable("profiles") {
            ProfilesPage(modifier, navController, profileViewModel)
        }

        // Control de Alérgenos
        composable(
            route = "control_type/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            ControlTypePage(
                modifier = modifier,
                navController = navController,
                controlTypeViewModel = controlTypeViewModel,
                foodEntryViewModel = foodEntryViewModel,
                profileId = profileId
            )
        }

        // Rutas de Alimentación
        composable(
            route = "food_entry/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            FoodEntryPage(
                modifier = modifier,
                navController = navController,
                viewModel = foodEntryViewModel,
                controlTypeViewModel = controlTypeViewModel,
                profileId = profileId
            )
        }
        composable(
            route = "food_entry_edit/{entryId}/{profileId}",
            arguments = listOf(
                navArgument("entryId") { type = NavType.StringType },
                navArgument("profileId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            FoodEntryPage(
                modifier = modifier,
                navController = navController,
                viewModel = foodEntryViewModel,
                controlTypeViewModel = controlTypeViewModel,
                entryId = entryId,
                profileId = profileId
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
                    stoolEntryViewModel,
                    profileViewModel  // Agregar profileViewModel
                )
            )
            FoodCorrelationPage(
                modifier = modifier,
                navController = navController,
                viewModel = foodCorrelationViewModel,
                foodEntryViewModel = foodEntryViewModel,
                activeProfileViewModel = activeProfileViewModel,
                profileViewModel = profileViewModel  // Agregar profileViewModel
            )
        }
    }
}