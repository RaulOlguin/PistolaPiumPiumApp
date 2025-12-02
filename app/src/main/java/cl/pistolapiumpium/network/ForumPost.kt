// En: network/ForumPost.kt
package cl.pistolapiumpium.network

import com.google.gson.annotations.SerializedName
import java.util.Date // Importa la clase Date para hacer la conversión

//recreando un timesatamp en json
data class FirebaseTimestamp(
    @SerializedName("_seconds")
    val seconds: Long,

    @SerializedName("_nanoseconds")
    val nanoseconds: Long
) {
    //convierte el timestamp a un date
    fun toDate(): Date {
        return Date(seconds * 1000) // Multiplicamos por 1000 para pasar de segundos a milisegundos
    }
}

data class ForumPost(
    val id: String,
    val titulo: String,
    val glosa: String,
    val avatar: String,

    @SerializedName("usuario")
    val usuario: String,

    @SerializedName("fecha_inicio")
    val fechaInicio: FirebaseTimestamp
)
