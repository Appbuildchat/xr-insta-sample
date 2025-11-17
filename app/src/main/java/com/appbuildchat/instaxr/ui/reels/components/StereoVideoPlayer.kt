package com.appbuildchat.instaxr.ui.reels.components

import android.content.ContentResolver
import android.net.Uri
import android.view.SurfaceView
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

/**
 * Composable for playing side-by-side stereo video using ExoPlayer
 * Renders the video surface directly in the Compose UI (in-panel)
 *
 * @param videoResId Raw resource ID of the video to play
 * @param autoPlay Whether to start playing automatically
 * @param modifier Modifier for the video player
 */
@Composable
fun StereoVideoPlayer(
    @RawRes videoResId: Int,
    autoPlay: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Create ExoPlayer - recreate when videoResId changes
    val exoPlayer = remember(videoResId) {
        // Create video URI from raw resource
        val videoUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.packageName)
            .path(videoResId.toString())
            .build()

        // Create ExoPlayer
        val player = ExoPlayer.Builder(context).build()
        val mediaItem = MediaItem.fromUri(videoUri)

        // Set media item and prepare player
        player.setMediaItem(mediaItem)
        player.repeatMode = ExoPlayer.REPEAT_MODE_ONE // Loop video
        player.prepare()

        if (autoPlay) {
            player.play()
        }

        player
    }

    // AndroidView to display video - recreate when videoResId changes
    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                exoPlayer.setVideoSurfaceView(this)
            }
        },
        update = { view ->
            // Ensure the surface is attached to the current player
            exoPlayer.setVideoSurfaceView(view)
        },
        modifier = modifier.fillMaxSize()
    )

    // Clean up when composable leaves composition or videoResId changes
    DisposableEffect(videoResId) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }
}
