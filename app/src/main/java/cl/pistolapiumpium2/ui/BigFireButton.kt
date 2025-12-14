package cl.pistolapiumpium2.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BigFireButton(
    onClickStart: () -> Unit,
    onClickStop: () -> Unit,
    isGunEmpty: Boolean,
    onClickReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(250.dp)
            .pointerInput(isGunEmpty) { // Pasamos isGunEmpty para que el gestor se actualice
                if (!isGunEmpty) { // Solo detecta el gesto de mantener pulsado si no está vacía
                    detectTapGestures(
                        onPress = {
                            onClickStart()
                            tryAwaitRelease()
                            onClickStop()
                        }
                    )
                }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGunEmpty) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
            contentColor = if (isGunEmpty) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            if (isGunEmpty) {
                // Si está vacía, mostramos el botón de recarga
                Button(
                    onClick = onClickReload,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("RECARGAR", fontSize = 24.sp)
                }
            } else {
                // Si no, mostramos el texto de siempre
                Text(
                    text = "DISPARAR",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
