/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package yt.javi.gridirontimer.presentation

import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import yt.javi.gridirontimer.presentation.theme.GridironTimerTheme
import yt.javi.gridirontimer.presentation.viewmodel.TimerState
import yt.javi.gridirontimer.presentation.viewmodel.TimerViewModel
import yt.javi.gridirontimer.presentation.views.CustomTimerScreen
import yt.javi.gridirontimer.presentation.views.MainScreen
import yt.javi.gridirontimer.presentation.views.TimerScreen

class MainActivity : ComponentActivity() {
    private var wakeLock: PowerManager.WakeLock? = null
    var timerViewModel: TimerViewModel? = null
    var physicalButtonPressed by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GridironTimerTheme {
                WearApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        acquireWakeLock()
    }

    override fun onPause() {
        super.onPause()
        releaseWakeLock()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.d("MainActivity", "onKeyDown: $keyCode")
        if (keyCode == KeyEvent.KEYCODE_STEM_PRIMARY) {
            physicalButtonPressed = !physicalButtonPressed
        }
        return super.onKeyDown(keyCode, event)
    }

    @Composable
    fun ManageTimer() {
        timerViewModel?.let {
            if (physicalButtonPressed) {
                if (it.state.value is TimerState.Running) {
                    it.pauseTimer()
                } else if (it.state.value is TimerState.Paused) {
                    it.resumeTimer()
                }
                physicalButtonPressed = false
            }
            if (it.state.value is TimerState.Finished || it.state.value is TimerState.Idle) {
                releaseWakeLock()
            } else {
                acquireWakeLock()
            }
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock =
                    // Full wake lock available on API level 20 (Wear OS) and above.
                @Suppress("DEPRECATION")
                powerManager.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "WearCountdown:TimerWakeLock"
                )
        }
        wakeLock?.acquire(30*60*1000L /*30 minutes*/)
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
    }

    sealed class Screen(val route: String) {
        object Main : Screen("main")
        object Timer : Screen("timer/{duration}") {
            fun createRoute(duration: Long) = "timer/$duration"
        }

        object CustomTimer : Screen("custom_timer")
    }

    @Composable
    fun WearApp() {
        val navController = rememberSwipeDismissableNavController()
        timerViewModel = viewModel()
        ManageTimer()
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Screen.Main.route
        ) {
            composable(Screen.Main.route) { MainScreen(navController) }
            composable(
                route = Screen.Timer.route,
                arguments = listOf(navArgument("duration") { type = NavType.LongType })
            ) { navBackStackEntry ->
                val duration = navBackStackEntry.arguments?.getLong("duration") ?: 0L
                val initialDuration = navBackStackEntry.arguments?.getLong("duration") ?: 0L
                TimerScreen(duration, navController, initialDuration = initialDuration)
            }
            composable(Screen.CustomTimer.route) { CustomTimerScreen(navController) }
        }
    }
}
