package yt.javi.gridirontimer.presentation.views

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import yt.javi.gridirontimer.R
import yt.javi.gridirontimer.presentation.theme.GridironTimerTheme
import yt.javi.gridirontimer.presentation.viewmodel.PlayClockViewModel
import yt.javi.gridirontimer.presentation.viewmodel.TimerConfig
import yt.javi.gridirontimer.presentation.viewmodel.TimerConfigs
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

private enum class FlagActiveButton {
    NONE,
    TWENTY_FIVE,
    SEVEN
}

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
    timerConfig: TimerConfig = TimerConfigs.Default,
    viewModels: TimerScreenViewModels = createTimerScreenViewModels(),
    onStemPrimaryHandlerChange: (((() -> Unit)?) -> Unit)? = null,
    onStemPrimaryDoubleHandlerChange: (((() -> Unit)?) -> Unit)? = null,
) {
    val context = LocalContext.current
    val gameClockState by viewModels.gameClockViewModel.state.collectAsState()
    val gameClockRemaining by viewModels.gameClockViewModel.time.collectAsState()
    val playClockState by viewModels.playClockViewModel.state.collectAsState()
    val playClockRemaining by viewModels.playClockViewModel.time.collectAsState()
    val playClockPresetDuration by viewModels.playClockViewModel.presetDuration.collectAsState()
    val sevenSecondClockState by viewModels.sevenSecondClockViewModel.state.collectAsState()
    val sevenSecondClockRemaining by viewModels.sevenSecondClockViewModel.time.collectAsState()
    val timeoutState by viewModels.timeoutViewModel.state.collectAsState()
    val timeoutTimeRemaining by viewModels.timeoutViewModel.time.collectAsState()
    val isFlagMode = TimerRules.isFlagMode(initialDuration, timerConfig)
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
    val flagActiveButton = when {
        sevenSecondClockState in listOf(TimerState.Running, TimerState.Paused) -> FlagActiveButton.SEVEN
        playClockState in listOf(TimerState.Running, TimerState.Paused) -> FlagActiveButton.TWENTY_FIVE
        else -> FlagActiveButton.NONE
    }
    val resetFlagTimers by rememberUpdatedState(newValue = {
        viewModels.playClockViewModel.stopAndReset(timerConfig.flagPlayClockMs)
        viewModels.sevenSecondClockViewModel.stopAndReset(timerConfig.flagSevenSecondMs)
    })
    val startFlagPlayClock25 by rememberUpdatedState(newValue = {
        viewModels.sevenSecondClockViewModel.stopAndReset(timerConfig.flagSevenSecondMs)
        viewModels.playClockViewModel.startTimer(timerConfig.flagPlayClockMs)
    })
    val startSevenSecondClock by rememberUpdatedState(newValue = {
        viewModels.playClockViewModel.stopAndReset(timerConfig.flagPlayClockMs)
        viewModels.sevenSecondClockViewModel.startTimer(timerConfig.flagSevenSecondMs)
    })
    val stemPrimaryAction by rememberUpdatedState(newValue = {
        handleStemPrimaryAction(timeoutState, gameClockState, viewModels)
    })
    val stemPrimaryDoubleAction by rememberUpdatedState(newValue = {
        handleStemPrimaryDoubleAction(
            timeoutState,
            gameClockState,
            isFlagMode,
            viewModels,
            startFlagPlayClock25,
            timerConfig
        )
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
        modifier = Modifier.fillMaxSize()
    ) {
        val bg = Brush.radialGradient(
            colors = listOf(Color(0xFF132033), MaterialTheme.colors.background),
            radius = 360f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
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
                        ),
                playClockPresetDuration = playClockPresetDuration,
                flagActiveButton = flagActiveButton
                    )
                } else {
                    TimeOutScreen(timeoutTimeRemaining, timeoutState, viewModels.timeoutViewModel, gameClockRemaining)
                }
            }
        }
    }

    LaunchedEffect(key1 = gameClockRemaining) {
        if (
            (gameClockRemaining.isTwoMinutesWarning(timerConfig) || gameClockRemaining.isFinalSeconds(timerConfig)) &&
            gameClockState is TimerState.Running
        ) {
            Log.d("TimerScreen", "Vibration")
            TimerUtils.vibrate(context)
        }
    }
    LaunchedEffect(key1 = activePlayClockRemaining) {
        if (TimerRules.shouldVibratePlayClockWarning(activePlayClockRemaining, activePlayClockState, timerConfig)) {
            Log.d("TimerScreen", "Vibration")
            TimerUtils.vibrate(context)
        }
    }
    LaunchedEffect(key1 = timeoutTimeRemaining) {
        if (TimerRules.shouldVibrateTimeoutWarning(timeoutTimeRemaining, timeoutState, timerConfig)) {
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
    callbacks: GameAndPlayClockScreenCallbacks,
    playClockPresetDuration: Long?,
    flagActiveButton: FlagActiveButton
) {
    GameClockDisplay(state.gameClockRemaining, state.gameClockState, state.gameClockViewModel)
    if (state.gameClockState !is TimerState.Finished) {
        Spacer(modifier = Modifier.height(10.dp))
        PlayClockSelector(
            isFlagMode = isFlagMode,
            playClockViewModel = state.playClockViewModel,
            callbacks = callbacks,
            playClockState = state.playClockState,
            playClockRemaining = state.playClockRemaining,
            playClockPresetDuration = playClockPresetDuration,
            flagActiveButton = flagActiveButton
        )
        Spacer(modifier = Modifier.height(10.dp))
        TimeoutButton(isFlagMode, state, callbacks)
    }
}

@Composable
private fun GameClockDisplay(
    gameClockRemaining: Long,
    gameClockState: TimerState,
    gameClockViewModel: TimerViewModel
) {
    val gameClockProgress by animateFloatAsState(
        targetValue = if (gameClockState is TimerState.Running) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 300f),
        label = "game_clock_progress"
    )
    val gameClockColor = lerp(MaterialTheme.colors.onSurface, MaterialTheme.colors.onBackground, gameClockProgress)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = TimerUtils.formatTime(gameClockRemaining),
            style = TextStyle(
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = gameClockColor,
                letterSpacing = 0.6.sp
            ),
            modifier = Modifier.clickable(true, onClick = {
                when (gameClockState) {
                    is TimerState.Running -> gameClockViewModel.pauseTimer()
                    is TimerState.Paused -> gameClockViewModel.resumeTimer()
                    else -> {}
                }
            }),
        )
        Text(
            text = when (gameClockState) {
                is TimerState.Running -> stringResource(R.string.status_running)
                is TimerState.Paused -> stringResource(R.string.status_paused)
                else -> stringResource(R.string.status_ready)
            },
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colors.onSurface
            )
        )
    }
}

