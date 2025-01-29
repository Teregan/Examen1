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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
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

@Composable
fun AppDrawer(
    navController: NavController,
    activeProfileViewModel: ActiveProfileViewModel,
    onCloseDrawer: () -> Unit
) {
    val activeProfile = activeProfileViewModel.activeProfileState.observeAsState()

    ModalDrawerSheet {
        DrawerHeader(activeProfile)
        Divider()

        DrawerNavigationItem("Inicio", Icons.Default.Home) {
            navController.navigate("home")
            onCloseDrawer()
        }

        DrawerNavigationItem("Control Alérgenos", Icons.Default.AddCircle) {
            activeProfile.value?.let {
                if (it is ActiveProfileState.Success) {
                    navController.navigate("control_type/${it.profile.id}")
                    onCloseDrawer()
                }
            }
        }

        DrawerNavigationItem("Registro Alimentos", Icons.Default.Star) {
            activeProfile.value?.let {
                if (it is ActiveProfileState.Success) {
                    navController.navigate("food_entry/${it.profile.id}")
                    onCloseDrawer()
                }
            }
        }

        DrawerNavigationItem("Registro Sintomas", Icons.Default.Warning) {
            activeProfile.value?.let {
                if (it is ActiveProfileState.Success) {
                    navController.navigate("symptom_entry/${it.profile.id}")
                    onCloseDrawer()
                }
            }
        }

        DrawerNavigationItem("Registro Deposiciones", Icons.Default.Check) {
            activeProfile.value?.let {
                if (it is ActiveProfileState.Success) {
                    navController.navigate("stool_entry/${it.profile.id}")
                    onCloseDrawer()
                }
            }
        }
        DrawerNavigationItem("Etiquetas", Icons.Default.Edit) {
            navController.navigate("tag_management")
            onCloseDrawer()
        }
        Divider()
        DrawerNavigationItem("Calendario", Icons.Default.DateRange) {
            activeProfile.value?.let {
                if (it is ActiveProfileState.Success) {
                    navController.navigate("monthly_calendar/${it.profile.id}")
                    onCloseDrawer()
                }
            }
        }

        DrawerNavigationItem("Estadisticas", Icons.Default.Analytics) {
            navController.navigate("statistics")
            onCloseDrawer()
        }

        DrawerNavigationItem("Historial", Icons.Default.List) {
            navController.navigate("history")
            onCloseDrawer()
        }

        DrawerNavigationItem("Correlaciones", Icons.Default.Face) {
            navController.navigate("food_correlation")
            onCloseDrawer()
        }

        Divider()


    }
}

@Composable
private fun DrawerNavigationItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, null) },
        label = { Text(text) },
        selected = false,
        onClick = onClick
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

