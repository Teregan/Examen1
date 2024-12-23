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

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { showDialog = true }) {

                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Eliminar perfil"
                    )
                }
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { /* Dismiss dialog */ },
                    title = { Text("¿Estás seguro de eliminar este perfil?") },
                    text = { Text("Esta acción no se puede deshacer.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onDelete()
                                showDialog = false
                            }
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {showDialog = false}) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            Text(
                text = when (profile.profileType) {
                    ProfileType.MOTHER -> "Madre"
                    ProfileType.INFANT -> "Infante"
                },
                style = MaterialTheme.typography.bodyMedium
            )

            if (profile.profileType == ProfileType.INFANT && profile.isNursing == true) {
                Text(
                    text = "Lactante",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryPink
                )
            }
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Nuevo Perfil") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tipo de Perfil:")
                    RadioButton(
                        selected = profileType == ProfileType.INFANT,
                        onClick = { profileType = ProfileType.INFANT }
                    )
                    Text("Infante")
                    RadioButton(
                        selected = profileType == ProfileType.MOTHER,
                        onClick = { profileType = ProfileType.MOTHER }
                    )
                    Text("Madre")
                }

                AnimatedVisibility(visible = profileType == ProfileType.INFANT) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isNursing,
                            onCheckedChange = { isNursing = it }
                        )
                        Text("Es lactante")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val profile = UserProfile(
                        name = name,
                        profileType = profileType,
                        isNursing = if (profileType == ProfileType.INFANT) isNursing else null
                    )
                    onAdd(profile)
                },
                enabled = name.isNotBlank()
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