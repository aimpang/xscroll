package com.xscroll.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import com.xscroll.ui.danmaku.DanmakuInput
import com.xscroll.ui.danmaku.DanmakuOverlay
import com.xscroll.ui.danmaku.DanmakuViewModel
import com.xscroll.ui.theme.LocalXScrollColors
import kotlinx.coroutines.delay

@Composable
fun FeedScreen(
    onRecordClick: () -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel(),
    danmakuViewModel: DanmakuViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val danmakuState by danmakuViewModel.state.collectAsState()
    val colors = MaterialTheme.colorScheme
    val accent = LocalXScrollColors.current

    val topPad = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val botPad = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val focusManager = LocalFocusManager.current

    if (state.videos.isEmpty() && state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = colors.onBackground)
        }
        return
    }

    if (state.videos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No videos yet",
                color = colors.onBackground.copy(alpha = 0.6f),
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
                    .padding(start = 21.dp, top = topPad + 17.dp),
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
            .background(colors.background),
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
                .padding(start = 21.dp, top = topPad + 17.dp),
        )

        // Message button (bottom-center)
        // Derive locked state directly from secondsOnVideo to avoid async flicker
        MessageButton(
            tokenCount = danmakuState.tokenCount,
            isLocked = secondsOnVideo >= 9,
            secondsOnVideo = secondsOnVideo,
            onClick = { danmakuViewModel.showInput() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = botPad + 20.dp),
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
                color = colors.onBackground.copy(alpha = 0.5f),
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
    val colors = MaterialTheme.colorScheme
    val accent = LocalXScrollColors.current

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accent.overlayLight)
            .clickable { onClick() }
            .semantics {
                contentDescription = "Record video"
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+",
            color = colors.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

