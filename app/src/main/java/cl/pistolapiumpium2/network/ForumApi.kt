// En: network/ForumApi.kt
package cl.pistolapiumpium2.network

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import cl.pistolapiumpium2.network.FirebaseTimestampDeserializer

object ForumApi {

    // Esta URL ahora actúa solo como un marcador de posición, ya que nuestras llamadas usan @Url.
    // Es buena práctica apuntarla a la región donde están tus funciones.
    private const val BASE_URL = "https://uc.a.run.app/"

    private val gson = GsonBuilder()
        .registerTypeAdapter(com.google.firebase.Timestamp::class.java, FirebaseTimestampDeserializer())
        .create()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl(BASE_URL)
        .build()

    val retrofitService: ForumApiService by lazy {
        retrofit.create(ForumApiService::class.java)
    }
}
