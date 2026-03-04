package yt.javi.gridirontimer.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerRulesTest {

    @Test
    fun `isFlagMode true only for 20 minutes`() {
        assertTrue(TimerRules.isFlagMode(20L * 60L * 1000L))
        assertFalse(TimerRules.isFlagMode(12L * 60L * 1000L))
    }

    @Test
    fun `double press play clock duration follows flag and tackle rules`() {
        assertEquals(25_000L, TimerRules.playClockDurationOnDoublePress(true, TimerState.Running))
        assertEquals(40_000L, TimerRules.playClockDurationOnDoublePress(false, TimerState.Running))
        assertEquals(25_000L, TimerRules.playClockDurationOnDoublePress(false, TimerState.Paused))
        assertEquals(25_000L, TimerRules.playClockDurationOnDoublePress(false, TimerState.Idle))
    }

    @Test
    fun `play clock warning vibrates only in running and 10 second mark`() {
        assertTrue(TimerRules.shouldVibratePlayClockWarning(10_000L, TimerState.Running))
        assertTrue(TimerRules.shouldVibratePlayClockWarning(9_500L, TimerState.Running))
        assertFalse(TimerRules.shouldVibratePlayClockWarning(8_999L, TimerState.Running))
        assertFalse(TimerRules.shouldVibratePlayClockWarning(10_000L, TimerState.Paused))
    }

    @Test
    fun `timeout warnings vibrate at 10 and 5 seconds only while running`() {
        assertTrue(TimerRules.shouldVibrateTimeoutWarning(10_000L, TimerState.Running))
        assertTrue(TimerRules.shouldVibrateTimeoutWarning(5_000L, TimerState.Running))
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
}
