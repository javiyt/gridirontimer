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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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
import yt.javi.gridirontimer.presentation.viewmodel.TimerRules
import yt.javi.gridirontimer.presentation.viewmodel.TimerState
import yt.javi.gridirontimer.presentation.viewmodel.TimerUtils
import yt.javi.gridirontimer.presentation.viewmodel.TimerViewModel

data class TimerScreenViewModels(
    val gameClockViewModel: TimerViewModel,
    val playClockViewModel: PlayClockViewModel,
    val sevenSecondClockViewModel: PlayClockViewModel,
    val timeoutViewModel: TimeoutViewModel,
)

data class GameAndPlayClockScreenState(
    val gameClockRemaining: Long,
    val gameClockState: TimerState,
    val gameClockViewModel: TimerViewModel,
    val playClockViewModel: PlayClockViewModel,
    val playClockState: TimerState,
    val playClockRemaining: Long,
    val timeoutViewModel: TimeoutViewModel
)

data class GameAndPlayClockScreenCallbacks(
    val startFlagPlayClock25: () -> Unit,
    val startSevenSecondClock: () -> Unit,
    val resetFlagTimers: () -> Unit
)

@Composable
fun createTimerScreenViewModels(): TimerScreenViewModels = TimerScreenViewModels(
    gameClockViewModel = viewModel(),
    playClockViewModel = viewModel(key = "play_clock"),
    sevenSecondClockViewModel = viewModel(key = "seven_second_clock"),
    timeoutViewModel = viewModel(),
)

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun TimerScreen(
    duration: Long,
    navController: NavController,
    initialDuration: Long,
    viewModels: TimerScreenViewModels = createTimerScreenViewModels(),
    onStemPrimaryHandlerChange: (((() -> Unit)?) -> Unit)? = null,
    onStemPrimaryDoubleHandlerChange: (((() -> Unit)?) -> Unit)? = null,
) {
    val context = LocalContext.current
    val gameClockState by viewModels.gameClockViewModel.state.collectAsState()
    val gameClockRemaining by viewModels.gameClockViewModel.time.collectAsState()
    val playClockState by viewModels.playClockViewModel.state.collectAsState()
    val playClockRemaining by viewModels.playClockViewModel.time.collectAsState()
    val sevenSecondClockState by viewModels.sevenSecondClockViewModel.state.collectAsState()
    val sevenSecondClockRemaining by viewModels.sevenSecondClockViewModel.time.collectAsState()
    val timeoutState by viewModels.timeoutViewModel.state.collectAsState()
    val timeoutTimeRemaining by viewModels.timeoutViewModel.time.collectAsState()
    val isFlagMode = TimerRules.isFlagMode(initialDuration)
    val activePlayClockState = if (isFlagMode && sevenSecondClockState in listOf(TimerState.Running, TimerState.Paused)) {
        sevenSecondClockState
    } else {
        playClockState
    }
    val activePlayClockRemaining = if (isFlagMode && sevenSecondClockState in listOf(TimerState.Running, TimerState.Paused)) {
        sevenSecondClockRemaining
    } else {
        playClockRemaining
    }
    val resetFlagTimers by rememberUpdatedState(newValue = {
        viewModels.playClockViewModel.stopAndReset(25_000L)
        viewModels.sevenSecondClockViewModel.stopAndReset(7_000L)
    })
    val startFlagPlayClock25 by rememberUpdatedState(newValue = {
        viewModels.sevenSecondClockViewModel.stopAndReset(7_000L)
        viewModels.playClockViewModel.startTimer(25_000L)
    })
    val startSevenSecondClock by rememberUpdatedState(newValue = {
        viewModels.playClockViewModel.stopAndReset(25_000L)
        viewModels.sevenSecondClockViewModel.startTimer(7_000L)
    })
    val stemPrimaryAction by rememberUpdatedState(newValue = {
        handleStemPrimaryAction(timeoutState, gameClockState, viewModels)
    })
    val stemPrimaryDoubleAction by rememberUpdatedState(newValue = {
        handleStemPrimaryDoubleAction(timeoutState, gameClockState, isFlagMode, viewModels, startFlagPlayClock25)
    })

    DisposableEffect(onStemPrimaryHandlerChange) {
        onStemPrimaryHandlerChange?.invoke { stemPrimaryAction() }
        onDispose { onStemPrimaryHandlerChange?.invoke(null) }
    }
    DisposableEffect(onStemPrimaryDoubleHandlerChange) {
        onStemPrimaryDoubleHandlerChange?.invoke { stemPrimaryDoubleAction() }
        onDispose { onStemPrimaryDoubleHandlerChange?.invoke(null) }
    }

    LaunchedEffect(key1 = duration) {
        viewModels.gameClockViewModel.startTimer(duration)
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
                isFlagMode,
                GameAndPlayClockScreenState(
                    gameClockRemaining,
                    gameClockState,
                    viewModels.gameClockViewModel,
                    viewModels.playClockViewModel,
                    activePlayClockState,
                    activePlayClockRemaining,
                    viewModels.timeoutViewModel
                ),
                GameAndPlayClockScreenCallbacks(
                    startFlagPlayClock25,
                    startSevenSecondClock,
                    resetFlagTimers
                )
            )
        } else {
            TimeOutScreen(timeoutTimeRemaining, timeoutState, viewModels.timeoutViewModel, gameClockRemaining)
        }
    }

    LaunchedEffect(key1 = gameClockRemaining) {
        if ((gameClockRemaining.isTwoMinutesWarning() || gameClockRemaining.isFinalSeconds()) && gameClockState is TimerState.Running) {
            Log.d("TimerScreen", "Vibration")
            TimerUtils.vibrate(context)
        }
    }
    LaunchedEffect(key1 = activePlayClockRemaining) {
        if (TimerRules.shouldVibratePlayClockWarning(activePlayClockRemaining, activePlayClockState)) {
            Log.d("TimerScreen", "Vibration")
            TimerUtils.vibrate(context)
        }
    }
    LaunchedEffect(key1 = timeoutTimeRemaining) {
        if (TimerRules.shouldVibrateTimeoutWarning(timeoutTimeRemaining, timeoutState)) {
            Log.d("TimerScreen", "Vibration")
            TimerUtils.vibrate(context, pulses = 2)
        }
    }
    LaunchedEffect(key1 = timeoutState) {
        if (TimerRules.shouldVibrateOnTimeoutFinish(timeoutState)) {
            Log.d("TimerScreen", "Timeout finished vibration")
            TimerUtils.vibrate(context, pulses = 3)
        }
    }
    LaunchedEffect(key1 = sevenSecondClockState) {
        if (TimerRules.shouldVibrateOnSevenSecondFinish(isFlagMode, sevenSecondClockState)) {
            Log.d("TimerScreen", "7-second clock finished vibration")
            TimerUtils.vibrate(context, pulses = 3)
        }
    }
}

