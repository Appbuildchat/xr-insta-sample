package com.appbuildchat.instaxr.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appbuildchat.instaxr.data.local.MockDataLoader
import com.appbuildchat.instaxr.data.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the Home feature
 * Uses Hilt for dependency injection and activity-scoped sharing
 */
@HiltViewModel
class HomeViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _activeHearts = MutableStateFlow<List<HeartInstance>>(emptyList())
    val activeHearts: StateFlow<List<HeartInstance>> = _activeHearts.asStateFlow()

    init {
        loadHomeFeed()
    }

    fun handleAction(action: HomeAction) {
        when (action) {
            is HomeAction.Refresh -> loadHomeFeed()
            is HomeAction.LikePost -> likePost(action.postId)
            is HomeAction.SelectPost -> selectPost(action.postId, expandedForComments = false)
            is HomeAction.SelectPostForComments -> selectPost(action.postId, expandedForComments = true)
            is HomeAction.DeselectPost -> deselectPost()
        }
    }

    private fun loadHomeFeed() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                val posts = MockDataLoader.loadPosts(getApplication())
                _uiState.value = HomeUiState.Success(posts)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun likePost(postId: String) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    val wasLiked = post.isLiked
                    val newPost = post.copy(
                        isLiked = !post.isLiked,
                        likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
                    )

                    // Show heart animation when liking (not when unliking)
                    if (!wasLiked) {
                        showHeartAnimation()
                    }

                    newPost
                } else {
                    post
                }
            }
            val updatedSelectedPost = if (currentState.selectedPost?.id == postId) {
                updatedPosts.find { it.id == postId }
            } else {
                currentState.selectedPost
            }
            _uiState.value = HomeUiState.Success(updatedPosts, updatedSelectedPost, currentState.expandedForComments)
        }
    }

    private fun showHeartAnimation() {
        // Create a new heart instance with a unique ID
        // Position: centered (0dp x/y), in front of panel (positive Z = towards user)
        // Since it's now in the panel's Subspace, offsets are relative to the panel
        val newHeart = HeartInstance(
            id = UUID.randomUUID().toString(),
            offsetX = 0f,      // Centered horizontally relative to panel
            offsetY = 0f,      // Centered vertically relative to panel
            offsetZ = 300f     // In front of the panel (positive Z = towards user in XR space)
        )
        _activeHearts.value = _activeHearts.value + newHeart
    }

    fun removeHeart(heartId: String) {
        _activeHearts.value = _activeHearts.value.filter { it.id != heartId }
    }

    private fun selectPost(postId: String, expandedForComments: Boolean) {
        android.util.Log.d("HomeViewModel", "selectPost called with postId=$postId, expandedForComments=$expandedForComments")
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val selectedPost = currentState.posts.find { it.id == postId }
            android.util.Log.d("HomeViewModel", "Found post: $selectedPost")
            _uiState.value = currentState.copy(selectedPost = selectedPost, expandedForComments = expandedForComments)
            android.util.Log.d("HomeViewModel", "Updated state, selectedPost=${(_uiState.value as? HomeUiState.Success)?.selectedPost?.id}")
        }
    }

    private fun deselectPost() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(selectedPost = null, expandedForComments = false)
        }
    }
}

/**
 * UI State for Home screen
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val posts: List<Post>,
        val selectedPost: Post? = null,
        val expandedForComments: Boolean = false
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
