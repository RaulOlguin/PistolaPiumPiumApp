// En: ui/screens/HomeScreen.kt
package cl.pistolapiumpium2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

@Composable
fun HomeScreen(
    onNavigateToGun: () -> Unit,
    onNavigateToForum: () -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    currentUser: GoogleSignInAccount?
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Pistola Pium Pium",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botón para ir al modo disparo, siempre visible
        Button(onClick = onNavigateToGun, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("Modo Disparo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lógica condicional para el foro y el login
        if (currentUser == null) {
            // Si no hay usuario, mostrar botón de inicio de sesión
            Button(onClick = onSignInClick, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("Iniciar sesión con Google")
            }
        } else {
            // Si hay un usuario, saludarlo y mostrar botones de foro y cierre de sesión
            Text(
                text = "Bienvenido, ${currentUser.displayName}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(onClick = onNavigateToForum, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("Foro de Reunión")
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onSignOutClick) {
                Text("Cerrar Sesión")
            }
        }
    }
}
