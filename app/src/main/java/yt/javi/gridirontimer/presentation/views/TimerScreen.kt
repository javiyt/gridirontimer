package yt.javi.gridirontimer.presentation.views

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.ui.platform.LocalView
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
import yt.javi.gridirontimer.presentation.TimerService
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

data class StemHandlerCallbacks(
    val onPrimaryChange: (((() -> Unit)?) -> Unit)? = null,
    val onPrimaryDoubleChange: (((() -> Unit)?) -> Unit)? = null,
    val onPrimaryTripleChange: (((() -> Unit)?) -> Unit)? = null,
    val onPrimaryLongChange: (((() -> Unit)?) -> Unit)? = null,
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
    isFlagMode: Boolean,
    timerConfig: TimerConfig = TimerConfigs.Default,
    viewModels: TimerScreenViewModels = createTimerScreenViewModels(),
    isAmbientMode: Boolean = false,
    stemHandlerCallbacks: StemHandlerCallbacks = StemHandlerCallbacks(),
) {
    val context = LocalContext.current
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
            context.stopService(Intent(context, TimerService::class.java))
        }
    }
    val gameClockState by viewModels.gameClockViewModel.state.collectAsState()
    val gameClockRemaining by viewModels.gameClockViewModel.time.collectAsState()
    val playClockState by viewModels.playClockViewModel.state.collectAsState()
    val playClockRemaining by viewModels.playClockViewModel.time.collectAsState()
    val playClockPresetDuration by viewModels.playClockViewModel.presetDuration.collectAsState()
    val sevenSecondClockState by viewModels.sevenSecondClockViewModel.state.collectAsState()
    val sevenSecondClockRemaining by viewModels.sevenSecondClockViewModel.time.collectAsState()
    val timeoutState by viewModels.timeoutViewModel.state.collectAsState()
    val timeoutTimeRemaining by viewModels.timeoutViewModel.time.collectAsState()
    
    LaunchedEffect(gameClockState, playClockState, sevenSecondClockState, timeoutState) {
        val isAnyTimerRunning = gameClockState is TimerState.Running || 
                                playClockState is TimerState.Running || 
                                sevenSecondClockState is TimerState.Running || 
                                timeoutState is TimerState.Running
        
        if (isAnyTimerRunning) {
            context.startForegroundService(Intent(context, TimerService::class.java))
        }
    }

    val activePlayClockState = resolveActivePlayClockState(isFlagMode, sevenSecondClockState, playClockState)
    val activePlayClockRemaining = resolveActivePlayClockRemaining(isFlagMode, sevenSecondClockState, sevenSecondClockRemaining, playClockRemaining)
    val flagActiveButton = resolveFlagActiveButton(sevenSecondClockState, playClockState)
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
    val stemPrimaryTripleAction by rememberUpdatedState(newValue = {
        handleStemPrimaryTripleAction(
            timeoutState,
            gameClockState,
            isFlagMode,
            viewModels,
            timerConfig
        )
    })
    val stemPrimaryLongAction by rememberUpdatedState(newValue = {
        handleStemPrimaryLongAction(
            timeoutState,
            gameClockState,
            isFlagMode,
            viewModels,
            timerConfig
        )
    })

    DisposableEffect(stemHandlerCallbacks.onPrimaryChange) {
        stemHandlerCallbacks.onPrimaryChange?.invoke { stemPrimaryAction() }
        onDispose { stemHandlerCallbacks.onPrimaryChange?.invoke(null) }
    }
    DisposableEffect(stemHandlerCallbacks.onPrimaryDoubleChange) {
        stemHandlerCallbacks.onPrimaryDoubleChange?.invoke { stemPrimaryDoubleAction() }
        onDispose { stemHandlerCallbacks.onPrimaryDoubleChange?.invoke(null) }
    }
    DisposableEffect(stemHandlerCallbacks.onPrimaryTripleChange) {
        stemHandlerCallbacks.onPrimaryTripleChange?.invoke { stemPrimaryTripleAction() }
        onDispose { stemHandlerCallbacks.onPrimaryTripleChange?.invoke(null) }
    }
    DisposableEffect(stemHandlerCallbacks.onPrimaryLongChange) {
        stemHandlerCallbacks.onPrimaryLongChange?.invoke { stemPrimaryLongAction() }
        onDispose { stemHandlerCallbacks.onPrimaryLongChange?.invoke(null) }
    }

    // Store the duration for later start when user taps on the time display
    LaunchedEffect(key1 = Unit) {
        // Timer is not started automatically anymore
        // User must tap on the game clock time display to start it
    }

    if (gameClockState is TimerState.Finished) {
        LaunchedEffect(key1 = Unit) {
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val bg = if (isAmbientMode) {
            Brush.linearGradient(colors = listOf(Color.Black, Color.Black))
        } else {
            Brush.radialGradient(
                colors = listOf(Color(0xFF132033), MaterialTheme.colors.background),
                radius = 360f
            )
        }
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
                flagActiveButton = flagActiveButton,
                gameClockDuration = duration,
                timeoutDurationMs = timerConfig.timeoutDurationMs,
                isAmbientMode = isAmbientMode
                    )
                } else {
                    TimeOutScreen(
                        timeoutTimeRemaining, 
                        timeoutState, 
                        viewModels.timeoutViewModel, 
                        gameClockRemaining,
                        isAmbientMode = isAmbientMode
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = gameClockRemaining) {
        if (
            (gameClockRemaining.isTwoMinutesWarning(timerConfig) || gameClockRemaining.isFinalSeconds(timerConfig)) &&
            gameClockState is TimerState.Running && !isAmbientMode
        ) {
            if (gameClockRemaining.isTwoMinutesWarning(timerConfig)) {
                TimerUtils.vibrate(context, pulses = 5)
            } else {
                TimerUtils.vibrate(context)
            }
        }
    }
    LaunchedEffect(key1 = activePlayClockRemaining) {
        if (TimerRules.shouldVibratePlayClockWarning(activePlayClockRemaining, activePlayClockState, timerConfig) && !isAmbientMode) {
            TimerUtils.vibrate(context)
        }
    }
    LaunchedEffect(key1 = timeoutTimeRemaining) {
        if (TimerRules.shouldVibrateTimeoutWarning(timeoutTimeRemaining, timeoutState, timerConfig) && !isAmbientMode) {
            TimerUtils.vibrate(context, pulses = 2)
        }
    }
    LaunchedEffect(key1 = timeoutState) {
        if (TimerRules.shouldVibrateOnTimeoutFinish(timeoutState) && !isAmbientMode) {
            TimerUtils.vibrate(context, pulses = 3)
        }
    }
    LaunchedEffect(key1 = sevenSecondClockState) {
        if (TimerRules.shouldVibrateOnSevenSecondFinish(isFlagMode, sevenSecondClockState) && !isAmbientMode) {
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
    flagActiveButton: FlagActiveButton,
    gameClockDuration: Long,
    timeoutDurationMs: Long,
    isAmbientMode: Boolean = false
) {
    GameClockDisplay(
        gameClockRemaining = state.gameClockRemaining,
        gameClockState = state.gameClockState,
        gameClockViewModel = state.gameClockViewModel,
        gameClockDuration = gameClockDuration,
        isAmbientMode = isAmbientMode
    )
    if (state.gameClockState !is TimerState.Finished && !isAmbientMode) {
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
        TimeoutButton(isFlagMode, state, callbacks, timeoutDurationMs)
    }
}

@Composable
private fun GameClockDisplay(
    gameClockRemaining: Long,
    gameClockState: TimerState,
    gameClockViewModel: TimerViewModel,
    gameClockDuration: Long = 0L,
    isAmbientMode: Boolean = false
) {
    val gameClockProgress by animateFloatAsState(
        targetValue = if (gameClockState is TimerState.Running) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 300f),
        label = "game_clock_progress"
    )
    val gameClockColor = if (isAmbientMode) Color.White else lerp(MaterialTheme.colors.onSurface, MaterialTheme.colors.onBackground, gameClockProgress)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = TimerUtils.formatTime(
                if (gameClockState is TimerState.Idle && gameClockDuration > 0L) 
                    gameClockDuration 
                else 
                    gameClockRemaining
            ),
            style = TextStyle(
                fontSize = if (isAmbientMode) 54.sp else 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = gameClockColor,
                letterSpacing = 0.6.sp
            ),
            modifier = Modifier.clickable(!isAmbientMode, onClick = {
                when (gameClockState) {
                    is TimerState.Running -> gameClockViewModel.pauseTimer()
                    is TimerState.Paused -> gameClockViewModel.resumeTimer()
                    is TimerState.Idle -> if (gameClockDuration > 0L) gameClockViewModel.startTimer(gameClockDuration)
                    else -> {}
                }
            }),
        )
        if (!isAmbientMode) {
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
    callbacks: GameAndPlayClockScreenCallbacks,
    timeoutDurationMs: Long
) {
    Button(
        onClick = {
            state.gameClockViewModel.pauseTimer()
            if (isFlagMode) {
                callbacks.resetFlagTimers()
            } else {
                state.playClockViewModel.cancelTimer()
            }
            state.timeoutViewModel.startTimer(timeoutDurationMs)
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
    gameClockRemaining: Long,
    isAmbientMode: Boolean = false
) {
    val timeoutProgress by animateFloatAsState(
        targetValue = if (timeoutState is TimerState.Running) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 260f),
        label = "timeout_progress"
    )
    val timeoutColor = if (isAmbientMode) Color.White else lerp(MaterialTheme.colors.onSurface, MaterialTheme.colors.secondary, timeoutProgress)
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
            fontSize = if (isAmbientMode) 64.sp else 54.sp,
            fontWeight = FontWeight.ExtraBold,
            color = timeoutColor,
            letterSpacing = 0.4.sp
        ),
        modifier = Modifier.clickable(!isAmbientMode, onClick = {
            when (timeoutState) {
                is TimerState.Running -> timeoutViewModel.pauseTimer()
                is TimerState.Paused -> timeoutViewModel.resumeTimer()
                else -> {}
            }
        }),
    )
    if (!isAmbientMode) {
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
            // Flag mode: 2 taps = reset 25 second play clock
            startFlagPlayClock25()
        } else {
            // Tackle mode: 2 taps = start 40 second play clock
            viewModels.playClockViewModel.startTimer(timerConfig.tacklePlayClock40Ms)
        }
    }
}

