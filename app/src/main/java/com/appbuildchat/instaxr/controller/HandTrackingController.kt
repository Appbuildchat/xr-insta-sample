/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appbuildchat.instaxr.controller

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.xr.runtime.Config
import androidx.xr.runtime.Session
import androidx.xr.runtime.SessionConfigureSuccess
import androidx.xr.runtime.math.Pose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Controller for managing hand tracking in XR
 * Enables hand tracking configuration
 *
 * Note: Full hand pose tracking API is not yet publicly available in Android XR SDK.
 * This controller prepares the session for hand tracking support.
 */
class HandTrackingController(
    private val xrSession: Session?,
    private val coroutineScope: CoroutineScope
) {
    var isHandTrackingEnabled by mutableStateOf(false)
        private set

    var rightHandIndexTipPose by mutableStateOf<Pose?>(null)
        private set

    var leftHandPalmPose by mutableStateOf<Pose?>(null)
        private set

    init {
        enableHandTracking()
    }

    @SuppressLint("RestrictedApi")
    private fun enableHandTracking() {
        coroutineScope.launch {
            xrSession?.let { session ->
                try {
                    val newConfig = session.config.copy(
                        handTracking = Config.HandTrackingMode.BOTH
                    )
                    when (val result = session.configure(newConfig)) {
                        is SessionConfigureSuccess -> {
                            isHandTrackingEnabled = true
                            Log.d(TAG, "Hand tracking enabled successfully")
                        }
                        else -> {
                            Log.e(TAG, "Failed to enable hand tracking: $result")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error enabling hand tracking", e)
                }
            } ?: Log.w(TAG, "Cannot enable hand tracking, session is null")
        }
    }

    /**
     * Update hand poses from the current perception state
     *
     * Note: This is a placeholder for when the hand tracking perception API
     * becomes publicly available. For now, it does nothing.
     */
    @SuppressLint("RestrictedApi")
    fun updateHandPoses() {
        // TODO: Implement when perception API is available
        // The perception API for accessing hand joints is not yet public
        // in the current Android XR SDK
    }

    companion object {
        const val TAG = "HandTrackingController"
    }
}
