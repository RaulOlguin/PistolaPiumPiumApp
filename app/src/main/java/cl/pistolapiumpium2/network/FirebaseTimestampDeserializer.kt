// En: network/FirebaseTimestampDeserializer.kt
package cl.pistolapiumpium2.network

import com.google.firebase.Timestamp
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * Esta clase le enseña a Gson cómo convertir un objeto JSON de Firestore
 * que representa un Timestamp en el objeto com.google.firebase.Timestamp de Kotlin.
 * El JSON de Firestore se ve así: { "_seconds": 1672531200, "_nanoseconds": 0 }
 */
class FirebaseTimestampDeserializer : JsonDeserializer<Timestamp> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Timestamp? {
        // Se asegura de que el JSON no sea nulo y sea un objeto
        if (json == null || !json.isJsonObject) {
            return null
        }

        val jsonObject: JsonObject = json.asJsonObject

        // Extrae los segundos y nanosegundos del objeto JSON
        val seconds = jsonObject.get("_seconds")?.asLong ?: 0L
        val nanoseconds = jsonObject.get("_nanoseconds")?.asInt ?: 0

        // Crea y devuelve un objeto Timestamp de Firebase
        return Timestamp(seconds, nanoseconds)
    }
}
    