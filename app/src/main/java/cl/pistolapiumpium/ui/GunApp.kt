// /ui/GunApp.kt (versión refactorizada)
package cl.pistolapiumpium.ui
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import cl.pistolapiumpium.viewmodel.GunViewModel
import cl.pistolapiumpium.ui.screens.GunScreen

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

    //GunApp(state = state, actions = actions)
    GunScreen(state = state, actions = actions)
}

