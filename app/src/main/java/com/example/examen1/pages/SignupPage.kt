package com.example.examen1.pages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examen1.AuthState
import com.example.examen1.AuthViewModel
import com.example.examen1.R
import com.example.examen1.utils.LocalAlertsController
import com.example.examen1.viewmodels.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@Composable
fun SignupPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val alertsController = LocalAlertsController.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                isLoading = false
                errorMessage = null

                // Esperar a que los perfiles se carguen
                try {
                    withTimeout(5000) { // 5 segundos de timeout
                        while (profileViewModel.profiles.value?.isEmpty() == true) {
                            delay(100)
                        }
                    }

                    navController.navigate("home") {
                        popUpTo("signup") { inclusive = true }
                    }
                } catch (e: Exception) {
                    Log.e("SignupPage", "Error waiting for profiles: ${e.message}")
                    // Aún así navegamos al home, ya que el perfil debería estar creado
                    navController.navigate("home") {
                        popUpTo("signup") { inclusive = true }
                    }
                } finally {
                    isLoading = false
                }
            }
            is AuthState.Error -> {
                Log.e("SignupPage", "Authentication Error: ${(authState.value as AuthState.Error).message}")
                isLoading = false
                errorMessage = (authState.value as AuthState.Error).message
            }
            is AuthState.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is AuthState.Unauthenticated -> {
                isLoading = false
                errorMessage = null
            }
            else -> {
                isLoading = false
            }
        }
    }

    BoxWithConstraints {
        val isCompactScreen = maxHeight < 600.dp

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(colorScheme.background)
                .systemBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isCompactScreen)
                Arrangement.Top
            else
                Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isSystemInDarkTheme()) {
                                listOf(
                                    colorScheme.surface,
                                    colorScheme.primaryContainer
                                )
                            } else {
                                listOf(
                                    colorScheme.primaryContainer,
                                    colorScheme.surface
                                )
                            }
                        )
                    )
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center)
                )
            }

            // Mostrar alerta de error si existe
            errorMessage?.let { message ->
                ErrorAlert(
                    message = message,
                    onDismiss = { errorMessage = null }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSystemInDarkTheme()) 2.dp else 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Crear Cuenta",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Regístrate para comenzar",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_person),
                                contentDescription = null,
                                tint = colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Email
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedLabelColor = colorScheme.primary,
                            unfocusedLabelColor = colorScheme.onSurfaceVariant
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_key),
                                contentDescription = null,
                                tint = colorScheme.primary
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    authViewModel.signup(email, password)
                                } else {
                                    alertsController.showWarningAlert(
                                        title = "Aviso",
                                        message = "Por favor completa todos los campos",
                                        confirmText = "Entendido"
                                    )
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedLabelColor = colorScheme.primary,
                            unfocusedLabelColor = colorScheme.onSurfaceVariant
                        )
                    )

                    Button(
                        onClick = {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                authViewModel.signup(email, password)
                            } else {
                                alertsController.showWarningAlert(
                                    title = "Aviso",
                                    message = "Por favor completa todos los campos",
                                    confirmText = "Entendido"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                    ) {
                        Text(
                            text = if (isLoading) "Creando cuenta..." else "Crear cuenta",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes una cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onBackground
                )
                TextButton(
                    onClick = { navController.navigate("login") }
                ) {
                    Text(
                        text = "Inicia sesión",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}