package com.example.examen1

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.examen1.components.AppScaffold
import com.example.examen1.onboarding.screens.OnboardingScreen
import com.example.examen1.onboarding.viewmodel.OnboardingViewModel
import com.example.examen1.pages.*
import com.example.examen1.utils.preferences.PreferencesManager
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
    controlTypeViewModel: ControlTypeViewModel,
    tagViewModel: TagViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Determinar la ruta inicial
    val startDestination = remember {
        if (PreferencesManager.isOnboardingCompleted(context)) {
            "login"
        } else {
            "onboarding"
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable("onboarding") {
            val viewModel = remember { OnboardingViewModel() }
            val state by viewModel.onboardingState.collectAsState()

            OnboardingScreen(
                viewModel = viewModel,
                onNext = {
                    if (state.onboardingComplete) {
                        PreferencesManager.setOnboardingCompleted(context)
                        navController.navigate("login") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    } else {
                        viewModel.nextPage()
                    }
                }
            )
        }

        composable("login") {
            LoginPage(modifier, navController, authViewModel, profileViewModel)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel, profileViewModel)
        }

        composable("chatbot") {
            ChatbotScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable("home") {
            AppScaffold(navController, activeProfileViewModel, authViewModel) {
                HomePage(
                    modifier = modifier,
                    navController = navController,
                    authViewModel = authViewModel,
                    activeProfileViewModel = activeProfileViewModel,
                    profileViewModel = profileViewModel,
                    foodEntryViewModel = foodEntryViewModel,
                    symptomEntryViewModel = symptomEntryViewModel,
                    stoolEntryViewModel = stoolEntryViewModel,
                    controlTypeViewModel = controlTypeViewModel
                )
            }
        }

        composable("profiles") {
            AppScaffold(navController, activeProfileViewModel, authViewModel) {
                ProfilesPage(
                    modifier = modifier,
                    navController = navController,
                    profileViewModel = profileViewModel,
                    activeProfileViewModel = activeProfileViewModel
                )
            }
        }

        composable(
            route = "control_type/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId")
            profileId?.let {
                AppScaffold(navController, activeProfileViewModel, authViewModel) {
                    ControlTypePage(
                        modifier = modifier,
                        navController = navController,
                        controlTypeViewModel = controlTypeViewModel,
                        foodEntryViewModel = foodEntryViewModel,
                        profileId = it
                    )
                }
            }
        }

        composable(
            route = "food_entry/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId")
            profileId?.let {
                AppScaffold(navController, activeProfileViewModel, authViewModel) {
                    FoodEntryPage(
                        modifier = modifier,
                        navController = navController,
                        viewModel = foodEntryViewModel,
                        controlTypeViewModel = controlTypeViewModel,
                        profileId = it,
                        tagViewModel = tagViewModel
                    )
                }
            }
        }

        composable(
            route = "food_entry_edit/{entryId}/{profileId}",
            arguments = listOf(
                navArgument("entryId") { type = NavType.StringType },
                navArgument("profileId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")
            val profileId = backStackEntry.arguments?.getString("profileId")
            if (entryId != null && profileId != null) {
                AppScaffold(navController, activeProfileViewModel, authViewModel) {
                    FoodEntryPage(
                        modifier = modifier,
                        navController = navController,
                        viewModel = foodEntryViewModel,
                        controlTypeViewModel = controlTypeViewModel,
                        entryId = entryId,
                        profileId = profileId,
                        tagViewModel = tagViewModel
                    )
                }
            }
        }

        composable(
            route = "symptom_entry/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId")
            profileId?.let {
                AppScaffold(navController, activeProfileViewModel, authViewModel) {
                    SymptomEntryPage(
                        modifier = modifier,
                        navController = navController,
                        viewModel = symptomEntryViewModel,
                        profileId = it
                    )
                }
            }
        }

        composable(
            route = "symptom_entry_edit/{entryId}/{profileId}",
            arguments = listOf(
                navArgument("entryId") { type = NavType.StringType },
                navArgument("profileId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")
            val profileId = backStackEntry.arguments?.getString("profileId")
            if (entryId != null && profileId != null) {
                AppScaffold(navController, activeProfileViewModel, authViewModel) {
                    SymptomEntryPage(
                        modifier = modifier,
                        navController = navController,
                        viewModel = symptomEntryViewModel,
                        entryId = entryId,
                        profileId = profileId
                    )
                }
            }
        }

        composable(
            route = "stool_entry/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId")
            profileId?.let {
                AppScaffold(navController, activeProfileViewModel, authViewModel) {
                    StoolEntryPage(
                        modifier = modifier,
                        navController = navController,
                        viewModel = stoolEntryViewModel,
                        profileId = it
                    )
                }
            }
        }

        composable(
            route = "stool_entry_edit/{entryId}/{profileId}",
            arguments = listOf(
                navArgument("entryId") { type = NavType.StringType },
                navArgument("profileId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")
            val profileId = backStackEntry.arguments?.getString("profileId")
            if (entryId != null && profileId != null) {
                AppScaffold(navController, activeProfileViewModel, authViewModel) {
                    StoolEntryPage(
                        modifier = modifier,
                        navController = navController,
                        viewModel = stoolEntryViewModel,
                        entryId = entryId,
                        profileId = profileId
                    )
                }
            }
        }

        composable("monthly_calendar/{profileId}") { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId")
            profileId?.let {
                AppScaffold(navController, activeProfileViewModel, authViewModel) {
                    MonthlyCalendarPage(
                        navController = navController,
                        foodEntryViewModel = foodEntryViewModel,
                        symptomEntryViewModel = symptomEntryViewModel,
                        stoolEntryViewModel = stoolEntryViewModel,
                        profileId = it
                    )
                }
            }
        }

        composable("history") {
            AppScaffold(navController, activeProfileViewModel, authViewModel) {
                HistoryPage(
                    modifier = modifier,
                    navController = navController,
                    historyViewModel = historyViewModel,
                    activeProfileViewModel = activeProfileViewModel,
                    tagViewModel = tagViewModel
                )
            }
        }

        composable("food_correlation") {
            AppScaffold(navController, activeProfileViewModel, authViewModel) {
                FoodCorrelationPage(
                    modifier = modifier,
                    navController = navController,
                    viewModel = viewModel(
                        factory = FoodCorrelationViewModelFactory(
                            foodEntryViewModel,
                            symptomEntryViewModel,
                            stoolEntryViewModel,
                            profileViewModel
                        )
                    ),
                    foodEntryViewModel = foodEntryViewModel,
                    symptomEntryViewModel = symptomEntryViewModel,
                    activeProfileViewModel = activeProfileViewModel,
                    profileViewModel = profileViewModel
                )
            }
        }

        composable("tag_management") {
            AppScaffold(navController, activeProfileViewModel, authViewModel) {
                TagManagementPage(
                    navController = navController,
                    viewModel = tagViewModel
                )
            }
        }

        composable("statistics") {
            AppScaffold(navController, activeProfileViewModel, authViewModel) {
                StatisticsPage(
                    modifier = modifier,
                    navController = navController,
                    viewModel = viewModel(
                        factory = StatisticsViewModelFactory(
                            foodEntryViewModel,
                            symptomEntryViewModel,
                            stoolEntryViewModel,
                            profileViewModel
                        )
                    ),
                    profileViewModel = profileViewModel
                )
            }
        }
    }
}
