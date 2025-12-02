// En: network/ForumApiService.kt
package cl.pistolapiumpium.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url // <-- ¡NUEVA IMPORTACIÓN!

data class NewPostRequest(
    val titulo: String,
    val glosa: String,
    val usuario: String
)

data class NewPostResponse(
    val message: String,
    val id: String
)

interface ForumApiService {

    @GET
    suspend fun getPosts(@Url url: String = "https://getposts-op3ae5k63q-uc.a.run.app"): List<ForumPost>

    @POST
    suspend fun createPost(
        @Url url: String = "https://createpost-op3ae5k63q-uc.a.run.app",
        @Body postData: NewPostRequest
    ): Response<NewPostResponse>
}
