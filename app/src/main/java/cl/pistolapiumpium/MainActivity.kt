package cl.pistolapiumpium

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.pistolapiumpium.ui.GunAppRoute
import cl.pistolapiumpium.ui.screens.ForumScreen
import cl.pistolapiumpium.ui.screens.HomeScreen
import cl.pistolapiumpium.ui.theme.PistolaPiumPiumTheme
import cl.pistolapiumpium.util.ShakeDetector
import cl.pistolapiumpium.viewmodel.GunViewModel

// La ViewModelFactory no cambia
class GunViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GunViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GunViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Objeto con las rutas de navegación
object AppRoutes {
    const val HOME = "home"
    const val GUN = "gun"
    const val FORUM = "forum"
}

class MainActivity : ComponentActivity() {

    private val viewModel: GunViewModel by viewModels {
        GunViewModelFactory(application)
    }

    private lateinit var shakeDetector: ShakeDetector

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                println("Permiso de CÁMARA concedido por el usuario.")
            } else {
                println("Permiso de CÁMARA denegado por el usuario.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        askForCameraPermission()

        shakeDetector = ShakeDetector(this) {
            viewModel.reload()
        }

        // ================================================================== //
        // AQUI ESTÁ EL CAMBIO PRINCIPAL (Paso 7)                             //
        // ================================================================== //
        setContent {
            PistolaPiumPiumTheme {
                // 1. Crea el controlador de navegación, que recuerda el estado
                val navController = rememberNavController()

                // 2. Define el "mapa" de navegación con NavHost
                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.HOME // <-- La app ahora empieza en la pantalla de inicio
                ) {

                    // Define la pantalla de INICIO (HOME)
                    composable(route = AppRoutes.HOME) {
                        HomeScreen(
                            onNavigateToGun = { navController.navigate(AppRoutes.GUN) },
                            onNavigateToForum = { navController.navigate(AppRoutes.FORUM) }
                        )
                    }

                    // Define la pantalla del ARMA (GUN)
                    composable(route = AppRoutes.GUN) {
                        // Aquí llamamos al Composable que conecta el ViewModel con la UI
                        GunAppRoute(viewModel = viewModel)
                    }

                    // Define la pantalla del FORO (FORUM)
                    composable(route = AppRoutes.FORUM) {
                        ForumScreen()
                    }
                }
            }
        }
    }

    private fun askForCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                println("El permiso de cámara ya estaba concedido.")
            }
            else -> {
                println("Solicitando permiso de CÁMARA...")
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        shakeDetector.start()
    }

    override fun onPause() {
        super.onPause()
        shakeDetector.stop()
    }
}
