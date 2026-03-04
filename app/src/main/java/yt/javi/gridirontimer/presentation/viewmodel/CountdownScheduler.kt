package yt.javi.gridirontimer.presentation.viewmodel

import android.os.CountDownTimer

interface CountdownHandle {
    fun start()
    fun cancel()
}

interface CountdownScheduler {
    fun create(
        durationMs: Long,
        intervalMs: Long = 1_000L,
        onTick: (Long) -> Unit,
        onFinish: () -> Unit
    ): CountdownHandle
}

class AndroidCountdownScheduler : CountdownScheduler {
    override fun create(
        durationMs: Long,
        intervalMs: Long,
        onTick: (Long) -> Unit,
        onFinish: () -> Unit
    ): CountdownHandle {
        return object : CountdownHandle {
            private val timer = object : CountDownTimer(durationMs, intervalMs) {
                override fun onTick(millisUntilFinished: Long) = onTick(millisUntilFinished)
                override fun onFinish() = onFinish()
            }

            override fun start() {
                timer.start()
            }

            override fun cancel() {
                timer.cancel()
            }
        }
    }
}
