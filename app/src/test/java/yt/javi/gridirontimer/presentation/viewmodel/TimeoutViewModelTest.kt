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
class TimeoutViewModelTest {

    @Test
    fun `startTimer moves to running then finishes`() {
        val viewModel = TimeoutViewModel()

        viewModel.startTimer()
        assertTrue(viewModel.state.value is TimerState.Running)

        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(61))
        assertTrue(viewModel.state.value is TimerState.Finished)
        assertEquals(0L, viewModel.time.value)
    }

    @Test
    fun `pause and resume update state`() {
        val viewModel = TimeoutViewModel()
        viewModel.startTimer()

        viewModel.pauseTimer()
        assertTrue(viewModel.state.value is TimerState.Paused)

        viewModel.resumeTimer()
        assertTrue(viewModel.state.value is TimerState.Running)
    }

    @Test
    fun `stopTimer from running sets idle`() {
        val viewModel = TimeoutViewModel()
        viewModel.startTimer()

        viewModel.stopTimer()

        assertTrue(viewModel.state.value is TimerState.Idle)
    }
}
