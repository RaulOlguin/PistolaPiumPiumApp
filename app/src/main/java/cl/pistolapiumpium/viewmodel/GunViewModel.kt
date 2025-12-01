// GunViewModel.kt

package cl.pistolapiumpium.viewmodel

import android.Manifest // ✅ 1. Import para el permiso de CÁMARA
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager // ✅ 2. Import para comprobar permisos
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat // ✅ 3. Import para comprobar permisos de forma fácil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.pistolapiumpium.R
import cl.pistolapiumpium.data.AppConfig
import cl.pistolapiumpium.data.AppDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class GunViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application.applicationContext)
    private val configDao = db.configDao()

    // --- StateFlows sin cambios ---
    private val _ammo = MutableStateFlow(5)
    val ammo: StateFlow<Int> = _ammo.asStateFlow()

    private val _maxFireDurationMs = MutableStateFlow(3000L)
    open val maxFireDurationMs: StateFlow<Long> = _maxFireDurationMs.asStateFlow()

    private val _fireRemainingMs = MutableStateFlow(_maxFireDurationMs.value)
    open val fireRemainingMs: StateFlow<Long> = _fireRemainingMs.asStateFlow()

    private val _config = MutableStateFlow(AppConfig())
    open val config: StateFlow<AppConfig> = _config.asStateFlow()

    private var fireJob: Job? = null
    private var isFiringState = false

    private var soundPlayer: MediaPlayer? = null
    private val vibrator: Vibrator
    private val cameraManager: CameraManager
    private val cameraId: String?

    init {
        val context = getApplication<Application>().applicationContext

        // Inicialización del Vibrador (sin cambios, ya era correcta)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // Inicialización de la Cámara (sin cambios, ya era correcta)
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = try { // ✅ 4. Añadimos try-catch por si hay problemas de seguridad al listar cámaras
            cameraManager.cameraIdList.firstOrNull {
                cameraManager.getCameraCharacteristics(it).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch(e: CameraAccessException) {
            println("Error al acceder a la cámara para buscar flash: ${e.message}")
            null
        }

        initializeSoundPlayer()

        viewModelScope.launch {
            configDao.getConfigFlow().collect { loadedConfig ->
                if (loadedConfig != null) {
                    _config.value = loadedConfig
                    _maxFireDurationMs.value = loadedConfig.fireDuration
                    _fireRemainingMs.value = loadedConfig.fireDuration
                    soundPlayer?.setVolume(loadedConfig.soundVolume, loadedConfig.soundVolume)
                }
            }
        }
    }

    private fun initializeSoundPlayer() {
        soundPlayer = MediaPlayer.create(getApplication(), R.raw.machine_gun).apply {
            isLooping = true
            val initialVolume = _config.value.soundVolume
            setVolume(initialVolume, initialVolume)
        }
    }

    // --- startFiring, stopFiring, reload sin cambios ---
    open fun startFiring() {
        if (isFiringState || _fireRemainingMs.value <= 0) return
        isFiringState = true
        _ammo.value = 1
        startGunEffects()

        fireJob?.cancel()
        fireJob = viewModelScope.launch {
            while (_fireRemainingMs.value > 0) {
                delay(100L)
                _fireRemainingMs.value -= 100L
                if (_fireRemainingMs.value <= 0) {
                    stopFiring()
                }
            }
        }
    }

    open fun stopFiring() {
        if (!isFiringState) return
        isFiringState = false
        fireJob?.cancel()
        fireJob = null
        stopGunEffects()
    }

    open fun reload(): Boolean {
        if (isFiringState) {
            println("Intento de recarga fallido: el arma se está disparando.")
            return false
        }
        viewModelScope.launch {
            stopFiring()
            _fireRemainingMs.value = _maxFireDurationMs.value
            _ammo.value = 5
        }
        return true
    }

    // --- EFECTOS CORREGIDOS ---

    private fun startGunEffects() = viewModelScope.launch {
        // --- Sonido (sin cambios) ---
        if (soundPlayer?.isPlaying == false) {
            soundPlayer?.start()
        }

        // --- Vibración ---
        val vibrationPattern = longArrayOf(0, 500, 50)
        // ✅ 5. CORRECCIÓN VIBRACIÓN: El método hasVibrator() está obsoleto.
        // Ahora simplemente llamamos a vibrate y envolvemos en try-catch por si no hay vibrador o permisos.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(vibrationPattern, 0)
            }
        } catch (e: Exception) {
            println("Error al vibrar: ${e.message}")
        }

        // --- Flash ---
        if (cameraId != null) {
            // ✅ 6. CORRECCIÓN FLASH: Comprobamos el permiso de cámara explícitamente.
            val hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (hasCameraPermission) {
                try {
                    cameraManager.setTorchMode(cameraId, true)
                } catch (e: CameraAccessException) { // ✅ 7. CORRECCIÓN FLASH: Capturamos la excepción específica.
                    println("Error al encender el flash (CameraAccessException): ${e.message}")
                } catch (e: Exception) {
                    println("Error al encender el flash: ${e.message}")
                }
            } else {
                println("No se puede usar el flash: Permiso de cámara denegado.")
            }
        }
    }

    private fun stopGunEffects() {
        // --- Sonido (sin cambios) ---
        if (soundPlayer?.isPlaying == true) {
            soundPlayer?.pause()
            soundPlayer?.seekTo(0)
        }

        // --- Vibración ---
        vibrator.cancel()

        // --- Flash ---
        if (cameraId != null) {
            // ✅ 8. CORRECCIÓN FLASH: Comprobamos el permiso también al apagar.
            val hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (hasCameraPermission) {
                try {
                    cameraManager.setTorchMode(cameraId, false)
                } catch (e: CameraAccessException) { // ✅ 9. CORRECCIÓN FLASH: Capturamos la excepción específica.
                    println("Error al apagar el flash (CameraAccessException): ${e.message}")
                } catch (e: Exception) {
                    println("Error al apagar el flash: ${e.message}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGunEffects()
        soundPlayer?.release()
        soundPlayer = null
    }

    open fun saveConfig(newConfig: AppConfig) {
        viewModelScope.launch {
            configDao.insertConfig(newConfig)
        }
    }
}
