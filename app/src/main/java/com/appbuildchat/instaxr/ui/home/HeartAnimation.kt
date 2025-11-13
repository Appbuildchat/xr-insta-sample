package com.appbuildchat.instaxr.ui.home

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SceneCoreEntity
import androidx.xr.compose.subspace.SceneCoreEntitySizeAdapter
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.scale
import androidx.xr.compose.unit.Meter
import androidx.xr.runtime.math.Quaternion
import androidx.xr.scenecore.GltfModelEntity
import com.appbuildchat.instaxr.controller.HeartController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Data class representing an animated heart instance
 */
data class HeartInstance(
    val id: String,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val offsetZ: Float = -500f
)

// Heart glb height in meters (adjust based on actual model)
private const val heartHeight = 0.5f

// The desired amount of the available layout height to use for the heart
private const val fillRatio = 0.8f

const val TAG = "HeartModel"

/**
 * Composable that renders a 3D heart model with scale animation
 * The heart appears in front of the user in XR space and auto-fades after animation
 */
@SuppressLint("RestrictedApi")
@Composable
fun HeartModel(
    showHeart: Boolean,
    modifier: SubspaceModifier = SubspaceModifier,
) {
    val xrSession = LocalSession.current
    if (xrSession != null && showHeart) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val heartController = remember(xrSession, context, coroutineScope) {
            HeartController(xrSession, context, coroutineScope)
        }
        val gltfModel = heartController.gltfModel

        gltfModel?.let { model ->
            Subspace {
                val density = LocalDensity.current
                var scaleFromLayout by remember { mutableFloatStateOf(1f) }

                SceneCoreEntity(
                    factory = {
                        GltfModelEntity.create(xrSession, model)
                    },
                    update = { entity: GltfModelEntity ->
                        // Optional: Start animation if available
                        try {
                            entity.startAnimation(loop = false)
                        } catch (e: Exception) {
                            // No animation available, that's okay
                        }
                    },
                    sizeAdapter = SceneCoreEntitySizeAdapter(onLayoutSizeChanged = { size ->
                        val scaleToFillLayoutHeight = Meter
                            .fromPixel(size.height.toFloat(), density).toM() / heartHeight
                        scaleFromLayout = scaleToFillLayoutHeight * fillRatio
                    }),
                    modifier = modifier.scale(scaleFromLayout)
                )
            }
        }
    }
}

