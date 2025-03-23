package yt.javi.gridirontimer.presentation.views

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import yt.javi.gridirontimer.R
import yt.javi.gridirontimer.presentation.theme.GridironTimerTheme
import yt.javi.gridirontimer.presentation.viewmodel.PlayClockViewModel
import yt.javi.gridirontimer.presentation.viewmodel.TimeoutViewModel
import yt.javi.gridirontimer.presentation.viewmodel.TimerState
import yt.javi.gridirontimer.presentation.viewmodel.TimerUtils
import yt.javi.gridirontimer.presentation.viewmodel.TimerViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun TimerScreen(
    duration: Long,
    navController: NavController,
    gameClockViewModel: TimerViewModel = viewModel(),
    playClockViewModel: PlayClockViewModel = viewModel(),
    timeoutViewModel: TimeoutViewModel = viewModel(),
    initialDuration: Long,
) {
    val context = LocalContext.current
    val gameClockState by gameClockViewModel.state.collectAsState()
    val gameClockRemaining by gameClockViewModel.time.collectAsState()
    val playClockState by playClockViewModel.state.collectAsState()
    val playClockRemaining by playClockViewModel.time.collectAsState()
    val timeoutState by timeoutViewModel.state.collectAsState()
    val timeoutTimeRemaining by timeoutViewModel.time.collectAsState()

    LaunchedEffect(key1 = duration) {
        gameClockViewModel.startTimer(duration)
    }

    if (gameClockState is TimerState.Finished) {
        LaunchedEffect(key1 = Unit) {
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (timeoutState !in listOf(TimerState.Running, TimerState.Paused)) {
            GameAndPlayClockScreen(
                gameClockRemaining,
                gameClockState,
                gameClockViewModel,
                initialDuration,
                playClockViewModel,
                playClockState,
                playClockRemaining,
                timeoutViewModel
            )
        } else {
            TimeOutScreen(timeoutTimeRemaining, timeoutState, timeoutViewModel, gameClockRemaining)
        }
    }

    LaunchedEffect(key1 = gameClockRemaining) {
        if ((gameClockRemaining.isTwoMinutesWarning() || gameClockRemaining.isFinalSeconds()) && gameClockState is TimerState.Running) {
            Log.d("TimerScreen", "Vibration")
            TimerUtils.vibrate(context)
            //TimerUtils.playSound(context)
        }
    }
    LaunchedEffect(key1 = playClockRemaining) {
        if (playClockRemaining <= 10_000L && playClockState is TimerState.Running) {
            Log.d("TimerScreen", "Vibration")
            TimerUtils.vibrate(context)
            //TimerUtils.playSound(context)
        }
    }
    LaunchedEffect(key1 = timeoutTimeRemaining) {
        if (timeoutTimeRemaining <= 5_000L && timeoutState is TimerState.Running) {
            Log.d("TimerScreen", "Vibration")
            TimerUtils.vibrate(context)
            //TimerUtils.playSound(context)
        }
    }
}

@Composable
private fun GameAndPlayClockScreen(
    gameClockRemaining: Long,
    gameClockState: TimerState,
    gameClockViewModel: TimerViewModel,
    initialDuration: Long,
    playClockViewModel: PlayClockViewModel,
    playClockState: TimerState,
    playClockRemaining: Long,
    timeoutViewModel: TimeoutViewModel
) {
    Text(
        text = TimerUtils.formatTime(gameClockRemaining),
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = if (gameClockState is TimerState.Running) Color.White else Color.Gray
        ),
        modifier = Modifier.clickable(true, onClick = {
            if (gameClockState is TimerState.Running) gameClockViewModel.pauseTimer() else gameClockViewModel.resumeTimer()
        }),
    )
    if (gameClockState !is TimerState.Finished) {
        Spacer(modifier = Modifier.width(16.dp))

        if (initialDuration == 20L * 60L * 1000L) {
            FlagPlayClock(playClockViewModel)
        } else {
            DualPlayClock(playClockViewModel)
        }

        if (playClockState !in listOf(
                TimerState.Idle,
                TimerState.Finished
            ) && playClockRemaining >= 500L
        ) {
            Text(
                text = TimerUtils.formatSeconds(playClockRemaining),
                style = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (playClockState is TimerState.Running) Color.White else Color.Gray
                ),
                modifier = Modifier.clickable(true, onClick = {
                    if (playClockState is TimerState.Running) playClockViewModel.pauseTimer() else playClockViewModel.resumeTimer()
                }),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = {
                gameClockViewModel.pauseTimer()
                playClockViewModel.cancelTimer()
                timeoutViewModel.startTimer()
            },
            colors = ButtonDefaults.primaryButtonColors()
        ) {
            Text(text = stringResource(R.string.timeout))
        }
    }
}

@Composable
private fun DualPlayClock(playClockViewModel: PlayClockViewModel) {
    Row {
        FlagPlayClock(playClockViewModel)
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = {
                playClockViewModel.startTimer(40_000L)
            },
            colors = ButtonDefaults.primaryButtonColors()
        ) {
            Text(text = stringResource(R.string._40s))
        }
    }
}

@Composable
private fun FlagPlayClock(playClockViewModel: PlayClockViewModel) {
    Button(
        onClick = {
            playClockViewModel.startTimer(25_000L)
        },
        colors = ButtonDefaults.primaryButtonColors(),
    ) {
        Text(text = stringResource(R.string._25s))
    }
}

@Composable
private fun TimeOutScreen(
    timeoutTimeRemaining: Long,
    timeoutState: TimerState,
    timeoutViewModel: TimeoutViewModel,
    gameClockRemaining: Long
) {
    Text(text = stringResource(R.string.game_time, TimerUtils.formatTime(gameClockRemaining)))
    Text(
        text = TimerUtils.formatSeconds(timeoutTimeRemaining),
        style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
        modifier = Modifier.clickable(true, onClick = {
            if (timeoutState is TimerState.Running) timeoutViewModel.pauseTimer() else timeoutViewModel.resumeTimer()
        }),
    )
    Button(
        onClick = {
            timeoutViewModel.stopTimer()
        },
        colors = ButtonDefaults.primaryButtonColors()
    ) {
        Text(text = stringResource(R.string.stop))
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun TimerPreview() {
    GridironTimerTheme {
        TimerScreen(
            duration = 20L * 60L * 1000L,
            rememberSwipeDismissableNavController(),
            initialDuration = 20L * 60L * 1000L
        )
    }
}

private fun Long.isTwoMinutesWarning() = this <= 120_000L && this >= 115_000L

private fun Long.isFinalSeconds() = this <= 10_000L