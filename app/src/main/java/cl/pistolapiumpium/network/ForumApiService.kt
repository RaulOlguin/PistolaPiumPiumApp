// En: network/ForumApiService.kt
package cl.pistolapiumpium.network

import retrofit2.http.GET
import retrofit2.http.Url

interface ForumApiService {
    /**
     * Función suspendible para obtener la lista de posts del foro.
     * Realiza una petición GET a la URL base + "getPosts".
     * Retrofit se encargará de convertir la respuesta JSON en una List<ForumPost>.
     */
    @GET
    suspend fun getPosts(@Url url:String): List<ForumPost>

    // En el futuro, aquí añadiremos la función para crear un post nuevo.
    // @POST("createPost")
    // suspend fun createPost(@Body newPost: ForumPost): Response<Void>
}
    