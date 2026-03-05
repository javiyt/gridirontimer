package yt.javi.gridirontimer.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class AppTimerSettingsTest {

    @Test
    fun `asTimerConfig reflects updated flag and timeout values`() {
        val originalFlag = AppTimerSettings.flagGameDurationMs
        val originalTackle = AppTimerSettings.tackleGameDurationMs
        val originalTimeout = AppTimerSettings.timeoutDurationMs

        try {
            AppTimerSettings.flagGameDurationMs = 900_000L
            AppTimerSettings.tackleGameDurationMs = 600_000L
            AppTimerSettings.timeoutDurationMs = 45_000L

            val config = AppTimerSettings.asTimerConfig()

            assertEquals(900_000L, config.flagGameDurationMs)
            assertEquals(45_000L, config.timeoutDurationMs)
            assertEquals(TimerConfigs.Default.flagPlayClockMs, config.flagPlayClockMs)
        } finally {
            AppTimerSettings.flagGameDurationMs = originalFlag
            AppTimerSettings.tackleGameDurationMs = originalTackle
            AppTimerSettings.timeoutDurationMs = originalTimeout
        }
    }

    @Test
    fun `settings values can be updated independently`() {
        val originalFlag = AppTimerSettings.flagGameDurationMs
        val originalTackle = AppTimerSettings.tackleGameDurationMs
        val originalTimeout = AppTimerSettings.timeoutDurationMs

        try {
            AppTimerSettings.flagGameDurationMs = 1_500_000L
            AppTimerSettings.tackleGameDurationMs = 900_000L
            AppTimerSettings.timeoutDurationMs = 30_000L

            assertEquals(1_500_000L, AppTimerSettings.flagGameDurationMs)
            assertEquals(900_000L, AppTimerSettings.tackleGameDurationMs)
            assertEquals(30_000L, AppTimerSettings.timeoutDurationMs)
        } finally {
            AppTimerSettings.flagGameDurationMs = originalFlag
            AppTimerSettings.tackleGameDurationMs = originalTackle
            AppTimerSettings.timeoutDurationMs = originalTimeout
        }
    }
}
