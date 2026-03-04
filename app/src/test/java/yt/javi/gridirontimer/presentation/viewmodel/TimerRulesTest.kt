package yt.javi.gridirontimer.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerRulesTest {

    @Test
    fun `isFlagMode true only for 20 minutes`() {
        val config = TimerConfigs.Default
        assertTrue(TimerRules.isFlagMode(config.flagGameDurationMs, config))
        assertFalse(TimerRules.isFlagMode(12L * 60L * 1000L, config))
    }

    @Test
    fun `double press play clock duration follows flag and tackle rules`() {
        val config = TimerConfigs.Default
        assertEquals(config.flagPlayClockMs, TimerRules.playClockDurationOnDoublePress(true, TimerState.Running, config))
        assertEquals(config.tacklePlayClockRunningMs, TimerRules.playClockDurationOnDoublePress(false, TimerState.Running, config))
        assertEquals(config.flagPlayClockMs, TimerRules.playClockDurationOnDoublePress(false, TimerState.Paused, config))
        assertEquals(config.flagPlayClockMs, TimerRules.playClockDurationOnDoublePress(false, TimerState.Idle, config))
    }

    @Test
    fun `play clock warning vibrates only in running and 10 second mark`() {
        val config = TimerConfigs.Default
        assertTrue(TimerRules.shouldVibratePlayClockWarning(10_000L, TimerState.Running, config))
        assertTrue(TimerRules.shouldVibratePlayClockWarning(9_500L, TimerState.Running))
        assertFalse(TimerRules.shouldVibratePlayClockWarning(8_999L, TimerState.Running))
        assertFalse(TimerRules.shouldVibratePlayClockWarning(10_000L, TimerState.Paused))
    }

    @Test
    fun `timeout warnings vibrate at 10 and 5 seconds only while running`() {
        val config = TimerConfigs.Default
        assertTrue(TimerRules.shouldVibrateTimeoutWarning(10_000L, TimerState.Running, config))
        assertTrue(TimerRules.shouldVibrateTimeoutWarning(5_000L, TimerState.Running, config))
        assertFalse(TimerRules.shouldVibrateTimeoutWarning(4_000L, TimerState.Running))
        assertFalse(TimerRules.shouldVibrateTimeoutWarning(10_000L, TimerState.Paused))
    }

    @Test
    fun `finish vibrations follow timeout and 7-second rules`() {
        assertTrue(TimerRules.shouldVibrateOnTimeoutFinish(TimerState.Finished))
        assertFalse(TimerRules.shouldVibrateOnTimeoutFinish(TimerState.Running))

        assertTrue(TimerRules.shouldVibrateOnSevenSecondFinish(true, TimerState.Finished))
        assertFalse(TimerRules.shouldVibrateOnSevenSecondFinish(false, TimerState.Finished))
        assertFalse(TimerRules.shouldVibrateOnSevenSecondFinish(true, TimerState.Running))
    }

    @Test
    fun `rules respect custom fast config thresholds`() {
        val fast = TimerConfigs.Fast
        assertTrue(TimerRules.shouldVibratePlayClockWarning(1_000L, TimerState.Running, fast))
        assertTrue(TimerRules.shouldVibrateTimeoutWarning(500L, TimerState.Running, fast))
        assertEquals(2_000L, TimerRules.playClockDurationOnDoublePress(false, TimerState.Running, fast))
    }
}
