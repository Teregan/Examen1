package com.example.examen1.pages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.examen1.AuthState
import com.example.examen1.AuthViewModel
import com.example.examen1.components.QuickAccessItem
import com.example.examen1.models.ActiveProfileState
import com.example.examen1.models.AllergenControl

import com.example.examen1.models.ProfileType
import com.example.examen1.ui.theme.DarkGreen
import com.example.examen1.ui.theme.GradientEnd
import com.example.examen1.ui.theme.GradientStart
import com.example.examen1.ui.theme.LightBlue
import com.example.examen1.ui.theme.MainGreen
import com.example.examen1.ui.theme.MediumGreen
import com.example.examen1.viewmodels.*
import com.example.examen1.ui.theme.PrimaryPinkDark
import java.text.SimpleDateFormat
import java.util.Locale

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
    stoolEntryViewModel: StoolEntryViewModel,
    controlTypeViewModel: ControlTypeViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val activeProfileState = activeProfileViewModel.activeProfileState.observeAsState()
    val profiles = profileViewModel.profiles.observeAsState(initial = emptyList())
    val foodEntries = foodEntryViewModel.foodEntries.observeAsState(initial = emptyList())
    val symptomEntries = symptomEntryViewModel.symptomEntries.observeAsState(initial = emptyList())
    val stoolEntries = stoolEntryViewModel.stoolEntries.observeAsState(initial = emptyList())
    val activeControls = controlTypeViewModel.activeControls.observeAsState(initial = emptyList())
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    // Agregar el estado para el diálogo
    val currentlyActiveControls = activeControls.value.filter { it.isCurrentlyActive() }
    Log.d("HomePage", "Currently active controls: ${currentlyActiveControls.size}")
    currentlyActiveControls.forEach { control ->
        Log.d("HomePage", """
        Control ID: ${control.id}
        Type: ${control.controlType}
        Start Date: ${control.startDateAsDate}
        End Date: ${control.endDateAsDate}
    """.trimIndent())
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showRegistroDialog by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var showSettingsContent by remember { mutableStateOf(false) }
    var showActiveControlDialog by remember { mutableStateOf(false) }

    // Función para verificar si hay un perfil activo
    fun hasActiveProfile(): Boolean {
        return activeProfileState.value is ActiveProfileState.Success
    }

    // Función para mostrar el mensaje de no hay perfil activo
    fun showNoActiveProfileMessage() {
        Toast.makeText(
            context,
            "Selecciona un perfil para realizar esta acción",
            Toast.LENGTH_SHORT
        ).show()
    }

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        activeProfileViewModel.loadLastActiveProfile()
    }

    LaunchedEffect(activeProfileState.value) {
        when (activeProfileState.value) {
            is ActiveProfileState.Success -> {
                val profileId = (activeProfileState.value as ActiveProfileState.Success).profile.id
                foodEntryViewModel.updateProfileFilter(profileId)
                controlTypeViewModel.updateProfileFilter(profileId)
            }
            else -> {
                foodEntryViewModel.updateProfileFilter(null)
                controlTypeViewModel.updateProfileFilter(null)
            }
        }
    }
    // Agregar este LaunchedEffect después de obtener los controles activos
    LaunchedEffect(activeControls.value) {
        val currentlyActiveControls = activeControls.value.filter { it.isCurrentlyActive() }
        showActiveControlDialog = currentlyActiveControls.isNotEmpty()
    }
    LaunchedEffect(profiles.value) {
        if (profiles.value.isEmpty()) {
            showAddProfileDialog = true
        }
    }
    if (profiles.value.isEmpty()) {
        NoProfilesView(
            onCreateProfile = { showAddProfileDialog = true },
            navController = navController
        )
    } else if (showSettingsContent) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    /*ListItem(
                        headlineContent = { Text("Gestionar Perfiles") },
                        leadingContent = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryPinkDark
                            )
                        },
                        modifier = Modifier.clickable {
                            selectedTabIndex = 0  // Resetear el tab seleccionado
                            showSettingsContent = false  // Ocultar la vista de ajustes
                            navController.navigate("profiles")
                        }
                    )*/
                    Divider()

                    ListItem(
                        headlineContent = { Text("Cerrar Sesión") },
                        leadingContent = {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = PrimaryPinkDark
                            )
                        },
                        modifier = Modifier.clickable {
                            authViewModel.signout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MainGreen,

                ) {
                    listOf(
                        Triple("Home", Icons.Default.Home, 0),
                        Triple("Calendario", Icons.Default.DateRange, 1),
                        Triple("Informes", Icons.Default.Face, 2),
                        Triple("Ajustes", Icons.Default.Settings, 3)
                    ).forEachIndexed { index, (label, icon, _) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label, tint = Color.White) },
                            label = {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,  // Fuente más pequeña
                                    maxLines = 1,  // Limitar a una línea
                                    overflow = TextOverflow.Ellipsis  // Manejar texto largo
                                )
                            },
                            selected = selectedTabIndex == index,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                indicatorColor = MediumGreen
                            ),
                            onClick = {
                                selectedTabIndex = index
                                when (index) {
                                    1->{
                                        if (hasActiveProfile()) {
                                            navController.navigate("monthly_calendar/${(activeProfileState.value as ActiveProfileState.Success).profile.id}")
                                        } else {
                                            showNoActiveProfileMessage()
                                        }
                                    }
                                    2 -> {
                                        if (hasActiveProfile()) {
                                            navController.navigate("food_correlation")
                                        } else {
                                            showNoActiveProfileMessage()
                                        }
                                    }
                                    3 -> {
                                        selectedTabIndex = index
                                        showSettingsContent = true
                                    }
                                    else -> {
                                        selectedTabIndex = index
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GradientEnd,GradientStart )
                        )
                    )
                    .padding(paddingValues),
            )  {
                // Perfil Activo Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Perfil Activo",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MainGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { navController.navigate("profiles") }
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Agregar perfil",
                                        tint = MainGreen
                                    )
                                }
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(profiles.value) { profile ->
                                    FilterChip(
                                        selected = when (activeProfileState.value) {
                                            is ActiveProfileState.Success ->
                                                (activeProfileState.value as ActiveProfileState.Success).profile.id == profile.id
                                            else -> false
                                        },
                                        onClick = { activeProfileViewModel.setActiveProfile(profile) },
                                        label = { Text(profile.name) },
                                        leadingIcon = {
                                            Icon(
                                                if (profile.profileType == ProfileType.MOTHER) Icons.Default.Person
                                                else Icons.Default.Face,
                                                contentDescription = null
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MainGreen,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Título de Accesos Rápidos
                item {
                    Text(
                        text = "Acceso Rápido",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                // Grid de Accesos Rápidos
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                QuickAccessItem(
                                    icon = Icons.Default.AddCircle,
                                    text = "Control Alérgenos",
                                    subtitle = if (activeControls.value.isNotEmpty())
                                        "${activeControls.value.size} activo(s)" else null
                                ) {
                                    if (hasActiveProfile()) {
                                        navController.navigate("control_type/${(activeProfileState.value as ActiveProfileState.Success).profile.id}")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                QuickAccessItem(
                                    icon = Icons.Default.Star,
                                    text = "Alimentos"
                                ) {
                                    if (hasActiveProfile()) {
                                        navController.navigate("food_entry/${(activeProfileState.value as ActiveProfileState.Success).profile.id}")
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                QuickAccessItem(
                                    icon = Icons.Default.Warning,
                                    text = "Síntomas"
                                ) {
                                    if (hasActiveProfile()) {
                                        navController.navigate("symptom_entry/${(activeProfileState.value as ActiveProfileState.Success).profile.id}")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                QuickAccessItem(
                                    icon = Icons.Default.CheckCircle,
                                    text = "Deposiciones"
                                ) {
                                    if (hasActiveProfile()) {
                                        navController.navigate("stool_entry/${(activeProfileState.value as ActiveProfileState.Success).profile.id}")
                                    }
                                }
                            }
                        }
                    }
                }
                /*Card(
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

                                    // Control de Alérgenos
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                "Control de Alérgenos",
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        },
                                        supportingContent = {
                                            if (activeControls.value.isNotEmpty()) {
                                                Text(
                                                    "${activeControls.value.size} control(es) activo(s)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        leadingContent = {
                                            Icon(
                                                Icons.Default.AddCircle,
                                                contentDescription = null,
                                                tint = PrimaryPinkDark
                                            )
                                        },
                                        modifier = Modifier.clickable {
                                            navController.navigate("control_type/$profileId")
                                        }
                                    )
                                    Divider()

                                    ListItem(
                                        headlineContent = { Text("Registro Alimento") },
                                        leadingContent = { Icon(Icons.Default.Star, contentDescription = null) },
                                        modifier = Modifier.clickable {
                                            if (hasActiveProfile()) {
                                                navController.navigate("food_entry/$profileId")
                                            } else {
                                                showNoActiveProfileMessage()
                                            }
                                        }
                                    )
                                    Divider()
                                    ListItem(
                                        headlineContent = { Text("Registro Síntomas") },
                                        leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                                        modifier = Modifier.clickable {
                                            if (hasActiveProfile()) {
                                                navController.navigate("symptom_entry/$profileId")
                                            } else {
                                                showNoActiveProfileMessage()
                                            }
                                        }
                                    )
                                    Divider()
                                    ListItem(
                                        headlineContent = { Text("Registro Deposiciones") },
                                        leadingContent = { Icon(Icons.Default.Check, contentDescription = null) },
                                        modifier = Modifier.clickable {
                                            if (hasActiveProfile()) {
                                                navController.navigate("stool_entry/$profileId")
                                            } else {
                                                showNoActiveProfileMessage()
                                            }
                                        }
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
                }*/

                // Últimos Registros
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
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
                            TextButton(onClick = { navController.navigate("history") }) {
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
                                when (activeProfileState.value) {
                                    is ActiveProfileState.Success -> {
                                        val currentProfileId = (activeProfileState.value as ActiveProfileState.Success).profile.id

                                        // Controles Activos
                                        activeControls.value
                                            .filter { it.isCurrentlyActive() }  // Asegurar que solo se muestren los realmente activos
                                            .take(3)
                                            .forEach { control ->
                                                ListItem(
                                                    headlineContent = { Text("Control de Alérgeno") },
                                                    supportingContent = {
                                                        Column {
                                                            Text("Tipo: ${control.controlType.displayName}")
                                                            val allergen = foodEntryViewModel.allergens.find { it.id == control.allergenId }
                                                            Text(allergen?.name ?: "")
                                                        }
                                                    },
                                                    leadingContent = {
                                                        Icon(
                                                            Icons.Default.AddCircle,
                                                            contentDescription = null,
                                                            tint = DarkGreen
                                                        )
                                                    }

                                                )
                                                HorizontalDivider()
                                            }

                                        // Alimentos
                                        foodEntries.value
                                            .filter { it.profileId == currentProfileId }
                                            .take(3)
                                            .forEach { entry ->
                                                ListItem(
                                                    headlineContent = { Text("Alimentos") },
                                                    supportingContent = { Text(entry.time) },
                                                    leadingContent = { Icon(Icons.Default.Star, contentDescription = null) },
                                                    trailingContent = { Text(dateFormatter.format(entry.date)) }
                                                )
                                                Divider()
                                            }

                                        // Síntomas
                                        symptomEntries.value
                                            .filter { it.profileId == currentProfileId }
                                            .take(3)
                                            .forEach { entry ->
                                                ListItem(
                                                    headlineContent = { Text("Síntomas") },
                                                    supportingContent = { Text(entry.time) },
                                                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                                                    trailingContent = { Text(dateFormatter.format(entry.date)) }
                                                )
                                                Divider()
                                            }

                                        // Deposiciones
                                        stoolEntries.value
                                            .filter { it.profileId == currentProfileId }
                                            .take(3)
                                            .forEach { entry ->
                                                ListItem(
                                                    headlineContent = { Text("Deposición") },
                                                    supportingContent = { Text(entry.time) },
                                                    leadingContent = { Icon(Icons.Default.Check, contentDescription = null) },
                                                    trailingContent = { Text(dateFormatter.format(entry.date)) }
                                                )
                                                Divider()
                                            }

                                        if (foodEntries.value.none { it.profileId == currentProfileId } &&
                                            symptomEntries.value.none { it.profileId == currentProfileId } &&
                                            stoolEntries.value.none { it.profileId == currentProfileId } &&
                                            activeControls.value.isEmpty()) {
                                            Text(
                                                "No hay registros para este perfil",
                                                modifier = Modifier.padding(16.dp),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                    else -> {
                                        Text(
                                            "Selecciona un perfil para ver los registros",
                                            modifier = Modifier.padding(16.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    // Dialogs
    /*if (showRegistroDialog) {
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

                                // Botón de Control de Alérgenos
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkGreen,
                                        contentColor = Color.White
                                    ),
                                    onClick = {
                                        showRegistroDialog = false
                                        navController.navigate("control_type/$profileId")
                                    }
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                                        Text("Control de Alérgenos")
                                    }
                                }

                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkGreen,
                                        contentColor = Color.White
                                    ),
                                    onClick = {
                                        showRegistroDialog = false
                                        navController.navigate("food_entry/$profileId")
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
                                        containerColor = DarkGreen,
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
                                        containerColor = DarkGreen,
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
    }*/

    if (showAddProfileDialog) {
        AddProfileDialog(
            onDismiss = { showAddProfileDialog = false },
            onAdd = { profile ->
                profileViewModel.addProfile(profile)
                showAddProfileDialog = false
            }
        )
    }

    if (showActiveControlDialog) {
        AlertDialog(
            onDismissRequest = { showActiveControlDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Control de Alérgeno Activo",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkGreen
                )
            },
            text = {
                val currentlyActiveControls = activeControls.value.filter { it.isCurrentlyActive() }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentlyActiveControls.forEach { control ->
                        val allergen = foodEntryViewModel.allergens.find { it.id == control.allergenId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MediumGreen.copy(alpha = 0.1f)
                            ),
                            border = BorderStroke(1.dp, MediumGreen)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Tipo: ${control.controlType.displayName}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = DarkGreen
                                )
                                Text(
                                    text = "Alérgeno: ${allergen?.name ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "Desde: ${dateFormatter.format(control.startDateAsDate)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "Hasta: ${dateFormatter.format(control.endDateAsDate)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                                if (control.notes.isNotEmpty()) {
                                    Text(
                                        text = "Notas: ${control.notes}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showActiveControlDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MediumGreen
                    )
                ) {
                    Text("Entendido")
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    }
}

@Composable
private fun NoProfilesView(
    onCreateProfile: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Bienvenido!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Para comenzar, necesitas crear un perfil",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateProfile,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryPinkDark,
                contentColor = Color.White
            )
        ) {
            Text("Crear Primer Perfil")
        }
    }
}