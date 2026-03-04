package yt.javi.gridirontimer.presentation.viewmodel

import android.os.Looper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
class PlayClockViewModelTest {

    @Test
    fun `startTimer moves to running then finishes with zero time`() {
        val viewModel = PlayClockViewModel()

        viewModel.startTimer(2_000L)
        assertTrue(viewModel.state.value is TimerState.Running)

        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(2_100))
        assertTrue(viewModel.state.value is TimerState.Finished)
        assertEquals(0L, viewModel.time.value)
    }

    @Test
    fun `pause and resume keep timer active`() {
        val viewModel = PlayClockViewModel()
        viewModel.startTimer(5_000L)
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1_100))

        viewModel.pauseTimer()
        assertTrue(viewModel.state.value is TimerState.Paused)

        viewModel.resumeTimer()
        assertTrue(viewModel.state.value is TimerState.Running)
    }

    @Test
    fun `cancelTimer sets idle`() {
        val viewModel = PlayClockViewModel()
        viewModel.startTimer(5_000L)

        viewModel.cancelTimer()

        assertTrue(viewModel.state.value is TimerState.Idle)
    }

    @Test
    fun `stopAndReset sets idle and resets configured duration`() {
        val viewModel = PlayClockViewModel()
        viewModel.startTimer(5_000L)

        viewModel.stopAndReset(7_000L)

        assertTrue(viewModel.state.value is TimerState.Idle)
        assertEquals(7_000L, viewModel.time.value)
    }
}
