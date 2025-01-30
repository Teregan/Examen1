package com.example.examen1.pages

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examen1.models.Tag
import com.example.examen1.ui.theme.MainGreen
import com.example.examen1.viewmodels.TagViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManagementPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: TagViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val tags = viewModel.tags.observeAsState(initial = emptyList())
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(tags.value) {
        Log.d("TagManagementPage", "Tags loaded: ${tags.value.size}")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(16.dp)
    ) {
        // Título y botón de agregar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gestionar Etiquetas",
                style = MaterialTheme.typography.headlineMedium,
                color = colorScheme.primary
            )
            IconButton(
                onClick = {
                    Log.d("TagManagementPage", "Add button clicked")
                    showAddDialog = true
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar etiqueta",
                    tint = colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de etiquetas
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tags.value) { tag ->
                TagItem(
                    tag = tag,
                    onDelete = {
                        viewModel.deleteTag(tag.id)
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddTagDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, color ->
                viewModel.addTag(name, color)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TagItem(
    tag: Tag,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(android.graphics.Color.parseColor(tag.colorHex)).copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, Color(android.graphics.Color.parseColor(tag.colorHex)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurface
            )
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar etiqueta",
                    tint = colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddTagDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, color: String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var tagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4CAF50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surface,
        title = {
            Text(
                "Nueva Etiqueta",
                color = colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = {
                        tagName = it
                        Log.d("AddTagDialog", "Nombre de tag cambiado: $it")
                    },
                    label = { Text("Nombre de la etiqueta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurfaceVariant
                    )
                )

                Text(
                    "Seleccionar color",
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("#4CAF50", "#2196F3", "#FFC107", "#E91E63", "#9C27B0").forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(android.graphics.Color.parseColor(color)),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (selectedColor == color) Color.Black else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColor = color
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (tagName.isNotBlank()) {
                        onAdd(tagName, selectedColor)
                    }
                }
            ) {
                Text(
                    "Agregar",
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
        }
    )
}