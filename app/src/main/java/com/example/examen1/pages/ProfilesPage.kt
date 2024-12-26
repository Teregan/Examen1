package com.example.examen1.pages

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examen1.R
import com.example.examen1.components.ActionButton
import com.example.examen1.models.ProfileType
import com.example.examen1.models.UserProfile
import com.example.examen1.models.ProfileState
import com.example.examen1.viewmodels.ProfileViewModel
import com.example.examen1.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val profiles = profileViewModel.profiles.observeAsState(initial = emptyList())
    val profileState = profileViewModel.profileState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(profileState.value) {
        when (val state = profileState.value) {
            is ProfileState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }
    Column(
        modifier = modifier.fillMaxSize()
    ){
        SmallTopAppBar(
            title = { Text("Perfiles Familiares") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = PrimaryPinkDark,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Perfiles Familiares",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Add Profile Button
            ActionButton(
                text = "Agregar Nuevo Perfil",
                isNavigationArrowVisible = true,
                onClicked = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPinkDark,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shadowColor = PrimaryPinkDark,
                enabled = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Profiles List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(profiles.value) { profile ->
                    ProfileCard(
                        profile = profile,
                        onDelete = { profileViewModel.deleteProfile(profile.id) }
                    )
                }
            }
        }
    }


    // Add Profile Dialog
    if (showAddDialog) {
        AddProfileDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { profile ->
                profileViewModel.addProfile(profile)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProfileCard(
    profile: UserProfile,
    onDelete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // ... resto del código del Card ...

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("¿Estás seguro de eliminar este perfil?") },
                text = { Text("Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            try {
                                onDelete()
                                showDialog = false
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Error al eliminar el perfil"
                                showErrorDialog = true
                            }
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}

@Composable
fun AddProfileDialog(
    onDismiss: () -> Unit,
    onAdd: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var profileType by remember { mutableStateOf(ProfileType.INFANT) }
    var isNursing by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Nuevo Perfil") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    label = { Text("Nombre") },
                    isError = isError,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Text(
                        text = "El nombre es requerido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Tipo de Perfil
                Column {
                    Text(
                        text = "Tipo de Perfil",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = profileType == ProfileType.INFANT,
                            onClick = { profileType = ProfileType.INFANT },
                            label = { Text("Infante") }
                        )
                        FilterChip(
                            selected = profileType == ProfileType.MOTHER,
                            onClick = { profileType = ProfileType.MOTHER },
                            label = { Text("Madre") }
                        )
                    }
                }

                // Opción de lactancia solo para infantes
                AnimatedVisibility(visible = profileType == ProfileType.INFANT) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isNursing,
                            onCheckedChange = { isNursing = it }
                        )
                        Text(
                            text = "¿Es lactante?",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                        return@TextButton
                    }
                    val profile = UserProfile(
                        name = name.trim(),
                        profileType = profileType,
                        isNursing = if (profileType == ProfileType.INFANT) isNursing else null
                    )
                    onAdd(profile)
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}