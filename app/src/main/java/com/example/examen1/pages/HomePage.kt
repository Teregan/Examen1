package com.example.examen1.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examen1.AuthState
import com.example.examen1.AuthViewModel
import com.example.examen1.models.ActiveProfileState
import com.example.examen1.models.ProfileType
import com.example.examen1.viewmodels.*
import com.example.examen1.ui.theme.PrimaryPinkDark
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    activeProfileViewModel: ActiveProfileViewModel,
    profileViewModel: ProfileViewModel,
    foodEntryViewModel: FoodEntryViewModel,
    symptomEntryViewModel: SymptomEntryViewModel,
    stoolEntryViewModel: StoolEntryViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val activeProfileState = activeProfileViewModel.activeProfileState.observeAsState()
    val profiles = profileViewModel.profiles.observeAsState(initial = emptyList())
    val foodEntries = foodEntryViewModel.foodEntries.observeAsState(initial = emptyList())
    val symptomEntries = symptomEntryViewModel.symptomEntries.observeAsState(initial = emptyList())
    val stoolEntries = stoolEntryViewModel.stoolEntries.observeAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showRegistroDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    // Cargar perfil activo cuando se inicia
    LaunchedEffect(Unit) {
        activeProfileViewModel.loadLastActiveProfile()

    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = PrimaryPinkDark,
                contentColor = Color.White
            ) {
                listOf(
                    Triple("Home", Icons.Default.Home, 0),
                    Triple("Registros", Icons.Default.Edit, 1),
                    Triple("Historial", Icons.Default.DateRange, 2),
                    Triple("Informes", Icons.Default.Face, 3),
                    Triple("Ajustes", Icons.Default.Settings, 4)
                ).forEachIndexed { index, (label, icon, _) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label, tint = Color.White) },
                        label = { Text(label, color = Color.White) },
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            when (index) {
                                1 -> showRegistroDialog = true
                                2 -> navController.navigate("history")
                                else -> {
                                    // Navegación para las otras opciones de la barra inferior
                                    selectedTabIndex = index
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Perfil Activo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryPinkDark,
                    contentColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Perfil Activo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        profiles.value.forEach { profile ->
                            FilterChip(
                                selected = when (activeProfileState.value) {
                                    is ActiveProfileState.Success ->
                                        (activeProfileState.value as ActiveProfileState.Success).profile.id == profile.id
                                    else -> false
                                },
                                onClick = { activeProfileViewModel.setActiveProfile(profile) },
                                label = { Text(profile.name) },
                                leadingIcon = {
                                    if (profile.profileType == ProfileType.MOTHER) {
                                        Icon(Icons.Default.Person, "Madre")
                                    } else {
                                        Icon(Icons.Default.Face, "Niño")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Accesos Rápidos
            Text(
                text = "Acceso Rápido",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    activeProfileState.value?.let { state ->
                        when (state) {
                            is ActiveProfileState.Success -> {
                                val profileId = state.profile.id
                                ListItem(
                                    headlineContent = { Text("Registro Alimento") },
                                    leadingContent = { Icon(Icons.Default.Star, contentDescription = null) },
                                    modifier = Modifier.clickable { navController.navigate("food_entry") }
                                )
                                Divider()
                                ListItem(
                                    headlineContent = { Text("Registro Síntomas") },
                                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                                    modifier = Modifier.clickable { navController.navigate("symptom_entry/$profileId") }
                                )
                                Divider()
                                ListItem(
                                    headlineContent = { Text("Registro Deposiciones") },
                                    leadingContent = { Icon(Icons.Default.Check, contentDescription = null) },
                                    modifier = Modifier.clickable { navController.navigate("stool_entry/$profileId") }
                                )
                            }
                            is ActiveProfileState.Error -> {
                                Text("Selecciona un perfil para comenzar", modifier = Modifier.padding(16.dp))
                            }
                            else -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                    }
                }
            }

            // Últimos Registros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimos Registros",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { /* Navegar a ver todos */ }) {
                    Text("Ver más")
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Mostrar últimos registros de alimentación
                    foodEntries.value.take(3).forEach { entry ->
                        ListItem(
                            headlineContent = { Text("Alimentos") },
                            supportingContent = { Text(entry.time) },
                            leadingContent = { Icon(Icons.Default.Star, contentDescription = null) },
                            trailingContent = { Text(entry.date.toString()) }
                        )
                        Divider()
                    }

                    // Mostrar últimos registros de síntomas
                    symptomEntries.value.take(3).forEach { entry ->
                        ListItem(
                            headlineContent = { Text("Síntomas") },
                            supportingContent = { Text(entry.time) },
                            leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                            trailingContent = { Text(entry.date.toString()) }
                        )
                        Divider()
                    }

                    // Mostrar últimos registros de deposiciones
                    stoolEntries.value.take(3).forEach { entry ->
                        ListItem(
                            headlineContent = { Text("Deposición") },
                            supportingContent = { Text(entry.time) },
                            leadingContent = { Icon(Icons.Default.Check, contentDescription = null) },
                            trailingContent = { Text(entry.date.toString()) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
    if (showRegistroDialog) {
        AlertDialog(
            onDismissRequest = { showRegistroDialog = false },
            title = { Text("Seleccione un registro") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    activeProfileState.value?.let { state ->
                        when (state) {
                            is ActiveProfileState.Success -> {
                                val profileId = state.profile.id
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryPinkDark,
                                        contentColor = Color.White
                                    ),
                                    onClick = {
                                        showRegistroDialog = false
                                        navController.navigate("food_entry")
                                    }
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null)
                                        Text("Registro de Alimentos")
                                    }
                                }

                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryPinkDark,
                                        contentColor = Color.White
                                    ),
                                    onClick = {
                                        showRegistroDialog = false
                                        navController.navigate("symptom_entry/$profileId")
                                    }
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null)
                                        Text("Registro de Síntomas")
                                    }
                                }

                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryPinkDark,
                                        contentColor = Color.White
                                    ),
                                    onClick = {
                                        showRegistroDialog = false
                                        navController.navigate("stool_entry/$profileId")
                                    }
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                        Text("Registro de Deposiciones")
                                    }
                                }
                            }
                            else -> Unit
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRegistroDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
    /*Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "Home page", fontSize = 32.sp)


        ActionButton(
            text = "Gestionar Perfiles",
            isNavigationArrowVisible = true,
            onClicked = { navController.navigate("profiles") },
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryPinkDark,
                contentColor = Color.White
            ),
            shadowColor = PrimaryPinkDark,
            enabled = true
        )
        ActionButton(
            text = "Registrar Alimentación",
            isNavigationArrowVisible = true,
            onClicked = { navController.navigate("food_entry") },
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryPinkDark,
                contentColor = Color.White
            ),
            shadowColor = PrimaryPinkDark,
            enabled = true
        )
        ActionButton(
            text = "Ver Registros de Alimentación",
            isNavigationArrowVisible = true,
            onClicked = { navController.navigate("food_entries") },
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryPinkDark,
                contentColor = Color.White
            ),
            shadowColor = PrimaryPinkDark,
            enabled = true
        )

        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = {
            authViewModel.signout()
        }) {
            Text(text = "Sign out")
        }
    }*/
