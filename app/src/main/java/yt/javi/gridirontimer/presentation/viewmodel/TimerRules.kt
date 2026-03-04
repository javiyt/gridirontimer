package yt.javi.gridirontimer.presentation.viewmodel

object TimerRules {
    fun isFlagMode(initialDuration: Long, config: TimerConfig = TimerConfigs.Default): Boolean =
        initialDuration == config.flagGameDurationMs

    fun playClockDurationOnDoublePress(
        isFlagMode: Boolean,
        gameClockState: TimerState,
        config: TimerConfig = TimerConfigs.Default
    ): Long {
        return if (isFlagMode) {
            config.flagPlayClockMs
        } else if (gameClockState is TimerState.Running) {
            config.tacklePlayClockRunningMs
        } else {
            config.flagPlayClockMs
        }
    }

    fun shouldVibratePlayClockWarning(
        remainingMs: Long,
        state: TimerState,
        config: TimerConfig = TimerConfigs.Default
    ): Boolean {
        return remainingMs.isWithinSecondMark(config.playClockWarningMs) && state is TimerState.Running
    }

    fun shouldVibrateTimeoutWarning(
        remainingMs: Long,
        state: TimerState,
        config: TimerConfig = TimerConfigs.Default
    ): Boolean {
        return state is TimerState.Running &&
            (
                remainingMs.isWithinSecondMark(config.timeoutWarningHighMs) ||
                    remainingMs.isWithinSecondMark(config.timeoutWarningLowMs)
                )
    }

    fun shouldVibrateOnSevenSecondFinish(isFlagMode: Boolean, sevenSecondState: TimerState): Boolean {
        return isFlagMode && sevenSecondState is TimerState.Finished
    }

    fun shouldVibrateOnTimeoutFinish(timeoutState: TimerState): Boolean {
        return timeoutState is TimerState.Finished
    }
}

private fun Long.isWithinSecondMark(markMs: Long) = this <= markMs && this > markMs - 1_000L
