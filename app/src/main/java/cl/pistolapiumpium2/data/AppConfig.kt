package cl.pistolapiumpium2.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,
    val vibrationLevel: Float = 0.5f,
    val enableFlashlight: Boolean = false,
    val soundVolume: Float = 1.0f,

    @ColumnInfo(name = "fire_duration") // Le decimos a Room cómo llamar a la columna
    val fireDuration: Long = 3000L

)
