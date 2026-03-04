package yt.javi.gridirontimer.presentation.viewmodel

object TimerRules {
    const val FLAG_GAME_DURATION_MS = 20L * 60L * 1000L

    fun isFlagMode(initialDuration: Long): Boolean = initialDuration == FLAG_GAME_DURATION_MS

    fun playClockDurationOnDoublePress(isFlagMode: Boolean, gameClockState: TimerState): Long {
        return if (isFlagMode) {
            25_000L
        } else if (gameClockState is TimerState.Running) {
            40_000L
        } else {
            25_000L
        }
    }

    fun shouldVibratePlayClockWarning(remainingMs: Long, state: TimerState): Boolean {
        return remainingMs.isWithinSecondMark(10_000L) && state is TimerState.Running
    }

    fun shouldVibrateTimeoutWarning(remainingMs: Long, state: TimerState): Boolean {
        return state is TimerState.Running &&
            (remainingMs.isWithinSecondMark(10_000L) || remainingMs.isWithinSecondMark(5_000L))
    }

    fun shouldVibrateOnSevenSecondFinish(isFlagMode: Boolean, sevenSecondState: TimerState): Boolean {
        return isFlagMode && sevenSecondState is TimerState.Finished
    }

    fun shouldVibrateOnTimeoutFinish(timeoutState: TimerState): Boolean {
        return timeoutState is TimerState.Finished
    }
}

private fun Long.isWithinSecondMark(markMs: Long) = this <= markMs && this > markMs - 1_000L
