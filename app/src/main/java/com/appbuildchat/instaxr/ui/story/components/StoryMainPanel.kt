package com.appbuildchat.instaxr.ui.story.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.Orbiter
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
 * Main story panel - displays the current story image/video
 * This is the central panel closest to the user
 */
@Composable
fun StoryMainPanel(
    story: Story,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    hasNext: Boolean = false,
    hasPrevious: Boolean = false,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        StoryImage(
            imageUrl = story.mediaUrl,
            modifier = Modifier.fillMaxSize()
        )

        // Close button overlay (top right)
        FilledTonalIconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close story"
            )
        }
    }
}

/**
 * Spatial version of main story panel for XR mode
 */
@SuppressLint("RestrictedApi")
@Composable
fun StoryMainPanel(
    story: Story,
    onClose: () -> Unit,
    hasNext: Boolean = false,
    hasPrevious: Boolean = false,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    xOffset: androidx.compose.ui.unit.Dp = 0.dp
) {
    SpatialPanel(
        modifier = SubspaceModifier
            .width(900.dp)
            .height(1200.dp)
            .offset(x = xOffset, y = 100.dp, z = 50.dp), // Shifted up for clearance from bottom nav
        dragPolicy = MovePolicy(isEnabled = true),
        resizePolicy = ResizePolicy(isEnabled = true)
    ) {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                StoryImage(
                    imageUrl = story.mediaUrl,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Floating close button using Orbiter
        Orbiter(
            position = androidx.xr.compose.spatial.ContentEdge.Top,
            offset = 16.dp,
            alignment = Alignment.End
        ) {
            FilledTonalIconButton(
                onClick = onClose,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close story"
                )
            }
        }
    }
}

/**
 * Story image component with loading indicator
 */
@Composable
private fun StoryImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(
        imageUrl.substringBeforeLast("."),
        "drawable",
        context.packageName
    )

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (resourceId != 0) {
            // Loading indicator shown while image loads
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resourceId)
                    .size(1920, 1920)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Story image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback when image not found
            androidx.compose.material3.Text(
                text = "Story image not available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
