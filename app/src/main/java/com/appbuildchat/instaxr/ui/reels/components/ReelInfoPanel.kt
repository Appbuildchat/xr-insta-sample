package com.appbuildchat.instaxr.ui.reels.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appbuildchat.instaxr.data.model.Reel
import kotlinx.coroutines.launch

@Composable
fun ReelInfoPanel(
    reel: Reel?,
    onLikeClick: () -> Unit,
    onShareClick: () -> Unit,
    onMoreClick: () -> Unit,
    onCommentLikeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newCommentText by remember { mutableStateOf("") }
    val commentsListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    // Local state for comments (without persisting to ViewModel)
    var localComments by remember(reel) { mutableStateOf(reel?.comments ?: emptyList()) }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        if (reel != null) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Reel Information Section
                ReelInfoSection(
                    reel = reel,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                // Comments Section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Comments (${localComments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )

                    LazyColumn(
                        state = commentsListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (localComments.isEmpty()) {
                            item {
                                Text(
                                    text = "No comments yet. Be the first to comment!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        } else {
                            items(localComments) { comment ->
                                CommentItem(
                                    comment = comment,
                                    onLikeClick = onCommentLikeClick
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Comment input field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Add a comment...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Send
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSend = {
                                if (newCommentText.isNotBlank()) {
                                    val newComment = com.appbuildchat.instaxr.data.model.Comment(
                                        id = "comment_${System.currentTimeMillis()}",
                                        postId = reel.id,
                                        userId = "current_user",
                                        username = "You",
                                        userProfileImageUrl = null,
                                        text = newCommentText,
                                        likeCount = 0,
                                        isLiked = false,
                                        timestamp = System.currentTimeMillis()
                                    )
                                    localComments = localComments + newComment
                                    newCommentText = ""
                                    keyboardController?.hide()

                                    // Scroll to bottom
                                    coroutineScope.launch {
                                        commentsListState.animateScrollToItem(localComments.size - 1)
                                    }
                                }
                            }
                        ),
                        maxLines = 3
                    )

                    // Send button
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                val newComment = com.appbuildchat.instaxr.data.model.Comment(
                                    id = "comment_${System.currentTimeMillis()}",
                                    postId = reel.id,
                                    userId = "current_user",
                                    username = "You",
                                    userProfileImageUrl = null,
                                    text = newCommentText,
                                    likeCount = 0,
                                    isLiked = false,
                                    timestamp = System.currentTimeMillis()
                                )
                                localComments = localComments + newComment
                                newCommentText = ""
                                keyboardController?.hide()

                                // Scroll to bottom
                                coroutineScope.launch {
                                    commentsListState.animateScrollToItem(localComments.size - 1)
                                }
                            }
                        },
                        enabled = newCommentText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send comment",
                            tint = if (newCommentText.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }

                HorizontalDivider()

                // Action Buttons Section
                ActionButtons(
                    isLiked = reel.isLiked,
                    onLikeClick = onLikeClick,
                    onShareClick = onShareClick,
                    onMoreClick = onMoreClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Loading state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Loading reel information...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
