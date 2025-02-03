package com.example.examen1.onboarding.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.examen1.onboarding.data.OnboardingPage

@Composable
fun OnboardingTemplate(
    page: OnboardingPage,
    onNext: () -> Unit,
    isLastPage: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Imagen
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    id = when(page) {
                        is OnboardingPage.Welcome -> page.image
                        is OnboardingPage.Features -> page.image
                        is OnboardingPage.Doctor -> page.image
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(280.dp)
                    .padding(16.dp)
            )
        }

        // Texto
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = when(page) {
                    is OnboardingPage.Welcome -> page.title
                    is OnboardingPage.Features -> page.title
                    is OnboardingPage.Doctor -> page.title
                },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = when(page) {
                    is OnboardingPage.Welcome -> page.description
                    is OnboardingPage.Features -> page.description
                    is OnboardingPage.Doctor -> page.description
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        // Botón
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isLastPage) "Comenzar" else "Siguiente",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}