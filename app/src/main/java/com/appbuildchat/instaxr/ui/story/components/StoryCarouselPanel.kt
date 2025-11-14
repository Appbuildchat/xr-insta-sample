package com.appbuildchat.instaxr.ui.story.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.width
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.appbuildchat.instaxr.data.model.Story

/**
 * Story carousel panel - displays stories horizontally in the background
 * Each story is shown in a smaller rectangular panel
 * When clicked, the story moves forward to become the main story
 */
@SuppressLint("RestrictedApi")
@Composable
fun StoryCarouselPanel(
    stories: List<Story>,
    selectedStoryId: String?,
    onStoryClick: (String) -> Unit,
    onCloseClick: () -> Unit = {}
) {
    SpatialPanel(
        modifier = SubspaceModifier
            .width(2800.dp)
            .height(800.dp)
            .offset(x = 0.dp, y = 0.dp, z = 0.dp), // Centered horizontally and vertically
        dragPolicy = MovePolicy(isEnabled = true),
        resizePolicy = ResizePolicy(isEnabled = false)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            items(stories, key = { it.id }) { story ->
                StoryCarouselItem(
                    story = story,
                    isSelected = story.id == selectedStoryId,
                    onClick = { onStoryClick(story.id) },
                    onCloseClick = onCloseClick
                )
            }
        }
    }
}

/**
 * Individual story item in the carousel
 */
@Composable
private fun StoryCarouselItem(
    story: Story,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(
        story.mediaUrl.substringBeforeLast("."),
        "drawable",
        context.packageName
    )

    var isHovered by remember { mutableStateOf(false) }

    // All stories same size in carousel
    val width = 450.dp
    val height = 700.dp
    val cornerRadius = 20.dp

    // Determine alpha based on hover state only
    val alpha = when {
        isHovered -> 1f  // Full color when hovering
        else -> 0.8f
    }

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick)
            .hoverable(
                interactionSource = remember { MutableInteractionSource() }
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> isHovered = true
                            PointerEventType.Exit -> isHovered = false
                        }
                    }
                }
            }
            .border(
                width = if (isSelected) 4.dp else 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(cornerRadius)
            )
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (resourceId != 0) {
            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp)
            )

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resourceId)
                    .size(1920, 1920)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Story thumbnail",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius)),
                contentScale = ContentScale.Crop
            )
        } else {
            androidx.compose.material3.Text(
                text = "Story ${story.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
