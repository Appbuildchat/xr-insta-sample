package com.appbuildchat.instaxr.ui.story

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width
import com.appbuildchat.instaxr.ui.shared.StoryBar
import com.appbuildchat.instaxr.ui.shared.StoryBarHorizontalSpatial
import com.appbuildchat.instaxr.ui.shared.StoryBarSpatial
import com.appbuildchat.instaxr.ui.story.components.StoryCarouselPanel
import com.appbuildchat.instaxr.ui.story.components.StoryDetailPanel
import com.appbuildchat.instaxr.ui.story.components.StoryMainPanel

/**
 * Top-level composable for the Story feature screen
 */
@Composable
fun StoryScreen(
    modifier: Modifier = Modifier,
    viewModel: StoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StoryContent(
        uiState = uiState,
        onAction = viewModel::handleAction,
        modifier = modifier
    )
}

/**
 * Internal composable for Story screen content (2D mode)
 */
@Composable
internal fun StoryContent(
    uiState: StoryUiState,
    onAction: (StoryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (uiState) {
            is StoryUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is StoryUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${uiState.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            is StoryUiState.Success -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left side: Story bar
                    StoryBar(
                        userStoryGroups = uiState.userStoryGroups,
                        onStoryClick = { userId ->
                            onAction(StoryAction.SelectUserStories(userId))
                        },
                        modifier = Modifier.width(100.dp)
                    )

                    // Right side: Story view or placeholder
                    val story = uiState.currentStory
                    if (uiState.isStoryViewOpen && story != null) {
                        StoryMainPanel(
                            story = story,
                            onClose = { onAction(StoryAction.CloseStoryView) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Select a story to view",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Spatial content for XR mode with multiple floating panels:
 * 1. Left panel: Vertical story bar (shared across screens)
 * 2. Center panel: Main story display (closest to user)
 * 3. Background panels: Horizontal carousel of other stories from same user
 * 4. Right panel: Story details (username, timestamp, reactions)
 *
 * IMPORTANT: Wraps everything in Subspace to create spatial context
 */
@SuppressLint("RestrictedApi")
@Composable
fun StorySpatialContent(
    uiState: StoryUiState,
    onAction: (StoryAction) -> Unit
) {
    Subspace {
        when (uiState) {
            is StoryUiState.Loading -> {
                // Show loading in a simple spatial panel
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(800.dp)
                        .height(900.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = true)
                ) {
                    Surface {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            is StoryUiState.Error -> {
                // Show error in a simple spatial panel
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(800.dp)
                        .height(900.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = true)
                ) {
                    Surface {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Error: ${uiState.message}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            is StoryUiState.Success -> {
                val story = uiState.currentStory

                // Dynamic positioning: story bar moves up when viewing individual story (taller panel)
                val storyBarYOffset = if (uiState.isStoryViewOpen && story != null) {
                    850.dp // Position above the main story panel with proper clearance (panels shifted up 100dp)
                } else {
                    450.dp // Position above the carousel (800dp height)
                }

                // Show horizontal story bar above the carousel/story
                StoryBarHorizontalSpatial(
                    userStoryGroups = uiState.userStoryGroups,
                    onStoryClick = { userId ->
                        onAction(StoryAction.SelectUserStories(userId))
                    },
                    selectedUserId = uiState.selectedUserId,
                    yOffset = storyBarYOffset
                )

                if (uiState.isStoryViewOpen) {
                    if (story != null) {
                        // Story is selected: Show foreground panels (main + detail), hide carousel
                        SpatialRow {
                            // Main panel: Current story display (no offset, natural position)
                            StoryMainPanel(
                                story = story,
                                onClose = { onAction(StoryAction.CloseStoryView) },
                                hasNext = uiState.hasNext,
                                hasPrevious = uiState.hasPrevious,
                                onNext = { onAction(StoryAction.NextStory) },
                                onPrevious = { onAction(StoryAction.PreviousStory) },
                                xOffset = 0.dp // No offset, natural position
                            )

                            // Right panel: Story details (info, reactions, comments)
                            StoryDetailPanel(
                                story = story
                            )
                        }
                    } else {
                        // No story selected: Show only the carousel
                        StoryCarouselPanel(
                            stories = uiState.userStories,
                            selectedStoryId = null,
                            onStoryClick = { storyId ->
                                onAction(StoryAction.SelectStory(storyId))
                            },
                            onCloseClick = {
                                onAction(StoryAction.CloseStoryView)
                            }
                        )
                    }
                } else {
                    // Placeholder when no user stories are loaded
                    SpatialPanel(
                        modifier = SubspaceModifier
                            .width(800.dp)
                            .height(900.dp),
                        dragPolicy = MovePolicy(isEnabled = true),
                        resizePolicy = ResizePolicy(isEnabled = true)
                    ) {
                        Surface {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Select a story from the story bar",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
