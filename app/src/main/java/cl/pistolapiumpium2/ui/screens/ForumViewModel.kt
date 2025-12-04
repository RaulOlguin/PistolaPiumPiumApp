package cl.pistolapiumpium2.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.pistolapiumpium2.network.ForumApi
import cl.pistolapiumpium2.network.ForumPost
import cl.pistolapiumpium2.network.NewPostRequest
import kotlinx.coroutines.launch
import java.io.IOException

data class ForumUiState(
    val posts: List<ForumPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ForumViewModel : ViewModel() {

    var uiState by mutableStateOf(ForumUiState())
        private set

    init {
        refreshPosts()
    }

    fun refreshPosts() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val posts = ForumApi.retrofitService.getPosts()
                uiState = uiState.copy(posts = posts, isLoading = false)
            } catch (e: IOException) {
                uiState = uiState.copy(error = "Error de red: ${e.message}", isLoading = false)
                Log.e("ForumViewModel", "Error de red al obtener posts: ${e.message}")
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Error inesperado: ${e.message}", isLoading = false)
                Log.e("ForumViewModel", "Error inesperado al obtener posts: ${e.message}")
            }
        }
    }

    fun createPost(titulo: String, glosa: String, usuario: String) {
        viewModelScope.launch {

            try {
                val request = NewPostRequest(titulo, glosa, usuario)

                val response = ForumApi.retrofitService.createPost(postData = request)

                if (response.isSuccessful) {
                    Log.i("ForumViewModel", "Post creado con éxito: ${response.body()?.message}")
                    refreshPosts()
                } else {
                    val errorBody = response.errorBody()?.string()
                    uiState = uiState.copy(error = "Error del servidor: $errorBody", isLoading = false)
                    Log.e("ForumViewModel", "Error al crear el post (servidor): $errorBody")
                }

            } catch (e: Exception) {
                uiState = uiState.copy(error = "No se pudo crear el post: ${e.message}", isLoading = false)
                Log.e("ForumViewModel", "Excepción al crear el post: ${e.message}")
            }
        }
    }
}
