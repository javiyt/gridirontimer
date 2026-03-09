/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package yt.javi.gridirontimer.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import yt.javi.gridirontimer.presentation.theme.GridironTimerTheme
import yt.javi.gridirontimer.presentation.viewmodel.AppTimerSettings
import yt.javi.gridirontimer.presentation.views.CustomTimerScreen
import yt.javi.gridirontimer.presentation.views.MainScreen
import yt.javi.gridirontimer.presentation.views.TimerScreen

class MainActivity : ComponentActivity() {
    private var onStemPrimaryPressed: (() -> Unit)? = null
    private var onStemPrimaryDoublePressed: (() -> Unit)? = null
    private var onStemPrimaryTriplePressed: (() -> Unit)? = null
    private var onStemPrimaryLongPressed: (() -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastStemPrimaryPressAt = 0L
    private var stemPressCount = 0
    private var pendingSinglePress: Runnable? = null
    private var pendingDoublePress: Runnable? = null
    private var stemPrimaryDownTime = 0L

    private var isAmbientMode by mutableStateOf(false)

    private val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            isAmbientMode = true
        }

        override fun onExitAmbient() {
            isAmbientMode = false
        }

        override fun onUpdateAmbient() {
            // Optional: Handle periodic updates (once per minute)
        }
    }

    private val ambientObserver = AmbientLifecycleObserver(this, ambientCallback)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(ambientObserver)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            GridironTimerTheme {
                WearApp(isAmbientMode)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (isControlButton(event.keyCode, event)) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> onKeyDown(event.keyCode, event)
                KeyEvent.ACTION_UP -> onKeyUp(event.keyCode, event)
                else -> true
            }
        } else {
            super.dispatchKeyEvent(event)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (isControlButton(keyCode, event)) {
            if (event.repeatCount > 0) {
                // Ignore key repeats
                return true
            }
            stemPrimaryDownTime = event.eventTime
            val now = System.currentTimeMillis()
            val withinMultiPressWindow = now - lastStemPrimaryPressAt <= MULTI_PRESS_WINDOW_MS

            if (withinMultiPressWindow) {
                stemPressCount++
            } else {
                stemPressCount = 1
            }

            // Cancel any pending single or double press actions
            pendingSinglePress?.let { mainHandler.removeCallbacks(it) }
            pendingDoublePress?.let { mainHandler.removeCallbacks(it) }
            pendingSinglePress = null
            pendingDoublePress = null

            when (stemPressCount) {
                1 -> {
                    // Schedule single press action
                    pendingSinglePress = Runnable { 
                        onStemPrimaryPressed?.invoke()
                        stemPressCount = 0
                    }.also {
                        mainHandler.postDelayed(it, MULTI_PRESS_WINDOW_MS)
                    }
                }
                2 -> {
                    // Schedule double press action
                    pendingDoublePress = Runnable { 
                        onStemPrimaryDoublePressed?.invoke()
                        stemPressCount = 0
                    }.also {
                        mainHandler.postDelayed(it, MULTI_PRESS_WINDOW_MS)
                    }
                }
                3 -> {
                    // Execute triple press immediately
                    onStemPrimaryTriplePressed?.invoke()
                    stemPressCount = 0
                }
            }

            lastStemPrimaryPressAt = now
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (isControlButton(keyCode, event)) {
            val pressDuration = event.eventTime - stemPrimaryDownTime
            if (pressDuration >= LONG_PRESS_DURATION_MS) {
                // Cancel any pending actions
                pendingSinglePress?.let { mainHandler.removeCallbacks(it) }
                pendingDoublePress?.let { mainHandler.removeCallbacks(it) }
                pendingSinglePress = null
                pendingDoublePress = null
                stemPressCount = 0
                
                onStemPrimaryLongPressed?.invoke()
            }
            // Always consume the control button event
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        pendingSinglePress?.let { mainHandler.removeCallbacks(it) }
        pendingDoublePress?.let { mainHandler.removeCallbacks(it) }
        pendingSinglePress = null
        pendingDoublePress = null
        super.onDestroy()
    }

    sealed class Screen(val route: String) {
        object Main : Screen("main")
        object Timer : Screen("timer/{duration}/{mode}") {
            fun createRoute(duration: Long, isFlagMode: Boolean): String {
                val mode = if (isFlagMode) "flag" else "tackle"
                return "timer/$duration/$mode"
            }
        }

        object CustomTimer : Screen("custom_timer")
    }

    @Composable
    fun WearApp(isAmbient: Boolean) {
        val navController = rememberSwipeDismissableNavController()
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Screen.Main.route
        ) {
            composable(Screen.Main.route) { MainScreen(navController) }
            composable(
                route = Screen.Timer.route,
                arguments = listOf(
                    navArgument("duration") { type = NavType.LongType },
                    navArgument("mode") { type = NavType.StringType }
                )
            ) { navBackStackEntry ->
                val duration = navBackStackEntry.arguments?.getLong("duration") ?: 0L
                val mode = navBackStackEntry.arguments?.getString("mode") ?: "flag"
                val isFlagMode = parseIsFlagMode(mode)
                TimerScreen(
                    duration = duration,
                    navController = navController,
                    isFlagMode = isFlagMode,
                    timerConfig = AppTimerSettings.asTimerConfig(),
                    isAmbientMode = isAmbient,
                    onStemPrimaryHandlerChange = { handler -> onStemPrimaryPressed = handler },
                    onStemPrimaryDoubleHandlerChange = { handler -> onStemPrimaryDoublePressed = handler },
                    onStemPrimaryTripleHandlerChange = { handler -> onStemPrimaryTriplePressed = handler },
                    onStemPrimaryLongHandlerChange = { handler -> onStemPrimaryLongPressed = handler }
                )
            }
            composable(Screen.CustomTimer.route) { CustomTimerScreen(navController) }
        }
    }

    /**
     * Checks if the given key event corresponds to a control button.
     * Accepts STEM_PRIMARY (real device), VOLUME_UP, DPAD_UP, and KEYCODE_UNKNOWN with scanCode 125 (emulator workarounds).
     */
    private fun isControlButton(keyCode: Int, event: KeyEvent): Boolean {
        return keyCode == KeyEvent.KEYCODE_STEM_PRIMARY ||
               keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
               keyCode == KeyEvent.KEYCODE_DPAD_UP ||
               (keyCode == KeyEvent.KEYCODE_UNKNOWN && event.scanCode == VOLUME_BUTTON_SCAN_CODE)
    }

    private companion object {
        const val MULTI_PRESS_WINDOW_MS = 350L
        const val LONG_PRESS_DURATION_MS = 500L
        const val VOLUME_BUTTON_SCAN_CODE = 125  // Emulator volume button raw scan code
    }
}

internal fun parseIsFlagMode(mode: String?): Boolean = mode == "flag"
