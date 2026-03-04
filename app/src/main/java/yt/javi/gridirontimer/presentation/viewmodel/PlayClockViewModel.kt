package yt.javi.gridirontimer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayClockViewModel(
    private val countdownScheduler: CountdownScheduler = AndroidCountdownScheduler()
): ViewModel() {
    private var timer: CountdownHandle? = null

    private val _time = MutableStateFlow(0L)
    val time: StateFlow<Long> = _time.asStateFlow()

    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _presetDuration = MutableStateFlow<Long?>(null)
    val presetDuration: StateFlow<Long?> = _presetDuration.asStateFlow()

    fun startTimer(duration: Long) {
        startTimerInternal(duration, updatePreset = true)
    }

    private fun startTimerInternal(duration: Long, updatePreset: Boolean) {
        timer?.cancel()
        if (updatePreset) {
            _presetDuration.value = duration
        }
        _state.value = TimerState.Running
        timer = countdownScheduler.create(duration, 1_000L, onTick = { millisUntilFinished ->
                _time.value = millisUntilFinished
            }, onFinish = {
            _time.value = 0L
            _state.value = TimerState.Finished
        })
        timer?.start()
    }

    fun pauseTimer() {
        timer?.cancel()
        _state.value = TimerState.Paused
    }

    fun resumeTimer() {
        if (state.value == TimerState.Paused) {
            startTimerInternal(_time.value, updatePreset = false)
        }
    }

    fun cancelTimer() {
        timer?.cancel()
        _presetDuration.value = null
        _state.value = TimerState.Idle
    }

    fun stopAndReset(duration: Long) {
        timer?.cancel()
        _presetDuration.value = null
        _time.value = duration
        _state.value = TimerState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        timer = null
    }
}
