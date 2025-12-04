// GunViewModel.kt (Versión con Inyección de Dependencias)

package cl.pistolapiumpium2.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.pistolapiumpium2.R
import cl.pistolapiumpium2.data.AppConfig
import cl.pistolapiumpium2.data.AppDatabase
import cl.pistolapiumpium2.data.ConfigDao
import cl.pistolapiumpium2.ui.GunAppState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// SOLUCIÓN: El ViewModel ahora RECIBE el ConfigDao, no lo crea.
open class GunViewModel(application: Application, private val configDao: ConfigDao) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(GunAppState())
    val state: StateFlow<GunAppState> = _state.asStateFlow()

    private var fireJob: Job? = null

    protected open var soundPlayer: MediaPlayer? = null
    private val vibrator: Vibrator
    private val cameraManager: CameraManager
    private val cameraId: String?

    init {
        val context = getApplication<Application>().applicationContext

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = try {
            cameraManager.cameraIdList.firstOrNull {
                cameraManager.getCameraCharacteristics(it)[android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE] == true
            }
        } catch(e: CameraAccessException) { null }

        initializeSoundPlayer(context)

        viewModelScope.launch {
            val savedConfig = configDao.getConfigFlow().firstOrNull()
            if (savedConfig != null) {
                val fireDuration = if (savedConfig.fireDuration > 0) savedConfig.fireDuration else 3000L
                _state.update {
                    it.copy(
                        config = savedConfig,
                        maxFireDurationMs = fireDuration,
                        fireRemainingMs = fireDuration
                    )
                }
                soundPlayer?.setVolume(savedConfig.soundVolume, savedConfig.soundVolume)
            }
        }
    }

    protected open fun initializeSoundPlayer(context: Context) {
        soundPlayer = MediaPlayer.create(context, R.raw.machine_gun).apply {
            isLooping = true
            setVolume(state.value.config.soundVolume, state.value.config.soundVolume)
        }
    }

    protected open fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    open fun startFiring() {
        if (fireJob?.isActive == true || state.value.isGunEmpty) return

        startGunEffects()
        fireJob = viewModelScope.launch {
            while (state.value.fireRemainingMs > 0) {
                delay(100L)
                _state.update { it.copy(fireRemainingMs = it.fireRemainingMs - 100L) }
            }
            stopGunEffects()
        }
    }

    open fun stopFiring() {
        fireJob?.cancel()
        stopGunEffects()
    }

    open fun reload(): Boolean {
        if (fireJob?.isActive == true) return false
        _state.update { it.copy(fireRemainingMs = it.maxFireDurationMs) }
        return true
    }

    private fun startGunEffects() = viewModelScope.launch {
        soundPlayer?.start()
        val vibrationPattern = longArrayOf(0, 50, 100)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(vibrationPattern, 0)
        }
        setTorch(true)
    }

    private fun stopGunEffects() {
        soundPlayer?.pause()
        soundPlayer?.seekTo(0)
        vibrator.cancel()
        setTorch(false)
    }

    private fun setTorch(enable: Boolean) {
        if (cameraId != null && hasCameraPermission()) {
            try { cameraManager.setTorchMode(cameraId, enable) } catch (e: Exception) { /* Ignorar */ }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer?.release()
        soundPlayer = null
        vibrator.cancel()
        setTorch(false)
    }

    open fun saveConfig(newConfig: AppConfig) {
        viewModelScope.launch {
            configDao.insertConfig(newConfig)
            val fireDuration = if (newConfig.fireDuration > 0) newConfig.fireDuration else 3000L
            _state.update {
                it.copy(
                    config = newConfig,
                    maxFireDurationMs = fireDuration,
                    fireRemainingMs = fireDuration
                )
            }
            soundPlayer?.setVolume(newConfig.soundVolume, newConfig.soundVolume)
        }
    }
}

// --- FÁBRICA PARA CREAR EL VIEWMODEL ---
// Esta clase le enseña a Android cómo crear tu GunViewModel ahora que tiene un nuevo parámetro
class GunViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GunViewModel::class.java)) {
            val dao = AppDatabase.getDatabase(application).configDao()
            @Suppress("UNCHECKED_CAST")
            return GunViewModel(application, dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
