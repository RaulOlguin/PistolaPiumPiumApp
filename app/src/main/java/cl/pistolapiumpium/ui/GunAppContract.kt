// /ui/GunAppContract.kt
package cl.pistolapiumpium.ui

import cl.pistolapiumpium.data.AppConfig

// El modelo de datos
data class GunAppState(
    val fireRemainingMs: Long = 0,
    val maxFireDurationMs: Long = 3000,
    val config: AppConfig = AppConfig(),
    val isGunEmpty: Boolean = fireRemainingMs <= 0,
    val progress: Float = if (maxFireDurationMs > 0) fireRemainingMs.toFloat() / maxFireDurationMs.toFloat() else 0f
)

// El contrato de acciones
interface GunAppActions {
    fun onStartFiring()
    fun onStopFiring()
    fun onSaveConfig(newConfig: AppConfig)

    fun onReload(): Boolean
    fun onShowSettings()
    fun onDismissSettings()
}
