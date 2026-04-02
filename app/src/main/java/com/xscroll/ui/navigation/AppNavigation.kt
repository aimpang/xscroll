package com.xscroll.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xscroll.debug.DebugFeedScreen
import com.xscroll.ui.SplashScreen
import com.xscroll.ui.feed.FeedScreen
import com.xscroll.ui.record.RecordScreen

// Flip to false to use real Firebase data
private const val USE_FAKE_DATA = true

object Routes {
    const val SPLASH = "splash"
    const val FEED = "feed"
    const val RECORD = "record"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.FEED) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.FEED) {
            Log.d("AppNav", "Feed composable entered, USE_FAKE_DATA=$USE_FAKE_DATA")
            if (USE_FAKE_DATA) {
                DebugFeedScreen(
                    onRecordClick = { navController.navigate(Routes.RECORD) },
                )
            } else {
                FeedScreen(
                    onRecordClick = { navController.navigate(Routes.RECORD) },
                )
            }
        }
        composable(Routes.RECORD) {
            RecordScreen(
                onDone = { navController.popBackStack() },
                onClose = { navController.popBackStack() },
            )
        }
    }
}
