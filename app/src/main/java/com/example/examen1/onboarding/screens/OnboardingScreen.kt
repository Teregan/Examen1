package com.example.examen1.onboarding.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
// Importaciones de tus propios componentes y viewmodels
import com.example.examen1.onboarding.components.OnboardingTemplate
import com.example.examen1.onboarding.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.onboardingState.collectAsState()
    val pages by viewModel.onboardingPages.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contenido principal con animación
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = state.currentPageIndex,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth }
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth }
                        )
                    }
                ) { pageIndex ->
                    OnboardingTemplate(
                        page = pages[pageIndex],
                        onNext = onNext,
                        isLastPage = pageIndex == pages.size - 1
                    )
                }
            }

            // Indicadores de página
            Row(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    PageIndicator(
                        isSelected = index == state.currentPageIndex,
                        onClick = { viewModel.goToPage(index) }
                    )
                }
            }

            // Botones de navegación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { viewModel.skipOnboarding() },
                    enabled = !state.onboardingComplete
                ) {
                    Text("Saltar")
                }

                if (!state.onboardingComplete) {
                    TextButton(
                        onClick = onNext
                    ) {
                        Text("Siguiente")
                    }
                }
            }
        }
    }
}

@Composable
private fun PageIndicator(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(CircleShape)
            .size(if (isSelected) 10.dp else 8.dp)
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
    )
}