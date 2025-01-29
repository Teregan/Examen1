package com.example.examen1.pages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.examen1.AuthState
import com.example.examen1.AuthViewModel
import com.example.examen1.R
import com.example.examen1.components.DecorativeCircles
import com.example.examen1.components.InputField
import com.example.examen1.models.ProfileState
import com.example.examen1.ui.theme.DarkTextColor
import com.example.examen1.ui.theme.LightGreen
import com.example.examen1.viewmodels.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

@Composable
fun SignupPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value, profileViewModel.profiles.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                isLoading = true

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
                Toast.makeText(
                    context,
                    (authState.value as AuthState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
                isLoading = false
            }
            is AuthState.Loading -> {
                isLoading = true
            }
            else -> {
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DecorativeCircles()

        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(vertical = 16.dp)
        )

        Text(
            text = "Crear Cuenta",
            style = MaterialTheme.typography.headlineMedium,
            color = DarkTextColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Regístrate para comenzar",
            style = MaterialTheme.typography.bodyLarge,
            color = DarkTextColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        InputField(
            leadingIconRes = R.drawable.ic_person,
            placeholderText = "Email",
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        InputField(
            leadingIconRes = R.drawable.ic_key,
            placeholderText = "Contraseña",
            value = password,
            onValueChange = { password = it },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    authViewModel.signup(email, password)
                } else {
                    Toast.makeText(
                        context,
                        "Por favor completa todos los campos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LightGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            Text(
                text = if (isLoading) "Creando cuenta..." else "Crear cuenta",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Ya tienes una cuenta?",
                style = MaterialTheme.typography.bodyMedium,
                color = DarkTextColor.copy(alpha = 0.7f)
            )
            TextButton(
                onClick = { navController.navigate("login") }
            ) {
                Text(
                    text = "Inicia sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}