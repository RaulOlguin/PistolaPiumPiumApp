package cl.pistolapiumpium2

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.pistolapiumpium2.data.AppDatabase
import cl.pistolapiumpium2.ui.GunAppRoute
import cl.pistolapiumpium2.ui.screens.ForumScreen
import cl.pistolapiumpium2.ui.screens.HomeScreen
import cl.pistolapiumpium2.ui.theme.PistolaPiumPiumTheme
import cl.pistolapiumpium2.util.ShakeDetector
import cl.pistolapiumpium2.viewmodel.GunViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        shakeDetector = ShakeDetector(this) {
            viewModel.reload()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            PistolaPiumPiumTheme {
                val navController = rememberNavController()
                var currentUser by remember { mutableStateOf(GoogleSignIn.getLastSignedInAccount(this)) }

                val requestPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted: Boolean ->
                        if (isGranted) {
                            println("Permiso de CÁMARA concedido por el usuario.")
                        } else {
                            println("Permiso de CÁMARA denegado por el usuario.")
                        }
                    }
                )

                val signInLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                ) { result ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        currentUser = account
                    } catch (e: ApiException) {
                        println("Error al iniciar sesión: ${e.statusCode}")
                    }
                }

                LaunchedEffect(Unit) {
                    askForCameraPermission(requestPermissionLauncher)
                }

                NavHost(navController = navController, startDestination = AppRoutes.HOME) {
                    composable(route = AppRoutes.HOME) {
                        HomeScreen(
                            onNavigateToGun = { navController.navigate(AppRoutes.GUN) },
                            onNavigateToForum = { navController.navigate(AppRoutes.FORUM) },
                            onSignInClick = { signInLauncher.launch(googleSignInClient.signInIntent) },
                            onSignOutClick = {
                                googleSignInClient.signOut().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        currentUser = null
                                    }
                                }
                            },
                            currentUser = currentUser
                        )
                    }

                    composable(route = AppRoutes.GUN) {
                        GunAppRoute(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(route = AppRoutes.FORUM) {
                        ForumScreen(
                            currentUser = currentUser,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    private fun askForCameraPermission(launcher: androidx.activity.result.ActivityResultLauncher<String>) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                println("El permiso de cámara ya estaba concedido.")
            }
            else -> {
                println("Solicitando permiso de CÁMARA...")
                launcher.launch(Manifest.permission.CAMERA)
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
