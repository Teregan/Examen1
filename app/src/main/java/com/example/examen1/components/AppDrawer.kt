package com.example.examen1.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examen1.models.ActiveProfileState
import com.example.examen1.models.ProfileType
import com.example.examen1.ui.theme.MainGreen
import com.example.examen1.viewmodels.ActiveProfileViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import com.example.examen1.AuthViewModel

@Composable
fun AppDrawer(
    navController: NavController,
    activeProfileViewModel: ActiveProfileViewModel,
    authViewModel: AuthViewModel,
    onCloseDrawer: () -> Unit
) {
    val activeProfile = activeProfileViewModel.activeProfileState.observeAsState()
    val colorScheme = MaterialTheme.colorScheme

    ModalDrawerSheet {
        DrawerHeader(activeProfile)
        Divider()

        DrawerNavigationItem(
            text = "Inicio",
            icon = Icons.Default.Home,
            onClick = {
                navController.navigate("home")
                onCloseDrawer()
            },
            colors = NavigationDrawerItemDefaults.colors()
        )

        DrawerNavigationItem(
            text = "Control Alérgenos",
            icon = Icons.Default.AddCircle,
            onClick = {
                activeProfile.value?.let {
                    if (it is ActiveProfileState.Success) {
                        navController.navigate("control_type/${it.profile.id}")
                        onCloseDrawer()
                    }
                }
            },
            colors = NavigationDrawerItemDefaults.colors()
        )

        DrawerNavigationItem(
            text = "Registro Alimentos",
            icon = Icons.Default.Restaurant,
            onClick = {
                activeProfile.value?.let {
                    if (it is ActiveProfileState.Success) {
                        navController.navigate("food_entry/${it.profile.id}")
                        onCloseDrawer()
                    }
                }
            },
            colors = NavigationDrawerItemDefaults.colors()
        )

        DrawerNavigationItem(
            text = "Registro Síntomas",
            icon = Icons.Default.MedicalServices,
            onClick = {
                activeProfile.value?.let {
                    if (it is ActiveProfileState.Success) {
                        navController.navigate("symptom_entry/${it.profile.id}")
                        onCloseDrawer()
                    }
                }
            },
            colors = NavigationDrawerItemDefaults.colors()
        )

        DrawerNavigationItem(
            text = "Registro Deposiciones",
            icon = Icons.Default.BabyChangingStation,
            onClick = {
                activeProfile.value?.let {
                    if (it is ActiveProfileState.Success) {
                        navController.navigate("stool_entry/${it.profile.id}")
                        onCloseDrawer()
                    }
                }
            },
            colors = NavigationDrawerItemDefaults.colors()
        )

        DrawerNavigationItem(
            text = "Etiquetas",
            icon = Icons.Default.Edit,
            onClick = {
                navController.navigate("tag_management")
                onCloseDrawer()
            },
            colors = NavigationDrawerItemDefaults.colors()
        )
        Divider()
        DrawerNavigationItem(
            text = "Calendario",
            icon = Icons.Default.DateRange,
            onClick = {
                activeProfile.value?.let {
                    if (it is ActiveProfileState.Success) {
                        navController.navigate("monthly_calendar/${it.profile.id}")
                        onCloseDrawer()
                    }
                }
            },
            colors = NavigationDrawerItemDefaults.colors()
        )

        DrawerNavigationItem(
            text = "Estadísticas",
            icon = Icons.Default.Analytics,
            onClick = {
                navController.navigate("statistics")
                onCloseDrawer()
            },
            colors = NavigationDrawerItemDefaults.colors()
        )

        DrawerNavigationItem(
            text = "Historial",
            icon = Icons.Default.List,
            onClick = {
                navController.navigate("history")
                onCloseDrawer()
            },
            colors = NavigationDrawerItemDefaults.colors()
        )

        DrawerNavigationItem(
            text = "Correlaciones",
            icon = Icons.Default.Face,
            onClick = {
                navController.navigate("food_correlation")
                onCloseDrawer()
            },
            colors = NavigationDrawerItemDefaults.colors()
        )

        Divider()

        DrawerNavigationItem(
            text = "Cerrar Sesión",
            icon = Icons.Default.ExitToApp,
            onClick = {
                authViewModel.signout()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
                onCloseDrawer()
            },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedIconColor = colorScheme.error,
                unselectedTextColor = colorScheme.error
            )
        )

    }
}

@Composable
private fun DrawerNavigationItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    colors: NavigationDrawerItemColors = NavigationDrawerItemDefaults.colors()
) {
    NavigationDrawerItem(
        icon = { Icon(icon, null) },
        label = { Text(text) },
        selected = false,
        onClick = onClick,
        colors = colors
    )
}

@Composable
private fun DrawerHeader(activeProfile: State<ActiveProfileState?>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(MainGreen),
        contentAlignment = Alignment.Center
    ) {
        when (val profile = activeProfile.value) {
            is ActiveProfileState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if(profile.profile.profileType == ProfileType.MOTHER)
                            Icons.Default.Person else Icons.Default.Face,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = profile.profile.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            else -> {
                Text("Seleccione un perfil", color = Color.White)
            }
        }
    }
}

