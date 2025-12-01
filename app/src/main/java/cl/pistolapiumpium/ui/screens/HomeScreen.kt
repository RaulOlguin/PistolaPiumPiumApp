// En: ui/screens/HomeScreen.kt
package cl.pistolapiumpium.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToGun: () -> Unit,
    onNavigateToForum: () -> Unit
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

        Button(onClick = onNavigateToGun, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("Modo Disparo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateToForum, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("Foro de Reunión")
        }
    }
}