private fun handleStemPrimaryTripleAction(
    timeoutState: TimerState,
    gameClockState: TimerState,
    isFlagMode: Boolean,
    viewModels: TimerScreenViewModels,
    timerConfig: TimerConfig
) {
    if (gameClockState !is TimerState.Finished) {
        if (isFlagMode) {
            // Flag mode: 3 taps = start timeout
            if (timeoutState is TimerState.Idle) {
                viewModels.timeoutViewModel.startTimer(timerConfig.timeoutDurationMs)
            }
        } else {
            // Tackle mode: 3 taps = start 25 second play clock
            if (timeoutState !in listOf(TimerState.Running, TimerState.Paused)) {
                viewModels.playClockViewModel.startTimer(timerConfig.tacklePlayClock25Ms)
            }
        }
    }
}

private fun handleStemPrimaryLongAction(
    timeoutState: TimerState,
    gameClockState: TimerState,
    isFlagMode: Boolean,
    viewModels: TimerScreenViewModels,
    timerConfig: TimerConfig
) {
    if (gameClockState !is TimerState.Finished) {
        // Tackle mode only: long press = start timeout
        if (!isFlagMode && timeoutState is TimerState.Idle) {
            viewModels.timeoutViewModel.startTimer(timerConfig.timeoutDurationMs)
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
            isFlagMode = true
        )
    }
}

