package cl.pistolapiumpium

import android.app.Application
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.pistolapiumpium.ui.GunAppRoute
import cl.pistolapiumpium.ui.theme.PistolaPiumPiumTheme
import cl.pistolapiumpium.util.ShakeDetector
import cl.pistolapiumpium.viewmodel.GunViewModel

class GunViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GunViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GunViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {


    private val viewModel: GunViewModel by viewModels {
        GunViewModelFactory(application)
    }

   //lateint sirve para detectar si hay giroscopio antes de usarlo
    private lateinit var shakeDetector: ShakeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        shakeDetector = ShakeDetector(this) {
            val reloadSuccessful = viewModel.reload()
            if (!reloadSuccessful) {
                println("Intento de recarga bloqueado: el arma se está disparando.")
            }
        }
        setContent {
            PistolaPiumPiumTheme {
                // La UI se configura de la misma manera
                GunAppRoute(viewModel = this.viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Es seguro llamar a start() porque onCreate() ya se ha ejecutado.
        shakeDetector.start()
    }

    override fun onPause() {
        super.onPause()
        // Es seguro llamar a stop() por la misma razón.
        shakeDetector.stop()
    }
}