@Composable
private fun GameAndPlayClockScreen(
    isFlagMode: Boolean,
    state: GameAndPlayClockScreenState,
    callbacks: GameAndPlayClockScreenCallbacks
) {
    GameClockDisplay(state.gameClockRemaining, state.gameClockState, state.gameClockViewModel)
    if (state.gameClockState !is TimerState.Finished) {
        Spacer(modifier = Modifier.width(16.dp))
        PlayClockSelector(isFlagMode, state.playClockViewModel, callbacks)
        PlayClockDisplay(
            isFlagMode,
            state.playClockState,
            state.playClockRemaining,
            state.playClockViewModel,
            callbacks.resetFlagTimers
        )
        Spacer(modifier = Modifier.width(16.dp))
        TimeoutButton(isFlagMode, state, callbacks)
    }
}

@Composable
private fun GameClockDisplay(
    gameClockRemaining: Long,
    gameClockState: TimerState,
    gameClockViewModel: TimerViewModel
) {
    Text(
        text = TimerUtils.formatTime(gameClockRemaining),
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = if (gameClockState is TimerState.Running) Color.White else Color.Gray
        ),
        modifier = Modifier.clickable(true, onClick = {
            when (gameClockState) {
                is TimerState.Running -> gameClockViewModel.pauseTimer()
                is TimerState.Paused -> gameClockViewModel.resumeTimer()
                else -> {}
            }
        }),
    )
}

