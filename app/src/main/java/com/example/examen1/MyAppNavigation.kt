package com.example.examen1

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.examen1.pages.HomePage
import com.example.examen1.pages.LoginPage
import com.example.examen1.pages.SignupPage

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login", builder ={
        composable("login") {
            LoginPage(modifier,navController, authViewModel)
        }
        composable("signup") {
            SignupPage(modifier,navController, authViewModel)
        }
        composable("home") {
            HomePage(modifier,navController, authViewModel)
        }
    })
}