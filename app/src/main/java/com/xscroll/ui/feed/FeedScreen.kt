package com.xscroll.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.xscroll.ui.danmaku.DanmakuInput
import com.xscroll.ui.danmaku.DanmakuOverlay
import com.xscroll.ui.danmaku.DanmakuViewModel
import kotlinx.coroutines.delay

@Composable
fun FeedScreen(
    onRecordClick: () -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel(),
    danmakuViewModel: DanmakuViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val danmakuState by danmakuViewModel.state.collectAsState()

    val topPad = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val botPad = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val focusManager = LocalFocusManager.current

    if (state.videos.isEmpty() && state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    if (state.videos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No videos yet",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 16.sp,
            )
        }
        // Still show record button on empty state
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            RecordFab(
                onClick = onRecordClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = topPad + 12.dp),
            )
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = state.currentIndex,
        pageCount = { state.videos.size },
    )

    // Per-second clock for 3→2→1 in the final 3s; loopIndex = seconds / 3 for danmaku sync
    var secondsOnVideo by remember { mutableIntStateOf(0) }
    LaunchedEffect(pagerState.settledPage) {
        secondsOnVideo = 0
        while (true) {
            delay(1000L)
            secondsOnVideo++
        }
    }
    val loopIndex = secondsOnVideo / 3

    // Use settledPage so nothing triggers mid-drag
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            viewModel.onPageChanged(page)
            val currentVideo = state.videos.getOrNull(page)
            if (currentVideo != null) danmakuViewModel.observeVideo(currentVideo.video.id)
            secondsOnVideo = 0
            danmakuViewModel.onLoopCountChanged(0)
        }
    }

    LaunchedEffect(loopIndex) {
        danmakuViewModel.onLoopCountChanged(loopIndex)
    }

    LaunchedEffect(state.currentIndex) {
        if (pagerState.settledPage != state.currentIndex) {
            pagerState.animateScrollToPage(state.currentIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Video pager
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
        ) { page ->
            val item = state.videos[page]
            val isActive = page == pagerState.settledPage

            Box(modifier = Modifier.fillMaxSize()) {
                if (item.downloadUri != null) {
                    VideoPlayer(
                        videoUri = item.downloadUri,
                        isActive = isActive,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        // Danmaku overlay — hidden during drag so comments don't clutter the swipe;
        // tap-to-dismiss is only wired up while the input is visible
        DanmakuOverlay(
            messages = danmakuState.messages,
            loopCount = loopIndex,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (pagerState.isScrollInProgress) 0f else 1f }
                .then(
                    if (danmakuState.isInputVisible) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures {
                                focusManager.clearFocus()
                                danmakuViewModel.hideInput()
                            }
                        }
                    } else {
                        Modifier
                    }
                ),
        )

        // Record FAB (top-left)
        RecordFab(
            onClick = onRecordClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = topPad + 12.dp),
        )

        // Message button (bottom-right)
        // Derive locked state directly from secondsOnVideo to avoid async flicker
        MessageButton(
            tokenCount = danmakuState.tokenCount,
            isLocked = secondsOnVideo >= 9,
            secondsOnVideo = secondsOnVideo,
            onClick = { danmakuViewModel.showInput() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = botPad + 20.dp),
        )

        // Danmaku text input
        DanmakuInput(
            visible = danmakuState.isInputVisible,
            tokenCount = danmakuState.tokenCount,
            isLocked = danmakuState.isMessagingLocked,
            onSend = { text -> danmakuViewModel.sendMessage(text, loopIndex) },
            onDismiss = { danmakuViewModel.hideInput() },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        // Loading indicator
        if (state.isLoading && state.videos.isNotEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .size(24.dp),
                color = Color.White.copy(alpha = 0.5f),
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
private fun RecordFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MessageButton(
    tokenCount: Int,
    isLocked: Boolean,
    secondsOnVideo: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Final 9s window is three 3s loops; show 3, 2, 1 in the last three seconds (6s–9s)
    val countdownSec = if (secondsOnVideo in 6..8) 9 - secondsOnVideo else null
    val showCountdown = countdownSec != null && !isLocked

    AnimatedVisibility(
        visible = !isLocked,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (showCountdown) Color(0xFFFF6B6B).copy(alpha = 0.25f)
                    else Color.White.copy(alpha = 0.12f)
                )
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (showCountdown) "⚡" else "💬",
                    fontSize = 14.sp,
                )
                Text(
                    text = if (countdownSec != null) " $countdownSec" else " drop a comment",
                    color = if (showCountdown) Color(0xFFFF6B6B) else Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = if (showCountdown) FontWeight.Bold else FontWeight.Normal,
                )
                Text(
                    text = "  $tokenCount✦",
                    color = Color(0xFFFFD700).copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
