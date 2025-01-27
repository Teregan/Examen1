package com.example.examen1.pages

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.examen1.AuthState
import com.example.examen1.AuthViewModel
import com.example.examen1.R
import com.example.examen1.components.ActionButton
import com.example.examen1.components.InputField
import com.example.examen1.components.Message
import com.example.examen1.components.DecorativeCircles
import com.example.examen1.ui.theme.DarkTextColor
import com.example.examen1.ui.theme.LightGreen
import com.example.examen1.ui.theme.PrimaryPink
import com.example.examen1.ui.theme.PrimaryPinkBlended
import com.example.examen1.ui.theme.PrimaryPinkDark
import com.example.examen1.ui.theme.PrimaryPinkLight
import com.example.examen1.ui.theme.PrimaryYellow
import com.example.examen1.ui.theme.PrimaryYellowDark
import com.example.examen1.ui.theme.PrimaryYellowLight
import com.example.examen1.viewmodels.ProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginPage (modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel,
               profileViewModel: ProfileViewModel
) {
    val backgroundGradient = arrayOf(
        0f to PrimaryPinkBlended,
        1f to PrimaryPink
    )


    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }
    var isLoading by remember { mutableStateOf(false) }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // Reset los valores al entrar a la página
        email = ""
        password = ""
        isLoading = false
    }

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Authenticated -> {
                isLoading = true
                delay(500) // Dar tiempo para que se carguen los perfiles
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
                isLoading = false
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
    ){
        DecorativeCircles()

        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(vertical = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineMedium,
            color = DarkTextColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Logueate para continuar",
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
            placeholderText = "Clave",
            value = password,
            onValueChange = { password = it },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )


        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    authViewModel.login(email, password)
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
                text = if (isLoading) "Ingresando..." else "Ingresar",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Separator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .height(62.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿No tienes una cuenta?",
                style = MaterialTheme.typography.bodyMedium,
                color = DarkTextColor.copy(alpha = 0.7f)
            )
            TextButton(
                onClick = { navController.navigate("signup") }
            ) {
                Text(
                    text = "Regístrate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }

    }
}

@Composable
private fun Separator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DashedLine(
            modifier = Modifier.weight(weight = 1f)
        )
        Text(
            text = "Or",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
        DashedLine(
            modifier = Modifier.weight(weight = 1f)
        )
    }
}
@Composable
private fun DashedLine(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawLine(
            color = Color.White,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
            cap = StrokeCap.Round,
            strokeWidth = 1.dp.toPx()
        )
    }
}