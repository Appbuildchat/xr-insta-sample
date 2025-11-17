package com.appbuildchat.instaxr.ui.shared

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.width
import com.appbuildchat.instaxr.data.model.UserStoryGroup

/**
 * Vertical story bar that displays user avatars
 * Shows stories from followed users with unviewed stories highlighted
 * This is a shared component used across Home, Profile, and Story screens
 */
@Composable
fun StoryBar(
    userStoryGroups: List<UserStoryGroup>,
    onStoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(userStoryGroups, key = { it.userId }) { storyGroup ->
                StoryAvatar(
                    userStoryGroup = storyGroup,
                    onClick = { onStoryClick(storyGroup.userId) }
                )
            }
        }
    }
}

/**
 * Spatial version of StoryBar for XR mode (Vertical)
 * Creates a floating vertical panel on the left side
 */
@SuppressLint("RestrictedApi")
@Composable
fun StoryBarSpatial(
    userStoryGroups: List<UserStoryGroup>,
    onStoryClick: (String) -> Unit,
    xOffset: androidx.compose.ui.unit.Dp = (-500).dp
) {
    SpatialPanel(
        modifier = SubspaceModifier
            .width(100.dp)
            .height(700.dp)
            .offset(x = xOffset, y = 0.dp, z = 0.dp), // Position to the left of main content
        dragPolicy = MovePolicy(isEnabled = true),
        resizePolicy = ResizePolicy(isEnabled = false)
    ) {
        StoryBar(
            userStoryGroups = userStoryGroups,
            onStoryClick = onStoryClick,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Horizontal story bar for Stories page
 * Shows user avatars in a horizontal row
 */
@Composable
fun StoryBarHorizontal(
    userStoryGroups: List<UserStoryGroup>,
    onStoryClick: (String) -> Unit,
    selectedUserId: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(userStoryGroups, key = { it.userId }) { storyGroup ->
                StoryAvatarHorizontal(
                    userStoryGroup = storyGroup,
                    isSelected = storyGroup.userId == selectedUserId,
                    onClick = { onStoryClick(storyGroup.userId) }
                )
            }
        }
    }
}

/**
 * Spatial version of horizontal StoryBar for XR mode
 * Creates a floating horizontal panel above the carousel
 */
@SuppressLint("RestrictedApi")
@Composable
fun StoryBarHorizontalSpatial(
    userStoryGroups: List<UserStoryGroup>,
    onStoryClick: (String) -> Unit,
    selectedUserId: String?,
    yOffset: androidx.compose.ui.unit.Dp = 450.dp
) {
    SpatialPanel(
        modifier = SubspaceModifier
            .width(1800.dp)
            .height(100.dp)
            .offset(x = 0.dp, y = yOffset, z = 0.dp), // Centered horizontally, positioned above carousel/story
        dragPolicy = MovePolicy(isEnabled = true),
        resizePolicy = ResizePolicy(isEnabled = false)
    ) {
        StoryBarHorizontal(
            userStoryGroups = userStoryGroups,
            onStoryClick = onStoryClick,
            selectedUserId = selectedUserId,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Individual story avatar item for horizontal layout
 */
@Composable
private fun StoryAvatarHorizontal(
    userStoryGroup: UserStoryGroup,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary // Highlight selected user
    } else if (userStoryGroup.hasUnviewedStories) {
        MaterialTheme.colorScheme.primary // Highlight unviewed stories
    } else {
        MaterialTheme.colorScheme.outline // Grey for viewed stories
    }

    Box(
        modifier = modifier
            .size(72.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring (border)
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Profile image or placeholder with inner container for proper sizing
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (userStoryGroup.userProfileImageUrl != null) {
                    val resourceId = context.resources.getIdentifier(
                        userStoryGroup.userProfileImageUrl.substringBeforeLast("."),
                        "drawable",
                        context.packageName
                    )

                    if (resourceId != 0) {
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = "Story by ${userStoryGroup.username}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userStoryGroup.username.first().uppercaseChar().toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    // Fallback placeholder with first letter
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userStoryGroup.username.first().uppercaseChar().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual story avatar item
 * Shows user profile image in a circle with a colored ring indicating viewed/unviewed status
 */
@Composable
private fun StoryAvatar(
    userStoryGroup: UserStoryGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val borderColor = if (userStoryGroup.hasUnviewedStories) {
        MaterialTheme.colorScheme.primary // Highlight unviewed stories
    } else {
        MaterialTheme.colorScheme.outline // Grey for viewed stories
    }

    Box(
        modifier = modifier
            .size(72.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring (border)
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Profile image or placeholder with inner container for proper sizing
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (userStoryGroup.userProfileImageUrl != null) {
                    val resourceId = context.resources.getIdentifier(
                        userStoryGroup.userProfileImageUrl.substringBeforeLast("."),
                        "drawable",
                        context.packageName
                    )

                    if (resourceId != 0) {
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = "Story by ${userStoryGroup.username}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userStoryGroup.username.first().uppercaseChar().toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    // Fallback placeholder with first letter
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userStoryGroup.username.first().uppercaseChar().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
