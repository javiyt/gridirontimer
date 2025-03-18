package yt.javi.gridirontimer.presentation.viewmodel

sealed class TimerState {
    object Idle : TimerState()
    object Running : TimerState()
    object Paused : TimerState()
    object Finished : TimerState()
}