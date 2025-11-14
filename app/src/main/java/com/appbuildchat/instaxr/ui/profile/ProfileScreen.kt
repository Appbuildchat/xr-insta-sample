package com.appbuildchat.instaxr.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import com.appbuildchat.instaxr.ui.icons.CommentBubble
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.width
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.appbuildchat.instaxr.data.model.Comment
import com.appbuildchat.instaxr.data.model.Post
import com.appbuildchat.instaxr.data.model.User
import kotlinx.coroutines.launch

/**
 * Top-level composable for the Profile feature screen
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileContent(
        uiState = uiState,
        onAction = viewModel::handleAction,
        modifier = modifier
    )
}

/**
 * Spatial content for XR mode
 */
@SuppressLint("RestrictedApi")
@Composable
fun ProfileSpatialContent(
    uiState: ProfileUiState,
    onAction: (ProfileAction) -> Unit
) {
    Subspace {
        when (uiState) {
            is ProfileUiState.Loading -> {
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(680.dp)
                        .height(800.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = true)
                ) {
                    Surface {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            is ProfileUiState.Success -> {
                if (uiState.selectedPost != null) {
                    // EXPANDED STATE: Three independent panels (grid + media + comments)
                    ProfileScreenSpatialPanelsAnimated(
                        uiState = uiState,
                        onAction = onAction
                    )
                } else {
                    // NORMAL STATE: Single panel with profile
                    SpatialPanel(
                        modifier = SubspaceModifier
                            .width(680.dp)
                            .height(900.dp),
                        dragPolicy = MovePolicy(isEnabled = true),
                        resizePolicy = ResizePolicy(isEnabled = true)
                    ) {
                        Surface {
                            ProfileMainContent(
                                user = uiState.user,
                                posts = uiState.posts,
                                selectedTab = uiState.selectedTab,
                                onAction = onAction,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                    }
                }
            }
            is ProfileUiState.Error -> {
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(680.dp)
                        .height(800.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = true)
                ) {
                    Surface {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Error: ${uiState.message}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Three spatial panels for expanded state (thumbnails + large image + comments)
 */
@SuppressLint("RestrictedApi")
@Composable
fun ProfileScreenSpatialPanelsAnimated(
    uiState: ProfileUiState,
    onAction: (ProfileAction) -> Unit
) {
    if (uiState is ProfileUiState.Success && uiState.selectedPost != null) {
        androidx.xr.compose.subspace.SpatialCurvedRow(
            curveRadius = 925.dp
        ) {
            // Left panel - Thumbnail grid
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(250.dp)
                    .height(900.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = true)
            ) {
                Surface {
                    ProfileMainContent(
                        user = uiState.user,
                        posts = uiState.posts,
                        selectedTab = uiState.selectedTab,
                        onAction = onAction,
                        isCompact = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Center panel - Large image
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(900.dp)
                    .height(900.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = true)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    PostImageView(
                        post = uiState.selectedPost,
                        modifier = Modifier.fillMaxSize()
                    )

                    PostActionButtons(
                        post = uiState.selectedPost,
                        onLikeClick = { onAction(ProfileAction.ToggleLike(uiState.selectedPost.id)) },
                        onMessageClick = { onAction(ProfileAction.SendMessage(uiState.selectedPost.id)) },
                        onRepostClick = { onAction(ProfileAction.Repost(uiState.selectedPost.id)) },
                        onArrowClick = { onAction(ProfileAction.ShowNextPost(uiState.selectedPost.id)) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                    )
                }

                // Close button orbiter
                Orbiter(
                    position = androidx.xr.compose.spatial.ContentEdge.Top,
                    offset = 16.dp,
                    alignment = Alignment.End
                ) {
                    FilledTonalIconButton(
                        onClick = { onAction(ProfileAction.DeselectPost) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

            }

            // Right panel - Profile info + comments
            SpatialPanel(
                modifier = SubspaceModifier
                    .width(400.dp)
                    .height(700.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = false)
            ) {
                Surface {
                    PostCommentsPanel(
                        post = uiState.selectedPost,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Internal composable for Profile screen content (2D mode)
 */
@Composable
internal fun ProfileContent(
    uiState: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator()
            }
            is ProfileUiState.Success -> {
                if (uiState.selectedPost != null) {
                    // Three panel layout
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left: Thumbnail grid
                        ProfileMainContent(
                            user = uiState.user,
                            posts = uiState.posts,
                            selectedTab = uiState.selectedTab,
                            onAction = onAction,
                            isCompact = true,
                            modifier = Modifier
                                .weight(0.2f)
                                .fillMaxHeight()
                        )

                        // Center: Large image
                        PostImageView(
                            post = uiState.selectedPost,
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight()
                        )

                        // Right: Profile info + comments
                        PostCommentsPanel(
                            post = uiState.selectedPost,
                            modifier = Modifier
                                .weight(0.3f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    // Normal profile view
                    ProfileMainContent(
                        user = uiState.user,
                        posts = uiState.posts,
                        selectedTab = uiState.selectedTab,
                        onAction = onAction,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            is ProfileUiState.Error -> {
                Text(
                    text = "Error: ${uiState.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Main profile content with header and post grid
 */
@Composable
private fun ProfileMainContent(
    user: User,
    posts: List<Post>,
    selectedTab: ProfileTab,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            ProfileHeader(
                user = user,
                isCompact = isCompact
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Tab navigation
            if (!isCompact) {
                TabNavigation(
                    selectedTab = selectedTab,
                    onTabChange = { tab -> onAction(ProfileAction.ChangeTab(tab)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            // Post grid
            PostGrid(
                posts = posts,
                onPostClick = { post -> onAction(ProfileAction.SelectPost(post)) },
                columns = if (isCompact) 3 else 4
            )
        }
    }
}

/**
 * Profile header with avatar and bio (Instagram style)
 */
@Composable
private fun ProfileHeader(
    user: User,
    isCompact: Boolean = false
) {
    if (isCompact) {
        // Compact layout - just name
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    } else {
        // Full layout - Row with avatar + info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile picture
                ProfileAvatar(
                    profileImageUrl = user.profileImageUrl,
                    username = user.username,
                    size = 80.dp
                )

                // Username and bio
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (!user.bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Profile avatar with circular shape
 */
@Composable
private fun ProfileAvatar(
    profileImageUrl: String?,
    username: String,
    size: Dp
) {
    val context = LocalContext.current

    if (profileImageUrl != null) {
        val resourceId = context.resources.getIdentifier(
            profileImageUrl.substringBeforeLast("."),
            "drawable",
            context.packageName
        )

        if (resourceId != 0) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            PlaceholderAvatar(username = username, size = size)
        }
    } else {
        PlaceholderAvatar(username = username, size = size)
    }
}

/**
 * Placeholder avatar with initial letter
 */
@Composable
private fun PlaceholderAvatar(username: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = username.first().uppercaseChar().toString(),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Tab navigation for Posts, Reels, Tagged
 */
@Composable
private fun TabNavigation(
    selectedTab: ProfileTab,
    onTabChange: (ProfileTab) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Tab(
            selected = selectedTab == ProfileTab.POSTS,
            onClick = { onTabChange(ProfileTab.POSTS) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            text = { Text("Posts") }
        )
        Tab(
            selected = selectedTab == ProfileTab.REELS,
            onClick = { onTabChange(ProfileTab.REELS) },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
            text = { Text("Reels") }
        )
        Tab(
            selected = selectedTab == ProfileTab.TAGGED,
            onClick = { onTabChange(ProfileTab.TAGGED) },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            text = { Text("Tagged") }
        )
    }
}

/**
 * Tab navigation as orbiter (for XR mode)
 */
@Composable
private fun TabNavigationOrbiter(
    selectedTab: ProfileTab,
    onTabChange: (ProfileTab) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { onTabChange(ProfileTab.POSTS) },
                colors = if (selectedTab == ProfileTab.POSTS) {
                    IconButtonDefaults.filledIconButtonColors()
                } else {
                    IconButtonDefaults.iconButtonColors()
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Posts")
            }
            IconButton(
                onClick = { onTabChange(ProfileTab.REELS) },
                colors = if (selectedTab == ProfileTab.REELS) {
                    IconButtonDefaults.filledIconButtonColors()
                } else {
                    IconButtonDefaults.iconButtonColors()
                }
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Reels")
            }
            IconButton(
                onClick = { onTabChange(ProfileTab.TAGGED) },
                colors = if (selectedTab == ProfileTab.TAGGED) {
                    IconButtonDefaults.filledIconButtonColors()
                } else {
                    IconButtonDefaults.iconButtonColors()
                }
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Tagged")
            }
        }
    }
}

/**
 * Post grid (Instagram-style grid)
 */
@Composable
private fun PostGrid(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    columns: Int = 4
) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No posts yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column {
            // Calculate rows needed
            val rows = (posts.size + columns - 1) / columns

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until columns) {
                        val index = row * columns + col
                        if (index < posts.size) {
                            PostThumbnail(
                                post = posts[index],
                                onClick = { onPostClick(posts[index]) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Post thumbnail for grid with rounded corners
 */
@Composable
private fun PostThumbnail(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(
        post.imageUrl.substringBeforeLast("."),
        "drawable",
        context.packageName
    )

    if (resourceId != 0) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(resourceId)
                .size(400)
                .crossfade(true)
                .build(),
            contentDescription = "Post thumbnail",
            modifier = modifier
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onClick),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Post image view with carousel for multiple images
 */
@Composable
private fun PostImageView(
    post: Post,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { post.imageUrls.size })

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (post.imageUrls.size > 1) {
            // Multiple images - show pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                userScrollEnabled = true
            ) { page ->
                val imageUrl = post.imageUrls[page]
                val resourceId = context.resources.getIdentifier(
                    imageUrl.substringBeforeLast("."),
                    "drawable",
                    context.packageName
                )

                if (resourceId != 0) {
                    coil3.compose.SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(resourceId)
                            .size(1920, 1920)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Post image ${page + 1} of ${post.imageUrls.size}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.FillWidth,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    )
                }
            }

            // Page indicator with current page info
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(post.imageUrls.size) { iteration ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == iteration)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Page counter
                Text(
                    text = "${pagerState.currentPage + 1}/${post.imageUrls.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        } else {
            // Single image
            val resourceId = context.resources.getIdentifier(
                post.imageUrl.substringBeforeLast("."),
                "drawable",
                context.packageName
            )

            if (resourceId != 0) {
                coil3.compose.SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(resourceId)
                        .size(1920, 1920)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Large post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                )
            }
        }
    }
}

/**
 * Post comments panel with profile info and comments
 */
@Composable
private fun PostCommentsPanel(
    post: Post,
    modifier: Modifier = Modifier
) {
    var newCommentText by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val commentsListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    // Local state for comments (without persisting to ViewModel)
    var localComments by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(post.comments) }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        LazyColumn(
            state = commentsListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Profile header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatar(
                        profileImageUrl = post.userProfileImageUrl,
                        username = post.username,
                        size = 48.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Caption
            if (!post.caption.isNullOrBlank()) {
                item {
                    Text(
                        text = post.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Comments section header
            item {
                Text(
                    text = "Comments (${localComments.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Comments list
            items(localComments) { comment ->
                CommentItem(comment = comment)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        HorizontalDivider()

        // Comment input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(24.dp)
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
                            val newComment = Comment(
                                id = "comment_${System.currentTimeMillis()}",
                                postId = post.id,
                                userId = "current_user",
                                username = "You",
                                userProfileImageUrl = null,
                                text = newCommentText,
                                likeCount = 0,
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
                        val newComment = Comment(
                            id = "comment_${System.currentTimeMillis()}",
                            postId = post.id,
                            userId = "current_user",
                            username = "You",
                            userProfileImageUrl = null,
                            text = newCommentText,
                            likeCount = 0,
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Individual comment item
 */
@Composable
private fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProfileAvatar(
            profileImageUrl = comment.userProfileImageUrl,
            username = comment.username,
            size = 32.dp
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = comment.username,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Format count for display
 */
private fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 10000 -> String.format("%.1fK", count / 1000.0)
        else -> String.format("%.0fK", count / 1000.0)
    }
}

/**
 * Post action buttons for XR mode (Instagram style)
 */
@Composable
private fun PostActionButtons(
    post: Post,
    onLikeClick: () -> Unit,
    onMessageClick: () -> Unit,
    onRepostClick: () -> Unit,
    onArrowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Filled.Favorite
                                     else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) MaterialTheme.colorScheme.error
                              else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = formatCount(post.likeCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Message button
            IconButton(
                onClick = onMessageClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Repost button
            IconButton(
                onClick = onRepostClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Repost",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Arrow button
            IconButton(
                onClick = onArrowClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