private fun resolveActivePlayClockState(
    isFlagMode: Boolean,
    sevenSecondClockState: TimerState,
    playClockState: TimerState
): TimerState = if (isFlagMode && sevenSecondClockState in listOf(TimerState.Running, TimerState.Paused)) {
    sevenSecondClockState
} else {
    playClockState
}

private fun resolveActivePlayClockRemaining(
    isFlagMode: Boolean,
    sevenSecondClockState: TimerState,
    sevenSecondClockRemaining: Long,
    playClockRemaining: Long
): Long = if (isFlagMode && sevenSecondClockState in listOf(TimerState.Running, TimerState.Paused)) {
    sevenSecondClockRemaining
} else {
    playClockRemaining
}

private fun resolveFlagActiveButton(
    sevenSecondClockState: TimerState,
    playClockState: TimerState
): FlagActiveButton = when {
    sevenSecondClockState in listOf(TimerState.Running, TimerState.Paused) -> FlagActiveButton.SEVEN
    playClockState in listOf(TimerState.Running, TimerState.Paused) -> FlagActiveButton.TWENTY_FIVE
    else -> FlagActiveButton.NONE
}

private fun Long.isTwoMinutesWarning(config: TimerConfig = TimerConfigs.Default) =
    this in config.twoMinuteWarningEndMs..config.twoMinuteWarningStartMs

private fun Long.isFinalSeconds(config: TimerConfig = TimerConfigs.Default) =
    this <= config.playClockWarningMs
