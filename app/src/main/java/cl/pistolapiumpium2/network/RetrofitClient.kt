// En: network/RetrofitClient.kt
package cl.pistolapiumpium2.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {


    private const val FAKE_BASE_URL = "https://localhost/"


    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(FAKE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Usa Gson para convertir JSON
            .build()
    }

    // instancia pública de nuestro API.
    // La app llamará a esta variable para hacer las peticiones de red.
    val instance: ForumApiService by lazy {
        retrofit.create(ForumApiService::class.java)
    }
}
    