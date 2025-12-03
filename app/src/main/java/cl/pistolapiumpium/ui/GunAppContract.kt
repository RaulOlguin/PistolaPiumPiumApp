// /ui/GunAppContract.kt
package cl.pistolapiumpium.ui

import cl.pistolapiumpium.data.AppConfig

// El modelo de datos
data class GunAppState(
    val fireRemainingMs: Long = 3000, // Inicia lleno por defecto
    val maxFireDurationMs: Long = 3000,
    val config: AppConfig = AppConfig()
) {
    // SOLUCIÓN: Estas son propiedades calculadas. 
    // Siempre devuelven el valor correcto basado en el estado actual.
    val isGunEmpty: Boolean
        get() = fireRemainingMs <= 0

    val progress: Float
        get() = if (maxFireDurationMs > 0) fireRemainingMs.toFloat() / maxFireDurationMs.toFloat() else 0f
}

// El contrato de acciones
interface GunAppActions {
    fun onStartFiring()
    fun onStopFiring()
    fun onSaveConfig(newConfig: AppConfig)

    fun onReload(): Boolean
    fun onShowSettings()
    fun onDismissSettings()
}
