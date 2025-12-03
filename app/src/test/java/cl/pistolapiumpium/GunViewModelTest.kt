// En: app/src/test/java/cl/pistolapiumpium/GunViewModelTest.kt
package cl.pistolapiumpium

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.Vibrator
import app.cash.turbine.test
import cl.pistolapiumpium.data.AppConfig
import cl.pistolapiumpium.data.ConfigDao
import cl.pistolapiumpium.ui.GunAppState
import cl.pistolapiumpium.viewmodel.GunViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

// --- SOLUCIÓN: FAKECONFIGDAO CORREGIDO ---
// La interfaz real devuelve Flow<AppConfig>, no Flow<AppConfig?>.
// Nuestro "doble" debe cumplir el mismo contrato.
class FakeConfigDao : ConfigDao {
    private val _configFlow = MutableStateFlow<AppConfig?>(null)

    // Usamos filterNotNull para convertir el Flow<AppConfig?> interno en el Flow<AppConfig> público.
    // Esto simula el comportamiento de Room: si no hay datos, el flow está vacío.
    override fun getConfigFlow(): Flow<AppConfig> = _configFlow.filterNotNull()

    override suspend fun insertConfig(config: AppConfig) {
        _configFlow.value = config
    }

    // Un método extra para limpiar el estado entre pruebas si fuera necesario.
    fun clear() {
        _configFlow.value = null
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GunViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: GunViewModel
    // Mocks para Android Framework
    private val mockApplication: Application = mock()
    private val mockContext: Context = mock()
    private val mockVibrator: Vibrator = mock()
    private val mockCameraManager: CameraManager = mock()
    private val mockMediaPlayer: MediaPlayer = mock()
    // Dependencia FALSA para la base de datos
    private lateinit var configDao: FakeConfigDao

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // --- Mocks ---
        whenever(mockApplication.applicationContext).thenReturn(mockContext)
        whenever(mockContext.getSystemService(Context.VIBRATOR_SERVICE)).thenReturn(mockVibrator)
        whenever(mockContext.getSystemService(Context.CAMERA_SERVICE)).thenReturn(mockCameraManager)
        whenever(mockCameraManager.cameraIdList).thenReturn(emptyArray())

        // --- DAO Falso Y VIEWMODEL ---
        configDao = FakeConfigDao()

        // --- SOLUCIÓN AL "HANG" Y AL LOOPER ---
        // El init del ViewModel llama a `getConfigFlow().firstOrNull()`.
        // Si nuestro DAO falso está vacío, la prueba se colgará esperando un valor.
        // Por eso, insertamos un valor por defecto ANTES de crear el ViewModel.
        runTest {
            configDao.insertConfig(AppConfig())
        }

        // --- ViewModel de Prueba con Dependencias Inyectadas ---
        viewModel = object : GunViewModel(mockApplication, configDao) { 
            override fun initializeSoundPlayer(context: Context) {
                soundPlayer = mockMediaPlayer
            }
            override fun hasCameraPermission(): Boolean {
                return true
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        configDao.clear()
    }

    @Test
    fun `cuando el ViewModel se crea, el estado inicial es el correcto`() = runTest {
        val initialState: GunAppState = viewModel.state.value
        assertEquals(initialState.maxFireDurationMs, initialState.fireRemainingMs)
        assertEquals(false, initialState.isGunEmpty)
    }

    @Test
    fun `cuando se llama a startFiring, el tiempo restante disminuye`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()
            viewModel.startFiring()
            testDispatcher.scheduler.advanceTimeBy(100)
            val stateAfterFiring = awaitItem()
            assert(stateAfterFiring.fireRemainingMs < initialState.fireRemainingMs)
            viewModel.stopFiring()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cuando se llama a reload, la municion se recarga al maximo`() = runTest {
        viewModel.startFiring()
        testDispatcher.scheduler.advanceTimeBy(500)
        viewModel.stopFiring()

        val stateBeforeReload = viewModel.state.value
        assert(stateBeforeReload.fireRemainingMs < stateBeforeReload.maxFireDurationMs)

        val reloadedSuccessfully = viewModel.reload()
        assertEquals(true, reloadedSuccessfully)

        val stateAfterReload = viewModel.state.value
        assertEquals(stateAfterReload.maxFireDurationMs, stateAfterReload.fireRemainingMs)
    }

    @Test
    fun `cuando se llama a stopFiring, el tiempo restante deja de disminuir`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.startFiring()
            testDispatcher.scheduler.advanceTimeBy(200)
            val stateWhileFiring = awaitItem()
            assert(stateWhileFiring.fireRemainingMs < stateWhileFiring.maxFireDurationMs)
            viewModel.stopFiring()
            testDispatcher.scheduler.advanceTimeBy(500)
            assertEquals(stateWhileFiring.fireRemainingMs, viewModel.state.value.fireRemainingMs)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
