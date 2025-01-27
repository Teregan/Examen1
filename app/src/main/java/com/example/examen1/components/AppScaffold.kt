package com.example.examen1.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.examen1.ui.theme.MainGreen
import com.example.examen1.viewmodels.ActiveProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavController,
    activeProfileViewModel: ActiveProfileViewModel,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showDrawer = when (currentRoute) {
        "login", "signup" -> false
        else -> true
    }

    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawer(navController, activeProfileViewModel) {
                    scope.launch { drawerState.close() }
                }
            },
            gesturesEnabled = true
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(getRouteTitle(currentRoute)) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Menú")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MainGreen,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                }
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    content()
                }
            }
        }
    } else {
        content()
    }
}

private fun getRouteTitle(route: String?): String = when (route) {
    "home" -> "Inicio"
    "control_type/{profileId}" -> "Control de Alérgenos"
    "tag_management" -> "Etiquetas"
    "monthly_calendar/{profileId}" -> "Calendario"
    "history" -> "Historial"
    "food_correlation" -> "Correlaciones"
    "settings" -> "Ajustes"
    else -> ""
}