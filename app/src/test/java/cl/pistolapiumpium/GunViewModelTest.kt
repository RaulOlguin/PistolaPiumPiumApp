// En: app/src/test/java/cl/pistolapiumpium/GunViewModelTest.kt
package cl.pistolapiumpium

import android.app.Application
import app.cash.turbine.test
import cl.pistolapiumpium.ui.GunAppState
import cl.pistolapiumpium.viewmodel.GunViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class GunViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: GunViewModel
    // Usamos un mock de Application porque el ViewModel lo requiere, pero no necesitamos su funcionalidad.
    private val mockApplication: Application = mock()

    @Before
    fun setUp() {
        // Reemplazamos el hilo principal por nuestro dispatcher de prueba
        Dispatchers.setMain(testDispatcher)
        viewModel = GunViewModel(mockApplication)
    }

    @After
    fun tearDown() {
        // Restauramos el hilo principal original para no afectar a otras pruebas
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando el ViewModel se crea, el estado inicial es el correcto`() = runTest {
        val initialState: GunAppState = viewModel.state.value

        // Comprobamos que la munición inicial es la máxima
        assertEquals(initialState.maxFireDurationMs, initialState.fireRemainingMs)
        // Comprobamos que la pistola no está vacía
        assertEquals(false, initialState.isGunEmpty)
    }

    @Test
    fun `cuando se llama a startFiring, el tiempo restante disminuye`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem() // Espera y consume el estado inicial

            // Comienza el disparo
            viewModel.startFiring()

            // Avanzamos el reloj virtual 100ms para que la corrutina de disparo se ejecute
            testDispatcher.scheduler.advanceTimeBy(100)

            val stateAfterFiring = awaitItem() // Espera y consume el nuevo estado

            // Afirmamos que la munición ha disminuido
            assert(stateAfterFiring.fireRemainingMs < initialState.fireRemainingMs) {
                "El tiempo restante (${stateAfterFiring.fireRemainingMs}) no disminuyó como se esperaba."
            }

            viewModel.stopFiring() // Detenemos para limpiar
            cancelAndIgnoreRemainingEvents() // Cancelamos para no esperar más eventos
        }
    }

    @Test
    fun `cuando se llama a reload, la municion se recarga al maximo`() = runTest {
        // 1. Disparamos un poco para gastar munición
        viewModel.startFiring()
        testDispatcher.scheduler.advanceTimeBy(500) // Avanza el tiempo 500ms
        viewModel.stopFiring()

        // 2. Nos aseguramos de que la munición realmente se ha gastado
        val stateBeforeReload = viewModel.state.value
        assert(stateBeforeReload.fireRemainingMs < stateBeforeReload.maxFireDurationMs)

        // 3. Llamamos a la función de recargar
        val reloadedSuccessfully = viewModel.reload()
        assertEquals(true, reloadedSuccessfully) // La recarga no debería fallar

        // 4. Comprobamos que el estado ahora refleja la munición completa
        val stateAfterReload = viewModel.state.value
        assertEquals(stateAfterReload.maxFireDurationMs, stateAfterReload.fireRemainingMs)
    }

    @Test
    fun `cuando se llama a stopFiring, el tiempo restante deja de disminuir`() = runTest {
        viewModel.state.test {
            awaitItem() // Ignoramos el estado inicial

            viewModel.startFiring()

            // Avanzamos el tiempo y verificamos que la munición baja
            testDispatcher.scheduler.advanceTimeBy(200)
            val stateWhileFiring = awaitItem()
            assert(stateWhileFiring.fireRemainingMs < 3000L)

            // Dejamos de disparar
            viewModel.stopFiring()

            // Avanzamos el tiempo OTRA VEZ. Si stopFiring funciona, la munición no debería cambiar.
            testDispatcher.scheduler.advanceTimeBy(500)

            // Comprobamos que el estado no ha cambiado desde que dejamos de disparar
            assertEquals(stateWhileFiring.fireRemainingMs, viewModel.state.value.fireRemainingMs)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
