// /ui/GunApp.kt (versión refactorizada)
package cl.pistolapiumpium.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.pistolapiumpium.viewmodel.GunViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GunApp(
    state: GunAppState,
    actions: GunAppActions,
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
            .padding(paddingValues), contentAlignment = Alignment.Center) {
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
                onClickReload = actions::onReload
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

//==================================================================
//          --- La conexión entre el ViewModel y la UI ---        //
//==================================================================
@Composable
fun GunAppRoute(viewModel: GunViewModel) {
    // Este Composable se encarga de "traducir" el ViewModel al estado que la UI espera
    val fireRemainingMs by viewModel.fireRemainingMs.collectAsState()
    val maxFireDurationMs by viewModel.maxFireDurationMs.collectAsState()
    val currentConfig by viewModel.config.collectAsState()

    // Creamos el objeto de estado
    val state = GunAppState(
        fireRemainingMs = fireRemainingMs,
        maxFireDurationMs = maxFireDurationMs,
        config = currentConfig
    )

    // Creamos el objeto de acciones
    val actions = object : GunAppActions {
        override fun onStartFiring() = viewModel.startFiring()
        override fun onStopFiring() = viewModel.stopFiring()
        override fun onSaveConfig(newConfig: cl.pistolapiumpium.data.AppConfig) = viewModel.saveConfig(newConfig)

        override fun onReload() = viewModel.reload()

        // Dejamos estas vacías por ahora, ya que la visibilidad se maneja dentro de GunApp
        override fun onShowSettings() {}
        override fun onDismissSettings() {}
    }

    GunApp(state = state, actions = actions)
}


// ==============================================================================================
//                                        PREVIEWS                                             //
// ==============================================================================================

// creamos un objeto de acciones fake para poder usar el preview
private val FakeActions = object : GunAppActions {
    override fun onStartFiring() {}
    override fun onStopFiring() {}
    override fun onSaveConfig(newConfig: cl.pistolapiumpium.data.AppConfig) {}

    override fun onReload() {}

    override fun onShowSettings() {}
    override fun onDismissSettings() {}
}

//previews de la app
@Preview(showBackground = true, name = "App Lista")
@Composable
private fun GunAppPreview() {
    // Creamos un estado de ejemplo
    val state = GunAppState(fireRemainingMs = 1500, maxFireDurationMs = 3000)
    GunApp(state = state, actions = FakeActions)
}

@Preview(showBackground = true, name = "App Sin Munición")
@Composable
private fun GunAppEmptyPreview() {
    // Creamos otro estado de ejemplo
    val state = GunAppState(fireRemainingMs = 0, maxFireDurationMs = 3000)
    GunApp(state = state, actions = FakeActions)
}
