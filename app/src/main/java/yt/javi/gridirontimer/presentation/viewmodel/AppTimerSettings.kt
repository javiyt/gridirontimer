package yt.javi.gridirontimer.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppTimerSettings {
    var flagGameDurationMs by mutableStateOf(TimerConfigs.Default.flagGameDurationMs)
    var tackleGameDurationMs by mutableStateOf(12L * 60L * 1_000L)
    var timeoutDurationMs by mutableStateOf(TimerConfigs.Default.timeoutDurationMs)
    var twoMinuteWarningMs by mutableStateOf(TimerConfigs.Default.twoMinuteWarningStartMs)

    fun asTimerConfig(): TimerConfig = TimerConfigs.Default.copy(
        flagGameDurationMs = flagGameDurationMs,
        timeoutDurationMs = timeoutDurationMs,
        twoMinuteWarningStartMs = twoMinuteWarningMs,
        twoMinuteWarningEndMs = twoMinuteWarningMs - 5_000L
    )
}
