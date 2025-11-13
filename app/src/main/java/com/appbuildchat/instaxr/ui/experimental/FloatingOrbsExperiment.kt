package com.appbuildchat.instaxr.ui.experimental

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialBox
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
import coil3.size.Size
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
 * - Uses SpatialBox with move() and rotate() modifiers for 3D positioning
 */
@Composable
fun FloatingOrbsExperiment(
    modifier: Modifier = Modifier
) {
    // Create floating orbs for each user profile
    val profileImages = listOf(
        "profile_1.jpg",
        "profile_2.jpg",
        "profile_3.jpg",
        "profile_4.jpg",
        "profile_5.jpg",
        "profile_6.jpg",
        "profile_7.jpg"
    )

    // Wrap in Subspace to create spatial context
    Subspace {
        profileImages.forEachIndexed { index, profileImage ->
            FloatingOrb(
                profileImage = profileImage,
                index = index,
                totalCount = profileImages.size
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
    totalCount: Int
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

    // Create the floating orb in 3D space using SpatialBox
    SpatialBox(
        modifier = SubspaceModifier
            .offset(
                x = orbPosition.x,
                y = offsetY,
                z = orbPosition.z
            )
            .rotate(pitch = 0f, yaw = rotationY, roll = 0f)
    ) {
        // SpatialPanel bridges subspace and regular Compose
        SpatialPanel(
            modifier = SubspaceModifier
                .width(120.dp)
                .height(120.dp),
            dragPolicy = MovePolicy(isEnabled = false),
            resizePolicy = ResizePolicy(isEnabled = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape
            ) {
                // Circular profile image
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("file:///android_asset/mock_data/$profileImage")
                        .crossfade(true)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = "Floating profile orb",
                    modifier = Modifier
                        .fillMaxSize()
                        .border(4.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
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
