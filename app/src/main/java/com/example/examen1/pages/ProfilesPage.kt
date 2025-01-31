package com.example.examen1.pages

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
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
import com.example.examen1.viewmodels.ActiveProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    profileViewModel: ProfileViewModel,
    activeProfileViewModel: ActiveProfileViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    var showAddDialog by remember { mutableStateOf(false) }
    var profileToEdit by remember { mutableStateOf<UserProfile?>(null) }
    val profiles = profileViewModel.profiles.observeAsState(initial = emptyList())
    val profileState = profileViewModel.profileState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(profileState.value) {
        when (val state = profileState.value) {
            is ProfileState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            is ProfileState.Success -> {
                showAddDialog = false
                profileToEdit = null
                // Si es el primer perfil creado, establecerlo como activo y navegar al home
                if (profiles.value.size == 1 && navController.previousBackStackEntry?.destination?.route in listOf("signup", "login")) {
                    profiles.value.firstOrNull()?.let { profile ->
                        activeProfileViewModel.setActiveProfile(profile)
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            }
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Perfiles Familiares",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Agregar Perfil
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Agregar perfil",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar Nuevo Perfil")
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Lista de perfiles
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(profiles.value) { profile ->
                ProfileCard(
                    profile = profile,
                    onEdit = { profileToEdit = profile },
                    onDelete = { profileViewModel.deleteProfile(profile.id) }
                )
            }
        }
    }

    // Diálogo para añadir/editar perfil
    if (showAddDialog || profileToEdit != null) {
        ProfileDialog(
            profile = profileToEdit,
            onDismiss = {
                showAddDialog = false
                profileToEdit = null
            },
            onSave = { profile ->
                if (profileToEdit != null) {
                    profileViewModel.updateProfile(profile.copy(id = profileToEdit!!.id))
                } else {
                    profileViewModel.addProfile(profile)
                }
            }
        )
    }
}

@Composable
fun ProfileCard(
    profile: UserProfile,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (profile.profileType == ProfileType.MOTHER)
                            Icons.Default.Person else Icons.Default.Face,
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = colorScheme.error
                        )
                    }
                }
            }

            Text(
                text = "Tipo: ${if (profile.profileType == ProfileType.MOTHER) "Madre" else "Infante"}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (profile.profileType == ProfileType.INFANT) {
                Text(
                    text = "Lactante: ${if (profile.isNursing == true) "Sí" else "No"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar perfil?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        "Eliminar",
                        color = colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        "Cancelar",
                        color = colorScheme.primary
                    )
                }
            },
            containerColor = colorScheme.surface,
            titleContentColor = colorScheme.onSurface,
            textContentColor = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileDialog(
    profile: UserProfile? = null,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var profileType by remember { mutableStateOf(profile?.profileType ?: ProfileType.INFANT) }
    var isNursing by remember { mutableStateOf(profile?.isNursing ?: false) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (profile != null) "Editar Perfil" else "Nuevo Perfil",
                color = colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    label = { Text("Nombre") },
                    isError = isError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurfaceVariant
                    )
                )

                if (isError) {
                    Text(
                        text = "El nombre es requerido",
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Tipo de Perfil
                Text(
                    text = "Tipo de Perfil",
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = profileType == ProfileType.INFANT,
                        onClick = { profileType = ProfileType.INFANT },
                        label = { Text("Infante") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary,
                            selectedLabelColor = colorScheme.onPrimary
                        )
                    )
                    FilterChip(
                        selected = profileType == ProfileType.MOTHER,
                        onClick = { profileType = ProfileType.MOTHER },
                        label = { Text("Madre") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary,
                            selectedLabelColor = colorScheme.onPrimary
                        )
                    )
                }

                // Opción de lactancia solo para infantes
                AnimatedVisibility(visible = profileType == ProfileType.INFANT) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isNursing,
                            onCheckedChange = { isNursing = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colorScheme.primary,
                                uncheckedColor = colorScheme.outline
                            )
                        )
                        Text(
                            text = "¿Es lactante?",
                            modifier = Modifier.padding(start = 8.dp),
                            color = colorScheme.onSurface
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
                    val updatedProfile = UserProfile(
                        id = profile?.id ?: "",
                        userId = profile?.userId ?: "", // Mantener el userId original
                        name = name.trim(),
                        profileType = profileType,
                        isNursing = if (profileType == ProfileType.INFANT) isNursing else null,
                        createdAt = profile?.createdAt ?: System.currentTimeMillis() // Mantener la fecha original
                    )
                    onSave(updatedProfile)
                }
            ) {
                Text(
                    text = if (profile != null) "Actualizar" else "Guardar",
                    color = colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancelar",
                    color = colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = colorScheme.surface,
        textContentColor = colorScheme.onSurface
    )
}