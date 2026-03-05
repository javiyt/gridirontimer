package yt.javi.gridirontimer.presentation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import yt.javi.gridirontimer.R
import yt.javi.gridirontimer.presentation.MainActivity.Screen
import yt.javi.gridirontimer.presentation.theme.GridironTimerTheme
import yt.javi.gridirontimer.presentation.viewmodel.AppTimerSettings

@Composable
fun MainScreen(navController: NavController) {
    var reveal by remember { mutableStateOf(false) }
    val flagDurationMs = AppTimerSettings.flagGameDurationMs
    val tackleDurationMs = AppTimerSettings.tackleGameDurationMs
    LaunchedEffect(Unit) { reveal = true }
    val titleAlpha by animateFloatAsState(
        targetValue = if (reveal) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 220f),
        label = "title_alpha"
    )
    val topButtonScale by animateFloatAsState(
        targetValue = if (reveal) 1f else 0.9f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 280f),
        label = "flag_scale"
    )
    val bottomButtonScale by animateFloatAsState(
        targetValue = if (reveal) 1f else 0.88f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 250f),
        label = "tackle_scale"
    )
    val bg = Brush.radialGradient(
        colors = listOf(Color(0xFF16243A), MaterialTheme.colors.background),
        radius = 360f
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier.alpha(titleAlpha),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colors.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate(Screen.Timer.createRoute(flagDurationMs, isFlagMode = true)) },
                    modifier = Modifier
                        .width(124.dp)
                        .height(44.dp)
                        .scale(topButtonScale),
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Text(
                        text = stringResource(R.string.flag),
                        style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate(Screen.Timer.createRoute(tackleDurationMs, isFlagMode = false)) },
                    modifier = Modifier
                        .width(124.dp)
                        .height(44.dp)
                        .scale(bottomButtonScale),
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Text(
                        text = stringResource(R.string.tackle),
                        style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate(Screen.CustomTimer.route) },
                    modifier = Modifier
                        .width(124.dp)
                        .height(36.dp),
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Text(
                        text = stringResource(R.string.settings),
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    GridironTimerTheme {
        MainScreen(rememberSwipeDismissableNavController())
    }
}
