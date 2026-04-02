package com.xscroll.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xscroll.ui.danmaku.DanmakuInput
import com.xscroll.ui.danmaku.DanmakuOverlay
import com.xscroll.ui.feed.MessageButton
import com.xscroll.ui.feed.VideoPlayer
import com.xscroll.ui.theme.LocalXScrollColors
import kotlinx.coroutines.delay

@Composable
fun DebugFeedScreen(
    onRecordClick: () -> Unit = {},
    feedViewModel: FakeFeedViewModel = viewModel(),
    danmakuViewModel: FakeDanmakuViewModel = viewModel(),
) {
    val state by feedViewModel.state.collectAsState()
    val danmakuState by danmakuViewModel.state.collectAsState()
    val colors = MaterialTheme.colorScheme
    val accent = LocalXScrollColors.current

    val topPad = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val botPad = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val focusManager = LocalFocusManager.current

    if (state.isLoading) {
        Box(Modifier.fillMaxSize().background(colors.background), Alignment.Center) {
            CircularProgressIndicator(color = colors.onBackground)
        }
        return
    }

    if (state.videos.isEmpty()) {
        Box(Modifier.fillMaxSize().background(colors.background), Alignment.Center) {
            Text("No fake videos found", color = colors.onBackground)
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = state.currentIndex,
        pageCount = { state.videos.size },
    )

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
            feedViewModel.onPageChanged(page)
            danmakuViewModel.observeVideo(state.videos.getOrNull(page)?.video?.id ?: "")
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

    LaunchedEffect(Unit) {
        state.videos.firstOrNull()?.let { danmakuViewModel.observeVideo(it.video.id) }
    }

    Box(Modifier.fillMaxSize().background(colors.background)) {

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
        ) { page ->
            val item = state.videos[page]
            val videoColor = FakeVideos.videoColors[item.video.id] ?: Color(0xFF111111)
            val isActive = page == pagerState.settledPage

            Box(modifier = Modifier.fillMaxSize()) {
                if (item.downloadUri != null) {
                    VideoPlayer(
                        videoUri = item.downloadUri,
                        isActive = isActive,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                // Color bar overlay at bottom — always visible regardless of video content
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(videoColor),
                )
                // Color dot + label
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(videoColor.copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Video ${page + 1}",
                        color = colors.onBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
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

        // Record FAB — top-left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = topPad + 12.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.overlayLight)
                .clickable { onRecordClick() }
                .semantics {
                    contentDescription = "Record video"
                    role = Role.Button
                },
            contentAlignment = Alignment.Center,
        ) {
            Text("+", color = colors.onBackground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // DEBUG badge — top-right
        Text(
            text = "DEBUG",
            color = Color.Yellow.copy(alpha = 0.6f),
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = topPad + 16.dp),
        )

        // Message button — bottom-right
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

        // Danmaku input
        DanmakuInput(
            visible = danmakuState.isInputVisible,
            tokenCount = danmakuState.tokenCount,
            isLocked = danmakuState.isMessagingLocked,
            onSend = { text -> danmakuViewModel.sendMessage(text, loopIndex) },
            onDismiss = { danmakuViewModel.hideInput() },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        // Video counter — bottom-center
        Text(
            text = "${state.currentIndex + 1} / ${state.videos.size}",
            color = colors.onBackground.copy(alpha = 0.35f),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = botPad + 8.dp),
        )
    }
}

