package yt.javi.gridirontimer.presentation.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerViewModel : ViewModel() {

    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _time = MutableStateFlow(0L)
    val time: StateFlow<Long> = _time.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    fun startTimer(duration: Long) {
        _state.value = TimerState.Running
        countDownTimer = object : CountDownTimer(duration, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _time.value = millisUntilFinished
                if (millisUntilFinished == 0L){
                    onFinish()
                }
            }

            override fun onFinish() {
                _state.value = TimerState.Finished
            }
        }.start()
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _state.value = TimerState.Paused
    }

    fun resumeTimer() {
        if (state.value is TimerState.Paused) {
            startTimer(_time.value)
        }
        _state.value = TimerState.Running
    }
}