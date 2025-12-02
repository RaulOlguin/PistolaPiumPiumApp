// En: ui/screens/ForumScreen.kt
package cl.pistolapiumpium.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.pistolapiumpium.network.ForumPost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    onNavigateBack: () -> Unit,
    forumViewModel: ForumViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState = forumViewModel.uiState
    // --> NUEVO: Estado para controlar si el diálogo de "Crear Post" está visible.
    var showCreatePostDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Foro PistolaPiumPium") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                }
            )
        },
        // --> NUEVO: FloatingActionButton para abrir el diálogo.
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreatePostDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Crear nuevo post")
            }
        }
    ) { innerPadding ->
        // Contenido principal de la pantalla
        ForumContent(
            uiState = uiState,
            modifier = Modifier.padding(innerPadding)
        )

        // --> NUEVO: El diálogo que se mostrará cuando showCreatePostDialog sea true.
        if (showCreatePostDialog) {
            CreatePostDialog(
                onDismiss = { showCreatePostDialog = false },
                onConfirm = { titulo, glosa ->
                    // Llamamos a la función del ViewModel para crear el post.
                    // Usamos un nombre de usuario temporal.
                    forumViewModel.createPost(titulo, glosa, "Usuario Temporal")
                    showCreatePostDialog = false // Cerramos el diálogo después de confirmar
                }
            )
        }
    }
}

// --> NUEVA FUNCIÓN COMPOSABLE: El diálogo para crear un post.
@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onConfirm: (titulo: String, glosa: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var titulo by remember { mutableStateOf("") }
    var glosa by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Crear Nuevo Tema",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = glosa,
                    onValueChange = { glosa = it },
                    label = { Text("Contenido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp) // Hacemos el campo de contenido más grande
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(titulo, glosa) },
                        // El botón solo se activa si el título y la glosa no están vacíos
                        enabled = titulo.isNotBlank() && glosa.isNotBlank()
                    ) {
                        Text("Publicar")
                    }
                }
            }
        }
    }
}


// --- El resto de tu código (ForumContent, PostList, PostCard) se queda igual ---
// ... (Aquí irían ForumContent, PostList y PostCard sin cambios)
// ... Asegúrate de que esas funciones sigan en el archivo.

@Composable
fun ForumContent(uiState: ForumUiState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.error != null) {
            Text(text = "Error: ${uiState.error}", color = Color.Red)
        } else {
            PostList(posts = uiState.posts)
        }
    }
}

@Composable
fun PostList(posts: List<ForumPost>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posts) { post ->
            PostCard(post = post)
        }
    }
}

@Composable
fun PostCard(post: ForumPost, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = post.titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            // Aquí puedes ajustar cómo muestras el timestamp, si es necesario
            Text(
                text = "por ${post.usuario} el ${post.fechaInicio.toDate()}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            Text(text = post.glosa, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
