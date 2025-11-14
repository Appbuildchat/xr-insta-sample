package com.appbuildchat.instaxr.ui.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appbuildchat.instaxr.data.model.Story
import com.appbuildchat.instaxr.data.model.UserStoryGroup
import com.appbuildchat.instaxr.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Story feature
 * Manages story groups and individual story navigation
 */
@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StoryUiState>(StoryUiState.Loading)
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()

    init {
        loadStories()
    }

    fun handleAction(action: StoryAction) {
        when (action) {
            is StoryAction.Refresh -> loadStories()
            is StoryAction.SelectUserStories -> selectUserStories(action.userId)
            is StoryAction.SelectStory -> selectStory(action.storyId)
            is StoryAction.CloseStoryView -> closeStoryView()
            is StoryAction.NextStory -> navigateToNextStory()
            is StoryAction.PreviousStory -> navigateToPreviousStory()
            is StoryAction.MarkAsViewed -> markStoryAsViewed(action.storyId)
        }
    }

    private fun loadStories() {
        viewModelScope.launch {
            try {
                _uiState.update { StoryUiState.Loading }

                storyRepository.getUserStoryGroups().collect { groups ->
                    _uiState.update {
                        StoryUiState.Success(
                            userStoryGroups = groups,
                            selectedUserId = null,
                            selectedStoryIndex = 0,
                            userStories = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    StoryUiState.Error(
                        message = e.message ?: "Failed to load stories"
                    )
                }
            }
        }
    }

    private fun selectUserStories(userId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is StoryUiState.Success) {
                val userStories = storyRepository.getStoriesForUser(userId)
                _uiState.update {
                    currentState.copy(
                        selectedUserId = userId,
                        userStories = userStories,
                        selectedStoryIndex = -1 // No story selected by default, just show carousel
                    )
                }
            }
        }
    }

    private fun selectStory(storyId: String) {
        val currentState = _uiState.value
        if (currentState is StoryUiState.Success) {
            val storyIndex = currentState.userStories.indexOfFirst { it.id == storyId }
            if (storyIndex >= 0) {
                _uiState.update {
                    currentState.copy(selectedStoryIndex = storyIndex)
                }
            }
        }
    }

    private fun closeStoryView() {
        val currentState = _uiState.value
        if (currentState is StoryUiState.Success) {
            // Keep the carousel visible by maintaining selectedUserId and userStories
            // Just set index to -1 to indicate no story is selected for foreground view
            _uiState.update {
                currentState.copy(
                    selectedStoryIndex = -1
                )
            }
        }
    }

    private fun navigateToNextStory() {
        val currentState = _uiState.value
        if (currentState is StoryUiState.Success) {
            val nextIndex = (currentState.selectedStoryIndex + 1)
                .coerceAtMost(currentState.userStories.size - 1)

            _uiState.update {
                currentState.copy(selectedStoryIndex = nextIndex)
            }
        }
    }

    private fun navigateToPreviousStory() {
        val currentState = _uiState.value
        if (currentState is StoryUiState.Success) {
            val previousIndex = (currentState.selectedStoryIndex - 1).coerceAtLeast(0)

            _uiState.update {
                currentState.copy(selectedStoryIndex = previousIndex)
            }
        }
    }

    private fun markStoryAsViewed(storyId: String) {
        viewModelScope.launch {
            storyRepository.markStoryAsViewed(storyId)
        }
    }
}

/**
 * UI State for Story screen
 */
sealed interface StoryUiState {
    data object Loading : StoryUiState

    data class Success(
        val userStoryGroups: List<UserStoryGroup>,
        val selectedUserId: String?,
        val userStories: List<Story>,
        val selectedStoryIndex: Int
    ) : StoryUiState {
        val currentStory: Story?
            get() = userStories.getOrNull(selectedStoryIndex)

        val hasNext: Boolean
            get() = selectedStoryIndex < userStories.size - 1

        val hasPrevious: Boolean
            get() = selectedStoryIndex > 0

        val isStoryViewOpen: Boolean
            get() = selectedUserId != null && userStories.isNotEmpty()
    }

    data class Error(val message: String) : StoryUiState
}

/**
 * Actions that can be performed on the Story screen
 */
sealed interface StoryAction {
    data object Refresh : StoryAction
    data class SelectUserStories(val userId: String) : StoryAction
    data class SelectStory(val storyId: String) : StoryAction
    data object CloseStoryView : StoryAction
    data object NextStory : StoryAction
    data object PreviousStory : StoryAction
    data class MarkAsViewed(val storyId: String) : StoryAction
}
