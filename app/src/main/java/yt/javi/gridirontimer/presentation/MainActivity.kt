/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package yt.javi.gridirontimer.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
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
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastStemPrimaryPressAt = 0L
    private var pendingSinglePress: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            GridironTimerTheme {
                WearApp()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.d("MainActivity", "onKeyDown: $keyCode")
        if (keyCode == KeyEvent.KEYCODE_STEM_PRIMARY && event.action == KeyEvent.ACTION_DOWN) {
            val now = System.currentTimeMillis()
            val withinDoublePressWindow = now - lastStemPrimaryPressAt <= DOUBLE_PRESS_WINDOW_MS
            if (withinDoublePressWindow) {
                pendingSinglePress?.let { mainHandler.removeCallbacks(it) }
                pendingSinglePress = null
                onStemPrimaryDoublePressed?.invoke()
            } else {
                pendingSinglePress = Runnable { onStemPrimaryPressed?.invoke() }.also {
                    mainHandler.postDelayed(it, DOUBLE_PRESS_WINDOW_MS)
                }
            }
            lastStemPrimaryPressAt = now
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        pendingSinglePress?.let { mainHandler.removeCallbacks(it) }
        pendingSinglePress = null
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
    fun WearApp() {
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
                val isFlagMode = mode == "flag"
                TimerScreen(
                    duration = duration,
                    navController = navController,
                    isFlagMode = isFlagMode,
                    timerConfig = AppTimerSettings.asTimerConfig(),
                    onStemPrimaryHandlerChange = { handler -> onStemPrimaryPressed = handler },
                    onStemPrimaryDoubleHandlerChange = { handler -> onStemPrimaryDoublePressed = handler }
                )
            }
            composable(Screen.CustomTimer.route) { CustomTimerScreen(navController) }
        }
    }

    private companion object {
        const val DOUBLE_PRESS_WINDOW_MS = 350L
    }
}
