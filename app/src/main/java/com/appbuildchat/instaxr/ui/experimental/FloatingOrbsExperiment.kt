package com.appbuildchat.instaxr.ui.experimental

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.rotate
import androidx.xr.compose.subspace.layout.width
import androidx.xr.compose.subspace.layout.height
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Experimental screen showing floating profile picture orbs around the user
 *
 * Features:
 * - Orbs positioned randomly in a 180° arc in front of user
 * - Distance: 1-2 meters from user
 * - Each orb continuously floats up and down
 * - Tap orb to show story panel with user's post
 */
@Composable
fun FloatingOrbsExperiment(
    modifier: Modifier = Modifier
) {
    // State for selected orb (to show story panel)
    var selectedOrbIndex by remember { mutableStateOf<Int?>(null) }

    // Create floating orbs for each user profile
    val profileImages = listOf(
        "profile_1.jpg",
        "profile_2.jpg",
        "profile_3.jpg",
        "profile_4.jpg",
        "profile_6.jpg",
        "profile_7.jpg"
    )

    // Map profile to post images
    val postImages = listOf(
        "post_1.jpg",
        "post_2.jpg",
        "post_3.jpg",
        "post_4.jpg",
        "post_6.jpg",
        "post_7.jpg"
    )

    // Wrap in Subspace to create spatial context
    Subspace {
        // Show floating orbs
        profileImages.forEachIndexed { index, profileImage ->
            FloatingOrb(
                profileImage = profileImage,
                index = index,
                totalCount = profileImages.size,
                onClick = { selectedOrbIndex = index }
            )
        }

        // Show story panel if an orb is selected
        selectedOrbIndex?.let { index ->
            StoryPanel(
                postImage = postImages[index],
                profileImage = profileImages[index],
                onClose = { selectedOrbIndex = null }
            )
        }
    }
}

/**
 * Individual floating orb with random 3D positioning and animation
 */
@Composable
private fun FloatingOrb(
    profileImage: String,
    index: Int,
    totalCount: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // Generate random position (persistent across recomposition)
    val orbPosition = remember(index) {
        generateRandomOrbPosition(index, totalCount)
    }

    // Infinite animation for up/down floating
    val infiniteTransition = rememberInfiniteTransition(label = "orb_$index")

    // Animate Y position (up and down) in Dp
    val offsetY by infiniteTransition.animateValue(
        initialValue = orbPosition.baseY - 50.dp,
        targetValue = orbPosition.baseY + 50.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = orbPosition.animationDuration,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY_$index"
    )

    // Optional: Gentle rotation animation
    val rotationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = orbPosition.rotationDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationY_$index"
    )

    // Create each orb as a separate draggable SpatialPanel
    SpatialPanel(
        modifier = SubspaceModifier
            .width(120.dp)
            .height(120.dp)
            .offset(
                x = orbPosition.x,
                y = offsetY,
                z = orbPosition.z
            )
            .rotate(pitch = 0f, yaw = rotationY, roll = 0f),
        dragPolicy = MovePolicy(isEnabled = true),  // Enable dragging!
        resizePolicy = ResizePolicy(isEnabled = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            shape = CircleShape
        ) {
            // Get drawable resource ID from the profileImage filename
            val drawableResId = remember(profileImage) {
                context.resources.getIdentifier(
                    profileImage.removeSuffix(".jpg"),
                    "drawable",
                    context.packageName
                )
            }

            // Load image from drawable resources
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(drawableResId)
                    .crossfade(true)
                    .build(),
                contentDescription = "Floating profile orb $profileImage",
                modifier = Modifier
                    .fillMaxSize()
                    .border(4.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * Data class to hold orb positioning information
 */
private data class OrbPosition(
    val x: Dp,
    val y: Dp,
    val z: Dp,
    val baseY: Dp,
    val animationDuration: Int,
    val rotationDuration: Int
)

/**
 * Generate random position for an orb
 * - Distance: 1-2 meters from user (converted to Dp)
 * - Angle: 180° arc in front of user (-90° to +90°)
 * - Height: Random Y position
 */
private fun generateRandomOrbPosition(index: Int, totalCount: Int): OrbPosition {
    val random = Random(index) // Seed with index for consistent randomness

    // Random distance between 1.0m and 2.0m (1 meter ≈ 1000dp in XR space)
    val distance = random.nextFloat() * 1000f + 1000f // 1000-2000 dp

    // Angle in 180° arc (spread evenly with some randomness)
    val baseAngle = (index.toFloat() / totalCount) * 180f - 90f
    val angleVariation = random.nextFloat() * 30f - 15f // ±15° variation
    val angleDegrees = baseAngle + angleVariation
    val angleRadians = Math.toRadians(angleDegrees.toDouble())

    // Calculate X and Z position based on angle and distance
    val x = (sin(angleRadians) * distance).toFloat().dp
    val z = -(cos(angleRadians) * distance).toFloat().dp // Negative Z is forward

    // Random Y position (height) between -300dp and 500dp
    val baseY = (random.nextFloat() * 800f - 300f).dp

    // Random animation duration (2-5 seconds)
    val animationDuration = random.nextInt(2000, 5000)

    // Random rotation duration (8-15 seconds)
    val rotationDuration = random.nextInt(8000, 15000)

    return OrbPosition(
        x = x,
        y = baseY,
        z = z,
        baseY = baseY,
        animationDuration = animationDuration,
        rotationDuration = rotationDuration
    )
}

/**
 * Story panel that appears when an orb is tapped
 * Shows the user's post image in a centered panel
 */
@Composable
private fun StoryPanel(
    postImage: String,
    profileImage: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    // Create centered BIG panel in front of user
    SpatialPanel(
        modifier = SubspaceModifier
            .width(600.dp)
            .height(1000.dp)
            .offset(x = 0.dp, y = 0.dp, z = -1500.dp), // Center, 1.5m in front
        dragPolicy = MovePolicy(isEnabled = true),
        resizePolicy = ResizePolicy(isEnabled = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with profile and close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile image - BIGGER
                    val profileResId = remember(profileImage) {
                        context.resources.getIdentifier(
                            profileImage.removeSuffix(".jpg"),
                            "drawable",
                            context.packageName
                        )
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(profileResId)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Story",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )

                    // Close button - BIGGER
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Post image (story content)
                val postResId = remember(postImage) {
                    context.resources.getIdentifier(
                        postImage.removeSuffix(".jpg"),
                        "drawable",
                        context.packageName
                    )
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(postResId)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Story post",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
