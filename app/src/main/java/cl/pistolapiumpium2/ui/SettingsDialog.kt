package cl.pistolapiumpium2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.pistolapiumpium2.data.AppConfig

@Composable
fun SettingsDialog(
    config: AppConfig,
    onSave: (AppConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val tempVibrationLevel = remember { mutableStateOf(config.vibrationLevel) }
    val tempEnableFlashlight = remember { mutableStateOf(config.enableFlashlight) }
    val tempSoundVolume = remember { mutableStateOf(config.soundVolume) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configuración de la Pistola") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Nivel de Vibración: ${(tempVibrationLevel.value * 100).toInt()}%")
                Slider(value = tempVibrationLevel.value, onValueChange = { tempVibrationLevel.value = it }, steps = 9, valueRange = 0f..1f, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Encender Linterna al Disparar")
                    Switch(checked = tempEnableFlashlight.value, onCheckedChange = { tempEnableFlashlight.value = it })
                }

                Text("Volumen del Disparo: ${(tempSoundVolume.value * 100).toInt()}%")
                Slider(value = tempSoundVolume.value, onValueChange = { tempSoundVolume.value = it }, steps = 9, valueRange = 0f..1f, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val newConfig = config.copy(
                    vibrationLevel = tempVibrationLevel.value,
                    enableFlashlight = tempEnableFlashlight.value,
                    soundVolume = tempSoundVolume.value
                )
                onSave(newConfig)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