@Composable
private fun PlayClockSelector(
    isFlagMode: Boolean,
    playClockViewModel: PlayClockViewModel,
    callbacks: GameAndPlayClockScreenCallbacks,
    playClockState: TimerState,
    playClockRemaining: Long,
    playClockPresetDuration: Long?,
    flagActiveButton: FlagActiveButton
) {
    if (isFlagMode) {
        FlagPlayClock(
            startFlagPlayClock25 = callbacks.startFlagPlayClock25,
            startSevenSecondClock = callbacks.startSevenSecondClock,
            activeButton = flagActiveButton,
            playClockRemaining = playClockRemaining
        )
    } else {
        DualPlayClock(
            playClockViewModel = playClockViewModel,
            playClockState = playClockState,
            playClockRemaining = playClockRemaining,
            playClockPresetDuration = playClockPresetDuration
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
        modifier = Modifier.width(92.dp),
        colors = ButtonDefaults.secondaryButtonColors()
    ) {
        Text(text = stringResource(R.string.timeout))
    }
}

@Composable
private fun DualPlayClock(
    playClockViewModel: PlayClockViewModel,
    playClockState: TimerState,
    playClockRemaining: Long,
    playClockPresetDuration: Long?
) {
    val isPlayClockActive = playClockState in listOf(TimerState.Running, TimerState.Paused)
    val is25Active = isPlayClockActive && playClockPresetDuration == 25_000L
    val is40Active = isPlayClockActive && playClockPresetDuration == 40_000L
    val playClockLabel = TimerUtils.formatSeconds(playClockRemaining)
    Row {
        Button(
            onClick = {
                playClockViewModel.startTimer(25_000L)
            },
            modifier = Modifier.width(88.dp),
            colors = ButtonDefaults.primaryButtonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            ),
        ) {
            Text(
                text = if (is25Active) playClockLabel else stringResource(R.string._25s),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = {
                playClockViewModel.startTimer(40_000L)
            },
            modifier = Modifier.width(88.dp),
            colors = ButtonDefaults.secondaryButtonColors(
                backgroundColor = MaterialTheme.colors.secondary,
                contentColor = MaterialTheme.colors.onSecondary
            )
        ) {
            Text(
                text = if (is40Active) playClockLabel else stringResource(R.string._40s),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun FlagPlayClock(
    startFlagPlayClock25: () -> Unit,
    startSevenSecondClock: () -> Unit,
    activeButton: FlagActiveButton,
    playClockRemaining: Long
) {
    val activeLabel = TimerUtils.formatSeconds(playClockRemaining)
    Row {
        Button(
            onClick = startFlagPlayClock25,
            modifier = Modifier.width(88.dp),
            colors = ButtonDefaults.primaryButtonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            ),
        ) {
            Text(
                text = if (activeButton == FlagActiveButton.TWENTY_FIVE) activeLabel else stringResource(R.string._25s),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = startSevenSecondClock,
            modifier = Modifier.width(88.dp),
            colors = ButtonDefaults.secondaryButtonColors(
                backgroundColor = MaterialTheme.colors.secondary,
                contentColor = MaterialTheme.colors.onSecondary
            ),
        ) {
            Text(
                text = if (activeButton == FlagActiveButton.SEVEN) activeLabel else stringResource(R.string._7s),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )
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
    val timeoutProgress by animateFloatAsState(
        targetValue = if (timeoutState is TimerState.Running) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 260f),
        label = "timeout_progress"
    )
    val timeoutColor = lerp(MaterialTheme.colors.onSurface, MaterialTheme.colors.secondary, timeoutProgress)
    Text(
        text = stringResource(R.string.game_time, TimerUtils.formatTime(gameClockRemaining)),
        style = TextStyle(
            fontSize = 17.sp,
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )
    )
    Text(
        text = TimerUtils.formatSeconds(timeoutTimeRemaining),
        style = TextStyle(
            fontSize = 54.sp,
            fontWeight = FontWeight.ExtraBold,
            color = timeoutColor,
            letterSpacing = 0.4.sp
        ),
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
        modifier = Modifier.width(100.dp),
        colors = ButtonDefaults.secondaryButtonColors()
    ) {
        Text(text = stringResource(R.string.stop), style = TextStyle(fontWeight = FontWeight.Bold))
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
    startFlagPlayClock25: () -> Unit,
    timerConfig: TimerConfig
) {
    if (timeoutState !in listOf(TimerState.Running, TimerState.Paused) && gameClockState !is TimerState.Finished) {
        if (isFlagMode) {
            startFlagPlayClock25()
        } else {
            val playClockDuration = TimerRules.playClockDurationOnDoublePress(
                isFlagMode = false,
                gameClockState = gameClockState,
                config = timerConfig
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
            duration = TimerConfigs.Default.flagGameDurationMs,
            rememberSwipeDismissableNavController(),
            initialDuration = TimerConfigs.Default.flagGameDurationMs
        )
    }
}

private fun Long.isTwoMinutesWarning(config: TimerConfig = TimerConfigs.Default) =
    this in config.twoMinuteWarningEndMs..config.twoMinuteWarningStartMs

private fun Long.isFinalSeconds(config: TimerConfig = TimerConfigs.Default) =
    this <= config.playClockWarningMs
