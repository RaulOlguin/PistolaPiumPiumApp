// /ui/GunApp.kt (versión refactorizada)
package cl.pistolapiumpium.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import cl.pistolapiumpium.viewmodel.GunViewModel
import cl.pistolapiumpium.ui.screens.GunScreen
import cl.pistolapiumpium.data.AppConfig

//==================================================================
//          --- La conexión entre el ViewModel y la UI ---        //
//==================================================================
@Composable
fun GunAppRoute(
    viewModel: GunViewModel,
    onNavigateBack: () -> Unit
) {
    // SOLUCIÓN: Se recolecta el estado único desde el viewModel.
    val state by viewModel.state.collectAsState()

    // Las acciones no cambian, siguen llamando a los métodos del viewModel.
    val actions = object : GunAppActions {
        override fun onStartFiring() = viewModel.startFiring()
        override fun onStopFiring() = viewModel.stopFiring()
        override fun onSaveConfig(newConfig: AppConfig) = viewModel.saveConfig(newConfig)
        override fun onReload(): Boolean = viewModel.reload()

        // Estas acciones se manejan localmente en la UI, por lo que no necesitan llamar al viewModel.
        override fun onShowSettings() {}
        override fun onDismissSettings() {}
    }

    // Se pasa el estado y las acciones directamente a la pantalla principal.
    GunScreen(state = state, actions = actions, onNavigateBack = onNavigateBack)
}
