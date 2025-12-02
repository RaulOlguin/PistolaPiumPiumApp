package cl.pistolapiumpium.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


// Añade todos los imports que te pida Android Studio (con Alt+Enter)
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.pistolapiumpium.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GunScreen( // <--- RENOMBRADO AQUÍ
    state: GunAppState,
    actions: GunAppActions,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // El estado de si el diálogo es visible ahora vive aquí
    var isSettingsVisible by remember { mutableStateOf(false) }

    // La lógica de mostrar/ocultar el diálogo
    val showSettings = { isSettingsVisible = true }
    val hideSettings = { isSettingsVisible = false }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Pistola de Juguete 🔫") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // Usa la nueva función
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Icono de flecha
                            contentDescription = "Volver a Home"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = showSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Configuración"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)) {
                Text(
                    text = if (state.isGunEmpty) "¡RECARGA NECESARIA!" else "¡LISTO PARA DISPARAR!",
                    fontSize = 20.sp,
                    color = if (state.progress > 0.2f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.CenterHorizontally)
                        .width(200.dp)
                        .height(10.dp)
                )

                Text(text = "Tiempo Restante: ${state.fireRemainingMs / 1000}s", fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                Text(text = "Sacude el celular para RECARGAR.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
            }


            BigFireButton(
                onClickStart = actions::onStartFiring,
                onClickStop = actions::onStopFiring,
                isGunEmpty = state.isGunEmpty,
                onClickReload = actions::onReload,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
            )

            if (isSettingsVisible) {
                SettingsDialog(
                    config = state.config,
                    onSave = { newConfig ->
                        actions.onSaveConfig(newConfig)
                        hideSettings() // Ocultamos al guardar
                    },
                    onDismiss = hideSettings // Ocultamos al descartar
                )
            }
        }
    }
}
