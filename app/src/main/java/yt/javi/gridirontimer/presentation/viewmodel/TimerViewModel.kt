package yt.javi.gridirontimer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerViewModel(
    private val countdownScheduler: CountdownScheduler = AndroidCountdownScheduler()
) : ViewModel() {

    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _time = MutableStateFlow(0L)
    val time: StateFlow<Long> = _time.asStateFlow()

    private var countDownTimer: CountdownHandle? = null

    fun startTimer(duration: Long) {
        countDownTimer?.cancel()
        _state.value = TimerState.Running
        countDownTimer = countdownScheduler.create(duration, 1_000L, onTick = { millisUntilFinished ->
                _time.value = millisUntilFinished
                if (millisUntilFinished == 0L){
                    _state.value = TimerState.Finished
                }
            }, onFinish = {
            _time.value = 0L
            _state.value = TimerState.Finished
        })
        countDownTimer?.start()
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _state.value = TimerState.Paused
    }

    fun resumeTimer() {
        if (state.value is TimerState.Paused) {
            startTimer(_time.value)
        } else {
            _state.value = TimerState.Running
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        countDownTimer = null
    }
}
