package com.example.examen1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.examen1.ui.theme.Examen1Theme
import com.example.examen1.viewmodels.ActiveProfileViewModel
import com.example.examen1.viewmodels.FoodEntryViewModel
import com.example.examen1.viewmodels.ProfileViewModel
import com.example.examen1.viewmodels.StoolEntryViewModel
import com.example.examen1.viewmodels.SymptomEntryViewModel
import com.example.examen1.viewmodels.HistoryViewModel
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.examen1.utils.NotificationPermissionHandler
import com.example.examen1.viewmodels.ControlTypeViewModel

class MainActivity : ComponentActivity() {
    private lateinit var notificationPermissionHandler: NotificationPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        notificationPermissionHandler = NotificationPermissionHandler(this)
        notificationPermissionHandler.checkAndRequestNotificationPermission()


        val profileViewModel: ProfileViewModel by viewModels()
        val activeProfileViewModel: ActiveProfileViewModel by viewModels()
        val foodEntryViewModel: FoodEntryViewModel by viewModels()
        val symptomEntryViewModel: SymptomEntryViewModel by viewModels()
        val stoolEntryViewModel: StoolEntryViewModel by viewModels()
        val controlEntryViewModel: ControlTypeViewModel by viewModels()
        // Inicializa AuthViewModel con sus dependencias
        val authViewModel by viewModels<AuthViewModel> {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return AuthViewModel(
                        profileViewModel,
                        symptomEntryViewModel,
                        stoolEntryViewModel,
                        foodEntryViewModel
                    ) as T
                }
            }
        }
        // Inicializa HistoryViewModel con sus dependencias
        val historyViewModel by viewModels<HistoryViewModel> {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return HistoryViewModel(
                        foodEntryViewModel,
                        symptomEntryViewModel,
                        stoolEntryViewModel
                    ) as T
                }
            }
        }

        setContent {
            Examen1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        profileViewModel = profileViewModel,
                        activeProfileViewModel = activeProfileViewModel,
                        foodEntryViewModel = foodEntryViewModel,
                        symptomEntryViewModel = symptomEntryViewModel,
                        stoolEntryViewModel = stoolEntryViewModel,
                        historyViewModel = historyViewModel,
                        controlTypeViewModel = controlEntryViewModel
                    )
                }
            }
        }
    }
}
