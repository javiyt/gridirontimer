package yt.javi.gridirontimer.presentation.viewmodel

data class TimerConfig(
    val tickIntervalMs: Long = 1_000L,
    val flagGameDurationMs: Long = 20L * 60L * 1_000L,
    val flagPlayClockMs: Long = 25_000L,
    val tacklePlayClockRunningMs: Long = 40_000L,
    val flagSevenSecondMs: Long = 7_000L,
    val timeoutDurationMs: Long = 60_000L,
    val twoMinuteWarningStartMs: Long = 120_000L,
    val twoMinuteWarningEndMs: Long = 115_000L,
    val playClockWarningMs: Long = 10_000L,
    val timeoutWarningHighMs: Long = 10_000L,
    val timeoutWarningLowMs: Long = 5_000L
)

object TimerConfigs {
    val Default = TimerConfig()

    // Useful for local UI/manual tests to speed up flows.
    val Fast = TimerConfig(
        tickIntervalMs = 100L,
        flagGameDurationMs = 2_000L,
        flagPlayClockMs = 1_500L,
        tacklePlayClockRunningMs = 2_000L,
        flagSevenSecondMs = 700L,
        timeoutDurationMs = 2_000L,
        twoMinuteWarningStartMs = 2_000L,
        twoMinuteWarningEndMs = 1_500L,
        playClockWarningMs = 1_000L,
        timeoutWarningHighMs = 1_000L,
        timeoutWarningLowMs = 500L
    )
}
