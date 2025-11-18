package cl.pistolapiumpium.viewmodel

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.pistolapiumpium.R // Importamos R para acceder a los recursos
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

    // CREAMOS EL REPRODUCTOR DE SONIDO
    private var soundPlayer: MediaPlayer? = null

    init {
        //  inicializamos el sonido.
        initializeSoundPlayer()

        viewModelScope.launch {
            configDao.getConfigFlow().collect { loadedConfig ->
                if (loadedConfig != null) {
                    _config.value = loadedConfig
                    _maxFireDurationMs.value = loadedConfig.fireDuration
                    _fireRemainingMs.value = loadedConfig.fireDuration
                    // Actualizamos el volumen del reproductor cuando cambia la configuración
                    soundPlayer?.setVolume(loadedConfig.soundVolume, loadedConfig.soundVolume)
                }
            }
        }
    }

    // inicializando el reproductor de sonido
    private fun initializeSoundPlayer() {

        soundPlayer = MediaPlayer.create(getApplication(), R.raw.machine_gun).apply {
            // isLooping = true significa que el sonido se repetirá sin parar
             isLooping = true
            // volumen inicial
            val initialVolume = _config.value.soundVolume
            setVolume(initialVolume, initialVolume)
        }
    }

    private suspend fun getCurrentConfig(): AppConfig = _config.value

    open fun startFiring() {
        if (isFiringState || _fireRemainingMs.value <= 0) return
        isFiringState = true
        _ammo.value = 1
        startGunEffects() //

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
        stopGunEffects() // <-- Y esta también
    }

    open fun reload():Boolean {

        if(isFiringState) {
            println("Intento de recarga falla cuando se esta disparando")
            return false
        }

        viewModelScope.launch {
            stopFiring()
            _fireRemainingMs.value = _maxFireDurationMs.value
            _ammo.value = 5
        }

        return true
    }

    // FUNCIONES DE EFECTOS
    private fun startGunEffects() = viewModelScope.launch {
        // Solo iniciamos el sonido si no se está reproduciendo ya
        if (soundPlayer?.isPlaying == false) {
            soundPlayer?.start()
        }

    }

    private fun stopGunEffects() {
        // se pausa el sonido si se está reproduciendo
        if (soundPlayer?.isPlaying == true) {
            soundPlayer?.pause()
            //sonido al principio para el próximo disparo
            soundPlayer?.seekTo(0)
        }
    }

    // LIBERANDO RECURSOS CUANDO EL VIEWMODEL SE DESTRUYE
    override fun onCleared() {
        super.onCleared()
        soundPlayer?.release() // para no dejar fugas de memoria
        soundPlayer = null
    }

    open fun saveConfig(newConfig: AppConfig) {
        viewModelScope.launch {
            configDao.insertConfig(newConfig)
        }
    }
}