@Composable
private fun PlayClockSelector(
    isFlagMode: Boolean,
    playClockViewModel: PlayClockViewModel,
    callbacks: GameAndPlayClockScreenCallbacks
) {
    if (isFlagMode) {
        FlagPlayClock(callbacks.startFlagPlayClock25, callbacks.startSevenSecondClock)
    } else {
        DualPlayClock(playClockViewModel)
    }
}

@Composable
private fun PlayClockDisplay(
    isFlagMode: Boolean,
    playClockState: TimerState,
    playClockRemaining: Long,
    playClockViewModel: PlayClockViewModel,
    resetFlagTimers: () -> Unit
) {
    val shouldShowPlayClock = playClockState !in listOf(TimerState.Idle, TimerState.Finished) && playClockRemaining >= 500L
    if (shouldShowPlayClock) {
        Text(
            text = TimerUtils.formatSeconds(playClockRemaining),
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = if (playClockState is TimerState.Running) Color.White else Color.Gray
            ),
            modifier = Modifier.clickable(true, onClick = {
                if (isFlagMode) {
                    resetFlagTimers()
                } else {
                    when (playClockState) {
                        is TimerState.Running -> playClockViewModel.pauseTimer()
                        is TimerState.Paused -> playClockViewModel.resumeTimer()
                        else -> {}
                    }
                }
            }),
        )
    }
}

@Composable
private fun TimeoutButton(
    isFlagMode: Boolean,
    state: GameAndPlayClockScreenState,
    callbacks: GameAndPlayClockScreenCallbacks
) {
    Button(
        onClick = {
            state.gameClockViewModel.pauseTimer()
            if (isFlagMode) {
                callbacks.resetFlagTimers()
            } else {
                state.playClockViewModel.cancelTimer()
            }
            state.timeoutViewModel.startTimer()
        },
        colors = ButtonDefaults.primaryButtonColors()
    ) {
        Text(text = stringResource(R.string.timeout))
    }
}

@Composable
private fun DualPlayClock(playClockViewModel: PlayClockViewModel) {
    Row {
        Button(
            onClick = {
                playClockViewModel.startTimer(25_000L)
            },
            colors = ButtonDefaults.primaryButtonColors(),
        ) {
            Text(text = stringResource(R.string._25s))
        }
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
private fun FlagPlayClock(
    startFlagPlayClock25: () -> Unit,
    startSevenSecondClock: () -> Unit
) {
    Row {
        Button(
            onClick = startFlagPlayClock25,
            colors = ButtonDefaults.primaryButtonColors(),
        ) {
            Text(text = stringResource(R.string._25s))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = startSevenSecondClock,
            colors = ButtonDefaults.primaryButtonColors(),
        ) {
            Text(text = stringResource(R.string._7s))
        }
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
            when (timeoutState) {
                is TimerState.Running -> timeoutViewModel.pauseTimer()
                is TimerState.Paused -> timeoutViewModel.resumeTimer()
                else -> {}
            }
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

private fun handleStemPrimaryAction(
    timeoutState: TimerState,
    gameClockState: TimerState,
    viewModels: TimerScreenViewModels
) {
    when {
        timeoutState in listOf(TimerState.Running, TimerState.Paused) -> {
            when (timeoutState) {
                is TimerState.Running -> viewModels.timeoutViewModel.pauseTimer()
                is TimerState.Paused -> viewModels.timeoutViewModel.resumeTimer()
                else -> {}
            }
        }
        gameClockState is TimerState.Running -> viewModels.gameClockViewModel.pauseTimer()
        gameClockState is TimerState.Paused -> viewModels.gameClockViewModel.resumeTimer()
    }
}

private fun handleStemPrimaryDoubleAction(
    timeoutState: TimerState,
    gameClockState: TimerState,
    isFlagMode: Boolean,
    viewModels: TimerScreenViewModels,
    startFlagPlayClock25: () -> Unit
) {
    if (timeoutState !in listOf(TimerState.Running, TimerState.Paused) && gameClockState !is TimerState.Finished) {
        if (isFlagMode) {
            startFlagPlayClock25()
        } else {
            val playClockDuration = TimerRules.playClockDurationOnDoublePress(
                isFlagMode = false,
                gameClockState = gameClockState
            )
            viewModels.playClockViewModel.startTimer(playClockDuration)
        }
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
