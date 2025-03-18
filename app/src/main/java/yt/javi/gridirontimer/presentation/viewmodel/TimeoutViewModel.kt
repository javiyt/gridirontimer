package yt.javi.gridirontimer.presentation.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimeoutViewModel: ViewModel() {
    private var timer: CountDownTimer? = null

    private val _time = MutableStateFlow(0L)
    val time: StateFlow<Long> = _time.asStateFlow()

    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    fun startTimer() {
        timer?.cancel()
        _state.value = TimerState.Running
        timer = object : CountDownTimer(60_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _time.value = millisUntilFinished
            }

            override fun onFinish() {
                _time.value = 0L
                _state.value = TimerState.Finished
            }
        }.start()
    }

    fun pauseTimer() {
        timer?.cancel()
        _state.value = TimerState.Paused
    }

    fun resumeTimer() {
        if (state.value == TimerState.Paused) {
            startTimer()
        }
    }

    fun stopTimer() {
        if (state.value in listOf(TimerState.Paused, TimerState.Running)) {
            timer?.cancel()
            _state.value = TimerState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        timer = null
    }
}