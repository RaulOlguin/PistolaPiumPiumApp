// En: ui/screens/ForumViewModel.kt
package cl.pistolapiumpium.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.pistolapiumpium.network.ForumPost
import cl.pistolapiumpium.network.RetrofitClient
import kotlinx.coroutines.launch


data class ForumUiState(
    val posts: List<ForumPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ForumViewModel : ViewModel() {

    var uiState by mutableStateOf(ForumUiState())
        private set

    // Definimos la URL completa y correcta que nos dio el deploy de Firebase.
    private val getPostsUrl = "https://getposts-op3ae5k63q-uc.a.run.app"

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                // Aquí está el cambio: llamamos a getPosts() pasándole la URL correcta.
                val posts = RetrofitClient.instance.getPosts(url = getPostsUrl)
                uiState = uiState.copy(posts = posts, isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message, isLoading = false)
            }
        }
    }
}
