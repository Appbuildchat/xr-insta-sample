package com.appbuildchat.instaxr.data.repository

import android.content.Context
import com.appbuildchat.instaxr.data.model.MediaType
import com.appbuildchat.instaxr.data.model.Story
import com.appbuildchat.instaxr.data.model.UserStoryGroup
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Repository interface for Story data
 */
interface StoryRepository {
    fun getStories(): Flow<List<Story>>
    fun getUserStoryGroups(): Flow<List<UserStoryGroup>>
    suspend fun getStoryById(storyId: String): Story?
    suspend fun getStoriesForUser(userId: String): List<Story>
    suspend fun markStoryAsViewed(storyId: String): Result<Unit>
    suspend fun deleteStory(storyId: String): Result<Unit>
}

/**
 * Default implementation of StoryRepository with Firestore
 */
class DefaultStoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : StoryRepository {

    // Mock story data for development
    private val mockStories = createMockStories()

    override fun getStories(): Flow<List<Story>> {
        // TODO: Implement actual data fetching from local/remote sources
        return flowOf(mockStories)
    }

    override fun getUserStoryGroups(): Flow<List<UserStoryGroup>> {
        // Group stories by userId and sort by latest timestamp
        val groups = mockStories
            .groupBy { it.userId }
            .map { (userId, stories) ->
                val sortedStories = stories.sortedByDescending { it.timestamp }
                UserStoryGroup(
                    userId = userId,
                    username = stories.first().username,
                    userProfileImageUrl = stories.first().userProfileImageUrl,
                    stories = sortedStories,
                    hasUnviewedStories = sortedStories.any { !it.isViewed },
                    latestTimestamp = sortedStories.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis()
                )
            }
            .sortedByDescending { it.latestTimestamp } // Most recent first

        return flowOf(groups)
    }

    override suspend fun getStoryById(storyId: String): Story? {
        return mockStories.find { it.id == storyId }
    }

    override suspend fun getStoriesForUser(userId: String): List<Story> {
        return mockStories.filter { it.userId == userId }.sortedByDescending { it.timestamp }
    }

    override suspend fun markStoryAsViewed(storyId: String): Result<Unit> {
        // TODO: Implement mark as viewed logic
        return Result.success(Unit)
    }

    override suspend fun deleteStory(storyId: String): Result<Unit> {
        // TODO: Implement delete story logic
        return Result.success(Unit)
    }

    private fun createMockStories(): List<Story> {
        val currentTime = System.currentTimeMillis()

        return listOf(
            // User 1: johndoe - 3 stories
            Story(
                id = "story_1",
                userId = "user_1",
                username = "johndoe",
                userProfileImageUrl = "profile_1.jpg",
                mediaUrl = "post_1.jpg",
                mediaType = MediaType.IMAGE,
                isViewed = false,
                timestamp = currentTime - 1000 * 60 * 30 // 30 minutes ago
            ),
            Story(
                id = "story_2",
                userId = "user_1",
                username = "johndoe",
                userProfileImageUrl = "profile_1.jpg",
                mediaUrl = "post_2.jpg",
                mediaType = MediaType.IMAGE,
                isViewed = false,
                timestamp = currentTime - 1000 * 60 * 60 // 1 hour ago
            ),
            Story(
                id = "story_3",
                userId = "user_1",
                username = "johndoe",
                userProfileImageUrl = "profile_1.jpg",
                mediaUrl = "post_3.jpg",
                mediaType = MediaType.IMAGE,
                isViewed = false,
                timestamp = currentTime - 1000 * 60 * 90 // 1.5 hours ago
            ),
            // User 2: janedoe - 2 stories
            Story(
                id = "story_4",
                userId = "user_2",
                username = "janedoe",
                userProfileImageUrl = "profile_2.jpg",
                mediaUrl = "post_4.jpg",
                mediaType = MediaType.IMAGE,
                isViewed = false,
                timestamp = currentTime - 1000 * 60 * 15 // 15 minutes ago
            ),
            Story(
                id = "story_5",
                userId = "user_2",
                username = "janedoe",
                userProfileImageUrl = "profile_2.jpg",
                mediaUrl = "post_5.jpg",
                mediaType = MediaType.IMAGE,
                isViewed = false,
                timestamp = currentTime - 1000 * 60 * 45 // 45 minutes ago
            ),
            // User 3: mike_photo - 4 stories
            Story(
                id = "story_6",
                userId = "user_3",
                username = "mike_photo",
                userProfileImageUrl = "profile_3.jpg",
                mediaUrl = "post_6.jpg",
                mediaType = MediaType.IMAGE,
                isViewed = true, // Viewed story
                timestamp = currentTime - 1000 * 60 * 120 // 2 hours ago
            ),
            Story(
                id = "story_7",
                userId = "user_3",
                username = "mike_photo",
                userProfileImageUrl = "profile_3.jpg",
                mediaUrl = "post_7.jpg",
                mediaType = MediaType.IMAGE,
                isViewed = true,
                timestamp = currentTime - 1000 * 60 * 150 // 2.5 hours ago
            ),
            Story(
                id = "story_8",
                userId = "user_3",
                username = "mike_photo",
                userProfileImageUrl = "profile_3.jpg",
                mediaUrl = "post_8.jpg",
                mediaType = MediaType.IMAGE,
                isViewed = false,
                timestamp = currentTime - 1000 * 60 * 180 // 3 hours ago
            ),
            Story(
                id = "story_9",
                userId = "user_3",
                username = "mike_photo",
                userProfileImageUrl = "profile_3.jpg",
                mediaUrl = "post_9.jpg",
                mediaType = MediaType.IMAGE,
                isViewed = false,
                timestamp = currentTime - 1000 * 60 * 200 // 3.3 hours ago
            ),
            // User 4: sarah_wilson - 1 story
            Story(
                id = "story_10",
                userId = "user_4",
                username = "sarah_wilson",
                userProfileImageUrl = "profile_4.jpg",
                mediaUrl = "post_10.jpg",
                mediaType = MediaType.IMAGE,
                isViewed = false,
                timestamp = currentTime - 1000 * 60 * 5 // 5 minutes ago (most recent)
            )
        )
    }
}
